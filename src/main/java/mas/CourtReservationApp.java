package mas;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import mas.entity.StaticStorage;
import mas.util.DBController;

import java.util.Objects;

public class CourtReservationApp extends Application {

    @Getter
    private static Stage stage;

    @Getter
    private static Stage additionalStage;

    @Override
    public void start(Stage stage) throws Exception {
        DBController db = DBController.INSTANCE;

//        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("court-reservation.fxml")));
//        stage.setTitle("Court Reservation App");
//        stage.setMinWidth(500);
//        stage.setMinHeight(400);
//
        CourtReservationApp.stage = stage;
        CourtReservationApp.additionalStage = new Stage();
        CourtReservationApp.additionalStage.setOnCloseRequest(e -> {
            CourtReservationApp.additionalStage.close();
            CourtReservationApp.stage.show();
        });

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("start.fxml")));
        CourtReservationApp.stage.setScene(new Scene(root, 600, 400));
        CourtReservationApp.stage.setMinWidth(500);
        CourtReservationApp.stage.setMinHeight(400);
        CourtReservationApp.stage.setTitle("Court reservation app");
        CourtReservationApp.stage.show();

    }

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        DBController.INSTANCE.setEm(entityManagerFactory.createEntityManager());
        try {

            // TODO: remove seeding
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
