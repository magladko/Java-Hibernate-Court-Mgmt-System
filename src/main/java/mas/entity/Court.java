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

/**
 * Represents an abstract class for a tennis court.
 * It provides functionality to manage reservations and trainings on the court.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Court {

    /**
     * Enumeration of the possible surface types of the court.
     */
    public enum SurfaceType {
        Grass("Trawa"),
        Clay("Glina"),
        Hard("Beton"),
        ArtificialGrass("Sztuczna trawa");

        private final String surfaceName;

        /**
         * Constructs a SurfaceType with the given surface name.
         * @param surfaceName the name of the surface type
         */
        SurfaceType(String surfaceName) {
            this.surfaceName = surfaceName;
        }

        /**
         * Returns the string representation of the surface type.
         * @return the string representation of the surface type
         */
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

    /**
     * Constructs a Court with the given number and surface type.
     * @param number the court number
     * @param surfaceType the surface type of the court
     */
    public Court(Integer number, SurfaceType surfaceType) {
        this.number = number;
        this.surfaceType = surfaceType;
    }

    @OneToMany(mappedBy = "court")
    private Set<Reservation> reservations = new HashSet<>();

    /**
     * Adds the given reservations to the court.
     * @param reservations the reservations to add
     */
    public void addReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (!getReservations().contains(r)) {
                this.getReservations().add(r);
                r.setCourt(this);
            }
        }
    }

    /**
     * Removes the given reservations from the court.
     * @param reservations the reservations to remove
     */
    public void removeReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            this.getReservations().remove(r);
            if (r.getCourt() == this) r.setCourt(null);
        }
    }

    @OneToMany(mappedBy = "court")
    private Set<Training> trainings = new HashSet<>();

    /**
     * Adds the given trainings to the court.
     * @param trainings the trainings to add
     */
    public void addTrainings(Training... trainings) {
        for (Training t : trainings) {
            this.getTrainings().add(t);
            if (t.getCourt() == null || t.getCourt() != this) t.setCourt(this);
        }
    }

    /**
     * Removes the given trainings from the court.
     * @param trainings the trainings to remove
     */
    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            this.getTrainings().remove(t);
            if (t.getCourt() == this) t.setCourt(null);
        }
    }

    /**
     * Checks if the court is available for the given time period.
     * The court is considered available if there are no overlapping reservations or trainings.
     * @param from the start time of the period
     * @param duration the duration of the period
     * @return true if the court is available, false otherwise
     */
    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream()
                .noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                getTrainings().stream()
                        .noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    /**
     * Checks if there is any available time slot on the court for the given date.
     * @param date the date to check
     * @return true if there is an available time slot, false otherwise
     */
    public boolean anyAvailable(LocalDate date) {
        Duration d = Duration.between(getOpeningHour(), getClosingHour());
        return IntStream.of((int) d.toHours()).anyMatch(h -> isAvailable(date.atTime(h, 0), Duration.ofHours(1)));
    }

    /**
     * Returns the opening hour of the court from the database.
     * @return the opening hour of the court
     */
    public static LocalTime getOpeningHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtOpeningHour from StaticStorage ss", LocalTime.class).getSingleResult();
    }

    /**
     * Returns the closing hour of the court from the database.
     * @return the closing hour of the court
     */
    public static LocalTime getClosingHour() {
        return DBController.INSTANCE.getEm()
                .createQuery("select ss.courtClosingHour from StaticStorage ss", LocalTime.class).getSingleResult();
    }

    /**
     * Checks if the court is equal to the given object.
     * Courts are considered equal if their numbers are equal.
     * @param o the object to compare
     * @return true if the court is equal to the object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Court court)) return false;

        return getNumber().equals(court.getNumber());
    }

    /**
     * Returns the hash code of the court.
     * @return the hash code of the court
     */
    @Override
    public int hashCode() {
        return getNumber().hashCode();
    }

    /**
     * Returns the information text for the court.
     * This method needs to be implemented by subclasses.
     * @return the information text for the court
     */
    public abstract String getInfoTxt();

    /**
     * Represents a mapping of TableColumn to BooleanProperty for marking hours in a TableView.
     * This field is used to persist the marked hours when making a reservation.
     */
    @Transient
    @Getter
    private LinkedHashMap<TableColumn<Court, Boolean>, BooleanProperty> markedHours = new LinkedHashMap<>();

}
