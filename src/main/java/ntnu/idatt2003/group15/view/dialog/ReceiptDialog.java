package ntnu.idatt2003.group15.view.dialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import javafx.animation.ParallelTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import ntnu.idatt2003.group15.model.stocks.Stock;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;

/**
 * Post-transaction confirmation dialog. Opens after a successful buy/sell to
 * acknowledge the order with a full breakdown — quantity, per-share price, and
 * totals (with fees for sells). Modal-style: user must click Done to dismiss.
 *
 * <p>Built on {@link BaseDialog} and visually a sibling of {@link TransactionDialog};
 * the two share several CSS classes ({@code stock-dialog-card}, {@code buy-header*},
 * {@code buy-card}, {@code buy-cost-card}) so the receipt feels like a natural
 * next step after the buy/sell flow rather than a different dialog system.
 */
public class ReceiptDialog extends BaseDialog {

  public enum Type { BUY, SELL }

  private final Label avatarLabel = new Label();
  private final Label headerTitle = new Label("Order succeeded");
  private final Label companyLabel = new Label();
  private final Label typeBadge = new Label();
  private final Label quantityValue = new Label();
  private final Label priceValue = new Label();

  // Totals card — three rows; row 3 is hidden for BUY (no fees).
  private final Label totalsLabel1 = new Label();
  private final Label totalsLabel2 = new Label();
  private final Label totalsLabel3 = new Label();
  private final Label totalsValue1 = new Label();
  private final Label totalsValue2 = new Label();
  private final Label totalsValue3 = new Label();
  private final HBox totalsRow3;

  private final ParallelTransition openAnimation;
  private final ParallelTransition closeAnimation;

  public ReceiptDialog() {
    super();
    // Pull in the trade-badge + buy-dialog stylesheets so the BUY/SELL chip
    // and the cost-card layout look identical to their counterparts elsewhere
    // in the app. The DialogStyle.css from BaseDialog stays loaded too.
    dialog.getStylesheets().addAll(
        Objects.requireNonNull(getClass().getResource("/style/TradesViewStyle.css"))
            .toExternalForm(),
        Objects.requireNonNull(getClass().getResource("/style/BuyStockDialogStyle.css"))
            .toExternalForm());

    // Match TransactionDialog's width (35% of screen, 420px floor) so receipts
    // and buy/sell dialogs share a footprint.
    double screenW = Screen.getPrimary().getVisualBounds().getWidth();
    dialog.setMaxWidth(Math.max(420, screenW * 0.35));
    // Lock height to the dialog's preferred (content) size — without this the
    // StackPane stretches the dialog vertically to fill the whole game-view
    // height, leaving lots of empty space below the Done button.
    dialog.setMaxHeight(Region.USE_PREF_SIZE);
    dialog.getStyleClass().setAll("stock-dialog-card", "buy-stock-dialog");

    totalsRow3 = makeSummaryRow(totalsLabel3, totalsValue3, false);

    VBox body = new VBox(buildHeader(), buildBody());
    body.getStyleClass().add("buy-dialog-body");
    dialog.getChildren().add(body);

    openAnimation = createOpenAnimation();
    closeAnimation = createCloseAnimation(_ -> {
      if (root != null) root.getChildren().remove(dialog);
    });
  }

  /**
   * Display the receipt over the given root with the supplied transaction
   * details. Safe to call repeatedly — each call re-binds the contents and
   * re-runs the open animation.
   *
   * @param root          parent {@link StackPane} the dialog mounts into
   * @param type          {@link Type#BUY} or {@link Type#SELL}
   * @param stock         the stock that was traded
   * @param quantity      number of shares
   * @param pricePerShare per-share price paid (BUY) or received (SELL)
   * @param fees          commission + tax (0 for BUY)
   * @param net           net cash change — gross for BUY, gross-minus-fees for SELL
   */
  public void show(StackPane root, Type type, Stock stock,
                   BigDecimal quantity, BigDecimal pricePerShare,
                   BigDecimal fees, BigDecimal net) {
    this.root = Objects.requireNonNull(root, "root");
    populate(type, stock, quantity, pricePerShare, fees, net);
    if (!root.getChildren().contains(dialog)) {
      dialog.setOpacity(0);
      dialog.setScaleX(0.1);
      dialog.setScaleY(0.1);
      root.getChildren().add(dialog);
      openAnimation.play();
    }
  }

  public void close() {
    if (root != null && root.getChildren().contains(dialog)) {
      closeAnimation.play();
    }
  }

  private void populate(Type type, Stock stock, BigDecimal quantity,
                        BigDecimal pricePerShare, BigDecimal fees, BigDecimal net) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(stock, "stock");
    Objects.requireNonNull(quantity, "quantity");
    Objects.requireNonNull(pricePerShare, "pricePerShare");
    BigDecimal safeFees = fees == null ? BigDecimal.ZERO : fees;
    BigDecimal safeNet = net == null ? BigDecimal.ZERO : net;

    String sym = stock.getSymbol() == null ? "" : stock.getSymbol();
    avatarLabel.setText(sym.length() >= 3 ? sym.substring(0, 3) : sym);
    companyLabel.setText(stock.getCompany() == null ? "" : stock.getCompany());

