package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public class BaseDialog {

  protected final VBox dialog;
  protected final StackPane dialogPane;
  protected final StackPane overlay;
  protected StackPane root;
  protected final HBox closeButtonContainer;
  protected Button closeButton;
  Duration animationDuration;
  GaussianBlur gaussianBlur;

  public BaseDialog(Duration animationDuration, double blurrAmount) {
    dialog = new VBox();
    dialog.getStyleClass().add("pop-up-container");
    dialogPane = new StackPane();
    overlay = createOverlay();
    closeButtonContainer = createCloseButtonAndContainer();
    dialog.getChildren().addAll(closeButtonContainer, dialogPane);
    gaussianBlur = new GaussianBlur(blurrAmount);
    this.animationDuration = animationDuration;
  }

  public void show(StackPane root, Runnable onTransitionFinished) {
    Objects.requireNonNull(root, "root must not be null");
    Objects.requireNonNull(onTransitionFinished, "onTransitionFinished must not be null");
    this.root = root;
    root.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        close();
        event.consume();
      }
    });
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().forEach(node -> node.setEffect(gaussianBlur));
      root.getChildren().addAll(overlay, dialog);

      ParallelTransition transition = new ParallelTransition(
          createFadeTransition(dialog, 0,1, animationDuration),
          createScaleTransition(dialog, 0, 1, animationDuration)
      );
      transition.setOnFinished(_ -> onTransitionFinished.run());
      transition.play();
    }
    dialog.requestFocus();
  }

  public void close() {
    if (root != null && root.getChildren().contains(overlay)) {
      ParallelTransition closeAnimation = new ParallelTransition(
          createFadeTransition(dialog, 1, 0, animationDuration),
          createScaleTransition(dialog, 1.0, 0.1, animationDuration)
      );

      closeAnimation.setOnFinished(_ -> {
        root.getChildren().removeAll(overlay, dialog);
        root.getChildren().forEach(node -> node.setEffect(null));
      });

      closeAnimation.play();
    }
  }

  private HBox createCloseButtonAndContainer() {
    HBox newCloseButtonContainer = new HBox();
    newCloseButtonContainer.setAlignment(Pos.TOP_RIGHT);


    closeButton = new Button("X");
    closeButton.setOnAction(_ -> close());
    closeButton.getStyleClass().add("close-button");

    newCloseButtonContainer.setMaxHeight(closeButton.getHeight());
    newCloseButtonContainer.getChildren().add(closeButton);

    return newCloseButtonContainer;
  }

  private StackPane createOverlay() {
    StackPane newOverlay = new StackPane();
    newOverlay.getStyleClass().add("overlay");
    newOverlay.setOpacity(0.0);
    newOverlay.setOnMouseClicked(mouseEvent -> close());
    return newOverlay;
  }

  private FadeTransition createFadeTransition(Node node, double from, double to, Duration duration) {
    FadeTransition fadeTransition = new FadeTransition(duration, node);
    fadeTransition.setFromValue(from);
    fadeTransition.setToValue(to);
    return fadeTransition;
  }

  private ScaleTransition createScaleTransition(Node node, double from, double to, Duration duration) {
    ScaleTransition scaleTransition = new ScaleTransition(duration, node);
    scaleTransition.setFromX(from);
    scaleTransition.setFromY(from);
    scaleTransition.setToX(to);
    scaleTransition.setToY(to);
    return scaleTransition;
  }

}
