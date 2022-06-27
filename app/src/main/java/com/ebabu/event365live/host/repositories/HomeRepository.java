package com.ebabu.event365live.host.repositories;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserData;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import androidx.lifecycle.MutableLiveData;

import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository{

    MutableLiveData<UserResponse> mutableLiveData=new MutableLiveData<>();
    private static HomeRepository homeRepository=null;
    private HomeRepository(){}
    private UserData user;

    public static HomeRepository getInstance(){
        if (homeRepository==null)
            homeRepository=new HomeRepository();

        return homeRepository;
    }

    public MutableLiveData<UserResponse> fetchData(ApiInterface apiInterface, Boolean hotReload){
        if(!hotReload && mutableLiveData.getValue()!=null && mutableLiveData.getValue().getUserData()!=null) return mutableLiveData;

        Call<JsonElement> call=apiInterface.getUserHomeDetail();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse=new UserResponse();

                if(response.isSuccessful()){
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ= new JSONObject(response.body().toString());
                        JSONObject data=OBJ.getJSONObject(API.DATA);

                        if(data.length()>0){
                            Utility.setSharedPreferencesString(App.context, Constants.USER_LOGIN_DATA, data.getJSONObject("user").toString());
                            user=new Gson().fromJson(data.toString(), UserData.class);
                        }

                        userResponse.setUserData(user);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ= new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                UserResponse response=new UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(response);
            }
        });

        return mutableLiveData;
    }

}
