package ntnu.idatt2003.group15.view;

import javafx.animation.ParallelTransition;
import javafx.geometry.Pos;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;

public class ExceptionDialog extends BaseDialog {

  private final StackPane dialogPane;
  private final StackPane overlay;
  private final GaussianBlur gaussianBlur;

  public ExceptionDialog(Duration animationDuration, double blurAmount) {

    super(animationDuration);

    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 3);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 3);

    this.dialogPane = new StackPane();
    this.overlay = createOverlay();
    HBox closeButtonContainer = createCloseButtonAndContainer();
    this.gaussianBlur = new GaussianBlur(blurAmount);

    this.dialog.getChildren().addAll(closeButtonContainer, dialogPane);

    titleLabel.getStyleClass().add("dialog-title-label");
    messageLabel.getStyleClass().add("dialog-message-label");
  }

  @Override
  public void show(StackPane root, String title, String message) {
    prepareDialog(root, title, message);
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
          createFadeTransition(dialog, 0, 1),
          createScaleTransition(dialog, 0, 1)
      );
      transition.play();
    }
    dialog.requestFocus();
  }

  @Override
  public void close() {
    if (root != null && root.getChildren().contains(overlay)) {
      ParallelTransition closeAnimation = new ParallelTransition(
          createFadeTransition(dialog, 1, 0),
          createScaleTransition(dialog, 1.0, 0.1)
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


}
