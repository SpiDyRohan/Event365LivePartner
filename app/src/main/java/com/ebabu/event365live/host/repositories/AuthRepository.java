package com.ebabu.event365live.host.repositories;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private MutableLiveData<String> signUpResponse = new MutableLiveData<>();
    private MutableLiveData<String> loginResponse = new MutableLiveData<>();
    private ApiInterface apiInterface;

    public AuthRepository(ApiInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    public LiveData<String> signup(String username, String email, String password, String userType) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(API.NAME, username);
        jsonObject.addProperty(API.EMAIL, email);
        jsonObject.addProperty(API.PASSWORD, password);
        jsonObject.addProperty(API.USER_TYPE, userType);

        Call<JsonElement> call = apiInterface.signup(jsonObject);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful())
                    signUpResponse.setValue(response.body().toString());

                else {
                    try {
                        signUpResponse.setValue(response.errorBody().string());
                    } catch (Exception e) {
                        signUpResponse.setValue("something went wrong!");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                signUpResponse.setValue(t.getMessage());
            }
        });
        return signUpResponse;
    }

    public LiveData<String> login(Context context, String email, String password, String deviceToken, String deviceId, String sourceIp) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(API.EMAIL, email);
        jsonObject.addProperty(API.PASSWORD, password);
        jsonObject.addProperty(API.DEVICE_TOKEN, deviceToken);
        jsonObject.addProperty(API.DEVICE_TYPE, "android");
        jsonObject.addProperty("OS", "android");
        jsonObject.addProperty("platform", "playstore");
        jsonObject.addProperty("deviceId", deviceId);
        jsonObject.addProperty("sourceIp", sourceIp);

        Call<JsonElement> call = apiInterface.login(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    String auth = response.headers().get(API.AUTHORIZATION);
                    Log.e("Login Success", "onResponse: " + auth);
                    Utility.setSharedPreferencesString(context, API.AUTHORIZATION, auth);
                    loginResponse.setValue(response.body().toString());
                } else {
                    try {
                        String data = response.errorBody().string();
                        Log.e("got", data);
                        loginResponse.setValue(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginResponse.setValue("something went wrong!");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loginResponse.setValue(t.getMessage());
            }
        });
        return loginResponse;
    }

    public LiveData<String> requestForOTP(int id, String phone, String countryCode) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(API.ID, id);
        jsonObject.addProperty(API.COUNTRY_CODE, countryCode);
        jsonObject.addProperty(API.PHONE_NO, phone);

        Call<JsonElement> call = apiInterface.sendPhoneOTP(jsonObject);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    loginResponse.setValue(response.body().toString());
                } else {
                    try {
                        String data = response.errorBody().string();
                        Log.e("got", data);
                        loginResponse.setValue(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginResponse.setValue("something went wrong!");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loginResponse.setValue(t.getMessage());
            }
        });
        return loginResponse;
    }

    public LiveData<String> verifyPhone(Context context, int id, String phone, String OTP, String countryCode, String selectedCountryNameCode) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(API.ID, id);
        jsonObject.addProperty(API.COUNTRY_CODE, countryCode);
        jsonObject.addProperty(API.PHONE_NO, phone);
        jsonObject.addProperty(API.OTP, OTP);
        jsonObject.addProperty(API.COUNTRY, selectedCountryNameCode);
        jsonObject.addProperty(API.CURRENCY_CODE, currencyCode(context, selectedCountryNameCode));
        jsonObject.addProperty(API.DEVICE_TOKEN, Utility.getSharedPreferencesString(context, API.FIREBASE_TOKEN));
        jsonObject.addProperty(API.DEVICE_TYPE, "android");
        jsonObject.addProperty("OS", "android");
        jsonObject.addProperty("platform", "playstore");
        jsonObject.addProperty("deviceId", Utility.getSharedPreferencesString(context, API.DEVICE_ID));
        jsonObject.addProperty("sourceIp",Utility.getSharedPreferencesString(context, API.SOURCE_IP));


        Call<JsonElement> call = apiInterface.verifyPhone(jsonObject);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {

                    String auth = response.headers().get(API.AUTHORIZATION);
                    Log.e("Login Success", "onResponse: " + auth);
                    Utility.setSharedPreferencesString(context, API.AUTHORIZATION, auth);

                    loginResponse.setValue(response.body().toString());
                } else {
                    try {
                        String data = response.errorBody().string();
                        Log.e("got", data);
                        loginResponse.setValue(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginResponse.setValue("something went wrong!");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loginResponse.setValue(t.getMessage());
            }
        });
        return loginResponse;
    }

    private String currencyCode(Context context, String countryCode) {
        String currencyCode = "";
        try {
            InputStream is = context.getAssets().open("currency.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(countryCode)) {
                currencyCode = jsonObject.getString(countryCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currencyCode;
    }

    public LiveData<String> socialLogin(Context context, String name, String socialEmail, String socialId, String socialLoginType, String deviceToken, String userType) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(API.EMAIL, socialEmail);
        jsonObject.addProperty(API.SOCIAL_USER_ID, socialId);
        jsonObject.addProperty(API.NAME, name);
        jsonObject.addProperty(API.DEVICE_TOKEN, deviceToken);
        jsonObject.addProperty(API.LOGIN_TYPE, socialLoginType);
        jsonObject.addProperty(API.DEVICE_TYPE, "android");
        if(!TextUtils.isEmpty(userType))
        jsonObject.addProperty(API.USER_TYPE, userType);

        Call<JsonElement> call = apiInterface.socialLogin(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    String auth = response.headers().get(API.AUTHORIZATION);
                    Utility.setSharedPreferencesString(context, API.AUTHORIZATION, auth);
                    if(TextUtils.isEmpty(userType))
                        loginResponse.setValue(response.body().toString());
                    else
                        signUpResponse.setValue(response.body().toString());
                } else {
                    try {
                        String data = response.errorBody().string();
                        if(TextUtils.isEmpty(userType))
                            loginResponse.setValue(data);
                        else
                            signUpResponse.setValue(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(TextUtils.isEmpty(userType))
                            loginResponse.setValue("something went wrong!");
                        else
                            signUpResponse.setValue("something went wrong!");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                if(TextUtils.isEmpty(userType))
                    loginResponse.setValue(t.getMessage());
                else
                    signUpResponse.setValue(t.getMessage());
            }
        });
        if(TextUtils.isEmpty(userType))
            return loginResponse;
        else
            return signUpResponse;
    }

}
