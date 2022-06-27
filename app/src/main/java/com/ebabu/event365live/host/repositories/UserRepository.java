package com.ebabu.event365live.host.repositories;

import android.util.Log;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.entities.UserResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    MutableLiveData<UserResponse> users = new MutableLiveData<>();
    MutableLiveData<MyResponse> isAdded = new MutableLiveData<>();

    List<UserDAO> userDAOList = new ArrayList<>();

    private UserRepository() {
    }

    private static UserRepository userRepository;

    public static UserRepository getInstance() {
        return userRepository == null ? userRepository = new UserRepository() : userRepository;
    }

    /*
        if priority is 0, and View model has data already, It'll not call API, instead return the samedata
     */
    public MutableLiveData<UserResponse> getUsers(ApiInterface apiInterface) {

        Call<JsonElement> call = apiInterface.getUsers();

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                Log.e("got result", "yes");
                userDAOList.clear();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (OBJ.getJSONArray(API.DATA).length() > 0) {
                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                userDAOList.add(new Gson().fromJson(s, UserDAO.class));
                            }
                        }
                        userResponse.setUsers(userDAOList);
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
                users.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                UserResponse response = new UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                userDAOList.clear();
                users.setValue(response);
            }
        });
        return users;
    }

    public MutableLiveData<UserResponse> getUserById(ApiInterface apiInterface, int id) {
        MutableLiveData<UserResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.getManagedUser(id);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                UserResponse userResponse = new UserResponse();
                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    userResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());

                        JSONObject data = OBJ.getJSONObject(API.DATA);
                        userResponse.setMessage(data.getString("roles"));
                        UserDAO dao = new Gson().fromJson(data.toString(), UserDAO.class);
                        userResponse.setUserDAO(dao);
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
                myResponseMutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                UserResponse response = new UserResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                myResponseMutableLiveData.setValue(response);
            }
        });
        return myResponseMutableLiveData;
    }

    public MutableLiveData<MyResponse> addUser(ApiInterface apiInterface, JsonObject jsonObject) {
        isAdded.setValue(null);
        Call<JsonElement> call = apiInterface.addUser(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponse.setCode(OBJ.getInt(API.CODE));
                        isAdded.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        isAdded.setValue(new MyResponse("Something went wrong!"));
                    }
                } else {
                    try {
                        MyResponse myResponse = new MyResponse();
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        isAdded.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        isAdded.setValue(new MyResponse("Something went wrong!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                isAdded.setValue(new MyResponse("Something went wrong!"));
            }
        });
        return isAdded;
    }

    public MutableLiveData<MyResponse> updateUser(ApiInterface apiInterface, int id, JsonObject jsonObject) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();
        Call<JsonElement> call = apiInterface.updateUser(id, jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
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

    public MutableLiveData<MyResponse> deleteUser(ApiInterface apiInterface, int id) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.deleteUser(id);
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

}
