package cs455.scaling.server;

public class TimeSpan {

    private static final long MS_PER_SECOND = 1000;
    private static final long MS_PER_MINUTE = MS_PER_SECOND * 60;
    private static final long MS_PER_HOUR = MS_PER_MINUTE * 60;
    private static final long MS_PER_DAY = MS_PER_HOUR * 24;
    private final long ms;
    private final long secs;
    private final long mins;
    private final long hours;
    private final long days;

    public TimeSpan(long start, long end) {
        long difference = end - start;
        if (difference > MS_PER_DAY) {
            days = difference / MS_PER_DAY;
        } else {
            days = 0;
        }

        difference -= days * MS_PER_DAY;

        if (difference > MS_PER_HOUR) {
            hours = difference / MS_PER_HOUR;
        } else {
            hours = 0;
        }

        difference -= hours * MS_PER_HOUR;

        if (difference > MS_PER_MINUTE) {
            mins = difference / MS_PER_MINUTE;
        } else {
            mins = 0;
        }

        difference -= mins * MS_PER_MINUTE;

        if (difference > MS_PER_SECOND) {
            secs = difference / MS_PER_SECOND;
        } else {
            secs = 0;
        }

        difference -= secs * MS_PER_SECOND;

        ms = difference;
    }

    public long getMs() {
        return ms;
    }

    public long getSeconds() {
        return secs;
    }

    public long getMinutes() {
        return mins;
    }

    public long getHours() {
        return hours;
    }

    public long getDays() {
        return days;
    }

    public String getFormatted() {
        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days);
            if (days > 1) {
                builder.append(" days ");
            } else {
                builder.append(" day ");
            }
        }

        if (hours > 0) {
            builder.append(hours);
            if (hours > 1) {
                builder.append(" hours ");
            } else {
                builder.append(" hour ");
            }
        }

        if (mins > 0) {
            builder.append(mins);
            if (mins > 1) {
                builder.append(" minutes ");
            } else {
                builder.append(" minute ");
            }
        }

        if (secs > 0) {
            builder.append(secs);
            if (secs > 1) {
                builder.append(" seconds ");
            } else {
                builder.append(" second ");
            }
        }

        if (ms > 0) {
            builder.append(ms);
            builder.append(" ms ");
        }

        return builder.toString();
    }
}
