package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtUnroofed extends Court {

    public CourtUnroofed(Integer number, SurfaceType surfaceType) {
        super(number, surfaceType);
    }

    @Override
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return from.isAfter(getSeasonStart().atStartOfDay()) &&
                from.isBefore(getSeasonEnd().atStartOfDay()) &&
                super.isAvailable(from, duration);
    }

    @Override
    public String toString() {
        return "CourtUnroofed{" +
                "number=" + getNumber() +
                ", surfaceType=" + getSurfaceType() +
                '}';
    }

    public static BigDecimal getPricePerHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedPricePerHour from StaticStorage ss", BigDecimal.class).getSingleResult();
    }

    public static LocalDate getSeasonStart() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    public static LocalDate getSeasonEnd() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtUnroofedSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
    }
}
