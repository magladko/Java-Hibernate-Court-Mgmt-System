package mas;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mas.util.DBController;

import java.util.Objects;

/**
 * Main class for the Tennis Courts Management Application.
 * This class extends the JavaFX Application class and serves as the entry point for the application.
 */
public class CourtReservationApp extends Application {

    /**
     * The primary stage of the application.
     */
    private static Stage stage;

    /**
     * An additional stage used for displaying additional windows in the application.
     */
    private static Stage additionalStage;

    /**
     * Retrieves the primary stage of the application.
     *
     * @return The primary stage
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Entry point for the JavaFX application.
     *
     * @param stage The primary stage
     * @throws Exception if an error occurs during application startup
     */
    @Override
    public void start(Stage stage) throws Exception {
        CourtReservationApp.stage = stage;
        CourtReservationApp.additionalStage = new Stage();
        CourtReservationApp.additionalStage.setOnCloseRequest(e -> {
            CourtReservationApp.additionalStage.close();
            CourtReservationApp.stage.show();
        });

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("start.fxml")));
        CourtReservationApp.stage.setScene(new Scene(root, 600, 400));
        CourtReservationApp.stage.setMinWidth(200);
        CourtReservationApp.stage.setMinHeight(200);
        CourtReservationApp.stage.setTitle("Court Reservation App");
        CourtReservationApp.stage.show();
    }

    /**
     * Main method for launching the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        DBController.INSTANCE.setEm(entityManagerFactory.createEntityManager());
        try {
            // DBController.INSTANCE.seedDb();
            launch(args);
        } finally {
            if (DBController.INSTANCE.getEm().getTransaction().isActive()) {
                DBController.INSTANCE.getEm().getTransaction().rollback();
            }
            DBController.INSTANCE.getEm().close();
            entityManagerFactory.close();
        }
    }
}
