package com.lyyjy.zdhyjs.bluetoothfish.SwimMode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.lyyjy.zdhyjs.bluetoothfish.Bluetooth.BluetoothBleManager;
import com.lyyjy.zdhyjs.bluetoothfish.CommandCode;
import com.lyyjy.zdhyjs.bluetoothfish.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/4.
 */
public class SwimModeManager {
    private Context mContext;
    private MenuItem mMenuItem;
    private ArrayList<SwimMode> mArrayList;
    private ListView mListView;

    private final int MAX_MODE_NUM=6;
    private final int MIN_MODE_NUM=1;

    public SwimModeManager(Context context, MenuItem menuItem){
        mContext=context;
        mMenuItem=menuItem;
        mArrayList=new ArrayList<SwimMode>();
    }

    private boolean mIsAutoSwim=false;
    public void changeSwimMode(){
        mIsAutoSwim=!mIsAutoSwim;
        if (mIsAutoSwim){
            changeToSimpleAutoSwim();
        }
        else{
            changeToManualSwim();
        }
    }

    private void changeToManualSwim() {
        mIsAutoSwim=false;
        mMenuItem.setTitle("自由游动");
        BluetoothBleManager.GetInstance(mContext).writeDataToDevice(CommandCode.getManualModeCommand());
    }

    private void changeToSimpleAutoSwim() {
        mIsAutoSwim=true;
        mMenuItem.setTitle("手动控制");
        BluetoothBleManager.GetInstance(mContext).writeDataToDevice(CommandCode.getAutoModeCommand());
    }

    public byte[] getSwimCommand(){
        byte[] data=new byte[10];


        return data;
    }

    private void changeToAutoSwim() {
        LinearLayout layout=getLayout();

        ((Button) layout.findViewById(R.id.btnAddModeState)).setOnClickListener(addState());
        ((Button) layout.findViewById(R.id.btnRemoveModeState)).setOnClickListener(removeState());

        showDialog(layout);
    }

    private View.OnClickListener removeState() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayList.size()<=MIN_MODE_NUM){
                    Toast.makeText(mContext,"状态数最少为1",Toast.LENGTH_SHORT).show();
                }
                else{
                    mArrayList.remove(mArrayList.size()-1);
                    mListView.setAdapter(getSwimModeAdapter());
                }
            }
        };
    }

    private View.OnClickListener addState() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArrayList.size()>=MAX_MODE_NUM){
                    Toast.makeText(mContext,"状态数最多为6",Toast.LENGTH_SHORT).show();
                }
                else{
                    addListState();
                    mListView.setAdapter(getSwimModeAdapter());
                }
            }
        };
    }

    private void addListState() {
        mArrayList.add(new SwimMode(30, mArrayList.size()+1));
    }

    private void showDialog(LinearLayout layout) {
        AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
        builder.setTitle("设置游动方式");
        builder.setView(layout);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsAutoSwim = true;
                mMenuItem.setTitle("手动控制");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsAutoSwim = false;
                mMenuItem.setTitle("自由游动");
            }
        });
        builder.show();
    }

    private LinearLayout getLayout() {
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LayoutInflater inflater=LayoutInflater.from(mContext);
        LinearLayout layout=(LinearLayout) inflater.inflate(R.layout.dialog_swim_mode, null);

        mListView = (ListView) layout.findViewById(R.id.lvSwimMode);
        mArrayList.clear();
        addListState();
        mListView.setAdapter(getSwimModeAdapter());

        return layout;
    }

    private SwimModeAdapter getSwimModeAdapter() {
        return new SwimModeAdapter(mContext, R.layout.adapter_swim_mode,mArrayList);
    }
}
