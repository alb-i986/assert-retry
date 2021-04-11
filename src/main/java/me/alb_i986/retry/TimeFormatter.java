package me.alb_i986.retry;

import java.time.Duration;

public class TimeFormatter {

    public static final TimeFormatter SINGLETON = new TimeFormatter();

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;

    /**
     * Pretty prints the given duration.
     * <p>
     * Examples:
     * <pre>
     * prettyPrint(Duration.ofMinutes(1).plusSeconds(1).plusMillis(1)) -&gt; "1m 1s 1ms"
     * prettyPrint(Duration.ofDays(1)) -&gt; "24h"
     * </pre>
     */
    public String prettyPrint(Duration duration) {
        StringBuilder sb = new StringBuilder();

        long durationSeconds = duration.getSeconds();
        long hours = durationSeconds / SECONDS_PER_HOUR;
        int minutes = (int) ((durationSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
        int secs = (int) (durationSeconds % SECONDS_PER_MINUTE);
        int millis = duration.getNano() / 1000 / 1000;

        if (hours != 0) {
            sb.append(String.format("%sh", hours));
        }
        if (minutes != 0) {
            sb.append(String.format(" %sm", minutes));
        }
        if (secs != 0) {
            sb.append(String.format(" %ss", secs));
        }
        if (millis != 0 || sb.length() == 0) {
            sb.append(String.format(" %sms", millis));
        }

        // the first char might be a space
        if (sb.charAt(0) == ' ') {
            return sb.substring(1);
        } else {
            return sb.toString();
        }
    }
}
