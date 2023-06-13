package mas.gui.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import mas.entity.Court;
import mas.entity.Reservation;
import mas.util.DBController;
import mas.util.MoneyFormatCell;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final List<TableColumn<Court, Boolean>> hourColumns = new ArrayList<>();

    @Getter
    private Reservation tempReservationDTO;


    private Map<CheckBoxTableCell<Court, Boolean>, BooleanProperty> checkedCells = new HashMap<>();

    @FXML
    protected void initialize() {
        // Hour columns
        availabilityTable.setDisable(true);
        availabilityTable.setSelectionModel(null);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAvailabilityTable();
        });

        tempReservationDTO = new Reservation();

    }

    public void showReservationSummary() {
        // Implement the logic for showing the reservation summary
    }

    private void updateAvailabilityTable() {

        availabilityTable.getColumns().removeAll(hourColumns);
        availabilityTable.getItems().clear();

        availabilityTable.setEditable(true);

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

            hourColumn.setCellValueFactory(courtBooleanCellDataFeatures -> {
                courtBooleanCellDataFeatures.getValue().getMarkedHours().add(new SimpleBooleanProperty(false));
                return courtBooleanCellDataFeatures.getValue().getMarkedHours()
                        .get(courtBooleanCellDataFeatures.getValue().getMarkedHours().size() - 1);
            });

            // NO DISABLING
//            hourColumn.setCellFactory(column -> new CheckBoxTableCell<>() {
//                @Override
//                public void updateItem(Boolean item, boolean empty) {
//                    System.out.println("UPDATE");
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                        setStyle("");
//                    } else {
//                        var courtAvailable = column.getTableView().getItems().get(getIndex()).isAvailable(LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue()), Duration.ofHours(1));
//                        if (courtAvailable) {
//                            setDisable(false);
//                            // TODO: User can check only hours on one court and only adjacent hours
//
//                            var checkBox = new CheckBox();
//                            checkBox.setSelected(item);
//                            setGraphic(checkBox);
//
////                            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
////                                System.out.println("CHECKED");
////                                if (newValue) {
////                                    // Disable other rows
////                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
////                                        if (i != getIndex()) {
////                                            column.getTableView().getItems().get(i).getDisabledTableRow().setValue(false);
////                                        }
////                                    }
////                                } else {
////                                    // Enable all rows
////                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
////                                        column.getTableView().getItems().get(i).getDisabledTableRow().setValue(false);
////                                    }
////                                }
////                            });
//
//                            setStyle("");
//                        } else {
//                            setDisable(true);
//                            setStyle("-fx-background-color: #ffb4b4;"+ "-fx-opacity: 0.4;");
//                        }
//                    }
//                }
//            });

            // DISABLES, NO GRAPHICAL EFFECTS
            hourColumn.setCellFactory(column -> new CheckBoxTableCell<>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        var courtAvailable = column.getTableView().getItems().get(getIndex()).isAvailable(LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue()), Duration.ofHours(1));
                        if (courtAvailable) {
                            setDisable(false);
                            // TODO: User can check only hours on one court and only adjacent hours

                            var checkBox = new CheckBox();
                            checkBox.setSelected(item);
                            setGraphic(checkBox);

                            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                System.out.println("CHECKED");
                                if (newValue) {
                                    // Disable other rows
                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
                                        if (i != getIndex()) {
                                            column.getTableView().getItems().get(i).getDisabledTableRow().setValue(true);
                                        }
                                    }
                                } else {
                                    // Enable all rows
                                    for (int i = 0; i < column.getTableView().getItems().size(); i++) {
                                        column.getTableView().getItems().get(i).getDisabledTableRow().setValue(false);
                                    }
                                }
                            });

                            // Bind the disable and opacity properties to the disabledTableRow property of the Court
                            var disabledTableRow = column.getTableView().getItems().get(getIndex()).getDisabledTableRow();
                            checkBox.disableProperty().bind(disabledTableRow);
                            setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0);

                            setStyle("");
                        } else {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffb4b4;"+ "-fx-opacity: 0.4;");
                        }
                    }
                }
            });


            // ALMOST AS INTENDED
            hourColumn.setCellFactory(column -> new TableCell<>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        System.out.println("CHECKED");
                        if (newValue) {
                            // Disable other rows
                            for (int i = 0; i < column.getTableView().getItems().size(); i++) {
                                if (i != getIndex()) {
                                    column.getTableView().getItems().get(i).getDisabledTableRow().setValue(true);
                                }
                            }
                        } else {
                            // Enable all rows
                            for (int i = 0; i < column.getTableView().getItems().size(); i++) {
                                column.getTableView().getItems().get(i).getDisabledTableRow().setValue(false);
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        var courtAvailable = column.getTableView().getItems().get(getIndex()).isAvailable(LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue()), Duration.ofHours(1));
                        if (courtAvailable) {
                            setGraphic(checkBox);
                            checkBox.setSelected(item);

                            // Bind the disableProperty() of the checkbox to the disabledTableRow property of the Court
                            var disabledTableRow = column.getTableView().getItems().get(getIndex()).getDisabledTableRow();
                            checkBox.disableProperty().bind(disabledTableRow);
                            setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0);

                            setText(null);
                        } else {
                            setGraphic(null);
                            setText(null);
                            setDisable(true);
                            setStyle("-fx-background-color: #ffb4b4;");
                        }
                    }
                }
            });


            // THROWS SOME ERRORS
