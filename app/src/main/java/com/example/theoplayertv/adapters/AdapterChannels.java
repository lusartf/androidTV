package com.example.theoplayertv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.theoplayertv.R;

import com.example.theoplayertv.models.Categoria;
import com.example.theoplayertv.models.Channel;

import java.util.ArrayList;

public class AdapterChannels extends BaseAdapter {

    private Context context;
    private ArrayList<Channel> listItems;
    private Categoria categoria;

    public AdapterChannels(Context context, ArrayList<Channel> listItems) {
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

        Channel channel = (Channel) getItem(position);

        //En este metodo se crea cada item para el listview
        convertView = LayoutInflater.from(context).inflate(R.layout.elemento_lista,null);

        ImageView logo = (ImageView) convertView.findViewById(R.id.logo_canal);
        TextView nombre = (TextView) convertView.findViewById(R.id.nombre_canal);
        TextView categoria = (TextView) convertView.findViewById(R.id.cat_canal);
        TextView url = (TextView) convertView.findViewById(R.id.url_canal);

        //Llenado
        //logo.setImageResource(R.drawable.ic_launcher_foreground);
        Glide
                .with(context)
                .load(channel.getIcon_url())
                .into(logo);
        nombre.setText(channel.getTitle());
        categoria.setText(channel.getGenre_id());
        url.setText(channel.getStream_url());

        return convertView;

    }
}
