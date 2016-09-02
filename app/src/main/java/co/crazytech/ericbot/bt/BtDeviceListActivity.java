package co.crazytech.ericbot.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import co.crazytech.ericbot.R;

import static android.bluetooth.BluetoothAdapter.*;

/**
 * Created by eric on 9/2/2016.
 */
public class BtDeviceListActivity extends AppCompatActivity {
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;

    //widgets
    private Button btnPaired;
    private ListView lvDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //call widgets
        btnPaired = (Button)findViewById(R.id.button);
        lvDevices = (ListView)findViewById(R.id.listView);

        btAdapter = getDefaultAdapter();
        if(btAdapter==null) {
            Toast.makeText(this, "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if(btAdapter.isEnabled()){}
            else {
                Intent turnBtOn = new Intent(ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBtOn,1);
            }
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDeviceList();
            }
        });

    }

    private void pairedDeviceList() {
        pairedDevices = btAdapter.getBondedDevices();
        ArrayList list = new ArrayList();
        if(pairedDevices.size()>0){
            for (BluetoothDevice device : pairedDevices) {
                list.add(device.getName()+"\n"+device.getAddress());
            }
        } else {
            Toast.makeText(this,"No Paired Bluetooth Device found",Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        lvDevices.setAdapter(adapter);
        lvDevices.setOnItemClickListener(listClickListener);
    }

    private AdapterView.OnItemClickListener listClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length()-17);
            Intent i = new Intent();
            i.putExtra("btAddress",address);
            setResult(Activity.RESULT_OK,i);
            finish();
        }
    };
}
