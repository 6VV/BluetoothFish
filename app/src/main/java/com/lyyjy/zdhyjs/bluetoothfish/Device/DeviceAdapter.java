package com.lyyjy.zdhyjs.bluetoothfish.Device;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyyjy.zdhyjs.bluetoothfish.R;

import java.util.List;

/**
 * Created by Administrator on 2016/1/15.
 */
public class DeviceAdapter extends ArrayAdapter<Device> {
    private int mResourceId;

    public DeviceAdapter(Context context, int resource, List<Device> objects) {
        super(context, resource, objects);
        mResourceId=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Device device=getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(mResourceId, null);
        ImageView deviceIcon = (ImageView) view.findViewById(R.id.ivDeviceIcon);
        TextView deviceName = (TextView) view.findViewById(R.id.tvDeviceName);
        TextView deviceAddress= (TextView) view.findViewById(R.id.tvDeviceAddress);
        deviceIcon.setImageResource(device.getIcon());
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());
        return view;
    }
}
