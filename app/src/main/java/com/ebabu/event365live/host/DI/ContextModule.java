package com.ebabu.event365live.host.DI;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.R;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {
    private Context context;
    private Application application;


    public ContextModule(Application application, Context context) {
        this.context = context;
        this.application=application;
    }

    @Provides
    @Singleton
    public Context getContext(){
        return context;
    }

    @Provides
    @Singleton
    public Application getApplication(){
        return application;
    }

    @Provides
    @Singleton
    public static RequestOptions provideRequestOptions(){
        return  new RequestOptions()
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.error_img);
    }

    @Provides
    @Singleton
    public static RequestManager provideGlideInstance(Application application, RequestOptions options){
        return Glide.with(application)
                .setDefaultRequestOptions(options);
    }
}
