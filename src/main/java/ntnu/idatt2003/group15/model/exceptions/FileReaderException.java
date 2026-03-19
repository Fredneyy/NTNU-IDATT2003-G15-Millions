package ntnu.idatt2003.group15.model.exceptions;

public class FileReaderException extends RuntimeException {

  private final Throwable cause;

  public FileReaderException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  public Throwable getCause() {
    return cause;
  }
}
