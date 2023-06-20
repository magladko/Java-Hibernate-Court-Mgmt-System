package mas.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import mas.CourtReservationApp;
import mas.util.DBController;
import mas.util.SessionData;
import mas.util.Util;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button seedDBButton;
    @FXML private Button runUseCaseButton;

    @FXML
    public void seedDbClick() {
        try {
            DBController.INSTANCE.seedDb();
        } catch(Exception e) {
            seedDBButton.setDisable(true);
            messageLabel.setText("Seeding failed. Database might already contain data.");
            return;
        }
        seedDBButton.setDisable(true);
        messageLabel.setText("Seed method initialized.");
    }

    @FXML
    public void runUseCase() {
        SessionData.setCourtReservationScene(Util.changeScene("court-reservation.fxml"));
//        var stage = CourtReservationApp.getStage();
//        try {
//            stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource("court-reservation.fxml")))));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

}
