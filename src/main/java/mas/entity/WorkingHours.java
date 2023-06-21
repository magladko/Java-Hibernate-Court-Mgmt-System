package mas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Represents the working hours of a tennis court in a tennis court management application.
 * It is used as an embeddable entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class WorkingHours {
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * Constructs a WorkingHours object with the specified start time and end time.
     *
     * @param startTime the start time of the working hours
     * @param endTime   the end time of the working hours
     */
    public WorkingHours(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns a string representation of the WorkingHours object.
     *
     * @return a string representation of the WorkingHours object
     */
    @Override
    public String toString() {
        return "WorkingHours{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}