//            hourColumn.setCellFactory(column -> new CheckBoxTableCell<>() {
//                @Override
//                public void updateItem(Boolean item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        var courtAvailable = column.getTableView().getItems().get(getIndex()).isAvailable(LocalTime.parse(column.getText(), DateTimeFormatter.ofPattern("HH:mm")).atDate(datePicker.getValue()), Duration.ofHours(1));
//                        if (courtAvailable) {
//                            setDisable(false);
//                            // TODO: User can check only hours on one court and only adjacent hours
//                            setStyle("");
//
//                            // Bind the disableProperty() of the checkbox to the disabledTableRow property of the Court
//                            var disabledTableRow = column.getTableView().getItems().get(getIndex()).getDisabledTableRow();
//                            disableProperty().bind(disabledTableRow);
//                            setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0);
//                        } else {
//                            setDisable(true);
//                            setStyle("-fx-background-color: #ffb4b4;");
//                        }
//                    }
//                }
//            });
//
//            availabilityTable.setRowFactory(column -> new TableRow<>() {
//                @Override
//                public void updateItem(Court item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setStyle("");
//                    } else {
//                        var disabledTableRow = item.getDisabledTableRow();
//                        setOpacity(disabledTableRow.getValue() ? 0.4 : 1.0);
//                    }
//                }
//            });




            hourColumns.add(hourColumn);
            availabilityTable.getColumns().add(hourColumn);
        }

        availabilityTable.itemsProperty().addListener((obs, oldItems, newItems) -> {
            availabilityTable.setRowFactory(tableView -> {
                TableRow<Court> row = new TableRow<>();
//                row.disableProperty().bindBidirectional(tableView.getItems().get(tableView.getSelectionModel().getSelectedIndex()).getDisabledTableRow());
                row.disableProperty().bindBidirectional(tableView.getItems().get(row.getIndex()).getDisabledTableRow());
                return row;
            });
        });


//        availabilityTable.setRowFactory(tableView -> {
//            TableRow<Court> row = new TableRow<>();
////            row.disableProperty().bindBidirectional(tableView.getItems().get(row.getIndex()).getDisabledTableRow());
//            row.disableProperty().bindBidirectional(tableView
//                    .getItems().get(tableView.getSelectionModel().getSelectedIndex()).getDisabledTableRow());
//            return row;
//        });

        availabilityTable.getItems().addAll(
                DBController.INSTANCE.getEm().createQuery("from Court", Court.class).getResultList());

        availabilityTable.setDisable(false);
    }

    // Define your event handlers and other methods

    // Define your CourtAvailability class (used by TableView)
}
