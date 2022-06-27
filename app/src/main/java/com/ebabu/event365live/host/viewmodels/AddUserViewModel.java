package com.ebabu.event365live.host.viewmodels;

import android.app.Application;
import android.content.Context;
import android.view.View;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.UserRepository;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.inject.Inject;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AddUserViewModel extends AndroidViewModel {
   public  String firstName,lastName,mail,phone,pwd,userType;
    private UserRepository repository;
    private MutableLiveData<MyResponse> mutableLiveData;
    @Inject
    ApiInterface apiInterface;

    @Inject
    Context context;
    public AddUserViewModel(@NonNull Application application) {
        super(application);
        this.userType="host";
        App.getAppComponent().inject(this);
        repository=UserRepository.getInstance();

    }

    public LiveData<MyResponse> go(JsonArray userArray){

        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty(API.FIRST_NAME,firstName);
        jsonObject.addProperty(API.LAST_NAME,lastName);
        jsonObject.addProperty(API.PHONE_NO,phone);
        jsonObject.addProperty(API.EMAIL,mail);
        jsonObject.addProperty(API.PASSWORD,pwd);
        jsonObject.addProperty(API.USER_TYPE,userType);


        jsonObject.add(API.ROLES,userArray);

        mutableLiveData=repository.addUser(apiInterface,jsonObject);
        return mutableLiveData;
    }

    public LiveData<UserResponse> getUserById(int id){
        return repository.getUserById(apiInterface,id);
    }

    public LiveData<MyResponse> updateUser(int id,JsonObject object){
        return repository.updateUser(apiInterface,id,object);
    }

    public void setUserType(View view){
        Chip chip=(Chip)view;
        userType=chip.getText().toString().toLowerCase();
    }

}
