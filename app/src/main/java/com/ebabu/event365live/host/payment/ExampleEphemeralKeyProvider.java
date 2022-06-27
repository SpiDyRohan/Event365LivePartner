package com.ebabu.event365live.host.payment;

import android.util.Log;

import com.ebabu.event365live.host.api.ApiInterface;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExampleEphemeralKeyProvider implements EphemeralKeyProvider {

    @Inject
    ApiInterface apiInterface;

    @Override
    public void createEphemeralKey(@NonNull @Size(min = 4) String apiVersion,
            @NonNull final EphemeralKeyUpdateListener keyUpdateListener) {

        final Map<String, String> apiParamMap = new HashMap<>();
        apiParamMap.put("api_version", apiVersion);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_version", apiVersion);
        jsonObject.addProperty("customer", "cid");

        Call<JsonElement> jsonElementCall = apiInterface.GetEphemeralKey(jsonObject);
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
//                        final String rawKey = response.string();
                        final String rawKey = response.body().toString();
                        keyUpdateListener.onKeyUpdate(rawKey);
                    } catch (Exception ignored) {
                    }
                } else if (response.code() == 406) {

                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        String msg = errorObj.getString("message");
                        if (msg.equalsIgnoreCase("Invalid sort code")) {

                            Log.d("nflkasnklfa", "onFailure:  Invalid routing no."  );

                            return;
                        }
                        Log.d("nflkasnklfa", "onFailure:  Something went wrong"  );

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("nflkasnklfa", "onFailure:  Something went wrong"  );
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("nflkasnklfa", "onFailure:  Something went wrong"  );
                    }


                } else {
                    Log.d("nflkasnklfa", "onFailure: "  );
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

                Log.d("nflkasnklfa", "onFailure: " + t.getMessage());
            }
        });


    }
}
