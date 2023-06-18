package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mas.util.DBController;
import mas.util.Util;

import java.math.BigDecimal;
import java.time.*;
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

    public void addTrainings(Training training) {
        if (!getTrainings().contains(training)) {
            getTrainings().add(training);
            training.setTrainer(this);
        }
    }

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

    // outside documentation
    public boolean isAvailable(LocalDate date) {
        if (workingHours.containsKey(date.getDayOfWeek())) {
            var workingHours = getWorkingHours().get(date.getDayOfWeek());
            var courtOpeningHour = DBController.INSTANCE.getStaticStorage().getCourtOpeningHour();
            var courtClosingHour = DBController.INSTANCE.getStaticStorage().getCourtClosingHour();

            var earliestPossible = workingHours.getStartTime().isAfter(courtOpeningHour) ? workingHours.getStartTime() : courtOpeningHour;
            var latestPossible = workingHours.getEndTime().isBefore(courtClosingHour) ? workingHours.getEndTime() : courtClosingHour;

            for (int h = earliestPossible.getHour(); h < latestPossible.getHour(); h++) {
                if (isAvailable(LocalDateTime.of(date, LocalTime.of(h, 0)), Duration.ofHours(1))) return true;
            }
        }
        return false;
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
