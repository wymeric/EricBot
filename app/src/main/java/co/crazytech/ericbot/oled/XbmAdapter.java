package co.crazytech.ericbot.oled;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import java.util.List;

import co.crazytech.ericbot.R;
import co.crazytech.ericbot.bt.ConnectBT;

/**
 * Created by eric on 9/9/2016.
 */
public class XbmAdapter extends BaseAdapter{
    private Context context;
    private List<Xbm> xbms;
    private ConnectBT bt;

    public XbmAdapter(Context context, List<Xbm> xbms, ConnectBT bt) {
        this.context = context;
        this.xbms = xbms;
        this.bt = bt;
    }

    @Override
    public int getCount() {
        return xbms.size();
    }

    @Override
    public Object getItem(int position) {
        return xbms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.imagegrid_item,parent,false);
        }
        ImageButton imgBtn = (ImageButton)convertView.findViewById(R.id.imageButton);
        final Xbm xbm = xbms.get(position);
        imgBtn.setImageBitmap(xbm.getImage());
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.writeBtOutputStream(xbm.getArduinoCommand());
            }
        });
        return null;
    }
}
