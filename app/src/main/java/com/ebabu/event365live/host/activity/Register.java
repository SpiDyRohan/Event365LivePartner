package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import javax.inject.Inject;

public class Register extends BaseActivity {

    int userId;
    @Inject
    ApiInterface apiInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        setContentView(R.layout.activity_register);
        App.createEventDAO = null;
        if (getIntent().hasExtra("userId")) {
            userId = getIntent().getIntExtra("userId", -1);
        }
    }

    public void proceed(View v) {
        if (v.getId() == R.id.venue) {
            Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUER, true);
            Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_VENUE_OWNER, true);
            Utility.setSharedPreferencesString(getApplicationContext(), Constants.USER_TYPE, "venuer");
        } else if (v.getId() == R.id.host) {
            Utility.setSharedPreferencesString(getApplicationContext(), Constants.USER_TYPE, "host");
            Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_VENUE_OWNER, false);
        } else {
            Utility.setSharedPreferencesString(getApplicationContext(), Constants.USER_TYPE, "promoter");
            Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_VENUE_OWNER, false);
        }


        if (userId > 0) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            startActivity(new Intent(this, SignUp.class));
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }
}
