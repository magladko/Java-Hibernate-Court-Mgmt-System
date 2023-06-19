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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Racket extends Equipment {

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @OneToMany(mappedBy = "racket")
    private Set<Reservation> reservations = new HashSet<>();

    public void addReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (!this.getReservations().contains(r)) {
                this.getReservations().add(r);
                r.setRacket(this);
            }
        }
    }

    public void removeReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (this.getReservations().contains(r)) {
                this.getReservations().remove(r);
                r.setRacket(null);
            }
        }
    }

    public Racket(String manufacturer, Double weight, BigDecimal pricePerHour) {
        this.manufacturer = manufacturer;
        this.weight = weight;
        this.pricePerHour = pricePerHour;
    }

    public Racket(String manufacturer, Double weight, BigDecimal pricePerHour, String notes) {
        super(notes);
        this.manufacturer = manufacturer;
        this.weight = weight;
        this.pricePerHour = pricePerHour;
    }

    @Override
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream().noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                super.isAvailable(from, duration);
    }

    public boolean isAvailable(LocalDate date) {
        var openingHours = DBController.INSTANCE.getSS().getCourtOpeningHour();
        var closingHours = DBController.INSTANCE.getSS().getCourtClosingHour();

        for (int h = openingHours.getHour(); h < closingHours.getHour(); h++) {
            if (isAvailable(LocalDateTime.of(date, LocalTime.of(h, 0)), Duration.ofHours(1))) return true;
        }
        return false;
    }

    public boolean makeReservation(Reservation reservation) {
        if (!isAvailable(reservation.getStart(), reservation.getDuration())) return false;
        addReservations(reservation);
        return true;
    }

    @Override
    public String toString() {
        return "Racket{" +
                "notes='" + getNotes() + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", weight=" + weight +
                ", pricePerHour=" + pricePerHour +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Racket racket)) return false;
        if (!super.equals(o)) return false;

        if (!getManufacturer().equals(racket.getManufacturer())) return false;
        if (!getWeight().equals(racket.getWeight())) return false;
        return getPricePerHour().equals(racket.getPricePerHour());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getManufacturer().hashCode();
        result = 31 * result + getWeight().hashCode();
        result = 31 * result + getPricePerHour().hashCode();
        return result;
    }
}
