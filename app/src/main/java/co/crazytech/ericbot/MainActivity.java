package co.crazytech.ericbot;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.concurrent.ExecutionException;

import co.crazytech.ericbot.bt.BtDeviceListActivity;
import co.crazytech.ericbot.bt.ConnectBT;

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

        if(btSocket==null) {
            try {
                btSocket = new ConnectBT(MainActivity.this).execute("20:16:05:31:30:48").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

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
                    else Log.e("EricBot","BT Socket NULL");
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
                   try {
                        btSocket = new ConnectBT(MainActivity.this).execute(btAddress).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
