package mas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Represents a roofed tennis court.
 * It extends the base Court class and adds additional properties
 * related to the roofed courts' nature.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtRoofed extends Court {

    /**
     * Enumeration of the possible roof types for the court.
     */
    public enum RoofType {
        Hall("Hala"),
        Balloon("Balon");

        private final String name;

        /**
         * Constructs a RoofType with the given name.
         * @param name the name of the roof type
         */
        RoofType(String name) {
            this.name = name;
        }

        /**
         * Returns the string representation of the roof type.
         * @return the string representation of the roof type
         */
        @Override
        public String toString() {
            return name;
        }
    }

    @Enumerated
    private RoofType roofType;

    /**
     * Constructs a CourtRoofed with the given number, surface type, and roof type.
     * @param number the court number
     * @param surfaceType the surface type of the court
     * @param roofType the roof type of the court
     */
    public CourtRoofed(Integer number, SurfaceType surfaceType, RoofType roofType) {
        super(number, surfaceType);
        this.roofType = roofType;
    }

    /**
     * Returns the string representation of the CourtRoofed.
     * @return the string representation of the CourtRoofed
     */
    @Override
    public String toString() {
        return "CourtRoofed{" +
                "number=" + getNumber() +
                ", surfaceType=" + getSurfaceType() +
                ", roofType=" + roofType +
                "}";
    }

    /**
     * Returns the price per hour for a roofed court from the database.
     * @return the price per hour for a roofed court
     */
    public static BigDecimal getPricePerHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedPricePerHour from StaticStorage ss", BigDecimal.class).getSingleResult();
    }

    /**
     * Returns the heating surcharge for a roofed court from the database.
     * @return the heating surcharge for a roofed court
     */
    public static BigDecimal getHeatingSurcharge() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSurcharge from StaticStorage ss", BigDecimal.class).getSingleResult();
    }

    /**
     * Returns the start date of the heating season for a roofed court from the database.
     * @return the start date of the heating season for a roofed court
     */
    public static LocalDate getHeatingSeasonStart() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    /**
     * Returns the end date of the heating season for a roofed court from the database.
     * @return the end date of the heating season for a roofed court
     */
    public static LocalDate getHeatingSeasonEnd() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    /**
     * Returns the heating surcharge for a roofed court on the given date.
     * If the date is outside the heating season, the surcharge is zero.
     * @param date the date to check
     * @return the heating surcharge for a roofed court
     */
    public static BigDecimal getHeatingSurcharge(LocalDate date) {
        if (date.isBefore(getHeatingSeasonStart()) || date.isAfter(getHeatingSeasonEnd())) {
            return BigDecimal.ZERO;
        } else {
            return getHeatingSurcharge();
        }
    }

    /**
     * Returns the information text for the roofed court.
     * @return the information text for the roofed court
     */
    @Override
    public String getInfoTxt() {
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());

        return "Zadaszenie: " + getRoofType() + "\n" +
                "Nawierzchnia: " + getSurfaceType() + "\n" +
                "Cena poza sezonem grzewczym: " + NumberFormat.getCurrencyInstance().format(getPricePerHour()) + "\n" +
                "Początek sezonu grzewczego: " + getHeatingSeasonStart().format(df) + "\n" +
                "Koniec sezonu grzewczego: " + getHeatingSeasonEnd().format(df) + "\n" +
                "Dopłata grzewcza: " + NumberFormat.getCurrencyInstance().format(getHeatingSurcharge()) + "\n";
    }
}
