package ntnu.idatt2003.group15.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.controller.ExchangeController;
import ntnu.idatt2003.group15.controller.MainMenuController;
import ntnu.idatt2003.group15.controller.NewsController;
import ntnu.idatt2003.group15.controller.PlayerController;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.model.news.NewsArchive;
import ntnu.idatt2003.group15.model.news.NewsItem;
import ntnu.idatt2003.group15.model.player.Player;
import ntnu.idatt2003.group15.model.stocks.Stock;
import ntnu.idatt2003.group15.model.stocks.StockSectors;
import ntnu.idatt2003.group15.utilities.CsvParser;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for the largest views. Each constructor wires many child views,
 * listeners, and bindings — running it covers a lot of lines without driving
 * real input.
 */
class BigViewSmokeTest {

  @BeforeAll
  static void initJavaFx() {
    JavaFxTestSupport.ensureStarted();
  }

  @Test
  void settingsViewConstructorWiresStatCards() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = new StackPane();
      SettingsView view = new SettingsView(root);
      assertNotNull(view.getView());
      assertNotNull(view.getDifficultySlider());
      // Drive the slider value — exercises listeners.
      view.getDifficultySlider().setValue(2.0);
      view.getDifficultySlider().setValue(0.5);
    });
  }

  @Test
  void settingsViewRejectsNullRoot() {
    JavaFxTestSupport.runAndWait(() -> {
      assertThrows(NullPointerException.class, () -> new SettingsView(null));
    });
  }

  @Test
  void mainMenuConstructorBuildsWithoutThrowing() {
    JavaFxTestSupport.runAndWait(() -> {
      StackPane root = new StackPane();
      Exchange exchange = new Exchange("FREX", List.of(
          new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
              List.of(StockSectors.TECHNOLOGY))));
      MainMenuController controller = new MainMenuController(exchange, (ex, pl) -> { /* no-op */ });

      MainMenu menu = new MainMenu(
          root,
          throwable -> { /* error handler */ },
          new CsvParser(),
          new TaskUtil(),
          controller);

      assertNotNull(menu.getView());
      // refreshContinueCard reads SaveIndex — should be safe since the test
      // process has its own user.home set; redirect to avoid touching real saves.
      String originalHome = System.getProperty("user.home");
      try {
        System.setProperty("user.home", System.getProperty("java.io.tmpdir"));
        menu.refreshContinueCard();
      } finally {
        if (originalHome != null) {
          System.setProperty("user.home", originalHome);
        } else {
          System.clearProperty("user.home");
        }
      }
    });
  }

  @Test
  void gameViewConstructorWiresEverything() {
    JavaFxTestSupport.runAndWait(() -> {
      Stock apple = new Stock("AAPL", "Apple", BigDecimal.valueOf(100), 0.0, 0.0,
          List.of(StockSectors.TECHNOLOGY));
      Exchange exchange = new Exchange("FREX", List.of(apple));
      Player player = new Player("Trader", BigDecimal.valueOf(10000));
      NewsArchive archive = new NewsArchive(List.of(new NewsItem(
          "headline", StockSectors.TECHNOLOGY,
          BigDecimal.ONE, BigDecimal.ZERO, 3, Instant.now(), false)));

      GameView gameView = new GameView(
          new PlayerController(player),
          new ExchangeController(exchange),
          new GameSettings(),
          () -> { /* exit */ },
          throwable -> { /* errorHandler */ },
          new NewsController(archive),
          () -> { /* advance */ },
          () -> { /* autoAdvanceOn */ },
          () -> { /* autoAdvanceOff */ });

      assertNotNull(gameView.getNewsFeedView());
      assertNotNull(gameView.getSettingsController());

      // Push a news item through to exercise pushItem + onNewsEmitted.
      NewsItem item = new NewsItem(
          "Breaking", StockSectors.TECHNOLOGY,
          BigDecimal.ONE, BigDecimal.valueOf(0.05), 3, Instant.now(), false);
      gameView.onNewsEmitted(item);
    });
  }
}
