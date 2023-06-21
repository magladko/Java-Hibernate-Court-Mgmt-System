package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents equipment in a tennis court management application.
 * It is an abstract class providing common properties and methods for equipment.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Equipment {

    /**
     * The unique identifier of the equipment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /**
     * Additional notes or description about the equipment.
     */
    private String notes;

    /**
     * The set of trainings associated with this equipment.
     */
    @ManyToMany(mappedBy = "equipmentSet")
    private Set<Training> trainings = new HashSet<>();

    /**
     * Adds the given trainings to the equipment's associated trainings.
     * If a training is already associated with the equipment, it will not be added again.
     * @param trainings the trainings to be added
     */
    public void addTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (!this.getTrainings().contains(t)) {
                this.getTrainings().add(t);
                t.addEquipment(this);
            }
        }
    }

    /**
     * Removes the given trainings from the equipment's associated trainings.
     * If a training is not associated with the equipment, it will be ignored.
     * @param trainings the trainings to be removed
     */
    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (this.getTrainings().contains(t)) {
                this.getTrainings().remove(t);
                t.removeEquipment(this);
            }
        }
    }

    /**
     * Constructs an instance of Equipment class with the given notes.
     * @param notes the additional notes or description about the equipment
     */
    public Equipment(String notes) {
        this.notes = notes;
    }

    /**
     * Returns an array of available equipment instances for the specified time and duration.
     * This method is not implemented and is out of the project scope.
     * @param time the starting time of the availability check
     * @param duration the duration of the availability check
     * @return an array of available equipment instances
     * @throws UnsupportedOperationException because it is out of the project scope
     */
    public Equipment[] getAvailable(LocalDateTime time, Duration duration) {
        throw new UnsupportedOperationException("Not implemented, out of project scope.");
    }

    /**
     * Checks if the equipment is available during the specified time and duration.
     * @param from the starting time of the availability check
     * @param duration the duration of the availability check
     * @return true if the equipment is available, false otherwise
     */
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getTrainings().stream().noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    /**
     * Makes a reservation for the equipment within the context of a training.
     * This method is not implemented and is out of the project scope.
     * @param training the training for which the reservation is made
     * @throws UnsupportedOperationException because it is out of the project scope
     */
    public void makeReservation(Training training) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
