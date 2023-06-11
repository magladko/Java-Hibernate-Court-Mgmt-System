package mas.gui.controllers;

import mas.entity.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CourtReservationController {
    @FXML private DatePicker datePicker;
    @FXML private TableView<Court> availabilityTable;
    @FXML private TableColumn<CourtAvailability, String> courtNumberColumn;
    @FXML private TableColumn<CourtAvailability, String> priceColumn;
    @FXML private CheckBox trainingCheckBox;
    @FXML private ComboBox<String> trainerComboBox;
    @FXML private CheckBox racketCheckBox;
    @FXML private ComboBox<String> racketComboBox;
    @FXML private TextArea commentsTextArea;
    @FXML private Label totalPriceLabel;

    @FXML
    protected void initialize() {
        // Hour columns
        LocalTime startTime = LocalTime.of(9, 0);
        for (int i = 0; i < 12; i++) {
            LocalTime hour = startTime.plusHours(i);
            String hourText = hour.format(DateTimeFormatter.ofPattern("HH:mm"));

            TableColumn<Court, Boolean> hourColumn = new TableColumn<>(hourText);
            hourColumn.setReorderable(false);
            hourColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
            hourColumn.setCellFactory(CheckBoxTableCell.forTableColumn(hourColumn));

            availabilityTable.getColumns().add(hourColumn);
        }
    }

    public void showReservationSummary() {
        // Implement the logic for showing the reservation summary
    }

    // Define your event handlers and other methods

    // Define your CourtAvailability class (used by TableView)
}
