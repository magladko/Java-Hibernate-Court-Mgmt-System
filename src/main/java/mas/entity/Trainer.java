package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Trainer {

    public enum TrainerQualification {
        Animator, Instructor, Trainer, TrainerBasicTraining, TrainerPerformanceTraining, TrainerCoach
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
    private Map<DayOfWeek, WorkingHours> workingHours;

}
