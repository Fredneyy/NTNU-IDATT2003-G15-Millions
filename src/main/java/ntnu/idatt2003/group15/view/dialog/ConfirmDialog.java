package ntnu.idatt2003.group15.view.dialog;

import javafx.animation.ParallelTransition;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.util.Duration;
import java.util.Objects;

public class ConfirmDialog extends BaseDialog {

  private final StackPane dialogPane = new StackPane();
  private final StackPane overlay;
  private final Button cancelButton = new Button("Cancel");
  private final Button altButton = new Button();
  private final Button confirmButton = new Button("Confirm");
  private final ParallelTransition closeAnimation;
  private final EventHandler<KeyEvent> escapeFilter = event -> {
    if (event.getCode() == KeyCode.ESCAPE) {
      close();
      event.consume();
    }
  };
  private Runnable onConfirm;
  private Runnable onAlt;
  private Scene installedScene;

  public ConfirmDialog() {
    super();

    dialog.getStyleClass().add("confirm-dialog");
    // Let the dialog size to its content rather than the screen-fraction
    // default that BaseDialog-derived dialogs sometimes use.
    dialog.setMinHeight(Region.USE_PREF_SIZE);
    dialog.setPrefHeight(Region.USE_COMPUTED_SIZE);
    dialog.setMaxHeight(Region.USE_PREF_SIZE);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 3.0);

    // BaseDialog wires Priority.ALWAYS on the shared title/message labels so
    // they can fill tall dialogs. We want this dialog to hug its content, so
    // undo that inheritance for our use of those labels.
    VBox.setVgrow(titleLabel, Priority.NEVER);
    VBox.setVgrow(messageLabel, Priority.NEVER);

    overlay = createOverlay();

    cancelButton.getStyleClass().add("confirm-cancel-button");
    cancelButton.setOnAction(_ -> close());

    confirmButton.getStyleClass().add("confirm-confirm-button");
    confirmButton.setOnAction(_ -> {
      Runnable action = onConfirm;
      close();
      if (action != null) {
        action.run();
      }
    });

    altButton.getStyleClass().add("confirm-alt-button");
    altButton.setOnAction(_ -> {
      Runnable action = onAlt;
      close();
      if (action != null) {
        action.run();
      }
    });

    closeAnimation = createCloseAnimation(_ -> {
      root.getChildren().removeAll(overlay, dialog);
      blurBackground(root, false, 0);
      uninstallEscapeFilter();
    });
  }

  public void show(StackPane root, String title, String message,
                   String confirmText, String cancelText, Runnable onConfirm) {
    showInternal(root, title, message, confirmText, null, cancelText, onConfirm, null);
  }

  /**
   * Three-button variant. Adds an "alternative" action that sits between the
   * cancel and confirm buttons. Useful when a third intermediate choice exists,
   * e.g. "Save", "Save and exit", "Cancel".
   */
  public void show(StackPane root, String title, String message,
                   String confirmText, String altText, String cancelText,
                   Runnable onConfirm, Runnable onAlt) {
    showInternal(root, title, message, confirmText, altText, cancelText, onConfirm, onAlt);
  }

  private void showInternal(StackPane root, String title, String message,
                            String confirmText, String altText, String cancelText,
                            Runnable onConfirm, Runnable onAlt) {
    this.root = Objects.requireNonNull(root);
    this.onConfirm = onConfirm;
    this.onAlt = onAlt;

    titleLabel.setText(title);
    messageLabel.setText(message);
    confirmButton.setText(confirmText == null ? "Confirm" : confirmText);
    cancelButton.setText(cancelText == null ? "Cancel" : cancelText);

    VBox messageBox = new VBox(messageLabel);
    messageBox.setAlignment(Pos.CENTER);

    HBox buttonRow = new HBox(12);
    buttonRow.getChildren().add(cancelButton);
    if (altText != null) {
      altButton.setText(altText);
      buttonRow.getChildren().add(altButton);
    }
    buttonRow.getChildren().add(confirmButton);
    buttonRow.setAlignment(Pos.CENTER);
    buttonRow.getStyleClass().add("confirm-button-row");

    VBox content = new VBox(16, titleLabel, messageBox, buttonRow);
    content.setAlignment(Pos.CENTER);

    dialogPane.setAlignment(Pos.CENTER);
    dialogPane.getChildren().setAll(content);
    dialog.getChildren().setAll(dialogPane);

    installEscapeFilter(root.getScene());

    if (!root.getChildren().contains(dialog)) {
      blurBackground(root, true, 2);
      root.getChildren().addAll(overlay, dialog);
      ParallelTransition open = new ParallelTransition(
          createFadeTransition(dialog, Duration.millis(300), 0, 1),
          createScaleTransition(dialog, Duration.millis(300), 0, 1)
      );
      open.play();
    }
    dialog.requestFocus();
  }

  public void close() {
    if (root != null && root.getChildren().contains(dialog)
        && root.getChildren().contains(overlay)) {
      closeAnimation.play();
    }
  }

  private StackPane createOverlay() {
    StackPane created = new StackPane();
    created.getStyleClass().add("overlay");
    created.setOpacity(0.0);
    created.setOnMouseClicked(_ -> close());
    return created;
  }

  private void installEscapeFilter(Scene scene) {
    if (scene == null || scene == installedScene) {
      return;
    }
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
