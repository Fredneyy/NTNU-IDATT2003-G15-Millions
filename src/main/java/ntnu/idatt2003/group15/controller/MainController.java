package ntnu.idatt2003.group15.controller;

import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.GameView;
import ntnu.idatt2003.group15.view.MainMenu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MainController {

  private final MainMenu mainMenu;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private final StackPane root;
  private ExchangeController exchangeController;
  private PlayerController playerController;
  private final MainMenuController mainMenuController;


  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    mainMenuController = new MainMenuController(new Exchange("OSEBX", seedStocks()), this::startGame);
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil, mainMenuController);
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;
  }

  public void showMainMenu() {
    root.getChildren().setAll(mainMenu.getView());
  }

  private void startGame(ExchangeController exchangeController, PlayerController playerController) {
    this.playerController = playerController;
    this.exchangeController = exchangeController;
    GameView gameView = new GameView(playerController, exchangeController, this::showMainMenu);
    gameView.setMarketStocks(exchangeController.getAllStocks());
    root.getChildren().setAll(gameView.getView());
  }

  private static List<Stock> seedStocks() {
    List<Stock> stocks = new ArrayList<>();
    stocks.add(seed("EQNR",  "Equinor ASA",              "295.20", "298.10", "301.45", "300.80"));
    stocks.add(seed("DNB",   "DNB Bank ASA",             "210.50", "212.75", "215.40", "218.90"));
    stocks.add(seed("TEL",   "Telenor ASA",              "135.20", "133.80", "131.50", "129.95"));
    stocks.add(seed("YAR",   "Yara International",       "342.00", "338.50", "335.20", "340.10"));
    stocks.add(seed("NHY",   "Norsk Hydro ASA",          "62.40",  "63.15",  "65.80",  "68.20"));
    stocks.add(seed("MOWI",  "Mowi ASA",                 "188.30", "186.50", "184.20", "182.75"));
    stocks.add(seed("AKER",  "Aker BP ASA",              "275.00", "281.20", "289.40", "295.10"));
    stocks.add(seed("ORK",   "Orkla ASA",                "82.50",  "82.80",  "83.10",  "82.95"));
    stocks.add(seed("SCATC", "Scatec ASA",               "55.30",  "52.10",  "49.80",  "47.20"));
    stocks.add(seed("REC",   "REC Silicon",              "12.40",  "13.20",  "14.80",  "16.55"));
    stocks.add(seed("KAHOT", "Kahoot! ASA",              "28.10",  "27.50",  "26.90",  "26.40"));
    stocks.add(seed("NEL",   "Nel ASA",                  "5.85",   "5.42",   "4.98",   "4.65"));
    return stocks;
  }

  private static Stock seed(String symbol, String company, String... priceHistory) {
    Stock stock = new Stock(symbol, company, new BigDecimal(priceHistory[0]));
    for (int i = 1; i < priceHistory.length; i++) {
      stock.addNewSalesPrice(new BigDecimal(priceHistory[i]));
    }
    return stock;
  }
}
