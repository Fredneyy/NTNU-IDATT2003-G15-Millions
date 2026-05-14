package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.MainMenuController;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import ntnu.idatt2003.group15.utilities.*;

public class MainMenu {

  private final StackPane view = new StackPane();
  private final TextField nameField = new TextField();
  private final TextField startingMoneyField = new TextField();
  private final Button playButton = new Button();
  private final FontIcon playIcon = new FontIcon(FontAwesome.PLAY);
  private final Label quoteLabel;
  private final Label authorLabel;
  private final VBox tipContainer = new VBox();
  private final StackPane root;
  private final TranslateTransition shakeAnimationNameField;
  private final TranslateTransition shakeAnimationStartMoneyField;
  Random random = new Random();
  Consumer<Throwable> errorHandler;
  CsvUtil csvUtil;
  TaskUtil taskUtil;
  private final MainMenuController mainMenuController;

  public MainMenu(StackPane root, Consumer<Throwable> errorHandler,
                  CsvUtil csvUtil, TaskUtil taskUtil,
                  MainMenuController mainMenuController) throws NullPointerException {
    Objects.requireNonNull(root);
    Objects.requireNonNull(errorHandler);
    Objects.requireNonNull(csvUtil);
    Objects.requireNonNull(taskUtil);
    Objects.requireNonNull(mainMenuController);

    this.root = root;
    this.errorHandler = errorHandler;
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.mainMenuController = mainMenuController;

    shakeAnimationNameField = configureShakeAnimation(nameField);
    shakeAnimationStartMoneyField = configureShakeAnimation(startingMoneyField);

    quoteLabel = buildTipLabel();
    authorLabel = buildTipLabel();

    buildUI();
    wireEvents();
    loadQuotes();
  }

  public StackPane getView() {
    return view;
  }

  private void buildUI() {
    view.getStylesheets().add(
        Objects.requireNonNull(getClass().getResource("/style/MainMenuStyle.css")).toExternalForm());
    view.setAlignment(Pos.CENTER);

    VBox center = new VBox(24);
    center.setAlignment(Pos.CENTER);
    center.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 4.0);
    tipContainer.setSpacing(5);
    tipContainer.setMinHeight(90);
    tipContainer.getStyleClass().add("tip-banner");
    tipContainer.setFillWidth(true);
    tipContainer.setVisible(false);

    HBox quoteWrapper = new HBox();
    VBox.setVgrow(quoteWrapper, Priority.ALWAYS);
    quoteWrapper.setMaxHeight(Double.MAX_VALUE);

    HBox authorWrapper = new HBox();
    VBox.setVgrow(authorWrapper, Priority.ALWAYS);
    authorWrapper.setAlignment(Pos.BOTTOM_RIGHT);
    authorWrapper.setMaxHeight(Double.MAX_VALUE);

    authorLabel.getStyleClass().add("author");
    authorWrapper.getChildren().add(authorLabel);
    quoteWrapper.getChildren().add(quoteLabel);
    tipContainer.getChildren().addAll(quoteWrapper, authorWrapper);

    playButton.setGraphic(playIcon);
    VBox.setVgrow(playButton, Priority.ALWAYS);


    center.getChildren().addAll(
        buildIcon(),
        buildTitleBlock(),
        tipContainer,
        buildCard()
    );

    Pane particleLayer = new Pane();
    particleLayer.setEffect(new GaussianBlur(2));
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

  private void loadQuotes() {
    taskUtil.runTaskAsync(() -> {
      List<List<String>> rawQuotes = new ArrayList<>(
          csvUtil.readCsvFile("src/main/resources/storage/mainmenu.csv")
      );
      rawQuotes.removeFirst();
      Collections.shuffle(rawQuotes);
      return rawQuotes;
    }, result -> {
      startQuoteAnimation(result, tipContainer, quoteLabel, authorLabel);
      tipContainer.setVisible(true);
    }, errorHandler);
  }

  private void startQuoteAnimation(List<List<String>> quotes,VBox tipContainer, Label qouteLabel, Label authorLabel) {
    AtomicInteger index = new AtomicInteger(2);

    if (quotes.isEmpty()) {
      qouteLabel.setText("Millions the game");
      authorLabel.setText("Master the game");
    } else {
      qouteLabel.setText(quotes.get(index.getAndIncrement()).getFirst());
      authorLabel.setText(quotes.get(index.get()).getLast());

      scheduleAnimation(quotes, tipContainer, qouteLabel, authorLabel, index);
    }
  }

  private void scheduleAnimation(List<List<String>> quotes,VBox tipContainer, Label qouteLabel, Label authorLabel, AtomicInteger index) {
    if (index.get() == quotes.size()) {
      index.set(2);
    }
    double durationDouble = quotes.get(index.get()).getFirst().split(" ").length * 0.5;
    Duration duration = Duration.seconds(durationDouble);

    qouteLabel.setText(quotes.get(index.getAndIncrement()).getFirst());
    authorLabel.setText(quotes.get(index.get()).getLast());

    ParallelTransition fadeInTransitions = new ParallelTransition();
    TranslateTransition translateIn = new TranslateTransition(Duration.millis(800), tipContainer);
    translateIn.setByY(5);
    translateIn.setInterpolator(Interpolator.EASE_OUT);
    FadeTransition fadeIn = new FadeTransition(Duration.millis(800), tipContainer);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);
    fadeInTransitions.getChildren().addAll(translateIn, fadeIn);
    fadeInTransitions.play();

