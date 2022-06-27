package com.ebabu.event365live.host.repositories;

import android.util.Log;
import android.widget.Toast;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.SubVenueDao;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

public class VenueRepository {

    MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
    List<VenueDAO> list = new ArrayList<>();
    List<VenueDAO> previouslyUsedList = new ArrayList<>();
    List<SubVenueDao> subVenueList = new ArrayList<>();
    private static VenueRepository repository = null;

    private VenueRepository() {
    }

    public static VenueRepository getInstance() {
        if (repository == null)
            repository = new VenueRepository();

        return repository;
    }

    public MutableLiveData<UserResponse> getVenue(ApiInterface apiInterface, boolean hotReload, boolean isVenuer,String from) {
        if (!hotReload && mutableLiveData.getValue() != null && mutableLiveData.getValue().getMyVenueList() != null)
            return mutableLiveData;

        Call<JsonElement> call;
       /* if(from.equalsIgnoreCase("profile"))
            call = apiInterface.getUserVenues();
        else
            call = apiInterface.getMyVenues();*/
        call = apiInterface.getUserVenues();

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                list.clear();
                previouslyUsedList.clear();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (!isVenuer) {
                            if (OBJ.getJSONArray(API.DATA).length() > 0) {
                                JSONArray data = OBJ.getJSONArray(API.DATA);
                                for (int i = 0; i < data.length(); i++) {
                                    String s = data.getJSONObject(i).toString();
                                    VenueDAO dao = new Gson().fromJson(s, VenueDAO.class);
                                    try {
                                        dao.setImgPath(data.getJSONObject(i).getJSONArray("venueImages").getJSONObject(0).getString("venueImages"));
                                    } catch (Exception ignored) {
                                    }
                                    list.add(dao);
                                }
                            }
                        } else {

                            if (!from.equalsIgnoreCase("profile") && OBJ.getJSONObject(API.DATA).getJSONArray(API.PREVIOUSLY_USED).length() > 0) {
                                JSONArray data = OBJ.getJSONObject(API.DATA).getJSONArray(API.PREVIOUSLY_USED);
                                for (int i = 0; i < data.length(); i++) {
                                    String s = data.getJSONObject(i).toString();
                                    VenueDAO dao = new Gson().fromJson(s, VenueDAO.class);
                                    try {
                                        dao.setImgPath(data.getJSONObject(i).getJSONArray("venueImages").getJSONObject(0).getString("venueImages"));
                                    } catch (Exception ignored) {
                                    }
                                    previouslyUsedList.add(dao);
                                }
                            }
                            if (OBJ.getJSONObject(API.DATA).getJSONArray(API.MY_VENEUS).length() > 0) {
                                JSONArray data = OBJ.getJSONObject(API.DATA).getJSONArray(API.MY_VENEUS);
                                for (int i = 0; i < data.length(); i++) {
                                    String s = data.getJSONObject(i).toString();
                                    VenueDAO dao = new Gson().fromJson(s, VenueDAO.class);
                                    try {
                                        dao.setImgPath(data.getJSONObject(i).getJSONArray("venueImages").getJSONObject(0).getString("venueImages"));
                                    } catch (Exception ignored) {
                                    }
                                    list.add(dao);
                                }
                            }
                        }
                        userResponse.setMyVenueList(list);
                        userResponse.setPreviouslyUsedVenueList(previouslyUsedList);
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
                list.clear();
                previouslyUsedList.clear();
                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getVenueImages(ApiInterface apiInterface, int id) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();


        Call<JsonElement> call = apiInterface.getVenueImages(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                List<String> images = new ArrayList<>();

                UserResponse userResponse = new UserResponse();

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        Log.d("fnasklfnsa", "onResponse: " + response.body());

                        if (OBJ.getJSONArray(API.DATA).length() > 0) {

                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            if (data.length() > 0) {
                                for (int i = 0; i < data.length(); i++) {
                                    images.add(data.getJSONObject(i).getString("venueImages"));
                                }
                            }
                        }
                        userResponse.setVenueImages(images);
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
                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getVeneuDetail(ApiInterface apiInterface, int venueId,String startDate, String endDate) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.getVenueDetail(venueId,startDate,endDate);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                VenueDAO dao;
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);

                        dao = new Gson().fromJson(data.toString(), VenueDAO.class);

                        if( dao.getSubVenues()==null && dao.getSubVenues().size()<=0) {
                            if(dao.getIsBooked().equalsIgnoreCase("Yes"))
                                Toast.makeText(getApplicationContext(), OBJ.getString("message"), Toast.LENGTH_LONG).show();
                        }

                        dao.setAvailableDays(data.getJSONArray("daysAvailable").toString());

                        userResponse.setVenueDAO(dao);
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

                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<MyResponse> deleteVenue(ApiInterface apiInterface, int id) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.deleteVenue(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.DATA));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponse.setCode(OBJ.getInt(API.CODE));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                } else {
                    try {
                        MyResponse myResponse = new MyResponse();
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
            }
        });
        return myResponseMutableLiveData;
    }

    public MutableLiveData<UserResponse> lockSubVenue(ApiInterface apiInterface, JsonObject jsonObject) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.lockSubVenue(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    userResponse.setMessage(response.message());
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

                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getSubVenueDetail(ApiInterface apiInterface, int venueId,String startDate,String endDate) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.subVenueDetail(venueId,startDate,endDate);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                subVenueList.clear();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (OBJ.getJSONArray(API.DATA).length() > 0) {
                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                SubVenueDao venueDao = new Gson().fromJson(s, SubVenueDao.class);
                                subVenueList.add(venueDao);
                            }
                        }
                        userResponse.setSubVenueDaoList(subVenueList);
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

                mutableLiveData.setValue(response);
            }
        });
        return mutableLiveData;
    }
}
