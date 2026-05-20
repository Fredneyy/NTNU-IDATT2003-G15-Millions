package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.MainMenuController;
import ntnu.idatt2003.group15.model.SaveData;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
  private final VBox continuePlayingList = new VBox(8);
  private final Label continuePlayingEmpty = new Label("No saved games yet.");
  private VBox continueCard;
  private final Label stocksStatusLabel = new Label("Default stocks");
  private final Button chooseStocksButton = new Button("Use custom stocks…");
  private final Button resetStocksButton = new Button("Reset");
  private List<Stock> customStocks;

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


    continueCard = buildContinueCard();
    doRefreshContinueCard();

    center.getChildren().addAll(
        buildIcon(),
        buildTitleBlock(),
        tipContainer,
        buildCard(),
        continueCard
    );

    Pane particleLayer = new Pane();
    particleLayer.setEffect(new GaussianBlur(2));
    particleLayer.getChildren().addAll(createBackgroundCircles());
    particleLayer.setMouseTransparent(true);

    // Anchor the menu to the top so growing content (e.g. the Continue Playing
    // card) pushes downward only, and overflow becomes scrollable.
    StackPane centerHolder = new StackPane(center);
    centerHolder.setAlignment(Pos.TOP_CENTER);
    centerHolder.setPadding(new javafx.geometry.Insets(48, 0, 48, 0));

    ScrollPane scroll = new ScrollPane(centerHolder);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scroll.getStyleClass().add("menu-scroll");
    StackPane.setAlignment(scroll, Pos.TOP_CENTER);

    view.getChildren().addAll(particleLayer, scroll);
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
    playButton.setMaxWidth(Double.MAX_VALUE);

    VBox inputFields = new VBox(10, nameField, startingMoneyField);
    HBox.setHgrow(inputFields, Priority.ALWAYS);

    VBox stocksSection = buildStocksSection();

    VBox card = new VBox(16, sectionBox, inputFields, stocksSection, playButton);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.CENTER_LEFT);

    return card;
  }

  private VBox buildStocksSection() {
    Label title = new Label("Stocks:");
    title.getStyleClass().add("save-meta");
    stocksStatusLabel.getStyleClass().add("save-meta");
    HBox header = new HBox(6, title, stocksStatusLabel);
    header.setAlignment(Pos.CENTER_LEFT);

    FontIcon chooseIcon = new FontIcon(FontAwesome.UPLOAD);
    chooseIcon.getStyleClass().add("load-save-icon");
    chooseStocksButton.setGraphic(chooseIcon);
    chooseStocksButton.getStyleClass().add("load-save-button");
    HBox.setHgrow(chooseStocksButton, Priority.ALWAYS);
    chooseStocksButton.setMaxWidth(Double.MAX_VALUE);

    resetStocksButton.getStyleClass().add("load-save-button");
    resetStocksButton.setVisible(false);
    resetStocksButton.setManaged(false);

    HBox controls = new HBox(8, chooseStocksButton, resetStocksButton);
    controls.setAlignment(Pos.CENTER_LEFT);

    return new VBox(6, header, controls);
  }

  private VBox buildContinueCard() {
    FontIcon uploadIcon = new FontIcon(FontAwesome.UPLOAD);
    uploadIcon.getStyleClass().add("continue-icon");
    Label sectionLabel = new Label("Continue Playing");
    sectionLabel.getStyleClass().add("card-section-label");
    HBox sectionBox = new HBox(8, uploadIcon, sectionLabel);
    sectionBox.setAlignment(Pos.CENTER_LEFT);

    continuePlayingList.setFillWidth(true);
    continuePlayingEmpty.getStyleClass().add("continue-empty");
    continuePlayingEmpty.setMaxWidth(Double.MAX_VALUE);
    continuePlayingEmpty.setAlignment(Pos.CENTER);

    FontIcon folderIcon = new FontIcon(FontAwesome.FOLDER_OPEN);
    folderIcon.getStyleClass().add("load-save-icon");
    Button loadOther = new Button("Load different save…");
    loadOther.setGraphic(folderIcon);
    loadOther.getStyleClass().add("load-save-button");
    loadOther.setMaxWidth(Double.MAX_VALUE);
    loadOther.setOnAction(_ -> loadFromFileChooser());

    VBox card = new VBox(16, sectionBox, continuePlayingList, loadOther);
    card.getStyleClass().add("card");
    card.setAlignment(Pos.TOP_LEFT);
    return card;
  }

  /** Re-read the save index and repopulate the Continue Playing card. */
  public void refreshContinueCard() {
    doRefreshContinueCard();
  }

  private void doRefreshContinueCard() {
    List<SaveIndex.Entry> entries = SaveIndex.prune();
    continuePlayingList.getChildren().clear();
    if (entries.isEmpty()) {
      continuePlayingList.getChildren().add(continuePlayingEmpty);
      return;
    }
    // Keep the menu compact: show at most the three most-recent saves.
    int max = Math.min(3, entries.size());
    for (int i = 0; i < max; i++) {
      continuePlayingList.getChildren().add(buildSaveRow(entries.get(i)));
    }
  }

  private HBox buildSaveRow(SaveIndex.Entry entry) {
    String name = entry.playerName() == null || entry.playerName().isBlank() ? "Trader" : entry.playerName();

    Label avatar = new Label(name.substring(0, 1).toUpperCase());
    avatar.getStyleClass().add("save-avatar");
    avatar.setMinSize(44, 44);
    avatar.setPrefSize(44, 44);
    avatar.setAlignment(Pos.CENTER);

    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add("save-name");

    FontIcon dollarIcon = new FontIcon(FontAwesome.DOLLAR);
    dollarIcon.getStyleClass().add("save-meta-icon");
    Label cashLabel = new Label(formatMoney(entry.cash()));
    cashLabel.getStyleClass().add("save-meta");
    // Refuse to shrink past their natural size — otherwise the cash value gets
    // chopped to "$8..." when the row is narrow.
    cashLabel.setMinWidth(Region.USE_PREF_SIZE);

    FontIcon clockIcon = new FontIcon(FontAwesome.CLOCK_O);
    clockIcon.getStyleClass().add("save-meta-icon");
    Label whenLabel = new Label(formatWhen(entry.savedAt()));
    whenLabel.getStyleClass().add("save-meta");
    whenLabel.setMinWidth(Region.USE_PREF_SIZE);

    // Two stacked sub-rows keep both money and date fully visible even on
    // narrow menu widths instead of cramming everything into one HBox.
    HBox cashRow = new HBox(6, dollarIcon, cashLabel);
    cashRow.setAlignment(Pos.CENTER_LEFT);
    cashRow.getStyleClass().add("save-meta-row");

    HBox whenRow = new HBox(6, clockIcon, whenLabel);
    whenRow.setAlignment(Pos.CENTER_LEFT);
    whenRow.getStyleClass().add("save-meta-row");

    VBox text = new VBox(2, nameLabel, cashRow, whenRow);
    HBox.setHgrow(text, Priority.ALWAYS);
    HBox row = new HBox(14, avatar, text);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("save-row");
    row.setOnMouseClicked(_ -> loadFromPath(entry.path()));
    return row;
  }

  private void loadFromFileChooser() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Load Game");
    chooser.getExtensionFilters().add(
        new FileChooser.ExtensionFilter("Millions save file (*.json)", "*.json"));
    File picked = chooser.showOpenDialog(view.getScene() == null ? null : view.getScene().getWindow());
    if (picked != null) loadFromFile(picked);
  }

  private void loadFromPath(String absolutePath) {
    if (absolutePath == null) return;
    loadFromFile(new File(absolutePath));
  }

  private void loadFromFile(File file) {
    if (!file.exists()) {
      showError("Save not found", file.getAbsolutePath());
      doRefreshContinueCard();
      return;
    }
    try {
      SaveData save = LoadGameUtil.load(file);
      mainMenuController.loadGame(save);
      close();
    } catch (IOException | RuntimeException ex) {
      showError("Could not load save", ex.getMessage() == null ? ex.toString() : ex.getMessage());
    }
  }

  private static String formatMoney(BigDecimal v) {
    if (v == null) return "$—";
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
    nf.setMaximumFractionDigits(0);
    return "$" + nf.format(v);
  }

  private static final DateTimeFormatter WHEN_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, yyyy, hh:mm a", Locale.US);

  private static String formatWhen(java.time.Instant when) {
    if (when == null) return "—";
    return WHEN_FORMAT.format(when.atZone(ZoneId.systemDefault()));
  }

  private static void showError(String header, String message) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle("Millions");
    a.setHeaderText(header);
    a.setContentText(message);
    a.showAndWait();
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

    chooseStocksButton.setOnAction(_ -> chooseCustomStocks());
    resetStocksButton.setOnAction(_ -> resetStocks());
  }

  private void chooseCustomStocks() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose custom stocks");
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Stock data (*.csv, *.json)", "*.csv", "*.json"),
        new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"),
        new FileChooser.ExtensionFilter("JSON (*.json)", "*.json"));
    File picked = chooser.showOpenDialog(view.getScene() == null ? null : view.getScene().getWindow());
    if (picked == null) return;
    try {
      List<Stock> loaded = new StockLoader(picked.getAbsolutePath()).load();
      if (loaded.isEmpty()) {
        showError("Empty stock file", "The selected file contains no stocks.");
        return;
      }
      customStocks = loaded;
      stocksStatusLabel.setText(picked.getName() + " (" + loaded.size() + ")");
      resetStocksButton.setVisible(true);
      resetStocksButton.setManaged(true);
    } catch (RuntimeException ex) {
      showError("Could not load stocks",
          ex.getMessage() == null ? ex.toString() : ex.getMessage());
    }
  }

  private void resetStocks() {
    customStocks = null;
    stocksStatusLabel.setText("Default stocks");
    resetStocksButton.setVisible(false);
    resetStocksButton.setManaged(false);
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
          mainMenuController.startGame(
              name, BigDecimal.valueOf(Long.parseLong(startingMoney)), customStocks);
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