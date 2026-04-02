package com.infilect.assignment.dto;

import java.util.ArrayList;
import java.util.List;

public class MappingCSVRow {
    public String username;
    public String storeId;
    public String date;
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

        if (username == null || username.isBlank()) {
            errors.add("username is required");
        }

        if (storeId == null || storeId.isBlank()) {
            errors.add("storeId is required");
        }

        if (date != null && !date.isBlank()) {
            if (!com.infilect.assignment.validator.ValidationUtils.isValidDate(date)) {
                errors.add("date must be in format YYYY-MM-DD");
            }
        }

        return new Validation(errors.isEmpty(), errors);
    }
}
