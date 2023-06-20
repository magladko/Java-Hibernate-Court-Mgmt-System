package mas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CourtRoofed extends Court {

    public enum RoofType {
        Hall("Hala"), Balloon("Balon");

        private final String name;
        RoofType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

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
    }

    public static BigDecimal getHeatingSurcharge() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSurcharge from StaticStorage ss", BigDecimal.class).getSingleResult();
    }

    public static LocalDate getHeatingSeasonStart() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonStart from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    public static LocalDate getHeatingSeasonEnd() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtRoofedHeatingSeasonEnd from StaticStorage ss", LocalDate.class).getSingleResult();
    }

    public static BigDecimal getHeatingSurcharge(LocalDate date) {
        // not in documentation
//        LocalDate date = LocalDate.now();
        if (date.isBefore(getHeatingSeasonStart()) || date.isAfter(getHeatingSeasonEnd())) {
            return BigDecimal.ZERO;
        } else {
            return getHeatingSurcharge();
        }
    }

    @Override
    public String getInfoTxt() {
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault());

        return "Zadaszenie: " + getRoofType() + "\n" +
                "Nawierzchnia: " + getSurfaceType() + "\n" +
                "Cena poza sezonem grzewczym: " + NumberFormat.getCurrencyInstance().format(getPricePerHour()) + "\n" +
                "Początek sezonu grzewczego: " +  getHeatingSeasonStart().format(df) + "\n" +
                "Koniec sezonu grzewczego: " + getHeatingSeasonEnd().format(df) + "\n" +
                "Dopłata grzewcza: " + NumberFormat.getCurrencyInstance().format(getHeatingSurcharge()) + "\n";
    }
}
