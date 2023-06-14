package mas.util;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;
import mas.entity.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum DBController {
    INSTANCE;

    @Getter
    @Setter
    private EntityManager em;

    public void seedDb() {
        try {
            em.getTransaction().begin();

            var staticStorage = new StaticStorage();
            staticStorage.setCourtOpeningHour(LocalTime.of(9, 0));
            staticStorage.setCourtClosingHour(LocalTime.of(21, 0));
            staticStorage.setCourtRoofedPricePerHour(BigDecimal.valueOf(100));
            staticStorage.setCourtRoofedHeatingSurcharge(BigDecimal.valueOf(30));
            staticStorage.setCourtRoofedHeatingSeasonStart(LocalDate.of(LocalDate.now().getYear(), 10, 1));
            staticStorage.setCourtRoofedHeatingSeasonEnd(staticStorage.getCourtRoofedHeatingSeasonStart()
                                                                 .plusMonths(6));

            staticStorage.setCourtUnroofedPricePerHour(BigDecimal.valueOf(80));
            staticStorage.setCourtUnroofedSeasonStart(LocalDate.of(LocalDate.now().getYear(), 5, 1));
            staticStorage.setCourtUnroofedSeasonEnd(staticStorage.getCourtUnroofedSeasonStart().plusMonths(4));

            em.persist(staticStorage);

            int nr = 0;
            Court[] courts = {
                    new CourtRoofed(++nr, Court.SurfaceType.Clay, CourtRoofed.RoofType.Hall),
                    new CourtRoofed(++nr, Court.SurfaceType.Grass, CourtRoofed.RoofType.Balloon),
                    new CourtRoofed(++nr, Court.SurfaceType.Hard, CourtRoofed.RoofType.Balloon),
                    new CourtRoofed(++nr, Court.SurfaceType.ArtificialGrass, CourtRoofed.RoofType.Hall),
                    new CourtUnroofed(++nr, Court.SurfaceType.Clay),
                    new CourtUnroofed(++nr, Court.SurfaceType.Grass),
                    new CourtUnroofed(++nr, Court.SurfaceType.Hard)
            };
            Arrays.stream(courts).forEach(em::persist);

            Person client1 = Person.registerClient("Andrzej", "Kowalski", "123456789", "a@a.pl");
            Person client2 = Person.registerClient("Zbigniew", "Żaba", "223456789");
            Person client3 = Person.registerClient("Konrad", "Wiśnia", "323456789", "c@a.pl");

            em.persist(client1);
            em.persist(client2);
            em.persist(client3);

            Person participant1 = Person.registerParticipant("Jerzy", "Kowalski", client1);
            Person participant2 = Person.registerParticipant("Anna", "Kowalska",
                                                             LocalDate.of(2004, 10, 10),
                                                             client1
            );
            Person participant3 = Person.registerParticipant("Jerzy", "Kowalski", client2);

            em.persist(participant1);
            em.persist(participant2);
            em.persist(participant3);

            Trainer trainer1 = new Trainer("Jędrzej", "Kasztan", "423456789", "f@f.pl",
                                           Trainer.TrainerQualification.Animator, BigDecimal.valueOf(80),
                                           Map.of(
                                                   DayOfWeek.SATURDAY,
                                                   new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0))
                                           )
            );
            Trainer trainer2 = new Trainer("Zdzisław", "Wąski", "523456789", "xxx@f.pl",
                                           Trainer.TrainerQualification.TrainerCoach, BigDecimal.valueOf(180),
                                           Map.of(
                                                   DayOfWeek.SATURDAY,
                                                   new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                                                   DayOfWeek.MONDAY,
                                                   new WorkingHours(LocalTime.of(17, 0), LocalTime.of(21, 0))
                                           )
            );

            em.persist(trainer1);
            em.persist(trainer2);

            Training training1;
            Training training2;

            try {
                training1 = Training.makeReservation(
                        client1,
                        client1,
                        trainer1,
                        courts[0],
                        Util.getNextDayOfWeek(LocalDate.now(), trainer1
                                        .getWorkingHours()
                                        .keySet().stream().findAny().get())
                                .atTime(10, 0),
                        Duration.ofHours(1)
                );

                em.persist(training1);
                em.getTransaction().commit();

            } catch (TimeUnavailableException e) {
                System.out.printf("Trainer is not available: %s%n", e.getMessage());
            }

            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }

            try {

                training2 = Training.makeReservation(
                        client1,
                        participant1,
                        trainer1,
                        courts[1],
                        Util.getNextDayOfWeek(LocalDate.now(), trainer1
                                        .getWorkingHours()
                                        .keySet().stream().findAny().get())
                                .atTime(11, 0),
                        Duration.ofHours(1)
                );

                em.persist(training2);
                em.getTransaction().commit();
            } catch (TimeUnavailableException e) {
                System.out.printf("Trainer is not available: %s%n", e.getMessage());
            }

            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.getTransaction().begin();

            Racket r1 = new Racket("Yonex", 100., BigDecimal.valueOf(20));
            Racket r2 = new Racket("Willson", 150., BigDecimal.valueOf(20));
            Racket r3 = new Racket("Head", 80., BigDecimal.valueOf(20));

            em.persist(r1);
            em.persist(r2);
            em.persist(r3);

            Reservation reservation1 = Reservation.makeReservation(
                    LocalDateTime.of(
                            LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonth(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth(), 12, 0
                    ),
                    Duration.ofHours(2),
                    courts[1], client2, client2
            );

            em.persist(reservation1);

            // Fully booked day
            LocalDate fullyBookedDay = LocalDate.now().plusDays(2);
            Arrays.stream(courts).forEach(c -> {
                try {
                    em.persist(Reservation
                            .makeReservation(fullyBookedDay
                                            .atTime(staticStorage.getCourtOpeningHour()),
                                    Duration.between(staticStorage.getCourtOpeningHour(), staticStorage.getCourtClosingHour()),
                                    c, client3, client3));
                } catch (TimeUnavailableException e) {
                    System.err.println(e.getMessage());
                }
            });

            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    public List<Trainer> getTrainers() {
        return INSTANCE.getEm().createQuery("SELECT t FROM Trainer t", Trainer.class).getResultList();
    }

    public List<Court> getCourts() {
        return INSTANCE.getEm().createQuery("SELECT c FROM Court c", Court.class).getResultList();
    }

    public StaticStorage getStaticStorage() {
        return INSTANCE.getEm().createQuery("SELECT s FROM StaticStorage s", StaticStorage.class).getSingleResult();
    }
}
