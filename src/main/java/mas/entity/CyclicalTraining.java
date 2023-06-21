package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a cyclical training in a tennis court management application.
 * It extends the base Training class and adds properties related to cyclical trainings.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CyclicalTraining extends Training {

    /**
     * The list of start times for each training.
     */
    @ElementCollection
    @Column(nullable = false)
    private List<LocalDateTime> cyclicStarts;

    /**
     * The list of durations for each training.
     */
    @ElementCollection
    @Column(nullable = false)
    private List<Duration> cyclicDurations;

    /**
     * Constructs a CyclicalTraining with the given cyclic starts and durations.
     * @param cyclicStarts the list of start times for each cycle
     * @param cyclicDurations the list of durations for each cycle
     */
    public CyclicalTraining(List<LocalDateTime> cyclicStarts, List<Duration> cyclicDurations) {
        this.cyclicStarts = cyclicStarts;
        this.cyclicDurations = cyclicDurations;
    }

    /**
     * Returns the total price per participant for the cyclical training.
     * The total price per participant is calculated based on the price per participant per training
     * and the number of trainings in the cycle.
     * @return the total price per participant for the cyclical training
     * @throws UnsupportedOperationException because it is out of the project scope
     */
    public BigDecimal getTotalPricePerParticipant() {
        throw new UnsupportedOperationException("Not implemented.");
    }

}
