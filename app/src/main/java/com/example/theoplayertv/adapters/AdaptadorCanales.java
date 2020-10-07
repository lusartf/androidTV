package com.example.theoplayertv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.theoplayertv.R;
import com.example.theoplayertv.models.Canal;

import java.util.ArrayList;

public class AdaptadorCanales extends BaseAdapter {

    private Context context;
    private ArrayList<Canal> listItems;

    public AdaptadorCanales(Context context, ArrayList<Canal> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Canal canal = (Canal) getItem(position);

        //En este metodo se crea cada item para el listview
        convertView = LayoutInflater.from(context).inflate(R.layout.elemento_lista,null);

        ImageView logo = (ImageView) convertView.findViewById(R.id.logo_canal);
        TextView nombre = (TextView) convertView.findViewById(R.id.nombre_canal);
        TextView categoria = (TextView) convertView.findViewById(R.id.cat_canal);
        TextView url = (TextView) convertView.findViewById(R.id.url_canal);

       //Llenado
        logo.setImageResource(canal.getLogo());
        nombre.setText(canal.getNombre());
        categoria.setText(canal.getCategoria());
        url.setText(canal.getUrl());

        return convertView;
    }
}
