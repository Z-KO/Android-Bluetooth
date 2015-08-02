package test.zko.androidbluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import test.zko.androidbluetooth.events.ConnectEvent;
import test.zko.androidbluetooth.events.SendDataEvent;
import test.zko.androidbluetooth.jobs.ConnectJob;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_turn_on) Button mBtnOn;
    @Bind(R.id.btn_turn_off) Button mBtnOff;
    @Bind(R.id.btn_scan) Button mBtnScan;
    @Bind(R.id.btn_disconnect) Button mBtnDisconnect;
    @Bind(R.id.btn_send1) Button mBtnSend1;
    @Bind(R.id.btn_send2) Button mBtnsend2;
    @Bind(R.id.main_status_scan_progressbar) ProgressBar mScanProgressbar;
    @Bind(R.id.main_status_text) TextView mStatusText;
    @Bind(R.id.list_view) ListView mDevicesFoundListView;

    @BindString(R.string.status_enabled) String ENABLED;
    @BindString(R.string.status_disabled) String DISABLED;

    private DevicesListAdapter mListViewAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBluetoothReceiver;

    public static final int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ButterKnife.bind(this);

        if(mBluetoothAdapter.isEnabled()) {
            mStatusText.setText(ENABLED);
            mStatusText.setTextColor(getResources().getColor(R.color.black));
        } else {
            mStatusText.setText(DISABLED);
            mStatusText.setTextColor(getResources().getColor(R.color.red));
        }

        mListViewAdapter = new DevicesListAdapter(this,R.layout.device_list_item);
        mDevicesFoundListView.setAdapter(mListViewAdapter);

        mDevicesFoundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice) view.getTag();
                createAlertDialog(device);
            }
        });

        setUpBroadcastReceiver();
        setUpButtons();
        getPairedDevices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpButtons() {
        mBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(),"Bluetooth is already on",Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_ENABLE_BT);
                    Toast.makeText(getApplicationContext(),"Turning on...",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.disable();
                mStatusText.setText(DISABLED);
                mStatusText.setTextColor(getResources().getColor(R.color.red));
            }
        });

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanProgressbar.setVisibility(View.VISIBLE);
                mListViewAdapter.clear();
                mBluetoothAdapter.startDiscovery();
            }
        });

        mBtnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SendDataEvent(null,true));
            }
        });

        mBtnSend1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SendDataEvent("1".getBytes(),false));
            }
        });

        mBtnsend2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SendDataEvent("0".getBytes(),false));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mStatusText.setText(ENABLED);
                mStatusText.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    /**
     * Adds paired devices to the listview
     */
    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mListViewAdapter.addAll(pairedDevices);
            mListViewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Creates an alert dialog to check if the user wants to connect to the device or not
     * @param bluetoothDevice the device to connect to
     */
    private void createAlertDialog(final BluetoothDevice bluetoothDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Connect to device")
                .setMessage("Are you sure you want to connect to " + bluetoothDevice.getName() + " ?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothApplication.getJobManager().addJobInBackground(new ConnectJob(bluetoothDevice));
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void setUpBroadcastReceiver() {
        mBluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mListViewAdapter.add(device);
                    mListViewAdapter.notifyDataSetChanged();
                }
                mScanProgressbar.setVisibility(View.GONE);
            }
        };
        registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    public void onEventMainThread(ConnectEvent event) {
        if(event.success) {
            mStatusText.setText("Connected to "+event.deviceName);
            mStatusText.setTextColor(getResources().getColor(R.color.green));
            mBtnDisconnect.setVisibility(View.VISIBLE);
            mBtnSend1.setVisibility(View.VISIBLE);
            mBtnsend2.setVisibility(View.VISIBLE);
        } else {
            mStatusText.setText("Disconnected");
            mBtnDisconnect.setVisibility(View.GONE);
            mBtnSend1.setVisibility(View.GONE);
            mBtnsend2.setVisibility(View.GONE);
        }
    }
}
