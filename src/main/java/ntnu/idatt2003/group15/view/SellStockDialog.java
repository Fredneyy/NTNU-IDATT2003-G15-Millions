package ntnu.idatt2003.group15.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.Share;
import ntnu.idatt2003.group15.model.Stock;
import ntnu.idatt2003.group15.model.TransactionCalculator;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SellStockDialog extends TransactionDialog {

  private final Label ownedLabel = new Label("Owned: 0");
  private final Label grossProceeds = new Label();
  private final Label feeAmount = new Label();
  private final Label netReceive = new Label();

  private final PortfolioController portfolioController;
  private final TransactionCalculator calculator;
  private final BigDecimal commissionRate;
  private final BigDecimal taxRate;
  private final Consumer<Share> onConfirm;
  private Share share;

  public SellStockDialog(ObservableValue<BigDecimal> cashProperty,
                         PortfolioController portfolioController,
                         TransactionCalculator calculator,
                         BigDecimal commissionRate,
                         BigDecimal taxRate,
                         Consumer<Share> onConfirm) {
    super(cashProperty);
    this.onConfirm = Objects.requireNonNull(onConfirm);
    this.portfolioController = Objects.requireNonNull(portfolioController, "portfolioController cannot be null");
    this.calculator = Objects.requireNonNull(calculator, "calculator cannot be null");
    this.commissionRate = Objects.requireNonNull(commissionRate, "commissionRate cannot be null");
    this.taxRate = Objects.requireNonNull(taxRate, "taxRate cannot be null");
    assembleBody();
  }

  public void show(StackPane root, Share share) {
    this.root = Objects.requireNonNull(root);
    this.share = Objects.requireNonNull(share);
    bindToStock(share);
    if (!root.getChildren().contains(dialog)) {
      dialog.setOpacity(0);
      dialog.setScaleX(0.1);
      dialog.setScaleY(0.1);
      root.getChildren().add(dialog);
      openAnimation.play();
    }
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

    ownedLabel.getStyleClass().add("buy-max-hint");

    VBox quantityContainer = new VBox(quantityLabel, ownedLabel);
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

  protected void bindToStock(Share share) {
    stockSymbol.setText("Sell " + share.stock().getSymbol());
    stockName.setText(share.stock().getCompany());
    transactionButton.setText("$ Sell");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = share.stock().getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    ownedLabel.setText("%b".formatted(share.quantity()));

    ObjectBinding<BigDecimal> gross = Bindings.createObjectBinding(() -> {
      return calculator.calculateGross(share);
    }, share.stock().getPriceBinding());

    ObjectBinding<BigDecimal> net = Bindings.createObjectBinding(() -> {
      return calculator.calculateTotal(share, commissionRate, taxRate);
    }, share.stock().getPriceBinding());

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

    transactionButton.setOnAction(_ -> {
      onConfirm.accept(share);
      close();
    });
  }
}
