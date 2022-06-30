package com.ebabu.event365live.host.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.repositories.EventRepository;
import com.ebabu.event365live.host.utils.Utility;

import java.util.List;

import javax.inject.Inject;

public class PastEventViewModel  extends AndroidViewModel {

    //  private MutableLiveData<List<EventDAO>> upComingEvents;
    private EventRepository repository;
    private MutableLiveData<Boolean> _sessionExpired;

    @Inject
    ApiInterface apiInterface;

    @Inject
    Context context;

    public PastEventViewModel(@NonNull Application application) {
        super(application)
        ;
        App.getAppComponent().inject(this);
        repository=EventRepository.getInstance();
        _sessionExpired=repository.sessionExpired;

        boolean hotReload=false;

        if(Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_PAST_EVENTS)) {
//            Utility.setSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS,false);
            Log.d("fnalskfla", "PastEventViewModel: "+Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS));
            hotReload = true;
        }
        Log.d("fnalskfla", "out: "+Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS));

        fetchData(hotReload);
    }

    public LiveData<Boolean> sessionExpired(){
        return _sessionExpired;
    }

    public void resetState(){
        _sessionExpired.setValue(false);
    }

    public MutableLiveData<List<EventDAO>> fetchData(boolean hotReload){
        Log.d("AUTHENTICATION"," -- "+API.AUTH_PREFIX + Utility.getSharedPreferencesString(context, API.AUTHORIZATION));
        return repository.getPastEventData(apiInterface, API.AUTH_PREFIX + Utility.getSharedPreferencesString(context, API.AUTHORIZATION),hotReload);
    }

    public LiveData<List<EventDAO>> getPastEvents(){
        boolean hotReload=false;

        if(Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_PAST_EVENTS)) {
            // Utility.setSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS,false);
            Log.d("fnalskfla", "getPastEvents: "+Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS));
            hotReload = true;
        }
        Log.d("fnalskfla", "out:getPastEvents "+Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS));
        return fetchData(hotReload);
    }

    public LiveData<MyResponse> deleteEvent(int venueId){
        return repository.deleteEvent(apiInterface,venueId);
    }


}