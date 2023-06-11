package mas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.Util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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

    public boolean makeReservation(Reservation reservation) {
        if (!isAvailable(reservation.getStart(), reservation.getDuration())) return false;

        reservation.setRacket(this);
        return getReservations().add(reservation);
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
}
