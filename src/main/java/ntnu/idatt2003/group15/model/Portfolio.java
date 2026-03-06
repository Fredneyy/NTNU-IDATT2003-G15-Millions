package ntnu.idatt2003.group15.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portfolio {
  private final List<Share> shares = new ArrayList<>();

  public boolean addShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "InputShare cannot be null");
    return shares.add(inputShare);
  }

  public boolean removeShare(Share inputShare) throws NullPointerException {
    Objects.requireNonNull(inputShare, "InputShare cannot be null");
    if (shares.stream().anyMatch(share -> share.equals(inputShare))) {
      return false;
    } else {
      return shares.remove(inputShare);
    }
  }

  public List<Share> getShares() {
    return List.copyOf(shares);
  }

  public List<Share> getShares(String symbol) throws NullPointerException {
    Objects.requireNonNull(symbol, "Symbol cannot be null");
    return shares.stream()
        .filter(share -> share.getStock().getSymbol().equalsIgnoreCase(symbol))
        .toList();
  }

  public boolean contains(Share inputShare) throws NullPointerException {
    return shares.contains(inputShare);
  }
}
