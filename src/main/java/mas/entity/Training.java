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

    public void setTrainer(Trainer trainer) {
        if (getTrainer() != null && getTrainer().equals(trainer)) return;
        if (this.trainer != null) this.trainer.getTrainings().remove(this);
        this.trainer = trainer;
        getTrainer().addTrainings(this);
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Court court;

    public void setCourt(Court court) {
        if (getCourt() != null && getCourt().equals(court)) return;
        if (getCourt() != null) this.getCourt().removeTrainings(this);
        this.court = court;
        if (getCourt() != null) getCourt().addTrainings(this);
    }

    @ManyToMany
    private Set<Equipment> equipmentSet = new HashSet<>();

    public void addEquipment(Equipment... equipment) {
        for (Equipment e : equipment) {
            if (!this.getEquipmentSet().contains(e)) {
                this.getEquipmentSet().add(e);
                e.addTrainings(this);
            }
        }
    }

    public void removeEquipment(Equipment... equipment) {
        for (Equipment e : equipment) {
            if (this.getEquipmentSet().contains(e)) {
                this.getEquipmentSet().remove(e);
                e.removeTrainings(this);
            }
        }
    }

    @ManyToMany
    @JoinTable(name = "trainings_clients")
    private Set<Person> clients = new HashSet<>();

    public void addClients(Person... clients) {
        for (Person c : clients) {
            if (!c.getPersonTypes().contains(Person.PersonType.Client))
                throw new TypeMismatchException("Person referred as client is not a Client instance");

            if (!this.getClients().contains(c)) {
                this.getClients().add(c);
                c.addTrainingsBought(this);
            }
        }
    }

    public void removeClients(Person... clients) {
        for (Person c : clients) {
            if (this.getClients().contains(c)) {
                this.getClients().remove(c);
                c.removeTrainingsBought(this);
            }
        }
    }

    @ManyToMany
    @JoinTable(name = "trainings_participants")
    private Set<Person> participants = new HashSet<>();

    public void addParticipants(Person... participants) {
        for (Person p : participants) {
            if (!p.getPersonTypes().contains(Person.PersonType.Participant))
                throw new TypeMismatchException("Person referred as participant is not a Participant instance");

            if (!this.getParticipants().contains(p)) {
                this.getParticipants().add(p);
                p.addTrainings(this);
            }
        }
    }

    public void removeParticipants(Person... participants) {
        for (Person p : participants) {
            if (this.getParticipants().contains(p)) {
                this.getParticipants().remove(p);
                p.removeTrainings(this);
            }
        }
    }

    private Training(LocalDateTime start, Duration duration, Trainer trainer, Court court,
                     List<Person> clients, List<Person> participants,
                     List<Equipment> equipmentList) {
        this.start = start;
        this.duration = duration;
        setCourt(court);
        // -- end needed non-null for hashcode
        setTrainer(trainer);
        this.isPaid = false;
        addClients(clients.toArray(Person[]::new));
        addParticipants(participants.toArray(Person[]::new));
        addEquipment(equipmentList.toArray(Equipment[]::new));
    }

    public static Training makeReservation(Person client, Person participant, Trainer trainer, Court court,
                                           LocalDateTime from, Duration duration) {
        if (!trainer.isAvailable(from, duration)) throw new TimeUnavailableException(trainer, from, duration);
        if (!court.isAvailable(from, duration)) throw new TimeUnavailableException(court, from, duration);
        if (!client.getPersonTypes().contains(Person.PersonType.Client))
            throw new TypeMismatchException("Person referred as client is not a Client instance");
        if (!participant.getPersonTypes().contains(Person.PersonType.Participant))
            throw new TypeMismatchException("Person referred as participant is not a Participant instance");

        return new Training(from, duration, trainer, court, List.of(client), List.of(participant), List.of());
    }

    public void pay() {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    public static List<Training> getAssigned(Trainer trainer) {
        // out of scope
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    public BigDecimal getPricePerParticipantPerTraining() {
        throw new UnsupportedOperationException("out of project's scope");
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
        result = 31 * result + getCourt().hashCode();
        return result;
    }
}
