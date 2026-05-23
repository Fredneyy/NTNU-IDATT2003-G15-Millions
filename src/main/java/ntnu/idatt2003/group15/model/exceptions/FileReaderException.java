package ntnu.idatt2003.group15.model.exceptions;

/**
 * Thrown when an error occurs during file reading operations.
 */
public class FileReaderException extends RuntimeException {

  /**
   * Create a new instance of file reader exception.
   *
   * @param message the message of the exception
   */
  public FileReaderException(String message) {
    super(message);
  }
}
