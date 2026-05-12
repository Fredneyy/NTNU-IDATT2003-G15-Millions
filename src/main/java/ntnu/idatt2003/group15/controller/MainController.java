package ntnu.idatt2003.group15.controller;

import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.GameView;
import ntnu.idatt2003.group15.view.MainMenu;

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


  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    mainMenuController = new MainMenuController(new Exchange("OSEBX", new ArrayList<>()), this::startGame);
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
    GameView gameView = new GameView(playerController.getName());
    root.getChildren().setAll(gameView.getView());
  }

}
