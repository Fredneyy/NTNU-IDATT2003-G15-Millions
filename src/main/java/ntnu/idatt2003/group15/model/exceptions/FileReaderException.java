package ntnu.idatt2003.group15.model.exceptions;

/**
 * Thrown when an error occurs during file reading operations.
 */
public class FileReaderException extends RuntimeException {

  private final Throwable cause;

  /**
   * Create a new instance of file reader exception.
   *
   * @param message the message of the exception
   * @param cause the root cause
   */
  public FileReaderException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  /**
   * Returns the cause of the exception.
   *
   * @return throwable cause
   */
  public Throwable getCause() {
    return cause;
  }
}
