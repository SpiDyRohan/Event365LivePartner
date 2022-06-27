package com.ebabu.event365live.host.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.SearchVenueRepository;

import javax.inject.Inject;

public class SearchVenueViewModel extends AndroidViewModel {

    private final SearchVenueRepository repository;

    @Inject
    ApiInterface apiInterface;


    public SearchVenueViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);
        repository = SearchVenueRepository.getInstance();
    }


    public LiveData<UserResponse> getFilterVenues(String lat, String log, String search, int miles) {
        return repository.getFilterVenue(apiInterface, lat, log, search, miles);
    }

}

