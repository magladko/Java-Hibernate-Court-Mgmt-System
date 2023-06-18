package mas.util;

import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mas.entity.Court;
import mas.entity.Person;
import mas.entity.Racket;
import mas.entity.Trainer;

import java.time.Duration;
import java.time.LocalDateTime;

@NoArgsConstructor
public class SessionData {

    /*@Getter */private static final SimpleObjectProperty<Court> court = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<LocalDateTime> reservationStart = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Duration> reservationDuration = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Person> participant = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Person> client = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Racket> racket = new SimpleObjectProperty<>();
    /*@Getter */private static final SimpleObjectProperty<Trainer> trainer = new SimpleObjectProperty<>();

    public static void cancel() {
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
}
