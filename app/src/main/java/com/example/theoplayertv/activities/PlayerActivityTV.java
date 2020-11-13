package com.example.theoplayertv.activities;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.theoplayertv.R;
import com.example.theoplayertv.adapters.AdapterChannels;
import com.example.theoplayertv.api.RetrofitClient;
import com.example.theoplayertv.models.Category;
import com.example.theoplayertv.models.CategoryResponse;
import com.example.theoplayertv.models.Channel;
import com.example.theoplayertv.models.ChannelResponse;
import com.example.theoplayertv.models.LoginResponse;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistTracker;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
//import com.muddzdev.styleabletoast.StyleableToast;
/*
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.cache.Cache;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.SourceType;
import com.theoplayer.android.api.source.TypedSource;
import com.theoplayer.android.api.player.PreloadType;
 */

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PlayerActivityTV extends Activity {

    DrawerLayout drawerLayout; //Permite el despliegue de menu lateral en conjunto con mainLayout y menuLateral
    ConstraintLayout mainLayout, menuLateral;

    /* Exoplayer */
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    Spinner sp_categorias;
    ListView listaCanales;
    AdapterChannels adapterChannels;
    List<Category> cat = null;  //Lista de Objetos Categoria
    ArrayList<Channel> channels = null; //Lista de Objetos Channel
    ChannelResponse channelResponse; //Recibe la respuesta de API

    double volumen = 1;
    public String stream = "";
    String auth, id_categoria;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Referenciando y parametros de Theoplayer
        playerView = findViewById(R.id.video_view);
        iniciarPlayer(stream);

        //Recuperando valor enviado proveniente de LoginActivityTV
        Bundle myBundle = this.getIntent().getExtras();
        if (myBundle != null){
            auth = myBundle.getString("auth");
        }

        /**
         * Referencia de elementos Visuales
         * */
        //Selector de Categorias
        sp_categorias = (Spinner) findViewById(R.id.sp_canales);
        //Lista de Canales
        listaCanales = (ListView) findViewById(R.id.lista_canales) ;
        //Contenedor de mainLayout y menuLateral
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        //Contenedor Principal
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        menuLateral = (ConstraintLayout) findViewById(R.id.menuLateral);

        //Poblando Spinner de Categorias con arraylist
        loadCategories(auth);

        //Poblando listview de Canales
        listaCanales.setSelector(R.color.transparent); //color transparente en el focus de listview
        getChannelResponse(auth);

        /**
         * FUNCIONES ONCLICK DE TODOS LOS ELEMENTOS EN PANTALLA
         */

        /** Click en Select(Spinner) de Categorias **/
        sp_categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                id_categoria = Integer.toString(cat.get(position).getId());

                //Envia Arraylist Channel, a funcion que filtra canales por categoria
                if (channelResponse != null) {
                    loadChannelsByFilter(channelResponse, id_categoria);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         * Click listview de Canales, Cambia los canales segun seleccion
         **/
        listaCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Obtiene el objeto Canal, al que se le hizo click
                Channel canal = channels.get(position);

                //Llamado para reiniciar el player con la Nueva URL
                stream = canal.getStream_url();
                iniciarPlayer(canal.getStream_url());
            }
        });

    }


    /**
     * Metodos de Exoplayer
     * */
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            iniciarPlayer(stream);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            iniciarPlayer(stream);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private class PlayerEventListener implements Player.EventListener {

        /*
        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
            updateButtonVisibility();
        }
        */

        @Override
        public void onPlayerError(@NonNull ExoPlaybackException e) {

            if (isBehindLiveWindow(e)) {
                //clearStartPosition();
                //initializePlayer();
                //Toast.makeText(getApplicationContext(),"-----*** ERROR DE BehindLiveWindow ***-----",Toast.LENGTH_LONG).show();
                //System.out.println("---------------************* ERROR ***********-----------------");
                iniciarPlayer(stream);
            } else {
                //updateButtonVisibility();
                //showControls();
            }


        }

        /*
        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(
                @NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {
            updateButtonVisibility();
            if (trackGroups != lastSeenTrackGroupArray) {
                MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
        */
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            if (cause instanceof HlsPlaylistTracker.PlaylistStuckException){
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Inicializa el player con URL
     * Parametro: url valor de URL de canal.
     * */
    public void iniciarPlayer(String url){
        //Si URL es vacia, asigne por defecto
        if(url == ""){
            url = "https://xcdrsbsv-cf.beenet.com.sv/foxsports2_720/foxsports2_720_out/playlist.m3u8";
        }

        //Toast.makeText(getApplicationContext(),"dentro de IniciarPlayer()",Toast.LENGTH_LONG).show();

        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());

            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
        }

        player.addListener(new PlayerEventListener());
        playerView.setPlayer(player);


        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .build();
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare();

    }

    /**
     *  Cerrar Sesion en API
     *  parametro: Auth contiene la clave utilizada para transacciones con la API
     * */
    public void postDataLogout(final String auth){
        Call<LoginResponse> call = RetrofitClient.getInstance().getApi()
                .userLogout(auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){

                    Toast.makeText(getApplicationContext(),"Sesion Finalizada",Toast.LENGTH_LONG).show();
                    //StyleableToast.makeText(getApplicationContext(),"Sesion Finalizada",R.style.msgToast).show();
                    //Volver a Inicio de Sesion
                    Intent intent = new Intent (getApplicationContext(), LoginActivityTV.class);
                    startActivityForResult(intent, 0);

                    finish();

                }else{
                    Toast.makeText(getApplicationContext(), loginResponse.getError_description(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Metodo conecta con API y carga lista de categorias a Spinner
     * parametro: Auth contiene la clave utilizada para transacciones con la API
     * */
    public void loadCategories(final String auth){
        Call<CategoryResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .allCategories(auth);

        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                CategoryResponse categoryResponse = response.body();

                cat = new ArrayList<>();

                if(categoryResponse != null){
                    //Agregando una Categoria 'TODOS'
                    cat.add(new Category(0,"Todos"));

                    //Agregar Categorias a Spinner Categoria
                    for (Category category : categoryResponse.getResponse_object()) {

                        cat.add(new Category(category.getId(), category.getName()));
                    }

                    ArrayAdapter<Category> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                            R.layout.spinner_item,
                            cat);
                    sp_categorias.setAdapter(arrayAdapter);

                }else{
                    Toast.makeText(getApplicationContext(), "Objeto Vacio", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println("*********************** ERROR: " + t.getMessage());
            }
        });
    }

    /**
     * Metodo conecta a API y obtiene objeto con Canales
     * Parametro: Auth contiene la clave utilizado para transacciones con la API
     * */
    public void getChannelResponse(final String auth){
        Call<ChannelResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .allChannels(auth);

        call.enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                //Guarda respuesta de API en channelResponse
                channelResponse = response.body();
                channels = new ArrayList<>();

                if (channelResponse != null){
                    // Cargar canales por filtro 0 = TODOS
                    loadChannelsByFilter(channelResponse,"0");
                }else{
                    Toast.makeText(getApplicationContext(), "Objeto Vacio", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println("*********************** ERROR: " + t.getMessage());
            }
        });
    }

    /**
     * Funcion Carga canales por Categoria
     * Parametros: Lista de Canales, Id de la categoria
     * */
    public void loadChannelsByFilter(ChannelResponse listChannel, String idCat){

        //Borrando Elementos de Arraylist
        channels.clear();

        //Agregar Canales a lista segun categoria o Todos
        for (Channel channel : channelResponse.getResponse_object()) {
            //Todas las Categorias
            if (idCat.equals("0")){
                channels.add(new Channel(
                        channel.getId(),
                        channel.getGenre_id(),
                        channel.getTitle(),
                        channel.getIcon_url(),
                        channel.getStream_url()
                ));
            }

            //Filtrado por Categoria
            if(channel.getGenre_id().equals(idCat)) {
               channels.add(new Channel(
                        channel.getId(),
                        channel.getGenre_id(),
                        channel.getTitle(),
                        channel.getIcon_url(),
                        channel.getStream_url()
                ));
            }
        }

        //Envia el ArrayList de Canales a Adaptador de ListView
        adapterChannels = new AdapterChannels(getApplicationContext(),channels,cat);
        listaCanales.setAdapter(adapterChannels);

    }

    /** Metodo que captura acciones del D-PAD **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handled = true;
                openList();
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handled = true;
                closeList();
                break;

            case KeyEvent.KEYCODE_BACK:
                handled = true;
                //Llamar a la funcion cerrar sesion

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Quieres salir de la Aplicacion?");
                builder.setTitle("Salir");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postDataLogout(auth);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }

        return handled || super.onKeyDown(keyCode, event);
    }

    /** Funcion Despliega la vista Lateral **/
    public void openList(){
        if (!drawerLayout.isDrawerOpen(menuLateral)){
            drawerLayout.openDrawer(menuLateral);
        }
    }

    /** Funcion Oculta lista lateral **/
    public void closeList(){
        if (drawerLayout.isDrawerOpen(menuLateral)){
            drawerLayout.closeDrawer(menuLateral);
        }
    }

}