package mas.gui.controllers;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import mas.util.DBController;
import mas.util.HourColumnHeaderStrConv;
import mas.util.MoneyFormatCell;
import mas.util.TrainerStringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CourtReservationController {

    // TODO: show court info

    @FXML public Button cancelButton;
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
    private final SimpleObjectProperty<Trainer> selectedTrainer = new SimpleObjectProperty<>(null);

    @FXML
    protected void initialize() {
        // Hour columns
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);
        availabilityTable.setFocusModel(null);

        datePickerSetup();

        trainingCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());
        racketCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());

        confirmButton.disableProperty().bind(currentCourt.isNull());

        trainerComboBox.disableProperty().bind(trainingCheckBox.selectedProperty().not());
        selectedTrainer.bind(Bindings.when(trainingCheckBox.selectedProperty())
                .then(trainerComboBox.valueProperty())
                .otherwise((Trainer) null));
        trainerComboBox.getItems().addAll(DBController.INSTANCE.getTrainers());

        trainerLabel.textProperty().bind(selectedTrainer.map(t -> new TrainerStringConverter().toString(t)));
        trainerMiscLabel.visibleProperty().bind(trainerLabel.textProperty().map(aString -> !aString.isEmpty()));

        trainerComboBox.setConverter(new TrainerStringConverter());

    }


    public void showReservationSummary() {
        throw new UnsupportedOperationException("Not yet implemented");
        // Implement the logic for showing the reservation summary
    }

    public void cancelReservation() {
        try {
            CourtReservationApp.getStage().setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(CourtReservationApp.class.getResource("start.fxml"))), 600, 400));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void datePickerSetup() {
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(newValue))) {
                    datePicker.setValue(oldValue);

                    datePicker.setOnHidden(event -> datePicker.show());
                    return;
                }
                datePicker.setOnHidden(null);
                updateAvailabilityTable();
            }
        });

        datePicker.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> datePicker.setOnHidden(null));

        Tooltip datePickerCloudTooltip = new Tooltip("No court available on this date");
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
//                    setDisable(true);
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

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());

        // create hour columns
        LocalTime startTime = Court.getOpeningHour();
        LocalTime endTime = Court.getClosingHour();

        for (int i = 0; i < Duration.between(startTime, endTime).toHours(); i++) {
            LocalTime hour = startTime.plusHours(i);
            TableColumn<Court, Boolean> hourColumn = new TableColumn<>(new HourColumnHeaderStrConv().toString(hour));
            hourColumn.setReorderable(false);

            hourColumn.setCellValueFactory(features -> features.getValue().getMarkedHours().get(hourColumn));

            hourColumn.setCellFactory(column -> {
                if (column == null) return null;

                CheckBoxTableCell<Court, Boolean> cell = new CheckBoxTableCell<>() {
//                    @Override
//                    public void updateItem(Boolean item, boolean empty) {
//                        super.updateItem(item, empty);
//
//                        if (item == null || empty) return;
//
//                        var court = this.getTableRow().getItem();
//                        if (court == null) return;
//
//
//                        var currentHourColumnIndex = hourColumns.indexOf(column);
////                    hourColumns.indexOf()
//                        System.out.println(currentHourColumnIndex);
//
//                        Optional<Boolean> previousCellValue;
//                        Optional<Boolean> nextCellValue;
//                        if (currentHourColumnIndex == 0) {
//                            previousCellValue = Optional.empty();
//                        } else previousCellValue = Optional.of(((BooleanProperty) court.getMarkedHours().values().toArray()[currentHourColumnIndex - 1]).get());
//                        if (currentHourColumnIndex == hourColumns.size() - 1) {
//                            nextCellValue = Optional.empty();
//                        } else nextCellValue = Optional.of(((BooleanProperty) court.getMarkedHours().values().toArray()[currentHourColumnIndex + 1]).get());
//
//                        if (currentCourt.getValue() != null && currentCourt.get().equals(court)) {
//                            int markedHours = court.getMarkedHours().values().stream().mapToInt(value -> value.get() ? 1 : 0).sum();
//
//                            boolean anyMarked = court.getMarkedHours().values().stream().anyMatch(ObservableBooleanValue::get);
//                            if (!previousCellValue.orElse(!anyMarked) && !nextCellValue.orElse(!anyMarked) && markedHours > 1) {
//                                this.setDisable(true);
//                                this.getStyleClass().add("unavailable-hour");
//                            } else {
//                                this.setDisable(false);
//                                this.getStyleClass().remove("unavailable-hour");
//                            }
//                        }
//
//                    }
                };

//                cell.disableProperty().bind(previousCellValueProperty.or(nextCellValueProperty));

//                Observable[] dependencies = new Observable[]{ currentCourt,  };


                // TODO: FIX ME!
//                List<Object> dependencies = new ArrayList<>(cell.getTableRow().getItem().getMarkedHours().values().stream().toList());
//                dependencies.add(currentCourt);

                BooleanBinding disableBinding = Bindings.createBooleanBinding(() -> {
                    if (cell.itemProperty().getValue() == null) return true;

                    Court court = cell.getTableRow().getItem();
                    if (court == null) return true;

//                    cell.setDisable(false);
                    cell.getStyleClass().remove("unavailable-hour");
                    cell.getStyleClass().remove("disabled-hour");

                    var time = (new HourColumnHeaderStrConv().fromString(column.getText())).atDate(datePicker.getValue());
                    if (!court.isAvailable(time, Duration.ofHours(1))) {
//                        cell.setDisable(true);
                        cell.getStyleClass().add("unavailable-hour");
                        return true;
                    }

                    var currentHourColumnIndex = hourColumns.indexOf(column);
                    Optional<Boolean> previousCellValue;
                    Optional<Boolean> nextCellValue;
                    if (currentHourColumnIndex == 0) {
                        previousCellValue = Optional.empty();
                    } else previousCellValue = Optional.of(((BooleanProperty) court.getMarkedHours().values().toArray()[currentHourColumnIndex - 1]).get());
                    if (currentHourColumnIndex == hourColumns.size() - 1) {
                        nextCellValue = Optional.empty();
                    } else nextCellValue = Optional.of(((BooleanProperty) court.getMarkedHours().values().toArray()[currentHourColumnIndex + 1]).get());

                    if (currentCourt.getValue() == null) return false;

                    if (!currentCourt.getValue().equals(court)) {
                        return false; // case managed by row factory
                    }

                    int markedHours = court.getMarkedHours().values().stream().mapToInt(value -> value.get() ? 1 : 0).sum();
                    if (markedHours == 1) {
                        if (!previousCellValue.orElse(false) && !nextCellValue.orElse(false) && !cell.getItem()) {
                            cell.getStyleClass().add("disabled-hour");
                            return true;
                        }
                    }

                    // enabled holds true and if neighbour is unmarked (or edge)
                    if (cell.getItem() && (!previousCellValue.orElse(false) || !nextCellValue.orElse(false))) {
                        return false;
                    }

                    if (!cell.getItem() && !previousCellValue.orElse(false) && !nextCellValue.orElse(false)) {
                        return false;
                    }

//                    if ((cell.getItem() && (!previousCellValue.orElse(false) || !nextCellValue.orElse(false)))) {
//                        cell.getStyleClass().add("disabled-hour");
//                        return true;
//                    }



                    if (previousCellValue.isEmpty() && markedHours == 0) return false;

                    return false;
                }, dependencies.toArray(new Observable[0])); //currentCourt, cell.getTableRow().getItem().getMarkedHours().values().toArray(new BooleanProperty[0]));

                cell.disableProperty().bind(disableBinding);

                cell.itemProperty().addListener((observable, oldValue, newValue) -> {
                    Court court = cell.getTableRow().getItem();
                    if (court == null) return;

//                    cell.setDisable(false);
//                    cell.getStyleClass().remove("unavailable-hour");
//
//                    var time = (new HourColumnHeaderStrConv().fromString(column.getText())).atDate(datePicker.getValue());
//                    if (!court.isAvailable(time, Duration.ofHours(1))) {
//                        cell.setDisable(true);
//                        cell.getStyleClass().add("unavailable-hour");
//                        return;
//                    }
////
//                    BooleanBinding disableBinding = Bindings.createBooleanBinding(() -> {
//                        if (currentCourt.getValue() == null) return false;
//
//                        if (previousCellValueProperty.getValue() == null && nextCellValueProperty.getValue() == null) {
//                            return false;
//                        }
//
//                        if (previousCellValueProperty.getValue() == null && nextCellValueProperty.getValue() != null) {
//                            return !nextCellValueProperty.get();
//                        }
//
//                        if (previousCellValueProperty.getValue() != null && nextCellValueProperty.getValue() == null) {
//                            return !previousCellValueProperty.get();
//                        }
//
//                        return !previousCellValueProperty.get() && !nextCellValueProperty.get();
//                    }, currentCourt, previousCellValueProperty, nextCellValueProperty);
//
//                    cell.disableProperty().bind(disableBinding);



//                    cell.setDisable(false);

                    if (oldValue == null) return;

                    if (newValue && currentCourt.getValue() == null) currentCourt.set(court);
                    if (oldValue && !newValue && currentCourt.get().getMarkedHours().values().stream().noneMatch(ObservableBooleanValue::get)) currentCourt.set(null);
                });
                return cell;
            });

//            hourColumn.setCellFactory(column -> new CheckBoxTableCell<>() {
//                @Override
//                public void updateItem(Boolean item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    try {
//                        Court court = column.getTableView().getItems().get(getIndex());
//                        System.out.println("Court: " + court.getNumber() + ";; item=" + item + " ; empty=" + empty);
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage() + ";; item=" + item + " ; empty=" + empty);
//                    }
//
//                    if (item == null || empty) return;
//
//                    Court court = column.getTableView().getItems().get(getIndex());
//
//                    var time = LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue());
//                    if (!court.isAvailable(time, Duration.ofHours(1))) {
//                        setDisable(true);
//                        getStyleClass().add("unavailable-hour");
//                        return;
//                    }
//
////                    this.selectedProperty().
//
//                    if ((currentCourt.getValue() == null || currentCourt.get().equals(court)) && court.isAvailable(time, Duration.ofHours(1))) {
//                        // TODO: trainer's availability
//                        setDisable(false);
//
//                        var checkBox = new CheckBox();
//                        checkBox.setSelected(item);
//                        setGraphic(checkBox);
//
//                        // Lock other Courts for hour choosing
//                        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
//                            if (newValue) {
//
//                                currentCourt.set(court);
////                                    currentCourt.set(column.getTableView().getItems().get(getIndex()));
//                                currentCourt.get().getMarkedHours().get(column).setValue(true);
////                                    currentCourt.get().getDisabledTableRow().set(false);
//
//                                // Disable other rows of checkboxes
////                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
////                                        if (i != getIndex()) {
////                                            column.getTableView().getItems().get(i).getDisabledTableRow().setValue(true);
////                                        }
////                                    }
//                            } else {
//                                // Enable all rows of checkboxes
//                                court.getMarkedHours().get(column).setValue(false);
////                                    column.getTableView().getItems().get(getIndex()).getMarkedHours().get(column).setValue(false);
//                                if (court.getMarkedHours().values().stream().noneMatch(ObservableBooleanValue::get)) {
//                                    // no marked boxes left
//                                    currentCourt.set(null);
////                                        column.getTableView().getItems().forEach(c -> c.getDisabledTableRow().setValue(false));
//                                }
//                            }
//                        });
//                        // Bind the disable and opacity properties to the disabledTableRow property of the Court
////                            var disabledTableRow = column.getTableView().getItems().get(getIndex()).getDisabledTableRow();
////                            checkBox.disableProperty().bind(disabledTableRow);
////                            disabledTableRow.addListener(observable -> setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0));
//
//                        setStyle("");
//                    } else {
//                        // Training or reservation at given time
//                        setDisable(true);
//                        getStyleClass().add("unavailable-hour");
//                    }
//
//                }
//            });


            hourColumns.add(hourColumn);
            availabilityTable.getColumns().add(hourColumn);
        }

        availabilityTable.setRowFactory(tableView -> {
            TableRow<Court> row = new TableRow<>();
            row.disableProperty().bind(currentCourt.isNotNull().and(currentCourt.isNotEqualTo(row.itemProperty())));
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
}
