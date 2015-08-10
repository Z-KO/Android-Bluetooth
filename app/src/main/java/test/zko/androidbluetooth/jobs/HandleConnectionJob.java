package test.zko.androidbluetooth.jobs;


import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import de.greenrobot.event.EventBus;
import test.zko.androidbluetooth.events.ConnectEvent;
import test.zko.androidbluetooth.events.LogEvent;
import test.zko.androidbluetooth.events.SendDataEvent;

public class HandleConnectionJob extends Job {

    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public HandleConnectionJob(BluetoothSocket socket) {
        super(new Params(1));
        mSocket = socket;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new LogEvent("Handling connection"));
    }

    @Override
    public void onRun() throws Throwable {
        try {
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e){}

        byte[] buffer = new byte[1024];
        int bytes;

        while(true) {
            try {
                bytes = mInputStream.read(buffer);
                if(bytes > 0) {
                    EventBus.getDefault().post(new LogEvent("RECEIVED: "+Arrays.toString(Arrays.copyOf(buffer,bytes))));
                }
            } catch (IOException exception) {
                break;
            }
        }

        EventBus.getDefault().post(new ConnectEvent(false,null));
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCancel() {}

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public void onEvent(SendDataEvent event) {
        if(event.disconnect) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mOutputStream.write(event.data);
                EventBus.getDefault().post(new LogEvent("SENDING: "+ Arrays.toString(event.data)));
            } catch (IOException e) {}
        }

    }

}
