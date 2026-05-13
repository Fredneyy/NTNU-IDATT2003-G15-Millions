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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class MainController {

  private final MainMenu mainMenu;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private final StackPane root;
  private ExchangeController exchangeController;
  private PlayerController playerController;
  private final MainMenuController mainMenuController;
  private final NewsController newsController;
  private Timeline priceTicker;
  private StockSimulator simulator;
  private Map<String, Stock> stocksBySymbol;


  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    mainMenuController = new MainMenuController(new Exchange("OSEBX", seedStocks()), this::startGame);
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil, mainMenuController);
    newsController = new NewsController(root);
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;
  }

  public void showMainMenu() {
    newsController.stop();
    stopPriceTicker();
    root.getChildren().setAll(mainMenu.getView());
  }

  private void startGame(ExchangeController exchangeController, PlayerController playerController) {
    this.playerController = playerController;
    this.exchangeController = exchangeController;
    GameView gameView = new GameView(playerController, exchangeController, this::showMainMenu);
    List<Stock> stocks = exchangeController.getAllStocks();
    gameView.setMarketStocks(stocks);
    root.getChildren().setAll(gameView.getView());
    OnBoardingDialog onBoardingDialog = new OnBoardingDialog(csvUtil, taskUtil);
    onBoardingDialog.show(root);
    newsController.start();
    newsController.setOnNewsEmitted(item -> {
      gameView.getNewsFeedView().prependEvent(item);
      // Route headline-style events through the simulator: any stock matching
      // the item's symbol gets shocked + an elevated-volatility window.
      Stock target = item.symbol() == null ? null : stocksBySymbol.get(item.symbol().toUpperCase());
      if (target != null) {
        simulator.applyNews(item, target);
      }
    });
    startPriceTicker(stocks);
  }

  private void startPriceTicker(List<Stock> stocks) {
    stopPriceTicker();
    simulator = new StockSimulator(1.0 / 52.0); // ~1 trading week per tick
    stocksBySymbol = new HashMap<>();
    for (Stock s : stocks) stocksBySymbol.put(s.getSymbol().toUpperCase(), s);

    priceTicker = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
      for (Stock stock : stocks) {
        BigDecimal next = simulator.nextPrice(stock);
        if (next.signum() > 0) {
          stock.addNewSalesPrice(next);
        }
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
    simulator = null;
    stocksBySymbol = null;
  }

  private static List<Stock> seedStocks() {
    Random rng = new Random();
    List<Stock> stocks = new ArrayList<>();
    stocks.add(seed(rng, "EQNR",  "Equinor ASA",        "295.20", "298.10", "301.45", "300.80"));
    stocks.add(seed(rng, "DNB",   "DNB Bank ASA",       "210.50", "212.75", "215.40", "218.90"));
    stocks.add(seed(rng, "TEL",   "Telenor ASA",        "135.20", "133.80", "131.50", "129.95"));
    stocks.add(seed(rng, "YAR",   "Yara International", "342.00", "338.50", "335.20", "340.10"));
    stocks.add(seed(rng, "NHY",   "Norsk Hydro ASA",    "62.40",  "63.15",  "65.80",  "68.20"));
    stocks.add(seed(rng, "MOWI",  "Mowi ASA",           "188.30", "186.50", "184.20", "182.75"));
    stocks.add(seed(rng, "AKER",  "Aker BP ASA",        "275.00", "281.20", "289.40", "295.10"));
    stocks.add(seed(rng, "ORK",   "Orkla ASA",          "82.50",  "82.80",  "83.10",  "82.95"));
    stocks.add(seed(rng, "SCATC", "Scatec ASA",         "55.30",  "52.10",  "49.80",  "47.20"));
    stocks.add(seed(rng, "REC",   "REC Silicon",        "12.40",  "13.20",  "14.80",  "16.55"));
    stocks.add(seed(rng, "KAHOT", "Kahoot! ASA",        "28.10",  "27.50",  "26.90",  "26.40"));
    stocks.add(seed(rng, "NEL",   "Nel ASA",            "5.85",   "5.42",   "4.98",   "4.65"));
    return stocks;
  }

  /** Build a stock with random baseline drift in [-5%, +15%] and σ in [0.15, 0.55]. */
  private static Stock seed(Random rng, String symbol, String company, String... priceHistory) {
    Stock stock = new Stock(symbol, company, new BigDecimal(priceHistory[0]));
    for (int i = 1; i < priceHistory.length; i++) {
      stock.addNewSalesPrice(new BigDecimal(priceHistory[i]));
    }
    stock.setDrift(-0.05 + rng.nextDouble() * 0.20);
    stock.setVolatility(0.15 + rng.nextDouble() * 0.40);
    return stock;
  }
}
