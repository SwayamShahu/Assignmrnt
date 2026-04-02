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
public class StoreUploadService {
    private static final Logger logger = Logger.getLogger(StoreUploadService.class.getName());
    private static final int MAX_ERRORS_TO_RETURN = 1000;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreBrandRepository storeBrandRepository;

    @Autowired
    private StoreTypeRepository storeTypeRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Transactional
    public UploadResponse uploadStores(InputStream fileInputStream, String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        List<RowError> errors = new ArrayList<>();
        List<Store> validStores = new ArrayList<>();

        int rowNumber = 0;
        Set<String> seenStoreIds = new HashSet<>();
        List<StoreCSVRow> rows = new ArrayList<>();

        // Parse CSV
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
             CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {

            for (CSVRecord record : csvParser) {
                rowNumber = (int) record.getRecordNumber();

                try {
                    StoreCSVRow row = new StoreCSVRow();
                    row.storeId = normalizeField(safeGet(record, "store_id"));
                    row.storeExternalId = normalizeField(safeGet(record, "store_external_id"));
                    row.name = normalizeField(safeGet(record, "name"));
                    row.title = normalizeField(safeGet(record, "title"));
                    row.storeBrand = normalizeField(safeGet(record, "store_brand"));
                    row.storeType = normalizeField(safeGet(record, "store_type"));
                    row.city = normalizeField(safeGet(record, "city"));
                    row.state = normalizeField(safeGet(record, "state"));
                    row.country = normalizeField(safeGet(record, "country"));
                    row.region = normalizeField(safeGet(record, "region"));
                    row.latitude = normalizeField(safeGet(record, "latitude"));
                    row.longitude = normalizeField(safeGet(record, "longitude"));
                    row.isActive = safeGet(record, "is_active");

                    rows.add(row);

                } catch (IllegalArgumentException e) {
                    RowError err = new RowError(rowNumber, "CSV", "Missing required column or malformed row", null);
                    errors.add(err);
                }
            }
        }

        // Validate rows
        for (int i = 0; i < rows.size(); i++) {
            StoreCSVRow row = rows.get(i);
            rowNumber = i + 2; // +2 because of header and 0-indexing

            StoreCSVRow.Validation validation = row.validate(rowNumber);
            if (!validation.isValid()) {
                for (String error : validation.getErrors()) {
                    errors.add(new RowError(rowNumber, "multiple", error, null));
                }
                continue;
            }

            // Check for duplicate store_id in same file
            if (seenStoreIds.contains(row.storeId)) {
                errors.add(new RowError(rowNumber, "store_id", "Duplicate store_id within file", row.storeId));
                continue;
            }
            seenStoreIds.add(row.storeId);

            // Check for duplicate in database
            if (storeRepository.findByStoreId(row.storeId).isPresent()) {
                errors.add(new RowError(rowNumber, "store_id", "Store already exists in database", row.storeId));
                continue;
            }

            try {
                Store store = buildStore(row);
                validStores.add(store);
            } catch (Exception e) {
                errors.add(new RowError(rowNumber, "multiple", "Error building store: " + e.getMessage(), null));
            }
        }

        // Ingest valid stores
        int successCount = 0;
        if (!validStores.isEmpty()) {
            try {
                storeRepository.saveAll(validStores);
                successCount = validStores.size();
                logger.info("Inserted " + successCount + " stores");
            } catch (Exception e) {
                logger.severe("Error ingesting stores: " + e.getMessage());
                errors.add(new RowError(-1, "DATABASE", "Failed to ingest stores: " + e.getMessage(), null));
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

    private Store buildStore(StoreCSVRow row) {
        Store store = new Store();
        store.setStoreId(row.storeId);
        store.setStoreExternalId(row.storeExternalId != null ? row.storeExternalId : "");
        store.setName(row.name);
        store.setTitle(row.title);
        store.setLatitude(ValidationUtils.parseFloat(row.latitude));
        store.setLongitude(ValidationUtils.parseFloat(row.longitude));
        store.setIsActive(ValidationUtils.parseBoolean(row.isActive, true));

        // Get-or-create lookup references
        if (row.storeBrand != null && !row.storeBrand.isBlank()) {
            store.setStoreBrand(getOrCreateStoreBrand(row.storeBrand));
        }

        if (row.storeType != null && !row.storeType.isBlank()) {
            store.setStoreType(getOrCreateStoreType(row.storeType));
        }

        if (row.city != null && !row.city.isBlank()) {
            store.setCity(getOrCreateCity(row.city));
        }

        if (row.state != null && !row.state.isBlank()) {
            store.setState(getOrCreateState(row.state));
        }

        if (row.country != null && !row.country.isBlank()) {
            store.setCountry(getOrCreateCountry(row.country));
        }

        if (row.region != null && !row.region.isBlank()) {
            store.setRegion(getOrCreateRegion(row.region));
        }

        return store;
    }

    private StoreBrand getOrCreateStoreBrand(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<StoreBrand> existing = storeBrandRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        StoreBrand brand = new StoreBrand();
        brand.setName(normalized);
        return storeBrandRepository.save(brand);
    }

    private StoreType getOrCreateStoreType(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<StoreType> existing = storeTypeRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        StoreType type = new StoreType();
        type.setName(normalized);
        return storeTypeRepository.save(type);
    }

    private City getOrCreateCity(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<City> existing = cityRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        City city = new City();
        city.setName(normalized);
        return cityRepository.save(city);
    }

    private State getOrCreateState(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<State> existing = stateRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        State state = new State();
        state.setName(normalized);
        return stateRepository.save(state);
    }

    private Country getOrCreateCountry(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<Country> existing = countryRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        Country country = new Country();
        country.setName(normalized);
        return countryRepository.save(country);
    }

    private Region getOrCreateRegion(String name) {
        String normalized = ValidationUtils.normalize(name);
        Optional<Region> existing = regionRepository.findByNameIgnoreCase(normalized);
        if (existing.isPresent()) {
            return existing.get();
        }
        Region region = new Region();
        region.setName(normalized);
        return regionRepository.save(region);
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
