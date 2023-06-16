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
public class ReservationData {

    @Getter private static final SimpleObjectProperty<Court> court = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<Duration> duration = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<Person> participant = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<Person> client = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<Racket> racket = new SimpleObjectProperty<>();
    @Getter private static final SimpleObjectProperty<Trainer> trainer = new SimpleObjectProperty<>();

    public static void cancel() {
        court.unbind();
        start.unbind();
        duration.unbind();
        participant.unbind();
        client.unbind();
        racket.unbind();
        trainer.unbind();

        court.set(null);
        start.set(null);
        duration.set(null);
        participant.set(null);
        client.set(null);
        racket.set(null);
        trainer.set(null);
    }

}
