package com.infilect.assignment.dto;

import java.util.ArrayList;
import java.util.List;

public class UserCSVRow {
    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public String userType;
    public String phoneNumber;
    public String supervisorUsername;
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
        } else if (username.length() > 150) {
            errors.add("username exceeds max length 150");
        }

        if (email == null || email.isBlank()) {
            errors.add("email is required");
        } else if (!com.infilect.assignment.validator.ValidationUtils.isValidEmail(email)) {
            errors.add("email format is invalid");
        }

        if (userType != null && !userType.isBlank()) {
            Integer type = com.infilect.assignment.validator.ValidationUtils.parseInteger(userType, null);
            if (type == null || !com.infilect.assignment.validator.ValidationUtils.isValidUserType(type)) {
                errors.add("userType must be one of: 1, 2, 3, 7");
            }
        }

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (!com.infilect.assignment.validator.ValidationUtils.isValidPhoneNumber(phoneNumber)) {
                errors.add("phoneNumber format is invalid");
            }
        }

        if (firstName != null && firstName.length() > 150) {
            errors.add("firstName exceeds max length 150");
        }

        if (lastName != null && lastName.length() > 150) {
            errors.add("lastName exceeds max length 150");
        }

        return new Validation(errors.isEmpty(), errors);
    }
}
