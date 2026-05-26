package ntnu.idatt2003.group15.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import ntnu.idatt2003.group15.controller.PortfolioController;
import ntnu.idatt2003.group15.model.stocks.Share;
import ntnu.idatt2003.group15.model.stocks.Stock;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Card-styled live market table. Renders one row per {@link Stock} and
 * recolours change columns based on direction. Backing list is observable so
 * controller updates propagate automatically.
 */
public class MarketTableView {

  public enum EventStatus { NONE, POSITIVE, NEGATIVE }

  private final VBox view = new VBox();
  private final Label title = new Label("Live Market");
  private final Label subtitle = new Label("Real-time stock prices");
  private final TableView<Stock> table = new TableView<>();
  private final FilteredList<Stock> filteredStocks;
  private final SortedList<Stock> sortedStocks;
  private final PortfolioController portfolioController;
  private Function<Stock, EventStatus> eventLookup = _ -> EventStatus.NONE;
  private final Consumer<Stock> onBuyPressed;
  private final Consumer<Stock> onChartPressed;

  public MarketTableView(ObservableList<Stock> stocks,
                         PortfolioController portfolioController,
                         Consumer<Stock> onBuyPressed,
                         Consumer<Stock> onChartPressed) {
    this.portfolioController = Objects.requireNonNull(portfolioController);
    this.onBuyPressed = onBuyPressed;
    this.onChartPressed = onChartPressed;
    this.sortedStocks = new SortedList<>(stocks);
    this.filteredStocks = new FilteredList<>(sortedStocks, _ -> true);
    table.setMaxHeight(Double.MAX_VALUE);
    table.setMaxWidth(Double.MAX_VALUE);

    buildHeader();
    buildTable();

    view.getStyleClass().add("market-table");
    view.setFillWidth(true);
    VBox.setVgrow(view, Priority.ALWAYS);
    view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    view.getChildren().addAll(buildHeaderRow(), table);
  }

  private void sortByWinners() {
    sortedStocks.setComparator(
        Comparator.comparing(Stock::getLatestPriceChangeRelative).reversed()
    );
  }

  private void sortByLosers() {
    sortedStocks.setComparator(
        Comparator.comparing(Stock::getLatestPriceChangeRelative)
    );
  }

  private void clearFilter() {
    sortedStocks.setComparator(null);
  }

  private void buildHeader() {
    title.getStyleClass().add("market-table-title");
    subtitle.getStyleClass().add("market-table-subtitle");
  }

  private HBox buildHeaderRow() {
    FontIcon icon = new FontIcon(FontAwesome.LINE_CHART);
    icon.getStyleClass().add("market-table-icon");

    VBox titleBox = new VBox(title, subtitle);
    titleBox.getStyleClass().add("market-table-title-box");

    Button clearFilterButton = new Button("Clear Filter");
    clearFilterButton.getStyleClass().add("market-clearFilter-button");
    clearFilterButton.setOnAction(_ -> clearFilter());

    Button getVinnersButton = new Button("Get Winners");
    getVinnersButton.getStyleClass().add("market-buy-button");
    getVinnersButton.setOnAction(_ -> sortByWinners());

    Button getLoosersButton = new Button("Get Losers");
    getLoosersButton.getStyleClass().add("market-sell-button");
    getLoosersButton.setOnAction(_ -> sortByLosers());

    HBox losersAndWinners = new HBox(20, clearFilterButton, getVinnersButton, getLoosersButton);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox row = new HBox(icon, titleBox, spacer, losersAndWinners);
    row.getStyleClass().add("market-table-header");
    return row;
  }

