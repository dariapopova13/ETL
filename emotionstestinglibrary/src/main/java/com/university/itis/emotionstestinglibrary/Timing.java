package com.university.itis.emotionstestinglibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daria Popova on 07.04.18.
 */
public class Timing {

    private static Map<String, Long> startTime = new HashMap<>();

    public static void start(String name) {
        startTime.put(name, System.currentTimeMillis());
    }

    public static void stop(String name) {
        if (startTime.get(name) == null) return;
        long stopTime = System.currentTimeMillis() - startTime.get(name);
        String time = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(stopTime),
                TimeUnit.MILLISECONDS.toSeconds(stopTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime)));

        System.out.println("Timing " + name + " " + time);
        startTime.remove(name);
    }


}
