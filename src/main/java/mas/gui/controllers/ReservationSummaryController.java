package mas.gui.controllers;

import jakarta.persistence.RollbackException;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mas.entity.*;
import mas.util.DBController;
import mas.util.ParticipantComboBoxStringConverter;
import mas.util.SessionData;
import mas.util.Util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ReservationSummaryController {
    @FXML
    private ComboBox<Person> participantComboBox;
    @FXML
    private Label dateValueLabel;
    @FXML
    private Label hourValueLabel;
    @FXML
    private Label durationValueLabel;
    @FXML
    private Label courtNrValueLabel;
    @FXML
    private Label courtPricePerHourLabel;
    @FXML
    private Label courtPricePerHourValueLabel;
    @FXML
    private Label heatingSurchargeLabel;
    @FXML
    private Label heatingSurchargeValueLabel;
    @FXML
    private Label reservationPriceValueLabel;
    @FXML
    private Label trainerDataLabel;
    @FXML
    private Label trainerDataValueLabel;
    @FXML
    private Label trainerPriceValueLabel;
    @FXML
    private Label racketDataLabel;
    @FXML
    private Label racketDataValueLabel;
    @FXML
    private Label racketPriceValueLabel;
    @FXML
    private Label totalPriceValueLabel;

    public void initialize() {
        validateData();
        participantChoiceSetup();
        courtReservationDetailsSetup();
        heatingSurchargeDetailsSetup();
        trainingDetailsSetup();
        racketDetailsSetup();

        totalPriceValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> NumberFormat.getCurrencyInstance().format(SessionData.getTotalPrice()),
                SessionData.courtProperty(),
                SessionData.reservationStartProperty(),
                SessionData.reservationDurationProperty(),
                SessionData.trainerProperty(),
                SessionData.racketProperty()));
    }

    private void racketDetailsSetup() {
        racketDataValueLabel.textProperty().bind(SessionData.racketProperty()
                .map(racket -> racket.getManufacturer() + ", " + new DecimalFormat("#,###0").format(racket.getWeight()) + "g")
                .orElse("niewybrano"));
        racketPriceValueLabel.textProperty().bind(SessionData.racketProperty()
                .map(racket -> NumberFormat.getCurrencyInstance().format(racket.getPricePerHour()))
                .orElse(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO)));
        racketDataValueLabel.disableProperty().bind(racketDataValueLabel.textProperty().isEqualTo("niewybrano"));
        racketPriceValueLabel.disableProperty().bind(racketDataValueLabel.disableProperty());
        racketDataLabel.disableProperty().bind(racketDataValueLabel.disableProperty());
    }

    private void trainingDetailsSetup() {
        trainerDataValueLabel.textProperty().bind(SessionData.trainerProperty()
                .map(trainer -> trainer.getName() + " " + trainer.getSurname())
                .orElse("niewybrano"));
        trainerPriceValueLabel.textProperty().bind(SessionData.trainerProperty()
                .map(trainer -> NumberFormat.getCurrencyInstance().format(trainer.getPricePerHour()))
                .orElse(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO)));

        trainerDataValueLabel.disableProperty().bind(trainerDataValueLabel.textProperty().isEqualTo("niewybrano"));
        trainerPriceValueLabel.disableProperty().bind(trainerDataValueLabel.disableProperty());
        trainerDataLabel.disableProperty().bind(trainerDataValueLabel.disableProperty());
    }

    private void heatingSurchargeDetailsSetup() {
        heatingSurchargeValueLabel.textProperty().bind(SessionData.courtProperty().map(court -> {
            String zero = NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO);
            if (SessionData.reservationStartProperty().get() == null) return "Start not set!";
            if (court instanceof CourtRoofed)
                return NumberFormat.getCurrencyInstance().format(CourtRoofed.getHeatingSurcharge(SessionData.reservationStartProperty().get().toLocalDate()));
            return zero;
        }).orElse(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO)));

        heatingSurchargeValueLabel.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            var court = SessionData.courtProperty().getValue();
            var start = SessionData.reservationStartProperty().getValue();
            if (court == null || start == null) return true;

            var text = heatingSurchargeValueLabel.textProperty().getValue();
            if (text == null) return true;
            return text.equals(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO));
        }, SessionData.courtProperty(), SessionData.reservationStartProperty(), heatingSurchargeValueLabel.textProperty()));
        heatingSurchargeLabel.disableProperty().bind(heatingSurchargeValueLabel.disabledProperty());
    }

    private void courtReservationDetailsSetup() {
        dateValueLabel.textProperty().bind(SessionData.reservationStartProperty().map(v -> v.toLocalDate().toString()));

        hourValueLabel.textProperty().bind(SessionData.reservationStartProperty().map(v -> v.toLocalTime().toString()));

        durationValueLabel.textProperty().bind(SessionData.reservationDurationProperty().map(d -> String.format("%02d:%02dh", d.toHours(), d.toMinutesPart())));

        courtNrValueLabel.textProperty().bind(SessionData.courtProperty().map(v -> v.getNumber().toString()));

        courtPricePerHourValueLabel.textProperty().bind(SessionData.courtProperty().map(court -> {
            if (court instanceof CourtRoofed) return NumberFormat.getCurrencyInstance().format(CourtRoofed.getPricePerHour());
            if (court instanceof CourtUnroofed) return NumberFormat.getCurrencyInstance().format(CourtUnroofed.getPricePerHour());
            throw new RuntimeException("Unknown court type");
        }));
        courtPricePerHourLabel.setText("Kort (" + NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + "/h):");

        reservationPriceValueLabel.textProperty().bind(SessionData.reservationDurationProperty().map(duration -> {
            var court = SessionData.courtProperty().getValue();
            if (court == null) return "Court not set!";
            var hours = duration.toHours();
            BigDecimal pricePerHour;
            if (court instanceof CourtRoofed) pricePerHour = CourtRoofed.getPricePerHour();
            else if (court instanceof CourtUnroofed) pricePerHour = CourtUnroofed.getPricePerHour();
            else throw new IllegalStateException("Unknown court type");
            return NumberFormat.getCurrencyInstance().format(pricePerHour.multiply(BigDecimal.valueOf(hours)));
        }));
    }

    private void participantChoiceSetup() {
        participantComboBox.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if (SessionData.clientProperty().getValue() == null) return FXCollections.emptyObservableList();

            Person[] participantsArr = SessionData.clientProperty().getValue().getOwnedParticipants().toArray(new Person[0]);
            var list = FXCollections.observableArrayList(participantsArr);
            list.add(0, SessionData.clientProperty().getValue());
            return list;
        }, SessionData.clientProperty()));
        participantComboBox.getSelectionModel().selectFirst();
        participantComboBox.setConverter(new ParticipantComboBoxStringConverter());
    }

    private static void validateData() {
        if (SessionData.clientProperty().getValue() == null) throw new RuntimeException("Client not set");
        if (SessionData.courtProperty().getValue() == null) throw new RuntimeException("Court not set");
        if (SessionData.reservationStartProperty() == null) throw new RuntimeException("Reservation start not set");
        if (SessionData.reservationDurationProperty() == null) throw new RuntimeException("Reservation duration not set");
    }

    /**
     * Confirms the reservation by saving it to the database and displaying a confirmation message.
     * If an error occurs, an error message is shown instead.
     */
    @FXML
    public void confirmReservation() {
        try {
            var participant = SessionData.participantProperty().getValue() == null ?
                    SessionData.clientProperty().getValue() :
                    SessionData.participantProperty().getValue();

            DBController.INSTANCE.getEm().getTransaction().begin();
            if (SessionData.trainerProperty().getValue() != null) {
                var training = Training.makeReservation(
                        SessionData.clientProperty().getValue(),
                        participant,
                        SessionData.trainerProperty().getValue(),
                        SessionData.courtProperty().getValue(),
                        SessionData.reservationStartProperty().getValue(),
                        SessionData.reservationDurationProperty().getValue()
                );

                if (SessionData.racketProperty().getValue() != null)
                    training.addEquipment(SessionData.racketProperty().getValue());

                DBController.INSTANCE.getEm().persist(training);
            } else {
                var reservation = Reservation.makeReservation(
                        SessionData.reservationStartProperty().getValue(),
                        SessionData.reservationDurationProperty().getValue(),
                        SessionData.courtProperty().getValue(),
                        SessionData.racketProperty().getValue(),
                        SessionData.clientProperty().getValue(),
                        participant,
                        SessionData.commentProperty().getValue()
                );
                DBController.INSTANCE.getEm().persist(reservation);
            }

            DBController.INSTANCE.getEm().getTransaction().commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Potwierdzenie rezerwacji");
            alert.setHeaderText("Rezerwacja została potwierdzona");
            alert.setContentText("Rezerwacja została pomyślnie zarejestrowana w systemie.");
            alert.showAndWait();
        } catch (RollbackException | IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Wystąpił błąd podczas rezerwacji");
            alert.setContentText("Wystąpił błąd podczas rezerwacji. Spróbuj ponownie.");
            alert.showAndWait();
        }

        SessionData.cancel();
        Util.changeScene("start.fxml");
    }

    /**
     * Navigates back to the court reservation scene.
     * Throws an exception if the court reservation scene is not set.
     */
    @FXML
    public void goBackToReservation() {
        if (SessionData.getCourtReservationScene() == null)
            throw new RuntimeException("Court reservation scene not set");
        Util.changeScene(SessionData.getCourtReservationScene());
    }
}
