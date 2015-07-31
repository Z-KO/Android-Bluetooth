package test.zko.androidbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_turn_on) Button mBtnOn;
    @Bind(R.id.btn_turn_off) Button mBtnOff;
    @Bind(R.id.btn_scan) Button mBtnScan;
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

        setUpBroadcastReceiver();
        setUpButtons();
        getPairedDevices();
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
}
