package com.ebabu.event365live.host.activity.ui.home;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.HomeRepository;
import com.ebabu.event365live.host.utils.Utility;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class HomeViewModel extends AndroidViewModel {

    private HomeRepository repository;

    @Inject
    ApiInterface apiInterface;

    @Inject
    Context context;

    private MutableLiveData<Boolean> isFetching =new MutableLiveData<>();
    private MutableLiveData<UserResponse> mutableLiveData=new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);

        repository = HomeRepository.getInstance();

        Boolean hotReload=Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD);
        fetchEvents(hotReload);

        if(hotReload){
            Log.d("fnklasnf", "hotReload: ");
            Utility.setSharedPreferencesBoolean(context,API.HOT_RELOAD,false);
        }
    }

    public void fetchEvents(boolean hotReload) {
        isFetching.setValue(true);
        mutableLiveData=repository.fetchData(apiInterface,hotReload);
        isFetching.setValue(false);
    }

    public LiveData<UserResponse> getHomeData() {
        return mutableLiveData;
    }
    LiveData<Boolean> getIfFetching(){
        return isFetching;
    }
}