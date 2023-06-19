package mas;

import mas.util.Util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
//        String a = "09:00";
//
//        System.out.println(LocalTime.parse(a, DateTimeFormatter.ofPattern("HH:mm")));

        // true: 2023-06-14T10:00PT1H[Training{start=2023-06-24T10:00, duration=PT1H, trainer=1, court=1}]

//        System.out.println(Util.isOverlapping(LocalDateTime.of(2023, 6, 14, 10, 0), Duration.ofHours(1), LocalDateTime.of(2023, 6, 14, 10, 0), Duration.ofHours(1)));
//        List<String> a = new ArrayList<>();

//        a.addAll(null);

//        System.out.println(a);

        System.out.println(LocalDateTime.of(2023, 6, 14, 10, 0)
                .isBefore(LocalDateTime.of(2023, 6, 14, 10, 1)));


    }
}
