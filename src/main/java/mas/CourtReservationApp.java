package mas;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import mas.util.DBController;

import java.util.Objects;

public class CourtReservationApp extends Application {

    @Getter
    @Setter
    private static volatile Stage primaryStage;

    @Getter
    @Setter
    private static volatile Scene primaryScene;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        DBController db = DBController.INSTANCE;

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("court-reservation.fxml")));
        primaryStage.setTitle("Court Reservation App");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        DBController.INSTANCE.setEm(entityManagerFactory.createEntityManager());
        try {

            DBController.INSTANCE.seedDb();
            launch(args);

        } finally {
            if(DBController.INSTANCE.getEm().getTransaction().isActive()) {
                DBController.INSTANCE.getEm().getTransaction().rollback();
            }
            DBController.INSTANCE.getEm().close();
            entityManagerFactory.close();
        }
    }
}
