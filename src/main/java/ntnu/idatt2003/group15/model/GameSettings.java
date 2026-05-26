package ntnu.idatt2003.group15.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Observable holder for tunable game-wide parameters driven by the settings panel.
 *
 * <p>A single {@code difficulty} multiplier (0.5x – 2.5x) is the user-facing knob.
 * All other knobs ({@link #volatilityMultiplierProperty()}, {@link #maxEventChanceProperty()})
 * are derived from it and recomputed whenever difficulty changes, so views and
 * controllers can simply bind to whichever value they care about.
 */
public class GameSettings {

  /**
   * The constant MIN_DIFFICULTY.
   */
  public static final double MIN_DIFFICULTY = 0.5;
  /**
   * The constant MAX_DIFFICULTY.
   */
  public static final double MAX_DIFFICULTY = 2.5;
  /**
   * The constant DEFAULT_DIFFICULTY.
   */
  public static final double DEFAULT_DIFFICULTY = 1.0;

  private final DoubleProperty difficulty =
      new SimpleDoubleProperty(this, "difficulty", DEFAULT_DIFFICULTY);
  private final ReadOnlyDoubleWrapper volatilityMultiplier =
      new ReadOnlyDoubleWrapper(this, "volatilityMultiplier", DEFAULT_DIFFICULTY);
  private final ReadOnlyDoubleWrapper maxEventChance =
      new ReadOnlyDoubleWrapper(this, "maxEventChance", 0.10);

  /**
   * Instantiates a new Game settings.
   */
  public GameSettings() {
    difficulty.addListener((_, _, v) -> recompute(v.doubleValue()));
    recompute(DEFAULT_DIFFICULTY);
  }

  private void recompute(double d) {
    double clamped = clamp(d);
    volatilityMultiplier.set(clamped);
    maxEventChance.set(0.06 + 0.04 * clamped);
  }

  private static double clamp(double v) {
    return Math.max(GameSettings.MIN_DIFFICULTY, Math.min(GameSettings.MAX_DIFFICULTY, v));
  }

  /**
   * Difficulty property double property.
   *
   * @return the double property
   */
  public DoubleProperty difficultyProperty() {
    return difficulty;
  }

  /**
   * Gets difficulty.
   *
   * @return the difficulty
   */
  public double getDifficulty() {
    return difficulty.get();
  }

  /**
   * Sets difficulty.
   *
   * @param v the v
   */
  public void setDifficulty(double v) {
    difficulty.set(v);
  }

  /**
   * Volatility multiplier property read only double property.
   *
   * @return the read only double property
   */
  public ReadOnlyDoubleProperty volatilityMultiplierProperty() {
    return volatilityMultiplier.getReadOnlyProperty();
  }

  /**
   * Gets volatility multiplier.
   *
   * @return the volatility multiplier
   */
  public double getVolatilityMultiplier() {
    return volatilityMultiplier.get();
  }

  /**
   * Max event chance property read only double property.
   *
   * @return the read only double property
   */
  public ReadOnlyDoubleProperty maxEventChanceProperty() {
    return maxEventChance.getReadOnlyProperty();
  }

  /**
   * Gets max event chance.
   *
   * @return the max event chance
   */
  public double getMaxEventChance() {
    return maxEventChance.get();
  }
}
