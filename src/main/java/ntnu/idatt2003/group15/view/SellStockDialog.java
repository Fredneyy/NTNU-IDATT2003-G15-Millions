package ntnu.idatt2003.group15.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SellStockDialog extends TransactionDialog {

  private final Label ownedLabel = new Label("Owned: 0");
  private final Label grossProceeds = new Label();
  private final Label feeAmount = new Label();
  private final Label netReceive = new Label();

  private final PortfolioController portfolioController;
  private final BigDecimal feeRate;

  public SellStockDialog(ObservableValue<BigDecimal> cashProperty,
                         PortfolioController portfolioController,
                         BigDecimal feeRate,
                         BiConsumer<Stock, BigDecimal> onConfirm) {
    super(cashProperty, onConfirm);
    this.portfolioController = Objects.requireNonNull(portfolioController, "portfolioController cannot be null");
    this.feeRate = Objects.requireNonNull(feeRate, "feeRate cannot be null");
    assembleBody();
  }

  @Override
  protected VBox buildMenu() {
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

    maxButton.getStyleClass().add("buy-max-button");

    HBox inputHBox = new HBox(quantity, maxButton);
    inputHBox.getStyleClass().add("buy-quantity-row");

    ownedLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, inputHBox, ownedLabel);
    quantityContainer.getStyleClass().add("buy-quantity-container");

    HBox summaryHbox = buildSummaryBox();

    transactionButton.getStyleClass().add("buy-confirm-button");
    transactionButton.setMaxWidth(Double.MAX_VALUE);

    VBox container = new VBox(priceContainer, quantityContainer, summaryHbox, transactionButton);
    container.getStyleClass().add("buy-content");
    return container;
  }

  private HBox buildSummaryBox() {
    Label grossTextLabel = new Label("Gross Proceeds");
    Label feeTextLabel = new Label("Fee");
    Label netTextLabel = new Label("You Receive");
    grossTextLabel.getStyleClass().add("buy-cost-label");
    feeTextLabel.getStyleClass().add("buy-cost-label");
    netTextLabel.getStyleClass().add("buy-cost-label-major");
    VBox labels = new VBox(grossTextLabel, feeTextLabel, netTextLabel);
    labels.getStyleClass().add("buy-cost-labels");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    grossProceeds.getStyleClass().add("buy-cost-value");
    feeAmount.getStyleClass().add("buy-cost-value");
    netReceive.getStyleClass().addAll("buy-cost-value-major", "value-positive");
    VBox values = new VBox(grossProceeds, feeAmount, netReceive);
    values.getStyleClass().add("buy-cost-values");
    values.setAlignment(Pos.CENTER_RIGHT);

    HBox row = new HBox(labels, spacer, values);
    row.getStyleClass().add("buy-cost-card");
    return row;
  }

  @Override
  protected void bindToStock(Stock stock) {
    stockSymbol.setText("Sell " + stock.getSymbol());
    stockName.setText(stock.getCompany());
    transactionButton.setText("$ Sell");
    quantity.setText("");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = stock.getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    ObservableList<Share> shares = portfolioController.getListProperty();
    ObjectBinding<BigDecimal> ownedQty = Bindings.createObjectBinding(() ->
        shares.stream()
            .filter(s -> s.getStock().getSymbol().equalsIgnoreCase(stock.getSymbol()))
            .map(Share::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add),
        shares);

    ownedLabel.textProperty().unbind();
    ownedLabel.textProperty().bind(Bindings.createStringBinding(
        () -> "Owned: " + ownedQty.get().toPlainString(), ownedQty));

    ObjectBinding<BigDecimal> gross = Bindings.createObjectBinding(() -> {
      BigDecimal p = price.getValue();
      BigDecimal q = qtyValue.get();
      if (p == null || q == null) return BigDecimal.ZERO;
      return p.multiply(q);
    }, price, qtyValue);

    grossProceeds.textProperty().unbind();
    grossProceeds.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(gross.get()), gross));

    ObjectBinding<BigDecimal> fee = Bindings.createObjectBinding(() ->
        gross.get().multiply(feeRate), gross);

    feeAmount.textProperty().unbind();
    feeAmount.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(fee.get()), fee));

    ObjectBinding<BigDecimal> net = Bindings.createObjectBinding(() ->
        gross.get().subtract(fee.get()), gross, fee);

    netReceive.textProperty().unbind();
    netReceive.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(net.get()), net));

    BooleanBinding cannotSell = Bindings.createBooleanBinding(() -> {
      BigDecimal q = qtyValue.get();
      BigDecimal owned = ownedQty.get();
      return q == null || q.signum() <= 0 || owned == null || q.compareTo(owned) > 0;
    }, qtyValue, ownedQty);

    transactionButton.disableProperty().unbind();
    transactionButton.disableProperty().bind(cannotSell);

    transactionButton.setOnAction(_ -> {
      BigDecimal q = qtyValue.get();
      if (q == null || q.signum() <= 0) return;
      onConfirm.accept(stock, q);
      close();
    });

    maxButton.setOnAction(_ -> {
      BigDecimal owned = ownedQty.get();
      quantity.setText(owned == null ? "0" : owned.toPlainString());
    });
  }
}
