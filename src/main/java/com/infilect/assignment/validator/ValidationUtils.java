package com.infilect.assignment.validator;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,32}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches() && email.length() <= 254;
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) return true; // Optional field
        if (phone.length() > 32) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static String normalize(String value) {
        if (value == null) return null;
        return value.trim();
    }

    public static boolean isValidUserType(Integer userType) {
        if (userType == null) return true; // Default is 1
        return userType == 1 || userType == 2 || userType == 3 || userType == 7;
    }

    public static boolean isValidString(String value, int maxLength) {
        if (value == null) return false;
        return value.length() > 0 && value.length() <= maxLength;
    }

    public static boolean isValidFloat(String value) {
        if (value == null || value.isBlank()) return true; // Optional field
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Float parseFloat(String value) {
        if (value == null || value.isBlank()) return 0.0f;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.isBlank()) return true; // Optional field
        try {
            java.time.LocalDate.parse(dateString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static java.time.LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer parseInteger(String value, Integer defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}
