package ntnu.idatt2003.group15.controller;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.BiConsumer;

import javafx.beans.property.SimpleObjectProperty;
import ntnu.idatt2003.group15.model.Exchange;
import ntnu.idatt2003.group15.model.Player;
import ntnu.idatt2003.group15.model.exceptions.BlankArgumentException;

/**
 * Controller mediating the main menu, where a new {@link Player} is created before the game starts.
 * Validates the player name and produces the controllers needed to run the game.
 */
public class MainMenuController {

  private final BigDecimal DEFAULT_STARTING_MONEY = new BigDecimal("10000");
  private final BiConsumer<ExchangeController, PlayerController> onGameStartConsumer;

  private final Exchange exchange;

  /**
   * Constructs a main-menu controller bound to the given exchange.
   *
   * @param exchange the exchange that will be used for the game session
   * @throws NullPointerException if exchange is null
   */
  public MainMenuController(Exchange exchange, BiConsumer<ExchangeController, PlayerController> onGameStartConsumer) {
    this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
    this.onGameStartConsumer = onGameStartConsumer;
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
  public void startGame(String name, BigDecimal startingMoney) throws BlankArgumentException, NullPointerException {
    Objects.requireNonNull(name, "Name cannot be null");
    if (startingMoney == null) {
      startingMoney = DEFAULT_STARTING_MONEY;
    }
    Player player = new Player(name, startingMoney);
    PlayerController playerController = createPlayerController(player);
    onGameStartConsumer.accept(new  ExchangeController(exchange, player), playerController);
  }

  /**
   * Returns a {@link PlayerController} for the given name without starting a full game session.
   * Useful when the UI needs to preview player state before entering the exchange view.
   *
   * @param player the player object
   * @return a {@link PlayerController} wrapping the newly created player
   * @throws NullPointerException   if name is null
   */
  public PlayerController createPlayerController(Player player) throws NullPointerException {
    Objects.requireNonNull(player, "Player cannot be null");
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
