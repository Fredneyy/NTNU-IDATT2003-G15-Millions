package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnBoardingDialog extends BaseDialog {

  private static final String[] ICON_STYLES = {
      "onboarding-icon-rocket",
      "onboarding-icon-trending",
      "onboarding-icon-cart",
      "onboarding-icon-news",
      "onboarding-icon-chart",
      "onboarding-icon-zap"
  };

  private final StackPane overlay = new StackPane();
  private final ProgressBar progressBar;
  private final VBox textContainer;
  private final StackPane iconBox;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private List<List<String>> onboardingText = List.of();
  private StackPane root;
  private int currentStep = 0;
  private final List<FontIcon> icons;
  private final Hyperlink nextLabel;
  private final ParallelTransition closeAnimation;


  public OnBoardingDialog(CsvUtil csvUtil, TaskUtil taskUtil) throws NullPointerException {
    super();

    this.csvUtil = Objects.requireNonNull(csvUtil);
    this.taskUtil = Objects.requireNonNull(taskUtil);

    loadText();
    icons = setUpIcons();
    progressBar = setUpProgressBar();
    closeAnimation = createCloseAnimation(_ -> {
      root.getChildren().removeAll(overlay,dialog);
      blurBackground(root, false, 0);
    });

    dialog.getStyleClass().setAll("onboarding-card");
    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 3.0);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 3.0);
    dialog.setMinHeight(Region.USE_COMPUTED_SIZE);
    dialog.setMinWidth(Region.USE_COMPUTED_SIZE);

    overlay.setMinHeight(Screen.getPrimary().getVisualBounds().getHeight());
    overlay.setMinWidth(Screen.getPrimary().getVisualBounds().getWidth());
    overlay.setOnMouseClicked(Event::consume);

    titleLabel.getStyleClass().add("onboarding-title");

    messageLabel.getStyleClass().add("onboarding-description");
    messageLabel.setMinHeight(Region.USE_PREF_SIZE);

    textContainer = new VBox(15);
    textContainer.setAlignment(Pos.BOTTOM_CENTER);

    iconBox = new StackPane(icons.getFirst());
    iconBox.getStyleClass().add(ICON_STYLES[0]);
    iconBox.setAlignment(Pos.CENTER);
    iconBox.setMaxSize(80, 80);
    iconBox.setMinSize(80, 80);

    textContainer.getChildren().addAll(iconBox, titleLabel, messageLabel);

    Hyperlink backLabel = new Hyperlink("< Back");
    backLabel.getStyleClass().add("onboarding-nav-link");
    backLabel.setOnAction(_ -> previousSlide());

    nextLabel = new Hyperlink("Next >");
    nextLabel.getStyleClass().add("onboarding-nav-link-next");
    nextLabel.setOnAction(_ -> nextSlide());

    Region footerSpacer = new Region();
    HBox.setHgrow(footerSpacer, Priority.ALWAYS);

    HBox footerContainer = new HBox(backLabel, footerSpacer, nextLabel);
    footerContainer.setAlignment(Pos.CENTER);
    footerContainer.setMaxWidth(Double.MAX_VALUE);

    Region verticalSpacer = new Region();
    VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

    VBox content = new VBox(textContainer, verticalSpacer, footerContainer);
    content.setAlignment(Pos.TOP_CENTER);
    content.setMaxHeight(Double.MAX_VALUE);
    content.setSpacing(16);
    content.setPadding(new Insets(30));
    VBox.setVgrow(content, Priority.ALWAYS);

    dialog.getChildren().addAll(progressBar, content);
  }

  @Override
  public void close() {
    if (root != null && root.getChildren().contains(dialog)) {
      closeAnimation.play();
    }
  }

  @Override
  public void show(StackPane root) {
    this.root = root;
    if (!root.getChildren().contains(dialog)) {
      blurBackground(root, true, 4);
      root.getChildren().addAll(overlay, dialog);
    }
  }

  private void updateIconBox(int step) {
    iconBox.getChildren().setAll(icons.get(step));
    iconBox.getStyleClass().setAll(ICON_STYLES[step]);
  }

  private void updateNextButton(boolean lastSlide) {
    if (lastSlide) {
      nextLabel.getStyleClass().setAll("onboarding-nav-finish");
      nextLabel.setText("Let's Trade! 🚀");
      nextLabel.setOnAction(_ -> close());
    } else {
      nextLabel.getStyleClass().setAll("onboarding-nav-link-next");
      nextLabel.setText("Next >");
      nextLabel.setOnAction(_ -> nextSlide());
    }
  }

  private List<FontIcon> setUpIcons() {
    FontIcon rocket = FontIcon.of(MaterialDesignR.ROCKET_LAUNCH, 48, Color.web("#e8eaf6"));
    FontIcon trending = FontIcon.of(MaterialDesignT.TRENDING_UP, 48, Color.web("#e8eaf6"));
    FontIcon cart = FontIcon.of(MaterialDesignC.CART, 48, Color.web("#e8eaf6"));
    FontIcon news = FontIcon.of(MaterialDesignN.NEWSPAPER, 48, Color.web("#e8eaf6"));
    FontIcon chart = FontIcon.of(MaterialDesignC.CHART_BAR, 48, Color.web("#e8eaf6"));
    FontIcon zap = FontIcon.of(MaterialDesignL.LIGHTNING_BOLT, 48, Color.web("#e8eaf6"));
    return new ArrayList<>(List.of(rocket, trending, cart, news, chart, zap));
  }

  private void loadText() {
    taskUtil.runTaskAsync(() -> csvUtil.readCsvFile("src/main/resources/storage/onboarding.csv"), result -> {
          this.onboardingText = result == null ? List.of() : result;
          if (onboardingText.isEmpty()) {
            close();
            return;
          }
          currentStep = 0;
          renderStep(currentStep);
          progressBar.setProgress(progressFor(currentStep));
        },
        _ -> close());
  }

  private double progressFor(int step) {
    int last = onboardingText.size() - 1;
    return last <= 0 ? 1.0 : (double) step / last;
  }

  private void renderStep(int step) {
    if (step < 0 || step >= onboardingText.size()) return;
    List<String> row = onboardingText.get(step);
    titleLabel.setText(row.isEmpty() ? "" : row.get(0));
    messageLabel.setText(row.size() < 2 ? "" : row.get(row.size() - 1));
    updateIconBox(Math.min(step, icons.size() - 1));
    updateNextButton(step == onboardingText.size() - 1);
  }

  private ProgressBar setUpProgressBar() {
    ProgressBar progressTypeBar = new ProgressBar(0);
    progressTypeBar.setMaxWidth(Double.MAX_VALUE);
    progressTypeBar.getStyleClass().add("onboarding-progress-bar");
    dialog.layoutBoundsProperty().addListener((_, _, newBounds) -> {
      Rectangle clip = new Rectangle(newBounds.getWidth(), newBounds.getHeight());
      clip.setArcWidth(56);
      clip.setArcHeight(56);
      progressTypeBar.setClip(clip);
    });

    Rectangle clip = new Rectangle(dialog.getWidth(), dialog.getHeight());
    clip.setArcWidth(56);
    clip.setArcHeight(56);
    progressTypeBar.setClip(clip);
    return progressTypeBar;
  }

  private void animateSlide(double outDirection, Runnable contentUpdate) {
    double slideDistance = 40;

    FadeTransition fadeOut = createFadeTransition(textContainer,  Duration.millis(300), 1.0, 0.0);
    TranslateTransition slideOut = createTranslateTransition(textContainer,  Duration.millis(300), 0, -1 * outDirection * slideDistance);

    ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
    out.setOnFinished(_ -> {
      contentUpdate.run();
      textContainer.setTranslateX(outDirection * slideDistance);
      FadeTransition fadeIn = createFadeTransition(textContainer,  Duration.millis(300), 0.0, 1.0);
      TranslateTransition slideIn = createTranslateTransition(textContainer, Duration.millis(300),
          outDirection * slideDistance, 0);
      new ParallelTransition(fadeIn, slideIn).play();
    });

    out.play();
  }

  private void nextSlide() {
    if (currentStep >= onboardingText.size() - 1) return;
    currentStep++;
    progressBar.setProgress(progressFor(currentStep));
    animateSlide(1, () -> renderStep(currentStep));
  }

  private void previousSlide() {
    if (currentStep <= 0) return;
    currentStep--;
    progressBar.setProgress(progressFor(currentStep));
    animateSlide(-1, () -> renderStep(currentStep));
  }
}