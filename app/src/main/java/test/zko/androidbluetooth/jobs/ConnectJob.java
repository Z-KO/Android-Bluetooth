package test.zko.androidbluetooth.jobs;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.IOException;
import java.lang.reflect.Method;

import de.greenrobot.event.EventBus;
import test.zko.androidbluetooth.events.ConnectEvent;

public class ConnectJob extends Job {

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    public ConnectJob(BluetoothDevice device) {
        super(new Params(1));
        mDevice = device;
    }

    @Override
    public void onAdded() {}

    @Override
    public void onRun() throws Throwable {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            mSocket = (BluetoothSocket) m.invoke(mDevice,1);
            mSocket.connect();
        } catch (IOException connectionException){
            mSocket.close();
            throw connectionException;
        }
        EventBus.getDefault().post(new ConnectEvent(true,mDevice.getName()));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
