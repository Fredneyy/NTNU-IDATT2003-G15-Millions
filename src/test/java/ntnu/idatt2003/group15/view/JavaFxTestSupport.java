package ntnu.idatt2003.group15.view;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;

/**
 * Boots the JavaFX toolkit once for the test process.
 * Surefire is already configured for headless monocle in pom.xml;
 * this just makes sure {@link Platform} is initialized before any view
 * constructor that touches CSS / scene graph runs.
 */
public final class JavaFxTestSupport {

  private static boolean started = false;

  private JavaFxTestSupport() {}

  public static synchronized void ensureStarted() {
    if (started) {
      return;
    }
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await();
    } catch (IllegalStateException alreadyRunning) {
      // Platform.startup was called by an earlier test. That's fine.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while booting JavaFX", e);
    }
    started = true;
  }

  /** Run {@code action} on the JavaFX application thread and wait for it. */
  public static void runAndWait(Runnable action) {
    if (Platform.isFxApplicationThread()) {
      action.run();
      return;
    }
    CountDownLatch latch = new CountDownLatch(1);
    Throwable[] error = new Throwable[1];
    Platform.runLater(() -> {
      try {
        action.run();
      } catch (Throwable t) {
        error[0] = t;
      } finally {
        latch.countDown();
      }
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while waiting for FX action", e);
    }
    if (error[0] instanceof RuntimeException re) throw re;
    if (error[0] instanceof Error err) throw err;
    if (error[0] != null) throw new RuntimeException(error[0]);
  }
}
