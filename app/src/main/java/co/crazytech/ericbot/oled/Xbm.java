package co.crazytech.ericbot.oled;

import android.graphics.Bitmap;

/**
 * Created by eric on 9/9/2016.
 */
public class Xbm {
    private String name;
    private String xbmValues;
    private String arduinoCommand;
    private Bitmap image;

    public String getArduinoCommand() {
        return arduinoCommand;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getXbmValues() {
        return xbmValues;
    }
}
