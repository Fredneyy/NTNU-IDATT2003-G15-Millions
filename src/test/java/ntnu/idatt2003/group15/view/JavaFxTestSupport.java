package ntnu.idatt2003.group15.view;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;

/**
 * Boots the JavaFX toolkit once for the test process.
 *
 * <p>When the host has no display available (e.g. a headless GitHub Actions
 * runner without Xvfb), {@link Platform#startup} throws and we abort the test
 * via {@link Assumptions#abort} so it shows up as skipped, not failed. That
 * keeps view-dependent tests useful on a developer machine while letting CI
 * pass without extra display setup.
 */
public final class JavaFxTestSupport {

  private static boolean started = false;
  private static boolean unavailable = false;
  private static String unavailableReason;

  private JavaFxTestSupport() {}

  public static synchronized void ensureStarted() {
    if (unavailable) {
      Assumptions.abort("JavaFX toolkit unavailable: " + unavailableReason);
    }
    if (started) {
      return;
    }
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await();
    } catch (IllegalStateException alreadyRunning) {
      // Platform.startup was called by an earlier test. That's fine.
    } catch (UnsupportedOperationException | LinkageError noDisplay) {
      // Headless CI without a usable display — skip cleanly so the build still
      // passes. The same JVM will skip subsequent ensureStarted() calls too.
      unavailable = true;
      unavailableReason = noDisplay.getMessage();
      Assumptions.abort("JavaFX toolkit unavailable: " + unavailableReason);
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
