package ntnu.idatt2003.group15.model.exceptions;

/**
 * Thrown to indicate that a required string argument is blank or empty.
 */
public class BlankArgumentException extends RuntimeException {
  /**
   * Create a new instance of BlankArgumentException.
   *
   * @param message the message of the exception
   */
  public BlankArgumentException(String message) {
    super(message);
  }
}
