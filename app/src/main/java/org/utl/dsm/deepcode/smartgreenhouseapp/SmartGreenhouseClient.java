package org.utl.dsm.deepcode.smartgreenhouseapp;

import org.utl.dsm.deepcode.smartgreenhouseapp.globals.Globals;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SmartGreenhouseClient {
    private static final String BASE_URL = Globals.BASE_URL; // Dirección especial para el emulador
//    private static final String BASE_URL = "http://10.16.30.174:8080/"; // URL de la API local
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}