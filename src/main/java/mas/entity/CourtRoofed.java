package mas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtRoofed extends Court {

    public enum RoofType { Hall, Balloon }

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
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedPricePerHour from StaticStorage ss", BigDecimal.class).getSingleResult();
//        return pricePerHour;
    }

    public static BigDecimal getHeatingSurcharge() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSurcharge from StaticStorage ss", BigDecimal.class).getSingleResult();
//        return heatingSurcharge;
    }

    public static LocalDate getHeatingSeasonStart() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
//        return heatingSeasonStart;
    }

    public static LocalDate getHeatingSeasonEnd() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
//        return heatingSeasonEnd;
    }

}
