package test.zko.androidbluetooth;

public class Utility {

    public static int convertByte(byte data) {
        if (data < 0) {
            return (data & 0x7F) + 128;
        } else {
            return data;
        }
    }
}
