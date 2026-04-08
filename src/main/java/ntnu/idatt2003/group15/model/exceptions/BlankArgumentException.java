package ntnu.idatt2003.group15.model.exceptions;

/**
 * Thrown to indicate that a required string argument is blank or empty.
 */
public class BlankArgumentException extends RuntimeException {
    /**
     * Executes the  operation to manage market logic.
     * @param message the message of the exception
     */
    public BlankArgumentException(String message) {
        super(message);
    }
}
