package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.TimeUnavailableException;
import org.hibernate.TypeMismatchException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(nullable = false)
    private Duration duration;

    @Column(nullable = false)
    private Boolean isPaid;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Court court;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Person participant;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Person client;

    @ManyToOne
    private Racket racket;

    private Reservation(LocalDateTime start, Duration duration, Court court, Racket racket,
                        Person client, Person participant) {
        this.start = start;
        this.duration = duration;
        this.court = court;
        this.racket = racket;
        this.participant = participant;
        this.client = client;
        this.isPaid = false;
    }

    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Racket racket,
                                              Person client, Person participant) throws TimeUnavailableException, TypeMismatchException {
        if (!client.getPersonTypes().contains(Person.PersonType.Client))
            throw new TypeMismatchException("First argument person type is not Client.");
        if (!participant.getPersonTypes().contains(Person.PersonType.Participant))
            throw new TypeMismatchException("Second argument person type is not Participant.");

        if (!court.isAvailable(start, duration))
            throw new TimeUnavailableException(court, start, duration);

        var reservation = new Reservation(start, duration, court, null, participant, client);

        court.getReservations().add(reservation);
        client.getReservationsBought().add(reservation);
        participant.getReservations().add(reservation);

        if (racket != null) {
            reservation.setRacket(racket);
            racket.getReservations().add(reservation);
        }

        return reservation;
    }

    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Person client, Person participant) {
        return makeReservation(start, duration, court, null, client, participant);
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (getCourt() instanceof CourtRoofed) {
            totalPrice = totalPrice.add(CourtRoofed.getPricePerHour().multiply(BigDecimal.valueOf(getDuration().toHours())));
            if (LocalDateTime.now().isAfter(CourtRoofed.getHeatingSeasonStart().atStartOfDay()) && LocalDateTime.now().isBefore(CourtRoofed.getHeatingSeasonEnd().atStartOfDay())) {
                totalPrice = totalPrice.add(CourtRoofed.getHeatingSurcharge());
            }
        } else if (getCourt() instanceof CourtUnroofed) {
            totalPrice = totalPrice.add(CourtUnroofed.getPricePerHour().multiply(BigDecimal.valueOf(getDuration().toHours())));
        }

        if (getRacket() != null) {
            totalPrice = totalPrice.add(getRacket().getPricePerHour().multiply(BigDecimal.valueOf(getDuration().toHours())));
        }

        return totalPrice;
    }

    public void pay() {
        // out of scope
        throw new UnsupportedOperationException("out of scope");
    }

    public void cancel() {
        // out of scope
        throw new UnsupportedOperationException("out of scope");
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "start=" + getStart() +
                ", duration=" + getDuration() +
                ", court=" + getCourt().getNumber() +
                ", participant=" + getParticipant().getId() +
                ", client=" + getClient().getId() +
                ", racket=" + Optional.ofNullable(getRacket()).map(Racket::getId).map(Object::toString).orElse("null") +
                ", isPaid=" + getIsPaid() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;

        if (!getStart().equals(that.getStart())) return false;
        if (!getDuration().equals(that.getDuration())) return false;
        if (!getCourt().equals(that.getCourt())) return false;
        if (!getParticipant().equals(that.getParticipant())) return false;
        return getClient().equals(that.getClient());
    }

    @Override
    public int hashCode() {
        int result = getStart().hashCode();
        result = 31 * result + getDuration().hashCode();
        result = 31 * result + getCourt().hashCode();
        result = 31 * result + getParticipant().hashCode();
        result = 31 * result + getClient().hashCode();
        return result;
    }
}
