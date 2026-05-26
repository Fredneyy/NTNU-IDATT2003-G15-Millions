package ntnu.idatt2003.group15.view;

import javafx.animation.*;
import javafx.scene.control.Button;
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

    private final StackPane view = new StackPane();

  private boolean open = false;
    private final double naturalHeight = 0;

  private final Label difficultyValueLabel = new Label("Normal");

  private final Label sliderValueLabel = new Label("1.0x");

  private final Slider difficultySlider = new Slider(0.5, 2.5, 1.0);

  private final Label priceVolatilityValue;
    private final Label maxEventChanceValue;

  public SettingsView(StackPane root) {
        Objects.requireNonNull(root);

      VBox eventFrequencyCard = buildStatCard("Event Frequency", "Normal");
    Label eventFrequencyValue = (Label) eventFrequencyCard.getChildren().get(1);
      VBox priceVolatilityCard = buildStatCard("Price Volatility", "100%");
      priceVolatilityValue = (Label) priceVolatilityCard.getChildren().get(1);
    VBox maxEventChanceCard = buildStatCard("Max Event Chance", "12.0%");
    maxEventChanceValue = (Label) maxEventChanceCard.getChildren().get(1);

    Label tipLabel = new Label(
        "Pro tip: Higher difficulty means more frequent events, higher volatility, " +
            "and more technical stocks. Perfect for experienced traders!"
    );
    tipLabel.setWrapText(true);
        HBox.setHgrow(tipLabel, javafx.scene.layout.Priority.ALWAYS);
        tipLabel.setMaxWidth(Double.MAX_VALUE);

      FontIcon icon = new FontIcon(FontAwesome.SLIDERS);
      StackPane iconBox = new StackPane();
      iconBox.getChildren().add(icon);

      Region headerSpacer = new Region();
      HBox.setHgrow(headerSpacer, javafx.scene.layout.Priority.ALWAYS);
      Region sliderHeaderSpacer = new Region();
      HBox.setHgrow(sliderHeaderSpacer, javafx.scene.layout.Priority.ALWAYS);
      Region sliderScaleSpacerLeft = new Region();
      HBox.setHgrow(sliderScaleSpacerLeft, javafx.scene.layout.Priority.ALWAYS);
      Region sliderScaleSpacerRight = new Region();
      HBox.setHgrow(sliderScaleSpacerRight, javafx.scene.layout.Priority.ALWAYS);

        HBox.setHgrow(eventFrequencyCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(priceVolatilityCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(maxEventChanceCard, javafx.scene.layout.Priority.ALWAYS);
        eventFrequencyCard.setMaxWidth(Double.MAX_VALUE);
        priceVolatilityCard.setMaxWidth(Double.MAX_VALUE);
        maxEventChanceCard.setMaxWidth(Double.MAX_VALUE);

        view.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/style/SettingsStyle.css")).toExternalForm());
        view.getStyleClass().add("settings-view");
      VBox card = new VBox();
      card.getStyleClass().add("settings-card");

        iconBox.getStyleClass().add("settings-icon-box");
        icon.getStyleClass().add("settings-icon");
      Label title = new Label("Difficulty Settings");
      title.getStyleClass().add("settings-title");
      Label subtitle = new Label("Adjust market volatility and event frequency");
      subtitle.getStyleClass().add("settings-subtitle");
      VBox titleBox = new VBox(title, subtitle);
      titleBox.getStyleClass().add("settings-title-box");
      HBox iconAndTitle = new HBox(iconBox, titleBox);
      iconAndTitle.getStyleClass().add("settings-icon-and-title");
        difficultyValueLabel.getStyleClass().add("settings-difficulty-badge");

        FontIcon closeIcon = new FontIcon(FontAwesome.TIMES);
        closeIcon.getStyleClass().add("settings-close-icon");
        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().add("settings-close-button");
        closeButton.setOnAction(_ -> {
            if (open) {
              toggle();
            }
        });

      HBox headerContent = new HBox(iconAndTitle, headerSpacer, difficultyValueLabel, closeButton);
      headerContent.getStyleClass().add("settings-header");

      Label sliderLabel = new Label("Market Difficulty");
      sliderLabel.getStyleClass().add("settings-slider-label");
        sliderValueLabel.getStyleClass().add("settings-slider-value");
      HBox sliderHeader = new HBox(sliderLabel, sliderHeaderSpacer, sliderValueLabel);
      sliderHeader.getStyleClass().add("settings-slider-header");
        difficultySlider.getStyleClass().add("settings-slider");
      Label sliderMinLabel = new Label("0.5x (Easiest)");
      sliderMinLabel.getStyleClass().add("settings-slider-scale-label");
      Label sliderMidLabel = new Label("1.0x (Normal)");
      sliderMidLabel.getStyleClass().add("settings-slider-scale-label");
      Label sliderMaxLabel = new Label("2.5x (Hardest)");
      sliderMaxLabel.getStyleClass().add("settings-slider-scale-label");
      HBox sliderScale = new HBox(
          sliderMinLabel, sliderScaleSpacerLeft,
          sliderMidLabel, sliderScaleSpacerRight,
          sliderMaxLabel
      );
      sliderScale.getStyleClass().add("settings-slider-scale");
      VBox sliderSection = new VBox(sliderHeader, difficultySlider, sliderScale);
      sliderSection.getStyleClass().add("settings-slider-section");

      Region divider = new Region();
      divider.getStyleClass().add("settings-divider");

      HBox statsRow = new HBox(eventFrequencyCard, priceVolatilityCard, maxEventChanceCard);
      statsRow.getStyleClass().add("settings-stats-row");

    FontIcon tipIcon = new FontIcon(FontAwesome.BOLT);
    tipIcon.getStyleClass().add("settings-tip-icon");
        tipLabel.getStyleClass().add("settings-tip-label");
    HBox tipBox = new HBox(tipIcon, tipLabel);
    tipBox.getStyleClass().add("settings-tip-box");

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


    public Slider getDifficultySlider() {
        return difficultySlider;
    }

    public Label getDifficultyValueLabel() {
        return difficultyValueLabel;
    }

    public Label getSliderValueLabel() {
        return sliderValueLabel;
    }

  public Label getPriceVolatilityValue() {
        return priceVolatilityValue;
    }

    public Label getMaxEventChanceValue() {
        return maxEventChanceValue;
    }
}