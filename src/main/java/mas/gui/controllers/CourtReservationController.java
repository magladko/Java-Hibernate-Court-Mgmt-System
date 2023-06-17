package mas.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import mas.CourtReservationApp;
import mas.entity.Court;
import mas.entity.Reservation;
import mas.entity.Trainer;
import mas.util.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CourtReservationController {

    @FXML private Button cancelButton;
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

    @FXML
    protected void initialize() {
        // Hour columns
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);
        availabilityTable.setFocusModel(null);

        ReservationData.getStart().bind(Bindings.createObjectBinding(() -> {
            LocalDate date = datePicker.getValue();
            if (date == null) return null;
            return LocalDateTime.of(date, LocalTime.MIDNIGHT);
        }, datePicker.valueProperty()));


        totalPriceLabel.textProperty().bind(Bindings.createStringBinding(() -> {


            return "";
        }, ReservationData.getCourt(), ReservationData.getStart(), ReservationData.getDuration(), ReservationData.getRacket(), ReservationData.getTrainer()));

        datePickerSetup();

        trainingCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());
        racketCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());

        confirmButton.disableProperty().bind(ReservationData.getCourt().isNull());

        trainerComboBox.disableProperty().bind(trainingCheckBox.selectedProperty().not());
        ReservationData.getTrainer().bind(Bindings.when(trainingCheckBox.selectedProperty())
                .then(trainerComboBox.valueProperty())
                .otherwise((Trainer) null));
        trainerComboBox.getItems().addAll(DBController.INSTANCE.getTrainers());

        trainerLabel.textProperty().bind(ReservationData.getTrainer().map(t -> new TrainerStringConverter().toString(t)));
        trainerMiscLabel.visibleProperty().bind(trainerLabel.textProperty().map(aString -> !aString.isEmpty()));



        trainerComboBox.setConverter(new TrainerStringConverter());
    }


    public void showReservationSummary() {
        throw new UnsupportedOperationException("Not yet implemented");
        // TODO: Implement the logic for showing the reservation summary
    }

    public void cancelReservation() {
        try {
            ReservationData.cancel();
            CourtReservationApp.getStage().setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource("start.fxml"))), 600, 400));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void datePickerSetup() {
//        datePicker.setOnHidden(event -> {
//            if (datePicker.getValue() == null) return;
//            if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(datePicker.getValue())))
//                datePicker.show();
//            else {
//                datePicker.hide();
//                updateAvailabilityTable();
//            }
//        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(newValue))){
                datePicker.setValue(oldValue);
            } else {
                updateAvailabilityTable();
            }
        });

        // disable datepicker if any hour is selected:
        BooleanBinding anyHourSelected = Bindings.createBooleanBinding(() -> ReservationData.getCourt().get() != null && ReservationData.getCourt().get().getMarkedHours().values().stream().anyMatch(ObservableBooleanValue::get), ReservationData.getCourt());
        datePicker.disableProperty().bind(anyHourSelected);

