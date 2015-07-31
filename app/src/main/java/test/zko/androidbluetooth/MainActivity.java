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
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_turn_on) Button mBtnOn;
    @Bind(R.id.btn_turn_off) Button mBtnOff;
    @Bind(R.id.btn_scan) Button mBtnScan;
    @Bind(R.id.list_view) ListView mDevicesFoundListView;
    private DevicesListAdapter mListViewAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ButterKnife.bind(this);

        mListViewAdapter = new DevicesListAdapter(this,R.layout.device_list_item);
        mDevicesFoundListView.setAdapter(mListViewAdapter);
        setUpBroadcastReceiver();
        setUpButtons();
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
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),0);
                    Toast.makeText(getApplicationContext(),"Turning on...",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.disable();
                Toast.makeText(getApplicationContext(),"Bluetooth turned off",Toast.LENGTH_SHORT).show();
            }
        });

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Scanning...",Toast.LENGTH_SHORT).show();
                mListViewAdapter.clear();
                mBluetoothAdapter.startDiscovery();
            }
        });
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
            }
        };
        registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }
}
