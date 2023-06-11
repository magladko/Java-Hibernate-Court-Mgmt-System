package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.TimeUnavailableException;
import org.hibernate.TypeMismatchException;

import java.time.Duration;
import java.time.LocalDateTime;

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
    }

    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Racket racket,
                                              Person client, Person participant) {
        if (!client.getPersonTypes().contains(Person.PersonType.Client))
            throw new TypeMismatchException("First argument person type is not Client.");
        if (!participant.getPersonTypes().contains(Person.PersonType.Participant))
            throw new TypeMismatchException("Second argument person type is not Participant.");

        if (court.isAvailable(start, duration))
            throw new TimeUnavailableException(Court.class, start, duration);

        var reservation = new Reservation(start, duration, court, null, participant, client);
        reservation.getClient().getReservationsBought().add(reservation);
        reservation.getParticipant().getReservations().add(reservation);
        return reservation;
    }

    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Person client, Person participant) {
        return makeReservation(start, duration, court, null, client, participant);
    }

    public void getTotalPrice() {
        // TODO: getting total price of reservation
    }

    public void pay() {
        // out of scope
    }

    public void cancel() {
        // out of scope
    }

}
