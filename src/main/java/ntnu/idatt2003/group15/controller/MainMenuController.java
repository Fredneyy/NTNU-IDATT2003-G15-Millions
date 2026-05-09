package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.Objects;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Controller mediating the main menu, where a new {@link Player} is created before the game starts.
 * Validates the player name and produces the controllers needed to run the game.
 */
public class MainMenuController {

  private static final BigDecimal DEFAULT_STARTING_MONEY = new BigDecimal("10000");

  private final Exchange exchange;

  /**
   * Constructs a main-menu controller bound to the given exchange.
   *
   * @param exchange the exchange that will be used for the game session
   * @throws NullPointerException if exchange is null
   */
  public MainMenuController(Exchange exchange) {
    this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
  }

  /**
   * Creates a new {@link Player} with the given name and the default starting balance,
   * then returns an {@link ExchangeController} scoped to that player.
   *
   * @param name the name entered by the user
   * @return an {@link ExchangeController} ready for the game session
   * @throws NullPointerException   if name is null
   * @throws BlankArgumentException if name is blank
   */
  public ExchangeController startGame(String name, BigDecimal startingMoney) throws BlankArgumentException {
    Objects.requireNonNull(name, "Name cannot be null");
    if (startingMoney == null) {
      startingMoney = DEFAULT_STARTING_MONEY;
    }
    Player player = new Player(name, startingMoney);
    return new ExchangeController(exchange, player);
  }

  /**
   * Returns a {@link PlayerController} for the given name without starting a full game session.
   * Useful when the UI needs to preview player state before entering the exchange view.
   *
   * @param name the name entered by the user
   * @return a {@link PlayerController} wrapping the newly created player
   * @throws NullPointerException   if name is null
   * @throws BlankArgumentException if name is blank
   */
  public PlayerController createPlayerController(String name) throws BlankArgumentException {
    Objects.requireNonNull(name, "Name cannot be null");
    Player player = new Player(name, DEFAULT_STARTING_MONEY);
    return new PlayerController(player);
  }

  /**
   * Validates whether the given name is acceptable for a new player.
   *
   * @param name the name to validate
   * @return {@code true} if the name is non-null and non-blank, {@code false} otherwise
   */
  public boolean isValidName(String name) {
    return name != null && !name.isBlank();
  }

  /**
   * Returns the default starting money awarded to every new player.
   *
   * @return the default starting balance
   */
  public BigDecimal getDefaultStartingMoney() {
    return DEFAULT_STARTING_MONEY;
  }
}
