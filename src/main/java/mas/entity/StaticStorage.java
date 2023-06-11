package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.StaticallyStored;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StaticStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    private BigDecimal courtRoofedPricePerHour;
    private BigDecimal courtRoofedHeatingSurcharge;
    private LocalDate courtRoofedHeatingSeasonStart;
    private LocalDate courtRoofedHeatingSeasonEnd;

    private BigDecimal courtUnroofedPricePerHour;
    private LocalDate courtUnroofedSeasonStart;
    private LocalDate courtUnroofedSeasonEnd;

    public static void loadStaticValuesFromDB(Session session) {
        // TODO: load from DB
        throw new UnsupportedOperationException("Not implemented.");
//        session.createQuery("from StaticStorage", StaticStorage.class).getSingleResult().loadStaticValues();
    }

    public void loadStaticValues() {
        fillStaticFields(CourtRoofed.class);
        fillStaticFields(CourtUnroofed.class);
    }

    private void fillStaticFields(Class<?> cls) {
        Arrays.stream(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(StaticallyStored.class))
                .forEach(f -> {
                    if (f.isAnnotationPresent(StaticallyStored.class)) {
                        try {
                            f.setAccessible(true);
                            char[] clazz = CourtRoofed.class.getSimpleName().toCharArray();
                            clazz[0] = Character.toLowerCase(clazz[0]);
                            char[] filed = f.getName().toCharArray();
                            filed[0] = Character.toUpperCase(clazz[0]);
                            f.set(null, this.getClass().getField(new String(clazz) + new String(filed)));
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        } finally {
                            f.setAccessible(false);
                        }
                    }
                });
    }
}
