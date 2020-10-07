package com.example.theoplayertv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.theoplayertv.R;
import com.example.theoplayertv.api.RetrofitClient;
import com.example.theoplayertv.models.LoginResponse;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivityTV extends Activity {

    EditText user,pass;
    String username, password,data,auth,timestamp;
    long x;
    Button btnLogin;

    static final String APP_ID = "1";
    static final String BOX_ID = "123456789";
    static final String ENCRYPTION_KEY = "1234567890123456";
    static final String COMPANY_ID = "1";
    static final String APP_NAME = "BeenetPlay";
    static final String API_VERSION = "";
    static final String APPVERSION = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Referenciando variables con Formulario
        user = findViewById(R.id.txtUser);
        pass = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                //Recuperando Valores de Formulario
                username = user.getText().toString();
                password = pass.getText().toString();

                x = System.currentTimeMillis();
                timestamp = Long.toString(x);

                //Concatenando Variable a encriptar
                data ="username=" + username +
                        ";password=" + password +
                        ";appid=1" +
                        ";boxid=123456789" +
                        ";timestamp=" + timestamp;

                //Encriptar data
                try {
                    auth = encrypt(data,ENCRYPTION_KEY);
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }

                System.out.println("/******* Data Encrypt: " + auth);

                //Llamando a la API
                postDataLogin(auth);

            }
        });

    }

    /*
    *  Funcion que envia datos a la API
    * */
    public void postDataLogin(final String auth){
        Call<LoginResponse> call = RetrofitClient.getInstance().getApi()
                .userLogin(COMPANY_ID,APP_ID,APP_NAME,API_VERSION,APPVERSION,auth);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();
                if (loginResponse.getStatus_code() == 200){
                    //Proceder a siguiente ventana
                    Toast.makeText(getApplicationContext(),loginResponse.getError_description(),Toast.LENGTH_LONG).show();
                    Intent intent = new Intent (getApplicationContext(), PlayerActivityTV.class);

                    //Enviando parametro a segundo activity
                    Bundle myBundle = new Bundle();
                    myBundle.putString("auth",auth);
                    intent.putExtras(myBundle);
                    startActivityForResult(intent, 0);

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

    /*
    *   Funcion de Encriptamiento AES/CBS, recibe como parametro: Cadena y Llave de Encriptamiento
    * */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String encrypt(String dato, String key) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(key.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(dato.getBytes("utf-8"));
        //return new BASE64Encoder().encode(encrypted);
        return Base64.getEncoder().encodeToString(encrypted);

    }
}
