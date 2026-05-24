package ntnu.idatt2003.group15.utilities;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * The task util for running background tasks in program.
 *
 * @author ole
 */
public class TaskUtil {

  private ExecutorService executor;

  /**
   * Sets up executor service.
   *
   * @param threads the amount of thread in pool
   * @throws IllegalArgumentException if threads <= 0
   */
  public void init(int threads) throws IllegalArgumentException {
    executor = Executors.newFixedThreadPool(threads);
  }

  /**
   * Run a task.
   *
   * <p>Takes in a callable for running something on a background thread. The result is then
   * used in the onFinished consumer that runs on the thread that calls the runTaskAsync
   * . OnError is what to run if the task fails, also runs on the main thread</p>
   *
   * @param <T>        the type parameter
   * @param taskToRun  the task to run
   * @param onFinished what to do when finished
   * @param onError    what to do when error occurs
   * @throws NullPointerException if any parameter is null
   */
  public <T> void runTaskAsync(
      Callable<T> taskToRun,
      Consumer<T> onFinished,
      Consumer<Throwable> onError) throws NullPointerException {
    Objects.requireNonNull(taskToRun);
    Objects.requireNonNull(onFinished);
    Objects.requireNonNull(onError);
    if (executor == null) {
      // failsafe: run synchronously on the caller's thread (may freeze UI)
      try {
        var result = taskToRun.call();
        onFinished.accept(result);
      } catch (Exception e) {
        onError.accept(e);
      }
    } else {
      Task<T> task = new Task<>() {
        @Override
        protected T call() throws Exception {
          return taskToRun.call();
        }
      };
      task.setOnSucceeded(_ -> onFinished.accept(task.getValue()));
      task.setOnFailed(_ -> onError.accept(task.getException()));

      try {
        runBackgroundTask(task);
      } catch (RejectedExecutionException e) {
        onError.accept(e);
      }
    }
  }

  /**
   * Shutdown the thread pool.
   */
  public void shutdown() {
    executor.shutdown();
    System.out.println("Shutting down thread pool");
  }

  private <T> void runBackgroundTask(Task<T> task) throws RejectedExecutionException {
    executor.submit(task);
  }

}
