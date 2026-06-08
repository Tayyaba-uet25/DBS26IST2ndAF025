package hms.util;

import java.util.regex.Pattern;

/**
 * Reusable input validators. Throws ValidationException with a clear
 * message that the UI shows to the user.
 * SOFTWARE CLASS #4
 */
public final class ValidationUtil {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE =
            Pattern.compile("^[0-9+\\-\\s]{7,20}$");

    private ValidationUtil() { }

    public static void requireText(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required.");
        }
    }

    public static void maxLength(String value, int max, String field) throws ValidationException {
        if (value != null && value.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters.");
        }
    }

    public static void email(String value, String field) throws ValidationException {
        if (value != null && !value.trim().isEmpty() && !EMAIL.matcher(value.trim()).matches()) {
            throw new ValidationException(field + " is not a valid email address.");
        }
    }

    public static void phone(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) return;          // phone is optional
        String v = value.trim();
        if (!PHONE.matcher(v).matches()) {
            throw new ValidationException(field + " can only contain digits, +, - and spaces.");
        }
        int digits = v.replaceAll("\\D", "").length();                // count actual digits
        if (digits < 10 || digits > 15) {
            throw new ValidationException(field + " must have 10 to 15 digits.");
        }
    }

    public static double positiveOrZeroNumber(String value, String field) throws ValidationException {
        try {
            double d = Double.parseDouble(value.trim());
            if (d < 0) {
                throw new ValidationException(field + " cannot be negative.");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a valid number.");
        }
    }

    public static int positiveInt(String value, String field) throws ValidationException {
        try {
            int i = Integer.parseInt(value.trim());
            if (i <= 0) {
                throw new ValidationException(field + " must be greater than zero.");
            }
            return i;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    public static int intOrZero(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            int i = Integer.parseInt(value.trim());
            if (i < 0) {
                throw new ValidationException(field + " cannot be negative.");
            }
            return i;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    public static void requireSelection(Object value, String field) throws ValidationException {
        if (value == null) {
            throw new ValidationException("Please select a " + field + ".");
        }
    }
}
