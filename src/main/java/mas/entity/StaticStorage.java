package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity class for persisting static values related to the Tennis Courts management application using Hibernate mechanisms.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class StaticStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /**
     * The opening hour of the tennis court.
     */
    @Column(unique = true)
    private LocalTime courtOpeningHour;

    /**
     * The closing hour of the tennis court.
     */
    @Column(unique = true)
    private LocalTime courtClosingHour;

    /**
     * The price per hour for a roofed tennis court.
     */
    @Column(unique = true)
    private BigDecimal courtRoofedPricePerHour;

    /**
     * The surcharge amount for heating a roofed tennis court during the heating season.
     */
    @Column(unique = true)
    private BigDecimal courtRoofedHeatingSurcharge;

    /**
     * The start date of the heating season for a roofed tennis court.
     */
    @Column(unique = true)
    private LocalDate courtRoofedHeatingSeasonStart;

    /**
     * The end date of the heating season for a roofed tennis court.
     */
    @Column(unique = true)
    private LocalDate courtRoofedHeatingSeasonEnd;

    /**
     * The price per hour for an unroofed tennis court.
     */
    @Column(unique = true)
    private BigDecimal courtUnroofedPricePerHour;

    /**
     * The start date of the season for an unroofed tennis court.
     */
    @Column(unique = true)
    private LocalDate courtUnroofedSeasonStart;

    /**
     * The end date of the season for an unroofed tennis court.
     */
    @Column(unique = true)
    private LocalDate courtUnroofedSeasonEnd;
}
