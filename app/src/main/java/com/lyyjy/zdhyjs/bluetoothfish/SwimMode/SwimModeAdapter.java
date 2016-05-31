package com.lyyjy.zdhyjs.bluetoothfish.SwimMode;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.lyyjy.zdhyjs.bluetoothfish.LightColor.LightColorSpinner;
import com.lyyjy.zdhyjs.bluetoothfish.R;

import java.util.List;

/**
 * Created by Administrator on 2016/5/4.
 */
public class SwimModeAdapter extends ArrayAdapter<SwimMode> {
    private int mResourceId;

    public SwimModeAdapter(Context context, int resource, List<SwimMode> objects) {
        super(context, resource, objects);
        mResourceId=resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(mResourceId, null);

        final SwimMode swimMode=getItem(position);
        initStateView(view, swimMode);
        initDirectionView(view, swimMode);
        initTimeView(view, swimMode);
        initColorView(view, swimMode);

        return view;
    }

    private void initDirectionView(View view, final SwimMode swimMode) {
        Spinner spinner= (Spinner) view.findViewById(R.id.spinnerSwimMode);
        spinner.setSelection(swimMode.getDirectionID());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                swimMode.setDirectionID((int) id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initColorView(View view, final SwimMode swimMode) {
        final LightColorSpinner spinner= (LightColorSpinner) view.findViewById(R.id.spinnerLightColor);
        spinner.setSelection(swimMode.getColorID(), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                swimMode.setColorID((int) id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initStateView(View view, SwimMode swimMode) {
        ((TextView)view.findViewById(R.id.tvStateNum)).setText(swimMode.getStateNum());
    }

    private void initTimeView(View view, SwimMode swimMode) {
        final EditText etText= ((EditText) view.findViewById(R.id.etSwimTime));
        etText.setText(String.valueOf(swimMode.getSwimTime()));
        etText.addTextChangedListener(getWatcher(swimMode, etText));
    }

    private TextWatcher getWatcher(final SwimMode swimMode, final EditText etText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int num=0;
                try {
                    num=Integer.parseInt(s.toString());
                }catch (NumberFormatException e){
                    num=0;
                }
                if (num>64){
                    etText.setText("64");
                }
                else if(num<3){
                    etText.setText("3");
                }

                swimMode.setSwimTime(Integer.parseInt(etText.getText().toString()));
            }
        };
    }
}
