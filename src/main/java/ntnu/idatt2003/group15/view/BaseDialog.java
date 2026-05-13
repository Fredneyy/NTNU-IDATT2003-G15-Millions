package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public abstract class BaseDialog {

  protected final VBox dialog = new VBox();
  protected StackPane root;
  protected final Duration animationDuration;
  protected final Label titleLabel = new Label();
  protected final Label messageLabel = new Label();

  protected BaseDialog(Duration animationDuration) {
    this.animationDuration = animationDuration;

    dialog.getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/style/DialogStyle.css")).toExternalForm());
    dialog.getStyleClass().add("pop-up-container");
    dialog.setPickOnBounds(false);

    titleLabel.getStyleClass().add("dialog-title-label");
    messageLabel.getStyleClass().add("dialog-message-label");
    titleLabel.setWrapText(true);
    messageLabel.setWrapText(true);

    VBox.setVgrow(dialog, Priority.ALWAYS);
    VBox.setVgrow(titleLabel, Priority.ALWAYS);
    VBox.setVgrow(messageLabel, Priority.ALWAYS);
    HBox.setHgrow(titleLabel, Priority.ALWAYS);
    HBox.setHgrow(messageLabel, Priority.ALWAYS);
    HBox.setHgrow(dialog, Priority.ALWAYS);
  }

  protected FadeTransition createFadeTransition(Node node, double from, double to) {
    FadeTransition fadeTransition = new FadeTransition(animationDuration, node);
    fadeTransition.setFromValue(from);
    fadeTransition.setToValue(to);
    return fadeTransition;
  }

  protected ScaleTransition createScaleTransition(Node node, double from, double to) {
    ScaleTransition scaleTransition = new ScaleTransition(animationDuration, node);
    scaleTransition.setFromX(from);
    scaleTransition.setFromY(from);
    scaleTransition.setToX(to);
    scaleTransition.setToY(to);
    return scaleTransition;
  }

  protected TranslateTransition createTranslateTransition(Node node, double fromX, double toX) {
    TranslateTransition translateTransition = new TranslateTransition(animationDuration, node);
    translateTransition.setFromX(fromX);
    translateTransition.setToX(toX);
    translateTransition.setFromY(0);
    translateTransition.setToY(0);
    return translateTransition;
  }

  protected void blurBackground(StackPane root, boolean blur, int blurAmount) {
    if (blur) {
      root.getChildren().forEach(node -> node.setEffect(new GaussianBlur(blurAmount)));
    } else {
      root.getChildren().forEach(node -> node.setEffect(null));
    }
  }

  public abstract void show(StackPane root);

  public abstract void close();
}

