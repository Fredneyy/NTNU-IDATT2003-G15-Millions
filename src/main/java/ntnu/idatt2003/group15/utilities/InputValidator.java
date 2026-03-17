package ntnu.idatt2003.group15.utilities;

import java.math.BigDecimal;

public class InputValidator {
    public static boolean isBigDecimalValuePositive (String variableName, BigDecimal price) throws IllegalArgumentException, NullPointerException {
        if (price == null) {
            throw new NullPointerException(variableName + " cannot be null");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(variableName + " must be greater than zero");
        }

        return true;
    }
}
