package co.crazytech.ericbot.motor;

import android.bluetooth.BluetoothSocket;

import co.crazytech.ericbot.bt.ConnectBT;

/**
 * Created by eric on 9/8/2016.
 */
public class MotorAction {
    private ConnectBT connectBT;

    public void initActions(ConnectBT connectBT,Float rollVal,boolean go,boolean reverse) {
        this.connectBT = connectBT;
        float threshold = 30f;
        if(rollVal>threshold||rollVal<-threshold) {
            if (rollVal > threshold && go) turnLeft();
            else if (rollVal < -threshold && go) turnRight();
            else if (rollVal > threshold && reverse) reverseLeft();
            else if (rollVal < -threshold && reverse) reverseRight();
        } else if(go) forward();
        else if(reverse)reverse();
    }

    public void forward(){
        connectBT.writeBtOutputStream("1:1");
    }
    public void reverse(){
        connectBT.writeBtOutputStream("1:2");
    }
    public void stop(){
        connectBT.writeBtOutputStream("1:3");
    }
    public void turnLeft(){
        connectBT.writeBtOutputStream("1:4");
    }
    public void turnRight(){
        connectBT.writeBtOutputStream("1:5");
    }
    public void reverseLeft(){
        connectBT.writeBtOutputStream("1:6");
    }
    public void reverseRight(){
        connectBT.writeBtOutputStream("1:7");
    }

    public void setConnectBT(ConnectBT connectBT) {
        this.connectBT = connectBT;
    }
}
