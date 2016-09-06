package co.crazytech.ericbot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import co.crazytech.ericbot.bt.BtDeviceListActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //activity result identifier
    private static final int DEVICERES = 0;

    private MotorAction motor;
    private boolean goPressed,reversePressed;

    //bluetooth
    private BluetoothSocket btSocket;

    //Accelerometer
    private SensorManager sensorMan;
    private Sensor sensorGyro;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private static final float EPSILON = 0.00001f;
    private float timestamp;

    //widgets
    Button btnGo,btnStop,btnDevices,btnDetach,btnResetServo;
    SeekBar seekBar;
    TextView tvGyros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        motor = new MotorAction();

        //call widgets
        tvGyros = (TextView)findViewById(R.id.textViewGyros);
        btnDevices = (Button)findViewById(R.id.buttonDevices);
        btnDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), BtDeviceListActivity.class);
                startActivityForResult(intent,DEVICERES);
            }
        });
        btnDetach = (Button)findViewById(R.id.buttonDetach);
        btnDetach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket!=null&&btSocket.isConnected()) try {
                    btSocket.close();
                    Toast.makeText(v.getContext(),"Connection Ended",Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnGo = (Button)findViewById(R.id.buttonAccelerate);
        btnGo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        motor.forward();
                        goPressed = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        motor.stop();
                        goPressed = false;
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        btnStop = (Button)findViewById(R.id.buttonBrake);
        btnStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        motor.reverse();
                        reversePressed = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        motor.stop();
                        reversePressed = false;
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        btnResetServo = (Button)findViewById(R.id.buttonResetServo);
        btnResetServo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(90);
            }
        });
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(180);
        seekBar.setProgress(90);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    progress-=180;
                    if(progress<0)progress*=-1;
                    if(btSocket!=null)btSocket.getOutputStream().write(String.valueOf("0:"+progress).getBytes());
                    else Log.e("EricBot btSocket:","NULL");
                    Log.d("EricBot servo angle:",String.valueOf("0:"+progress));
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

        //call sensor
        sensorMan = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorGyro = sensorMan.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorMan.registerListener(this, sensorGyro,SensorManager.SENSOR_DELAY_NORMAL);
        float inR[] = new float[9];
        float outR[] = new float[9];
        sensorMan.remapCoordinateSystem(inR,sensorMan.AXIS_Y,sensorMan.AXIS_MINUS_X,outR);
    }

    @Override
    public void finish() {
        if(btSocket!=null&&btSocket.isConnected())btnDetach.callOnClick();
        sensorMan.unregisterListener(this);
        super.finish();
    }

    @Override
    protected void onPause() {
        sensorMan.unregisterListener(this);
        if(btSocket!=null)try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMan.registerListener(this, sensorGyro,SensorManager.SENSOR_DELAY_NORMAL);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        Float rollVal = event.values[1];
        tvGyros.setText("ROLL :"+ Float.toString(rollVal));
        float threshold = 30f;
        if(rollVal>threshold||rollVal<-threshold) {
            if (rollVal > threshold && goPressed) motor.turnLeft();
            else if (rollVal < -threshold && goPressed) motor.turnRight();
            else if (goPressed) motor.forward();
            else if (rollVal > threshold && reversePressed) motor.reverseLeft();
            else if (rollVal < -threshold && reversePressed) motor.reverseRight();
            else if (reversePressed) motor.reverse();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class MotorAction {
        public void forward(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+1).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void reverse(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+2).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void stop(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+3).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void turnLeft(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+4).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void turnRight(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+5).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void reverseLeft(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+6).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void reverseRight(){
            if(btSocket!=null&&btSocket.isConnected()) try {
                btSocket.getOutputStream().write(String.valueOf("1:"+7).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
