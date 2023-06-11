package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.StaticallyStored;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtUnroofed extends Court {

    @StaticallyStored private static BigDecimal pricePerHour;
    @StaticallyStored private static LocalDateTime seasonStart;
    @StaticallyStored private static LocalDateTime seasonEnd;

    public CourtUnroofed(Integer number, SurfaceType surfaceType) {
        super(number, surfaceType);
    }

    @Override
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return !from.isAfter(seasonStart) || !from.isBefore(seasonEnd) || super.isAvailable(from, duration);
    }

}
