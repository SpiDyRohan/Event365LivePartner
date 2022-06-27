package com.ebabu.event365live.host.activity.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.ebabu.event365live.host.R;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class EventActivity extends AppCompatActivity {

    View noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        noInternetLayout = findViewById(R.id.no_internet_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            checkConnectivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkConnectivity() {
        ConnectivityManager.NetworkCallback connectivityCallback
                = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> noInternetLayout.setVisibility(View.GONE));

            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> noInternetLayout.setVisibility(View.VISIBLE));
            }
        };

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting())) {
            runOnUiThread(() -> noInternetLayout.setVisibility(View.VISIBLE));
        }

        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);

    }
}
