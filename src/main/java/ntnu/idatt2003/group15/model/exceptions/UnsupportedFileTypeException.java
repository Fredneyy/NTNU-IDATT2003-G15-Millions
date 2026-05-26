package ntnu.idatt2003.group15.model.exceptions;

/**
 * An exception for a loader if the file type is not supported.
 */
public class UnsupportedFileTypeException extends RuntimeException {
  /**
   * Create a new instance of UnsupportedFileTypeException.
   *
   * @param message the message
   */
  public UnsupportedFileTypeException(String message) {
    super(message);
  }
}
