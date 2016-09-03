package co.crazytech.ericbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import co.crazytech.ericbot.bt.BtDeviceListActivity;

public class MainActivity extends AppCompatActivity {
    //activity result identifier
    private static final int DEVICERES = 0;

    //bluetooth
    private BluetoothSocket btSocket;

    //widgets
    Button btnGo,btnStop,btnDevices;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (btSocket==null) new ConnectBT().execute("20:16:05:31:30:48");

        //call widgets
        btnDevices = (Button)findViewById(R.id.buttonDevices);
        btnDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), BtDeviceListActivity.class);
                startActivityForResult(intent,DEVICERES);
            }
        });
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(180);
        seekBar.setProgress(90);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if(btSocket!=null)btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    else Log.e("EricBot btSocket:","NULL");
                    Log.d("EricBot servo angle:",String.valueOf(progress));
                } catch (IOException e){
                    Log.e("EricBot BT status:",e.getMessage());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case DEVICERES :
                if (resultCode== Activity.RESULT_OK) {
                    String btAddress = data.getStringExtra("btAddress");
                    if(btSocket==null)new ConnectBT().execute(btAddress);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ConnectBT extends AsyncTask<String,Void,Void> {
        private boolean connectSuccess = true;
        private BluetoothAdapter btAdapter;
        private boolean isBtConnected;
        private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        @Override
        protected Void doInBackground(String... addresses) {
            try {
                if (btSocket == null || !isBtConnected) {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = btAdapter.getRemoteDevice(addresses[0]);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                connectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            if (connectSuccess) Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_LONG).show();
            else Toast.makeText(MainActivity.this,"Failed to Connect",Toast.LENGTH_LONG).show();
        }
    }

}
