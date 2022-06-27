package com.ebabu.event365live.host;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.bankdetails.adapter.BankListAdapter;
import com.ebabu.event365live.host.bankdetails.modal.GetBankList;
import com.ebabu.event365live.host.databinding.FragmentChooseBankAccountBinding;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.HttpRequestForRoutingNo;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseBankAccountFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentChooseBankAccountBinding bankAccountBinding;
    private MyLoader myLoader;
    private BankListAdapter bankListAdapter;
    private TextView tvRetryBtn, tvShowMsg;
    private Activity activity;
    @Inject
    ApiInterface apiInterface;
    private int currentPage = 1;
    private int stripeAccountStatus = -1, accountLinkStatus = -1; //o -> true 1-> false

    public ChooseBankAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bankAccountBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_choose_bank_account, container, false);
        myLoader = new MyLoader(getActivity());
        tvRetryBtn = bankAccountBinding.noDatLayout.findViewById(R.id.tvRetryBtn);
        tvShowMsg = bankAccountBinding.noDatLayout.findViewById(R.id.tvShowMsg);
        bankAccountBinding.tvChooseCardTitle.setVisibility(View.GONE);
        bankAccountBinding.tvAddNewAccountBtn.setVisibility(View.GONE);
        bankAccountBinding.swipeLayout.setOnRefreshListener(this);
        App.getAppComponent().inject(this);

        bankAccountBinding.noDatLayout.findViewById(R.id.tvRetryBtn).setOnClickListener(v -> {
            if (tvRetryBtn.getText().toString().equals("Add Card")) {
                Navigation.findNavController(v).navigate(R.id.addNewAccountFragment);
                return;
            }
            getBankListRequest();
        });

        bankAccountBinding.tvAddNewAccountBtn.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addNewAccountFragment);
        });

        bankAccountBinding.ivBackBtn.setOnClickListener(v -> getActivity().onBackPressed());

        bankAccountBinding.tvLinkAccountBtn.setOnClickListener(v -> {
            linkAccountRequest();
        });


        return bankAccountBinding.getRoot();
    }

    private void getBankListRequest() {
        myLoader.show("");
        bankAccountBinding.tvChooseCardTitle.setVisibility(View.VISIBLE);
        Call<JsonElement> jsonElementCall = apiInterface.getBankList(30, currentPage);
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    Log.d("fnaslfnklasf", "getBankListRequest: " + response.body().toString());
                    GetBankList getBankList = new Gson().fromJson(response.body().toString(), GetBankList.class);

                    if (getBankList.getData().getBank_details().isEmpty()) {
                        stripeAccountStatus = getBankList.getData().isStripeAccountStatus() ? 0 : 1;
                        accountLinkStatus = getBankList.getData().isAccountLinkStatus() ? 0 : 1;
                        if (stripeAccountStatus == 0 && accountLinkStatus == 0) {
                            bankAccountBinding.tvChooseCardTitle.setVisibility(View.VISIBLE);
                            bankAccountBinding.tvChooseCardTitle.setText("Choose Card");
                            bankAccountBinding.accountLinkContainer.setVisibility(View.GONE);
                            tvRetryBtn.setText("Add Card");
                            tvShowMsg.setText("Add Bank Account");
                            bankAccountBinding.tvShowAddCardTitle.setVisibility(View.GONE);
                            showNoDataLayout(true);
                        } else if(stripeAccountStatus == 1 && accountLinkStatus == 0) {
                            bankAccountBinding.tvChooseCardTitle.setVisibility(View.VISIBLE);
                            bankAccountBinding.tvChooseCardTitle.setText("Info");
                            bankAccountBinding.accountLinkContainer.setVisibility(View.VISIBLE);
                            bankAccountBinding.tvLinkAccountBtn.setVisibility(View.GONE);
                            bankAccountBinding.ivOne.setText("Your stripe account is not setup yet it will take around 48hrs to became active. In case you are facing any issue please contact support team.");
                            bankAccountBinding.ivOne.setTextColor(Color.RED);
                            bankAccountBinding.tvShowAddCardTitle.setVisibility(View.GONE);
                        }else {
                            bankAccountBinding.tvChooseCardTitle.setVisibility(View.VISIBLE);
                            bankAccountBinding.accountLinkContainer.setVisibility(View.VISIBLE);
                            bankAccountBinding.showCardRecycler.setVisibility(View.GONE);
                            bankAccountBinding.tvShowAddCardTitle.setVisibility(View.GONE);
                            bankAccountBinding.tvChooseCardTitle.setText("Link You Account");
                        }

                        return;
                    }
                    if (getBankList.getData().getBank_details().size() > 0) {
                        bankAccountBinding.tvAddNewAccountBtn.setVisibility(View.VISIBLE);
                        bankAccountBinding.tvShowAddCardTitle.setVisibility(View.VISIBLE);
                        bankAccountBinding.accountLinkContainer.setVisibility(View.GONE);
                        showNoDataLayout(false);
                        setupBankListRecycler(getBankList.getData().getBank_details());
                    } else {
                        tvShowMsg.setText("Something went wrong");
                        showNoDataLayout(true);
                    }
                    bankAccountBinding.tvChooseCardTitle.setVisibility(View.VISIBLE);
                    bankAccountBinding.tvChooseCardTitle.setText("Choose Card");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                tvShowMsg.setText("Something went wrong");
                showNoDataLayout(true);
            }
        });
    }

    private void setupBankListRecycler(List<GetBankList.BankList> bankLists) {
        bankListAdapter = new BankListAdapter(activity, bankLists);
        bankAccountBinding.showCardRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        bankAccountBinding.showCardRecycler.setAdapter(bankListAdapter);
        bankListAdapter.notifyDataSetChanged();
    }

    private void showNoDataLayout(boolean isFailed) {
        if (isFailed) {
            bankAccountBinding.noDatLayout.setVisibility(View.VISIBLE);
            bankAccountBinding.showCardRecycler.setVisibility(View.GONE);

        } else {
            bankAccountBinding.noDatLayout.setVisibility(View.GONE);
            bankAccountBinding.showCardRecycler.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onRefresh() {
        getBankListRequest();
        bankAccountBinding.swipeLayout.setRefreshing(false);
    }

    private void linkAccountRequest() {
        myLoader.show("");
        Call<JsonElement> jsonElementCall = HttpRequestForRoutingNo.getLinkAccount("Bearer " + Utility.getSharedPreferencesString(activity, API.AUTHORIZATION));
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()){

                    try {
                        JSONObject object = new JSONObject(response.body().toString());
                        String message = object.getString("message");

                        JSONObject data = object.getJSONObject("data");
                        String created = data.getString("created");
                        String expiresAt = data.getString("expires_at");
                        String url = data.getString("url");

                        Log.d("fnaslkfa", "onResponse URLLLLL: "+url);

                        openBrowser(getActivity(),url);

//                        Intent navigateToWebViewActivity = new Intent(activity, LinkAccountWebViewActivity.class);
//                        navigateToWebViewActivity.putExtra("url", url);
//                        navigateToWebViewActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivityForResult(navigateToWebViewActivity, 4001);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Dialogs.toast(getContext(), bankAccountBinding.getRoot(), "Something went wrong");


                    }
                }else if(response.code() == 406) {
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());
                        String msg = object.getString("message");
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    Dialogs.toast(getContext(), bankAccountBinding.getRoot(), "Something went wrong");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, Throwable t) {
                myLoader.dismiss();
                Dialogs.toast(getContext(), bankAccountBinding.getRoot(), "Something went wrong");
            }
        });
    }

    public static void openBrowser(Context mContext, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mContext.startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 4001)
            getBankListRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBankListRequest();
    }

}
