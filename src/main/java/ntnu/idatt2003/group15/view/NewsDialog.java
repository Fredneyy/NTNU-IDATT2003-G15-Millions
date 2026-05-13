package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class NewsDialog extends BaseDialog {

  private final ProgressBar progressBar;
  private final Duration displayDuration;
  private Timeline progressTimeline;
  private final Button closeButton = new Button("X");

  public NewsDialog(Duration animationDuration, Duration displayDuration) {
    super(animationDuration);
    this.displayDuration = displayDuration;

    dialog.getStyleClass().setAll("news-popup-container");
    dialog.setMinHeight(Region.USE_PREF_SIZE);
    dialog.setMaxHeight(Region.USE_PREF_SIZE);


    progressBar = new ProgressBar(1.0);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.getStyleClass().add("news-progress-bar");
    dialog.layoutBoundsProperty().addListener((_, _, newBounds) -> {
      Rectangle clip = new Rectangle(
          newBounds.getWidth(),
          newBounds.getHeight()
      );
      clip.setArcWidth(24);
      clip.setArcHeight(24);
      progressBar.setClip(clip);
    });

    titleLabel.getStyleClass().add("news-title-label");

    messageLabel.getStyleClass().add("news-message-label");

    closeButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    closeButton.setOnAction(_ -> close());
    
    VBox textContainer = new VBox(5, titleLabel, messageLabel);
    
    HBox contentRow = new HBox(10, textContainer, closeButton);
    contentRow.setAlignment(Pos.TOP_LEFT);
    HBox.setHgrow(textContainer, Priority.ALWAYS);
    contentRow.getStyleClass().add("news-content-area");

    dialog.getChildren().addAll(progressBar, contentRow);
  }

  public void setText(String title, String message) {
    titleLabel.setText(title);
    messageLabel.setText(message);
  }

  @Override
  public void close() {
    if (root != null && root.getChildren().contains(dialog)) {
      if (progressTimeline != null) progressTimeline.stop();

      TranslateTransition tt = new TranslateTransition(animationDuration, dialog);
      tt.setToX(400);
      tt.setInterpolator(Interpolator.EASE_IN);

      FadeTransition ft = createFadeTransition(dialog, dialog.getOpacity(), 0);

      ParallelTransition exit = new ParallelTransition(tt, ft);

      exit.setOnFinished(_ -> root.getChildren().remove(dialog));
      exit.play();
    }
  }

  @Override
  public void show(StackPane root) {
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

    FadeTransition ft = createFadeTransition(dialog, 0, 1);

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
}
