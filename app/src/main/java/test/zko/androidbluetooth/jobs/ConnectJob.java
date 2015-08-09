package test.zko.androidbluetooth.jobs;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import test.zko.androidbluetooth.BluetoothApplication;
import test.zko.androidbluetooth.events.ConnectEvent;
import test.zko.androidbluetooth.events.LogEvent;

public class ConnectJob extends Job {

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    public ConnectJob(BluetoothDevice device) {
        super(new Params(1));
        mDevice = device;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new LogEvent("Connecting to " + mDevice.getName()));
    }

    @Override
    public void onRun() throws Throwable {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            ParcelUuid[] deviceID = mDevice.getUuids();
            UUID uuid = deviceID[0].getUuid();

            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            Log.d("UUID",uuid.toString());
            mSocket.connect();
        } catch (IOException connectionException){
            mSocket.close();
            throw connectionException;
        }

        EventBus.getDefault().post(new ConnectEvent(true,mDevice.getName()));
        EventBus.getDefault().post(new LogEvent("Connected to " + mDevice.getName()));

        BluetoothApplication.getJobManager().addJobInBackground(new HandleConnectionJob(mSocket));
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
