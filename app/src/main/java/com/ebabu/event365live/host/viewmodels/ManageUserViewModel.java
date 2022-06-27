package com.ebabu.event365live.host.viewmodels;

import android.app.Application;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.UserRepository;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ManageUserViewModel extends AndroidViewModel{

    private MutableLiveData<UserResponse> mutableLiveData;
    private UserRepository repository;

    @Inject
    ApiInterface apiInterface;

    public ManageUserViewModel(@NonNull Application application) {
        super(application);

        App.getAppComponent().inject(this);
        repository=UserRepository.getInstance();
        fetchUser();
    }

    public void fetchUser(){
        mutableLiveData=repository.getUsers(apiInterface);
    }

    public LiveData<MyResponse> deleteUser(int userId){
        return repository.deleteUser(apiInterface,userId);
    }

    public LiveData<UserResponse> getUsers(){
        return mutableLiveData;
    }
}
