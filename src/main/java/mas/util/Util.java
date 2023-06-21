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

/**
 * Utility class for the Tennis Courts management application.
 */
public class Util {

    /**
     * Checks if two time intervals overlap.
     *
     * @param start1    the start time of the first interval
     * @param duration1 the duration of the first interval
     * @param start2    the start time of the second interval
     * @param duration2 the duration of the second interval
     * @return true if the intervals overlap, false otherwise
     */
    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end1 = start1.plus(duration1);
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Checks if two time intervals overlap.
     *
     * @param start1 the start time of the first interval
     * @param duration1 the duration of the first interval
     * @param start2 the start time of the second interval
     * @param end2 the end time of the second interval
     * @return true if the intervals overlap, false otherwise
     */
    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime end1 = start1.plus(duration1);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Checks if two time intervals overlap.
     *
     * @param start1 the start time of the first interval
     * @param end1 the end time of the first interval
     * @param start2 the start time of the second interval
     * @param duration2 the duration of the second interval
     * @return true if the intervals overlap, false otherwise
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Checks if two time intervals overlap.
     *
     * @param start1 the start time of the first interval
     * @param end1 the end time of the first interval
     * @param start2 the start time of the second interval
     * @param end2 the end time of the second interval
     * @return true if the intervals overlap, false otherwise
     */
    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Returns the next occurrence of a given day of the week after the specified date.
     *
     * @param date      the reference date
     * @param dayOfWeek the day of the week
     * @return the next occurrence of the day of the week
     */
    public static LocalDate getNextDayOfWeek(LocalDate date, DayOfWeek dayOfWeek) {
        return date.plusDays(dayOfWeek.getValue() + 7 - date.getDayOfWeek().getValue() % 7);
    }

    /**
     * Changes the scene of the application.
     *
     * @param fxml the path to the FXML file
     * @return the new scene
     * @throws IllegalArgumentException if the fxml file does not end with ".fxml"
     * @throws RuntimeException         if an error occurs while loading the FXML file
     */
    public static Scene changeScene(String fxml) {
        if (!fxml.endsWith(".fxml")) {
            throw new IllegalArgumentException("FXML file must end with .fxml");
        }

        Scene scene;
        try {
            scene = new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource(fxml))));
            CourtReservationApp.getStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scene;
    }

    /**
     * Changes the scene of the application with the specified width and height.
     *
     * @param fxml   the path to the FXML file
     * @param width  the width of the scene
     * @param height the height of the scene
     * @return the new scene
     * @throws IllegalArgumentException if the fxml file does not end with ".fxml"
     * @throws RuntimeException         if an error occurs while loading the FXML file
     */
    public static Scene changeScene(String fxml, int width, int height) {
        if (!fxml.endsWith(".fxml")) {
            throw new IllegalArgumentException("FXML file must end with .fxml");
        }

        Scene scene;
        try {
            scene = new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource(fxml))), width, height);
            CourtReservationApp.getStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return scene;
    }

    /**
     * Changes the scene of the application.
     *
     * @param scene the new scene
     */
    public static void changeScene(Scene scene) {
        CourtReservationApp.getStage().setScene(scene);
    }
}