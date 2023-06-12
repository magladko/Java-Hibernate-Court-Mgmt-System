package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;
import mas.util.StaticallyStored;
import mas.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Court {

    public enum SurfaceType { Grass, Clay, Hard, ArtificialGrass }

    private static LocalTime openingHour;
    private static LocalTime closingHour;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer number;

    @Column(nullable = false)
    private SurfaceType surfaceType;

    public Court(Integer number, SurfaceType surfaceType) {
        this.number = number;
        this.surfaceType = surfaceType;
    }

    @OneToMany(mappedBy = "court")
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "court")
    private Set<Training> trainings = new HashSet<>();

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream()
                .noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                getTrainings().stream()
                        .noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    public static LocalTime getOpeningHour() {
        openingHour = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtOpeningHour from StaticStorage ss", LocalTime.class).getSingleResult();
        return openingHour;
    }

    public static LocalTime getClosingHour() {
        closingHour = DBController.INSTANCE.getEm()
                .createQuery("select ss.courtClosingHour from StaticStorage ss", LocalTime.class).getSingleResult();
        return closingHour;
    }

}
