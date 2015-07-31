package test.zko.androidbluetooth.events;

public class ConnectEvent {
    public final boolean success;
    public final String deviceName;

    public ConnectEvent(boolean success, String deviceName) {
        this.success = success;
        this.deviceName = deviceName;
    }
}
