package mas.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class Util {

    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end1 = start1.plus(duration1);
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, Duration duration1,
                                        LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime end1 = start1.plus(duration1);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, Duration duration2) {
        LocalDateTime end2 = start2.plus(duration2);
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }


}