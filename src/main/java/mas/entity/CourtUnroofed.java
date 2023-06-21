package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Represents an unroofed court in a tennis court management application.
 * It extends the base Court class and adds specific properties related to unroofed courts.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtUnroofed extends Court {

    /**
     * Constructs a CourtUnroofed with the given number and surface type.
     * @param number the court number
     * @param surfaceType the surface type of the court
     */
    public CourtUnroofed(Integer number, SurfaceType surfaceType) {
        super(number, surfaceType);
    }

    /**
     * Checks if the court is available for booking during the specified time period.
     * The court is available if it falls within the season start and end dates,
     * and if it is not already booked for the specified time period.
     * @param from the start time of the booking
     * @param duration the duration of the booking
     * @return true if the court is available, false otherwise
     */
    @Override
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return from.isAfter(getSeasonStart().atStartOfDay()) &&
                from.isBefore(getSeasonEnd().atStartOfDay()) &&
                super.isAvailable(from, duration);
    }

    /**
     * Returns the string representation of the CourtUnroofed.
     * @return the string representation of the CourtUnroofed
     */
    @Override
    public String toString() {
        return "CourtUnroofed{" +
                "number=" + getNumber() +
                ", surfaceType=" + getSurfaceType() +
                '}';
    }

    /**
     * Returns the price per hour for an unroofed court from the database.
     * @return the price per hour for an unroofed court
     */
    public static BigDecimal getPricePerHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedPricePerHour from StaticStorage ss", BigDecimal.class).getSingleResult();
    }

    /**
     * Returns the start date of the season for an unroofed court from the database.
     * @return the start date of the season for an unroofed court
     */
    public static LocalDate getSeasonStart() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    /**
     * Returns the end date of the season for an unroofed court from the database.
     * @return the end date of the season for an unroofed court
     */
    public static LocalDate getSeasonEnd() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    /**
     * Returns the information text for the unroofed court.
     * @return the information text for the unroofed court
     */
    public String getInfoTxt() {
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());

        return "Zadaszenie: brak\n" +
                "Nawierzchnia: " + getSurfaceType() + "\n" +
                "Cena: " + NumberFormat.getCurrencyInstance().format(getPricePerHour()) + "\n" +
                "Początek sezonu: " + getSeasonStart().format(df) + "\n" +
                "Koniec sezonu: " + getSeasonEnd().format(df) + "\n";
    }
}
