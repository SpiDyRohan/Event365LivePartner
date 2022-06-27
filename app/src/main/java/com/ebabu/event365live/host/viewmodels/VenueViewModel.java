package com.ebabu.event365live.host.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.VenueRepository;
import com.google.gson.JsonObject;

import javax.inject.Inject;

public class VenueViewModel extends AndroidViewModel {


    private final VenueRepository repository;

    @Inject
    ApiInterface apiInterface;


    public VenueViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);
        repository = VenueRepository.getInstance();
    }

    public LiveData<UserResponse> getMyVenues(boolean isVenuer,String from) {
        return getAllVenues(true, isVenuer,from);
    }
//
//    public LiveData<UserResponse> getFilterVenues(String lat, String log, String search, int miles) {
//        return getFilterVenues(false, lat, log, search, miles);
//    }

    public LiveData<UserResponse> getVenueDetail(int venueId,String startDate,String endDate) {
        return repository.getVeneuDetail(apiInterface, venueId,startDate,endDate);
    }

    public LiveData<UserResponse> getSubVenueDetail(int venueId,String startDate,String endDate) {
        return repository.getSubVenueDetail(apiInterface, venueId,startDate,endDate);
    }

    public LiveData<UserResponse> getAllVenues(boolean hotReload, boolean isVenuer,String from) {
        return repository.getVenue(apiInterface, hotReload, isVenuer,from);
    }

//    public LiveData<UserResponse> getFilterVenues(boolean hotReload, String lat, String log, String search, int miles) {
//        return repository.getFilterVenue(apiInterface, hotReload, lat, log, search, miles);
//    }

    public LiveData<MyResponse> deleteVenue(int venueId) {
        return repository.deleteVenue(apiInterface, venueId);
    }

    public LiveData<UserResponse> getVenueImages(int venueId) {
        return repository.getVenueImages(apiInterface, venueId);
    }


    public LiveData<UserResponse> lockSubVenue(JsonObject jsonObject) {
        return repository.lockSubVenue(apiInterface, jsonObject);
    }


}

