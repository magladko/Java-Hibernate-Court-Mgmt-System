package mas.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import mas.CourtReservationApp;
import mas.entity.Court;
import mas.entity.Trainer;
import mas.util.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

        // === AVAILABILITY TABLE ===
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);

        availabilityTable.setEditable(true);

        courtNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        courtNumberColumn.setCellFactory(column -> new ButtonTableCell());

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());

        availabilityTable.setRowFactory(tableView -> {
            TableRow<Court> row = new TableRow<>();
            row.disableProperty().bind(SessionData.courtProperty().isNotNull().and(SessionData.courtProperty().isNotEqualTo(row.itemProperty())));
            row.disabledProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue) row.getStyleClass().add("row-disabled");
                else row.getStyleClass().remove("row-disabled");
            });
            return row;
        });
        // === END OF AVAILABILITY TABLE ===


        SessionData.courtProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // bind duration to marked hours
                SessionData.reservationDurationProperty().unbind();
                SessionData.reservationDurationProperty().bind(Bindings.createObjectBinding(() ->
                        Duration.ofHours(newValue.getMarkedHours().values().stream()
                                .filter(BooleanExpression::getValue).count()),
                        newValue.getMarkedHours().values().toArray(new BooleanProperty[0])));

                // bind start to first marked hour
                SessionData.reservationStartProperty().unbind();
                SessionData.reservationStartProperty().bind(Bindings.createObjectBinding(() -> newValue
                        .getMarkedHours()
                        .entrySet().stream()
                        .filter(e -> e.getValue().get())
                        .findFirst()
                        .map(entry -> LocalDateTime.of(
                                datePicker.getValue(),
                                new HourColumnHeaderStrConv().fromString(entry.getKey().getText())))
                        .orElseGet(() -> LocalDateTime.of(datePicker.getValue(), LocalTime.MIN)),
                        newValue.getMarkedHours().values().toArray(new BooleanProperty[0])));
            }
        });

        // TODO: bind racket

        totalPriceLabel.textProperty().bind(Bindings.createStringBinding(
            () -> {
                if (SessionData.getTotalPrice().isEmpty()) return "";
                return NumberFormat.getCurrencyInstance().format(SessionData.getTotalPrice().get());
            },
            SessionData.courtProperty(),
            SessionData.reservationStartProperty(),
            SessionData.reservationDurationProperty(),
            SessionData.racketProperty(),
            SessionData.trainerProperty()));

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(newValue))){
                datePicker.setValue(oldValue);
            } else {
                // On valid date change:
                refreshAvailabilityTable();

                trainerComboBox.getItems().clear();
//                trainerReservationControlsSetup();
                // TODO: Racket
            }
        });
        datePickerSetup();

        trainerReservationControlsSetup();
        trainingCheckBox.selectedProperty().addListener(trainingCheckBoxSelectionListener());

        racketCheckBox.disableProperty().bind(SessionData.courtProperty().isNull());
        confirmButton.disableProperty().bind(SessionData.courtProperty().isNull());
    }

    private void trainerReservationControlsSetup() {
        trainerComboBox.getItems().clear();

        trainingCheckBox.disableProperty().unbind();
        trainingCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());

        trainerComboBox.disableProperty().unbind();
        trainerComboBox.disableProperty().bind(trainingCheckBox.selectedProperty().not());

        SessionData.trainerProperty().unbind();
        SessionData.trainerProperty().bind(Bindings.when(trainingCheckBox.selectedProperty())
                .then(trainerComboBox.valueProperty())
                .otherwise((Trainer) null));

        trainerLabel.textProperty().bind(SessionData.trainerProperty().map(t -> new TrainerStringConverter().toString(t)));
        trainerMiscLabel.visibleProperty().bind(trainerLabel.textProperty().map(aString -> !aString.isEmpty()));
        trainerComboBox.setConverter(new TrainerStringConverter());
    }

    @NotNull
    private ChangeListener<Boolean> trainingCheckBoxSelectionListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                var selected = trainerComboBox.getSelectionModel().getSelectedItem();
                trainerComboBox.getItems().clear();

                var availableTrainers = DBController.INSTANCE.getTrainers().stream().filter(t -> t.isAvailable(datePicker.getValue())).toList();

                if (availableTrainers.isEmpty()) {
//                    trainingCheckBox.setSelected(false);

                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Brak trenerów");
                    alert.setHeaderText("Brak trenerów");

                    alert.setContentText("Brak trenerów dostępnych w wybranym dniu");

                    var change = new ButtonType("Zmień datę");
                    var changeUC = new ButtonType("Rezerwuj trening\nzamiast kortu");
                    var resignFromTraining = new ButtonType("Zrezygnuj z treningu", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(change, changeUC, resignFromTraining);

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent()) {
                        if (result.get() == change) {
                            // TODO: maybe fix datepicker not showing when hours are unselected
                            if (SessionData.courtProperty().getValue() != null)
                                SessionData.courtProperty().get().getMarkedHours().values().forEach(v -> v.set(false));
                            datePicker.show();
                        } else if (result.get() == changeUC) {
                            cancelReservationProcess();
                        } else if (result.get() == resignFromTraining) {
                            trainingCheckBox.selectedProperty().unbind();
                            trainingCheckBox.setSelected(false);
                            trainingCheckBox.disableProperty().unbind();
                            trainingCheckBox.setDisable(true);
                        } else throw new RuntimeException("Unexpected button type");
                    }
                } else if (SessionData.courtProperty().getValue() != null) {
                    List<Trainer> availableTrainersForMarkedHours = DBController.INSTANCE.getTrainers().stream()
                            .filter(t -> t.isAvailable(SessionData.reservationStartProperty().get(), SessionData.reservationDurationProperty().get())).toList();

                    if (availableTrainersForMarkedHours.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Brak trenerów");
                        alert.setHeaderText("Brak trenerów");

                        alert.setContentText("Brak trenerów dostępnych w wybranych godzinach.");

                        var change = new ButtonType("Wyczyść godziny");
                        var changeUC = new ButtonType("Rezerwuj trening\nzamiast kortu");
                        var resignFromTraining = new ButtonType("Zrezygnuj z treningu", ButtonBar.ButtonData.CANCEL_CLOSE);

                        alert.getButtonTypes().setAll(change, changeUC, resignFromTraining);

                        Optional<ButtonType> result = alert.showAndWait();

                        if (result.isPresent()) {
                            if (result.get() == change) {
                                // TODO: maybe fix datepicker not showing when hours are unselected
                                if (SessionData.courtProperty().getValue() != null)
                                    SessionData.courtProperty().get().getMarkedHours().values().forEach(v -> v.set(false));
                                trainerComboBox.getSelectionModel().select(0);
//                                trainingCheckBox.setSelected(true);
                            } else if (result.get() == changeUC) {
                                cancelReservationProcess();
                            } else if (result.get() == resignFromTraining) {
                                trainingCheckBox.selectedProperty().unbind();
                                trainingCheckBox.setSelected(false);
                            } else throw new RuntimeException("Unexpected button type");
                        }
                    }
                }

                // TODO: MANAGE TRAINER SELECTION CORRECTLY

                trainerComboBox.getItems().addAll(availableTrainers);
                if (trainerComboBox.getItems().contains(selected)) trainerComboBox.getSelectionModel().select(selected);
                else if (!trainerComboBox.getItems().isEmpty()) {

                }
