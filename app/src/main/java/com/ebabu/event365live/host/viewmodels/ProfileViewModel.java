package com.ebabu.event365live.host.viewmodels;

import android.app.Application;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.ProfileRepository;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProfileViewModel extends AndroidViewModel {

    private MutableLiveData<UserResponse> profileLiveData;

    @Inject
    ApiInterface apiInterface;

    ProfileRepository repository;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        App.getAppComponent().inject(this);
        repository= ProfileRepository.getInstance();
    }

    public LiveData<UserResponse> getProfile(){
        return repository.getProfile(apiInterface);
    }

    public LiveData<UserResponse> getCustomerProfile(int id){
        return repository.getCustomerProfile(apiInterface, id);
    }
}
