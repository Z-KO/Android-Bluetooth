package test.zko.androidbluetooth.jobs;


import android.bluetooth.BluetoothSocket;

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
import test.zko.androidbluetooth.events.UpdateDeviceEvent;

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
        } catch (IOException e){
            EventBus.getDefault().post(new LogEvent("ERROR: "+e.getMessage()));
        }

        byte[] buffer = new byte[1024];
        int bytes;
        int bytes1;
        int bytes2;

        //Send signal to get data about devices
        EventBus.getDefault().post(new SendDataEvent(new byte[]{1},false));

        while(true) {
            //Thread.sleep(1,0);
            try {
                bytes1 = mInputStream.read();
                Thread.sleep(1,0);
                bytes2 = mInputStream.read();
                byte[] data = new byte[]{(byte)bytes1,(byte)bytes2};
                EventBus.getDefault().post(new LogEvent("RECEIVED: " + Arrays.toString(data)));
                handleData(data,2);
            } catch (IOException exception) {
                EventBus.getDefault().post(new LogEvent("ERROR: "+exception.getMessage()));
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

    private void handleData(byte[] buffer,int dataSize) {
        if(dataSize == 2) {
            EventBus.getDefault().post(new LogEvent("Updating device with id "+buffer[0]));
            EventBus.getDefault().post(new UpdateDeviceEvent(buffer[0],buffer[1]));
        } else {
            EventBus.getDefault().post(new LogEvent("ERROR: Incoming data size was not 2, size was: " + dataSize));
        }
    }

    public void onEvent(SendDataEvent event) {
        if(event.disconnect) {
            try {
                mSocket.close();
                EventBus.getDefault().post(new LogEvent("Closing connection"));
            } catch (IOException e) {
                EventBus.getDefault().post(new LogEvent("ERROR: "+e.getMessage()));
                e.printStackTrace();
            }
        } else {
            try {
                mOutputStream.write(event.data);
                EventBus.getDefault().post(new LogEvent("SENDING: "+ Arrays.toString(event.data)));
            } catch (IOException e) {
                EventBus.getDefault().post(new LogEvent("ERROR: "+e.getMessage()));
            }
        }

    }

}
