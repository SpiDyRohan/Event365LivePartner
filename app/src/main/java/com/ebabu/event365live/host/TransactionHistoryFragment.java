package com.ebabu.event365live.host;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebabu.event365live.host.bankdetails.adapter.TransactionHistoryVPAdapter;
import com.ebabu.event365live.host.bankdetails.fragment.CompletedPaymentFrag;
import com.ebabu.event365live.host.bankdetails.fragment.PendingHistoryFrag;
import com.ebabu.event365live.host.databinding.FragmentTransactionHistoryBinding;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TransactionHistoryFragment extends Fragment {
    private TransactionHistoryVPAdapter adapter;
    private FragmentTransactionHistoryBinding historyBinding;
    public static boolean isFromSuccessWithdraw;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(getArguments() != null){
           isFromSuccessWithdraw = getArguments().getBoolean("isFromSuccessWithdraw",false);
        }
    }

    public TransactionHistoryFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        historyBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_transaction_history, container, false);
        setupViewPager();
        historyBinding.ivBackBtn.setOnClickListener(v->getActivity().onBackPressed());
        return historyBinding.getRoot();
    }

    private void setupViewPager(){
        adapter = new TransactionHistoryVPAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFrag(new PendingHistoryFrag());
        adapter.addFrag(new CompletedPaymentFrag());
        historyBinding.transactionHistoryVP.setAdapter(adapter);
        historyBinding.transactionHistoryTL.setupWithViewPager(historyBinding.transactionHistoryVP);
        if(isFromSuccessWithdraw) historyBinding.transactionHistoryVP.setCurrentItem(1,true);
    }

}
