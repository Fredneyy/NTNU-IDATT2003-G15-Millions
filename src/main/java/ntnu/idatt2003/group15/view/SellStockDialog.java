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
import ntnu.idatt2003.group15.model.TransactionCalculator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SellStockDialog extends TransactionDialog {

  private final Label ownedLabel = new Label("Owned: 0");
  private final Label grossProceeds = new Label();
  private final Label feeAmount = new Label();
  private final Label netReceive = new Label();

  private final PortfolioController portfolioController;
  private final TransactionCalculator calculator;
  private final BigDecimal commissionRate;
  private final BigDecimal taxRate;

  public SellStockDialog(ObservableValue<BigDecimal> cashProperty,
                         PortfolioController portfolioController,
                         TransactionCalculator calculator,
                         BigDecimal commissionRate,
                         BigDecimal taxRate,
                         BiConsumer<Share, BigDecimal> onConfirm) {
    super(cashProperty, onConfirm);
    this.portfolioController = Objects.requireNonNull(portfolioController, "portfolioController cannot be null");
    this.calculator = Objects.requireNonNull(calculator, "calculator cannot be null");
    this.commissionRate = Objects.requireNonNull(commissionRate, "commissionRate cannot be null");
    this.taxRate = Objects.requireNonNull(taxRate, "taxRate cannot be null");
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
            .filter(s -> s.stock().getSymbol().equalsIgnoreCase(stock.getSymbol()))
            .map(Share::quantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add),
        shares);

    ownedLabel.textProperty().unbind();
    ownedLabel.textProperty().bind(Bindings.createStringBinding(
        () -> "Owned: " + ownedQty.get().toPlainString(), ownedQty));

    // Hypothetical share representing the intended sale; null when qty is non-positive
    // since Share requires a strictly positive quantity.
    ObjectBinding<Share> hypothetical = Bindings.createObjectBinding(() -> {
      BigDecimal q = qtyValue.get();
      BigDecimal p = price.getValue();
      if (q == null || q.signum() <= 0 || p == null || p.signum() <= 0) return null;
      return new Share(stock, q, p);
    }, qtyValue, price);

    ObjectBinding<BigDecimal> gross = Bindings.createObjectBinding(() -> {
      Share s = hypothetical.get();
      return s == null ? BigDecimal.ZERO : calculator.calculateGross(s);
    }, hypothetical);

    ObjectBinding<BigDecimal> net = Bindings.createObjectBinding(() -> {
      Share s = hypothetical.get();
      return s == null ? BigDecimal.ZERO : calculator.calculateTotal(s, commissionRate, taxRate);
    }, hypothetical);

    ObjectBinding<BigDecimal> fee = Bindings.createObjectBinding(
        () -> gross.get().subtract(net.get()), gross, net);

    grossProceeds.textProperty().unbind();
    grossProceeds.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(gross.get()), gross));

    feeAmount.textProperty().unbind();
    feeAmount.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(fee.get()), fee));

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
