package mas.util;

import javafx.util.StringConverter;
import mas.entity.Trainer;

import java.text.NumberFormat;

/**
 * A converter class that converts Trainer objects to Strings and vice versa.
 * The converted String representation includes the trainer's name, surname, qualification, and price per hour.
 * This class is used in the Tennis Courts management application for display and input purposes.
 */
public class TrainerStringConverter extends StringConverter<Trainer> {

    /**
     * Converts a Trainer object to its String representation.
     *
     * @param trainer the Trainer object to be converted
     * @return the String representation of the Trainer object
     */
    @Override
    public String toString(Trainer trainer) {
        if (trainer == null) {
            return null;
        } else {
            return String.format("%s %s - %s - %s",
                    trainer.getName(),
                    trainer.getSurname(),
                    trainer.getQualification(),
                    NumberFormat.getCurrencyInstance().format(trainer.getPricePerHour()));
        }
    }

    /**
     * Converts a String to a Trainer object.
     *
     * @param string the String to be converted
     * @return the Trainer object represented by the String
     */
    @Override
    public Trainer fromString(String string) {
        // Implement if needed
        return null;
    }

}
