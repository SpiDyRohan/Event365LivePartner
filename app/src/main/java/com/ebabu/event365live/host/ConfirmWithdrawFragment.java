package com.ebabu.event365live.host;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ConfirmationWithdrawLayoutBinding;
import com.ebabu.event365live.host.databinding.FragmentConfirmWithdrawBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmWithdrawFragment extends Fragment {
    private FragmentConfirmWithdrawBinding confirmWithdrawBinding;
    private MyLoader myLoader;
    private Activity activity;
    private int bankId;
    @Inject
    ApiInterface apiInterface;
    private boolean isVerified;
    private Double currentAmounts;

    public ConfirmWithdrawFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
        myLoader = new MyLoader(activity);
        if (getArguments() != null)
            bankId = getArguments().getInt("bankId");

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        confirmWithdrawBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_withdraw, container, false);
        confirmWithdrawBinding.ivBackBtn.setOnClickListener(v -> getActivity().onBackPressed());
        confirmWithdrawBinding.tvWithdrawNowBtn.setOnClickListener(v -> {
//
            String withdrawAmount = confirmWithdrawBinding.etEnterWithdrawAmount.getText().toString().replace("$", "");
            withdrawAmount = withdrawAmount.isEmpty() ? "0" : withdrawAmount;

            Log.d("nfklasnfklas", withdrawAmount + " onCreateView: " + Float.parseFloat(withdrawAmount));

            if (!isVerified) {
                amountWithdrawRequest(withdrawAmount, bankId);
                return;
            }
            if (currentAmounts == 0 || currentAmounts < Double.parseDouble(withdrawAmount)) {
                Dialogs.toast(getContext(), v, "Insufficient balance");
                return;
            } else if (withdrawAmount.equals("0")) {
                Dialogs.toast(getContext(), v, "Please enter amount to withdraw");
                return;
            }
            amountWithdrawRequest(withdrawAmount, bankId);


        });
        App.getAppComponent().inject(this);
        init();
        return confirmWithdrawBinding.getRoot();
    }

    private void init() {
        //confirmWithdrawBinding.etEnterWithdrawAmount.setText("$");
        confirmWithdrawBinding.etEnterWithdrawAmount.setCompoundDrawablesWithIntrinsicBounds(
                getSymbol(activity, "\u0024"/*Your Symbol*/, confirmWithdrawBinding.etEnterWithdrawAmount.getTextSize()
                        , confirmWithdrawBinding.etEnterWithdrawAmount.getCurrentTextColor()), null, null, null);
        //confirmWithdrawBinding.etEnterWithdrawAmount.setGravity(Gravity.CENTER);
        confirmWithdrawBinding.etEnterWithdrawAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        getWithdrawInfoRequest();
    }

    private void amountWithdrawRequest(String amount, int bankId) {
        myLoader.show("");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("withdrawnAmount", Float.parseFloat(amount));
        jsonObject.addProperty("bankId", bankId);

        Log.d("fnaslnfklas", "amountWithdrawRequest: " + jsonObject.toString());

        Call<JsonElement> jsonElementCall = apiInterface.amountWithdrawRequest(jsonObject);

        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response.body().toString());
                        String msg = jsonObject1.getString("message");
                        showWithdrawConfirmationDialog();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Dialogs.toast(getContext(), confirmWithdrawBinding.getRoot(), "Something went wrong");
                    }

                } else if (response.code() == 406) {
                    try {
                        JSONObject errorObj = new JSONObject(response.errorBody().string());
                        String msg = errorObj.getString("message");
                        showErrorMsg(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable throwable) {
                myLoader.dismiss();
                Dialogs.toast(getContext(), confirmWithdrawBinding.getRoot(), "Something went wrong");
            }
        });
    }

    private void getWithdrawInfoRequest() {
        myLoader.show("");

        Call<JsonElement> jsonElementCall = apiInterface.getWithdrawInfo();
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    Log.d("fnaklsfna", "onResponse: " + response.body());
                    try {
                        String msg = "";
                        JSONObject obj = new JSONObject(response.body().toString());
                        JSONObject data = obj.getJSONObject("data");
                        isVerified = data.getBoolean("isVerified");
                        currentAmounts = data.getDouble("currentAmounts");
                        // String getCurrentAmount = String.valueOf(currentAmounts);
                        String getCurrentAmount = Constants.FORMATOR_DECIMAL.format(currentAmounts);
                        int afterDecimal = getCurrentAmount.indexOf(".");
                        confirmWithdrawBinding.tvShowAvailableBlc.setText(getCurrentAmount.substring(0, afterDecimal));
                        confirmWithdrawBinding.tvShowAfterDecimalAmount.setText(getCurrentAmount.substring(afterDecimal));
                        confirmWithdrawBinding.etEnterWithdrawAmount.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                        confirmWithdrawBinding.tvWithdrawNowBtn.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                        if (isVerified)
                            msg = "Note : Transferred made before 8PM business days are proceed the same day and usually complete within 2-3 days.";
                        else
                            msg = "Note : Sorry, Your account is not verified.";

                        confirmWithdrawBinding.tvShowMsg.setText(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                Dialogs.toast(getContext(), confirmWithdrawBinding.getRoot(), "Something went wrong");
            }
        });
    }

    public Drawable getSymbol(Context context, String symbol, float textSize, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(color);
        //paint.setTextAlign(Paint.Align.CENTER);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(symbol) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(symbol, 0, baseline, paint);
        return new BitmapDrawable(context.getResources(), image);
    }

    public void showErrorMsg(String msg) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.alert))
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void showWithdrawConfirmationDialog() {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ConfirmationWithdrawLayoutBinding withdrawLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.confirmation_withdraw_layout, null, false);
        builder.setView(withdrawLayoutBinding.getRoot());

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        AlertDialog finalDialog = dialog;
        withdrawLayoutBinding.loaderBtn.setOnClickListener(v -> {
            finalDialog.dismiss();
            activity.setResult(4001);
            activity.finish();
        });


        withdrawLayoutBinding.tvBackTransactionHistory.setOnClickListener(v -> {
            finalDialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFromSuccessWithdraw", true);

            Navigation.findNavController(confirmWithdrawBinding.getRoot())
                    .navigate(R.id.action_confirmWithdrawFragment_to_transactionHistoryFragment2, bundle,
                            new NavOptions.Builder().setPopUpTo(R.id.transactionHistoryFragment, true).build());


        });

        finalDialog.show();

    }
}
