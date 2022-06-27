package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.ContactUs;
import com.ebabu.event365live.host.activity.ManageUser;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.bankdetails.WithdrawActivity;
import com.ebabu.event365live.host.databinding.FragmentSettingBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;
import com.jakewharton.rxbinding2.view.RxView;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    FragmentSettingBinding binding;
    Disposable disposable;
    public View lineView;
    @Inject
    ApiInterface apiInterface;
    MyLoader loader;
    private JSONObject userObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        App.getAppComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        lineView = binding.view5;
        loader = new MyLoader(getContext());
        binding.textView22.setVisibility(View.GONE);
        binding.view5.setVisibility(View.GONE);
        binding.getPaidTv.setVisibility(View.GONE);
        binding.view15.setVisibility(View.GONE);
        showHideOptions();

        binding.getPaidTv.setOnClickListener(v ->{
            startActivityForResult(
                    new Intent(getActivity(), WithdrawActivity.class),4001);
                });

        binding.switch1.setChecked(!Utility.getSharedPreferencesBoolean(getContext(), API.NOTIFICATION_CLOSED));
        binding.textView22.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ManageUser.class));
        });

        binding.logoutTv.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.really_want_logout))
                        .setPositiveButton("Yes", (dialogInterface, i) -> logoutAPI())
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());
        binding.privacyPolicy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)));
            startActivity(browserIntent);
        });

        binding.faqs.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.faq_url)));
            startActivity(browserIntent);
        });

        binding.termAndCondition.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_condition_url)));
            startActivity(browserIntent);
        });

        binding.shareBtn.setOnClickListener(v -> Utility.inviteFriend(getContext(), "Please connect with Event365Live app, Available on playstore!\nhttps://play.google.com/store/apps/details?id="+getContext().getPackageName()));
        binding.helpBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), ContactUs.class)));

        disposable = RxView.clicks(binding.switch1).throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread()).
                        subscribe(o -> Utility.setSharedPreferencesBoolean(getContext(), API.NOTIFICATION_CLOSED, !binding.switch1.isChecked()));

        return binding.getRoot();
    }

    private void showHideOptions() {
        try {
            if(!TextUtils.isEmpty(Utility.getSharedPreferencesString(getContext(), Constants.USER_LOGIN_DATA))) {
                userObject = new JSONObject(Utility.getSharedPreferencesString(getContext(), Constants.USER_LOGIN_DATA));

                if (!userObject.getString("userType").equalsIgnoreCase("host")) {
                    if (userObject.getString("roles").contains("user_management")) {
                        binding.textView22.setVisibility(View.VISIBLE);
                        binding.view5.setVisibility(View.VISIBLE);
                    }
                }

                if (userObject.getInt("createdBy") == userObject.getInt("id")) {
                    binding.getPaidTv.setVisibility(View.VISIBLE);
                    binding.view15.setVisibility(View.VISIBLE);
                }
            }else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showHideOptions();
                    }
                }, 2000);
            }

        }catch (Exception e){e.printStackTrace();}
    }

    public void logoutAPI(){
        if(Utility.isNetworkAvailable(getContext())) {
            loader.show("");
            userLogoutRequest();
        }else
            Toast.makeText(getContext(),R.string.check_network,Toast.LENGTH_LONG).show();
    }

    public  void userLogoutRequest() {
        /*Utility.getSharedPreferencesString(context, API.FIREBASE_TOKEN)
        Utility.getSharedPreferencesString(context, API.DEVICE_ID)*/
        Call<JsonElement> call = apiInterface.userLogout();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                loader.dismiss();
                Utility.logOut(getContext());
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loader.dismiss();
                Utility.logOut(getContext());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.dispose();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
