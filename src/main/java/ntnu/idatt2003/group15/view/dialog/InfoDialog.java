package ntnu.idatt2003.group15.view.dialog;

import javafx.animation.ParallelTransition;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.Objects;

/** Modal dialog for non-exception messages (e.g. "Game saved"). Mirrors {@link ExceptionDialog}. */
public class InfoDialog extends BaseDialog {

  private StackPane root;
  private final StackPane dialogPane;
  private final StackPane overlay;
  private final Button closeButton = new Button("X");
  private final ParallelTransition closeAnimation;
  private final EventHandler<KeyEvent> escapeFilter = event -> {
    if (event.getCode() == KeyCode.ESCAPE) {
      close();
      event.consume();
    }
  };
  private Scene installedScene;

  public InfoDialog() {
    super();

    dialog.getStyleClass().add("info-dialog");
    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 3.0);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 3.0);

    this.dialogPane = new StackPane();
    this.overlay = createOverlay();
    HBox closeButtonContainer = createCloseButtonAndContainer();

    closeButton.getStyleClass().add("close-button");
    closeButton.setOnAction(_ -> close());

    titleLabel.getStyleClass().add("dialog-title-label");
    messageLabel.getStyleClass().add("dialog-message-label");

    closeAnimation = createCloseAnimation(_ -> {
      root.getChildren().removeAll(overlay, dialog);
      blurBackground(root, false, 0);
      uninstallEscapeFilter();
    });
    dialog.getChildren().addAll(closeButtonContainer, dialogPane);
  }

  public void setText(String title, String message) {
    titleLabel.setText(title);
    messageLabel.setText(message);
  }

  public void show(StackPane root) {
    this.root = Objects.requireNonNull(root);
    dialogPane.setAlignment(Pos.CENTER);
    VBox mainContainer = new VBox(10);
    VBox messageBox = new VBox();
    messageBox.setAlignment(Pos.CENTER);
    messageBox.getChildren().add(messageLabel);
    mainContainer.getChildren().addAll(titleLabel, messageBox);
    dialogPane.getChildren().setAll(mainContainer);

    installEscapeFilter(root.getScene());

    if (!root.getChildren().contains(dialog)) {
      blurBackground(root, true, 2);
      root.getChildren().addAll(overlay, dialog);

      ParallelTransition transition = new ParallelTransition(
          createFadeTransition(dialog, Duration.millis(300), 0, 1),
          createScaleTransition(dialog, Duration.millis(300), 0, 1)
      );
      transition.play();
    }
    dialog.requestFocus();
  }

  public void close() {
    if (root != null && root.getChildren().contains(overlay)
        && root.getChildren().contains(dialog)) {
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

  private void installEscapeFilter(Scene scene) {
    if (scene == null || scene == installedScene) return;
    uninstallEscapeFilter();
    scene.addEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
    installedScene = scene;
  }

  private void uninstallEscapeFilter() {
    if (installedScene != null) {
      installedScene.removeEventFilter(KeyEvent.KEY_PRESSED, escapeFilter);
      installedScene = null;
    }
  }
}
