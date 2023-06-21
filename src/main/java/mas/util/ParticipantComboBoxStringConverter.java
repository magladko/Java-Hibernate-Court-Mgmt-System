package mas.util;

import javafx.util.StringConverter;
import mas.entity.Person;
import org.hibernate.TypeMismatchException;

/**
 * A custom StringConverter implementation for converting Person objects to strings and vice versa.
 */
public class ParticipantComboBoxStringConverter extends StringConverter<Person> {

    /**
     * Converts a Person object to its string representation.
     *
     * @param person the Person object to convert
     * @return the string representation of the Person object
     * @throws TypeMismatchException if the Person object is not a participant nor a client
     */
    @Override
    public String toString(Person person) {
        if (person == null) {
            return "";
        }

        if (person.getPersonTypes().contains(Person.PersonType.CLIENT)) {
            return "Klient: " + person.getName() + " " + person.getSurname();
        } else if (person.getPersonTypes().contains(Person.PersonType.PARTICIPANT)) {
            String result = "Uczestnik: " + person.getName() + " " + person.getSurname();
            if (person.getBirthday() != null) {
                result += " (" + person.getBirthday() + ")";
            }
            return result;
        } else {
            throw new TypeMismatchException("Person is not a participant nor a client");
        }
    }

    /**
     * Converts a string to a Person object (not supported).
     *
     * @param string the string to convert
     * @return the converted Person object
     * @throws UnsupportedOperationException because this operation is not supported
     */
    @Override
    public Person fromString(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
