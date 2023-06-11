package mas.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUnavailableException extends RuntimeException {
    public TimeUnavailableException(Class<?> clazz, LocalDateTime time, Duration duration) {
        super("Object of the class %s is unavailable for range: %s to %s"
                      .formatted(clazz.getSimpleName(), time, time.plus(duration)));
    }
}