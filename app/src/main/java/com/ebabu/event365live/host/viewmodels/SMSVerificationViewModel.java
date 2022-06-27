package com.ebabu.event365live.host.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.repositories.AuthRepository;

import org.greenrobot.eventbus.EventBus;

public class SMSVerificationViewModel extends ViewModel {

    Context context;
    ApiInterface apiInterface;

    public void init(Context context, ApiInterface apiInterface) {
        this.context = context;
        this.apiInterface = apiInterface;
    }


    public void verify(int id, String phone, String countryCode) {
        LiveData<String> liveData = new AuthRepository(apiInterface).requestForOTP(id, phone, countryCode);
        EventBus.getDefault().post(new SuccessEvent(liveData));
    }
}
