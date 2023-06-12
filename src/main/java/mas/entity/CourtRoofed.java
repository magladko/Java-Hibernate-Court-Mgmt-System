package mas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import mas.util.DBController;
import mas.util.StaticallyStored;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtRoofed extends Court {

    public enum RoofType { Hall, Balloon }

    private static BigDecimal pricePerHour;
    private static BigDecimal heatingSurcharge;
    private static LocalDate heatingSeasonStart;
    private static LocalDate heatingSeasonEnd;

    @Enumerated
    private RoofType roofType;

    public CourtRoofed(Integer number, SurfaceType surfaceType, RoofType roofType) {
        super(number, surfaceType);
        this.roofType = roofType;
    }

    @Override
    public String toString() {
        return "CourtRoofed{" +
                "number=" + getNumber() +
                ", surfaceType=" + getSurfaceType() +
                ", roofType=" + roofType +
                "}";
    }

    public static BigDecimal getPricePerHour() {
        pricePerHour = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedPricePerHour from StaticStorage ss", BigDecimal.class).getSingleResult();
        return pricePerHour;
    }

    public static BigDecimal getHeatingSurcharge() {
        heatingSurcharge = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSurcharge from StaticStorage ss", BigDecimal.class).getSingleResult();
        return heatingSurcharge;
    }

    public static LocalDate getHeatingSeasonStart() {
        heatingSeasonStart = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
        return heatingSeasonStart;
    }

    public static LocalDate getHeatingSeasonEnd() {
        heatingSeasonEnd = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
        return heatingSeasonEnd;
    }

}
