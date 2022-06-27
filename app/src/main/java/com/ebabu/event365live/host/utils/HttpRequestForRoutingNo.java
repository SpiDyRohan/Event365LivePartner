package com.ebabu.event365live.host.utils;

import com.ebabu.event365live.host.api.API;
import com.google.gson.JsonElement;
import com.grapesnberries.curllogger.CurlLoggerInterceptor;

import java.io.IOException;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class HttpRequestForRoutingNo {
    private static Retrofit retrofit;

    private static Retrofit getRetrofitInstance(String baseUrl) {

        if (retrofit == null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient())
                    .build();
        return retrofit;
    }

    public static Call<JsonElement> getRoutingApi(String routingNo) {
        return getRetrofitInstance(API.ROUTING_NO_BASE_URL).create(RoutingApiInterface.class).getRoutingNo(routingNo);
    }

    public static Call<JsonElement> getLinkAccount(String auth) {
        return getRetrofitInstance(API.BASE_URL).create(RoutingApiInterface.class).linkAccount(auth);
    }

    interface RoutingApiInterface {
        @GET(API.GET_ROUTING_NO)
        Call<JsonElement> getRoutingNo(@Query("rn") String routingNo);

        @POST(API.ACCOUNT_LINK)
        Call<JsonElement> linkAccount(@Header(API.AUTHORIZATION) String token);
    }

    private static OkHttpClient getHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .writeTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(new CurlLoggerInterceptor("CURL_LOG"))
                .build();
    }

    private static OkHttpClient getTimeZoneHeader() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.networkInterceptors().add(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.header("Timezone", TimeZone.getDefault().toString());
            return chain.proceed(requestBuilder.build());
        });
        return httpClient;
    }
}
