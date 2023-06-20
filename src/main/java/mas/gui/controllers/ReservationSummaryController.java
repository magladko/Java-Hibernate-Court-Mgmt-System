package mas.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import mas.util.SessionData;
import mas.util.Util;

public class ReservationSummaryController {
    @FXML
    private Label summaryLabel;

    @FXML
    private TextArea summaryTextArea;

    @FXML
    public void goBackToReservation() {
        if (SessionData.getCourtReservationScene() == null)
            throw new RuntimeException("Court reservation scene not set");
        Util.changeScene(SessionData.getCourtReservationScene());
    }

    // Define your event handlers and other methods
}
