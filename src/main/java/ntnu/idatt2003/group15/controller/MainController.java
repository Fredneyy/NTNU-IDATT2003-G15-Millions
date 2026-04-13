package ntnu.idatt2003.group15.controller;

import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.MainMenu;

import java.util.function.Consumer;

public class MainController {

  private final MainMenu mainMenu;
  private final CsvUtil csvUtil;
  private final TaskUtil taskUtil;
  private final StackPane root;


  public MainController(StackPane root, Consumer<Throwable> errorHandler, CsvUtil csvUtil, TaskUtil taskUtil) {
    mainMenu = new MainMenu(root, errorHandler, csvUtil, taskUtil);
    this.csvUtil = csvUtil;
    this.taskUtil = taskUtil;
    this.root = root;
  }

  public void showMainMenu() {
    root.getChildren().setAll(mainMenu.getView());
  }

}
