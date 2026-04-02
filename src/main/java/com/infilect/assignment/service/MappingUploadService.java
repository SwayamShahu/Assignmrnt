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
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@Service
public class MappingUploadService {
    private static final Logger logger = Logger.getLogger(MappingUploadService.class.getName());
    private static final int MAX_ERRORS_TO_RETURN = 1000;

    @Autowired
    private PermanentJourneyPlanRepository pjpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Transactional
    public UploadResponse uploadMappings(InputStream fileInputStream, String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        List<RowError> errors = new ArrayList<>();
        List<PermanentJourneyPlan> validMappings = new ArrayList<>();

        int rowNumber = 0;
        Set<String> seenMappings = new HashSet<>();
        List<MappingCSVRow> rows = new ArrayList<>();

        // Parse CSV
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : csvParser) {
                rowNumber = (int) record.getRecordNumber();

                try {
                    MappingCSVRow row = new MappingCSVRow();
                    row.username = normalizeField(safeGet(record, "username"));
                    row.storeId = normalizeField(safeGet(record, "store_id"));
                    row.date = normalizeField(safeGet(record, "date"));
                    row.isActive = safeGet(record, "is_active");

                    rows.add(row);

                } catch (IllegalArgumentException e) {
                    errors.add(new RowError(rowNumber, "CSV", "Missing required column or malformed row", null));
                }
            }
        }

        // Validate and resolve references
        for (int i = 0; i < rows.size(); i++) {
            MappingCSVRow row = rows.get(i);
            rowNumber = i + 2; // +2 for header and 0-indexing

            MappingCSVRow.Validation validation = row.validate(rowNumber);
            if (!validation.isValid()) {
                for (String error : validation.getErrors()) {
                    errors.add(new RowError(rowNumber, "multiple", error, null));
                }
                continue;
            }

            // Look up user
            Optional<User> userOpt = userRepository.findByUsername(row.username);
            if (!userOpt.isPresent()) {
                errors.add(new RowError(rowNumber, "username", "User not found: " + row.username, row.username));
                continue;
            }

            // Look up store
            Optional<Store> storeOpt = storeRepository.findByStoreId(row.storeId);
            if (!storeOpt.isPresent()) {
                errors.add(new RowError(rowNumber, "store_id", "Store not found: " + row.storeId, row.storeId));
                continue;
            }

            User user = userOpt.get();
            Store store = storeOpt.get();
            LocalDate date = ValidationUtils.parseDate(row.date);

            // Check for duplicate in same file
            String mapKey = user.getId() + "_" + store.getId() + "_" + date;
            if (seenMappings.contains(mapKey)) {
                errors.add(new RowError(rowNumber, "multiple", "Duplicate mapping within file (user, store, date)", null));
                continue;
            }
            seenMappings.add(mapKey);

            // Check for duplicate in database
            if (pjpRepository.existsByUserIdAndStoreIdAndDate(user.getId(), store.getId(), date)) {
                errors.add(new RowError(rowNumber, "multiple", "Mapping already exists in database", null));
                continue;
            }

            try {
                PermanentJourneyPlan pjp = new PermanentJourneyPlan();
                pjp.setUser(user);
                pjp.setStore(store);
                pjp.setDate(date);
                pjp.setIsActive(ValidationUtils.parseBoolean(row.isActive, true));

                validMappings.add(pjp);
            } catch (Exception e) {
                errors.add(new RowError(rowNumber, "multiple", "Error creating mapping: " + e.getMessage(), null));
            }
        }

        // Ingest valid mappings
        int successCount = 0;
        if (!validMappings.isEmpty()) {
            try {
                pjpRepository.saveAll(validMappings);
                successCount = validMappings.size();
                logger.info("Inserted " + successCount + " mappings");
            } catch (Exception e) {
                logger.severe("Error ingesting mappings: " + e.getMessage());
                errors.add(new RowError(-1, "DATABASE", "Failed to ingest mappings: " + e.getMessage(), null));
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
