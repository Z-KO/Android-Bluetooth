package test.zko.androidbluetooth.events;

public class UpdateDeviceEvent {

    public byte deviceID;
    public byte deviceValue;

    public UpdateDeviceEvent(byte deviceID, byte deviceValue) {
        this.deviceID = deviceID;
        this.deviceValue = deviceValue;
    }
}
