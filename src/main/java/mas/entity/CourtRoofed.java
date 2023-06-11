package mas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import mas.util.StaticallyStored;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtRoofed extends Court {

    public enum RoofType { Hall, Balloon }

    @StaticallyStored private static BigDecimal pricePerHour;
    @StaticallyStored private static BigDecimal heatingSurcharge;
    @StaticallyStored private static BigDecimal heatingSeasonStart;
    @StaticallyStored private static BigDecimal heatingSeasonEnd;

    @Enumerated
    @NonNull
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
}
