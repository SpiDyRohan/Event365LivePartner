package com.ebabu.event365live.host.DI;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.utils.Utility;
import com.grapesnberries.curllogger.CurlLoggerInterceptor;
//import com.readystatesoftware.chuck.ChuckInterceptor;

import java.io.IOException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class RetrofitModule {
    @Provides
    @Singleton
    public ApiInterface getRetrofitService(Retrofit retrofit) {
        return retrofit.create(ApiInterface.class);
    }

    @Singleton
    @Provides
    public Retrofit provideRetrofit(OkHttpClient okHttpClient)
    {
        return new Retrofit.Builder()
                .baseUrl(API.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();
    }
    @Singleton
    @Provides
    public OkHttpClient getHttpClient(Context context)
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .callTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + Utility.getSharedPreferencesString(context, API.AUTHORIZATION))
                            .addHeader("timezone", TimeZone.getDefault().getID())
                            .build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(logging)
                .build();
    }
}
