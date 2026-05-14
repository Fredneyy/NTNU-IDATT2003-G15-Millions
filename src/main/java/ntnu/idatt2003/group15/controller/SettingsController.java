package ntnu.idatt2003.group15.controller;

import java.util.Objects;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import ntnu.idatt2003.group15.model.GameSettings;
import ntnu.idatt2003.group15.view.SettingsView;

/**
 * Binds a {@link SettingsView} to a {@link GameSettings} model.
 *
 * <p>The slider in the view becomes the single source of truth for difficulty;
 * derived knobs (volatility multiplier, news interval, max event chance) are
 * exposed via {@code GameSettings} so other controllers (exchange, news) can
 * react. The view's labels are updated live as the user drags the slider.
 */
public class SettingsController {

  private final SettingsView view;
  private final GameSettings settings;

  public SettingsController(SettingsView view, GameSettings settings) {
    this.view = Objects.requireNonNull(view, "view");
    this.settings = Objects.requireNonNull(settings, "settings");
    bind();
  }

  public GameSettings getSettings() {
    return settings;
  }

  private void bind() {
    Slider slider = view.getDifficultySlider();
    // Seed the slider from the model, then keep them in sync both ways.
    slider.setValue(settings.getDifficulty());
    slider.valueProperty().bindBidirectional(settings.difficultyProperty());

    ChangeListener<Number> refresh = (_, _, _) -> refreshLabels();
    settings.difficultyProperty().addListener(refresh);
    settings.volatilityMultiplierProperty().addListener(refresh);
    settings.newsIntervalSecondsProperty().addListener(refresh);
    settings.maxEventChanceProperty().addListener(refresh);

    refreshLabels();
  }

  private void refreshLabels() {
    double d = settings.getDifficulty();
    view.getSliderValueLabel().setText(formatMultiplier(d));
    view.getDifficultyValueLabel().setText(difficultyName(d));
    view.getEventFrequencyValue().setText(eventFrequencyLabel(settings.getNewsIntervalSeconds()));
    view.getPriceVolatilityValue().setText(percentLabel(settings.getVolatilityMultiplier()));
    view.getMaxEventChanceValue().setText(
        String.format("%.1f%%", settings.getMaxEventChance() * 100.0));
  }

  private static String formatMultiplier(double d) {
    return String.format("%.1fx", d);
  }

  private static String percentLabel(double multiplier) {
    return String.format("%.0f%%", multiplier * 100.0);
  }

  private static String difficultyName(double d) {
    if (d < 0.75)  return "Easy";
    if (d < 1.25)  return "Normal";
    if (d < 1.75)  return "Hard";
    if (d < 2.25)  return "Expert";
    return "Insane";
  }

  private static String eventFrequencyLabel(double intervalSeconds) {
    if (intervalSeconds >= 100) return "Slow";
    if (intervalSeconds >= 55)  return "Normal";
    if (intervalSeconds >= 35)  return "Fast";
    return "Frantic";
  }
}
