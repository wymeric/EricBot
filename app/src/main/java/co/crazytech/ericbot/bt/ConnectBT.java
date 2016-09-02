package co.crazytech.ericbot.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by eric on 9/2/2016.
 */
public class ConnectBT extends AsyncTask<String,Void,BluetoothSocket> {
    private boolean connectSuccess = true;
    private BluetoothSocket btSocket;
    private BluetoothAdapter btAdapter;
    private boolean isBtConnected;
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;

    public ConnectBT(Context context) {
        this.context = context;
    }

    @Override
    protected BluetoothSocket doInBackground(String... addresses) {
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
    protected void onPostExecute(BluetoothSocket bluetoothSocket) {
        super.onPostExecute(btSocket);
    }
}
