package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Rich breaking-news notification.
 *
 * Layout (top to bottom): colored header strip with "BREAKING NEWS" caption +
 * close button, a body row with sentiment-tinted icon tile and content column
 * (symbol badge, percent change, headline, description), a metadata footer
 * with a colored sentiment chip on the right, and a progress bar pinned to
 * the bottom that drains over the dialog's lifetime.
 *
 * Coloring is driven by {@link Sentiment}; pass {@code NEUTRAL} for plain
 * informational dialogs (e.g. the welcome message) — badges and percent
 * collapse out of view when their data is missing.
 */
public class NewsDialog extends BaseDialog {

  /** Visual sentiment — drives border/header/icon colors. */
  public enum Sentiment { BULLISH, BEARISH, NEUTRAL }

  private final Duration displayDuration;
  private Timeline progressTimeline;

  // Header
  private final FontIcon headerIcon = new FontIcon(FontAwesome.EXCLAMATION_CIRCLE);
  private final Label headerTitle = new Label("BREAKING NEWS");
  private final Button closeButton = new Button();
  private final HBox header = new HBox();

  // Body
  private final FontIcon trendIcon = new FontIcon(FontAwesome.LINE_CHART);
  private final StackPane trendIconBox = new StackPane(trendIcon);
  private final Label symbolBadge = new Label();
  private final Label percentLabel = new Label();
  private final HBox topRow = new HBox();
  private final VBox bodyContent = new VBox();
  private final HBox body = new HBox();

  // Footer (volatility text + sentiment chip)
  private final Region divider = new Region();
  private final Label footerText = new Label();
  private final Label sentimentChip = new Label();
  private final HBox footerRow = new HBox();
  private final VBox footer = new VBox();

  private final ProgressBar progressBar = new ProgressBar(1.0);

  private Sentiment sentiment = Sentiment.NEUTRAL;

  /** When set, close() removes from this stack instead of the legacy root StackPane. */
  private NewsContainer container;

  public NewsDialog(Duration displayDuration) {
    super();
    this.displayDuration = displayDuration;
    dialog.getStyleClass().setAll("news-popup");
    dialog.setMinHeight(Region.USE_PREF_SIZE);
    dialog.setMaxHeight(Region.USE_PREF_SIZE);
    dialog.setPickOnBounds(false);

    buildHeader();
    buildBody();
    buildFooter();
    buildProgressBar();

    dialog.getChildren().addAll(header, body, footer, progressBar);
    applySentimentStyles();
  }

  // ---------- Construction helpers ----------

  private void buildHeader() {
    headerIcon.getStyleClass().add("news-popup-header-icon");
    headerTitle.getStyleClass().add("news-popup-header-title");

    FontIcon closeIcon = new FontIcon(FontAwesome.CLOSE);
    closeIcon.getStyleClass().add("news-popup-close-icon");
    closeButton.setGraphic(closeIcon);
    closeButton.getStyleClass().add("news-popup-close");
    closeButton.setOnAction(_ -> close());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    header.getChildren().setAll(headerIcon, headerTitle, spacer, closeButton);
    header.getStyleClass().add("news-popup-header");
    header.setAlignment(Pos.CENTER_LEFT);
    header.setSpacing(10);
  }

  private void buildBody() {
    trendIcon.getStyleClass().add("news-popup-trend-icon");
    trendIconBox.getStyleClass().add("news-popup-trend-box");

    symbolBadge.getStyleClass().add("news-popup-symbol-badge");
    percentLabel.getStyleClass().add("news-popup-percent");

    topRow.getChildren().setAll(symbolBadge, percentLabel);
    topRow.setSpacing(12);
    topRow.setAlignment(Pos.CENTER_LEFT);

    titleLabel.getStyleClass().setAll("news-popup-title");
    messageLabel.getStyleClass().setAll("news-popup-message");
    titleLabel.setWrapText(true);
    messageLabel.setWrapText(true);

    bodyContent.getChildren().setAll(topRow, titleLabel, messageLabel);
    bodyContent.getStyleClass().add("news-popup-body-content");
    bodyContent.setSpacing(6);
    HBox.setHgrow(bodyContent, Priority.ALWAYS);
    bodyContent.setMaxWidth(Double.MAX_VALUE);

    body.getChildren().setAll(trendIconBox, bodyContent);
    body.getStyleClass().add("news-popup-body");
    body.setSpacing(14);
    body.setAlignment(Pos.TOP_LEFT);
  }

  private void buildFooter() {
    divider.getStyleClass().add("news-popup-divider");
    footerText.getStyleClass().add("news-popup-footer-text");
    sentimentChip.getStyleClass().add("news-popup-sentiment-chip");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    footerRow.getChildren().setAll(footerText, spacer, sentimentChip);
    footerRow.setAlignment(Pos.CENTER_LEFT);

    footer.getChildren().setAll(divider, footerRow);
    footer.getStyleClass().add("news-popup-footer");
    footer.setSpacing(12);
  }

  private void buildProgressBar() {
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.getStyleClass().add("news-popup-progress");
    // Round only the bottom corners: anchor the clip ABOVE the bar so the top
    // arc lands off-bar (invisible) while the bottom arc curves the visible edge.
    progressBar.layoutBoundsProperty().addListener((_, _, b) -> {
      double r = 14;                                   // matches .news-popup radius
      double w = b.getWidth();
      double h = b.getHeight();
      if (w <= 0 || h <= 0) return;
      Rectangle clip = new Rectangle(0, -r, w, h + r);
      clip.setArcWidth(r * 2);
      clip.setArcHeight(r * 2);
      progressBar.setClip(clip);
    });
  }

  // ---------- Public setters ----------

