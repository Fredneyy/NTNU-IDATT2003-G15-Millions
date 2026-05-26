package ntnu.idatt2003.group15.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
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
 * A reusable tab container.
 * The top row holds the tab buttons; below it a content area swaps in the
 * Node associated with the currently selected tab.
 */
public class TabContainer {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    /** Data model for a single tab. */
    public static class Tab {
        private final String id;
        private final String label;
        private final Ikon icon;          // Ikonli icon code; nullable
        private final Node content;
        private String badgeText;         // nullable; e.g. "9+", "0", or null for no badge

        public Tab(String id, String label, Ikon icon, Node content, String badgeText) {
            this.id = Objects.requireNonNull(id);
            this.label = Objects.requireNonNull(label);
            this.icon = icon;
            this.content = Objects.requireNonNull(content);
            this.badgeText = badgeText;
        }

        public Tab(String id, String label, Ikon icon, Node content) {
            this(id, label, icon, content, null);
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
        public Ikon getIcon() { return icon; }
        public Node getContent() { return content; }
        public String getBadgeText() { return badgeText; }
        public void setBadgeText(String badgeText) { this.badgeText = badgeText; }
    }

    private final VBox view = new VBox();
    private final HBox tabBar = new HBox();
    private final StackPane contentArea = new StackPane();

    private final ObservableList<Tab> tabs = FXCollections.observableArrayList();
    private final Map<String, HBox> tabButtons = new LinkedHashMap<>();
    private final Map<String, Label> badgeLabels = new LinkedHashMap<>();

    private final ReadOnlyObjectWrapper<Tab> selectedTab = new ReadOnlyObjectWrapper<>();

    public TabContainer() {
        view.getStyleClass().add("tab-view");
        tabBar.getStyleClass().add("tab-bar");
        contentArea.getStyleClass().add("tab-content");

        VBox.setVgrow(contentArea, Priority.ALWAYS);
        view.getChildren().addAll(tabBar, contentArea);
    }

    /** Convenience constructor: build with a list of tabs and auto-select the first. */
    public TabContainer(Tab... initialTabs) {
        this();
        for (Tab t : initialTabs) addTab(t);
        if (!tabs.isEmpty()) {
            select(tabs.getFirst().getId());
        }
    }

    public void addTab(Tab tab) {
        Objects.requireNonNull(tab);
        if (tabButtons.containsKey(tab.getId())) {
            throw new IllegalArgumentException(
                "A tab with id '" + tab.getId()
                    + "' has already been added to this container. Each tab needs a unique id.");
        }

        HBox button = buildTabButton(tab);
        tabs.add(tab);
        tabButtons.put(tab.getId(), button);
        tabBar.getChildren().add(button);

        button.setOnMouseClicked(_ -> select(tab.getId()));
    }

    /** Select a tab by id. No-op if id is unknown. */
    public void select(String id) {
        Tab target = findTab(id);
        if (target == null) {
            return;
        }

        for (Map.Entry<String, HBox> entry : tabButtons.entrySet()) {
            boolean active = entry.getKey().equals(id);
            entry.getValue().pseudoClassStateChanged(SELECTED, active);
        }

        contentArea.getChildren().setAll(target.getContent());
        selectedTab.set(target);
    }

    /** Update (or clear) a tab's badge text at runtime. */
    public void setBadge(String tabId, String badgeText) {
        Tab tab = findTab(tabId);
        if (tab == null) {
            return;
        }
        tab.setBadgeText(badgeText);

        Label badge = badgeLabels.get(tabId);
        if (badge == null) {
            return;
        }

        if (badgeText == null || badgeText.isBlank()) {
            badge.setVisible(false);
            badge.setManaged(false);
        } else {
            badge.setText(badgeText);
            badge.setVisible(true);
            badge.setManaged(true);
        }
    }

    public VBox getView() { return view; }

    public ReadOnlyObjectProperty<Tab> selectedTabProperty() {
        return selectedTab.getReadOnlyProperty();
    }

    private Tab findTab(String id) {
        for (Tab t : tabs) if (t.getId().equals(id)) {
            return t;
        }
        return null;
    }

    private HBox buildTabButton(Tab tab) {
        HBox button = new HBox();
        button.getStyleClass().add("tab-button");
        button.setFocusTraversable(true);

        if (tab.getIcon() != null) {
            FontIcon icon = new FontIcon(tab.getIcon());
            icon.getStyleClass().add("tab-icon");
            button.getChildren().add(icon);
        }

        Label label = new Label(tab.getLabel());
        label.getStyleClass().add("tab-label");
        button.getChildren().add(label);

        // always created so visibility can toggle at runtime
        Label badge = new Label(tab.getBadgeText() == null ? "" : tab.getBadgeText());
        badge.getStyleClass().add("tab-badge");
        boolean hasBadge = tab.getBadgeText() != null && !tab.getBadgeText().isBlank();
        badge.setVisible(hasBadge);
        badge.setManaged(hasBadge);
        button.getChildren().add(badge);
        badgeLabels.put(tab.getId(), badge);

        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);

        return button;
    }
}
