package co.crazytech.ericbot.motor;

import android.bluetooth.BluetoothSocket;

import co.crazytech.ericbot.bt.ConnectBT;

/**
 * Created by eric on 9/8/2016.
 */
public class MotorAction {
    public void forward(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:1");
    }
    public void reverse(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:2");
    }
    public void stop(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:3");
    }
    public void turnLeft(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:4");
    }
    public void turnRight(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:5");
    }
    public void reverseLeft(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:6");
    }
    public void reverseRight(ConnectBT connectBT){
        connectBT.writeBtOutputStream("1:7");
    }
}
