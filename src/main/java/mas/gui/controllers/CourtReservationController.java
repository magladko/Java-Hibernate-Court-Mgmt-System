package mas.gui.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Callback;
import mas.entity.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import mas.util.DBController;
import mas.util.MoneyFormatCell;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CourtReservationController {
    @FXML private DatePicker datePicker;
    @FXML private TableView<Court> availabilityTable;
    @FXML private TableColumn<Court, Integer> courtNumberColumn;
    @FXML private TableColumn<Court, BigDecimal> priceColumn;
    @FXML private CheckBox trainingCheckBox;
    @FXML private ComboBox<String> trainerComboBox;
    @FXML private CheckBox racketCheckBox;
    @FXML private ComboBox<String> racketComboBox;
    @FXML private TextArea commentsTextArea;
    @FXML private Label totalPriceLabel;

    @FXML
    protected void initialize() {
        // Hour columns
        availabilityTable.setDisable(true);
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAvailabilityTable();
        });


    }

    public void showReservationSummary() {
        // Implement the logic for showing the reservation summary
    }

    private void updateAvailabilityTable() {

        courtNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());

        LocalTime startTime = Court.getOpeningHour();
        LocalTime endTime = Court.getClosingHour();

        for (int i = 0; i < Duration.between(startTime, endTime).toHours(); i++) {
            LocalTime hour = startTime.plusHours(i);
            String hourText = hour.format(DateTimeFormatter.ofPattern("HH:mm"));

            TableColumn<Court, Boolean> hourColumn = new TableColumn<>(hourText);
            hourColumn.setReorderable(false);
            hourColumn.setCellValueFactory(courtBooleanCellDataFeatures ->
                    new SimpleBooleanProperty(
                            !courtBooleanCellDataFeatures.getValue().isAvailable(hour.atDate(datePicker.getValue()), Duration.ofHours(1))));

            hourColumn.setCellFactory(CheckBoxTableCell.forTableColumn(hourColumn));

            availabilityTable.getColumns().add(hourColumn);
        }

        availabilityTable.getItems().addAll(DBController.INSTANCE.getEm().createQuery("from Court", Court.class).getResultList());

        availabilityTable.setDisable(false);
    }

    // Define your event handlers and other methods

    // Define your CourtAvailability class (used by TableView)
}
