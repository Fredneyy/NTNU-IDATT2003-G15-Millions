package ntnu.idatt2003.group15.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Observable holder for tunable game-wide parameters driven by the settings panel.
 *
 * <p>A single {@code difficulty} multiplier (0.5x – 2.5x) is the user-facing knob.
 * All other knobs ({@link #volatilityMultiplierProperty()},
 * {@link #newsIntervalSecondsProperty()}, {@link #maxEventChanceProperty()})
 * are derived from it and recomputed whenever difficulty changes, so views and
 * controllers can simply bind to whichever value they care about.
 */
public class GameSettings {

  /** Minimum difficulty multiplier (easiest). */
  public static final double MIN_DIFFICULTY = 0.5;
  /** Maximum difficulty multiplier (hardest). */
  public static final double MAX_DIFFICULTY = 2.5;
  /** Default difficulty multiplier (normal). */
  public static final double DEFAULT_DIFFICULTY = 1.0;

  private final DoubleProperty difficulty =
      new SimpleDoubleProperty(this, "difficulty", DEFAULT_DIFFICULTY);

  private final ReadOnlyDoubleWrapper volatilityMultiplier =
      new ReadOnlyDoubleWrapper(this, "volatilityMultiplier", DEFAULT_DIFFICULTY);
  private final ReadOnlyDoubleWrapper newsIntervalSeconds =
      new ReadOnlyDoubleWrapper(this, "newsIntervalSeconds", 60.0);
  private final ReadOnlyDoubleWrapper maxEventChance =
      new ReadOnlyDoubleWrapper(this, "maxEventChance", 0.10);

  public GameSettings() {
    difficulty.addListener((_, _, v) -> recompute(v.doubleValue()));
    recompute(DEFAULT_DIFFICULTY);
  }

  private void recompute(double d) {
    double clamped = clamp(d, MIN_DIFFICULTY, MAX_DIFFICULTY);
    // Volatility scales 1:1 with difficulty.
    volatilityMultiplier.set(clamped);
    // News interval: 1x -> 60s, 2.5x -> 24s, 0.5x -> 120s.
    newsIntervalSeconds.set(60.0 / clamped);
    // Max event chance: 0.5x -> 8%, 1x -> 10%, 2.5x -> 16%.
    maxEventChance.set(0.06 + 0.04 * clamped);
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }

  // ---- difficulty (read/write) ----
  public DoubleProperty difficultyProperty() { return difficulty; }
  public double getDifficulty() { return difficulty.get(); }
  public void setDifficulty(double v) { difficulty.set(v); }

  // ---- derived (read-only) ----
  public ReadOnlyDoubleProperty volatilityMultiplierProperty() {
    return volatilityMultiplier.getReadOnlyProperty();
  }
  public double getVolatilityMultiplier() { return volatilityMultiplier.get(); }

  public ReadOnlyDoubleProperty newsIntervalSecondsProperty() {
    return newsIntervalSeconds.getReadOnlyProperty();
  }
  public double getNewsIntervalSeconds() { return newsIntervalSeconds.get(); }

  public ReadOnlyDoubleProperty maxEventChanceProperty() {
    return maxEventChance.getReadOnlyProperty();
  }
  public double getMaxEventChance() { return maxEventChance.get(); }
}
