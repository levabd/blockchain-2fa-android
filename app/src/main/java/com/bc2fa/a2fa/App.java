package com.bc2fa.a2fa;

import android.app.Application;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Oleg Levitsky on 07.04.2018.
 */
public class App extends Application {

    private static FAService faService;
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        OkHttpClient client = httpClient.addInterceptor(new HeaderInterceptor()).connectTimeout(100, TimeUnit.SECONDS).readTimeout(100,TimeUnit.SECONDS).build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://allatrack-tfa.tk").client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        faService = retrofit.create(FAService.class);
    }

    public static FAService getApi() {
        return faService;
    }
}
