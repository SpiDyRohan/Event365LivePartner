package com.ebabu.event365live.host.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.repositories.EventRepository;
import com.ebabu.event365live.host.utils.Utility;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UpcomingEventsViewModel extends AndroidViewModel{

    //private MutableLiveData<List<EventDAO>> upComingEvents;
    private MutableLiveData<Boolean> _sessionExpired;

    private EventRepository repository;

    @Inject
    ApiInterface apiInterface;

    @Inject
    Context context;


    public UpcomingEventsViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);
        repository=EventRepository.getInstance();
        _sessionExpired=repository.sessionExpired;

        boolean hotReload=false;

        if(Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS))
            hotReload=true;

        fetchData(hotReload);
    }

    public LiveData<Boolean> sessionExpired(){
        return _sessionExpired;
    }

    public LiveData<List<EventDAO>> getUpComingEvents(){
        boolean hotReload=false;

        Log.d("fnsaokfnka", "getUpComingEvents: "+Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS));
        if(Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS)){
            //Utility.setSharedPreferencesBoolean(context,API.HOT_RELOAD_EVENTS,false);
            hotReload=true;
        }


        return fetchData(hotReload);
    }

    public MutableLiveData<List<EventDAO>> fetchData(boolean hotReload){
        return repository.getUpComingEventData(apiInterface, hotReload);
    }

    public LiveData<MyResponse> deleteEvent(int venueId){
        return repository.deleteEvent(apiInterface,venueId);
    }

    public void resetState(){
        _sessionExpired.setValue(false);
    }
}
