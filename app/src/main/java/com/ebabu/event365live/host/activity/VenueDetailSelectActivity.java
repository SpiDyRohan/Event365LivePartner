package com.ebabu.event365live.host.activity;

import android.os.Bundle;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;

public class VenueDetailSelectActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail_select);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(()->{
            if(isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }
}