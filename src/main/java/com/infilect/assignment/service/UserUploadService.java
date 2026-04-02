package com.infilect.assignment.service;

import com.infilect.assignment.dto.*;
import com.infilect.assignment.entity.*;
import com.infilect.assignment.repository.*;
import com.infilect.assignment.validator.ValidationUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

@Service
public class UserUploadService {
    private static final Logger logger = Logger.getLogger(UserUploadService.class.getName());
    private static final int MAX_ERRORS_TO_RETURN = 1000;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UploadResponse uploadUsers(InputStream fileInputStream, String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        List<RowError> errors = new ArrayList<>();
        List<UserCSVRow> validUsers = new ArrayList<>();

        int rowNumber = 0;
        Set<String> seenUsernames = new HashSet<>();
        List<UserCSVRow> rows = new ArrayList<>();

        // Parse CSV
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : csvParser) {
                rowNumber = (int) record.getRecordNumber();

                try {
                    UserCSVRow row = new UserCSVRow();
                    row.username = normalizeField(safeGet(record, "username"));
                    row.firstName = normalizeField(safeGet(record, "first_name"));
                    row.lastName = normalizeField(safeGet(record, "last_name"));
                    row.email = normalizeField(safeGet(record, "email"));
                    row.userType = normalizeField(safeGet(record, "user_type"));
                    row.phoneNumber = normalizeField(safeGet(record, "phone_number"));
                    row.supervisorUsername = normalizeField(safeGet(record, "supervisor_username"));
                    row.isActive = safeGet(record, "is_active");

                    rows.add(row);

                } catch (IllegalArgumentException e) {
                    errors.add(new RowError(rowNumber, "CSV", "Missing required column or malformed row", null));
                }
            }
        }

        // First pass: validate format
        for (int i = 0; i < rows.size(); i++) {
            UserCSVRow row = rows.get(i);
            rowNumber = i + 2; // +2 for header and 0-indexing

            UserCSVRow.Validation validation = row.validate(rowNumber);
            if (!validation.isValid()) {
                for (String error : validation.getErrors()) {
                    errors.add(new RowError(rowNumber, "multiple", error, null));
                }
                continue;
            }

            // Check for duplicate username in same file
            if (seenUsernames.contains(row.username)) {
                errors.add(new RowError(rowNumber, "username", "Duplicate username within file", row.username));
                continue;
            }
            seenUsernames.add(row.username);

            // Check for duplicate in database
            if (userRepository.findByUsername(row.username).isPresent()) {
                errors.add(new RowError(rowNumber, "username", "User already exists in database", row.username));
                continue;
            }

            validUsers.add(row);
        }

        // Second pass: resolve supervisor references (only for valid users)
        Map<String, User> createdUsers = new HashMap<>();
        List<User> usersToSave = new ArrayList<>();

        for (UserCSVRow row : validUsers) {
            User supervisor = null;
            if (row.supervisorUsername != null && !row.supervisorUsername.isBlank()) {
                // Check if supervisor exists in database or was created in this batch
                Optional<User> supOpt = userRepository.findByUsername(row.supervisorUsername);
                if (supOpt.isPresent()) {
                    supervisor = supOpt.get();
                } else if (createdUsers.containsKey(row.supervisorUsername)) {
                    supervisor = createdUsers.get(row.supervisorUsername);
                }

                if (supervisor == null) {
                    int errorRowNum = validUsers.indexOf(row) + 2;
                    errors.add(new RowError(errorRowNum, "supervisor_username", "Supervisor does not exist: " + row.supervisorUsername, row.supervisorUsername));
                    continue;
                }
            }

            User user = buildUser(row, supervisor);
            usersToSave.add(user);
            createdUsers.put(row.username, user);
        }

        // Ingest valid users
        int successCount = 0;
        if (!usersToSave.isEmpty()) {
            try {
                userRepository.saveAll(usersToSave);
                successCount = usersToSave.size();
                logger.info("Inserted " + successCount + " users");
            } catch (Exception e) {
                logger.severe("Error ingesting users: " + e.getMessage());
                errors.add(new RowError(-1, "DATABASE", "Failed to ingest users: " + e.getMessage(), null));
            }
        }

        long endTime = System.currentTimeMillis();
        String status = errors.isEmpty() ? "SUCCESS" : (successCount > 0 ? "PARTIAL_SUCCESS" : "FAILURE");

        // Truncate errors if too many
        boolean errorsTruncated = errors.size() > MAX_ERRORS_TO_RETURN;
        List<RowError> returnedErrors = new ArrayList<>();
        if (errorsTruncated) {
            returnedErrors = errors.subList(0, MAX_ERRORS_TO_RETURN);
            logger.warning("Truncated " + (errors.size() - MAX_ERRORS_TO_RETURN) + " errors from response. Total errors: " + errors.size());
        } else {
            returnedErrors = errors;
        }

        UploadResponse response = new UploadResponse();
        response.setFileId(UUID.randomUUID().toString());
        response.setFilename(filename);
        response.setStatus(status);
        response.setTotalRows(rows.size());
        response.setSuccessRows(successCount);
        response.setFailureRows(rows.size() - successCount);
        response.setErrors(returnedErrors);
        response.setTotalErrorCount(errors.size());
        response.setErrorsTruncated(errorsTruncated);
        response.setProcessingTimeMs(endTime - startTime);

        return response;
    }

    private User buildUser(UserCSVRow row, User supervisor) {
        Integer userType = ValidationUtils.parseInteger(row.userType, 1);

        User user = new User();
        user.setUsername(row.username);
        user.setFirstName(row.firstName != null ? row.firstName : "");
        user.setLastName(row.lastName != null ? row.lastName : "");
        user.setEmail(row.email);
        user.setUserType(userType);
        user.setPhoneNumber(row.phoneNumber != null ? row.phoneNumber : "");
        user.setSupervisor(supervisor);
        user.setIsActive(ValidationUtils.parseBoolean(row.isActive, true));

        return user;
    }

    private String normalizeField(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String safeGet(CSVRecord record, String name) {
        try {
            return record.get(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
