package mas.util;

import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HourColumnHeaderStrConv extends StringConverter<LocalTime> {

    @Override
    public String toString(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public LocalTime fromString(String string) {
        return LocalTime.parse(string, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
