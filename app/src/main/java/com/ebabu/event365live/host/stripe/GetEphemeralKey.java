package com.ebabu.event365live.host.stripe;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.RetrofitModule;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonObject;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetEphemeralKey implements EphemeralKeyProvider {
    private Activity activity;

    ApiInterface apiInterface;

    public GetEphemeralKey(Activity activity, ApiInterface apiInterface) {
        this.activity = activity;
        this.apiInterface = apiInterface;
    }

    RetrofitModule instance;

//    private BackendApi backendApi;
//    private BackendApi backendApi = ApiClient.getClient().create(BackendApi.class);
//    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void createEphemeralKey(
            @NonNull @Size(min = 4)  String apiVersion,
            @NonNull EphemeralKeyUpdateListener keyUpdateListener) {

        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("api_version", apiVersion);
            jsonObject.addProperty("customer", Utility.getSharedPreferencesString(activity, Constants.CUSTOMER_ID));

            Call call = apiInterface.GetEphemeralKey(jsonObject);

            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {

                    Log.e("got result", "yes");
                    Log.e("got result", "GetEphemeralKey response> " + response);

                    if (response.isSuccessful()) {

                        try {
                            JSONObject stripesRawJSON = new JSONObject(response.body().toString());
                            if (stripesRawJSON.has("data")) {
                                JSONObject getRawObj = stripesRawJSON.getJSONObject("data");
                                keyUpdateListener.onKeyUpdate(getRawObj.toString());
                                //keyUpdateListener.onKeyUpdate(rawKey);
                                Log.d("fnalknfklsa", "createEphemeralKey: " + getRawObj.toString());
                            }

                        } catch (Exception ignored) {
                            Toast.makeText(activity, activity.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(activity, activity.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(activity, activity.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
