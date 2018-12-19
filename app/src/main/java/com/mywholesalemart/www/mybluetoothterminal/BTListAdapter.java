package com.mywholesalemart.www.mybluetoothterminal;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class BTListAdapter extends ArrayAdapter<BluetoothDevice> {
    customButtonListener customListner;

    public interface customButtonListener {
        public void onButtonClickListner(int position,BluetoothDevice value);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    private int row_index = -1;
    private Context context;
    private ArrayList<BluetoothDevice> data = new ArrayList<BluetoothDevice>();

    public BTListAdapter(Context context, ArrayList<BluetoothDevice> dataItem) {
        super(context, R.layout.bluetoothlistview, dataItem);
        this.data = dataItem;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.bluetoothlistview, null);
            viewHolder = new ViewHolder();

            viewHolder.text =  convertView.findViewById(R.id.bttv1);
            viewHolder.button =  convertView.findViewById(R.id.btbtn1);
            viewHolder.lily = convertView.findViewById(R.id.lily);
            convertView.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final BluetoothDevice temp = getItem(position);

        if(temp!= null){
            String ss = temp.getName();
            if(ss !=null && !ss.isEmpty()){
                viewHolder.text.setText(temp.getName());
            }
            else {
                viewHolder.text.setText(temp.getAddress());
            }
        }

        /*viewHolder.lily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index=position;
                notifyDataSetChanged();
            }
        });*/

        if(row_index==position){
            viewHolder.lily.setBackgroundColor(Color.parseColor("#2ae719"));
        }
        else
        {
            viewHolder.lily.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        viewHolder.button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (customListner != null) {
                    customListner.onButtonClickListner(position, temp);
                    /*viewHolder.lily.setOnClickListener(this);*/
                    row_index=position;
                    notifyDataSetChanged();
                }

            }
        });

        return convertView;
    }

    public class ViewHolder {
        TextView text;
        Button button;
        ConstraintLayout lily;
    }
}