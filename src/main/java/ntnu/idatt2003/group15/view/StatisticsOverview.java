package ntnu.idatt2003.group15.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A row of stat cards: icon + label + big value.
 * All dynamic fields are observables, so cards update live as their sources change.
 */
public class StatisticsOverview {

    private static final String STYLESHEET =
        Objects.requireNonNull(
            StatisticsOverview.class.getResource("/style/StatisticsOverview.css")).toExternalForm();

    /** Data model for one stat card. All dynamic fields are observables. */
    public record StatCard(
        String id,
        String label,
        Ikon icon,
        ObservableValue<String> value
    ) {
        public StatCard {
            Objects.requireNonNull(id);
            Objects.requireNonNull(label);
            value = value == null ? new SimpleStringProperty("") : value;
        }
    }

    /** Holds the live nodes per card so they can be referenced later. */
    private static class CardNodes {
        final VBox root;
        final Label valueLabel;
        final StatCard model;

        CardNodes(VBox root, Label valueLabel, StatCard model) {
            this.root = root;
            this.valueLabel = valueLabel;
            this.model = model;

            applySign(valueLabel, valueLabel.getText());
            valueLabel.textProperty().addListener((_, _, newText) -> applySign(valueLabel, newText));
        }

        private static void applySign(Label label, String text) {
            label.getStyleClass().removeAll("stat-positive", "stat-negative");
            if (text == null || text.isBlank()) return;
            char first = text.charAt(0);
            if (first == '+') label.getStyleClass().add("stat-positive");
            else if (first == '-') label.getStyleClass().add("stat-negative");
        }
    }

    private final HBox view = new HBox();
    private final Map<String, CardNodes> cards = new LinkedHashMap<>();

    public StatisticsOverview() {
        view.getStyleClass().add("stats-overview");
        view.getStylesheets().add(STYLESHEET);
    }

    public void addCard(StatCard card) {
        Objects.requireNonNull(card);
        if (cards.containsKey(card.id())) {
            throw new IllegalArgumentException(
                "A statistic card with id '" + card.id()
                    + "' has already been added to this overview. Each card needs a unique id.");
        }

        VBox cardRoot = new VBox();
        cardRoot.getStyleClass().add("stat-card");

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("stat-icon-box");

        if (card.icon() != null) {
            FontIcon icon = new FontIcon(card.icon());
            icon.getStyleClass().add("stat-icon");
            iconBox.getChildren().add(icon);
        }

        Label labelLabel = new Label(card.label());
        labelLabel.getStyleClass().add("stat-card-label");

        HBox header = new HBox(iconBox, labelLabel);
        header.getStyleClass().add("stat-card-header");

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(card.value());
        valueLabel.getStyleClass().add("stat-card-value");

        cardRoot.getChildren().addAll(header, valueLabel);

        HBox.setHgrow(cardRoot, Priority.ALWAYS);
        cardRoot.setMaxWidth(Double.MAX_VALUE);

        cards.put(card.id(), new CardNodes(cardRoot, valueLabel, card));
        view.getChildren().add(cardRoot);
    }

    public HBox getView() { return view; }
}
