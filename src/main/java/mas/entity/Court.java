package mas.entity;

import jakarta.persistence.*;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TableColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;
import mas.util.Util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.IntStream;

@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Court {

    public enum SurfaceType {
        Grass("Trawa"), Clay("Glina"), Hard("Beton"), ArtificialGrass("Sztuczna trawa");

        private final String surfaceName;
        SurfaceType(String surfaceName) {
            this.surfaceName = surfaceName;
        }

        @Override
        public String toString() {
            return surfaceName;
        }
    }

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

    public void addReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (!getReservations().contains(r)) {
                this.getReservations().add(r);
                r.setCourt(this);
            }
        }
    }

    public void removeReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            this.getReservations().remove(r);
            if (r.getCourt() == this) r.setCourt(null);
        }
    }

    @OneToMany(mappedBy = "court")
    private Set<Training> trainings = new HashSet<>();

    public void addTrainings(Training... trainings) {
        for (Training t : trainings) {
            this.getTrainings().add(t);
            if (t.getCourt() == null || t.getCourt() != this) t.setCourt(this);
        }
    }

    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            this.getTrainings().remove(t);
            if (t.getCourt() == this) t.setCourt(null);
        }
    }

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream()
                .noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                getTrainings().stream()
                        .noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    public boolean anyAvailable(LocalDate date) {
        Duration d = Duration.between(getOpeningHour(), getClosingHour());
        return IntStream.of((int)d.toHours()).anyMatch(h -> isAvailable(date.atTime(h, 0), Duration.ofHours(1)));
    }

    public static LocalTime getOpeningHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtOpeningHour from StaticStorage ss", LocalTime.class).getSingleResult();
//        return openingHour;
    }

    public static LocalTime getClosingHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtClosingHour from StaticStorage ss", LocalTime.class).getSingleResult();
//        return closingHour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Court court)) return false;

        return getNumber().equals(court.getNumber());
    }

    public abstract String getInfoTxt();

    @Override
    public int hashCode() {
        return getNumber().hashCode();
    }

    @Transient
    @Getter
    private LinkedHashMap<TableColumn<Court, Boolean>, BooleanProperty> markedHours = new LinkedHashMap<>();

}
