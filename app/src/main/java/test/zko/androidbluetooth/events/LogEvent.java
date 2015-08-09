package test.zko.androidbluetooth.events;


import java.util.Calendar;

public class LogEvent {
    public final String message;

    public LogEvent(String message) {
        this.message = Calendar.getInstance().getTimeInMillis()+ ": " + message;
    }
}
