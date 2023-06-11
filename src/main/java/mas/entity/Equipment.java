package mas.entity;

import jakarta.persistence.*;
import jdk.tools.jlink.internal.plugins.StripNativeCommandsPlugin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

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

    public Equipment(String notes) {
        this.notes = notes;
    }

    public Equipment[] getAvailable(LocalDateTime time, Duration duration) {
        // TODO: get available equipment
        throw new UnsupportedOperationException("Not implemented.");
//        return new Equipment[]{};
    }

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        // TODO: check if equipment is available
        throw new UnsupportedOperationException("Not implemented.");
//        return getTrainings().stream().noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

//    public void makeReservation(Training training) {
//        throw new UnsupportedOperationException("Not implemented.");
//    }

}
