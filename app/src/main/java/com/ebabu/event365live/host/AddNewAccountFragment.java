package com.ebabu.event365live.host;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentAddNewAccountBinding;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewAccountFragment extends Fragment {

    private FragmentAddNewAccountBinding addNewAccountBinding;
    private Activity activity;
    private MyLoader myLoader;
    private String getAccountNO, confirmAccountNo;
    private String getRoutingNo, getYourBankName;
    private AlertDialog alertDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        myLoader = new MyLoader(activity);
    }

    @Inject
    ApiInterface apiInterface;

    public AddNewAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        addNewAccountBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_new_account, container, false);
        App.getAppComponent().inject(this);
        init();
        return addNewAccountBinding.getRoot();
    }

    private void addBankDetailsRequest(String accountNo, String routingNo, String bankName) {
        myLoader.show("");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("AccountNo", accountNo);
        jsonObject.addProperty("routingNo", routingNo);
        jsonObject.addProperty("bankName", bankName);


        Call<JsonElement> jsonElementCall = apiInterface.addBankDetailsRequest(jsonObject);
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    alertDialog.dismiss();
                    activity.onBackPressed();
                } else if (response.code() == 406) {

                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        String msg = errorObj.getString("message");
                        if (msg.equalsIgnoreCase("Invalid sort code")) {
                            Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Invalid routing no.");
                            return;
                        }
                        Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Something went wrong");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Something went wrong");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Something went wrong");
                    }


                } else {
                    Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Something went wrong");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Something went wrong");
                Log.d("nflkasnklfa", "onFailure: " + t.getMessage());
            }
        });
    }

    private void init() {

        addNewAccountBinding.ivBackBtn.setOnClickListener(v -> getActivity().onBackPressed());

        addNewAccountBinding.tvAddAccountBtn.setOnClickListener(v -> {
            getRoutingNo = addNewAccountBinding.etEnterRoutingNo.getText().toString();
            getAccountNO = addNewAccountBinding.etEnterAccountNo.getText().toString();
            getYourBankName = addNewAccountBinding.etEnterBankName.getText().toString();
            if (TextUtils.isEmpty(getRoutingNo) || getRoutingNo.length() < 4) {
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Please enter valid routing number");
            } else if (TextUtils.isEmpty(getYourBankName)) {
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Please enter bank name");
            } else if (getYourBankName.length() < 2) {
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Please enter valid bank name");
            } else if (TextUtils.isEmpty(getAccountNO)) {
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Please enter account number");
            } else {
                confirmAddAccountDialog();
            }
        });
    }

    private void confirmAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.confirm_add_dialog_layout, null);
        ((EditText) view.findViewById(R.id.etEnterConfirmAccountNo)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 11) {
                    view.findViewById(R.id.ivShowPassTick).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.ivShowPassTick).setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.tvAddAccountBtn).setOnClickListener(v -> {
            confirmAccountNo = ((EditText) view.findViewById(R.id.etEnterConfirmAccountNo)).getText().toString();
            if (!confirmAccountNo.equalsIgnoreCase(getAccountNO)) {
                Dialogs.toast(getContext(), addNewAccountBinding.getRoot(), "Please enter same account number");
                return;
            }
            String tvShowBankName = "fakeBank: f";
            addBankDetailsRequest(confirmAccountNo, getRoutingNo, getYourBankName);

        });
        builder.setView(view);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
