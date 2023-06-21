package mas.util;

import javafx.util.StringConverter;
import mas.entity.Racket;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class converts Racket objects to Strings and vice versa using JavaFX StringConverter.
 * It provides a formatted representation of the racket including manufacturer, weight, and price per hour.
 */
public class RacketStringConverter extends StringConverter<Racket> {

    /**
     * Converts a Racket object to a formatted String representation.
     *
     * @param racket The Racket object to convert.
     * @return The formatted String representation of the racket.
     */
    @Override
    public String toString(Racket racket) {
        if (racket == null) {
            return "";
        }

        String manufacturer = racket.getManufacturer();
        double weight = racket.getWeight();
        BigDecimal pricePerHour = racket.getPricePerHour();

        String formattedWeight = formatWeight(weight);
        String formattedPrice = formatPrice(pricePerHour);

        return manufacturer + ", " + formattedWeight + " - " + formattedPrice;
    }

    /**
     * Converts a String to a Racket object.
     * This operation is not supported and will throw an UnsupportedOperationException.
     *
     * @param string The String to convert.
     * @return This method always throws UnsupportedOperationException.
     * @throws UnsupportedOperationException This operation is not supported.
     */
    @Override
    public Racket fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Formats the weight value to include the unit "g".
     *
     * @param weight The weight value to format.
     * @return The formatted weight string.
     */
    private String formatWeight(double weight) {
        return new DecimalFormat("#,###0").format(weight) + "g";
    }

    /**
     * Formats the price per hour value as currency.
     *
     * @param pricePerHour The price per hour value to format.
     * @return The formatted price string.
     */
    private String formatPrice(BigDecimal pricePerHour) {
        NumberFormat currencyFormatter = DecimalFormat.getCurrencyInstance();
        return currencyFormatter.format(pricePerHour);
    }
}
