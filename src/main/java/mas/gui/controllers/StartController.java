package mas.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import mas.CourtReservationApp;
import mas.entity.Person;
import mas.util.DBController;
import mas.util.SessionData;
import mas.util.Util;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button seedDBButton;
    @FXML private Button runUseCaseButton;
    @FXML private ChoiceBox<Person> clientChoiceBox;

    @FXML
    public void initialize() {
        clientChoiceBox.getItems().setAll(DBController.INSTANCE.getEm().createQuery("from Person", Person.class).getResultStream().filter(p -> p.getPersonTypes().contains(Person.PersonType.Client)).toList());
        clientChoiceBox.getSelectionModel().selectFirst();
        clientChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Person object) {
                if (object == null) return "";
                return object.getName() + " " + object.getSurname() + " participants: " + object.getOwnedParticipants().size();
            }

            @Override
            public Person fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        SessionData.clientProperty().bind(clientChoiceBox.getSelectionModel().selectedItemProperty());

        runUseCaseButton.disableProperty().bind(clientChoiceBox.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    public void seedDbClick() {
        try {
            DBController.INSTANCE.seedDb();
            clientChoiceBox.getItems().setAll(DBController.INSTANCE.getEm().createQuery("from Person", Person.class).getResultStream().filter(p -> p.getPersonTypes().contains(Person.PersonType.Client)).toList());
            clientChoiceBox.getSelectionModel().selectFirst();
        } catch(Exception e) {
            seedDBButton.setDisable(true);
            messageLabel.setText("Seeding failed. Database might already contain data.");
            return;
        }
        seedDBButton.setDisable(true);
        messageLabel.setText("Seed method initialized.");
    }

    @FXML
    public void runUseCase() {
        SessionData.setCourtReservationScene(Util.changeScene("court-reservation.fxml"));
    }

}
