package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityResetPasswordBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPassword extends BaseActivity {

    ActivityResetPasswordBinding binding;
    String pwd="",confPwd="",mail;
    boolean isMasking = true;
    int id;

    @Inject
    ApiInterface apiInterface;

    MyLoader loader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= DataBindingUtil.setContentView(this,R.layout.activity_reset_password);
        App.getAppComponent().inject(this);

        loader=new MyLoader(this);
        id=getIntent().getIntExtra(API.ID,-1);
        mail=getIntent().getStringExtra(API.EMAIL);

        binding.pwdEt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confPwd=binding.pwdEt2.getText().toString().trim();
                validate();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.pwdEt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pwd=binding.pwdEt1.getText().toString().trim();
                validate();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        binding.pwdEyeimg1.setOnClickListener(v -> {
            if (binding.pwdEt1.getText().toString().trim().length() > 0) {

                isMasking = !isMasking;
                if (isMasking) {
                    binding.pwdEyeimg1.setImageResource(R.drawable.eye_gray);
                    binding.pwdEt1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    binding.pwdEt1.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.pwdEyeimg1.setImageResource(R.drawable.eye);
                }
                binding.pwdEt1.setSelection(binding.pwdEt1.getText().toString().trim().length());
            }

        });

        binding.pwdEyeimg2.setOnClickListener(v -> {
            if (binding.pwdEt2.getText().toString().trim().length() > 0) {

                isMasking = !isMasking;
                if (isMasking) {
                    binding.pwdEyeimg2.setImageResource(R.drawable.eye_gray);
                    binding.pwdEt2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    binding.pwdEt2.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.pwdEyeimg2.setImageResource(R.drawable.eye);
                }
                binding.pwdEt2.setSelection(binding.pwdEt2.getText().toString().trim().length());
            }

        });

        binding.createAccBtn.setOnClickListener(v->{
            if (pwd.length()<8)
                Dialogs.toast(getApplicationContext(),v,getString(R.string.txt_newpass_length));
            else if(!Constants.VALID_PASSWORD_REGEX.matcher(pwd.trim()).matches())
                Dialogs.toast(getApplicationContext(),v,getString(R.string.txt_newpass_validation));
            else if (confPwd.length()<8)
                Dialogs.toast(getApplicationContext(),v,getString(R.string.txt_conpass_length));
            else if(!Constants.VALID_PASSWORD_REGEX.matcher(confPwd.trim()).matches())
                Dialogs.toast(getApplicationContext(),v,getString(R.string.txt_conpass_validation));
            else if (!pwd.equals(confPwd))
                Dialogs.toast(getApplicationContext(),v,getString(R.string.pwd_not_matched));
            else if(!Utility.isNetworkAvailable(this))
                Dialogs.toast(getApplicationContext(),v,getString(R.string.check_network));
            else{

                loader.show("");

                JsonObject jsonObject=new JsonObject();
                jsonObject.addProperty(API.ID,id);
                if(mail!=null)
                    jsonObject.addProperty(API.EMAIL,mail);
                jsonObject.addProperty(API.NEW_PASSWORD,pwd);
                Call<JsonElement> call=apiInterface.resetPassword(jsonObject);
                call.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        loader.dismiss();
                        if(response.isSuccessful()){

                            try {
                                JSONObject OBJ = new JSONObject(response.body().toString());
                                Toast.makeText(getApplicationContext(),OBJ.getString(API.MESSAGE),Toast.LENGTH_LONG).show();
                                // TODO: 10/10/19
                                Intent intent=new Intent(ResetPassword.this,Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

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
                        Dialogs.toast(getApplicationContext(),v,getString(R.string.something_went_wrong));
                    }
                });
            }

        });

        binding.backArrow.setOnClickListener(v->onBackPressed());

    }

    private void validate(){
        binding.validMailImg1.setVisibility(View.INVISIBLE);
        binding.pwdEyeimg1.setVisibility(View.INVISIBLE);
        binding.validMailImg2.setVisibility(View.INVISIBLE);
        binding.pwdEyeimg2.setVisibility(View.INVISIBLE);

        if(pwd.length()>0 && confPwd.length()>0){
            binding.pwdEyeimg2.setVisibility(View.VISIBLE);
            binding.pwdEyeimg1.setVisibility(View.VISIBLE);
             if(pwd.equals(confPwd)){
                 if(pwd.length()>=8 && Constants.VALID_PASSWORD_REGEX.matcher(pwd).matches()) {
                     binding.validMailImg1.setVisibility(View.VISIBLE);
                     binding.validMailImg2.setVisibility(View.VISIBLE);
                 }
             }

        }else if(pwd.length()>0){
            binding.pwdEyeimg1.setVisibility(View.VISIBLE);
            if(pwd.length()>=8 && Constants.VALID_PASSWORD_REGEX.matcher(pwd).matches())
                binding.validMailImg1.setVisibility(View.VISIBLE);

        }else if(confPwd.length()>0){
            binding.pwdEyeimg2.setVisibility(View.VISIBLE);
            if(confPwd.length()>=8 && Constants.VALID_PASSWORD_REGEX.matcher(confPwd).matches())
                binding.validMailImg2.setVisibility(View.VISIBLE);

        }

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
