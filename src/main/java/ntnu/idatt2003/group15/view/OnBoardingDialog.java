package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class OnBoardingDialog extends BaseDialog {

  private final ProgressBar progressBar;
  private final VBox textContainer;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private List<String> onboardingText;
  private ParallelTransition transitionForward;
  private ParallelTransition transitionBackward;
  private ListIterator<String> onboardingTextIterator;
  private boolean forwardIteration = false;
  private StackPane root;


  public OnBoardingDialog(CsvUtil csvUtil, TaskUtil taskUtil) throws NullPointerException {
    super(Duration.millis(400));

    this.csvUtil = Objects.requireNonNull(csvUtil);
    this.taskUtil = Objects.requireNonNull(taskUtil);

    loadText();

    dialog.getStyleClass().setAll("onboarding-card");
    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 2.0);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 2.0);
    dialog.setMinHeight(Region.USE_COMPUTED_SIZE);
    dialog.setMinWidth(Region.USE_COMPUTED_SIZE);

    progressBar = new ProgressBar(0);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.getStyleClass().add("onboarding-progress-bar");
    dialog.layoutBoundsProperty().addListener((_, _, newBounds) -> {
      Rectangle clip = new Rectangle(
          newBounds.getWidth(),
          newBounds.getHeight()
      );
      clip.setArcWidth(24);
      clip.setArcHeight(24);
      progressBar.setClip(clip);
    });

    titleLabel.getStyleClass().add("onboarding-title");

    messageLabel.getStyleClass().add("onboarding-description");
    messageLabel.setMinHeight(Region.USE_PREF_SIZE);

    textContainer = new VBox(5, titleLabel, messageLabel);

    HBox footerContainer = new HBox(10);
    footerContainer.setAlignment(Pos.BOTTOM_CENTER);

    Hyperlink backLabel =  new Hyperlink("< Back");
    backLabel.getStyleClass().add("onboarding-nav-link-next");
    Hyperlink nextLabel = new Hyperlink("Next >");
    nextLabel.getStyleClass().add("onboarding-nav-link-next");
    nextLabel.setOnAction(e -> nextSlide());
    backLabel.setOnAction(e -> previousSlide());
    footerContainer.getChildren().addAll(backLabel, nextLabel);

    dialog.getChildren().addAll(progressBar, textContainer, footerContainer);
  }

  @Override
  public void close() {
    root.getChildren().remove(dialog);
  }

  @Override
  public void show(StackPane root) {
    this.root = root;
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  private void loadText() {
    taskUtil.runTaskAsync(() -> {
      List<String> rawData = csvUtil.readCsvFile("src/main/resources/storage/onboarding.csv");
      return rawData;
      }, result -> {
      this.onboardingText = (List<String>) result;
      onboardingTextIterator = this.onboardingText.listIterator();
      titleLabel.setText(onboardingText.get(0));
      messageLabel.setText(onboardingText.get(1));
      }
      , error -> close());
  }

  private void setUpTransition(int setByX) {
    TranslateTransition translateTransition = new TranslateTransition(Duration.millis(300), textContainer);
    translateTransition.setToX(setByX);
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), textContainer);
  }

  private void nextSlide() {
    if (onboardingTextIterator.nextIndex() <= onboardingText.size() - 1) {
      if (!forwardIteration) {
        onboardingTextIterator.next();
        onboardingTextIterator.next();
      }
      forwardIteration = true;
      titleLabel.setText(onboardingTextIterator.next());
      messageLabel.setText(onboardingTextIterator.next());
    }
  }

  private void previousSlide() {
    if (onboardingTextIterator.previousIndex() >= 1) {
      if (forwardIteration) {
        onboardingTextIterator.previous();
        onboardingTextIterator.previous();
      }
      forwardIteration = false;
      messageLabel.setText(onboardingTextIterator.previous());
      titleLabel.setText(onboardingTextIterator.previous());
    }
  }
}
