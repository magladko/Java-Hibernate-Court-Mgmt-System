package mas.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import mas.entity.Court;
import mas.entity.Racket;
import mas.entity.Trainer;
import mas.util.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourtReservationController {

    @FXML private Button cancelButton;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Court> availabilityTable;
    @FXML private TableColumn<Court, Integer> courtNumberColumn;
    @FXML private TableColumn<Court, BigDecimal> priceColumn;
    @FXML private CheckBox trainingCheckBox;
    @FXML private ComboBox<Trainer> trainerComboBox;
    @FXML private VBox racketReservationVBox;
    @FXML private CheckBox racketCheckBox;
    @FXML private ComboBox<Racket> racketComboBox;
    @FXML private TextArea commentsTextArea;
    @FXML private Label totalPriceLabel;
    @FXML private Button confirmButton;
    @FXML private Label trainerLabel;
    @FXML private Label trainerMiscLabel;

    private final List<TableColumn<Court, Boolean>> hourColumns = new ArrayList<>();

    private final WeakAdapter weakAdapter = new WeakAdapter();

    @FXML
    protected void initialize() {
        if (SessionData.clientProperty().getValue() == null)
            throw new RuntimeException("Client not set for reservation UC.");

        // === AVAILABILITY TABLE ===
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);

        availabilityTable.setEditable(true);

        courtNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        courtNumberColumn.setCellFactory(column -> new ButtonTableCell());

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerHour"));
        priceColumn.setCellFactory(list -> new MoneyFormatCell());
        priceColumn.setText(NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + "/1h");

        availabilityTable.setRowFactory(tableView -> {
            TableRow<Court> row = new TableRow<>();
            row.disableProperty().bind(SessionData.courtProperty().isNotNull().and(SessionData.courtProperty().isNotEqualTo(row.itemProperty())));
            weakAdapter.<Boolean>addChangeListener(row.disabledProperty(), (observable, oldValue, newValue) -> {
                if (newValue != null && newValue) row.getStyleClass().add("row-disabled");
                else row.getStyleClass().remove("row-disabled");
            });
            return row;
        });
        // === END OF AVAILABILITY TABLE ===

        weakAdapter.<Court>addChangeListener(SessionData.courtProperty(), (observable, oldValue, newValue) -> {
            if (newValue == null) {
                // prepare for new court selection
                SessionData.courtProperty().unbind();
                return;
            }

            // unset court when all hours are unmarked
            SessionData.courtProperty().bind(Bindings.createObjectBinding(() -> {
                if (newValue.getMarkedHours().values().stream().anyMatch(BooleanExpression::getValue)) return newValue;
                return null;
            }, newValue.getMarkedHours().values().toArray(new BooleanProperty[0])));

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

        });


        // === RACKET RESERVATION ===
        weakAdapter.<Court>addChangeListener(SessionData.courtProperty(), (observable, oldValue, newValue) -> {
            if (newValue == null) {
                racketCheckBox.setSelected(false);
            }
        });

        Tooltip racketVBoxTooltip = new Tooltip("Wymagane wybranie godziny rezerwacji");
        racketVBoxTooltip.setShowDelay(javafx.util.Duration.seconds(0.1));
        racketReservationVBox.setOnMouseEntered(e -> {
            if (SessionData.courtProperty().getValue() == null) Tooltip.install(racketReservationVBox, racketVBoxTooltip);
        });
        racketReservationVBox.setOnMouseExited(e -> Tooltip.uninstall(racketReservationVBox, racketVBoxTooltip));
        racketComboBox.setConverter(new RacketStringConverter());

        SessionData.racketProperty().bind(Bindings.createObjectBinding(() -> {
            if (racketCheckBox.isSelected()) return racketComboBox.getValue();
            return null;
        }, racketCheckBox.selectedProperty(), racketComboBox.valueProperty()));

        racketReservationControlsRefresh();

        weakAdapter.<Racket>addChangeListener(racketComboBox.getSelectionModel().selectedItemProperty(), (observable, oldValue, newValue) -> {
            if (newValue == null) return;
            var court = SessionData.courtProperty().getValue();
            if (court == null) return;
            if (!newValue.isAvailable(SessionData.reservationStartProperty().get(), SessionData.reservationDurationProperty().get())) {
                racketComboBox.getSelectionModel().select(oldValue);
            }
        });

        racketComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Racket item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(new RacketStringConverter().toString(item));
                    var court = SessionData.courtProperty().getValue();
                    if (court != null) {
                        itemProperty().unbind();
                        disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            var racket = this.itemProperty().getValue();
                            if (racket == null) return false;
                            if (SessionData.courtProperty().getValue() == null) return false;
                            return !racket.isAvailable(SessionData.reservationStartProperty().get(), SessionData.reservationDurationProperty().get());
                        }, court.getMarkedHours().values().toArray(new BooleanProperty[0])));
                    } else {
                        disableProperty().unbind();
                        setDisable(false);
                    }
                }
            }
        });

        weakAdapter.<Boolean>addChangeListener(racketCheckBox.selectedProperty(), (observable, oldValue, newValue) -> {
            racketCheckBox.getStyleClass().remove("marked-racket-box");

            if (newValue) {
                racketCheckBox.getStyleClass().add("marked-racket-box");
                if (SessionData.courtProperty().getValue() == null) {
                    racketCheckBox.setSelected(false);
                    return;
                }

                var rackets = DBController.INSTANCE.getRackets();
                var racketsToday = rackets.stream().filter(racket -> racket.isAvailable(datePicker.getValue())).toList();
                var racketsAtTime = racketsToday.stream().filter(racket -> racket.isAvailable(
                        SessionData.reservationStartProperty().getValue(),
                        SessionData.reservationDurationProperty().getValue())).toList();

                if (racketsAtTime.isEmpty()) {
                    if (racketsToday.isEmpty()) {
                        racketComboBox.getItems().clear();

                        racketComboBox.setPromptText("Brak dostępnych rakiet");
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Brak dostępnych rakiet");
                        alert.setHeaderText("Brak dostępnych rakiet");
                        alert.setContentText("Wybranego dnia wszystkie rakiety są już wypożyczone.");
                        alert.showAndWait();

                        racketCheckBox.selectedProperty().unbind();
                        racketCheckBox.setSelected(false);
                        racketCheckBox.disableProperty().unbind();
                        racketCheckBox.setDisable(true);
                    } else {
                        racketComboBox.getItems().setAll(racketsToday);

                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Brak dostępnych rakiet");
                        alert.setHeaderText("Brak dostępnych rakiet");
                        alert.setContentText("Wszystkie rakiety są obecnie wypożyczone w wybranych godzinach.");

                        alert.showAndWait();

                        racketCheckBox.selectedProperty().unbind();
                        racketCheckBox.setSelected(false);
                    }
                }

                racketComboBox.getItems().setAll(racketsToday);
                if (!racketComboBox.isDisabled()) {
                    if (racketsAtTime.size() > 0) racketComboBox.getSelectionModel().select(racketsAtTime.get(0));
                    else racketComboBox.getSelectionModel().selectFirst();
                }
            } else {
                racketComboBox.getItems().clear();
            }
        });

        // === END OF RACKET RESERVATION ===

        // bind price
        totalPriceLabel.textProperty().bind(Bindings.createStringBinding(
            () -> NumberFormat.getCurrencyInstance().format(SessionData.getTotalPrice()),
            SessionData.courtProperty(),
            SessionData.reservationStartProperty(),
            SessionData.reservationDurationProperty(),
            SessionData.racketProperty(),
            SessionData.trainerProperty()));

        weakAdapter.<LocalDate>addChangeListener(datePicker.valueProperty(), (observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (DBController.INSTANCE.getCourts().stream().noneMatch(c -> c.anyAvailable(newValue))) {
                datePicker.setValue(oldValue);
            } else {
                // On valid date change:
                refreshAvailabilityTable();
                trainerReservationControlsRefresh();
                racketReservationControlsRefresh();
            }
        });

        datePickerSetup();

        // === TRAINER RESERVATION ===
        trainerComboBox.setConverter(new TrainerStringConverter());

        trainerReservationControlsRefresh();

        weakAdapter.addChangeListener(trainingCheckBox.selectedProperty(), trainingCheckBoxSelectionListener());

        weakAdapter.<Trainer>addChangeListener(trainerComboBox.getSelectionModel().selectedItemProperty(), (observable, oldValue, newValue) -> {
            if (newValue == null) return;
            var court = SessionData.courtProperty().getValue();
            if (court == null) return;
            if (!newValue.isAvailable(SessionData.reservationStartProperty().get(), SessionData.reservationDurationProperty().get())) {
                trainerComboBox.getSelectionModel().select(oldValue);
            }
        });

        trainerComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Trainer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(new TrainerStringConverter().toString(item));
                    var court = SessionData.courtProperty().getValue();
                    if (court != null) {
                        itemProperty().unbind();
                        disableProperty().bind(Bindings.createBooleanBinding(() -> {
                            var trainer = this.itemProperty().getValue();
                            if (trainer == null) return false;
                            if (SessionData.courtProperty().getValue() == null) return false;
                            return !trainer.isAvailable(SessionData.reservationStartProperty().get(), SessionData.reservationDurationProperty().get());
                        }, court.getMarkedHours().values().toArray(new BooleanProperty[0])));
                    } else {
                        disableProperty().unbind();
                        setDisable(false);
                    }
                }
            }
        });
        // === END OF TRAINER RESERVATION ===

        commentsTextArea.disableProperty().bind(datePicker.valueProperty().isNull());
        SessionData.commentProperty().bind(commentsTextArea.textProperty());

        commentsTextArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                commentsTextArea.getParent().requestFocus();
                commentsTextArea.setText(commentsTextArea.getText().strip());
            }
        });

        confirmButton.disableProperty().bind(SessionData.courtProperty().isNull());

        datePicker.show();
    }

    private void trainerReservationControlsRefresh() {
        trainerComboBox.getItems().clear();

        trainingCheckBox.disableProperty().unbind();
        trainingCheckBox.disableProperty().bind(datePicker.valueProperty().isNull());

        trainerComboBox.disableProperty().unbind();
        trainerComboBox.disableProperty().bind(trainingCheckBox.selectedProperty().not());

        SessionData.trainerProperty().unbind();
        SessionData.trainerProperty().bind(Bindings.when(trainingCheckBox.selectedProperty())
                .then(trainerComboBox.valueProperty())
                .otherwise((Trainer) null));

        trainerLabel.textProperty().unbind();
        trainerLabel.textProperty().bind(SessionData.trainerProperty().map(t -> new TrainerStringConverter().toString(t)));

        trainerMiscLabel.visibleProperty().unbind();
        trainerMiscLabel.visibleProperty().bind(trainerLabel.textProperty().map(aString -> !aString.isEmpty()));
    }

    @NotNull
    private ChangeListener<Boolean> trainingCheckBoxSelectionListener() {
        return (observable, oldValue, newValue) -> {
            trainingCheckBox.getStyleClass().remove("marked-training-box");
            if (newValue) {
                trainingCheckBox.getStyleClass().add("marked-training-box");
                var selected = trainerComboBox.getSelectionModel().getSelectedItem();

                var trainers = DBController.INSTANCE.getTrainers();
                var availableTrainers = trainers.stream().filter(t -> t.isAvailable(datePicker.getValue())).toList();

                var court = SessionData.courtProperty().getValue();
                var availableTrainersForMarkedHours = court != null ?
                        trainers.stream().filter(t ->
                                t.isAvailable(
                                        SessionData.reservationStartProperty().get(),
                                        SessionData.reservationDurationProperty().get())).toList()
                        : availableTrainers;

                if (availableTrainers.isEmpty()) {

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
                            if (court != null)
                                court.getMarkedHours().values().forEach(v -> v.set(false));
                            trainingCheckBox.setSelected(false);
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
                } else if (court != null && availableTrainersForMarkedHours.isEmpty()) {
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
                            court.getMarkedHours().values().forEach(v -> v.set(false));
                        } else if (result.get() == changeUC) {
                            cancelReservationProcess();
                        } else if (result.get() == resignFromTraining) {
                            trainingCheckBox.selectedProperty().unbind();
                            trainingCheckBox.setSelected(false);
                        } else throw new RuntimeException("Unexpected button type");
                    }
                }

                trainerComboBox.getItems().setAll(availableTrainers);
                if (!trainerComboBox.getItems().isEmpty()) {
                    if (trainerComboBox.getItems().contains(selected) && availableTrainersForMarkedHours.contains(selected))
                        trainerComboBox.getSelectionModel().select(selected);
                    else {
                        trainerComboBox.getSelectionModel().select(
                                availableTrainersForMarkedHours.stream().findFirst().orElse(
                                        availableTrainers.stream().findFirst().orElse(null)));
                    }
                }
            }
        };
    }

    private void racketReservationControlsRefresh() {
        racketCheckBox.disableProperty().unbind();
        racketCheckBox.disableProperty().bind(SessionData.courtProperty().isNull());

        racketComboBox.disableProperty().unbind();
        racketComboBox.disableProperty().bind(racketCheckBox.selectedProperty().not());
    }

    public void showReservationSummary() {
        if (SessionData.getSummaryScene() == null) {
            SessionData.setSummaryScene(Util.changeScene("reservation-summary.fxml", -1, -1));
        }
        else Util.changeScene(SessionData.getSummaryScene());
    }

    @FXML
    public void cancelReservationProcess() {
        SessionData.cancel();
        weakAdapter.dispose();
        Util.changeScene("start.fxml");
    }

    private void datePickerSetup() {
        // disable datepicker if any checkbox is marked:
        BooleanBinding anyHourOrTrainingOrRacketSelected = Bindings.createBooleanBinding(() ->
                SessionData.courtProperty().getValue() != null || trainingCheckBox.isSelected() || racketCheckBox.isSelected(),
                SessionData.courtProperty(), trainingCheckBox.selectedProperty(), racketCheckBox.selectedProperty());
        datePicker.disableProperty().bind(anyHourOrTrainingOrRacketSelected);

        Tooltip datePickerCloudTooltip = new Tooltip("Brak dostępnych kortów");
        datePickerCloudTooltip.setShowDelay(javafx.util.Duration.seconds(0.1));

        // styling disabled dates
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
            hourColumn.setMinWidth(-1);
//            hourColumn.setPrefWidth(50);
            hourColumn.setMaxWidth(5000);

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
        weakAdapter.<Boolean>addChangeListener(cell.itemProperty(), (observable, oldValue, newValue) -> {
            Court rowCourt = cell.getTableRow().getItem();
            if (rowCourt == null) return;

            // set current court if it's not set yet after initialization
            if (oldValue != null && newValue && SessionData.courtProperty().getValue() == null)
                SessionData.courtProperty().set(rowCourt);

            // bind only once
            if (cell.disableProperty().isBound()) return;

            // getting previous and next cell property values (null property value if there is no previous/next cell)
            var currentHourColumnIndex = hourColumns.indexOf(column);
            BooleanProperty previousCellValueProperty;
            BooleanProperty nextCellValueProperty;
            if (currentHourColumnIndex == 0) {
                previousCellValueProperty = new SimpleBooleanProperty();
                previousCellValueProperty.setValue(null);
            } else
                previousCellValueProperty = rowCourt.getMarkedHours().values().toArray(new BooleanProperty[0])[currentHourColumnIndex - 1];
            if (currentHourColumnIndex == hourColumns.size() - 1) {
                nextCellValueProperty = new SimpleBooleanProperty();
                nextCellValueProperty.setValue(null);
            } else
                nextCellValueProperty = rowCourt.getMarkedHours().values().toArray(new BooleanProperty[0])[currentHourColumnIndex + 1];

            BooleanBinding disableAndStyleBinding = Bindings.createBooleanBinding(() -> {
                        cell.getStyleClass().remove("disabled-hour");
                        cell.getStyleClass().remove("unavailable-hour");
                        cell.getStyleClass().remove("trainer-available");
                        cell.getStyleClass().remove("racket-available");

                        if (cell.getTableRow().getItem() == null || cell.getItem() == null) return false;

                        var time = (new HourColumnHeaderStrConv().fromString(column.getText())).atDate(datePicker.getValue());
                        if (!rowCourt.isAvailable(time, Duration.ofHours(1))) {
                            cell.getStyleClass().add("unavailable-hour");
                            return true;
                        }

//                trainerComboBox.cel

                        Trainer trainer = SessionData.trainerProperty().getValue();
                        if (trainer != null) {
                            if (trainer.isAvailable(time, Duration.ofHours(1))) {
                                cell.getStyleClass().add("trainer-available");
                            } else {
                                cell.getStyleClass().add("disabled-hour");
                                return true;
                            }
                        }

                        Racket racket = SessionData.racketProperty().getValue();
                        if (racket != null) {
                            if (racket.isAvailable(time, Duration.ofHours(1))) {
                                cell.getStyleClass().add("racket-available");
                            } else {
                                cell.getStyleClass().add("disabled-hour");
                                return true;
                            }
                        }
                        if (SessionData.courtProperty().getValue() == null) {
                            return false;
                        }

                        if (!SessionData.courtProperty().get().equals(rowCourt)) {
                            cell.getStyleClass().add("disabled-hour");
                            return true;
                        }

                        // assure that all marked hours are neighbours
                        int markedHours = rowCourt.getMarkedHours().values().stream().mapToInt(value -> value.get() ? 1 : 0).sum();
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
                    }, SessionData.courtProperty(), SessionData.trainerProperty(),
                    SessionData.racketProperty(), racketCheckBox.selectedProperty(),
                    previousCellValueProperty, nextCellValueProperty,
                    datePicker.valueProperty(), availabilityTable.onScrollToProperty(),
                    availabilityTable.onScrollToColumnProperty());

            cell.disableProperty().bind(disableAndStyleBinding);

        });

        return cell;
    }
}
