package com.example.theoplayertv.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.theoplayertv.R;
import com.example.theoplayertv.adapters.AdaptadorCanales;
import com.example.theoplayertv.adapters.AdapterChannels;
import com.example.theoplayertv.api.RetrofitClient;
import com.example.theoplayertv.models.Canal;
import com.example.theoplayertv.models.Categoria;
import com.example.theoplayertv.models.CategoriasResponse;
import com.example.theoplayertv.models.Channel;
import com.example.theoplayertv.models.ChannelResponse;
import com.example.theoplayertv.models.LoginResponse;
import com.theoplayer.android.api.THEOplayerView;
import com.theoplayer.android.api.source.SourceDescription;
import com.theoplayer.android.api.source.SourceType;
import com.theoplayer.android.api.source.TypedSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PlayerActivityTV extends Activity {

    THEOplayerView theoPlayerView;
    Button btnPlayPause, btnMute, btnFullscreen, btnVol_plus, btnVol_minus,btnLogout;
    String auth, categoria_seleccionada, id_categoria;;
    TextView txtPlayStatus, txtTimeUpdate;
    Spinner sp_categorias;
    ListView listaCanales;
    AdaptadorCanales adaptadorCanales;
    AdapterChannels adapterChannels;

    List<Categoria> cat = null;  //Lista de Objetos Categoria
    ArrayList<Channel> channels = null; //Lista de Objetos Channel

    double volumen = 0.5;
    String stream = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        theoPlayerView = findViewById(R.id.theoplayer);
        theoPlayerView.getSettings().setFullScreenOrientationCoupled(true);
        iniciarPlayer(stream);
        theoPlayerView.getPlayer().setAutoplay(true);
        theoPlayerView.getPlayer().setVolume(volumen);

        //Recuperando valor enviado proveniente de LoginActivityTV
        Bundle myBundle = this.getIntent().getExtras();
        if (myBundle != null){
            auth = myBundle.getString("auth");
        }

        /**
         * Referencia de elementos Visuales
         * */
        // Botones de control de player
        btnPlayPause = findViewById(R.id.btn_playpause);
        btnMute = findViewById(R.id.btn_muted);
        btnFullscreen = findViewById(R.id.btn_fullscreen);
        btnVol_plus = findViewById(R.id.btn_volumeplus);
        btnVol_minus = findViewById(R.id.btn_volumeminus);
        //Boton de Cerrar sesion
        btnLogout = findViewById(R.id.btnLogout);
        //Textos de ejemplo de player
        txtPlayStatus = findViewById(R.id.txt_playstatus);
        txtTimeUpdate = findViewById(R.id.txt_timeupdate);
        //Selector de Categorias
        sp_categorias = (Spinner) findViewById(R.id.sp_canales);
        //Lista de Canales
        listaCanales = (ListView) findViewById(R.id.lista_canales) ;

        //Poblando listview de Canales
        loadChannels(auth);
        /*
        final ArrayList<Canal> listChannel = new ArrayList<>();
        listChannel.add(new Canal(R.drawable.canal10,"Canal 10 - TVES", "Locales","https://xcdrsbsv-cf.beenet.com.sv/local10_720/local10_720_out/playlist.m3u8"));
        listChannel.add(new Canal(R.drawable.discoverychannel,"Discovery Channel","Cultural","https://xcdrsbsv-cf.beenet.com.sv/DiscoveryChannel_720/DiscoveryChannel_720_out/playlist.m3u8"));
        listChannel.add(new Canal(R.drawable.investigationdiscovery,"Investigation Discovery","Series y Peliculas","https://xcdrsbsv-cf.beenet.com.sv/inv_discovery_720/inv_discovery_720_out/playlist.m3u8"));
        listChannel.add(new Canal(R.drawable.foxsports,"FoxSports","Deportes","https://xcdrsbsv-cf.beenet.com.sv/foxsports2_720/foxsports2_720_out/playlist.m3u8"));
        adaptadorCanales = new AdaptadorCanales(this,listChannel);
        listaCanales.setAdapter(adaptadorCanales);
        */

        //Poblando Spinner de Categorias con arraylist
        loadCategories(auth);

        /**
         * FUNCIONES ONCLICK DE TODOS LOS ELEMENTOS EN PANTALLA
         */
        //Click boton Play/Pause
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (theoPlayerView.getPlayer().isPaused()) {
                    theoPlayerView.getPlayer().play();
                } else {
                    theoPlayerView.getPlayer().pause();
                }
            }
        });

        //Click boton Mute
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(theoPlayerView.getPlayer().isMuted() == true){
                    theoPlayerView.getPlayer().setMuted(false);
                }else{ theoPlayerView.getPlayer().setMuted(true); }
            }
        });

        //Click Boton Pantalla Completa
        btnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theoPlayerView.getFullScreenManager().requestFullScreen();
            }
        });

        //Click boton Volumen +
        btnVol_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(volumen < 1){
                    volumen = volumen + 0.1;
                    theoPlayerView.getPlayer().setVolume(volumen);
                }

                if(volumen >= 1){
                    Toast.makeText(getApplicationContext(), "MAXIMO ALCANZADO", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Click boton Volumen -
        btnVol_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(volumen > 0){
                    volumen = volumen - 0.1;
                    theoPlayerView.getPlayer().setVolume(volumen);
                }

                if(volumen <= 0){
                    Toast.makeText(getApplicationContext(), "MINIMO ALCANZADO", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Click boton Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDataLogout(auth);
            }
        });

        //Click en Select(Spinner) de Categorias
        sp_categorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Recupera nombre de Categoria seleccionada
                /*
                categoria_seleccionada = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "SELECCIONADA: " + categoria_seleccionada,Toast.LENGTH_LONG).show();
                */

                id_categoria = Integer.toString(cat.get(position).getId());
                Toast.makeText(getApplicationContext(), "ID : " + id_categoria,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Clic en ListView de Canales
        listaCanales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Channel canal = channels.get(position);
                Toast.makeText(getApplicationContext(),canal.getStream_url(),Toast.LENGTH_LONG).show();
                //Llamando para reiniciar el player con la Nueva URL
                iniciarPlayer(canal.getStream_url());

            }
        });

        /**
         * METODOS DE THEOPLAYER
         */
        /*
        theoPlayerView.getPlayer().addEventListener(PlayerEventTypes.PLAY, new EventListener<PlayEvent>() {
            @Override
            public void handleEvent(PlayEvent playEvent) {
                //txtPlayStatus.setText("Playing");
            }
        });

        theoPlayerView.getPlayer().addEventListener(PlayerEventTypes.PAUSE, new EventListener<PauseEvent>() {
            @Override
            public void handleEvent(PauseEvent pauseEvent) {
                //txtPlayStatus.setText("Paused");
            }
        });

        theoPlayerView.getPlayer().addEventListener(PlayerEventTypes.TIMEUPDATE, new EventListener<TimeUpdateEvent>() {
            @Override
            public void handleEvent(TimeUpdateEvent timeUpdateEvent) {
                //txtTimeUpdate.setText(String.valueOf(timeUpdateEvent.getCurrentTime()));
            }
        });

         */
    }
    /*
    @Override
    protected void onPause() {
        super.onPause();
        theoPlayerView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theoPlayerView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        theoPlayerView.onDestroy();
    }


     */

    //Inicializa el Player con URL
    public void iniciarPlayer(String url){
        //Si URL es vacia, asigne por defecto
        if(url == ""){
            url = "https://xcdrsbsv-cf.beenet.com.sv/foxsports2_720/foxsports2_720_out/playlist.m3u8";  // B carga Bien
            //url = "https://xcdrsbsv-cf.beenet.com.sv/espn1/espn1_out/playlist.m3u8";  // B se queda cargando como en nimble
            //url = "https://xcdrsbsv-a.beenet.com.sv/foxnews_720/foxnews_720_out/playlist.m3u8"; // A Mensaje de CORS

            //mago
            //url = "https://xcdrsbsv-cf.beenet.com.sv/foxsports2/foxsports2_out/playlist.m3u8";  // B Se queda cargando
            //url = "https://xcdrsbsv-b.beenet.com.sv/espn1/espn1_out/playlist.m3u8";  // B Muestra mensaje de CORS
            //url = "https://xcdrsbsv-a.beenet.com.sv/foxnews_720/foxnews_720_out/playlist.m3u8"; // A Muestra CORS
        }

        TypedSource typedSource = TypedSource.Builder
                .typedSource()
                .src(url)
                .type(SourceType.HLS)
                .build();

        SourceDescription sourceDescription = SourceDescription.Builder
                .sourceDescription(typedSource)
                .build();

        theoPlayerView.getPlayer().setSource(sourceDescription);

    }

    //Metodo Logout API
    public void postDataLogout(final String auth){
        Call<LoginResponse> call = RetrofitClient.getInstance().getApi()
                .userLogout(auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){
                    //Proceder a siguiente ventana
                    Toast.makeText(getApplicationContext(),loginResponse.getError_description(),Toast.LENGTH_LONG).show();
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

    //Metodo retorna todas las Categorias
    public void loadCategories(final String auth){
        Call<CategoriasResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .allCategories(auth);

        call.enqueue(new Callback<CategoriasResponse>() {
            @Override
            public void onResponse(Call<CategoriasResponse> call, Response<CategoriasResponse> response) {
                CategoriasResponse categoryResponse = response.body();

                cat = new ArrayList<>();

                if(categoryResponse != null){

                    //Agregar Categorias a Spinner Categoria
                    for (Categoria categoria : categoryResponse.getResponse_object()) {

                        cat.add(new Categoria(categoria.getId(),categoria.getName()));
                    }

                    ArrayAdapter<Categoria> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            cat);
                    sp_categorias.setAdapter(arrayAdapter);

                }else{
                    Toast.makeText(getApplicationContext(), "Objeto Vacio", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<CategoriasResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println("*********************** ERROR: " + t.getMessage());
            }
        });
    }

    //Metodo retorna todos los Canales
    public void loadChannels(final String auth){
        Call<ChannelResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .allChannels(auth);

        call.enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                ChannelResponse channelResponse = response.body();

                channels = new ArrayList<>();

                if (channelResponse != null){
                    Toast.makeText(getApplicationContext(),"Objeto Lleno",Toast.LENGTH_LONG).show();

                    //Agregar Canales a ListView (panel derecho)
                    for (Channel channel : channelResponse.getResponse_object()) {
                        System.out.println("ID: "+ channel.getGenre_id() + " - " + channel.getId() + "  -  " + channel.getTitle() + " - " + channel.getIcon_url());

                        channels.add(new Channel(
                                channel.getId(),
                                channel.getGenre_id(),
                                channel.getTitle(),
                                channel.getIcon_url(),
                                channel.getStream_url()
                        ));
                    }

                    adapterChannels = new AdapterChannels(getApplicationContext(),channels);
                    listaCanales.setAdapter(adapterChannels);

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

}