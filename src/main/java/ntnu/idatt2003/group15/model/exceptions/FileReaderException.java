package ntnu.idatt2003.group15.model.exceptions;

/**
 * Thrown when an error occurs during file reading operations.
 */
public class FileReaderException extends RuntimeException {

  private final Throwable cause;

  /**
   * Executes the  operation to manage market logic.
   * @param message  
   * @param cause  
   */
  public FileReaderException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  /**
   * Returns the current value of the .
   * @return 
   */
  public Throwable getCause() {
    return cause;
  }
}
