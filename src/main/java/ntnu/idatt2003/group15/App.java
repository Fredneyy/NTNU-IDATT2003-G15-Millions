package ntnu.idatt2003.group15;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.MainController;
import ntnu.idatt2003.group15.controller.MainMenuController;
import ntnu.idatt2003.group15.model.StandardPriceEvent;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.StockSimulator;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.ExceptionDialog;
import ntnu.idatt2003.group15.view.OnBoardingDialog;
import ntnu.idatt2003.group15.view.StockChartDialog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    private TaskUtil taskUtil;
    private Consumer<Throwable> errorHandler;
    private CsvUtil csvUtil;
    private ExceptionDialog exceptionDialog;
    private StackPane root;

    @Override
    public void start(Stage stage) {
        setUpDependencies();

        root =  new StackPane();
        Scene scene = new Scene(root);
        MainController mainController = new MainController(root, errorHandler, csvUtil, taskUtil);
        mainController.showMainMenu();

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/RootStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/SettingsStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/TabView.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/MainMenuStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/MarketTableStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/StatsViewStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/TradesViewStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/NewsFeedStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/DialogStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/StatisticsOverview.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/StockChartDialog.css")).toExternalForm());
        root.getStyleClass().add("scene-root");
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (taskUtil != null) {
            taskUtil.shutdown();
        }
    }

    /**
     * This method is called by the Launcher class.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void setUpDependencies() {
        // infrastructure
        taskUtil = new TaskUtil();
        try {
            taskUtil.init(Runtime.getRuntime().availableProcessors());
        } catch (IllegalArgumentException e) {
            exceptionPopUp(e);
        }
        csvUtil = new CsvUtil();
        errorHandler = this::exceptionPopUp;
        exceptionDialog = new ExceptionDialog(Duration.millis(200));
    }



    private void exceptionPopUp(Throwable e) {
        exceptionDialog.setText(e.getClass().getSimpleName(), e.getMessage());
        exceptionDialog.show(root);
    }

}
