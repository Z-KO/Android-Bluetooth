package test.zko.androidbluetooth.events;


import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogEvent {
    public final String message;

    public LogEvent(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        this.message = sdf.format(cal.getTime()) + ": " + message;
    }
}
