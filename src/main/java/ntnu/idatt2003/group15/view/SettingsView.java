package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class SettingsView {

    // Root container
    private final StackPane view = new StackPane();
    private final VBox card = new VBox();

    private boolean open = false;
    private double naturalHeight = 0;

  private final Label title = new Label("Difficulty Settings");
    private final Label subtitle = new Label("Adjust market volatility and event frequency");
    private final VBox titleBox = new VBox(title, subtitle);
  private final Label difficultyValueLabel = new Label("Normal");
    private final Region headerSpacer = new Region();

  // --- Slider section ---
    private final Label sliderLabel = new Label("Market Difficulty");
    private final Label sliderValueLabel = new Label("1.0x");
    private final Region sliderHeaderSpacer = new Region();
    private final HBox sliderHeader = new HBox(sliderLabel, sliderHeaderSpacer, sliderValueLabel);

    private final Slider difficultySlider = new Slider(0.5, 2.5, 1.0);

    private final Label sliderMinLabel = new Label("0.5x (Easiest)");
    private final Label sliderMidLabel = new Label("1.0x (Normal)");
    private final Label sliderMaxLabel = new Label("2.5x (Hardest)");
    private final Region sliderScaleSpacerLeft = new Region();
    private final Region sliderScaleSpacerRight = new Region();
    private final HBox sliderScale = new HBox(
            sliderMinLabel, sliderScaleSpacerLeft,
            sliderMidLabel, sliderScaleSpacerRight,
            sliderMaxLabel
    );

    private final VBox sliderSection = new VBox(sliderHeader, difficultySlider, sliderScale);

    // --- Divider ---
    private final Region divider = new Region();

    // --- Stat cards row ---
    private final VBox eventFrequencyCard = buildStatCard("Event Frequency", "Normal");
    private final VBox priceVolatilityCard = buildStatCard("Price Volatility", "100%");
    private final VBox maxEventChanceCard = buildStatCard("Max Event Chance", "12.0%");

  // Keep references to the value labels so you can update them later
    private final Label eventFrequencyValue;
    private final Label priceVolatilityValue;
    private final Label maxEventChanceValue;

    // --- Pro tip footer ---
    private final FontIcon tipIcon = new FontIcon(FontAwesome.BOLT);
    private final Label tipLabel = new Label(
            "Pro tip: Higher difficulty means more frequent events, higher volatility, " +
                    "and more technical stocks. Perfect for experienced traders!"
    );
    private final HBox tipBox = new HBox(tipIcon, tipLabel);

    public SettingsView(StackPane root) {
        Objects.requireNonNull(root);

        // Grab references to the stat card value labels (second child of each card VBox)
        eventFrequencyValue = (Label) eventFrequencyCard.getChildren().get(1);
        priceVolatilityValue = (Label) priceVolatilityCard.getChildren().get(1);
        maxEventChanceValue = (Label) maxEventChanceCard.getChildren().get(1);

        tipLabel.setWrapText(true);
        HBox.setHgrow(tipLabel, javafx.scene.layout.Priority.ALWAYS);
        tipLabel.setMaxWidth(Double.MAX_VALUE);

      FontIcon icon = new FontIcon(FontAwesome.SLIDERS);
      // --- Header ---
      StackPane iconBox = new StackPane();
      iconBox.getChildren().add(icon);

        // Let spacers push content apart in HBoxes
        HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(sliderHeaderSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(sliderScaleSpacerLeft, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(sliderScaleSpacerRight, javafx.scene.layout.Priority.ALWAYS);

        // Make stat cards share the row equally
        HBox.setHgrow(eventFrequencyCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(priceVolatilityCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(maxEventChanceCard, javafx.scene.layout.Priority.ALWAYS);
        eventFrequencyCard.setMaxWidth(Double.MAX_VALUE);
        priceVolatilityCard.setMaxWidth(Double.MAX_VALUE);
        maxEventChanceCard.setMaxWidth(Double.MAX_VALUE);

        // Style class hooks for your CSS
        view.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/style/SettingsStyle.css")).toExternalForm());
        view.getStyleClass().add("settings-view");
        card.getStyleClass().add("settings-card");

        iconBox.getStyleClass().add("settings-icon-box");
        icon.getStyleClass().add("settings-icon");
        title.getStyleClass().add("settings-title");
        subtitle.getStyleClass().add("settings-subtitle");
        titleBox.getStyleClass().add("settings-title-box");
      HBox iconAndTitle = new HBox(iconBox, titleBox);
      iconAndTitle.getStyleClass().add("settings-icon-and-title");
        difficultyValueLabel.getStyleClass().add("settings-difficulty-badge");
      HBox headerContent = new HBox(iconAndTitle, headerSpacer, difficultyValueLabel);
      headerContent.getStyleClass().add("settings-header");

        sliderLabel.getStyleClass().add("settings-slider-label");
        sliderValueLabel.getStyleClass().add("settings-slider-value");
        sliderHeader.getStyleClass().add("settings-slider-header");
        difficultySlider.getStyleClass().add("settings-slider");
        sliderMinLabel.getStyleClass().add("settings-slider-scale-label");
        sliderMidLabel.getStyleClass().add("settings-slider-scale-label");
        sliderMaxLabel.getStyleClass().add("settings-slider-scale-label");
        sliderScale.getStyleClass().add("settings-slider-scale");
        sliderSection.getStyleClass().add("settings-slider-section");

        divider.getStyleClass().add("settings-divider");

      HBox statsRow = new HBox(eventFrequencyCard, priceVolatilityCard, maxEventChanceCard);
      statsRow.getStyleClass().add("settings-stats-row");

        tipIcon.getStyleClass().add("settings-tip-icon");
        tipLabel.getStyleClass().add("settings-tip-label");
        tipBox.getStyleClass().add("settings-tip-box");

        // Compose the card
        card.getChildren().addAll(
            headerContent,
                sliderSection,
                divider,
            statsRow,
                tipBox
        );

        view.getChildren().add(card);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        view.setManaged(false);
        view.setVisible(false);
        view.setPrefHeight(0);
    }

    public void toggle() {
        KeyValue heightKv;
        KeyValue opacityKv;

        if (open) {
            heightKv  = new KeyValue(view.prefHeightProperty(), 0, Interpolator.EASE_IN);
            opacityKv = new KeyValue(view.opacityProperty(), 0, Interpolator.EASE_IN);
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.millis(250), heightKv, opacityKv)
            );
            tl.setOnFinished(_ -> {
                view.setManaged(false);
                view.setVisible(false);
                view.setOpacity(1);       // reset for next open
                view.setPrefHeight(naturalHeight);  // reset for next open
            });
            tl.play();
        } else {
            view.setOpacity(0);
            view.setPrefHeight(0);
            view.setManaged(true);
            view.setVisible(true);
            heightKv  = new KeyValue(view.prefHeightProperty(), naturalHeight, Interpolator.EASE_OUT);
            opacityKv = new KeyValue(view.opacityProperty(), 1, Interpolator.EASE_OUT);
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(Duration.millis(250), heightKv, opacityKv)
            );
            tl.setOnFinished(_ -> view.setPrefHeight(Region.USE_COMPUTED_SIZE));
            tl.play();
        }
        open = !open;
    }

    private VBox buildStatCard(String labelText, String valueText) {
        Label label = new Label(labelText);
        Label value = new Label(valueText);
        label.setWrapText(true);
        value.setWrapText(true);
        label.getStyleClass().add("settings-stat-label");
        value.getStyleClass().add("settings-stat-value");
        VBox box = new VBox(label, value);
        box.getStyleClass().add("settings-stat-card");
        return box;
    }

    public StackPane getView() {
        return view;
    }

    // --- Public accessors for the controller ---

    public Slider getDifficultySlider() {
        return difficultySlider;
    }

    public Label getDifficultyValueLabel() {
        return difficultyValueLabel;
    }

    public Label getSliderValueLabel() {
        return sliderValueLabel;
    }

    public Label getEventFrequencyValue() {
        return eventFrequencyValue;
    }

    public Label getPriceVolatilityValue() {
        return priceVolatilityValue;
    }

    public Label getMaxEventChanceValue() {
        return maxEventChanceValue;
    }
}