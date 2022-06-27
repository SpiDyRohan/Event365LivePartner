package com.ebabu.event365live.host.repositories;

import android.content.Context;
import android.util.Log;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {

    @Inject
    Context context;

    MutableLiveData<UserResponse> responseMutableLiveData=new MutableLiveData<>();

    private ProfileRepository() {
        App.getAppComponent().inject(this);
    }

    private  static  ProfileRepository profileRepository;

    public static ProfileRepository getInstance(){
        return profileRepository==null? profileRepository = new ProfileRepository():profileRepository ;
    }

    public MutableLiveData<UserResponse> getProfile(ApiInterface apiInterface){

        boolean hotReload=false;

        if(Utility.getSharedPreferencesBoolean(context,API.HOT_RELOAD_PROFILE)){
           Utility.setSharedPreferencesBoolean(context,API.HOT_RELOAD_PROFILE,false);
           hotReload=true;
        }

        if(!hotReload && responseMutableLiveData.getValue()!=null && responseMutableLiveData.getValue().getUserDAO()!=null)return responseMutableLiveData;

        Call<JsonElement> call=apiInterface.getProfile();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                UserResponse userResponse=new UserResponse();

                if(response.isSuccessful()){
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {

                        JSONObject OBJ= new JSONObject(response.body().toString());
                        Log.e("WIG",OBJ.toString());
                        String contactVia = "";
                        try {
                            JSONArray contactArray = OBJ.getJSONObject(API.DATA).getJSONArray("contactVia");

                            for (int i = 0; i < contactArray.length(); i++) {
                                if (contactVia.length() == 0)
                                    contactVia = contactVia.concat(contactArray.getJSONObject(i).getString("contactVia"));
                                else
                                    contactVia = contactVia.concat("," + contactArray.getJSONObject(i).getString("contactVia"));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        UserDAO dao=new Gson().fromJson(OBJ.getJSONObject(API.DATA).toString(), UserDAO.class);
                        dao.setCanContact(contactVia);
                        userResponse.setUserDAO(dao);
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
                responseMutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                UserResponse response=new UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                responseMutableLiveData.setValue(response);
            }
        });
        return responseMutableLiveData;
    }

    public MutableLiveData<UserResponse> getCustomerProfile(ApiInterface apiInterface, int id){

        MutableLiveData<UserResponse> responseMutableLiveData=new MutableLiveData<>();

        Call<JsonElement> call=apiInterface.getCustomerUserProfile(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                com.ebabu.event365live.host.entities.UserResponse userResponse=new com.ebabu.event365live.host.entities.UserResponse();

                if(response.isSuccessful()){
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {

                        JSONObject OBJ= new JSONObject(response.body().toString());
                        Log.e("WIG",OBJ.toString());
                        UserDAO dao=new Gson().fromJson(OBJ.getJSONObject(API.DATA).toString(), UserDAO.class);
                        userResponse.setUserDAO(dao);
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
                responseMutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                com.ebabu.event365live.host.entities.UserResponse response=new com.ebabu.event365live.host.entities.UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                responseMutableLiveData.setValue(response);
            }
        });
        return responseMutableLiveData;
    }

}
