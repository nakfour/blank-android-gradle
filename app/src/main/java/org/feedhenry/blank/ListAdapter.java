package org.feedhenry.blank;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    ArrayList<Item> itemsDisplay;
    Context context;
    private static LayoutInflater inflater=null;
    Dialog newDialog;
    private MainInterface mainCallback;
    public class viewHolder
    {
        TextView nameView;
        ImageView imgDeleteView;
        ImageView imgEditView;
    }
    public ListAdapter(MainActivity mainActivityContext, MainInterface callback, ArrayList<Item> items) {
        itemsDisplay=items;
        context=mainActivityContext;
        mainCallback = callback;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return itemsDisplay.size();
    }

    @Override
    public Object getItem(int i) {
        return itemsDisplay.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        viewHolder holder=new viewHolder();
        View rowView = inflater.inflate(R.layout.listcustomview, null);

        holder.nameView=(TextView) rowView.findViewById(R.id.name);
        holder.imgDeleteView=(ImageView) rowView.findViewById(R.id.delete);
        holder.imgEditView=(ImageView) rowView.findViewById(R.id.edit);
        holder.nameView.setText(itemsDisplay.get(i).getName());


        holder.imgEditView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit text
                mainCallback.editName(itemsDisplay.get(i));
            }
        });

        holder.imgDeleteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit text
                mainCallback.deleteName(itemsDisplay.get(i));
            }
        });

        return rowView;
    }
}
