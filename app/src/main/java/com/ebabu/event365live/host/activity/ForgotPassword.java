package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityForgotPasswordBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends BaseActivity {

    ActivityForgotPasswordBinding binding;

    @Inject
    ApiInterface apiInterface;

    MyLoader loader;


    boolean isValid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        loader=new MyLoader(this);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_forgot_password);
        binding.backArrow.setOnClickListener(v->onBackPressed());

        addMailPWdWatcher();

        binding.createAccBtn.setOnClickListener(v->{
            if(!isValid){
                Dialogs.toast(getApplicationContext(),binding.parentLayout,getString(R.string.mail_err));
                binding.mailEt.requestFocus();
                return;
            }

            loader.show("");

            JsonObject jsonObject=new JsonObject();
            jsonObject.addProperty("email",binding.mailEt.getText().toString().trim());

            Call<JsonElement> call=apiInterface.forgotPassword(jsonObject);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    loader.dismiss();
                    if(response.isSuccessful()){

                        try {
                            JSONObject OBJ = new JSONObject(response.body().toString());
                            Toast.makeText(getApplicationContext(),OBJ.getString(API.MESSAGE),Toast.LENGTH_LONG).show();
                            int id=OBJ.getJSONObject(API.DATA).getInt(API.ID);
                            startActivity(
                                    new Intent(ForgotPassword.this,VerifiyCode.class)
                                        .putExtra(API.ID,id)
                                    .putExtra(Constants.FROM,API.FORGOT_PASSWORD)
                                    .putExtra(API.EMAIL,binding.mailEt.getText().toString().trim())
                            );
                        } catch (JSONException e) {
                            Dialogs.toast(getApplicationContext(),v,getString(R.string.something_went_wrong));
                        }
                    }
                    else{
                        try {
                            JSONObject OBJ = new JSONObject(response.errorBody().string());
                            Dialogs.toast(getApplicationContext(),v,OBJ.getString(API.MESSAGE));
                        }catch (Exception e){
                            Dialogs.toast(getApplicationContext(),v,getString(R.string.something_went_wrong));
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    loader.dismiss();
                    Dialogs.toast(getApplicationContext(),v,getString(R.string.something_went_wrong));
                }
            });

        });
    }


    private void addMailPWdWatcher() {
        binding.mailEt.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValid= Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(charSequence.toString()).matches();
                if(isValid) binding.validMailImg.setVisibility(View.VISIBLE);
                else binding.validMailImg.setVisibility(View.INVISIBLE);
            }
            public void afterTextChanged(Editable editable){}
        });
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
