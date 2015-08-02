package test.zko.androidbluetooth.events;

public class SendDataEvent {
    public final byte[] data;
    public final boolean disconnect;

    public SendDataEvent(byte[] data,boolean disconnect) {
        this.data = data;
        this.disconnect = disconnect;
    }
}
