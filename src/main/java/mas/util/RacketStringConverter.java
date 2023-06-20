package mas.util;

import mas.entity.Racket;

import java.text.DecimalFormat;

public class RacketStringConverter extends javafx.util.StringConverter<Racket> {
    @Override
    public String toString(Racket racket) {
        if (racket == null) return "";
        return racket.getManufacturer() + ", " + new DecimalFormat("#,###0").format(racket.getWeight()) + "g - " +
                DecimalFormat.getCurrencyInstance().format(racket.getPricePerHour());
    }

    @Override
    public Racket fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
