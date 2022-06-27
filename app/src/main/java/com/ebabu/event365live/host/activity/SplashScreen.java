package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    Thread homeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        move();

    }

    public void move() {
        homeThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (Utility.isLogin(getApplicationContext()))
                    startActivity(new Intent(this, Home.class));
                else if (Utility.getSharedPreferencesBoolean(getApplicationContext(), Constants.ON_BOARDING_DONE))
                    startActivity(new Intent(this, Login.class));
                else
                    startActivity(new Intent(this, OnBoarding.class));
                finish();

            } catch (InterruptedException ie) {
            }
        });
        homeThread.start();
    }

    @Override
    public void onBackPressed() {
        homeThread.interrupt();
        super.onBackPressed();
    }
}
