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

/**
 * Represents a training session in the Tennis Courts management application.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Training {

    /**
     * The unique identifier of the training.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /**
     * The start time of the training.
     */
    @Column(nullable = false)
    private LocalDateTime start;

    /**
     * The duration of the training.
     */
    @Column(nullable = false)
    private Duration duration;

    /**
     * Indicates whether the training session is paid.
     */
    @Column(nullable = false)
    private Boolean isPaid;

    /**
     * The trainer assigned to the training session.
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Trainer trainer;

    /**
     * Sets the trainer for the training session.
     *
     * @param trainer The trainer to be set
     */
    public void setTrainer(Trainer trainer) {
        if (getTrainer() != null && getTrainer().equals(trainer)) {
            return;
        }
        if (this.trainer != null) {
            this.trainer.getTrainings().remove(this);
        }
        this.trainer = trainer;
        getTrainer().addTrainings(this);
    }

    /**
     * The court assigned to the training session.
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Court court;

    /**
     * Sets the court for the training session.
     *
     * @param court The court to be set
     */
    public void setCourt(Court court) {
        if (getCourt() != null && getCourt().equals(court)) {
            return;
        }
        if (getCourt() != null) {
            this.getCourt().removeTrainings(this);
        }
        this.court = court;
        if (getCourt() != null) {
            getCourt().addTrainings(this);
        }
    }

    /**
     * The equipment used in the training session.
     */
    @ManyToMany
    private Set<Equipment> equipmentSet = new HashSet<>();

    /**
     * Adds equipment to the training session.
     *
     * @param equipment The equipment to be added
     */
    public void addEquipment(Equipment... equipment) {
        for (Equipment e : equipment) {
            if (!this.getEquipmentSet().contains(e)) {
                this.getEquipmentSet().add(e);
                e.addTrainings(this);
            }
        }
    }

    /**
     * Removes equipment from the training session.
     *
     * @param equipment The equipment to be removed
     */
    public void removeEquipment(Equipment... equipment) {
        for (Equipment e : equipment) {
            if (this.getEquipmentSet().contains(e)) {
                this.getEquipmentSet().remove(e);
                e.removeTrainings(this);
            }
        }
    }

    /**
     * The clients participating in the training session.
     */
    @ManyToMany
    @JoinTable(name = "trainings_clients")
    private Set<Person> clients = new HashSet<>();

    /**
     * Adds clients to the training session.
     *
     * @param clients The clients to be added
     */
    public void addClients(Person... clients) {
        for (Person c : clients) {
            if (!c.getPersonTypes().contains(Person.PersonType.CLIENT)) {
                throw new TypeMismatchException("Person referred as client is not a Client instance");
            }
            if (!this.getClients().contains(c)) {
                this.getClients().add(c);
                c.addTrainingsBought(this);
            }
        }
    }

    /**
     * Removes clients from the training session.
     *
     * @param clients The clients to be removed
     */
    public void removeClients(Person... clients) {
        for (Person c : clients) {
            if (this.getClients().contains(c)) {
                this.getClients().remove(c);
                c.removeTrainingsBought(this);
            }
        }
    }

    /**
     * The participants in the training session.
     */
    @ManyToMany
    @JoinTable(name = "trainings_participants")
    private Set<Person> participants = new HashSet<>();

    /**
     * Adds participants to the training session.
     *
     * @param participants The participants to be added
     */
    public void addParticipants(Person... participants) {
        for (Person p : participants) {
            if (!p.getPersonTypes().contains(Person.PersonType.PARTICIPANT)) {
                throw new TypeMismatchException("Person referred as participant is not a Participant instance");
            }
            if (!this.getParticipants().contains(p)) {
                this.getParticipants().add(p);
                p.addTrainings(this);
            }
        }
    }

    /**
     * Removes participants from the training session.
     *
     * @param participants The participants to be removed
     */
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

    /**
     * Creates a new training session.
     *
     * @param client     The client participating in the training
     * @param participant The participant in the training
     * @param trainer    The trainer assigned to the training
     * @param court      The court assigned to the training
     * @param from       The start time of the training
     * @param duration   The duration of the training
     * @return The newly created training session
     * @throws TimeUnavailableException   If the trainer or court is not available at the specified time
     * @throws TypeMismatchException      If the client or participant is not of the appropriate type
     */
    public static Training makeReservation(Person client, Person participant, Trainer trainer, Court court,
                                           LocalDateTime from, Duration duration) {
        if (!trainer.isAvailable(from, duration)) {
            throw new TimeUnavailableException(trainer, from, duration);
        }
        if (!court.isAvailable(from, duration)) {
            throw new TimeUnavailableException(court, from, duration);
        }
        if (!client.getPersonTypes().contains(Person.PersonType.CLIENT)) {
            throw new TypeMismatchException("Person referred as client is not a Client instance");
        }
        if (!participant.getPersonTypes().contains(Person.PersonType.PARTICIPANT)) {
            throw new TypeMismatchException("Person referred as participant is not a Participant instance");
        }

        return new Training(from, duration, trainer, court, List.of(client), List.of(participant), List.of());
    }

    /**
     * Marks the training session as paid.
     * This method is out of scope for this project and is not implemented.
     *
     * @throws UnsupportedOperationException This operation is not supported
     */
    public void pay() {
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    /**
     * Retrieves the list of assigned trainings for a given trainer.
     * This method is out of scope for this project and is not implemented.
     *
     * @param trainer The trainer to get the assigned trainings for
     * @return The list of assigned trainings
     * @throws UnsupportedOperationException This operation is not supported
     */
    public static List<Training> getAssigned(Trainer trainer) {
        throw new UnsupportedOperationException("Not implemented (out of scope)");
    }

    /**
     * Retrieves the price per participant per training.
     * This method is out of scope for this project and is not implemented.
     *
     * @return The price per participant per training
     * @throws UnsupportedOperationException This operation is not supported
     */
    public BigDecimal getPricePerParticipantPerTraining() {
        throw new UnsupportedOperationException("out of project's scope");
    }

    /**
     * Returns the string representation of the Training object.
     *
     * @return The string representation of the Training object
     */
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

    /**
     * Checks if this Training object is equal to another object.
     *
     * @param o The object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Training training)) {
            return false;
        }
        if (!getStart().equals(training.getStart())) {
            return false;
        }
        if (!getDuration().equals(training.getDuration())) {
            return false;
        }
        if (!getTrainer().equals(training.getTrainer())) {
            return false;
        }
        return getCourt().equals(training.getCourt());
    }

    /**
     * Returns the hash code value for the Training object.
     *
     * @return The hash code value for the Training object
     */
    @Override
    public int hashCode() {
        int result = getStart().hashCode();
        result = 31 * result + getDuration().hashCode();
        result = 31 * result + getCourt().hashCode();
        return result;
    }
}
