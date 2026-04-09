package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MainMenu {

  private final StackPane view = new StackPane();
  private final TextField nameField = new TextField();
  private final Button playButton = new Button("Play");
  private final TranslateTransition shakeAnimation = new TranslateTransition(Duration.millis(60), nameField);
  private boolean isPlaying = false;

  public MainMenu() {
    buildUI();
    wireEvents();
    playEntranceAnimation();
    shakeAnimation.setByX(8);
    shakeAnimation.setCycleCount(6);
    shakeAnimation.setAutoReverse(true);
    shakeAnimation.setOnFinished(_ -> {
      nameField.setTranslateX(0);
      isPlaying = false;
    });
  }

  public StackPane getView() {
    return view;
  }

  private void buildUI() {
    view.setAlignment(Pos.CENTER);

    VBox center = new VBox(24);
    center.setAlignment(Pos.CENTER);
    center.setMaxWidth(420);
    center.setPadding(new Insets(0, 24, 0, 24));

    List<String> quotes = loadQuotes();

    VBox tipContainer = new VBox();
    tipContainer.setSpacing(10);
    tipContainer.setAlignment(Pos.CENTER);
    tipContainer.getStyleClass().add("tip-banner");

    HBox authorWrapper = new HBox();
    authorWrapper.setAlignment(Pos.BOTTOM_RIGHT);

    Label quote = buildTipLabel();
    Label author = buildTipLabel();
    author.getStyleClass().add("author");
    authorWrapper.getChildren().add(author);
    tipContainer.getChildren().addAll(quote, authorWrapper);

    startQuoteAnimation(quotes, tipContainer, quote, author);

    center.getChildren().addAll(
        buildIcon(),
        buildTitleBlock(),
        tipContainer,
        buildCard()
    );

    Pane particleLayer = new Pane();
    particleLayer.getChildren().addAll(createBackgroundCircles());
    particleLayer.setMouseTransparent(true);

    view.getChildren().addAll(particleLayer, center);
  }

  private Pane buildIcon() {
    StackPane icon = new StackPane();
    icon.getStyleClass().add("app-icon");
    icon.setMaxSize(72, 72);
    icon.setMinSize(72, 72);

    SVGPath arrow = new SVGPath();
    arrow.setContent("M4 16 L10 10 L14 14 L20 8 M15 8 L20 8 L20 13");
    arrow.setStroke(Color.WHITE);
    arrow.setStrokeWidth(2.2);
    arrow.setFill(Color.TRANSPARENT);
    arrow.setScaleX(1.6);
    arrow.setScaleY(1.6);

    icon.getChildren().add(arrow);
    StackPane.setAlignment(icon, Pos.CENTER);

    HBox wrapper = new HBox(icon);
    wrapper.setAlignment(Pos.CENTER);
    return wrapper;
  }

  private Label buildLabel() {
    Label label = new Label();
    label.setWrapText(true);
    label.setMaxHeight(Region.USE_COMPUTED_SIZE);
    label.setMaxWidth(Region.USE_COMPUTED_SIZE);
    return label;
  }

  private Label buildTipLabel() {
    Label label = buildLabel();
    label.getStyleClass().add("tip-banner-label");
    return label;
  }

  private VBox buildTitleBlock() {
    Label title = new Label("Millions");
    title.getStyleClass().add("title-label");

    Label subtitle = new Label("Master the market. Build your fortune.");
    subtitle.setWrapText(true);
    subtitle.getStyleClass().add("subtitle-label");

    VBox block = new VBox(6, title, subtitle);
    block.setAlignment(Pos.CENTER);
    return block;
  }

  private List<String> loadQuotes() {
    CsvUtil fileReader = CsvUtil.getCsvUtil();
    return fileReader.readCsvFile("src/main/resources/storage/mainmenu.csv");
  }

  private void startQuoteAnimation(List<String> quotes,VBox tipConatiner, Label quoteLabel, Label authorLabel) {
    AtomicInteger index = new AtomicInteger(2);
    quoteLabel.setText(quotes.get(index.getAndIncrement()));
    authorLabel.setText(quotes.get(index.getAndIncrement()));
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(7), _ -> {
      if (index.get() == quotes.size()) {
        index.set(2);
      }
      FadeTransition fade = new FadeTransition(Duration.millis(700), tipConatiner);
      fade.setFromValue(1); fade.setToValue(0);
      fade.setOnFinished(_ -> {
        quoteLabel.setText(quotes.get(index.getAndIncrement()));
        authorLabel.setText(quotes.get(index.getAndIncrement()));
        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), tipConatiner);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        fadeIn.play();
      });
      fade.play();
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  private List<Circle> createBackgroundCircles() {
    List<Circle> circles = new ArrayList<>();
    Random random = new Random();
    Double randomAmount = random.nextGaussian();
    if (randomAmount < 0) {
      randomAmount = randomAmount * -1;
    }
    for (int i = 1; i <= randomAmount * 70; i++) {
      Circle circle = new Circle();

      circle.setRadius(3);

      Color[] colors = {
          Color.web("#5c6ef5", 0.3),
          Color.web("#7c6ef5", 0.2),
          Color.web("#3fcf8e", 0.3),
          Color.web("#ffffff", 0.2)
      };
      circle.setFill(colors[random.nextInt(colors.length)]);

      circle.setCenterX(random.nextDouble() * Screen.getPrimary().getBounds().getWidth());
      circle.setCenterY(random.nextDouble() * Screen.getPrimary().getBounds().getHeight());

      animateCircle(circle, random);
      circles.add(circle);
    }

    return circles;
  }

  private void animateCircle(Circle circle, Random random) {
    double duration = 4 + random.nextDouble() * 6;
    double drift    = 80 + random.nextDouble() * 120;
    double wobble   = random.nextGaussian() * 30;

    FadeTransition fade = new FadeTransition(Duration.seconds(duration), circle);
    fade.setFromValue(0.5 + random.nextDouble() * 0.3);
    fade.setToValue(0);

    TranslateTransition move = new TranslateTransition(Duration.seconds(duration), circle);
    move.setByY(-drift);
    move.setByX(wobble);

    move.setOnFinished(e -> {
      circle.setCenterX(random.nextDouble() * Screen.getPrimary().getBounds().getWidth());
      circle.setCenterY(random.nextDouble() * Screen.getPrimary().getBounds().getHeight());
      circle.setTranslateX(0);
      circle.setTranslateY(0);
      circle.setOpacity(0);

      PauseTransition pause = new PauseTransition(
          Duration.millis(random.nextDouble() * 1500)
      );
      pause.setOnFinished(p -> animateCircle(circle, random));
      pause.play();
    });

    FadeTransition fadeIn = new FadeTransition(Duration.millis(800), circle);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(0.5 + random.nextDouble() * 0.3);
    fadeIn.setOnFinished(f -> { fade.play(); move.play(); });
    fadeIn.play();
  }

  private VBox buildCard() {
    Label sectionLabel = new Label("New Game");
    sectionLabel.getStyleClass().add("card-section-label");

    nameField.setPromptText("Enter your trader name...");
    nameField.getStyleClass().add("text-field");
    HBox.setHgrow(nameField, Priority.ALWAYS);

    playButton.getStyleClass().add("button-primary");
    playButton.setMinWidth(90);

    HBox inputRow = new HBox(10, nameField, playButton);

    Label footer = new Label("Start with $10,000  •  Real-time Events");
    footer.getStyleClass().add("footer-label");

    VBox card = new VBox(16, sectionLabel, inputRow, footer);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_LEFT);

    return card;
  }

  private void wireEvents() {
    playButton.setOnAction(_ -> handlePlay());

    nameField.setOnAction(_ -> handlePlay());
  }

  private void handlePlay() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      shakeField();
      return;
    }
    animatePlayButton();
  }

  /** Staggered fade + slide-up entrance for the whole menu */
  private void playEntranceAnimation() {
    view.setOpacity(0);

    FadeTransition fade = new FadeTransition(Duration.millis(600), view);
    fade.setFromValue(0);
    fade.setToValue(1);

    TranslateTransition slide = new TranslateTransition(Duration.millis(500), view);
    slide.setFromY(20);
    slide.setToY(0);

    fade.play();
    slide.play();
  }

  /** Brief horizontal shake on the name field when submitted empty */
  private void shakeField() {
    if (!isPlaying) {
      isPlaying = true;
      nameField.setTranslateX(0);
      shakeAnimation.playFromStart();
      nameField.setStyle("-fx-border-color: #f0637a;");
      nameField.focusedProperty().addListener((_, _, _) -> nameField.setStyle(""));
    }
  }

  /** Scale-pulse on the Play button, then fire callback */
  private void animatePlayButton() {
    ScaleTransition pulse = new ScaleTransition(Duration.millis(120), playButton);
    pulse.setToX(0.92);
    pulse.setToY(0.92);
    pulse.setCycleCount(2);
    pulse.setAutoReverse(true);
  }
}