package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.TimeUnavailableException;
import org.hibernate.TypeMismatchException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Training {

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
    private Trainer trainer;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Court court;

    @ManyToMany
    private Set<Equipment> equipmentSet = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "trainings_clients")
    private Set<Person> clients = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "trainings_participants")
    private Set<Person> participants = new HashSet<>();

    private Training(LocalDateTime start, Duration duration, Trainer trainer, Court court,
                     List<Person> clients, List<Person> participants,
                     List<Equipment> equipmentList) {
        this.start = start;
        this.duration = duration;
        this.trainer = trainer;
        this.court = court;
        this.isPaid = false;
        getClients().addAll(clients);
        getParticipants().addAll(participants);

        if (equipmentList != null) {
            getEquipmentSet().addAll(equipmentList);
            equipmentList.forEach(e -> e.getTrainings().add(this));
        }

        trainer.getTrainings().add(this);
        court.getTrainings().add(this);
        clients.forEach(c -> c.getTrainingsBought().add(this));
        participants.forEach(p -> p.getTrainings().add(this));
    }

    public static Training makeReservation(Person client, Person participant, Trainer trainer, Court court,
                                           LocalDateTime from, Duration duration) {
        if (!trainer.isAvailable(from, duration)) throw new TimeUnavailableException(trainer, from, duration);
        if (!court.isAvailable(from, duration)) throw new TimeUnavailableException(court, from, duration);
        if (!client.getPersonTypes().contains(Person.PersonType.Client))
            throw new TypeMismatchException("Person referred as client is not a Client instance");
        if (!participant.getPersonTypes().contains(Person.PersonType.Participant))
            throw new TypeMismatchException("Person referred as participant is not a Participant instance");

        return new Training(from, duration, trainer, court, List.of(client), List.of(participant), null);
    }

    public void pay() {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    public static List<Training> getAssigned(Trainer trainer) {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    public static List<Training> bought(Person client) {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    public boolean reserveFromOffer(Person client, Person participant) {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    @Override
    public String toString() {
        return "Training{" +
                "start=" + getStart() +
                ", duration=" + getDuration() +
                ", trainer=" + getTrainer().getId() +
                ", court=" + getCourt().getNumber() +
                ", isPaid=" + getIsPaid() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Training training)) return false;

        if (!getStart().equals(training.getStart())) return false;
        if (!getDuration().equals(training.getDuration())) return false;
        if (!getTrainer().equals(training.getTrainer())) return false;
        return getCourt().equals(training.getCourt());
    }

    @Override
    public int hashCode() {
        int result = getStart().hashCode();
        result = 31 * result + getDuration().hashCode();
        result = 31 * result + getTrainer().hashCode();
        result = 31 * result + getCourt().hashCode();
        return result;
    }
}
