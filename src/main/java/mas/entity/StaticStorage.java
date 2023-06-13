package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.StaticallyStored;
import org.hibernate.Session;
import org.jboss.errai.reflections.Reflections;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StaticStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true)
    private LocalTime courtOpeningHour;
    @Column(unique = true)
    private LocalTime courtClosingHour;

    @Column(unique = true)
    private BigDecimal courtRoofedPricePerHour;
    @Column(unique = true)
    private BigDecimal courtRoofedHeatingSurcharge;
    @Column(unique = true)
    private LocalDate courtRoofedHeatingSeasonStart;
    @Column(unique = true)
    private LocalDate courtRoofedHeatingSeasonEnd;

    @Column(unique = true)
    private BigDecimal courtUnroofedPricePerHour;
    @Column(unique = true)
    private LocalDate courtUnroofedSeasonStart;
    @Column(unique = true)
    private LocalDate courtUnroofedSeasonEnd;

    public static void loadStaticValuesFromDB(Session session) {
        // TODO: load from DB
        throw new UnsupportedOperationException("Not implemented.");
//        session.createQuery("from StaticStorage", StaticStorage.class).getSingleResult().loadStaticValues();
    }
}
