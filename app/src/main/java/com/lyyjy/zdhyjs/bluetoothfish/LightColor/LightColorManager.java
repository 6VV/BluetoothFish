package com.lyyjy.zdhyjs.bluetoothfish.LightColor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BluetoothBleManager;
import com.lyyjy.zdhyjs.bluetoothfish.CommandCode;
import com.lyyjy.zdhyjs.bluetoothfish.R;

/**
 * Created by Administrator on 2016/5/5.
 */
public class LightColorManager {
    private Context mContext;

    public LightColorManager(Context context){
        mContext=context;
    }

    public void selectDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setTitle("设置灯光颜色");
        LayoutInflater inflater=LayoutInflater.from(mContext);
        LinearLayout layout= (LinearLayout) inflater.inflate(R.layout.dialog_choose_color, null);
        final LightColorSpinner colorSpinner= (LightColorSpinner) layout.findViewById(R.id.spinnerLightColor);
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                byte color=colorSpinner.getColor(colorSpinner.getSelectedItemPosition()).getSimpleColor();
                BluetoothBleManager.GetInstance(mContext).writeDataToDevice(CommandCode.getColorCommand(color));
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }
}
