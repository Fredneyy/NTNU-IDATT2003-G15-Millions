package ntnu.idatt2003.group15.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import ntnu.idatt2003.group15.model.Stock;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

import java.util.Objects;

public class BuyStockDialog extends BaseDialog {

  private final Label stockSymbol = new Label();
  private final Label stockName = new Label();
  private final Label stockPrice = new Label();
  private final HBox header = new HBox();
  private final Button closeButton = new Button();
  private final Label currentPrice = new Label();
  private final Label maxBuyStockAmountLabel = new Label("Max: ");
  private final Label totalCost = new Label();
  private final Label availableCash = new Label();
  private final Label remainingCash = new Label();
  private final TextField quantity = new TextField();
  private final Button buyButton = new Button();
  private StackPane root;

  public BuyStockDialog() {
    super();

    dialog.setMaxHeight((int) Screen.getPrimary().getVisualBounds().getHeight() / 2.0);
    dialog.setMaxWidth((int) Screen.getPrimary().getVisualBounds().getWidth() / 5.0);
    dialog.getStyleClass().setAll("stock-dialog-card", "buy-stock-dialog");

    VBox body = new VBox(buildHeader(), buildBuyMenu());
    body.getStyleClass().add("buy-dialog-body");
    dialog.getChildren().add(body);
  }

  public void show(StackPane root, Stock stock) {
    this.root = Objects.requireNonNull(root);
    Stock stock1 = Objects.requireNonNull(stock);
    populateDetails(stock);
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  @Override
  public void show(StackPane root) {
    this.root = Objects.requireNonNull(root);
    if (!root.getChildren().contains(dialog)) {
      root.getChildren().add(dialog);
    }
  }

  private void populateDetails(Stock stock) {
    stockSymbol.setText("Buy " + stock.getSymbol());
    stockName.setText(stock.getCompany());
    currentPrice.setText(String.format("$%.2f", stock.getSalesPrice()));
    buyButton.setText("$ Buy");
  }

  @Override
  public void close() {

  }

  private VBox buildBuyMenu() {
    Label labelInfo = new Label("Current Price");
    labelInfo.getStyleClass().add("buy-stat-title");
    currentPrice.getStyleClass().add("buy-stat-price");
    VBox priceContainer = new VBox(labelInfo, currentPrice);
    priceContainer.getStyleClass().add("buy-card");

    Label quantityLabel = new Label("Quantity");
    quantityLabel.getStyleClass().add("buy-section-label");
    quantity.setPromptText("Input amount...");
    quantity.getStyleClass().add("buy-quantity-input");
    HBox.setHgrow(quantity, Priority.ALWAYS);

    Button maxBuyButton = new Button("Max");
    maxBuyButton.getStyleClass().add("buy-max-button");

    HBox inputHBox = new HBox(quantity, maxBuyButton);
    inputHBox.getStyleClass().add("buy-quantity-row");

    maxBuyStockAmountLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, inputHBox, maxBuyStockAmountLabel);
    quantityContainer.getStyleClass().add("buy-quantity-container");

    HBox costHbox = buildCostBox();

    buyButton.getStyleClass().add("buy-confirm-button");
    buyButton.setMaxWidth(Double.MAX_VALUE);

    VBox container = new VBox(priceContainer, quantityContainer, costHbox, buyButton);
    container.getStyleClass().add("buy-content");
    return container;
  }

  private HBox buildCostBox() {
    Label costTextLabel = new Label("Total Cost");
    Label availableTextLabel = new Label("Available Cash");
    Label remainingTextLabel = new Label("Remaining");
    costTextLabel.getStyleClass().add("buy-cost-label-major");
    availableTextLabel.getStyleClass().add("buy-cost-label");
    remainingTextLabel.getStyleClass().add("buy-cost-label");
    VBox costContainerText = new VBox(costTextLabel, availableTextLabel, remainingTextLabel);
    costContainerText.getStyleClass().add("buy-cost-labels");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    totalCost.getStyleClass().addAll("buy-cost-value-major");
    availableCash.getStyleClass().add("buy-cost-value");
    remainingCash.getStyleClass().addAll("buy-cost-value", "value-positive");
    totalCost.setText("$1,075.16");
    availableCash.setText("$14,524.88");
    remainingCash.setText("$13,449.72");
    VBox costContainer = new VBox(totalCost, availableCash, remainingCash);
    costContainer.getStyleClass().add("buy-cost-values");
    costContainer.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(costContainerText, spacer, costContainer);
    row.getStyleClass().add("buy-cost-card");
    return row;
  }

  private HBox buildHeader() {
    stockSymbol.getStyleClass().add("buy-header-title");
    stockName.getStyleClass().add("buy-header-subtitle");

    VBox headerVBox = new VBox(stockSymbol, stockName);
    headerVBox.getStyleClass().add("buy-header-text");
    HBox.setHgrow(headerVBox, Priority.ALWAYS);

    closeButton.getStyleClass().setAll("close-button", "buy-close-button");
    closeButton.setOnAction(_ -> close());
    closeButton.setMaxHeight(Region.USE_PREF_SIZE);
    closeButton.setGraphic(FontIcon.of(MaterialDesignC.CLOSE));

    header.getChildren().setAll(headerVBox, closeButton);
    header.getStyleClass().add("buy-header");
    return header;
  }
}