package mas.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import mas.entity.CourtRoofed;
import mas.entity.CourtUnroofed;
import mas.entity.Person;
import mas.util.ParticipantComboBoxStringConverter;
import mas.util.SessionData;
import mas.util.Util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ReservationSummaryController {
    @FXML public ComboBox<Person> participantComboBox;
    @FXML public GridPane rootGridPane;
    @FXML public Label dateValueLabel;
    @FXML public Label hourValueLabel;
    @FXML public Label durationValueLabel;
    @FXML public Label courtNrValueLabel;
    @FXML public Label courtPricePerHourLabel;
    @FXML public Label courtPricePerHourValueLabel;
    @FXML public Label heatingSurchargeLabel;
    @FXML public Label heatingSurchargeValueLabel;
    @FXML public Label reservationPriceValueLabel;
    @FXML public Label trainerDataLabel;
    @FXML public Label trainerDataValueLabel;
    @FXML public Label trainerPriceValueLabel;
    @FXML public Label racketDataLabel;
    @FXML public Label racketDataValueLabel;
    @FXML public Label racketPriceValueLabel;
    @FXML public Label totalPriceValueLabel;

    public void initialize() {
        if (SessionData.clientProperty().getValue() == null) throw new RuntimeException("Client not set");
        if (SessionData.courtProperty().getValue() == null) throw new RuntimeException("Court not set");
        if (SessionData.reservationStartProperty() == null) throw new RuntimeException("Reservation start not set");
        if (SessionData.reservationDurationProperty() == null) throw new RuntimeException("Reservation duration not set");



        participantComboBox.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            if (SessionData.clientProperty().getValue() == null) return FXCollections.emptyObservableList();

            Person[] participantsArr = SessionData.clientProperty().getValue().getOwnedParticipants().toArray(new Person[0]);
            var list = FXCollections.observableArrayList(participantsArr);
            list.add(0, SessionData.clientProperty().getValue());
            return list;
        }, SessionData.clientProperty()));
        participantComboBox.getSelectionModel().selectFirst();
        participantComboBox.setConverter(new ParticipantComboBoxStringConverter());

        dateValueLabel.textProperty().bind(SessionData.reservationStartProperty().map(v -> v == null ? "" : v.toLocalDate().toString()));

        hourValueLabel.textProperty().bind(SessionData.reservationStartProperty().map(v -> v == null ? "" : v.toLocalTime().toString()));

        durationValueLabel.textProperty().bind(SessionData.reservationDurationProperty().map(d -> d == null ? "" : String.format("%02d:%02dh", d.toHours(), d.toMinutesPart())));

        courtNrValueLabel.textProperty().bind(SessionData.courtProperty().map(v -> v == null ? "" : v.getNumber().toString()));

        courtPricePerHourValueLabel.textProperty().bind(SessionData.courtProperty().map(court -> {
            if (court == null) return "";
            if (court instanceof CourtRoofed) return NumberFormat.getCurrencyInstance().format(CourtRoofed.getPricePerHour());
            if (court instanceof CourtUnroofed) return NumberFormat.getCurrencyInstance().format(CourtUnroofed.getPricePerHour());
            throw new RuntimeException("Unknown court type");
        }));
        courtPricePerHourLabel.setText("Kort (" + NumberFormat.getCurrencyInstance().getCurrency().getSymbol() + "/h):");

        heatingSurchargeValueLabel.textProperty().bind(SessionData.courtProperty().map(court -> {
            if (court == null) return "";
            if (SessionData.reservationStartProperty().get() == null) return "";
            if (court instanceof CourtRoofed)
                return NumberFormat.getCurrencyInstance().format(CourtRoofed.getHeatingSurcharge(SessionData.reservationStartProperty().get().toLocalDate()));
            return "";
        }));
        heatingSurchargeLabel.visibleProperty().bind(heatingSurchargeValueLabel.textProperty().isNotEmpty());


        reservationPriceValueLabel.textProperty().bind(SessionData.reservationDurationProperty().map(duration -> {
            if (duration == null) return "";
            if (SessionData.courtProperty().getValue() == null) return "";
            var hours = duration.toHours();
            BigDecimal pricePerHour;
            if (SessionData.courtProperty().getValue() instanceof CourtRoofed) pricePerHour = CourtRoofed.getPricePerHour();
            else if (SessionData.courtProperty().getValue() instanceof CourtUnroofed) pricePerHour = CourtUnroofed.getPricePerHour();
            else throw new RuntimeException("Unknown court type");
            return NumberFormat.getCurrencyInstance().format(pricePerHour.multiply(BigDecimal.valueOf(hours)));
        }));

        trainerDataValueLabel.textProperty().bind(SessionData.trainerProperty().map(trainer -> {
            if (trainer == null) return "";
            return trainer.getName() + " " + trainer.getSurname();
        }));
        trainerPriceValueLabel.textProperty().bind(SessionData.trainerProperty().map(trainer -> {
            if (trainer == null) return "";
            return NumberFormat.getCurrencyInstance().format(trainer.getPricePerHour());
        }));
        trainerDataLabel.visibleProperty().bind(SessionData.trainerProperty().isNotNull());

        racketDataValueLabel.textProperty().bind(SessionData.racketProperty().map(racket -> {
            if (racket == null) return "";
            return racket.getManufacturer() + ", " + new DecimalFormat("#,###0").format(racket.getWeight()) + "g";
        }));
        racketPriceValueLabel.textProperty().bind(SessionData.racketProperty().map(racket -> {
            if (racket == null) return "";
            return NumberFormat.getCurrencyInstance().format(racket.getPricePerHour());
        }));
        racketDataLabel.visibleProperty().bind(SessionData.racketProperty().isNotNull());

        totalPriceValueLabel.textProperty().bind(Bindings.createStringBinding(
                () -> NumberFormat.getCurrencyInstance().format(SessionData.getTotalPrice().orElse(BigDecimal.ZERO)),
                SessionData.courtProperty(),
                SessionData.reservationDurationProperty(),
                SessionData.trainerProperty(),
                SessionData.racketProperty()));

    }

    @FXML
    public void goBackToReservation() {
        if (SessionData.getCourtReservationScene() == null)
            throw new RuntimeException("Court reservation scene not set");
        Util.changeScene(SessionData.getCourtReservationScene());
    }

    // Define your event handlers and other methods
}
