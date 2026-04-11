package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public abstract class BaseDialog {

  protected final VBox dialog;
  protected StackPane root;
  protected final Duration animationDuration;
  protected final Button closeButton;

  protected final Label titleLabel;
  protected final Label messageLabel;

  protected BaseDialog(Duration animationDuration) {
    this.animationDuration = animationDuration;

    dialog = new VBox();
    dialog.getStyleClass().add("pop-up-container");
    dialog.setPickOnBounds(false);

    closeButton = new Button("X");
    closeButton.setOnAction(_ -> close());
    closeButton.getStyleClass().add("close-button");
    closeButton.setPickOnBounds(false);

    titleLabel = new Label();
    messageLabel = new Label();
    messageLabel.setWrapText(true);
  }

  protected void prepareDialog(StackPane root, String title, String message)
      throws NullPointerException {
    Objects.requireNonNull(root, "root must not be null");
    this.root = root;
    titleLabel.setText(title);
    messageLabel.setText(message);
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

  public abstract void show(StackPane root, String title, String message);

  public abstract void close();
}

