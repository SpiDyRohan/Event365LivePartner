package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivityWelcomeBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.databinding.DataBindingUtil;

public class Welcome extends BaseActivity {

    ActivityWelcomeBinding binding;
    Snackbar networkBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_welcome);
        networkBar= Snackbar.make(binding.getRoot(),"No internet connection...",Snackbar.LENGTH_INDEFINITE);
        binding.loginLayoutBtn.setOnClickListener(v->startActivity(new Intent(this,Login.class)));
        binding.createAccBtn.setOnClickListener(v->startActivity(new Intent(this,Register.class)));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(()->{
            if(isConnected)
                networkBar.dismiss();
            else networkBar.show();
        });
    }
}
