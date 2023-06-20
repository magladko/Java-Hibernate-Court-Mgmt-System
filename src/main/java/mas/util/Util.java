package mas.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import mas.CourtReservationApp;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Util {

    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end1 = start1.plus(duration1);
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime end1 = start1.plus(duration1);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static LocalDate getNextDayOfWeek(LocalDate date, DayOfWeek dayOfWeek) {
        return date.plusDays(dayOfWeek.getValue() + 7 - date.getDayOfWeek().getValue() % 7);
    }

    public static Scene changeScene(String fxml) {
        Scene scene = null;
        try {
            scene = new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource(fxml))));
            CourtReservationApp.getStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scene;
    }

    public static void changeScene(String fxml, int width, int height) {
        try {
            CourtReservationApp.getStage().setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource(fxml))), width, height));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changeScene(Scene scene) {
        CourtReservationApp.getStage().setScene(scene);
    }
}