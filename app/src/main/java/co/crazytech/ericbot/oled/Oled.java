package co.crazytech.ericbot.oled;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import co.crazytech.ericbot.R;
import co.crazytech.ericbot.bt.ConnectBT;

/**
 * Created by eric on 9/8/2016.
 */
public class Oled {
    private Context context;

    public Oled(Context context) {
        this.context = context;
    }

    public void  showPickerDialog(final ConnectBT connectBT){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle("Select One Name:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                context,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("EricBot");
        arrayAdapter.add("jw.org");
        arrayAdapter.add("no!");
        arrayAdapter.add("heart");

        dialog.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0: connectBT.writeBtOutputStream("2:2");
                }
            }
        });
        dialog.show();
    }

}