//                trainerComboBox.itemsProperty()
            }
        };
    }

    public void showReservationSummary() {
        throw new UnsupportedOperationException("Not yet implemented");
        // TODO: Implement the logic for showing the reservation summary
    }

    public void cancelReservationProcess() {
        try {
            SessionData.cancel();
            CourtReservationApp.getStage().setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource("start.fxml"))), 600, 400));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void datePickerSetup() {
        // disable datepicker if any hour is selected:
        BooleanBinding anyHourOrTrainingOrRacketSelected = Bindings.createBooleanBinding(() ->
                SessionData.courtProperty().getValue() != null || trainingCheckBox.isSelected() || racketCheckBox.isSelected(),
                SessionData.courtProperty(), trainingCheckBox.selectedProperty(), racketCheckBox.selectedProperty());
        datePicker.disableProperty().bind(anyHourOrTrainingOrRacketSelected);

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

    private void refreshAvailabilityTable() {

        availabilityTable.getColumns().removeAll(hourColumns);
        availabilityTable.getItems().clear();

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

            // set current court if it's not set yet
            if (oldValue != null && newValue && SessionData.courtProperty().getValue() == null) SessionData.courtProperty().set(court);

            // unmark court if all hours are unmarked
            if (oldValue != null && oldValue && !newValue && SessionData.courtProperty().getValue() != null &&
                    SessionData.courtProperty().get().getMarkedHours().values().stream().noneMatch(ObservableBooleanValue::get))
                SessionData.courtProperty().set(null);

            // continue only on initialization
            if (cell.disableProperty().isBound()) return;

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

                if (cell.getTableRow().getItem() == null || cell.getItem() == null) return false;
//                if (datePicker.valueProperty().getValue() == null) {
//                    court.getMarkedHours().values().forEach(p -> p.setValue(false));
//                    cell.getStyleClass().add("disabled-hour");
//                    return true;
//                }

                var time = (new HourColumnHeaderStrConv().fromString(column.getText())).atDate(datePicker.getValue());
                if (!court.isAvailable(time, Duration.ofHours(1))) {
                    cell.getStyleClass().add("unavailable-hour");
                    return true;
                }

                boolean trainerAvailableOrNull = SessionData.trainerProperty().getValue() == null;
                if (!trainerAvailableOrNull && SessionData.trainerProperty().get().isAvailable(time, Duration.ofHours(1))) {
                    cell.getStyleClass().add("trainer-available");
                    trainerAvailableOrNull = true;
                }

                if (!trainerAvailableOrNull) {
                    cell.getStyleClass().add("disabled-hour");
                    return true;
                }

                if (SessionData.courtProperty().getValue() == null || !SessionData.courtProperty().getValue().equals(court)) {
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
            }, SessionData.courtProperty(), SessionData.trainerProperty(), previousCellValueProperty, nextCellValueProperty, datePicker.valueProperty());

            cell.disableProperty().bind(disableAndStyleBinding);

        });

        return cell;
    }
}
