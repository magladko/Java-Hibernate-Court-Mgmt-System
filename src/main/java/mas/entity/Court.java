package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public abstract class Court {

    public enum SurfaceType { Grass, Clay, Hard, ArtificialGrass }

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
    private Set<Reservation> reservations;

    @OneToMany(mappedBy = "court")
    private Set<Training> trainings;

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        return getReservations().stream()
                .noneMatch(r -> Util.isOverlapping(from, duration, r.getStart(), r.getDuration())) &&
                getTrainings().stream()
                        .noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

}
