package ntnu.idatt2003.group15.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.model.news.NewsArchive;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.utilities.CsvParser;
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
                        TaskUtil taskUtil, List<Stock> stocks, List<NewsItem> news) {
    this.csvParser = csvParser;
    this.taskUtil = taskUtil;
    this.root = root;
    this.errorHandler = errorHandler;

    MainMenuController mainMenuController = new MainMenuController(new Exchange("OSEBX", stocks), this::startGame);
    mainMenuController.setGameSettings(gameSettings);
    mainMenuController.setOnGameLoadConsumer(this::resumeGame);

    mainMenu = new MainMenu(root, errorHandler, csvParser, taskUtil, mainMenuController);
    newsController = new NewsController(new NewsArchive(news));
  }

  public void showMainMenu() {
    stopPriceTicker();
    
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
        this::showMainMenu, errorHandler, newsController, () -> {
      try {
        exchangeController.advanceWeek();
        newsController.advanceWeek();
        newsController.publish(gameSettings.getMaxEventChance());
      } catch (RuntimeException ex) {
        errorHandler.accept(ex);
      }
    }, () -> startPriceTicker(exchangeController), this::stopPriceTicker);
    gameView.show(root);
    exchangeController.setNewsObserver(newsController.getNewsObservable());
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
  }

  private void startPriceTicker(ExchangeController exchangeController) {
    stopPriceTicker();
    priceTicker = new Timeline(new KeyFrame(Duration.seconds(3), _ -> {
      try {
        exchangeController.advanceWeek();
        newsController.advanceWeek();
        newsController.publish(gameSettings.getMaxEventChance());
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
