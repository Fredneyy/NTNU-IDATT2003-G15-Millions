package ntnu.idatt2003.group15;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ntnu.idatt2003.group15.controller.MainController;
import ntnu.idatt2003.group15.utilities.CsvParser;
import ntnu.idatt2003.group15.utilities.newsparser.NewsLoader;
import ntnu.idatt2003.group15.utilities.stockparser.StockLoader;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.dialog.ExceptionDialog;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    private TaskUtil taskUtil;
    private Consumer<Throwable> errorHandler;
    private CsvParser csvParser;
    private ExceptionDialog exceptionDialog;
    private StackPane root;

    @Override
    public void start(Stage stage) {
        setUpDependencies();

        root =  new StackPane();
        Scene scene = new Scene(root);
        MainController mainController = new MainController(
            root, errorHandler, csvParser, taskUtil,
            new StockLoader("src/main/resources/storage/defaultstocks.csv").load(),
            new NewsLoader("src/main/resources/storage/stock_news.csv").load());
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
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/BuyStockDialogStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/OnBoardStyle.css")).toExternalForm());
        root.getStyleClass().add("scene-root");
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
        exceptionDialog = new ExceptionDialog();
        errorHandler = this::exceptionPopUp;
        taskUtil = new TaskUtil();
        try {
            taskUtil.init(Runtime.getRuntime().availableProcessors());
        } catch (IllegalArgumentException e) {
            exceptionPopUp(e);
        }
        csvParser = new CsvParser();
    }



    private void exceptionPopUp(Throwable e) {
        exceptionDialog.setText(e.getClass().getSimpleName(), e.getMessage());
        exceptionDialog.show(root);
    }

}
