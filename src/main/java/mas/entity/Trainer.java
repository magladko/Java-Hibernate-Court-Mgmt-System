package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.Util;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Trainer {

    public enum TrainerQualification {
        Animator("Animator"), Instructor("Instruktor"), TrainerCoach("Trener Coach");

        private final String polishName;

        TrainerQualification(String polishName) {
            this.polishName = polishName;
        }

        @Override
        public String toString() {
            return polishName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String phoneNr;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated
    @Column(nullable = false)
    private TrainerQualification qualification;
    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @ElementCollection
    private Map<DayOfWeek, WorkingHours> workingHours = new HashMap<>();

    @OneToMany(mappedBy = "trainer")
    private Set<Training> trainings = new HashSet<>();

    public Trainer(String name, String surname, String phoneNr, String email,
                   TrainerQualification qualification, BigDecimal pricePerHour,
                   Map<DayOfWeek, WorkingHours> workingHours) {
        this.name = name;
        this.surname = surname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.qualification = qualification;
        this.pricePerHour = pricePerHour;
        this.workingHours.putAll(workingHours);
    }

    public boolean isAvailable(LocalDateTime from, Duration duration) {
        if (!workingHours.containsKey(from.getDayOfWeek())) return false;
        WorkingHours workingHours = getWorkingHours().get(from.getDayOfWeek());
        if (!workingHours.getStartTime().atDate(from.toLocalDate()).isBefore(from) &&
                workingHours.getEndTime().atDate(from.toLocalDate()).isAfter(from.plus(duration))) return false;

        return getTrainings().stream().noneMatch(t -> Util.isOverlapping(from, duration, t.getStart(), t.getDuration()));
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNr='" + phoneNr + '\'' +
                ", email='" + email + '\'' +
                ", qualification=" + qualification +
                ", pricePerHour=" + pricePerHour +
                ", workingHours=" + workingHours +
                '}';
    }
}
