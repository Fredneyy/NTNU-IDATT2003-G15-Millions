package ntnu.idatt2003.group15.view.dialog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import ntnu.idatt2003.group15.view.JavaFxTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Construction smoke tests for the simpler dialogs. We don't drive show/close
 * here because that requires attaching to a Scene/Stage which the headless test
 * runner can't host. Even so, exercising the constructor exercises the bulk of
 * each dialog's wiring.
 */
class DialogSmokeTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void infoDialogConstructorAndSetText() {
    JavaFxTestSupport.runAndWait(() -> {
      InfoDialog dialog = new InfoDialog();
      dialog.setText("Title", "Body");
      // Calling setText twice exercises the relabel path on existing nodes.
      dialog.setText("Title 2", "Body 2");
    });
  }

  @Test
  void exceptionDialogConstructorAndSetText() {
    JavaFxTestSupport.runAndWait(() -> {
      ExceptionDialog dialog = new ExceptionDialog();
      dialog.setText("Oops", "Something went wrong");
    });
  }

  @Test
  void infoDialogCloseBeforeShowIsSafe() {
    JavaFxTestSupport.runAndWait(() -> {
      InfoDialog dialog = new InfoDialog();
      assertDoesNotThrow(dialog::close);
    });
  }
}
