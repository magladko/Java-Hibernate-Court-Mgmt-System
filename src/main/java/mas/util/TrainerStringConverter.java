package mas.util;

import javafx.util.StringConverter;
import mas.entity.Trainer;

import java.text.NumberFormat;

public class TrainerStringConverter extends StringConverter<Trainer> {

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

    @Override
    public Trainer fromString(String string) {
        // Implement if needed
        return null;
    }

}
