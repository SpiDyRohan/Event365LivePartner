package com.ebabu.event365live.host;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentWithdrawBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class WithdrawFragment extends Fragment {
    private FragmentWithdrawBinding withdrawBinding;
    private MyLoader myLoader;
    @Inject
    ApiInterface apiInterface;

    public WithdrawFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        withdrawBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdraw, container, false);
        myLoader = new MyLoader(getActivity());
        App.getAppComponent().inject(this);
        getBalanceInfoRequest();
        withdrawBinding.backActContainer.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.chooseBankAccountFragment));
        withdrawBinding.tnxHistoryContainer.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.transactionHistoryFragment));
        withdrawBinding.notDataFoundLayout.findViewById(R.id.tvRetryBtn).setOnClickListener(v -> getBalanceInfoRequest());
        withdrawBinding.ivBackBtn.setOnClickListener(v -> getActivity().onBackPressed());
        return withdrawBinding.getRoot();
    }

    private void visibleNoDataFoundLayout(boolean isFailed) {
        if (isFailed) {
            withdrawBinding.notDataFoundLayout.setVisibility(View.VISIBLE);
        } else
            withdrawBinding.notDataFoundLayout.setVisibility(View.GONE);
    }

    private void getBalanceInfoRequest() {
        myLoader.show("");
        Call<JsonElement> balanceInfo = apiInterface.getBalanceInfo();
        balanceInfo.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                Log.d("fmnamn", "onResponse: " + response.body());
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    visibleNoDataFoundLayout(false);
                    try {
                        JSONObject resObj = new JSONObject(response.body().toString());
                        JSONObject data = resObj.getJSONObject("data");
                        Double currentAmounts = data.getDouble("currentAmounts");
                        boolean canWithdrawn = data.getBoolean("canWithdrawn");
                        //String getCurrentAmount = String.valueOf(currentAmounts);
                        String getCurrentAmount = Constants.FORMATOR_DECIMAL.format(currentAmounts);
                        int afterDecimal = getCurrentAmount.indexOf(".");
                        withdrawBinding.tvShowAvailableBlc.setText(getCurrentAmount.substring(0, afterDecimal));
                        withdrawBinding.tvShowAfterDecimalAmount.setText(getCurrentAmount.substring(afterDecimal));
                        withdrawBinding.backActContainer.setVisibility(canWithdrawn ? View.VISIBLE : View.GONE);
                        withdrawBinding.tvShowNote.setVisibility(canWithdrawn ? View.GONE : View.VISIBLE);
                        withdrawBinding.tnxHistoryContainer.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    if (response.code() == API.SESSION_EXPIRE)
                        Utility.sessionExpired(getContext());
                    else {
                        visibleNoDataFoundLayout(true);
                        ((TextView) withdrawBinding.notDataFoundLayout.findViewById(R.id.tvShowMsg)).setText("Something went wrong");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                visibleNoDataFoundLayout(true);
                myLoader.dismiss();
            }
        });
    }
}
