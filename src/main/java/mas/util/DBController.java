package mas.util;

import jakarta.persistence.EntityManager;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;
import mas.entity.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum DBController {
    INSTANCE;

    @Getter
    @Setter
    private EntityManager em;

    @Getter
    private final SimpleObjectProperty<Reservation> tempReservation = new SimpleObjectProperty<>();

    @Getter
    private final SimpleObjectProperty<Training> tempTraining = new SimpleObjectProperty<>();

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

            List<Person> clients = List.of(
                    Person.registerClient("Andrzej", "Kowalski", "123456789", "a@a.pl"),
                    Person.registerClient("Zbigniew", "Żaba", "223456789"),
                    Person.registerClient("Konrad", "Wiśnia", "323456789", "c@a.pl")
            );

            clients.forEach(em::persist);

            List<Person> participants = List.of(
                    Person.registerParticipant("Jerzy", "Kowalski", clients.get(0)),
                    Person.registerParticipant("Anna", "Kowalska", LocalDate.of(2004, 10, 10), clients.get(0)),
                    Person.registerParticipant("Jerzy", "Kowalski", clients.get(1))
            );

            participants.forEach(em::persist);

            List<Trainer> trainers = List.of(
                    new Trainer("Jędrzej", "Kasztan", "423456789", "f@f.pl", Trainer.TrainerQualification.Animator, BigDecimal.valueOf(80),
                            Map.of(DayOfWeek.SATURDAY,
                                    new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0))
                            )),
                    new Trainer("Zdzisław", "Wąski", "523456789", "xxx@f.pl", Trainer.TrainerQualification.TrainerCoach, BigDecimal.valueOf(180),
                            Map.of(
                                    DayOfWeek.SATURDAY,
                                    new WorkingHours(LocalTime.of(15, 0), LocalTime.of(18, 0)),
                                    DayOfWeek.MONDAY,
                                    new WorkingHours(LocalTime.of(17, 0), LocalTime.of(21, 0))
                            ))
            );

            trainers.forEach(em::persist);

            List<Training> trainings = new ArrayList<>();
            trainings.add(Training.makeReservation(
                    clients.get(0), clients.get(0), trainers.get(0), courts[0],
                    Util.getNextDayOfWeek(LocalDate.now(),
                            trainers.get(0).getWorkingHours().keySet().stream().findAny().orElseThrow())
                            .atTime(10, 0),
                    Duration.ofHours(1)));

            trainings.add(Training.makeReservation(
                    clients.get(0), participants.get(0), trainers.get(0), courts[1],
                    Util.getNextDayOfWeek(LocalDate.now(), trainers.get(0).getWorkingHours().keySet().stream().findAny().orElseThrow())
                            .atTime(11, 0),
                    Duration.ofHours(1)));

//            trainings.add(Training.makeReservation(
//                    clients.get(1), clients.get(1), trainers.get(0), courts[5],
//                    Util.getNextDayOfWeek(LocalDate.now(), trainers.get(0).getWorkingHours().keySet().stream().findAny().orElseThrow())
//                            .atTime(staticStorage.getCourtOpeningHour()),
//                    Duration.between(staticStorage.getCourtOpeningHour(), staticStorage.getCourtClosingHour())));

            trainings.forEach(em::persist);

            List<Racket> rackets = List.of(
                    new Racket("Yonex", 100., BigDecimal.valueOf(20)),
                    new Racket("Willson", 150., BigDecimal.valueOf(20)),
                    new Racket("Head", 80., BigDecimal.valueOf(20))
            );

            rackets.forEach(em::persist);

            // Fully booked day
            LocalDate fullyBookedDay = LocalDate.now().plusDays(2);

            List<Reservation> reservations = new ArrayList<>();
            reservations.add(Reservation.makeReservation(
                    LocalDateTime.of(
                            LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonth(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth(), 12, 0
                    ),
                    Duration.ofHours(2),
                    courts[1], rackets.get(0), clients.get(1), clients.get(1)));

            reservations.add(Reservation.makeReservation(
                    LocalDateTime.of(
                            LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonth(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth(), 12, 0
                    ),
                    Duration.ofHours(2),
                    courts[2], rackets.get(1), clients.get(1), clients.get(1)));

            reservations.add(Reservation.makeReservation(
                    LocalDateTime.of(
                            LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonth(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth(), 12, 0
                    ),
                    Duration.ofHours(2),
                    courts[3], rackets.get(2), clients.get(1), clients.get(1)));

            Arrays.stream(courts).forEach(c -> {
                try {
                    reservations.add(Reservation.makeReservation(
                            fullyBookedDay.atTime(staticStorage.getCourtOpeningHour()),
                            Duration.between(staticStorage.getCourtOpeningHour(), staticStorage.getCourtClosingHour()),
                            c, clients.get(2), clients.get(2)));

                } catch (TimeUnavailableException e) {
                    System.err.println(e.getMessage());
                }
            });

            LocalDate eqFullyBookedDay = LocalDate.now().plusDays(3);

            for (int r = 0; r < rackets.size(); r++) {
                try {
                    reservations.add(Reservation.makeReservation(
                            eqFullyBookedDay.atTime(staticStorage.getCourtOpeningHour()),
                            Duration.between(staticStorage.getCourtOpeningHour(), staticStorage.getCourtClosingHour()),
                            courts[r], rackets.get(r), clients.get(2), clients.get(2)));
                } catch (TimeUnavailableException e) {
                    System.err.println("For eq fully booked day: " + e.getMessage());
                }
            }

            reservations.forEach(em::persist);

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

    public StaticStorage getSS() {
        return getStaticStorage();
    }

    public List<Racket> getRackets() {
        return INSTANCE.getEm().createQuery("SELECT r FROM Racket r", Racket.class).getResultList();
    }
}
