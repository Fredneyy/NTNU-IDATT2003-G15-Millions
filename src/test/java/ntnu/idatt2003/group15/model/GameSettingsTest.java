package ntnu.idatt2003.group15.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSettingsTest {

  private static final double DELTA = 1e-9;

  private GameSettings settings;

  @BeforeEach
  void setUp() {
    settings = new GameSettings();
  }

  @Test
  void defaultDifficultyIsOne() {
    assertEquals(GameSettings.DEFAULT_DIFFICULTY, settings.getDifficulty(), DELTA);
  }

  @Test
  void derivedValuesMatchDefaultDifficulty() {
    assertEquals(1.0, settings.getVolatilityMultiplier(), DELTA);
    assertEquals(60.0, settings.getNewsIntervalSeconds(), DELTA);
    assertEquals(0.10, settings.getMaxEventChance(), DELTA);
  }

  @Test
  void difficultyBelowMinimumIsClampedInDerivedValues() {
    settings.setDifficulty(0.1);

    // difficultyProperty itself stores the raw value; only the derived knobs clamp.
    assertEquals(GameSettings.MIN_DIFFICULTY, settings.getVolatilityMultiplier(), DELTA);
    assertEquals(60.0 / GameSettings.MIN_DIFFICULTY, settings.getNewsIntervalSeconds(), DELTA);
    assertEquals(0.06 + 0.04 * GameSettings.MIN_DIFFICULTY, settings.getMaxEventChance(), DELTA);
  }

  @Test
  void difficultyAboveMaximumIsClampedInDerivedValues() {
    settings.setDifficulty(10.0);

    assertEquals(GameSettings.MAX_DIFFICULTY, settings.getVolatilityMultiplier(), DELTA);
    assertEquals(60.0 / GameSettings.MAX_DIFFICULTY, settings.getNewsIntervalSeconds(), DELTA);
    assertEquals(0.06 + 0.04 * GameSettings.MAX_DIFFICULTY, settings.getMaxEventChance(), DELTA);
  }

  @Test
  void changingDifficultyRecomputesDerivedValues() {
    settings.setDifficulty(2.0);

    assertEquals(2.0, settings.getVolatilityMultiplier(), DELTA);
    assertEquals(30.0, settings.getNewsIntervalSeconds(), DELTA);
    assertEquals(0.14, settings.getMaxEventChance(), DELTA);
  }

  @Test
  void difficultyPropertyExposesWritableProperty() {
    assertNotNull(settings.difficultyProperty());
    settings.difficultyProperty().set(1.75);
    assertEquals(1.75, settings.getDifficulty(), DELTA);
  }

  @Test
  void difficultyPropertyIsTheSameReferenceAcrossCalls() {
    assertSame(settings.difficultyProperty(), settings.difficultyProperty());
  }

  @Test
  void readOnlyDerivedPropertiesReflectDifficulty() {
    settings.setDifficulty(1.5);

    assertEquals(1.5, settings.volatilityMultiplierProperty().get(), DELTA);
    assertEquals(60.0 / 1.5, settings.newsIntervalSecondsProperty().get(), DELTA);
    assertEquals(0.06 + 0.04 * 1.5, settings.maxEventChanceProperty().get(), DELTA);
  }
}
