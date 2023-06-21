package mas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;
import mas.util.Util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a racket used in the Tennis Courts management application.
 * Extends the Equipment class.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Racket extends Equipment {

    /**
     * The manufacturer of the racket.
     */
    @Column(nullable = false)
    private String manufacturer;

    /**
     * The weight of the racket.
     */
    @Column(nullable = false)
    private Double weight;

    /**
     * The price per hour of using the racket.
     */
    @Column(nullable = false)
    private BigDecimal pricePerHour;

    /**
     * The set of reservations made for this racket.
     */
    @OneToMany(mappedBy = "racket")
    private Set<Reservation> reservations = new HashSet<>();

    /**
     * Adds the given reservations to the racket.
     *
     * @param reservations The reservations to add.
     */
    public void addReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (!this.getReservations().contains(r)) {
                this.getReservations().add(r);
                r.setRacket(this);
            }
        }
    }

    /**
     * Removes the given reservations from the racket.
     *
     * @param reservations The reservations to remove.
     */
    public void removeReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (this.getReservations().contains(r)) {
                this.getReservations().remove(r);
                r.setRacket(null);
            }
        }
    }

    /**
     * Constructs a Racket object with the given manufacturer, weight, and price per hour.
     *
     * @param manufacturer  The manufacturer of the racket.
     * @param weight        The weight of the racket.
     * @param pricePerHour  The price per hour of using the racket.
     */
    public Racket(String manufacturer, Double weight, BigDecimal pricePerHour) {
        this.manufacturer = manufacturer;
        this.weight = weight;
        this.pricePerHour = pricePerHour;
    }

    /**
     * Constructs a Racket object with the given manufacturer, weight, price per hour, and notes.
     *
     * @param manufacturer  The manufacturer of the racket.
     * @param weight        The weight of the racket.
     * @param pricePerHour  The price per hour of using the racket.
     * @param notes         The notes for the racket.
     */
    public Racket(String manufacturer, Double weight, BigDecimal pricePerHour, String notes) {
        super(notes);
        this.manufacturer = manufacturer;
        this.weight = weight;
        this.pricePerHour = pricePerHour;
    }

    /**
     * Checks if the racket is available for the given time period.
     *
     * @param from      The start time of the time period.
     * @param duration  The duration of the time period.
     * @return          True if the racket is available, false otherwise.
     */
    @Override
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream().noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                super.isAvailable(from, duration);
    }

    /**
     * Checks if the racket is available for the given date.
     *
     * @param date  The date to check.
     * @return      True if the racket is available, false otherwise.
     */
    public boolean isAvailable(LocalDate date) {
        var openingHours = DBController.INSTANCE.getSS().getCourtOpeningHour();
        var closingHours = DBController.INSTANCE.getSS().getCourtClosingHour();

        for (int h = openingHours.getHour(); h < closingHours.getHour(); h++) {
            if (isAvailable(LocalDateTime.of(date, LocalTime.of(h, 0)), Duration.ofHours(1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a reservation for the racket.
     *
     * @param reservation The reservation to make.
     * @return            True if the reservation was successfully made, false otherwise.
     */
    public boolean makeReservation(Reservation reservation) {
        if (!isAvailable(reservation.getStart(), reservation.getDuration())) {
            return false;
        }
        addReservations(reservation);
        return true;
    }

    /**
     * Returns a string representation of the Racket object.
     *
     * @return A string representation of the Racket object.
     */
    @Override
    public String toString() {
        return "Racket{" +
                "notes='" + getNotes() + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", weight=" + weight +
                ", pricePerHour=" + pricePerHour +
                '}';
    }

    /**
     * Checks if the Racket object is equal to another object.
     *
     * @param o The object to compare.
     * @return  True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Racket racket)) return false;
        if (!super.equals(o)) return false;

        if (!getManufacturer().equals(racket.getManufacturer())) return false;
        if (!getWeight().equals(racket.getWeight())) return false;
        return getPricePerHour().equals(racket.getPricePerHour());
    }

    /**
     * Returns the hash code of the Racket object.
     *
     * @return The hash code of the Racket object.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getManufacturer().hashCode();
        result = 31 * result + getWeight().hashCode();
        result = 31 * result + getPricePerHour().hashCode();
        return result;
    }
}
