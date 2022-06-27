package com.ebabu.event365live.host.bankdetails.fragment;

import android.content.Context;
import android.os.Bundle;
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
import com.ebabu.event365live.host.databinding.FragPendingHistoryBinding;
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
public class PendingHistoryFrag extends Fragment {
    private MyLoader myLoader;
    private FragPendingHistoryBinding historyBinding;
    private LinearLayoutManager linearLayoutManager;
    private TransactionAdapter transactionAdapter;
    private List<TransactionHistoryModal.TransactionHistory> transactionHistoryList;
    @Inject
    ApiInterface apiInterface;
    private int currentPage =  1;
    public PendingHistoryFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        linearLayoutManager = new LinearLayoutManager(context);
        myLoader = new MyLoader(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        historyBinding = DataBindingUtil.inflate(inflater,R.layout.frag_pending_history, container, false);
        App.getAppComponent().inject(this);
        transactionHistoryList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionHistoryList);
        historyBinding.pendingRecycler.setLayoutManager(linearLayoutManager);
        historyBinding.pendingRecycler.setAdapter(transactionAdapter);
        historyBinding.noDataLayout.findViewById(R.id.tvRetryBtn).setOnClickListener(v->getTransactionHistory());
        getTransactionHistory();
        return historyBinding.getRoot();
    }

    private void getTransactionHistory(){
        myLoader.show("");
        Call<JsonElement> jsonElementCall = apiInterface.getTransactionHistoryRequest(200,currentPage,"pending");
        jsonElementCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if(response.isSuccessful()){
                    TransactionHistoryModal transactionHistoryModal = new Gson().fromJson(response.body().toString(), TransactionHistoryModal.class);
                    if(transactionHistoryModal.getData().getTransactionHistory().size()>0){
                        historyBinding.noDataLayout.setVisibility(View.GONE);
                        transactionHistoryList.addAll(transactionHistoryModal.getData().getTransactionHistory());
                        Collections.reverse(transactionHistoryList);
                        transactionAdapter.notifyDataSetChanged();
                        return;
                    }
                    historyBinding.noDataLayout.setVisibility(View.VISIBLE);
                    historyBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);

                }else {
                    if (response.code() == API.SESSION_EXPIRE)
                        Utility.sessionExpired(getContext());
                    else {
                        historyBinding.noDataLayout.setVisibility(View.VISIBLE);
                        historyBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                historyBinding.noDataLayout.setVisibility(View.VISIBLE);
                historyBinding.noDataLayout.findViewById(R.id.rootView).setBackgroundResource(android.R.color.transparent);
                ((TextView)historyBinding.noDataLayout.findViewById(R.id.tvRetryBtn)).setText("Something went wrong");
            }
        });
    }

}
