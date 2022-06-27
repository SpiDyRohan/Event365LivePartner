package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivitySmsverificationBinding;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.viewmodels.SMSVerificationViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

public class SMSVerification extends BaseActivity {

    ActivitySmsverificationBinding binding;
    boolean isValid;
    String from;
    @Inject
    ApiInterface apiInterface;
    int id;
    SMSVerificationViewModel viewModel;


    MyLoader loader;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_smsverification);
        loader = new MyLoader(this);
        viewModel = ViewModelProviders.of(this).get(SMSVerificationViewModel.class);
        viewModel.init(getApplicationContext(), apiInterface);

        id = getIntent().getIntExtra(API.ID, -1);

        if (from != null && from.equalsIgnoreCase(Constants.SIGNUP))
            binding.btnText.setText(getString(R.string.continue_txt));


        binding.ccp.registerCarrierNumberEditText(binding.mobEt);

        binding.ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            isValid = isValidNumber;
            if (isValidNumber) binding.mobImg.setVisibility(View.VISIBLE);
            else binding.mobImg.setVisibility(View.INVISIBLE);
        });



        binding.createAccBtn.setOnClickListener(v -> {

            if (!isValid) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.invalid_mob));
                return;
            }
            phone = binding.mobEt.getText().toString().trim();
            loader.show("");

            viewModel.verify(id, phone, binding.ccp.getSelectedCountryCodeWithPlus());

            //TODO check for send country name code on which API TODAY
           /* Intent intent=new Intent(this,VerifiyCode.class);
            startActivity(intent);
            finish();*/
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(SuccessEvent event) {

        event.getMsg().observe(this, s -> {
            loader.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                String msg = jsonObject.getString(API.MESSAGE);
                if (jsonObject.getBoolean(API.SUCCESS)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SMSVerification.this, VerifiyCode.class);
                    intent.putExtra(API.ID, id);
                    intent.putExtra(API.COUNTRY_CODE, binding.ccp.getSelectedCountryCodeWithPlus());
                    intent.putExtra(API.PHONE_NO, phone);
                    intent.putExtra("countryNameCode", binding.ccp.getSelectedCountryNameCode());
                    startActivity(intent);
//                    finish();
                } else {
                    Dialogs.toast(getApplicationContext(), binding.createAccBtn, msg);
                }

            } catch (Exception e) {

                e.printStackTrace();
                Dialogs.toast(getApplicationContext(), binding.createAccBtn, "something went wrong!!!!");
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

    public void onBackOnClick(View view) {
        finish();
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
