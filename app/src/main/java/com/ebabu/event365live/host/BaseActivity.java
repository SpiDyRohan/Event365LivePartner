package com.ebabu.event365live.host;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean isConnected=true;
    private boolean monitoringConnectivity = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkConnectivity();
            if (!isConnected)
                onNetworkChanged(false);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkConnectivity() {
        ConnectivityManager.NetworkCallback connectivityCallback
                = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isConnected = true;
                onNetworkChanged(true);
                //EventBus.getDefault().post(new InternetConnectionEvent(isConnected));
            }

            @Override
            public void onLost(Network network) {
                isConnected = false;
                onNetworkChanged(false);
                //EventBus.getDefault().post(new InternetConnectionEvent(isConnected));
            }
        };

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);
        monitoringConnectivity = true;


    }

   public abstract void onNetworkChanged(boolean isConnected);
}

