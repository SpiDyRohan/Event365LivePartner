package com.ebabu.event365live.host.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.repositories.AuthRepository;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import org.greenrobot.eventbus.EventBus;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel{

    Context context;
    ApiInterface apiInterface;

    public void init(Context context,ApiInterface apiInterface){
        this.context=context;
        this.apiInterface=apiInterface;
    }

    public String email,pwd;
    private MutableLiveData<String> isFailed = new MutableLiveData<>();
    private MutableLiveData<Boolean> isConnecting=new MutableLiveData<>();

    public void login(View view){
        if(TextUtils.isEmpty(email)){
            isFailed.setValue(context.getString(R.string.mail_mandatory));
            return;
        }
        if(!Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()){
            isFailed.setValue(context.getString(R.string.enter_valid_mail));
            return;
        }
        if(TextUtils.isEmpty(pwd.trim())){
            isFailed.setValue(context.getString(R.string.enter_pwd_err));
            return;
        }
        if(pwd.trim().length()<8){
            isFailed.setValue(context.getString(R.string.txt_pass_length));
            return;
        }
        if(!Constants.VALID_PASSWORD_REGEX.matcher(pwd.trim()).matches()){
            isFailed.setValue(context.getString(R.string.txt_pass_validation));
            return;
        }

        isConnecting.setValue(true);
        LiveData<String> liveData = new AuthRepository(apiInterface).login(context, email, pwd, Utility.getSharedPreferencesString(context, API.FIREBASE_TOKEN),Utility.getSharedPreferencesString(context, API.DEVICE_ID),Utility.getSharedPreferencesString(context, API.SOURCE_IP));
        EventBus.getDefault().post(new SuccessEvent(liveData));
    }

    public void socialLogin(String name, String socialEmail, String socialId, String socialLoginType) {
        LiveData<String> liveData = new AuthRepository(apiInterface).socialLogin(context, name, socialEmail, socialId, socialLoginType, Utility.getSharedPreferencesString(context, API.FIREBASE_TOKEN),"");
        EventBus.getDefault().post(new SuccessEvent(liveData));

    }

    public LiveData<String> getIsFailed() {
        return isFailed;
    }

    public LiveData<Boolean> getIsConnecting() {
        return isConnecting;
    }

}
