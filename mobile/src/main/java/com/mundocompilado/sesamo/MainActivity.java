package com.mundocompilado.sesamo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements IEvent {
    private static int REQUEST_ENABLE_BT = 100;
    private String sharedPreferencesName;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;

    private SharedPreferences sharedPreferences;

    private Spinner spinnerDevices;
    private EditText editTextChave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferencesName = getString(R.string.app_preferences);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(this);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(this.bluetoothBroadcastReceiver, filter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAction(view);
            }
        });

        spinnerDevices = (Spinner) findViewById(R.id.spinnerBluetoothDevice);
        editTextChave = (EditText) findViewById(R.id.editTextChave);

        sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE);

        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()){
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
        else{
            call();
        }
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
            restorePreferences();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
            Toast.makeText(this, "Bluetooth não foi habilitado", Toast.LENGTH_LONG);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothBroadcastReceiver);
    }

    //Called when bluetooth enabled
    public void call(){
        if(bluetoothAdapter == null && !bluetoothAdapter.isEnabled()){
            return;
        }

        final BluetoothDeviceAdapter adapter = new BluetoothDeviceAdapter(this, bluetoothAdapter.getBondedDevices());
        spinnerDevices.setAdapter(adapter);

        loadFromPreferences();
    }

    private BluetoothDevice getSelectedDevice(){
        return  (BluetoothDevice) spinnerDevices.getSelectedItem();
    }

    private void sendAction(View view){
        savePrefenreces();

        try {
            OutputStream outputStream = openConnection();
            outputStream.write(editTextChave.getText().toString().getBytes());
        }catch (IOException e){
            Snackbar.make(view, "Não foi possível acionar o portão", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        Snackbar.make(view, "Portão acionado", Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    private OutputStream openConnection() throws IOException {
        BluetoothDevice device = getSelectedDevice();
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        socket.connect();
        return socket.getOutputStream();
    }

    private void savePrefenreces(){
        BluetoothDevice device = getSelectedDevice();
        String chave = editTextChave.getText().toString();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Device", device.getAddress());
        editor.putString("Chave", chave);
        editor.commit();
    }

    private void restorePreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        finish();
        startActivity(getIntent());
    }

    private void loadFromPreferences(){
        String chave = sharedPreferences.getString("Chave", "");
        editTextChave.setText(chave);

        String deviceAddress = sharedPreferences.getString("Device", null);

        if(deviceAddress != null){
            BluetoothDeviceAdapter bluetoothDeviceAdapter = (BluetoothDeviceAdapter) spinnerDevices.getAdapter();
            int position = bluetoothDeviceAdapter.getPosition(deviceAddress);

            if(position > -1) {
                spinnerDevices.setSelection(position);
            }
        }
    }
}
