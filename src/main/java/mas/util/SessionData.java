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
import java.util.Optional;

@NoArgsConstructor
public class SessionData {

    @Getter @Setter private static volatile Scene courtReservationScene = null;
    @Getter @Setter private static volatile Scene summaryScene = null;

    /*@Getter */private static final SimpleObjectProperty<Court> court = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<LocalDateTime> reservationStart = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Duration> reservationDuration = new SimpleObjectProperty<>();
    // TODO: Initialize with client
    /*@Getter */private static final SimpleObjectProperty<Person> client = new SimpleObjectProperty<>();
//    = new SimpleObjectProperty<>(
//            DBController.INSTANCE.getEm().createQuery("FROM Person", Person.class)
//                    .getResultStream().filter(p -> p.getPersonTypes().contains(Person.PersonType.Client))
//                    .findAny().orElseThrow());
    /*@Getter */private static final SimpleObjectProperty<Person> participant = new SimpleObjectProperty<>();
//        new SimpleObjectProperty<>(clientProperty().get()); // TODO: default to client
    /*@Getter */private static final SimpleObjectProperty<Racket> racket = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Trainer> trainer = new SimpleObjectProperty<>();
    private static final SimpleStringProperty comment = new SimpleStringProperty();

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

    public static Optional<BigDecimal> getTotalPrice() {
        if (reservationStart.getValue() == null ||
                reservationDuration.getValue() == null) return Optional.empty();

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal duration = BigDecimal.valueOf(reservationDuration.getValue().toHours());

        if (court.getValue() != null) {
            if (court.getValue() instanceof CourtRoofed)
                totalPrice = totalPrice.add(CourtRoofed.getPricePerHour().multiply(duration))
                                       .add(CourtRoofed.getHeatingSurcharge(reservationStart.getValue().toLocalDate()));
            else if (court.getValue() instanceof CourtUnroofed)
                totalPrice = totalPrice.add(CourtUnroofed.getPricePerHour().multiply(duration));
        }

        if (racket.getValue() != null) {
            totalPrice = totalPrice.add(racketProperty().getValue().getPricePerHour().multiply(duration));
        }

        if (trainer.getValue() != null) {
            totalPrice = totalPrice.add(trainerProperty().getValue().getPricePerHour().multiply(duration));
        }

        return Optional.of(totalPrice);
    }

    public static SimpleObjectProperty<Court> courtProperty() {
        return court;
    }

    public static SimpleObjectProperty<LocalDateTime> reservationStartProperty() {
        return reservationStart;
    }

    public static SimpleObjectProperty<Duration> reservationDurationProperty() {
        return reservationDuration;
    }

    public static SimpleObjectProperty<Person> participantProperty() {
        return participant;
    }

    public static SimpleObjectProperty<Person> clientProperty() {
        return client;
    }

    public static SimpleObjectProperty<Racket> racketProperty() {
        return racket;
    }

    public static SimpleObjectProperty<Trainer> trainerProperty() {
        return trainer;
    }

    public static SimpleStringProperty commentProperty() {
        return comment;
    }
}
