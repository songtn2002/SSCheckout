package com.example.sscheckout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NameAdapter extends ArrayAdapter<TabInfo> {

    private int resourceId;

    public NameAdapter (Context context, int textViewResourceId, List<TabInfo> objects){
        super (context, textViewResourceId, objects);
        this.resourceId = textViewResourceId;
    }

    public View getView (int position, View convertView, ViewGroup parent){
        TabInfo tabInfo = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView nameDisplay = (TextView) view.findViewById(R.id.name_display);
        nameDisplay.setText(tabInfo.getName());
        return view;
    }
}
