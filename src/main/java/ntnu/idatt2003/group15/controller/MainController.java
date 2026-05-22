package ntnu.idatt2003.group15.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.utilities.CsvParser;
import ntnu.idatt2003.group15.utilities.NewsParser.NewsLoader;
import ntnu.idatt2003.group15.utilities.StockParser.StockLoader;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.GameView;
import ntnu.idatt2003.group15.view.MainMenu;
import ntnu.idatt2003.group15.view.dialog.OnBoardingDialog;

import java.util.List;
import java.util.function.Consumer;
import java.util.Random;

public class MainController {

  private final MainMenu mainMenu;
  private final CsvParser csvParser;
  private final TaskUtil taskUtil;
  private final StackPane root;
  private final NewsController newsController;
  private final GameSettings gameSettings = new GameSettings();
  private final Consumer<Throwable> errorHandler;
  private Timeline priceTicker;
  private PauseTransition newsTicker;
  private final Random random = new Random();

  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvParser csvParser,
                        TaskUtil taskUtil, List<Stock> stocks) {
    this.csvParser = csvParser;
    this.taskUtil = taskUtil;
    this.root = root;
    this.errorHandler = errorHandler;

    MainMenuController mainMenuController = new MainMenuController(new Exchange("OSEBX", loadStocks()), this::startGame);
    mainMenuController.setGameSettings(gameSettings);
    mainMenuController.setOnGameLoadConsumer(this::resumeGame);
    mainMenu = new MainMenu(root, errorHandler, csvParser, taskUtil, mainMenuController);
    newsController = new NewsController(root, gameSettings, new NewsArchive(loadNewsItems()));
  }

  private List<Stock> loadStocks() {
    StockLoader stockLoader = new StockLoader("src/main/resources/storage/defaultstocks.csv");
    List<Stock> stocks = stockLoader.load();
    return stocks;
  }

  private List<NewsItem> loadNewsItems() {
    NewsLoader newsLoader = new NewsLoader("src/main/resources/storage/stock_news.csv");
    List<NewsItem> newsItems = newsLoader.load();
    return newsItems;
  }

  public void showMainMenu() {
    stopPriceTicker();
    stopNewsTicker();
    // Clear any blur/effect that may have leaked onto the menu from an open
    // dialog (e.g. logging out while the onboarding overlay is still up).
    mainMenu.getView().setEffect(null);
    mainMenu.refreshContinueCard();
    root.getChildren().setAll(mainMenu.getView());
  }

  private void startGame(ExchangeController exchangeController, PlayerController playerController) {
    enterGame(exchangeController, playerController, true);
  }

  private void resumeGame(ExchangeController exchangeController, PlayerController playerController) {
    enterGame(exchangeController, playerController, false);
  }

  private void enterGame(ExchangeController exchangeController, PlayerController playerController,
                         boolean showOnboarding) {
    GameView gameView = new GameView(playerController, exchangeController, gameSettings,
        this::showMainMenu, errorHandler, newsController);
    gameView.show(root);
    exchangeController.setNewsObserver(newsController.getNewsObservable());
    // Take the main menu out of the scene so it can't be blurred (or otherwise
    // affected) by overlays drawn on top of the game view.
    root.getChildren().remove(mainMenu.getView());
    if (showOnboarding) {
      new OnBoardingDialog(csvParser, taskUtil).show(root);
    }

    try {
      exchangeController.setVolatilityMultiplier(gameSettings.getVolatilityMultiplier());
    } catch (RuntimeException ex) {
      errorHandler.accept(ex);
    }
    gameSettings.volatilityMultiplierProperty().addListener((_, _, v) -> {
      try {
        exchangeController.setVolatilityMultiplier(v.doubleValue());
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
    });
    
    // Subscribe to news interval changes to adjust the ticker frequency dynamically
    gameSettings.newsIntervalSecondsProperty().addListener((_, _, _) -> {
      if (newsTicker != null) {
          startNewsTicker(newsController);
      }
    });

    startPriceTicker(exchangeController);
    startNewsTicker(newsController);
  }

  private void startNewsTicker(NewsController newsController) {
    stopNewsTicker();

    double meanSeconds = gameSettings.getNewsIntervalSeconds(); // Fetch bound duration from settings
    double stdDev = meanSeconds / 3.0; // Standard deviation scales based on the mean
    double minSeconds = 5.0; // Absolute bare minimum to avoid spam

    double nextDuration = meanSeconds + random.nextGaussian() * stdDev;
    if (nextDuration < minSeconds) {
      nextDuration = minSeconds;
    }

    newsTicker = new PauseTransition(Duration.seconds(nextDuration));
    newsTicker.setOnFinished(event -> {
      try {
        newsController.publish();
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
      startNewsTicker(newsController);
    });
    newsTicker.play();
  }

  private void stopNewsTicker() {
    if (newsTicker != null) {
      newsTicker.stop();
      newsTicker = null;
    }
  }

  private void startPriceTicker(ExchangeController exchangeController) {
    stopPriceTicker();
    priceTicker = new Timeline(new KeyFrame(Duration.seconds(5), _ -> {
      try {
        exchangeController.advanceWeek();
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
    }));
    priceTicker.setCycleCount(Animation.INDEFINITE);
    priceTicker.play();
  }

  private void stopPriceTicker() {
    if (priceTicker != null) {
      priceTicker.stop();
      priceTicker = null;
    }
  }
}
