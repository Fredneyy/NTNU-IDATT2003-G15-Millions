package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.Objects;

public class ExceptionDialog implements Dialog {

  private final Label titleLabel;
  private final Label messageLabel;
  
  private final VBox dialog;
  private StackPane root;
  private final Duration animationDuration;
  private final Button closeButton;

  private final StackPane dialogPane;
  private final StackPane overlay;
  private final GaussianBlur gaussianBlur;

  public ExceptionDialog(Duration animationDuration, double blurAmount) {
    this.animationDuration = animationDuration;
    
    this.dialog = new VBox();
    this.dialog.getStyleClass().add("pop-up-container");

    this.closeButton = new Button("X");
    this.closeButton.setOnAction(_ -> close());
    this.closeButton.getStyleClass().add("close-button");

    this.dialogPane = new StackPane();
    this.overlay = createOverlay();
    HBox closeButtonContainer = createCloseButtonAndContainer();
    this.gaussianBlur = new GaussianBlur(blurAmount);

    this.dialog.getChildren().addAll(closeButtonContainer, dialogPane);
    
    messageLabel = new Label();
    titleLabel = new Label();
    titleLabel.getStyleClass().add("dialog-title-label");
    messageLabel.getStyleClass().add("dialog-message-label");
    messageLabel.setWrapText(true);
  }

  @Override
  public void show(StackPane root, String title, String message) {
    Objects.requireNonNull(root, "root must not be null");
    this.root = root;
    titleLabel.setText(title);
    messageLabel.setText(message);
    dialogPane.setAlignment(Pos.CENTER);
    VBox mainContainer = new VBox(10);
    VBox messageBox = new VBox();
    messageBox.setAlignment(Pos.CENTER);
    messageBox.getChildren().add(messageLabel);
    mainContainer.getChildren().addAll(titleLabel, messageBox);
    dialogPane.getChildren().setAll(mainContainer);

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
          createFadeTransition(dialog, 0, 1, animationDuration),
          createScaleTransition(dialog, 0, 1, animationDuration)
      );
      transition.play();
    }
    dialog.requestFocus();
  }

  @Override
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
    newCloseButtonContainer.setMaxHeight(closeButton.getHeight());
    newCloseButtonContainer.getChildren().add(closeButton);
    return newCloseButtonContainer;
  }

  private StackPane createOverlay() {
    StackPane newOverlay = new StackPane();
    newOverlay.getStyleClass().add("overlay");
    newOverlay.setOpacity(0.0);
    newOverlay.setOnMouseClicked(_ -> close());
    return newOverlay;
  }

  protected FadeTransition createFadeTransition(Node node, double from, double to, Duration duration) {
    FadeTransition fadeTransition = new FadeTransition(duration, node);
    fadeTransition.setFromValue(from);
    fadeTransition.setToValue(to);
    return fadeTransition;
  }

  protected ScaleTransition createScaleTransition(Node node, double from, double to, Duration duration) {
    ScaleTransition scaleTransition = new ScaleTransition(duration, node);
    scaleTransition.setFromX(from);
    scaleTransition.setFromY(from);
    scaleTransition.setToX(to);
    scaleTransition.setToY(to);
    return scaleTransition;
  }

}
