package mas;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import mas.entity.*;
import mas.util.TimeUnavailableException;

import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            var staticStorage = new StaticStorage();
            staticStorage.setCourtRoofedPricePerHour(BigDecimal.valueOf(100));
            staticStorage.setCourtRoofedHeatingSurcharge(BigDecimal.valueOf(30));
            staticStorage.setCourtRoofedHeatingSeasonStart(LocalDate.of(LocalDate.now().getYear(), 10, 1));
            staticStorage.setCourtRoofedHeatingSeasonEnd(staticStorage.getCourtRoofedHeatingSeasonStart().plusMonths(6));

            staticStorage.setCourtUnroofedPricePerHour(BigDecimal.valueOf(80));
            staticStorage.setCourtUnroofedSeasonStart(LocalDate.of(LocalDate.now().getYear(), 5, 1));
            staticStorage.setCourtUnroofedSeasonEnd(staticStorage.getCourtRoofedHeatingSeasonStart().plusMonths(4));

            entityManager.persist(staticStorage);

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
            Arrays.stream(courts).forEach(entityManager::persist);

            Person client1 = Person.registerClient("Andrzej", "Kowalski", "123456789", "a@a.pl");
            Person client2 = Person.registerClient("Zbigniew", "Żaba", "223456789");
            Person client3 = Person.registerClient("Konrad", "Wiśnia", "323456789", "c@a.pl");

            entityManager.persist(client1);
            entityManager.persist(client2);
            entityManager.persist(client3);

            Person participant1 = Person.registerParticipant("Jerzy", "Kowalski", client1);
            Person participant2 = Person.registerParticipant("Anna", "Kowalska",
                                                             LocalDate.of(2004, 10, 10),
                                                             client1);

            // TODO: Check redundancy
            client1.addParticipant(participant1);
            client1.addParticipant(participant2);

            entityManager.persist(participant1);
            entityManager.persist(participant2);

            Trainer trainer1 = new Trainer("Jędrzej", "Kasztan", "423456789", "f@f.pl",
                                     Trainer.TrainerQualification.Animator, BigDecimal.valueOf(80),
                                     Map.of(DayOfWeek.SATURDAY, new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0))));
            Trainer trainer2 = new Trainer("Zdzisław", "Wąski", "523456789", "xxx@f.pl",
                                     Trainer.TrainerQualification.TrainerCoach, BigDecimal.valueOf(180),
                                     Map.of(DayOfWeek.SATURDAY, new WorkingHours(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                                            DayOfWeek.MONDAY, new WorkingHours(LocalTime.of(17, 0), LocalTime.of(21, 0))));

            entityManager.persist(trainer1);
            entityManager.persist(trainer2);

            Training training1;
            Training training2;

            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();

            Calendar calendar = Calendar.getInstance();
            try {
                var dayOfWeek = trainer1.getWorkingHours().keySet().stream().findFirst().get().getValue() % 7 + 1;
                calendar.add(Calendar.DAY_OF_MONTH, (dayOfWeek + 7 - calendar.get(Calendar.DAY_OF_WEEK)) % 7);

//                calendar.set(Calendar.DAY_OF_WEEK, trainer1.getWorkingHours().keySet().stream().findFirst().get().getValue());
                System.out.println(calendar.getTime());

                training1 = Training.makeReservation(client1, client1, trainer1, courts[0],
                                                     new java.sql.Date(calendar.getTime().getTime()).toLocalDate().atTime(10, 0),
                                                     Duration.ofHours(1));

                System.out.println(training1);
                System.out.println(training1.getId());
                System.out.println(training1.getTrainer());
                System.out.println(training1.getClients());
                System.out.println(training1.getParticipants());

                entityManager.persist(training1);
                entityManager.getTransaction().commit();

            } catch (TimeUnavailableException e) {
                System.out.printf("Trainer is not available: %s%n", e.getMessage());
            }
            entityManager.getTransaction().begin();
            try {
                var dayOfWeek = trainer1.getWorkingHours().keySet().stream().findFirst().get().getValue() % 7 + 1;
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, (dayOfWeek + 7 - calendar.get(Calendar.DAY_OF_WEEK)) % 7);

//                calendar.set(Calendar.DAY_OF_WEEK, trainer1.getWorkingHours().keySet().stream().findFirst().get().getValue());
                System.out.println(calendar.getTime());

                training2 = Training.makeReservation(client1, participant1, trainer1, courts[1],
                                                     new java.sql.Date(calendar.getTime().getTime()).toLocalDate().atTime(11, 0),
                                                     Duration.ofHours(1));
                entityManager.persist(training2);
                entityManager.getTransaction().commit();
            } catch (TimeUnavailableException e) {
                System.out.printf("Trainer is not available: %s%n", e.getMessage());
            }

            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
            }
            entityManager.getTransaction().begin();

            Racket r1 = new Racket("Yonex", 100., BigDecimal.valueOf(20));
            Racket r2 = new Racket("Willson", 150., BigDecimal.valueOf(20));
            Racket r3 = new Racket("Head", 80., BigDecimal.valueOf(20));

            entityManager.persist(r1);
            entityManager.persist(r2);
            entityManager.persist(r3);

            Reservation reservation1 = Reservation.makeReservation(
                    LocalDateTime.of(
                            LocalDateTime.now().plusDays(1).getYear(),
                            LocalDateTime.now().plusDays(1).getMonth(),
                            LocalDateTime.now().plusDays(1).getDayOfMonth(), 12, 0),
                    Duration.ofHours(2),
                    courts[1], client2, client2);

            entityManager.persist(reservation1);

            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}
