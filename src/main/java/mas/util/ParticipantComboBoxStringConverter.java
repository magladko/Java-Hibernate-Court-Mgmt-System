package mas.util;

import javafx.util.StringConverter;
import mas.entity.Person;
import org.hibernate.TypeMismatchException;

public class ParticipantComboBoxStringConverter extends StringConverter<Person> {

    @Override
    public String toString(Person person) {
        if (person == null) return "";
        if (person.getPersonTypes().contains(Person.PersonType.Client)) {
            return "Klient: " + person.getName() + " " + person.getSurname();
        } else if (person.getPersonTypes().contains(Person.PersonType.Participant)) {
            String result = "Uczestnik: " + person.getName() + " " + person.getSurname();
            if (person.getBirthday() != null) {
                result += " (" + person.getBirthday() + ")";
            }
            return result;
        } else {
            throw new TypeMismatchException("Person is not a participant nor client");
        }
    }

    @Override
    public Person fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
