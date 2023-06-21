package mas.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import mas.entity.Person;
import mas.util.DBController;
import mas.util.SessionData;
import mas.util.Util;

/**
 * The StartController class is responsible for managing the starting view of the Tennis Courts management application.
 * It initializes the user interface components, handles database seeding, and runs the selected use case.
 */
public class StartController {

    @FXML
    private Label messageLabel;

    @FXML
    private Button seedDBButton;

    @FXML
    private Button runUseCaseButton;

    @FXML
    private ChoiceBox<Person> clientChoiceBox;

    /**
     * Initializes the user interface components and sets up the event handlers.
     */
    @FXML
    public void initialize() {
        // Populate the client choice box with Person entities filtered by PersonType.CLIENT
        clientChoiceBox.getItems().setAll(DBController.INSTANCE.getEm()
                .createQuery("from Person", Person.class)
                .getResultStream()
                .filter(p -> p.getPersonTypes().contains(Person.PersonType.CLIENT))
                .toList());

        // Select the first item in the client choice box
        clientChoiceBox.getSelectionModel().selectFirst();

        // Set a converter to display client information in the choice box
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

        // Bind the selected client to the clientProperty in SessionData
        SessionData.clientProperty().bind(clientChoiceBox.getSelectionModel().selectedItemProperty());

        // Disable the runUseCaseButton if no client is selected
        runUseCaseButton.disableProperty().bind(clientChoiceBox.getSelectionModel().selectedItemProperty().isNull());
    }

    /**
     * Handles the click event of the seedDBButton.
     * Seeds the database with initial data and updates the client choice box.
     */
    @FXML
    public void seedDbClick() {
        try {
            DBController.INSTANCE.seedDb();

            // Update the client choice box with the latest data
            clientChoiceBox.getItems().setAll(DBController.INSTANCE.getEm()
                    .createQuery("from Person", Person.class)
                    .getResultStream()
                    .filter(p -> p.getPersonTypes().contains(Person.PersonType.CLIENT))
                    .toList());

            // Select the first item in the client choice box
            clientChoiceBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            seedDBButton.setDisable(true);
            messageLabel.setText("Seeding failed. Database might already contain data.");
            return;
        }
        seedDBButton.setDisable(true);
        messageLabel.setText("Seed method initialized.");
    }

    /**
     * Handles the click event of the runUseCaseButton.
     * Switches the scene to the Court Reservation scene.
     */
    @FXML
    public void runUseCase() {
        SessionData.setCourtReservationScene(Util.changeScene("court-reservation.fxml"));
    }

}
