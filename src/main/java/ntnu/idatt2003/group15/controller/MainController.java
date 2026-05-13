package ntnu.idatt2003.group15.controller;

import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.GameView;
import ntnu.idatt2003.group15.view.MainMenu;
import ntnu.idatt2003.group15.view.NewsDialog;
import ntnu.idatt2003.group15.view.NewsFeedView;

import java.math.BigDecimal;
import java.util.ArrayList;
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


  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    mainMenuController = new MainMenuController(new Exchange("OSEBX", new ArrayList<>()), this::startGame);
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil, mainMenuController);
    newsController = new NewsController(root);
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;
  }

  public void showMainMenu() {
    newsController.stop();
    root.getChildren().setAll(mainMenu.getView());
  }

  private void startGame(ExchangeController exchangeController, PlayerController playerController) {
    this.playerController = playerController;
    this.exchangeController = exchangeController;
    GameView gameView = new GameView(playerController.getName(), this::showMainMenu);
    root.getChildren().setAll(gameView.getView());
    // Start the news loop after the game view is in place — root.setAll() above
    // would otherwise wipe the NewsContainer node we mount.
    newsController.start();
    // Mirror every emitted news event into the "Market News Feed" tab.
    newsController.setOnNewsEmitted(item -> {
      NewsFeedView.NewsRecord record = toFeedRecord(item);
      if (record != null) gameView.getNewsFeedView().prependEvent(record);
    });
    newsController.push("Welcome to Millions!",
        "Your stock market simulation experience starts here.");
  }

  /** Convert a controller-emitted {@link NewsController.NewsItem} into a feed row.
   *  Returns {@code null} for plain INFO items (like the welcome dialog) which
   *  shouldn't show up as a market event. */
  private static NewsFeedView.NewsRecord toFeedRecord(NewsController.NewsItem item) {
    NewsFeedView.Sentiment sentiment = switch (item.sentiment()) {
      case BULLISH -> NewsFeedView.Sentiment.BULLISH;
      case BEARISH -> NewsFeedView.Sentiment.BEARISH;
      case NEUTRAL -> null;
    };
    if (sentiment == null) return null;
    return new NewsFeedView.NewsRecord(
        sentiment,
        item.symbol() == null ? "" : item.symbol(),
        item.changePercent() == null ? BigDecimal.ZERO : item.changePercent(),
        item.title(),
        item.message(),
        item.volatility() == null ? BigDecimal.ZERO : item.volatility(),
        item.durationUpdates(),
        item.type() == null ? "" : item.type(),
        item.when());
  }

}
