package com.example.sscheckout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MerchItemAdapter extends ArrayAdapter<ItemInfo> {

    private int resourceId;

    public MerchItemAdapter(Context context, int textViewResourceId, List<ItemInfo> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ItemInfo item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView merchName = (TextView) view.findViewById(R.id.merch_name);
        TextView merchPrice = (TextView) view.findViewById(R.id.merch_price);
        merchName.setText(item.getName());
        merchPrice.setText("$"+item.getPrice());
        return view;
    }
}