  public void setText(String title, String message) {
    titleLabel.setText(title == null ? "" : title);
    messageLabel.setText(message == null ? "" : message);
    setManaged(messageLabel, message != null && !message.isBlank());
  }

  public void setSentiment(Sentiment sentiment) {
    this.sentiment = sentiment == null ? Sentiment.NEUTRAL : sentiment;
    applySentimentStyles();
  }

  /** Show the symbol badge with the given ticker, or hide it if null/blank. */
  public void setSymbol(String symbol) {
    boolean show = symbol != null && !symbol.isBlank();
    symbolBadge.setText(show ? symbol : "");
    setManaged(symbolBadge, show);
  }

  /** Show the percent change label (e.g. +20.0%) or hide it if null. */
  public void setChangePercent(BigDecimal percent) {
    if (percent == null) {
      percentLabel.setText("");
      setManaged(percentLabel, false);
      return;
    }
    BigDecimal scaled = percent.setScale(1, RoundingMode.HALF_UP);
    String prefix = scaled.signum() >= 0 ? "+" : "";
    percentLabel.setText(prefix + scaled.toPlainString() + "%");
    setManaged(percentLabel, true);
  }

  /** Bottom-row metadata text (e.g. "Volatility increased for 16 updates"). Empty hides footer. */
  public void setFooter(String text) {
    boolean show = text != null && !text.isBlank();
    footerText.setText(show ? text : "");
    setManaged(footer, show || isChipVisible());
  }

  private boolean isChipVisible() {
    return !sentimentChip.getText().isBlank();
  }

  // ---------- Internals ----------

  private void applySentimentStyles() {
    // Reset tone classes and re-apply for the current sentiment.
    for (String c : new String[]{"sentiment-bullish", "sentiment-bearish", "sentiment-neutral"}) {
      dialog.getStyleClass().remove(c);
    }
    String toneClass = switch (sentiment) {
      case BULLISH -> "sentiment-bullish";
      case BEARISH -> "sentiment-bearish";
      case NEUTRAL -> "sentiment-neutral";
    };
    dialog.getStyleClass().add(toneClass);

    // Trend icon glyph reflects direction.
    trendIcon.setIconCode(switch (sentiment) {
      case BULLISH -> FontAwesome.LINE_CHART;
      case BEARISH -> FontAwesome.AREA_CHART;
      case NEUTRAL -> FontAwesome.INFO_CIRCLE;
    });

    // Sentiment chip text.
    switch (sentiment) {
      case BULLISH -> sentimentChip.setText("BULLISH");
      case BEARISH -> sentimentChip.setText("BEARISH");
      case NEUTRAL -> sentimentChip.setText("");
    }
    setManaged(sentimentChip, !sentimentChip.getText().isBlank());

    // Hide the rich top row entirely if NEUTRAL with no symbol/percent — e.g.
    // the welcome message reduces to header + title + message.
    boolean topRowVisible = sentiment != Sentiment.NEUTRAL
            || !symbolBadge.getText().isBlank()
            || !percentLabel.getText().isBlank();
    setManaged(topRow, topRowVisible);
    setManaged(trendIconBox, sentiment != Sentiment.NEUTRAL);
    if (sentiment == Sentiment.NEUTRAL) {
      headerTitle.setText("INFO");
    } else {
      headerTitle.setText("BREAKING NEWS");
    }
  }

  private static void setManaged(javafx.scene.Node node, boolean managed) {
    node.setManaged(managed);
    node.setVisible(managed);
  }

  // ---------- Lifecycle ----------

  @Override
  public void close() {
    if (container != null) {
      if (progressTimeline != null) progressTimeline.stop();
      ParallelTransition exit = buildExitAnimation();
      exit.setOnFinished(_ -> container.remove(dialog));
      exit.play();
      return;
    }
    if (root != null && root.getChildren().contains(dialog)) {
      if (progressTimeline != null) progressTimeline.stop();
      ParallelTransition exit = buildExitAnimation();
      exit.setOnFinished(_ -> root.getChildren().remove(dialog));
      exit.play();
    }
  }

  private ParallelTransition buildExitAnimation() {
    TranslateTransition tt = new TranslateTransition(Duration.millis(200), dialog);
    tt.setToX(400);
    tt.setInterpolator(Interpolator.EASE_IN);

    FadeTransition ft = createFadeTransition(dialog, Duration.millis(300), dialog.getOpacity(), 0);
    return new ParallelTransition(tt, ft);
  }

  @Override
  public void show(StackPane root) {
    if (!root.getChildren().contains(dialog)) {
      this.root = root;
      root.getChildren().add(dialog);
      StackPane.setAlignment(dialog, Pos.TOP_RIGHT);
      StackPane.setMargin(dialog, new Insets(20, 20, 0, 0));

      playEntranceAnimation();
      startProgressTimer();
    }
  }

  /** Show this dialog as the newest notification inside a {@link NewsContainer}. */
  public void showIn(NewsContainer container) {
    if (container == null) return;
    this.container = container;
    container.pushTop(dialog);
    playEntranceAnimation();
    startProgressTimer();
  }

  private void playEntranceAnimation() {
    dialog.setTranslateX(400);
    dialog.setOpacity(0);

    TranslateTransition tt = new TranslateTransition(Duration.millis(300), dialog);
    tt.setToX(0);
    tt.setInterpolator(Interpolator.EASE_OUT);

    FadeTransition ft = createFadeTransition(dialog, Duration.millis(300), 0, 1);

    new ParallelTransition(tt, ft).play();
  }

  private void startProgressTimer() {
    progressBar.setProgress(1.0);
    if (progressTimeline != null) progressTimeline.stop();

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
