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

@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    private String notes;

    @ManyToMany(mappedBy = "equipmentSet")
    private Set<Training> trainings = new HashSet<>();

    public void addTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (!this.getTrainings().contains(t)) {
                this.getTrainings().add(t);
                t.addEquipment(this);
            }
        }
    }

    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (this.getTrainings().contains(t)) {
                this.getTrainings().remove(t);
                t.removeEquipment(this);
            }
        }
    }

    public Equipment(String notes) {
        this.notes = notes;
    }

    public Equipment[] getAvailable(LocalDateTime time, Duration duration) {
        throw new UnsupportedOperationException("Not implemented, out of project scope.");
//        return new Equipment[]{};
    }

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getTrainings().stream().noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    public void makeReservation(Training training) {
        // out of project scope
        throw new UnsupportedOperationException("Not implemented.");
    }
}