    Timeline timeline = new Timeline();
    KeyFrame keyFrame = new KeyFrame(duration, _ -> {
      ParallelTransition fadeOutTransitions = new ParallelTransition();

      TranslateTransition translateOut = new TranslateTransition(Duration.millis(800), tipContainer);
      translateOut.setByY(5);
      translateOut.setInterpolator(Interpolator.EASE_IN);
      translateOut.setOnFinished(_ -> tipContainer.setTranslateY(-5));

      FadeTransition fadeOut = new FadeTransition(Duration.millis(800), tipContainer);
      fadeOut.setFromValue(1);
      fadeOut.setToValue(0);
      fadeOutTransitions.getChildren().addAll(translateOut, fadeOut);
      fadeOutTransitions.setOnFinished(_ -> scheduleAnimation(quotes,tipContainer, qouteLabel, authorLabel, index));
      fadeOutTransitions.play();
    });
    timeline.setCycleCount(1);
    timeline.getKeyFrames().add(keyFrame);
    timeline.play();
  }

  private List<Circle> createBackgroundCircles() {
    List<Circle> circles = new ArrayList<>();
    double randomAmount = random.nextGaussian();
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
    double drift = 80 + random.nextDouble() * 120;
    double wobble = random.nextGaussian() * 35;

    FadeTransition fade = new FadeTransition(Duration.seconds(duration), circle);
    fade.setFromValue(0.5 + random.nextDouble() * 0.3);
    fade.setToValue(0);

    TranslateTransition move = new TranslateTransition(Duration.seconds(duration), circle);
    move.setByY(-drift);
    move.setByX(wobble);

    ParallelTransition parallelTransition = new ParallelTransition(fade, move);

    move.setOnFinished(_ -> {
      circle.setCenterX(random.nextDouble() * Screen.getPrimary().getBounds().getWidth());
      circle.setCenterY(random.nextDouble() * Screen.getPrimary().getBounds().getHeight());
      circle.setTranslateX(0);
      circle.setTranslateY(0);
      circle.setOpacity(0);

      PauseTransition pause = new PauseTransition(
          Duration.millis(random.nextDouble() * 1500)
      );
      pause.setOnFinished(_ -> animateCircle(circle, random));
      pause.play();
    });

    FadeTransition fadeIn = new FadeTransition(Duration.millis(800), circle);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(0.5 + random.nextDouble() * 0.3);
    parallelTransition.play();
    fadeIn.play();
  }

  private VBox buildCard() {
    FontIcon playIcon =  new FontIcon(FontAwesome.PLAY);
    playIcon.getStyleClass().add("play-icon");
    Label sectionLabel = new Label("New Game");
    sectionLabel.getStyleClass().add("card-section-label");
    HBox sectionBox = new HBox(5, playIcon, sectionLabel);
    sectionBox.setAlignment(Pos.CENTER_LEFT);

    nameField.setPromptText("Enter your trader name...");
    nameField.getStyleClass().add("text-field");
    HBox.setHgrow(nameField, Priority.ALWAYS);

    startingMoneyField.setPromptText("Enter starting money amount...");
    startingMoneyField.getStyleClass().add("text-field");
    HBox.setHgrow(startingMoneyField, Priority.ALWAYS);

    playButton.getStyleClass().add("button-primary");
    playButton.setMinWidth(90);

    VBox inputFields = new VBox(10, nameField, startingMoneyField);
    HBox.setHgrow(inputFields, Priority.ALWAYS);
    HBox inputRow = new HBox(10, inputFields,  playButton);
    inputRow.setAlignment(Pos.CENTER_LEFT);

    Label footer = new Label("Have Fun!");
    footer.getStyleClass().add("footer-label");

    VBox card = new VBox(16, sectionBox, inputRow, footer);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_LEFT);

    return card;
  }

  private void close() {
    ScaleTransition st = new ScaleTransition(Duration.millis(500), view);
    st.setFromX(1.0);
    st.setFromY(1.0);
    st.setToX(0.0);
    st.setToY(0.0);
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), view);
    fadeOut.setFromValue(1);
    fadeOut.setToValue(0);
    ParallelTransition parallelTransition = new ParallelTransition(st, fadeOut);
    parallelTransition.setOnFinished(_ -> {
      root.getChildren().remove(view);
      view.setScaleX(1);
      view.setScaleY(1);
      view.setOpacity(1);
    });
    parallelTransition.play();
  }


  private void wireEvents() {
    playButton.setOnAction(_ -> handlePlay());

    nameField.setOnAction(_ -> handlePlay());
  }

  private void handlePlay() {
    if (playButton.isDisabled()) return;

    String name = nameField.getText().trim();
    String startingMoney = startingMoneyField.getText();
    if (name.isBlank()) {
      shakeField(nameField, shakeAnimationNameField);
    }
    if (!InputValidator.isInt(startingMoney)) {
      shakeField(startingMoneyField, shakeAnimationStartMoneyField);
    }
    if (!name.isBlank() && InputValidator.isInt(startingMoney)) {
        try {
          mainMenuController.startGame(name, BigDecimal.valueOf(Long.parseLong(startingMoney)));
          close();
        } catch (RuntimeException ex) {
          errorHandler.accept(ex);
        }
    }
  }

  private TranslateTransition configureShakeAnimation(TextField textField) {
    TranslateTransition shakeAnimation = new TranslateTransition(Duration.millis(60), textField);
    shakeAnimation.setByX(8);
    shakeAnimation.setCycleCount(6);
    shakeAnimation.setAutoReverse(true);
    shakeAnimation.setOnFinished(_ -> textField.setTranslateX(0));
    return shakeAnimation;
  }

  private void shakeField(TextField textField, TranslateTransition shakeAnimation) {
    textField.setTranslateX(0);
    shakeAnimation.playFromStart();
    textField.setStyle("-fx-border-color: #f0637a;");
    textField.focusedProperty().addListener((_, _, _) -> textField.setStyle(""));
  }
}