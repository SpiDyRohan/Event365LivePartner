package com.ebabu.event365live.host.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.repositories.AuthRepository;

import org.greenrobot.eventbus.EventBus;

public class VerifyViewModel extends ViewModel{
    ApiInterface apiInterface;
    Context context;
    public void init(ApiInterface apiInterface, Context context)
    {
        this.apiInterface=apiInterface;
        this.context=context;

    }

    public void verifyPhone(int id,String phone,String otp,String countryCode, String selectedCountryNameCode){
        LiveData<String> liveData=new AuthRepository(apiInterface).verifyPhone(context,id,phone,otp,countryCode,selectedCountryNameCode);
        EventBus.getDefault().post(new SuccessEvent(liveData));
    }
}
