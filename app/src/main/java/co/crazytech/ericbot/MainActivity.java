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
import co.crazytech.ericbot.bt.ConnectBT;
import co.crazytech.ericbot.motor.MotorAction;
import co.crazytech.ericbot.oled.Oled;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //activity result identifier
    private static final int DEVICERES = 0;

    private MotorAction motor;
    private boolean goPressed,reversePressed;

    //bluetooth
    private ConnectBT connectBT;


    //Accelerometer
    private SensorManager sensorMan;
    private Sensor sensorGyro;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private static final float EPSILON = 0.00001f;
    private float timestamp;

    //widgets
    private Button btnGo,btnStop,btnDevices,btnDetach,btnResetServo,btnOledNo,btnOledJw,btnOledEricBot,btnOledHeart;
    private SeekBar seekBar;
    private TextView tvGyros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        motor = new MotorAction();

        connectBT = new ConnectBT(this);

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
                try {
                    connectBT.closeBtConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnOledJw = (Button)findViewById(R.id.buttonOled);
        btnOledJw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Oled(v.getContext()).showPickerDialog();

            }
        });

        btnGo = (Button)findViewById(R.id.buttonAccelerate);
        btnGo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        motor.forward(connectBT);
                        goPressed = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        motor.stop(connectBT);
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
                        motor.reverse(connectBT);
                        reversePressed = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        motor.stop(connectBT);
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
                progress-=180;
                if(progress<0)progress*=-1;
                writeBtOutputStream("0:"+progress);
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

    private void writeBtOutputStream(String str){
        connectBT.writeBtOutputStream(str);
    }

    @Override
    public void finish() {
        if(connectBT.isBtConnected())btnDetach.callOnClick();
        sensorMan.unregisterListener(this);
        super.finish();
    }

    @Override
    protected void onPause() {
        sensorMan.unregisterListener(this);
        try {
            connectBT.closeBtConnection();
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
                    if(connectBT.getBtSocket()==null)connectBT.execute(btAddress);
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
            if (rollVal > threshold && goPressed) motor.turnLeft(connectBT);
            else if (rollVal < -threshold && goPressed) motor.turnRight(connectBT);
            else if (goPressed) motor.forward(connectBT);
            else if (rollVal > threshold && reversePressed) motor.reverseLeft(connectBT);
            else if (rollVal < -threshold && reversePressed) motor.reverseRight(connectBT);
            else if (reversePressed) motor.reverse(connectBT);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
