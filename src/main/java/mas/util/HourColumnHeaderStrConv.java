package mas.util;

import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is a custom StringConverter for converting LocalTime objects to strings and vice versa.
 * It is used in the Tennis Courts management application for formatting hour values in the UI.
 */
public class HourColumnHeaderStrConv extends StringConverter<LocalTime> {

    /**
     * Converts a LocalTime object to a string representation in the format "HH:mm".
     *
     * @param time the LocalTime object to convert
     * @return a string representation of the LocalTime object in "HH:mm" format
     */
    @Override
    public String toString(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Converts a string representation in the format "HH:mm" to a LocalTime object.
     *
     * @param string the string representation to convert
     * @return a LocalTime object parsed from the input string
     */
    @Override
    public LocalTime fromString(String string) {
        return LocalTime.parse(string, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
