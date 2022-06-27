package com.ebabu.event365live.host.repositories;

import android.util.Log;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchVenueRepository {

    MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
    List<VenueDAO> filterList = new ArrayList<>();
    private static SearchVenueRepository repository = null;

    private SearchVenueRepository() {
    }

    public static SearchVenueRepository getInstance() {
        if (repository == null)
            repository = new SearchVenueRepository();
        return repository;
    }

    public MutableLiveData<UserResponse> getFilterVenue(ApiInterface apiInterface,String lat, String log, String search, int miles) {
        Call<JsonElement> call = apiInterface.getFilterVenues(lat, log, search, miles);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                filterList.clear();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        Log.d("fnkasfkafa", "onResponse: " + response.body().toString());
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (OBJ.getJSONArray(API.DATA).length() > 0) {
                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                VenueDAO dao = new Gson().fromJson(s, VenueDAO.class);
                                try {
                                    dao.setImgPath(data.getJSONObject(i).getJSONArray("venueImages").getJSONObject(0).getString("venueImages"));
                                } catch (Exception ignored) {
                                }
                                filterList.add(dao);
                            }
                        }
                        userResponse.setFilterVenueList(filterList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                UserResponse response = new UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                filterList.clear();
                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }


}
