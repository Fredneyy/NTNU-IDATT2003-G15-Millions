package ntnu.idatt2003.group15.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.SaleCalculator;
import ntnu.idatt2003.group15.model.Share;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SellStockDialog extends TransactionDialog {

  private final SaleCalculator calculator;
  private final BigDecimal commissionRate;
  private final BigDecimal taxRate;
  private final BiConsumer<Share, BigDecimal> onConfirm;

  public SellStockDialog(ObservableValue<BigDecimal> cashProperty,
                         PortfolioController portfolioController,
                         SaleCalculator calculator,
                         BigDecimal commissionRate,
                         BigDecimal taxRate,
                         BiConsumer<Share, BigDecimal> onConfirm) {
    super(cashProperty);
    this.onConfirm = Objects.requireNonNull(onConfirm);
    PortfolioController portfolioController1 = Objects.requireNonNull(portfolioController, "portfolioController cannot be null");
    this.calculator = Objects.requireNonNull(calculator, "calculator cannot be null");
    this.commissionRate = Objects.requireNonNull(commissionRate, "commissionRate cannot be null");
    this.taxRate = Objects.requireNonNull(taxRate, "taxRate cannot be null");
    assembleBody();
    summaryTextLabel1.setText("You Receive");
    summaryTextLabel2.setText("Fees and Taxes");
    summaryTextLabel3.setText("Gross Proceeds");
  }

  public void show(StackPane root, Share share) {
    this.root = Objects.requireNonNull(root);
    bindToShare(share);
    if (!root.getChildren().contains(dialog)) {
      dialog.setOpacity(0);
      dialog.setScaleX(0.1);
      dialog.setScaleY(0.1);
      root.getChildren().add(dialog);
      openAnimation.play();
    }
  }

  private void bindToShare(Share share) {
    stockSymbol.setText("Sell " + share.stock().getSymbol());
    stockName.setText(share.stock().getCompany());
    transactionButton.setText("$ Sell");
    qtyValue.set(BigDecimal.ZERO);

    ObservableValue<BigDecimal> price = share.stock().getPriceBinding();

    currentPrice.textProperty().unbind();
    currentPrice.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(price.getValue()), price));

    amountLabel.setText("Owned: %s".formatted(share.quantity().toPlainString()));

    ObjectBinding<BigDecimal> gross = Bindings.createObjectBinding(() ->
        calculator.calculateGross(share), share.stock().getPriceBinding());

    ObjectBinding<BigDecimal> net = Bindings.createObjectBinding(() ->
      calculator.calculateTotal(share, commissionRate, taxRate)
    , share.stock().getPriceBinding());

    ObjectBinding<BigDecimal> fee = Bindings.createObjectBinding(
        () -> gross.get().subtract(net.get()), share.stock().getPriceBinding());

    maxButton.setOnAction(_ -> {
      BigDecimal m = share.quantity();
      quantityField.setText(m.toPlainString());
    });

    summaryLabel3.textProperty().unbind();
    summaryLabel3.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(gross.get()), gross));

    summaryLabel2.textProperty().unbind();
    summaryLabel2.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(fee.get()), fee));

    summaryLabel1.textProperty().unbind();
    summaryLabel1.textProperty().bind(Bindings.createStringBinding(
        () -> formatMoney(net.get()), net));

    transactionButton.setOnAction(_ -> {
      onConfirm.accept(share, qtyValue.get());
      close();
    });
  }
}
