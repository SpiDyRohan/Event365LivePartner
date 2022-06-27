package com.ebabu.event365live.host.viewmodels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.events.FailureEvent;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.repositories.AuthRepository;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SignUpViewModel extends ViewModel {

    @Inject
    ApiInterface apiInterface;

    @SuppressLint("StaticFieldLeak")
    @Inject
    Context context;

    public String name,mail,password;

    private MutableLiveData<Boolean> isConnecting=new MutableLiveData<>();
    private MutableLiveData<String> isFailed = new MutableLiveData<>();

    public void init(){
        App.getAppComponent().inject(this);
    }

    public void signUp(View view){

        if (TextUtils.isEmpty(name)) {
            EventBus.getDefault().post(new FailureEvent(context.getString(R.string.nameIsRequired)));
            return;
        }

        if(name.trim().length()<2){
            EventBus.getDefault().post(new FailureEvent("Enter valid name please!"));
            return;
        }


        if (TextUtils.isEmpty(mail)){
            EventBus.getDefault().post(new FailureEvent(context.getString(R.string.emailRequired)));
            return;
        }

        if (TextUtils.isEmpty(mail) || !Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(mail).matches()) {
            EventBus.getDefault().post(new FailureEvent(context.getString(R.string.invalidEmail)));
            return;
        }

        if (TextUtils.isEmpty(password.trim())) {
            EventBus.getDefault().post(new FailureEvent(context.getString(R.string.passwordRequired)));
            return;
        }

        if(password.trim().length()<8){
            isFailed.setValue(context.getString(R.string.txt_pass_length));
            return;
        }
        if(!Constants.VALID_PASSWORD_REGEX.matcher(password.trim()).matches()){
            isFailed.setValue(context.getString(R.string.txt_pass_validation));
            return;
        }

        isConnecting.setValue(true);

        LiveData<String> liveData = new AuthRepository(apiInterface).signup(name,mail,password, Utility.getSharedPreferencesString(context,Constants.USER_TYPE));
        EventBus.getDefault().post(new SuccessEvent(liveData));


    }

    public void socialLogin(String name, String socialEmail, String socialId, String socialLoginType) {
        LiveData<String> liveData = new AuthRepository(apiInterface).socialLogin(context, name, socialEmail, socialId, socialLoginType, Utility.getSharedPreferencesString(context, API.FIREBASE_TOKEN), Utility.getSharedPreferencesString(context,Constants.USER_TYPE));
        EventBus.getDefault().post(new SuccessEvent(liveData));
    }

    public LiveData<Boolean> getIsConnecting(){
        return isConnecting;
    }

    public LiveData<String> getIsFailed() {
        return isFailed;
    }

}
