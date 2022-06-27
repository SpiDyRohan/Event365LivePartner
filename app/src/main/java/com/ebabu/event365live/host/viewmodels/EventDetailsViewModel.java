package com.ebabu.event365live.host.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.EventRepository;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.JsonObject;

import javax.inject.Inject;

public class EventDetailsViewModel extends AndroidViewModel implements AppBarLayout.OnOffsetChangedListener {

    @Inject
    ApiInterface apiInterface;

    MutableLiveData<Boolean> changeColor = new MutableLiveData<>();

    EventRepository repository;

    public EventDetailsViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);

        changeColor.setValue(false);
        repository = EventRepository.getInstance();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if ((verticalOffset >= (appBarLayout.getTotalScrollRange() - 100) * -1)) {
            if (changeColor.getValue())
                changeColor.setValue(false);
        } else {
            if (!changeColor.getValue())
                changeColor.setValue(true);
        }
    }

    public LiveData<UserResponse> getMoreEventDetail(int eventId) {
        return repository.getMoreEventDetail(apiInterface, eventId);
    }

    public LiveData<UserResponse> getEventDetail(int eventId) {
        return repository.getEventDetail(apiInterface, eventId);
    }

    public LiveData<MyResponse> changeEventAvailability(int eventId, boolean available) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", eventId);
        jsonObject.addProperty("is_availability", available);

        return repository.changeAvailabilityStatus(apiInterface, jsonObject);
    }

    public LiveData<Boolean> getOnChangeColor() {
        return changeColor;
    }
}
