package hms.util;

/** Thrown when user input fails validation. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
