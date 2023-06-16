package mas.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import mas.CourtReservationApp;
import mas.entity.Court;
import mas.entity.CourtRoofed;
import mas.entity.CourtUnroofed;

public class ButtonTableCell extends TableCell<Court, Integer> {
    private final Button button;

    public ButtonTableCell() {
        this.button = new Button();
        button.textProperty().bind(this.itemProperty().asString());

        this.button.setOnAction(event -> {
            if (this.itemProperty().getValue() == null || this.tableRowProperty().getValue() == null) return;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            Court court = this.getTableRow().getItem();

            alert.setTitle("Informacja o korcie");
            alert.setHeaderText("Kort nr " + (court.getNumber()));
            alert.setContentText(court.getInfoTxt());
            alert.showAndWait();
        });
    }

    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            setGraphic(button);
        }
    }
}
