package ntnu.idatt2003.group15.view;

import javafx.scene.Node;
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
 * A row of stat cards: icon + label + big value + sub line.
 *
 * Cards are added via {@link #addCard(StatCard)} and values can be updated
 * at runtime via {@link #setValue(String, String)} and
 * {@link #setSubline(String, String, Tone)}.
 */
public class StatisticsOverview {

    /** Visual tone applied to the value/subline text and the icon background. */
    public enum Tone {
        NEUTRAL,  // white value, muted subline
        POSITIVE, // green
        NEGATIVE, // red
        BLUE,
        GREEN,
        PURPLE,
        RED
    }

    /** Data model for one stat card. */
    public static class StatCard {
        private final String id;
        private final String label;
        private final Ikon icon;
        private final Tone iconTone;       // controls the icon background color
        private String value;
        private String subline;
        private Tone valueTone;            // controls value text color
        private Tone sublineTone;          // controls subline text color

        public StatCard(String id,
                        String label,
                        Ikon icon,
                        Tone iconTone,
                        String value,
                        String subline,
                        Tone valueTone,
                        Tone sublineTone) {
            this.id = Objects.requireNonNull(id);
            this.label = Objects.requireNonNull(label);
            this.icon = icon;
            this.iconTone = iconTone == null ? Tone.NEUTRAL : iconTone;
            this.value = value == null ? "" : value;
            this.subline = subline == null ? "" : subline;
            this.valueTone = valueTone == null ? Tone.NEUTRAL : valueTone;
            this.sublineTone = sublineTone == null ? Tone.NEUTRAL : sublineTone;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public Ikon getIcon() { return icon; }
        public Tone getIconTone() { return iconTone; }
        public String getValue() { return value; }
        public String getSubline() { return subline; }
        public Tone getValueTone() { return valueTone; }
        public Tone getSublineTone() { return sublineTone; }
    }

    /** Holds the live nodes per card so values can be updated later. */
    private static class CardNodes {
        final VBox root;
        final Label valueLabel;
        final Label sublineLabel;
        final StatCard model;

        CardNodes(VBox root, Label valueLabel, Label sublineLabel, StatCard model) {
            this.root = root;
            this.valueLabel = valueLabel;
            this.sublineLabel = sublineLabel;
            this.model = model;
        }
    }

    private final HBox view = new HBox();
    private final Map<String, CardNodes> cards = new LinkedHashMap<>();

    public StatisticsOverview() {
        view.getStyleClass().add("stats-overview");
    }

    /** Convenience: build with an initial set of cards. */
    public StatisticsOverview(StatCard... initialCards) {
        this();
        for (StatCard c : initialCards) addCard(c);
    }

    public void addCard(StatCard card) {
        Objects.requireNonNull(card);
        if (cards.containsKey(card.getId())) {
            throw new IllegalArgumentException("Stat card id already exists: " + card.getId());
        }

        VBox cardRoot = new VBox();
        cardRoot.getStyleClass().add("stat-card");

        // ---- Header row (icon + label) ----
        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().addAll("stat-icon-box", toneClass("stat-icon-box", card.getIconTone()));

        if (card.getIcon() != null) {
            FontIcon icon = new FontIcon(card.getIcon());
            icon.getStyleClass().addAll("stat-icon", toneClass("stat-icon", card.getIconTone()));
            iconBox.getChildren().add(icon);
        }

        Label labelLabel = new Label(card.getLabel());
        labelLabel.getStyleClass().add("stat-card-label");

        HBox header = new HBox(iconBox, labelLabel);
        header.getStyleClass().add("stat-card-header");

        // ---- Value ----
        Label valueLabel = new Label(card.getValue());
        valueLabel.getStyleClass().addAll("stat-card-value", toneClass("stat-card-value", card.getValueTone()));

        // ---- Subline ----
        Label sublineLabel = new Label(card.getSubline());
        sublineLabel.getStyleClass().addAll("stat-card-subline", toneClass("stat-card-subline", card.getSublineTone()));

        cardRoot.getChildren().addAll(header, valueLabel, sublineLabel);

        HBox.setHgrow(cardRoot, Priority.ALWAYS);
        cardRoot.setMaxWidth(Double.MAX_VALUE);

        cards.put(card.getId(), new CardNodes(cardRoot, valueLabel, sublineLabel, card));
        view.getChildren().add(cardRoot);
    }

    /** Update the main value of a card by id. */
    public void setValue(String cardId, String newValue) {
        CardNodes nodes = cards.get(cardId);
        if (nodes == null) return;
        nodes.valueLabel.setText(newValue == null ? "" : newValue);
    }

    /** Update the main value AND its tone (e.g. switch to red on negative). */
    public void setValue(String cardId, String newValue, Tone tone) {
        CardNodes nodes = cards.get(cardId);
        if (nodes == null) return;
        nodes.valueLabel.setText(newValue == null ? "" : newValue);
        retoneStyleClasses(nodes.valueLabel, "stat-card-value", tone);
    }

    /** Update the subline text. */
    public void setSubline(String cardId, String newSubline) {
        CardNodes nodes = cards.get(cardId);
        if (nodes == null) return;
        nodes.sublineLabel.setText(newSubline == null ? "" : newSubline);
    }

    /** Update the subline text AND its tone. */
    public void setSubline(String cardId, String newSubline, Tone tone) {
        CardNodes nodes = cards.get(cardId);
        if (nodes == null) return;
        nodes.sublineLabel.setText(newSubline == null ? "" : newSubline);
        retoneStyleClasses(nodes.sublineLabel, "stat-card-subline", tone);
    }

    public HBox getView() { return view; }

    // ---- internals ----

    private static String toneClass(String base, Tone tone) {
        return base + "--" + tone.name().toLowerCase();
    }

    /**
     * Remove any old "{base}--xxx" class on the node and add the new one
     * for the given tone, so tone changes don't pile up classes.
     */
    private static void retoneStyleClasses(Node node, String base, Tone tone) {
        node.getStyleClass().removeIf(c -> c.startsWith(base + "--"));
        node.getStyleClass().add(toneClass(base, tone));
    }
}