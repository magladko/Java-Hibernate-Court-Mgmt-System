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

/**
 * Represents a reservation for a tennis court in the Tennis Courts management application.
 */
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

    @Column
    private String comment;

    /**
     * Sets the comment for the reservation.
     * If the comment is null, the existing comment is set to null.
     * If the comment is empty after trimming, the existing comment is set to null.
     *
     * @param comment the comment for the reservation
     */
    public void setComment(String comment) {
        if (comment == null) {
            this.comment = null;
            return;
        }

        comment = comment.trim();
        if (comment.isEmpty()) comment = null;

        this.comment = comment;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Court court;

    /**
     * Sets the court for the reservation.
     * If the new court is the same as the current court, no action is taken.
     * If the current court is not null, the reservation is removed from the current court's reservations list.
     * The reservation is added to the new court's reservations list.
     *
     * @param court the court for the reservation
     */
    public void setCourt(Court court) {
        if (this.getCourt() != null && this.getCourt().equals(court)) return;
        if (this.getCourt() != null) this.getCourt().getReservations().remove(this);
        this.court = court;
        if (this.getCourt() != null) this.getCourt().addReservations(this);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Person participant;

    /**
     * Sets the participant for the reservation.
     * If the participant is not a participant type, a TypeMismatchException is thrown.
     * If the new participant is the same as the current participant, no action is taken.
     * If the current participant is not null, the reservation is removed from the current participant's reservations list.
     * The reservation is added to the new participant's reservations list.
     *
     * @param participant the participant for the reservation
     * @throws TypeMismatchException if the person is not a participant
     */
    public void setParticipant(Person participant) {
        if (!participant.getPersonTypes().contains(Person.PersonType.PARTICIPANT))
            throw new TypeMismatchException("Person is not a participant");

        if (this.getParticipant() != null && this.getParticipant().equals(participant)) return;
        if (this.getParticipant() != null) this.getParticipant().getReservations().remove(this);
        this.participant = participant;
        if (this.getParticipant() != null) getParticipant().addReservations(this);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Person client;

    /**
     * Sets the client for the reservation.
     * If the client is not a client type, a TypeMismatchException is thrown.
     * If the new client is the same as the current client, no action is taken.
     * If the current client is not null, the reservation is removed from the current client's bought reservations list.
     * The reservation is added to the new client's bought reservations list.
     *
     * @param client the client for the reservation
     * @throws TypeMismatchException if the person is not a client
     */
    public void setClient(Person client) {
        if (client != null && !client.getPersonTypes().contains(Person.PersonType.CLIENT))
            throw new TypeMismatchException("Person is not a client");

        if (this.getClient() != null && this.getClient().equals(client)) return;
        if (this.getClient() != null) this.getClient().getReservationsBought().remove(this);
        this.client = client;
        if (this.getClient() != null) getClient().addReservationsBought(this);
    }

    @ManyToOne
    private Racket racket;

    /**
     * Sets the racket for the reservation.
     * If the new racket is the same as the current racket, no action is taken.
     * If the current racket is not null, the reservation is removed from the current racket's reservations list.
     * The reservation is added to the new racket's reservations list.
     *
     * @param racket the racket for the reservation
     */
    public void setRacket(Racket racket) {
        if (this.getRacket() != null && this.getRacket().equals(racket)) return;
        if (this.getRacket() != null) this.getRacket().getReservations().remove(this);
        this.racket = racket;
        if (this.getRacket() != null) getRacket().addReservations(this);
    }

    private Reservation(LocalDateTime start, Duration duration, Court court, Racket racket,
                        Person client, Person participant, String comment) {
        this.start = start;
        this.duration = duration;
        setCourt(court);
        setRacket(racket);
        setParticipant(participant);
        setClient(client);
        setIsPaid(false);
        setComment(comment);
    }

    /**
     * Creates a new reservation with the given parameters and returns it.
     *
     * @param start       the start time of the reservation
     * @param duration    the duration of the reservation
     * @param court       the court for the reservation
     * @param racket      the racket for the reservation (optional)
     * @param client      the client for the reservation
     * @param participant the participant for the reservation
     * @param comment     the comment for the reservation
     * @return the created reservation
     * @throws TimeUnavailableException if the court is unavailable during the specified time
     * @throws TypeMismatchException    if the participant or client is of incorrect type
     */
    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Racket racket,
                                              Person client, Person participant, String comment) throws TimeUnavailableException, TypeMismatchException {
        if (!client.getPersonTypes().contains(Person.PersonType.CLIENT))
            throw new TypeMismatchException("First argument person type is not Client.");
        if (!participant.getPersonTypes().contains(Person.PersonType.PARTICIPANT))
            throw new TypeMismatchException("Second argument person type is not Participant.");

        if (!court.isAvailable(start, duration))
            throw new TimeUnavailableException(court, start, duration);

        return new Reservation(start, duration, court, racket, participant, client, comment);
    }

    /**
     * Creates a new reservation with the given parameters and returns it.
     * The reservation does not include a racket or comment.
     *
     * @param start       the start time of the reservation
     * @param duration    the duration of the reservation
     * @param court       the court for the reservation
     * @param client      the client for the reservation
     * @param participant the participant for the reservation
     * @return the created reservation
     * @throws TimeUnavailableException if the court is unavailable during the specified time
     * @throws TypeMismatchException    if the participant or client is of incorrect type
     */
    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Person client, Person participant) throws TimeUnavailableException, TypeMismatchException {
        return makeReservation(start, duration, court, null, client, participant, null);
    }

    /**
     * Creates a new reservation with the given parameters and returns it.
     * The reservation includes a racket and does not include a comment.
     *
     * @param start       the start time of the reservation
     * @param duration    the duration of the reservation
     * @param court       the court for the reservation
     * @param racket      the racket for the reservation
     * @param client      the client for the reservation
     * @param participant the participant for the reservation
     * @return the created reservation
     * @throws TimeUnavailableException if the court is unavailable during the specified time
     * @throws TypeMismatchException    if the participant or client is of incorrect type
     */
    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Racket racket, Person client, Person participant) throws TimeUnavailableException, TypeMismatchException {
        return makeReservation(start, duration, court, racket, client, participant, null);
    }

    /**
     * Creates a new reservation with the given parameters and returns it.
     * The reservation includes a comment and does not include a racket.
     *
     * @param start       the start time of the reservation
     * @param duration    the duration of the reservation
     * @param court       the court for the reservation
     * @param client      the client for the reservation
     * @param participant the participant for the reservation
     * @param comment     the comment for the reservation
     * @return the created reservation
     * @throws TimeUnavailableException if the court is unavailable during the specified time
     * @throws TypeMismatchException    if the participant or client is of incorrect type
     */
    public static Reservation makeReservation(LocalDateTime start, Duration duration, Court court, Person client, Person participant, String comment) throws TimeUnavailableException, TypeMismatchException {
        return makeReservation(start, duration, court, null, client, participant, comment);
    }

    /**
     * Calculates and returns the total price of the reservation.
     * The price is calculated based on the type of court and the duration of the reservation.
     * If a racket is included in the reservation, the price is adjusted accordingly.
     *
     * @return the total price of the reservation
     */
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

    /**
     * Throws an UnsupportedOperationException as paying a reservation is out of scope for the class.
     *
     * @throws UnsupportedOperationException always
     */
    public void pay() {
        throw new UnsupportedOperationException("out of scope");
    }

    /**
     * Throws an UnsupportedOperationException as canceling a reservation is out of scope for the class.
     *
     * @throws UnsupportedOperationException always
     */
    public void cancel() {
        throw new UnsupportedOperationException("out of scope");
    }

    /**
     * Returns a string representation of the Reservation object.
     *
     * @return A string representation of the Reservation object.
     */
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

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the {@code o} argument; {@code false} otherwise.
     */
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

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = getStart().hashCode();
        result = 31 * result + getDuration().hashCode();
        result = 31 * result + getCourt().hashCode();
        return result;
    }
}
