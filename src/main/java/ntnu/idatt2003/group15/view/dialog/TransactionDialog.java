package ntnu.idatt2003.group15.view.dialog;

import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public abstract class TransactionDialog extends BaseDialog {

  protected final Label stockSymbol = new Label();
  protected final Label stockName = new Label();
  protected final HBox header = new HBox();
  protected final Button closeButton = new Button();
  protected final Label currentPrice = new Label();
  protected final TextField quantityField = new TextField();
  protected final Button maxButton = new Button("Max");
  protected final Button transactionButton = new Button();
  protected final Label amountLabel = new Label();
  protected final Label summaryLabel1 = new Label();
  protected final Label summaryLabel2 = new Label();
  protected final Label summaryLabel3 = new Label();
  protected final Label summaryTextLabel1 = new Label();
  protected final Label summaryTextLabel2 = new Label();
  protected final Label summaryTextLabel3 = new Label();
  protected final Label labelInfo = new Label("Current Price");


  protected final ObservableValue<BigDecimal> cashProperty;
  protected final ObjectProperty<BigDecimal> qtyValue = new SimpleObjectProperty<>(BigDecimal.ZERO);

  protected ParallelTransition closeAnimation;
  protected ParallelTransition openAnimation;

  protected TransactionDialog(ObservableValue<BigDecimal> cashProperty) {
    super();
    this.cashProperty = Objects.requireNonNull(cashProperty, "cashProperty cannot be null");

    // Match StockChartDialog's width (35% of the screen) so the Max button
    // and the summary values have room to render fully — the old /5.0 (20%)
    // truncated the Max button label to "..." on most resolutions. Floor at
    // 420px so the dialog stays usable on small screens too.
    double screenW = Screen.getPrimary().getVisualBounds().getWidth();
    double screenH = Screen.getPrimary().getVisualBounds().getHeight();
    dialog.setMaxWidth(Math.max(420, screenW * 0.35));
    dialog.setMaxHeight(screenH / 2.5);
    dialog.getStyleClass().setAll("stock-dialog-card", "buy-stock-dialog");

    quantityField.textProperty().addListener((_, _, nv) -> qtyValue.set(parseQuantity(nv)));

    closeAnimation = createCloseAnimation(_ -> root.getChildren().remove(dialog));
    openAnimation = createOpenAnimation();
  }

  /** Subclasses must call this from their constructor after their own fields are initialized. */
  protected final void assembleBody() {
    VBox body = new VBox(buildHeader(), buildMenu());
    body.getStyleClass().add("buy-dialog-body");
    dialog.getChildren().add(body);
  }

  public void show(StackPane root) {
    this.root = Objects.requireNonNull(root);
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  public void close() {
    if (root != null) {
      closeAnimation.play();
    }
  }

  protected static BigDecimal parseQuantity(String text) {
    if (text == null || text.isBlank()) return BigDecimal.ZERO;
    try {
      BigDecimal v = new BigDecimal(text.trim());
      return v.signum() < 0 ? BigDecimal.ZERO : v;
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  protected static String formatMoney(BigDecimal v) {
    if (v == null) return "$0.00";
    String sign = v.signum() < 0 ? "-" : "";
    return sign + "$" + v.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private HBox buildHeader() {
    stockSymbol.getStyleClass().add("buy-header-title");
    stockName.getStyleClass().add("buy-header-subtitle");

    VBox headerVBox = new VBox(stockSymbol, stockName);
    headerVBox.getStyleClass().add("buy-header-text");
    HBox.setHgrow(headerVBox, Priority.ALWAYS);

    closeButton.getStyleClass().setAll("close-button", "buy-close-button");
    closeButton.setOnAction(_ -> close());
    FontIcon closeIcon = FontIcon.of(MaterialDesignC.CLOSE);
    closeIcon.setIconColor(Color.WHITE);
    closeButton.setGraphic(closeIcon);
    closeButton.setAlignment(Pos.CENTER);

    header.getChildren().setAll(headerVBox, closeButton);
    header.getStyleClass().add("buy-header");
    return header;
  }

  protected VBox buildMenu() {
    labelInfo.getStyleClass().add("buy-stat-title");
    currentPrice.getStyleClass().add("buy-stat-price");
    VBox priceContainer = new VBox(labelInfo, currentPrice);
    priceContainer.getStyleClass().add("buy-card");

    Label quantityLabel = new Label("Quantity");
    quantityLabel.getStyleClass().add("buy-section-label");
    quantityField.setPromptText("Input amount...");
    quantityField.getStyleClass().add("buy-quantity-input");
    HBox.setHgrow(quantityField, Priority.ALWAYS);

    maxButton.getStyleClass().add("buy-max-button");

    HBox inputHBox = new HBox(quantityField, maxButton);
    inputHBox.getStyleClass().add("buy-quantity-row");

    amountLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, inputHBox, amountLabel);
    quantityContainer.getStyleClass().add("buy-quantity-container");

    HBox costHbox = buildCostBox();

    transactionButton.getStyleClass().add("buy-confirm-button");
    transactionButton.setMaxWidth(Double.MAX_VALUE);

    VBox container = new VBox(priceContainer, quantityContainer, costHbox, transactionButton);
    container.getStyleClass().add("buy-content");
    return container;
  }


  private HBox buildCostBox() {
    summaryTextLabel1.getStyleClass().add("buy-cost-label-major");
    summaryTextLabel2.getStyleClass().add("buy-cost-label");
    summaryTextLabel3.getStyleClass().add("buy-cost-label");
    VBox costContainerText = new VBox(summaryTextLabel1, summaryTextLabel2, summaryTextLabel3);
    costContainerText.getStyleClass().add("buy-cost-labels");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    summaryLabel1.getStyleClass().addAll("buy-cost-value-major");
    summaryLabel2.getStyleClass().add("buy-cost-value");
    summaryLabel3.getStyleClass().add("buy-cost-value");
    VBox costContainer = new VBox(summaryLabel1, summaryLabel2, summaryLabel3);
    costContainer.getStyleClass().add("buy-cost-values");
    costContainer.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(costContainerText, spacer, costContainer);
    row.getStyleClass().add("buy-cost-card");
    return row;
  }

}
