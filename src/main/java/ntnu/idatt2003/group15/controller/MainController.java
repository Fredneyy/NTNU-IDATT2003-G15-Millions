package ntnu.idatt2003.group15.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.GameView;
import ntnu.idatt2003.group15.view.MainMenu;
import ntnu.idatt2003.group15.view.NewsDialog;
import ntnu.idatt2003.group15.view.OnBoardingDialog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainController {

  private final MainMenu mainMenu;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private final StackPane root;
  private final NewsController newsController;
  private final GameSettings gameSettings = new GameSettings();
  private Timeline priceTicker;

  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;

    MainMenuController mainMenuController = new MainMenuController(new Exchange("OSEBX", loadStocks()), this::startGame);
    mainMenuController.setGameSettings(gameSettings);
    mainMenuController.setOnGameLoadConsumer(this::resumeGame);
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil, mainMenuController);
    newsController = new NewsController(root, gameSettings, new NewsArchive());
    seedNews(newsController);
  }

  public void showMainMenu() {
    stopPriceTicker();
    // Clear any blur/effect that may have leaked onto the menu from an open
    // dialog (e.g. logging out while the onboarding overlay is still up).
    mainMenu.getView().setEffect(null);
    mainMenu.refreshContinueCard();
    root.getChildren().setAll(mainMenu.getView());
  }

  private List<Stock> loadStocks() {
    List<List<String>> rawStockValues = new ArrayList<>(
        csvUtil.readCsvFile("src/main/resources/storage/stocks.csv")
    );
    rawStockValues.removeFirst();
    List<Stock> stocks = new ArrayList<>();
    for (List<String> stockvalue : rawStockValues) {
      String[] sectorsStrings = stockvalue.getLast().split("\\|");
      List<StockSectors> sectors = new ArrayList<>();
      for (String sector : sectorsStrings) {
        sectors.add(StockSectors.fromLabel(sector));
      }
      stocks.add(new Stock(
          stockvalue.getFirst(),
          stockvalue.get(1),
          BigDecimal.valueOf(Double.parseDouble(stockvalue.get(2))),
          Double.parseDouble(stockvalue.get(3)),
          Double.parseDouble(stockvalue.get(4)),
          sectors)
      );
    }
    return stocks;
  }

  private void startGame(ExchangeController exchangeController, PlayerController playerController) {
    enterGame(exchangeController, playerController, true);
  }

  private void resumeGame(ExchangeController exchangeController, PlayerController playerController) {
    enterGame(exchangeController, playerController, false);
  }

  private void enterGame(ExchangeController exchangeController, PlayerController playerController,
                         boolean showOnboarding) {
    GameView gameView = new GameView(playerController, exchangeController, gameSettings, this::showMainMenu, newsController);
    gameView.show(root);
    // Take the main menu out of the scene so it can't be blurred (or otherwise
    // affected) by overlays drawn on top of the game view.
    root.getChildren().remove(mainMenu.getView());
    if (showOnboarding) {
      new OnBoardingDialog(csvUtil, taskUtil).show(root);
    }

    // Propagate the current volatility multiplier and keep it in sync as the user adjusts settings.
    exchangeController.setVolatilityMultiplier(gameSettings.getVolatilityMultiplier());
    exchangeController.setNewsObserver(newsController.getNewsObservable());
    gameSettings.volatilityMultiplierProperty().addListener(
        (_, _, v) -> exchangeController.setVolatilityMultiplier(v.doubleValue()));

    startPriceTicker(exchangeController);
  }

  private void startPriceTicker(ExchangeController exchangeController) {
    stopPriceTicker();
    priceTicker = new Timeline(new KeyFrame(
        Duration.seconds(2), _ -> {
          exchangeController.advanceWeek();
          newsController.advanceWeek();
    }));
    priceTicker.setCycleCount(Animation.INDEFINITE);
    priceTicker.play();
    Timeline timeline = new Timeline(new KeyFrame(
        Duration.seconds(10), _ -> {
          newsController.publish();
    }));
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  private void stopPriceTicker() {
    if (priceTicker != null) {
      priceTicker.stop();
      priceTicker = null;
    }
  }

  private void seedNews(NewsController newsController) {
    newsController.addNewsItem(new NewsItem(
        NewsDialog.Sentiment.BULLISH,
        StockSectors.TECHNOLOGY,
        new BigDecimal("1.2"),
        "Tech rally",
        "Strong earnings lifted tech stocks this week.",
        new BigDecimal("0.15"),
        10,
        "sector",
        null,
        false
    ));

    newsController.addNewsItem(new NewsItem(
        NewsDialog.Sentiment.BEARISH,
        StockSectors.ENERGY,
        new BigDecimal("0.7"),
        "Energy pullback",
        "Oil demand worries hit the energy sector.",
        new BigDecimal("0.20"),
        5,
        "sector",
        null,
        false
    ));

    newsController.addNewsItem(new NewsItem(
        NewsDialog.Sentiment.BULLISH,
        StockSectors.TECHNOLOGY,
        new BigDecimal("1.2"),
        "Tech rally",
        "Strong earnings lifted tech stocks this week.",
        new BigDecimal("0.15"),
        10,
        "sector",
        null,
        false
    ));

    newsController.addNewsItem(new NewsItem(
        NewsDialog.Sentiment.BEARISH,
        StockSectors.ENERGY,
        new BigDecimal("0.8"),
        "Energy pullback",
        "Oil demandsorries hitdgy sector.",
        new BigDecimal("0.20"),
        5,
        "sector",
        null,
        false
    ));
  }

}
