package mas.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Exception thrown when a time slot is unavailable for a given object or class.
 */
public class TimeUnavailableException extends RuntimeException {

    /**
     * Constructs a TimeUnavailableException with the specified class, time, and duration.
     *
     * @param clazz    the class of the object that is unavailable
     * @param time     the starting time of the unavailable slot
     * @param duration the duration of the unavailable slot
     */
    public TimeUnavailableException(Class<?> clazz, LocalDateTime time, Duration duration) {
        super("Object of the class %s is unavailable for range: %s to %s"
                .formatted(clazz.getSimpleName(), time, time.plus(duration)));
    }

    /**
     * Constructs a TimeUnavailableException with the specified object, time, and duration.
     *
     * @param obj      the object that is unavailable
     * @param time     the starting time of the unavailable slot
     * @param duration the duration of the unavailable slot
     */
    public TimeUnavailableException(Object obj, LocalDateTime time, Duration duration) {
        super("%s is unavailable for range: %s to %s."
                .formatted(obj.toString(), time, time.plus(duration)));
    }
}