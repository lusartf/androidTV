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

import com.example.theoplayertv.models.Category;
import com.example.theoplayertv.models.Channel;

import java.util.ArrayList;
import java.util.List;

public class AdapterChannels extends BaseAdapter {

    /**
     * Adaptador de canales utilizado para "inflar" el listview de canales lateral
     * */

    private Context context;
    private ArrayList<Channel> listItems;
    private List<Category> listCategory;
    private String genero;

    public AdapterChannels(Context context, ArrayList<Channel> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    public AdapterChannels(Context context, ArrayList<Channel> listItems, List<Category> listCategory) {
        this.context = context;
        this.listItems = listItems;
        this.listCategory = listCategory;
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

    /**
     * getView: El metodo crea cada item para el listview
     * */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Channel channel = (Channel) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.elemento_lista,null);

        ImageView logo = (ImageView) convertView.findViewById(R.id.logo_canal);
        TextView nombre = (TextView) convertView.findViewById(R.id.nombre_canal);
        TextView categoria = (TextView) convertView.findViewById(R.id.cat_canal);
        TextView url = (TextView) convertView.findViewById(R.id.url_canal);

        //Recorriendo Arraylist de Categorias para traducir el id categoria en nombre
        for (int i = 0; i < listCategory.size(); i++){

            if (channel.getGenre_id().equals(Integer.toString(listCategory.get(i).getId()))){
                genero = listCategory.get(i).getName();
            }
        }

        // Seteado de un elemento del listview: Imagen,nombre, categoria, url
        // Se introduce la imagen dentro de ImageView con Glide(declarado en proyecto/app/build.gradle)
        Glide
                .with(context)
                .load(channel.getIcon_url())
                .into(logo);
        nombre.setText(channel.getTitle());
        categoria.setText(genero);
        url.setText(channel.getStream_url());

        return convertView;

    }
}
