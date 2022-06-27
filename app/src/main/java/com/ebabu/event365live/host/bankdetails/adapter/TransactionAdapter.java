package com.ebabu.event365live.host.bankdetails.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.bankdetails.modal.TransactionHistoryModal;
import com.ebabu.event365live.host.databinding.WithdrawCustomLayoutBinding;
import com.ebabu.event365live.host.utils.Utility;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionHolder> {

    List<TransactionHistoryModal.TransactionHistory> transactionHistoryList;

    public TransactionAdapter(List<TransactionHistoryModal.TransactionHistory> transactionHistoryList) {
        this.transactionHistoryList = transactionHistoryList;
    }

    @NonNull
    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WithdrawCustomLayoutBinding customLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.withdraw_custom_layout,parent
        ,false);
        return new TransactionHolder(customLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
        TransactionHistoryModal.TransactionHistory history =  transactionHistoryList.get(position);
        holder.customLayoutBinding.tvBankName.setText(history.getBankDetails().getBankName());
        holder.customLayoutBinding.tvAccountNo.setText(getAccountNo(history.getBankDetails().getAccountNo()));
        holder.customLayoutBinding.tvShowAmount.setText("$"+history.getWithdrawnAmount());
        holder.customLayoutBinding.tvShowDate.setText(Utility.getDateMonthYearName(history.getUpdatedAt(),true));
    }

    @Override
    public int getItemCount() {
        return transactionHistoryList.size();
    }

    static class TransactionHolder extends RecyclerView.ViewHolder {
        private WithdrawCustomLayoutBinding customLayoutBinding;
        public TransactionHolder(@NonNull WithdrawCustomLayoutBinding customLayoutBinding) {
            super(customLayoutBinding.getRoot());
            this.customLayoutBinding = customLayoutBinding;
        }
    }

    private String getAccountNo(String accountNO){
        int getNo = accountNO.length();
        return "********"+accountNO.substring(getNo-3 , getNo);


    }
}
