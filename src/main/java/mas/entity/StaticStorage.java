package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

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

}
