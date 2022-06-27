package com.ebabu.event365live.host.DI;

import android.app.Application;
import android.content.Context;

import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.google.android.libraries.places.api.Places;
import com.splunk.mint.Mint;
import com.stripe.android.PaymentConfiguration;

public class App extends Application {
    private static  AppComponent appComponent;
    public static Context context;
    public static CreateEventDAO createEventDAO = new CreateEventDAO();
    public static AppComponent getAppComponent(){
        return appComponent;
    }

    //*******************SANDBOX*******************
    //private String stripe_published_key = "pk_test_HVbzOPm3XvLdfPXver27u18D00EAuKW80q";
    //*******************LIVE**********************
    private String stripe_published_key = "pk_live_SA1hCRyhG9jwKOv5otXSEylr00ZREyZFGr";


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        appComponent=buildComponent();
        try {
            Mint.setApplicationEnvironment(Mint.appEnvironmentRelease);
//            Mint.initAndStartSession(this, "ecfd2ff1");
            Mint.initAndStartSession(this, "4648191d");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!Places.isInitialized()) {
            String apiKey = "AIzaSyDnzMr8HJEL5gCdH8UnIEC0JrSugVsGysQ";
            Places.initialize(getApplicationContext(), apiKey);
        }

        PaymentConfiguration.init(
                getApplicationContext(),
                stripe_published_key
        );

    }
    protected AppComponent buildComponent(){
        return DaggerAppComponent.builder()
                        .retrofitModule(new RetrofitModule())
                        .contextModule(new ContextModule(this,getApplicationContext()))
                        .build();
    }
}
