package test.zko.androidbluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DevicesListAdapter extends ArrayAdapter<BluetoothDevice> {

    public DevicesListAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item,parent,false);
        }

        convertView.setTag(device);

        TextView deviceNameTextView = (TextView) convertView.findViewById(R.id.device_name_text);
        TextView deviceAddressTextView = (TextView) convertView.findViewById(R.id.device_address_text);

        deviceNameTextView.setText(device.getName());
        deviceAddressTextView.setText(device.getAddress());

        return convertView;
    }
}