  @SuppressWarnings("unchecked")
  private void buildTable() {
    TableColumn<Stock, Stock> symbolCol = new TableColumn<>("Symbol");
    symbolCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    symbolCol.setCellFactory(_ -> symbolCell());
    styleCellsAs(symbolCol, "col-symbol");

    TableColumn<Stock, String> companyCol = new TableColumn<>("Company");
    companyCol.setCellValueFactory(c -> c.getValue().companyProperty());
    styleCellsAs(companyCol, "col-company");

    TableColumn<Stock, BigDecimal> priceCol = new TableColumn<>("Price");
    priceCol.setCellValueFactory(c -> c.getValue().getPriceBinding());
    priceCol.setCellFactory(_ -> moneyCell());
    styleCellsAs(priceCol, "col-price");

    TableColumn<Stock, Stock> changeCol = new TableColumn<>("Change");
    changeCol.setCellValueFactory(c -> {
      Stock s = c.getValue();
      return Bindings.createObjectBinding(() -> s, s.getPriceBinding());
    });
    changeCol.setCellFactory(_ -> combinedChangeCell());
    styleCellsAs(changeCol, "col-change");

    ObservableList<Share> portfolioShares = portfolioController.getListProperty();
    TableColumn<Stock, BigDecimal> ownedCol = new TableColumn<>("Owned");
    ownedCol.setCellValueFactory(c -> {
      Stock s = c.getValue();
      return Bindings.createObjectBinding(
          () -> totalOwnedFor(s, portfolioShares),
          portfolioShares);
    });
    ownedCol.setCellFactory(_ -> ownedCell());
    styleCellsAs(ownedCol, "col-owned");

    TableColumn<Stock, Stock> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    statusCol.setCellFactory(_ -> statusCell(this::lookupEvent));
    styleCellsAs(statusCol, "col-status");

    TableColumn<Stock, Stock> actionCol = new TableColumn<>("Action");
    actionCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
    actionCol.setCellFactory(_ -> buyButtonCell());
    actionCol.setSortable(false);
    styleCellsAs(actionCol, "col-action");

    table.getColumns().setAll(
        symbolCol, companyCol, priceCol, changeCol, ownedCol, statusCol, actionCol);

    table.setItems(filteredStocks);
    table.setPlaceholder(new Label("No stocks match your filter."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.getStyleClass().add("market-table-inner");
  }

  private static <S, T> void styleCellsAs(TableColumn<S, T> column, String styleClass) {
    column.getStyleClass().add(styleClass);
  }

  private static TableCell<Stock, BigDecimal> moneyCell() {
    return new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setText(null);
        } else {
          setText("$" + value.setScale(2, RoundingMode.HALF_UP).toPlainString());
        }
      }
    };
  }

  private static TableCell<Stock, Stock> symbolCell() {
    return new TableCell<>() {
      private final Label avatarLabel = new Label();
      private final StackPane avatar = new StackPane(avatarLabel);
      private final HBox wrapper = new HBox(avatar);
      {
        avatar.getStyleClass().add("symbol-avatar");
        avatarLabel.getStyleClass().add("symbol-avatar-text");
        wrapper.getStyleClass().add("symbol-cell");
        wrapper.setSpacing(12);
        avatarLabel.setWrapText(true);
      }

      @Override
      protected void updateItem(Stock stock, boolean empty) {
        super.updateItem(stock, empty);
        if (empty || stock == null) {
          setGraphic(null);
          return;
        }
        String sym = stock.getSymbol();
        avatarLabel.setText(sym.length() >= 3 ? sym.substring(0, 3) : sym);
        setGraphic(wrapper);
      }
    };
  }

  private static TableCell<Stock, Stock> combinedChangeCell() {
    return new TableCell<>() {
      private final FontIcon arrow = new FontIcon();
      private final Label amount = new Label();
      private final Label percent = new Label();
      private final HBox wrapper = new HBox(arrow, amount, percent);
      {
        wrapper.getStyleClass().add("change-cell");
        amount.getStyleClass().add("change-amount");
        percent.getStyleClass().add("change-percent");
        wrapper.setSpacing(6);
      }

      @Override
      protected void updateItem(Stock stock, boolean empty) {
        super.updateItem(stock, empty);
        wrapper.getStyleClass().removeAll("change-cell--positive", "change-cell--negative");
        if (empty || stock == null) {
          setGraphic(null);
          return;
        }
        BigDecimal delta = stock.getLatestPriceChange();
        BigDecimal pct = stock.getLatestPriceChangeRelative().movePointRight(2);
        int sign = delta.signum();
        String tone = sign >= 0 ? "change-cell--positive" : "change-cell--negative";
        wrapper.getStyleClass().add(tone);

        arrow.setIconCode(sign >= 0 ? FontAwesome.LINE_CHART : FontAwesome.AREA_CHART);
        amount.setText((sign >= 0 ? "+" : "-") + "$"
            + delta.abs().setScale(2, RoundingMode.HALF_UP).toPlainString());
        percent.setText("(" + (sign >= 0 ? "+" : "")
            + pct.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%)");
        setGraphic(wrapper);
      }
    };
  }

  private static TableCell<Stock, BigDecimal> ownedCell() {
    return new TableCell<>() {
      @Override
      protected void updateItem(BigDecimal value, boolean empty) {
        super.updateItem(value, empty);
        getStyleClass().removeAll("cell-muted", "cell-owned");
        if (empty || value == null || value.signum() <= 0) {
          setText(empty ? null : "—");
          if (!empty) getStyleClass().add("cell-muted");
        } else {
          setText(value.stripTrailingZeros().toPlainString());
          getStyleClass().add("cell-owned");
        }
      }
    };
  }

  private static BigDecimal totalOwnedFor(Stock stock, List<Share> shares) {
    BigDecimal total = BigDecimal.ZERO;
    String symbol = stock.getSymbol();
    for (Share share : shares) {
      if (share.stock().getSymbol().equalsIgnoreCase(symbol)) {
        total = total.add(share.quantity());
      }
    }
    return total;
  }

  private static TableCell<Stock, Stock> statusCell(Function<Stock, EventStatus> lookup) {
    return new TableCell<>() {
      private final FontIcon warning = new FontIcon(FontAwesome.EXCLAMATION_TRIANGLE);
      private final Label text = new Label("EVENT");
      private final HBox badge = new HBox(warning, text);
      {
        badge.getStyleClass().add("status-badge");
        badge.setSpacing(6);
        text.getStyleClass().add("status-badge-text");
      }

      @Override
      protected void updateItem(Stock stock, boolean empty) {
        super.updateItem(stock, empty);
        badge.getStyleClass().removeAll("status-badge--positive", "status-badge--negative");
        getStyleClass().removeAll("cell-muted");
        if (empty || stock == null) {
          setText(null);
          setGraphic(null);
          return;
        }
        EventStatus status = lookup.apply(stock);
        if (status == null || status == EventStatus.NONE) {
          setText("—");
          setGraphic(null);
          getStyleClass().add("cell-muted");
          return;
        }
        badge.getStyleClass().add(
            status == EventStatus.POSITIVE
                ? "status-badge--positive" : "status-badge--negative");
        setText(null);
        setGraphic(badge);
      }
    };
  }

  private EventStatus lookupEvent(Stock s) { return eventLookup.apply(s); }

  private TableCell<Stock, Stock> buyButtonCell() {
    return new TableCell<>() {
      private final Button graphButton = new Button();
      private final Button buyButton = new Button("Buy");
      private final HBox wrapper = new HBox(graphButton, buyButton);
      {
        graphButton.setGraphic(new FontIcon(FontAwesome.AREA_CHART));
        graphButton.getStyleClass().add("market-graph-button");
        graphButton.setOnAction(_ -> {
          Stock stock = getItem();
          if (stock != null && onChartPressed != null) {
            onChartPressed.accept(stock);
          }
        });

        buyButton.setGraphic(new FontIcon(FontAwesome.SHOPPING_CART));
        buyButton.getStyleClass().add("market-buy-button");
        buyButton.setOnAction(_ -> {
          Stock stock = getItem();
          if (stock != null && onBuyPressed != null) {
            onBuyPressed.accept(stock);
          }
        });

        wrapper.getStyleClass().add("market-action-cell");
        wrapper.setSpacing(8);
      }

      @Override
      protected void updateItem(Stock stock, boolean empty) {
        super.updateItem(stock, empty);
        setGraphic(empty || stock == null ? null : wrapper);
      }
    };
  }

  /** Filter table rows by symbol/company substring (case-insensitive). Empty resets. */
  public void setSearchFilter(String query) {
    if (query == null || query.isBlank()) {
      filteredStocks.setPredicate(_ -> true);
      return;
    }
    String q = query.trim().toLowerCase();
    filteredStocks.setPredicate(s ->
        s.getSymbol().toLowerCase().contains(q)
            || s.getCompany().toLowerCase().contains(q));
  }

  public VBox getView() { return view; }

  /** Plug in the source of event status per stock. */
  public void setEventLookup(Function<Stock, EventStatus> lookup) {
    this.eventLookup = lookup == null ? _ -> EventStatus.NONE : lookup;
    table.refresh();
  }
}
