package mas;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import mas.entity.Court;
import mas.entity.Reservation;
import mas.util.DBController;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            DBController.INSTANCE.setEm(entityManager);
            DBController.INSTANCE.seedDb();

            Court c2 = DBController.INSTANCE.getEm().find(Court.class, 2);
            System.out.println(" - " + c2);
            System.out.println(" - " + c2.getReservations());

            Reservation r = DBController.INSTANCE.getEm().find(Reservation.class, 1);
            System.out.println(" - " + r);
            System.out.println(" - " + r.getCourt());

            System.out.println(" - " + c2);
            System.out.println(" - " + c2.getReservations());

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
            entityManagerFactory.close();
        }
    }



}
