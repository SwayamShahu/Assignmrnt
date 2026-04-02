package com.infilect.assignment.dto;

import java.util.ArrayList;
import java.util.List;

public class StoreCSVRow {
    public String storeId;
    public String storeExternalId;
    public String name;
    public String title;
    public String storeBrand;
    public String storeType;
    public String city;
    public String state;
    public String country;
    public String region;
    public String latitude;
    public String longitude;
    public String isActive;

    public static class Validation {
        private boolean valid;
        private List<String> errors;

        public Validation(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }

    public Validation validate(int rowNumber) {
        List<String> errors = new ArrayList<>();

        if (storeId == null || storeId.isBlank()) {
            errors.add("storeId is required");
        } else if (storeId.length() > 255) {
            errors.add("storeId exceeds max length 255");
        }

        if (name == null || name.isBlank()) {
            errors.add("name is required");
        } else if (name.length() > 255) {
            errors.add("name exceeds max length 255");
        }

        if (title == null || title.isBlank()) {
            errors.add("title is required");
        } else if (title.length() > 255) {
            errors.add("title exceeds max length 255");
        }

        if (storeExternalId != null && storeExternalId.length() > 255) {
            errors.add("storeExternalId exceeds max length 255");
        }

        if (latitude != null && !latitude.isBlank()) {
            if (!com.infilect.assignment.validator.ValidationUtils.isValidFloat(latitude)) {
                errors.add("latitude must be a valid float");
            }
        }

        if (longitude != null && !longitude.isBlank()) {
            if (!com.infilect.assignment.validator.ValidationUtils.isValidFloat(longitude)) {
                errors.add("longitude must be a valid float");
            }
        }

        return new Validation(errors.isEmpty(), errors);
    }
}