    typeBadge.setText(type == Type.BUY ? "BUY" : "SELL");
    typeBadge.getStyleClass().removeAll("trade-badge--buy", "trade-badge--sell");
    typeBadge.getStyleClass().add(type == Type.BUY ? "trade-badge--buy" : "trade-badge--sell");

    quantityValue.setText(quantity.stripTrailingZeros().toPlainString());
    priceValue.setText(formatMoney(pricePerShare));

    BigDecimal gross = pricePerShare.multiply(quantity);
    if (type == Type.BUY) {
      // For a buy: cost is the gross. No fees in this game's buy flow, so we
      // hide the third row entirely (managed=false drops it from layout).
      totalsLabel1.setText("Total Cost");
      totalsValue1.setText(formatMoney(gross));
      totalsLabel2.setText("Cash Change");
      totalsValue2.setText("-" + formatMoney(gross));
      totalsRow3.setVisible(false);
      totalsRow3.setManaged(false);
    } else {
      // For a sell: show the full breakdown — what the user got, what the
      // market valued the shares at, and what the broker took off the top.
      totalsLabel1.setText("You Received");
      totalsValue1.setText(formatMoney(safeNet));
      totalsLabel2.setText("Gross Proceeds");
      totalsValue2.setText(formatMoney(gross));
      totalsLabel3.setText("Fees & Taxes");
      totalsValue3.setText("-" + formatMoney(safeFees.abs()));
      totalsRow3.setVisible(true);
      totalsRow3.setManaged(true);
    }
  }

  private VBox buildHeader() {
    StackPane avatar = new StackPane();
    avatar.getStyleClass().add("stock-avatar");
    avatar.setMinSize(56, 56);
    avatar.setMaxSize(56, 56);
    avatarLabel.getStyleClass().add("stock-avatar-text");
    avatar.getChildren().add(avatarLabel);

    headerTitle.getStyleClass().add("buy-header-title");
    companyLabel.getStyleClass().add("buy-header-subtitle");

    VBox titleBox = new VBox(headerTitle, companyLabel);
    titleBox.getStyleClass().add("buy-header-text");
    HBox.setHgrow(titleBox, Priority.ALWAYS);

    Button closeBtn = new Button();
    FontIcon x = FontIcon.of(MaterialDesignC.CLOSE);
    x.setIconColor(Color.WHITE);
    closeBtn.setGraphic(x);
    closeBtn.getStyleClass().setAll("close-button", "buy-close-button");
    closeBtn.setAlignment(Pos.CENTER);
    closeBtn.setOnAction(_ -> close());

    HBox row = new HBox(16, avatar, titleBox, closeBtn);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("buy-header");
    return new VBox(row);
  }

  private VBox buildBody() {
    // Top card: type badge + quantity + price-per-share
    Label typeRowLabel = new Label("Type");
    typeRowLabel.getStyleClass().add("buy-cost-label");
    typeBadge.getStyleClass().add("trade-badge");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox typeRow = new HBox(typeRowLabel, spacer, typeBadge);
    typeRow.setAlignment(Pos.CENTER_LEFT);

    HBox quantityRow = makeSummaryRow(new Label("Quantity"), quantityValue, false);
    HBox priceRow = makeSummaryRow(new Label("Price per share"), priceValue, false);

    VBox detailsCard = new VBox(typeRow, quantityRow, priceRow);
    detailsCard.getStyleClass().add("buy-card");
    detailsCard.setSpacing(10);

    // Bottom card: totals (row 3 hidden for BUY)
    HBox totalsRow1 = makeSummaryRow(totalsLabel1, totalsValue1, true);
    HBox totalsRow2 = makeSummaryRow(totalsLabel2, totalsValue2, false);
    VBox totalsCard = new VBox(totalsRow1, totalsRow2, totalsRow3);
    totalsCard.getStyleClass().add("buy-cost-card");
    totalsCard.setSpacing(8);

    Button doneButton = new Button("Done");
    doneButton.getStyleClass().add("buy-confirm-button");
    doneButton.setMaxWidth(Double.MAX_VALUE);
    doneButton.setOnAction(_ -> close());

    VBox container = new VBox(detailsCard, totalsCard, doneButton);
    container.getStyleClass().add("buy-content");
    return container;
  }

  private static HBox makeSummaryRow(Label label, Label value, boolean major) {
    // Add the major/minor style classes idempotently so populate() can swap
    // labels between BUY and SELL without leaking classes from prior shows.
    if (major) {
      if (!label.getStyleClass().contains("buy-cost-label-major")) {
        label.getStyleClass().add("buy-cost-label-major");
      }
      if (!value.getStyleClass().contains("buy-cost-value-major")) {
        value.getStyleClass().add("buy-cost-value-major");
      }
    } else {
      if (!label.getStyleClass().contains("buy-cost-label")) {
        label.getStyleClass().add("buy-cost-label");
      }
      if (!value.getStyleClass().contains("buy-cost-value")) {
        value.getStyleClass().add("buy-cost-value");
      }
    }
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox row = new HBox(label, spacer, value);
    row.setAlignment(Pos.CENTER_LEFT);
    return row;
  }

  private static String formatMoney(BigDecimal v) {
    if (v == null) return "$0.00";
    return "$" + v.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }
}
