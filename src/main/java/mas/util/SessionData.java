package mas.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.entity.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * This class represents session data for the Tennis Courts management application.
 * It stores information about court reservations, participants, prices, and comments.
 */
@NoArgsConstructor
public class SessionData {

    @Getter @Setter private static volatile Scene courtReservationScene = null;
    @Getter @Setter private static volatile Scene summaryScene = null;

    private static final SimpleObjectProperty<Court> court = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<LocalDateTime> reservationStart = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<Duration> reservationDuration = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<Person> client = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<Person> participant = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<Racket> racket = new SimpleObjectProperty<>();
    private static final SimpleObjectProperty<Trainer> trainer = new SimpleObjectProperty<>();
    private static final SimpleStringProperty comment = new SimpleStringProperty();

    /**
     * Cancels the session by resetting all the session data.
     */
    public static void cancel() {
        courtReservationScene = null;
        summaryScene = null;

        court.unbind();
        reservationStart.unbind();
        reservationDuration.unbind();
        participant.unbind();
        client.unbind();
        racket.unbind();
        trainer.unbind();

        court.set(null);
        reservationStart.set(null);
        reservationDuration.set(null);
        participant.set(null);
        client.set(null);
        racket.set(null);
        trainer.set(null);
    }

    /**
     * Calculates the total price of the session based on the court, racket, trainer, and duration.
     *
     * @return The total price of the session.
     */
    public static BigDecimal getTotalPrice() {
        if (reservationStart.getValue() == null || reservationDuration.getValue() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal duration = BigDecimal.valueOf(reservationDuration.getValue().toHours());

        if (court.getValue() instanceof CourtRoofed) {
            totalPrice = totalPrice.add(CourtRoofed.getPricePerHour().multiply(duration))
                    .add(CourtRoofed.getHeatingSurcharge(reservationStart.getValue().toLocalDate()));
        } else if (court.getValue() instanceof CourtUnroofed) {
            totalPrice = totalPrice.add(CourtUnroofed.getPricePerHour().multiply(duration));
        }

        if (racket.getValue() != null) {
            totalPrice = totalPrice.add(racketProperty().getValue().getPricePerHour().multiply(duration));
        }

        if (trainer.getValue() != null) {
            totalPrice = totalPrice.add(trainerProperty().getValue().getPricePerHour().multiply(duration));
        }

        return totalPrice;
    }

    /**
     * Returns the court property.
     *
     * @return The court property.
     */
    public static SimpleObjectProperty<Court> courtProperty() {
        return court;
    }

    /**
     * Returns the reservation start property.
     *
     * @return The reservation start property.
     */
    public static SimpleObjectProperty<LocalDateTime> reservationStartProperty() {
        return reservationStart;
    }

    /**
     * Returns the reservation duration property.
     *
     * @return The reservation duration property.
     */
    public static SimpleObjectProperty<Duration> reservationDurationProperty() {
        return reservationDuration;
    }

    /**
     * Returns the participant property.
     *
     * @return The participant property.
     */
    public static SimpleObjectProperty<Person> participantProperty() {
        return participant;
    }

    /**
     * Returns the client property.
     *
     * @return The client property.
     */
    public static SimpleObjectProperty<Person> clientProperty() {
        return client;
    }

    /**
     * Returns the racket property.
     *
     * @return The racket property.
     */
    public static SimpleObjectProperty<Racket> racketProperty() {
        return racket;
    }

    /**
     * Returns the trainer property.
     *
     * @return The trainer property.
     */
    public static SimpleObjectProperty<Trainer> trainerProperty() {
        return trainer;
    }

    /**
     * Returns the comment property.
     *
     * @return The comment property.
     */
    public static SimpleStringProperty commentProperty() {
        return comment;
    }
}

