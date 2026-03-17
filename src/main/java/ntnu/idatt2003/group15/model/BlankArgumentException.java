package ntnu.idatt2003.group15.model;

/**
 * If argument is blank
 */
public class BlankArgumentException extends RuntimeException {
    public BlankArgumentException(String message) {
        super(message);
    }
}
