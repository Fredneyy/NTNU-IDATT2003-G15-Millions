package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.Objects;

public class NewsDialog implements Dialog {

  private final VBox dialog;
  private StackPane root;
  private final Duration animationDuration;
  private final Button closeButton;

  private final Label titleLabel;
  private final Label messageLabel;
  private final ProgressBar progressBar;
  private final Duration displayDuration;
  private Timeline progressTimeline;
  private boolean isClosing = false;

  public NewsDialog(Duration animationDuration, Duration displayDuration) {
    this.animationDuration = animationDuration;
    this.displayDuration = displayDuration;

    this.dialog = new VBox();
    this.dialog.getStyleClass().add("pop-up-container");

    this.closeButton = new Button("X");
    this.closeButton.setOnAction(_ -> close());
    this.closeButton.getStyleClass().add("close-button");

    dialog.getStyleClass().setAll("news-popup-container");
    dialog.setMinHeight(Region.USE_PREF_SIZE);
    dialog.setMaxHeight(Region.USE_PREF_SIZE);

    progressBar = new ProgressBar(1.0);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.getStyleClass().add("news-progress-bar");

    titleLabel = new Label();
    titleLabel.getStyleClass().add("news-title-label");

    messageLabel = new Label();
    messageLabel.getStyleClass().add("news-message-label");
    messageLabel.setWrapText(true);
    messageLabel.setMinHeight(Region.USE_PREF_SIZE);

    closeButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    
    VBox textContainer = new VBox(5, titleLabel, messageLabel);
    
    HBox contentRow = new HBox(10, textContainer, closeButton);
    contentRow.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(textContainer, Priority.ALWAYS);
    contentRow.getStyleClass().add("news-content-area");

    dialog.getChildren().addAll(progressBar, contentRow);

    dialog.setPickOnBounds(true);
  }

  @Override
  public void show(StackPane root, String title, String message) {
    Objects.requireNonNull(root, "root must not be null");
    this.root = root;
    titleLabel.setText(title);
    messageLabel.setText(message);
    isClosing = false;

    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
      StackPane.setAlignment(dialog, Pos.TOP_RIGHT);
      StackPane.setMargin(dialog, new Insets(20, 20, 0, 0));

      playEntranceAnimation();
      startProgressTimer();
    }
  }

  private void playEntranceAnimation() {
    dialog.setTranslateX(400);
    dialog.setOpacity(0);

    TranslateTransition tt = new TranslateTransition(animationDuration, dialog);
    tt.setToX(0);
    tt.setInterpolator(Interpolator.EASE_OUT);

    FadeTransition ft = new FadeTransition(animationDuration, dialog);
    ft.setToValue(1);

    ParallelTransition entrance = new ParallelTransition(tt, ft);
    entrance.play();
  }

  private void startProgressTimer() {
    progressBar.setProgress(1.0);
    
    if (progressTimeline != null) {
      progressTimeline.stop();
    }

    progressTimeline = new Timeline(
        new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 1.0)),
        new KeyFrame(displayDuration, new KeyValue(progressBar.progressProperty(), 0.0))
    );
    progressTimeline.setOnFinished(_ -> close());
    
    dialog.setOnMouseEntered(_ -> progressTimeline.pause());
    dialog.setOnMouseExited(_ -> progressTimeline.play());

    progressTimeline.play();
  }

  @Override
  public void close() {
    if (isClosing) return;
    if (root != null && root.getChildren().contains(dialog)) {
      isClosing = true;
      if (progressTimeline != null) progressTimeline.stop();

      TranslateTransition tt = new TranslateTransition(animationDuration, dialog);
      tt.setToX(400);
      tt.setInterpolator(Interpolator.EASE_IN);

      FadeTransition ft = new FadeTransition(animationDuration, dialog);
      ft.setToValue(0);

      ParallelTransition exit = new ParallelTransition(tt, ft);

      exit.setOnFinished(_ -> {
        root.getChildren().remove(dialog);
        isClosing = false;
      });
      exit.play();
    }
  }

}
