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
  private Timeline priceTicker;

  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;

    MainMenuController mainMenuController = new MainMenuController(new Exchange("OSEBX", loadStocks()), this::startGame);
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil, mainMenuController);
    newsController = new NewsController(root);
  }

  public void showMainMenu() {
    newsController.stop();
    stopPriceTicker();
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
    GameView gameView = new GameView(playerController, exchangeController, this::showMainMenu);
    gameView.show(root);
    new OnBoardingDialog(csvUtil, taskUtil).show(root);

    newsController.start();
    newsController.setOnNewsEmitted(item -> {
      gameView.getNewsFeedView().prependEvent(item);
      if (item.sector() != null) {
        exchangeController.applyNews(item);
      }
    });
    startPriceTicker(exchangeController);
  }

  private void startPriceTicker(ExchangeController exchangeController) {
    stopPriceTicker();
    priceTicker = new Timeline(new KeyFrame(
        Duration.seconds(5), _ -> exchangeController.advanceWeek()));
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
