package com.ebabu.event365live.host.bankdetails.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.bankdetails.adapter.TransactionAdapter;
import com.ebabu.event365live.host.bankdetails.modal.TransactionHistoryModal;
import com.ebabu.event365live.host.databinding.FragCompletedPaymentBinding;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompletedPaymentFrag extends Fragment {
    private MyLoader myLoader;
    private FragCompletedPaymentBinding completedPaymentBinding;
    private LinearLayoutManager linearLayoutManager;
    private TransactionAdapter transactionAdapter;
    private int currentPage = 1;
    public List<TransactionHistoryModal.TransactionHistory>  transactionHistoryList;
    @Inject
    ApiInterface apiInterface;

    public CompletedPaymentFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        linearLayoutManager = new LinearLayoutManager(context);
        myLoader = new MyLoader(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        completedPaymentBinding = DataBindingUtil.inflate(inflater,R.layout.frag_completed_payment, container, false);
        App.getAppComponent().inject(this);
        transactionHistoryList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionHistoryList);
        completedPaymentBinding.completedRecycler.setLayoutManager(linearLayoutManager);
        completedPaymentBinding.completedRecycler.setAdapter(transactionAdapter);
        getTransactionHistory();
        completedPaymentBinding.noDataLayout.findViewById(R.id.tvRetryBtn).setOnClickListener(v-> getTransactionHistory());
        return completedPaymentBinding.getRoot();
    }

    private void getTransactionHistory(){
        myLoader.show("");
        Call<JsonElement> jsonElementCall = apiInterface.getTransactionHistoryRequest(200,currentPage,"completed");
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                Log.d("flnknaslfa", "completedFrag: "+response.toString());
                myLoader.dismiss();
                if(response.isSuccessful()){
                    TransactionHistoryModal transactionHistoryModal = new Gson().fromJson(response.body().toString(), TransactionHistoryModal.class);
                    if(transactionHistoryModal.getData().getTransactionHistory().size()>0){
                        completedPaymentBinding.noDataLayout.setVisibility(View.GONE);
                        transactionHistoryList.addAll(transactionHistoryModal.getData().getTransactionHistory());
                        Collections.reverse(transactionHistoryList);
                        transactionAdapter.notifyDataSetChanged();
                        return;
                    }
                    if (response.code() == API.SESSION_EXPIRE)
                        Utility.sessionExpired(getContext());
                    else {
                        completedPaymentBinding.noDataLayout.setVisibility(View.VISIBLE);
                        completedPaymentBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);
                    }
                }else {
                    completedPaymentBinding.noDataLayout.setVisibility(View.VISIBLE);
                    completedPaymentBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();

                completedPaymentBinding.noDataLayout.setVisibility(View.VISIBLE);
                completedPaymentBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);
                ((TextView)completedPaymentBinding.noDataLayout.findViewById(R.id.tvRetryBtn)).setText("Something went wrong");
            }
        });
    }
}
