package co.crazytech.ericbot.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import co.crazytech.ericbot.MainActivity;

/**
 * Created by eric on 9/8/2016.
 */
public class ConnectBT extends AsyncTask<String,Void,Void> {
    private boolean connectSuccess = true;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;

    public ConnectBT(Context context) {
        this.context = context;
    }

    public BluetoothSocket getBtSocket(){
        return btSocket;
    }

    public void writeBtOutputStream(String str){
        try {
            if(isBtConnected())getBtSocket().getOutputStream().write(String.valueOf(str).getBytes());
            else Log.e("EricBot btSocket","NULL");
        } catch (IOException e) {
            Log.e("EricBot BT status",e.getMessage());
        }
    }

    public boolean isBtConnected(){
        if(btSocket!=null)return btSocket.isConnected();
        return false;
    }

    public void closeBtConnection() throws IOException {
        if(isBtConnected()){
            btSocket.close();
            Toast.makeText(context,"Bluetooth Connection Ended",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected Void doInBackground(String... addresses) {
        try {
            if (!isBtConnected()) {
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
        if (connectSuccess) Toast.makeText(context,"Connected",Toast.LENGTH_LONG).show();
        else Toast.makeText(context,"Failed to Connect",Toast.LENGTH_LONG).show();
    }
}

