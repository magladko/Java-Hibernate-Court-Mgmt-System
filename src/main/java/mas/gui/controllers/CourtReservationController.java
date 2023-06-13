package mas.gui.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import lombok.Getter;
import mas.entity.Court;
import mas.entity.Reservation;
import mas.entity.Trainer;
import mas.util.DBController;
import mas.util.MoneyFormatCell;
import mas.util.TrainerStringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CourtReservationController {

    @FXML private DatePicker datePicker;
    @FXML private TableView<Court> availabilityTable;
    @FXML private TableColumn<Court, Integer> courtNumberColumn;
    @FXML private TableColumn<Court, BigDecimal> priceColumn;
    @FXML private CheckBox trainingCheckBox;
    @FXML private ComboBox<Trainer> trainerComboBox;
    @FXML private CheckBox racketCheckBox;
    @FXML private ComboBox<String> racketComboBox;
    @FXML private TextArea commentsTextArea;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmButton;
    @FXML private Label trainerLabel;
    @FXML private Label trainerMiscLabel;

    private final List<TableColumn<Court, Boolean>> hourColumns = new ArrayList<>();

    @Getter
    private Reservation tempReservationDTO;

    private final SimpleObjectProperty<Court> currentCourt = new SimpleObjectProperty<>(null);

    @FXML
    protected void initialize() {
        // Hour columns
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);
        availabilityTable.setFocusModel(null);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateAvailabilityTable();
            }
        });

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isBefore(LocalDate.now().plusDays(1))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc;"); // Customize the disabled cell color
                }
            }
        });



        trainingCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());
        racketCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());

        confirmButton.disableProperty().bind(currentCourt.isNull());

        trainerComboBox.disableProperty().bind(trainingCheckBox.selectedProperty().not());
        trainerComboBox.getItems().addAll(DBController.INSTANCE.getTrainers());

        trainerLabel.textProperty().bind(trainerComboBox.valueProperty().map(t -> new TrainerStringConverter().toString(t)));
        trainerMiscLabel.visibleProperty().bind(trainerLabel.textProperty().map(aString -> !aString.isEmpty()));

        trainerComboBox.setConverter(new TrainerStringConverter());

    }

    public void showReservationSummary() {
        throw new UnsupportedOperationException("Not yet implemented");
        // Implement the logic for showing the reservation summary
    }

    /**
     * Reload the availability table.
     */
    private void updateAvailabilityTable() {

        availabilityTable.getColumns().removeAll(hourColumns);
        availabilityTable.getItems().clear();

        availabilityTable.setEditable(true);

        courtNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());

        // create hour columns
        LocalTime startTime = Court.getOpeningHour();
        LocalTime endTime = Court.getClosingHour();

        for (int i = 0; i < Duration.between(startTime, endTime).toHours(); i++) {
            LocalTime hour = startTime.plusHours(i);
            String hourText = hour.format(DateTimeFormatter.ofPattern("HH:mm"));

            TableColumn<Court, Boolean> hourColumn = new TableColumn<>(hourText);
            hourColumn.setReorderable(false);

            // Value factory for marking checkboxes
            hourColumn.setCellValueFactory(courtBooleanCellDataFeatures ->
                    courtBooleanCellDataFeatures.getValue().getMarkedHours().get(hourColumn));

            hourColumn.setCellFactory(column -> new CheckBoxTableCell<>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        boolean courtAvailable = column.getTableView().getItems().get(getIndex())
                                .isAvailable(LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue()), Duration.ofHours(1));

                        if (courtAvailable) {
                            // TODO: trainer's availability

                            setDisable(false);

                            var checkBox = new CheckBox();
                            checkBox.setSelected(item);
                            setGraphic(checkBox);

                            // Lock other Courts for hour choosing
                            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue) {

                                    currentCourt.set(column.getTableView().getItems().get(getIndex()));
                                    currentCourt.get().getMarkedHours().get(column).setValue(true);
                                    currentCourt.get().getDisabledTableRow().set(false);

                                    // Disable other rows of checkboxes
                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
                                        if (i != getIndex()) {
                                            column.getTableView().getItems().get(i).getDisabledTableRow().setValue(true);
                                        }
                                    }
                                } else {
                                    // Enable all rows of checkboxes
                                    column.getTableView().getItems().get(getIndex()).getMarkedHours().get(column).setValue(false);
                                    if (column.getTableView().getItems().get(getIndex()).getMarkedHours().values().stream().noneMatch(ObservableBooleanValue::get)) {
                                        currentCourt.set(null);
                                        column.getTableView().getItems().forEach(c -> c.getDisabledTableRow().setValue(false));
                                    }
                                }
                            });

                            // Bind the disable and opacity properties to the disabledTableRow property of the Court
                            var disabledTableRow = column.getTableView().getItems().get(getIndex()).getDisabledTableRow();
                            checkBox.disableProperty().bind(disabledTableRow);
                            disabledTableRow.addListener(observable -> setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0));

                            setStyle("");
                        } else {
                            // Training or reservation at given time
                            setDisable(true);
                            setStyle("-fx-background-color: #ffb4b4;" + "-fx-opacity: 0.4;");
                        }
                    }
                }
            });

            hourColumns.add(hourColumn);
            availabilityTable.getColumns().add(hourColumn);
        }

        // Actual row disabling
        availabilityTable.setRowFactory(tableView -> {
            TableRow<Court> row = new TableRow<>();
            if (row.getIndex() > 0){
                row.disableProperty().bindBidirectional(tableView.getItems().get(row.getIndex()).getDisabledTableRow());
            }
            return row;
        });

        availabilityTable.getItems().addAll(DBController.INSTANCE.getCourts());

        availabilityTable.getItems().forEach(c -> {
            c.getMarkedHours().clear();
            hourColumns.forEach(col -> c.getMarkedHours().putIfAbsent(col, new SimpleBooleanProperty(false)));
        });

        availabilityTable.setDisable(false);
    }
}
