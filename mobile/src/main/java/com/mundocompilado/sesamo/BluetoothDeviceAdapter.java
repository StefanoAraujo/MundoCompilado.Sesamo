package com.mundocompilado.sesamo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

/**
 * Created by jean.almeida on 24/08/2016.
 */
public class BluetoothDeviceAdapter extends ArrayAdapter<Object> {
    LayoutInflater inflater;

    public BluetoothDeviceAdapter(Context context, Set<BluetoothDevice> bluetoothDevices) {
        super(context, R.layout.line_bluetooth_device, R.id.textViewName, bluetoothDevices.toArray());

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.line_bluetooth_device, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textViewName);

        BluetoothDevice bluetoothDevice = (BluetoothDevice) getItem(position);
        textView.setText(bluetoothDevice.getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.line_bluetooth_device, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textViewName);

        BluetoothDevice bluetoothDevice = (BluetoothDevice) getItem(position);
        textView.setText(bluetoothDevice.getName());

        return convertView;
    }

    public int getPosition(String address){
        for (int i = 0; i < getCount(); i++) {
            BluetoothDevice device = (BluetoothDevice) getItem(i);
            if(device.getAddress().equals(address)){
                return i;
            }
        }

        return -1;
    }
}
