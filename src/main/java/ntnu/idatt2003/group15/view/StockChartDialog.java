package ntnu.idatt2003.group15.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.util.Duration;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class StockChartDialog extends BaseDialog {

  private StackPane root;
  private final ParallelTransition openAnimation;
  private final ParallelTransition closeAnimation;

  private final Label symbolLabel = new Label();
  private final Label companyLabel = new Label();
  private final Label currentPriceLabel = new Label();
  private final Label lastRelativeChange = new Label();
  private final Label lastAbsoluteChangeLabel = new Label();
  private final Label dataPointsLabel = new Label();
  private final VBox chartContainer  = new VBox();
  private final VBox chartSection = new VBox();

  private final NumberAxis xAxis = new NumberAxis();
  private final NumberAxis yAxis = new NumberAxis();
  private final AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
  private final XYChart.Series<Number, Number> series = new XYChart.Series<>();

  private ObservableList<BigDecimal> historicalPrices;
  private Stock currentStock;
  private ListChangeListener<BigDecimal> pricesListener;

  public StockChartDialog() {
    super();

    double maxW = Screen.getPrimary().getVisualBounds().getWidth()  * 0.35;
    double maxH = Screen.getPrimary().getVisualBounds().getHeight() * 0.55;
    dialog.setMaxWidth(maxW);
    dialog.setMaxHeight(maxH);
    dialog.getStyleClass().setAll("stock-dialog-card");

    configureChart();

    VBox body = new VBox(16,
        buildHeader(),
        buildStatCards(),
        buildChartSection()
    );
    body.setPadding(new Insets(24, 28, 28, 28));
    body.setFillWidth(true);

    dialog.getChildren().add(body);
    dialog.applyCss();
    dialog.layout();

    openAnimation  = buildOpenAnimation();
    closeAnimation = createCloseAnimation(  _ -> {
        root.getChildren().remove(dialog);
        dialog.setOpacity(1);
        dialog.setScaleX(1);
        dialog.setScaleY(1);
      });
  }

  public void show(StackPane root, Stock stockData) {
    this.root = root;
    populate(stockData);
    isNotInRoot(root);
  }

  private void isNotInRoot(StackPane root) {
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
      dialog.setOpacity(0);
      dialog.setScaleX(0.92);
      dialog.setScaleY(0.92);
      openAnimation.play();
    }
  }

  public void close() {
    detachPricesListener();
    if (root != null && root.getChildren().contains(dialog)) {
      closeAnimation.play();
    }
  }

  private void populate(Stock stockData) {
    detachPricesListener();
    currentStock = stockData;
    historicalPrices = stockData.getHistoricalPrices();

    symbolLabel.setText(stockData.getSymbol());
    companyLabel.setText(stockData.getCompany());

    refreshLiveLabels();
    updateChart(stockData);

    pricesListener = _ -> {
      refreshLiveLabels();
      updateChart(currentStock);
    };
    historicalPrices.addListener(pricesListener);
  }

  private void refreshLiveLabels() {
    if (currentStock == null) return;
    currentPriceLabel.setText(String.format("$%.2f", currentStock.getSalesPrice()));

    BigDecimal absChange = currentStock.getLatestPriceChange();
    BigDecimal relChange = currentStock.getLatestPriceChangeRelative();

    String sign = absChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
    lastRelativeChange.setText(String.format("%s%.2f%%", sign, relChange));
    lastRelativeChange.getStyleClass().setAll("stock-stat-value",
        absChange.compareTo(BigDecimal.ZERO) >= 0 ? "value-positive" : "value-negative");

    String ssign = relChange.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
    lastAbsoluteChangeLabel.setText(String.format("%s%.2f", ssign, absChange));
    lastAbsoluteChangeLabel.getStyleClass().setAll("stock-stat-value",
        relChange.compareTo(BigDecimal.ZERO) >= 0 ? "value-positive" : "value-negative");

    dataPointsLabel.setText(String.valueOf(historicalPrices.size()));
  }

  private void detachPricesListener() {
    if (historicalPrices != null && pricesListener != null) {
      historicalPrices.removeListener(pricesListener);
    }
    pricesListener = null;
  }

  private HBox buildHeader() {
    StackPane avatar = new StackPane();
    avatar.getStyleClass().add("stock-avatar");
    avatar.setMinSize(56, 56);
    avatar.setMaxSize(56, 56);
    symbolLabel.getStyleClass().add("stock-avatar-text");
    avatar.getChildren().add(symbolLabel);

    companyLabel.getStyleClass().add("stock-company");

    VBox info = new VBox(2, companyLabel);
    info.setAlignment(Pos.CENTER_LEFT);

    Button closeBtn = new Button();
    FontIcon x = new FontIcon(MaterialDesignC.CLOSE);
    x.getStyleClass().add("close-icon");
    closeBtn.setGraphic(x);
    closeBtn.getStyleClass().add("stock-close-btn");
    closeBtn.setOnAction(_ -> close());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(16, avatar, info, spacer, closeBtn);
    header.setAlignment(Pos.CENTER_LEFT);

    Region sep = new Region();
    sep.getStyleClass().add("stock-separator");
    sep.setMinHeight(1);
    sep.setMaxHeight(1);
    sep.setMaxWidth(Double.MAX_VALUE);

    VBox headerSection = new VBox(16, header, sep);
    headerSection.setFillWidth(true);
    HBox.setHgrow(headerSection, Priority.ALWAYS);
    HBox wrapper = new HBox(headerSection);
    HBox.setHgrow(headerSection, Priority.ALWAYS);
    return wrapper;
  }

  private HBox buildStatCards() {
    currentPriceLabel.getStyleClass().addAll("stock-stat-value", "stock-price");
    lastRelativeChange.getStyleClass().addAll("stock-stat-value", "value-negative");
    lastAbsoluteChangeLabel.getStyleClass().addAll("stock-stat-value", "value-positive");
    dataPointsLabel.getStyleClass().addAll("stock-stat-value");

    HBox row = new HBox(12,
        statCard("Current Price",   currentPriceLabel),
        statCard("Last Relative Change", lastRelativeChange),
        statCard("Last Absolute Change", lastAbsoluteChangeLabel),
        statCard("Data Points",     dataPointsLabel)
    );
    row.setFillHeight(true);
    for (var child : row.getChildren()) {
      HBox.setHgrow(child, Priority.ALWAYS);
      if (child instanceof Region r) {
        r.setMaxWidth(Double.MAX_VALUE);
      }
    }
    return row;
  }

  private VBox statCard(String title, Label valueLabel) {
    Label titleLbl = new Label(title);
    titleLbl.getStyleClass().add("stock-stat-title");
    VBox card = new VBox(6, titleLbl, valueLabel);
    card.getStyleClass().add("stock-stat-card");
    card.setPadding(new Insets(16));
    card.setFillWidth(true);
    card.setMaxWidth(Double.MAX_VALUE);
    return card;
  }

  private VBox buildChartSection() {
    FontIcon chartIcon = new FontIcon(MaterialDesignC.CHART_BAR);
    chartIcon.getStyleClass().add("section-icon");

    Label title = new Label("Price History");
    title.getStyleClass().add("section-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(8, chartIcon, title);
    header.setAlignment(Pos.CENTER_LEFT);

    chartContainer.setSpacing(0);
    chartContainer.setFillWidth(true);
    chartContainer.setMaxWidth(Double.MAX_VALUE);
    chartContainer.setMaxHeight(Double.MAX_VALUE);
    chartContainer.getChildren().setAll(chart);

    VBox.setVgrow(chartContainer, Priority.ALWAYS);

    chartSection.getChildren().setAll(header, chartContainer);
    chartSection.setSpacing(12);
    chartSection.getStyleClass().add("stock-chart-section");
    chartSection.setPadding(new Insets(20));
    chartSection.setFillWidth(true);
    chartSection.setMaxWidth(Double.MAX_VALUE);
    chartSection.setMaxHeight(Double.MAX_VALUE);
    VBox.setVgrow(chartSection, Priority.ALWAYS);
    return chartSection;
  }

  private void configureChart() {
    xAxis.setTickLabelsVisible(false);
    xAxis.setTickMarkVisible(false);
    xAxis.setMinorTickVisible(false);
    xAxis.getStyleClass().add("chart-axis");

    yAxis.setAutoRanging(false);
    yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));
    yAxis.getStyleClass().add("chart-axis");
    yAxis.setMinorTickVisible(false);

    chart.setLegendVisible(false);
    chart.setAnimated(false);
    chart.getStyleClass().add("stock-area-chart");
    chart.setCreateSymbols(false);
    chart.setMaxWidth(Double.MAX_VALUE);
    chart.setMaxHeight(Double.MAX_VALUE);
    chart.getData().add(series);
  }

  private void updateChart(Stock stockData) {
    if (historicalPrices == null || historicalPrices.isEmpty()) {
      series.getData().clear();
      return;
    }

    BigDecimal min = stockData.getLowestPrice();
    BigDecimal max = stockData.getHighestPrice();
    BigDecimal range = max.subtract(min);
    boolean flat = range.signum() == 0;
    BigDecimal padding = flat ? BigDecimal.ONE : range.multiply(new BigDecimal("0.05"));
    BigDecimal tick = flat ? BigDecimal.ONE : range.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

    yAxis.setLowerBound(min.subtract(padding).doubleValue());
    yAxis.setUpperBound(max.add(padding).doubleValue());
    yAxis.setTickUnit(tick.doubleValue());

    List<XYChart.Data<Number, Number>> data = new ArrayList<>(historicalPrices.size());
    for (int i = 0; i < historicalPrices.size(); i++) {
      data.add(new XYChart.Data<>(i, historicalPrices.get(i).doubleValue()));
    }
    series.getData().setAll(data);
  }

  private ParallelTransition buildOpenAnimation() {
    FadeTransition fade = createFadeTransition(dialog,  Duration.millis(300), 0, 1);
    ScaleTransition scale = createScaleTransition(dialog,  Duration.millis(300), 0.92, 1.0);
    return new ParallelTransition(fade, scale);
  }
}