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

//    public void loadStaticValues() {
//        fillStaticFields();
////        fillStaticFields(CourtRoofed.class);
////        fillStaticFields(CourtUnroofed.class);
//    }

//    private void fillStaticFields() {
//        Reflections reflections = new Reflections("mas.entity");
//        Set<Field> fields = reflections.getFieldsAnnotatedWith(StaticallyStored.class);
//
//        (new Reflections("mas.entity")).getFieldsAnnotatedWith(StaticallyStored.class).forEach(f -> {
//            try {
//                f.setAccessible(true);
//                char[] clazz = CourtRoofed.class.getSimpleName().toCharArray();
//                clazz[0] = Character.toLowerCase(clazz[0]);
//                char[] filed = f.getName().toCharArray();
//                filed[0] = Character.toUpperCase(clazz[0]);
//                f.set(null, this.getClass().getField(new String(clazz) + new String(filed)));
//            } catch (IllegalAccessException | NoSuchFieldException e) {
//                throw new RuntimeException(e);
//            } finally {
//                f.setAccessible(false);
//            }
//        });
//
////        Arrays.stream(cls.getDeclaredFields()).filter(f -> f.isAnnotationPresent(StaticallyStored.class))
////                .forEach(f -> {
////                    if (f.isAnnotationPresent(StaticallyStored.class)) {
////                        try {
////                            f.setAccessible(true);
////                            char[] clazz = CourtRoofed.class.getSimpleName().toCharArray();
////                            clazz[0] = Character.toLowerCase(clazz[0]);
////                            char[] filed = f.getName().toCharArray();
////                            filed[0] = Character.toUpperCase(clazz[0]);
////                            f.set(null, this.getClass().getField(new String(clazz) + new String(filed)));
////                        } catch (IllegalAccessException | NoSuchFieldException e) {
////                            throw new RuntimeException(e);
////                        } finally {
////                            f.setAccessible(false);
////                        }
////                    }
////                });
//    }
}
