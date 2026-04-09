package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class BaseDialog {

  protected final StackPane dialog;

  public BaseDialog() {
    dialog = new StackPane();
  }

  public void show(StackPane root, Runnable onTransitionFinished) {
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);

      ParallelTransition transition = new ParallelTransition(
          createFadeTransition(dialog, 1, 0, Duration.millis(200)),
          createFadeTransition(dialog, 1, 0, Duration.millis(200)),
          createFadeTransition(dialog, 1, 0.8,  Duration.millis(200))
      );
      if (onTransitionFinished != null) {
        transition.setOnFinished(_ -> onTransitionFinished.run());
      }
      transition.play();
    }
  }

  private FadeTransition createFadeTransition(Node node, double from, double to, Duration duration) {
    FadeTransition fadeTransition = new FadeTransition(duration, node);
    fadeTransition.setFromValue(from);
    fadeTransition.setToValue(to);
    return fadeTransition;
  }

}
