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

    public Equipment(String notes) {
        this.notes = notes;
    }

    public Equipment[] getAvailable(LocalDateTime time, Duration duration) {
        // TODO: get available equipment
        throw new UnsupportedOperationException("Not implemented.");
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
