package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityVerifiyCodeBinding;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.VerifyViewModel;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifiyCode extends AppCompatActivity {

    ActivityVerifiyCodeBinding binding;
    private CountDownTimer resendTimer;
    String from;
    MyLoader loader;
    String phone, countryCode, selectedCountryNameCode;
    int id;

    String mail;
    String IPaddress;

    @Inject
    ApiInterface apiInterface;

    private static final String TAG = "VerifiyCode";

    VerifyViewModel verifyViewModel;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verifiy_code);
        verifyViewModel = ViewModelProviders.of(this).get(VerifyViewModel.class);

        App.getAppComponent().inject(this);
        binding.backImg.setOnClickListener(v -> onBackPressed());
        verifyViewModel.init(apiInterface, getApplicationContext());
        getFirebaseToken();
        getDeviceId();
        NetworkDetect();
        from = getIntent().getStringExtra(Constants.FROM);

        if (API.SIGNUP.equalsIgnoreCase(from) || API.FORGOT_PASSWORD.equalsIgnoreCase(from)) {
            mail = getIntent().getStringExtra(API.EMAIL);
            Log.e("mail mil gaya", mail);
        } else
            binding.textView16.setText(getString(R.string.verify_desc_phone));

        phone = getIntent().getStringExtra(API.PHONE_NO);
        countryCode = getIntent().getStringExtra(API.COUNTRY_CODE);
        id = getIntent().getIntExtra(API.ID, -1);
        if (getIntent().hasExtra("countryNameCode")) {
            selectedCountryNameCode = getIntent().getStringExtra("countryNameCode");
        }

        loader = new MyLoader(this);

        startTimer();


        binding.resendTv.setOnClickListener(v -> {
            loader.show("");

            if (API.SIGNUP.equalsIgnoreCase(from) || API.EMAIL.equalsIgnoreCase(from) || API.FORGOT_PASSWORD.equalsIgnoreCase(from))
                resendOtpForMail(v);
            else
                resendOtpForPhone(v);
        });


        binding.createAccBtn.setOnClickListener(v -> {


            if (binding.firstPinView.getText().toString().length() > 0) {
                if (API.SIGNUP.equalsIgnoreCase(from)) {

                    loader.show("");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(API.ID, id);
                    jsonObject.addProperty(API.OTP, binding.firstPinView.getText().toString());
                    jsonObject.addProperty(API.DEVICE_TOKEN, Utility.getSharedPreferencesString(this, API.FIREBASE_TOKEN));
                    jsonObject.addProperty(API.DEVICE_TYPE, "android");
                    jsonObject.addProperty("OS", "android");
                    jsonObject.addProperty("platform", "playstore");
                    jsonObject.addProperty("deviceId", Utility.getSharedPreferencesString(this, API.DEVICE_ID));
                    jsonObject.addProperty("sourceIp",Utility.getSharedPreferencesString(this, API.SOURCE_IP));

                    Log.d(TAG, binding.firstPinView.getText().toString() + " onCreate: " + id);

                    Call<JsonElement> call = apiInterface.verifyEmail(jsonObject);
                    call.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                            loader.dismiss();


                            if (response.isSuccessful()) {
                                try {
                                    JSONObject object = new JSONObject(response.body().toString());
                                    msg = object.getString(API.MESSAGE);
                                    String auth = response.headers().get(API.AUTHORIZATION);
                                    Log.e("auth", auth);
                                    Utility.setSharedPreferencesString(getApplicationContext(), API.AUTHORIZATION, auth);

                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(VerifiyCode.this, SMSVerification.class)
                                            .putExtra(API.ID, id)
                                            .putExtra(Constants.FROM, API.VERIFY_OTP)
                                    );
                                    finish();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    JSONObject object = new JSONObject(response.errorBody().string());

                                    if (object.getInt(API.CODE) == 434) {
                                        Intent intent = new Intent(VerifiyCode.this, SMSVerification.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra(API.ID, id);
                                        intent.putExtra(Constants.FROM, API.VERIFY_OTP);
                                        startActivity(intent);
                                    } else {
                                        Dialogs.toast(getApplicationContext(), binding.createAccBtn, object.getString(API.MESSAGE));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {
                            loader.dismiss();
                            Dialogs.toast(getApplicationContext(), binding.createAccBtn, "Something went wrong!");
                            t.printStackTrace();
                            Log.d(TAG, "onFailure: " + t.getMessage());
                        }
                    });

                } else if (API.FORGOT_PASSWORD.equalsIgnoreCase(from)) {
                    loader.show("");
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(API.ID, id);
                    jsonObject.addProperty(API.OTP, binding.firstPinView.getText().toString());
                    Call<JsonElement> jsonElementCall = apiInterface.verifyResetPassword(jsonObject);
                    jsonElementCall.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                            loader.dismiss();
                            if (response.isSuccessful()) {
                                try {
                                    JSONObject OBJ = new JSONObject(response.body().toString());
                                    Toast.makeText(getApplicationContext(), OBJ.getString(API.MESSAGE), Toast.LENGTH_LONG).show();
                                    startActivity(
                                            new Intent(VerifiyCode.this, ResetPassword.class)
                                                    .putExtra(API.ID, id)
                                                    .putExtra(API.EMAIL, mail)
                                    );
                                } catch (JSONException e) {
                                    Dialogs.toast(getApplicationContext(), v, getString(R.string.something_went_wrong));
                                }
                            } else {
                                try {
                                    JSONObject OBJ = new JSONObject(response.errorBody().string());
                                    Dialogs.toast(getApplicationContext(), v, OBJ.getString(API.MESSAGE));
                                } catch (Exception e) {
                                    Dialogs.toast(getApplicationContext(), v, getString(R.string.something_went_wrong));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {
                            Dialogs.toast(getApplicationContext(), binding.createAccBtn, "Something went wrong!");
                            t.printStackTrace();
                        }
                    });
                } else {
                    Log.e("inside", "else");
                    if (phone != null && phone.length() > 0) {
                        // TODO: 24/9/19 verify mobile number
                        loader.show("");
                        verifyViewModel.verifyPhone(getIntent().getIntExtra(API.ID, -1), phone, binding.firstPinView.getText().toString(), countryCode, selectedCountryNameCode);
                    }

                }
            }
        });
    }

    private void getFirebaseToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String token = instanceIdResult.getToken();
            Utility.setSharedPreferencesString(this, API.FIREBASE_TOKEN, token);
        });

    }

    private void getDeviceId() {
        String device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Android", "Android ID : " + device_id);
        Utility.setSharedPreferencesString(this, API.DEVICE_ID, device_id);
    }

    //Check the internet connection.
    private void NetworkDetect() {
        boolean WIFI = false;
        boolean MOBILE = false;
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = CM.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    WIFI = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    MOBILE = true;
        }
        if (WIFI) {
            IPaddress = Utility.getIPaddress(getApplicationContext());
            Utility.setSharedPreferencesString(this, API.SOURCE_IP, IPaddress);
        }
        if (MOBILE) {
            IPaddress = GetDeviceipMobileData();
            Utility.setSharedPreferencesString(this, API.SOURCE_IP, IPaddress);
        }

    }

    public String GetDeviceipMobileData() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    private void resendOtpForPhone(View v) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("countryCode", countryCode);
        jsonObject.addProperty("phoneNo", phone);

        //Call<JsonElement> call=apiInterface.resendOTPForPhone(jsonObject);
        Call<JsonElement> call = apiInterface.sendPhoneOTP(jsonObject);
        processResponse(call, v);
    }

    private void resendOtpForMail(View v) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", mail);
        jsonObject.addProperty(API.ID, id);
        jsonObject.addProperty(API.DEVICE_TOKEN, Utility.getSharedPreferencesString(this, API.FIREBASE_TOKEN));
        jsonObject.addProperty(API.DEVICE_TYPE, "android");
        jsonObject.addProperty("OS", "android");
        jsonObject.addProperty("platform", "playstore");
        jsonObject.addProperty("deviceId", Utility.getSharedPreferencesString(this, API.DEVICE_ID));
        jsonObject.addProperty("sourceIp",Utility.getSharedPreferencesString(this, API.SOURCE_IP));

        Call<JsonElement> call = apiInterface.resendOTPForMail(jsonObject);
        processResponse(call, v);
    }

    private void processResponse(Call<JsonElement> call, View v) {
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                loader.dismiss();
                try {
                    if (response.isSuccessful())
                        processSuccessResponse(response);
                    else
                        processFailedResponse(response, v);
                } catch (Exception e) {
                    e.printStackTrace();
                    Dialogs.toast(getApplicationContext(), v, getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                Dialogs.toast(getApplicationContext(), v, getString(R.string.something_went_wrong));
            }
        });
    }

    private void processFailedResponse(Response<JsonElement> response, View v) throws JSONException, IOException {
        JSONObject OBJ = new JSONObject(response.errorBody().string());
        Dialogs.toast(getApplicationContext(), v, OBJ.getString(API.MESSAGE));
    }

    private void processSuccessResponse(Response<JsonElement> response) throws JSONException {
        JSONObject OBJ = new JSONObject(response.body().toString());
        Toast.makeText(getApplicationContext(), OBJ.getString(API.MESSAGE), Toast.LENGTH_LONG).show();

        try {
            id = OBJ.getJSONObject(API.DATA).getInt(API.ID);
        } catch (Exception e) {
        }

        binding.resendTv.setEnabled(false);
        binding.resendTv.setTextColor(getResources().getColor(R.color.gray));
        startTimer();
    }

    private void startTimer() {
        resendTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                long secLef = l / 1000;
                binding.resendTv.setText(getString(R.string.txt_resend_otp) + " " + (secLef + 1));
            }

            @Override
            public void onFinish() {
                binding.resendTv.setEnabled(true);
                binding.resendTv.setTextColor(getResources().getColor(R.color.themeBlue));
                binding.resendTv.setText(getString(R.string.txt_resend_otp_now));
            }
        };
        resendTimer.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(SuccessEvent event) {
        event.getMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                loader.dismiss();
                Log.e("fired........>>", "yesss");
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String msg = jsonObject.getString(API.MESSAGE);
                    if (jsonObject.getBoolean(API.SUCCESS)) {
                        if ("profile".equalsIgnoreCase(from)) {
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("result", phone);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } else {
                            Utility.setSharedPreferencesBoolean(getApplicationContext(), API.SESSION_ACTIVE, true);
                            if (Utility.getSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUER)) {
                                startActivity(new Intent(VerifiyCode.this, RegisterVanue.class));
                                finish();
                            } else {
                                Intent intent = new Intent(VerifiyCode.this, Home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    } else {
                        Dialogs.toast(getApplicationContext(), binding.createAccBtn, msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Dialogs.toast(getApplicationContext(), binding.createAccBtn, "something went wrong!!!!");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