//        datePicker.disableProperty().bind()

        Tooltip datePickerCloudTooltip = new Tooltip("Brak dostępnych kortów");
        datePickerCloudTooltip.setShowDelay(javafx.util.Duration.seconds(0.1));

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isBefore(LocalDate.now().plusDays(1))) {
                    setDisable(true);
                    getStyleClass().add("past-date");

                } else if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(date))) {
                    setOnMouseEntered(event -> Tooltip.install(this, datePickerCloudTooltip));
                    setOnMouseExited(event -> Tooltip.uninstall(this, datePickerCloudTooltip));
                    getStyleClass().add("datepicker-no-court-available");
                }
            }
        });
    }

    /**
     * Reload the availability table.
     */
    private void updateAvailabilityTable() {

        availabilityTable.getColumns().removeAll(hourColumns);
        availabilityTable.getItems().clear();

        availabilityTable.setEditable(true);

        courtNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        courtNumberColumn.setCellFactory(column -> new ButtonTableCell());

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());

        // create hour columns
        LocalTime startTime = Court.getOpeningHour();
        LocalTime endTime = Court.getClosingHour();

        for (int i = 0; i < Duration.between(startTime, endTime).toHours(); i++) {
            LocalTime hour = startTime.plusHours(i);
            TableColumn<Court, Boolean> hourColumn = new TableColumn<>(new HourColumnHeaderStrConv().toString(hour));
            hourColumn.setReorderable(false);
            hourColumn.setSortable(false);

            hourColumn.setCellValueFactory(features -> features.getValue().getMarkedHours().get(hourColumn));
            hourColumn.setCellFactory(this::hourCellFactory);

            hourColumns.add(hourColumn);
            availabilityTable.getColumns().add(hourColumn);
        }

        availabilityTable.setRowFactory(tableView -> {
            TableRow<Court> row = new TableRow<>();
            row.disableProperty().bind(ReservationData.getCourt().isNotNull().and(ReservationData.getCourt().isNotEqualTo(row.itemProperty())));
            row.disabledProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue) row.getStyleClass().add("row-disabled");
                else row.getStyleClass().remove("row-disabled");
            });
            return row;
        });

        availabilityTable.getItems().addAll(DBController.INSTANCE.getCourts());

        availabilityTable.getItems().forEach(c -> {
            c.getMarkedHours().clear();
            hourColumns.forEach(col -> c.getMarkedHours().putIfAbsent(col, new SimpleBooleanProperty(false)));
        });

        availabilityTable.setDisable(false);
    }

    private TableCell<Court, Boolean> hourCellFactory(TableColumn<Court, Boolean> column) {
        if (column == null) return null;

        CheckBoxTableCell<Court, Boolean> cell = new CheckBoxTableCell<>();

        cell.itemProperty().addListener((observable, oldValue, newValue) -> {
            Court court = cell.getTableRow().getItem();
            if (court == null) return;

            if (oldValue != null && newValue && ReservationData.getCourt().getValue() == null) ReservationData.getCourt().set(court);
            if (oldValue != null && oldValue && !newValue && ReservationData.getCourt().get().getMarkedHours().values().stream().noneMatch(ObservableBooleanValue::get))
                ReservationData.getCourt().set(null);

            if (!cell.disableProperty().isBound()) {

                var currentHourColumnIndex = hourColumns.indexOf(column);
                BooleanProperty previousCellValueProperty;
                BooleanProperty nextCellValueProperty;
                if (currentHourColumnIndex == 0) {
                    previousCellValueProperty = new SimpleBooleanProperty();
                    previousCellValueProperty.setValue(null);
                } else
                    previousCellValueProperty = court.getMarkedHours().values().toArray(new BooleanProperty[0])[currentHourColumnIndex - 1];
                if (currentHourColumnIndex == hourColumns.size() - 1) {
                    nextCellValueProperty = new SimpleBooleanProperty();
                    nextCellValueProperty.setValue(null);
                } else
                    nextCellValueProperty = court.getMarkedHours().values().toArray(new BooleanProperty[0])[currentHourColumnIndex + 1];

                BooleanBinding disableAndStyleBinding = Bindings.createBooleanBinding(() -> {
                    cell.getStyleClass().remove("disabled-hour");
                    cell.getStyleClass().remove("unavailable-hour");
                    cell.getStyleClass().remove("trainer-available");

                    if (cell.getTableRow().getItem() == null || cell.getItem() == null) {
                        return false;
                    }

                    var time = (new HourColumnHeaderStrConv().fromString(column.getText())).atDate(datePicker.getValue());
                    if (!court.isAvailable(time, Duration.ofHours(1))) {
                        cell.getStyleClass().add("unavailable-hour");
                        return true;
                    }

                    boolean trainerAvailableOrNull = ReservationData.getTrainer().getValue() == null;
                    if (!trainerAvailableOrNull && ReservationData.getTrainer().get().isAvailable(time, Duration.ofHours(1))) {
                        cell.getStyleClass().add("trainer-available");
                        trainerAvailableOrNull = true;
                    }

                    if (!trainerAvailableOrNull) {
                        cell.getStyleClass().add("disabled-hour");
                        return true;
                    }

                    if (ReservationData.getCourt().getValue() == null || !ReservationData.getCourt().getValue().equals(court)) {
                        return false; // managed by row factory
                    }

                    int markedHours = court.getMarkedHours().values().stream().mapToInt(value -> value.get() ? 1 : 0).sum();
                    if (markedHours == 1) {
                        if (!previousCellValueProperty.orElse(false).getValue() && !nextCellValueProperty.orElse(false).getValue() && !cell.getItem()) {
                            cell.getStyleClass().add("disabled-hour");
                            return true;
                        }
                    } else if (previousCellValueProperty.getValue() || cell.getItem() || nextCellValueProperty.getValue()) {
                        if (previousCellValueProperty.getValue() && cell.getItem() && nextCellValueProperty.getValue()) {
                            cell.getStyleClass().add("disabled-hour");
                            return true;
                        }
                    } else {
                        cell.getStyleClass().add("disabled-hour");
                        return true;
                    }

                    return false;
                }, ReservationData.getCourt(), ReservationData.getTrainer(), previousCellValueProperty, nextCellValueProperty);

                cell.disableProperty().bind(disableAndStyleBinding);
            }
        });

        return cell;
    }
}
