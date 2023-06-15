package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CyclicalTraining extends Training {

    @ElementCollection
    @Column(nullable = false)
    private List<LocalDateTime> cyclicStarts;

    @ElementCollection
    @Column(nullable = false)
    private List<Duration> cyclicDurations;

    public CyclicalTraining(List<LocalDateTime> cyclicStarts, List<Duration> cyclicDurations) {
        this.cyclicStarts = cyclicStarts;
        this.cyclicDurations = cyclicDurations;
    }

    public BigDecimal getTotalPricePerParticipant() {
        // total price per participant = (price per participant per training) * (nr of trainings)
        // out of project scope
        throw new UnsupportedOperationException("Not implemented.");
    }

}
