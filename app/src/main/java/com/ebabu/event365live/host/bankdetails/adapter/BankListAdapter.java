package com.ebabu.event365live.host.bankdetails.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.bankdetails.modal.GetBankList;
import com.ebabu.event365live.host.databinding.ShowBankCardBinding;

import java.util.List;

public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.BankListHolder> {

    private Activity activity;
    private List<GetBankList.BankList> bankLists;

    public BankListAdapter(Activity activity, List<GetBankList.BankList> bankLists) {
        this.activity = activity;
        this.bankLists = bankLists;
    }

    @NonNull
    @Override
    public BankListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShowBankCardBinding cardBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.show_bank_card, parent, false);
        return new BankListHolder(cardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BankListHolder holder, int position) {
        GetBankList.BankList bankList = bankLists.get(position);
        holder.cardBinding.tvBankName.setText(bankList.getBankName());
        holder.cardBinding.tvAccountNo.setText(getSecretAccountNo(bankList.getAccountNo()));
    }

    @Override
    public int getItemCount() {
        return bankLists.size();
    }

    class BankListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ShowBankCardBinding cardBinding;


        BankListHolder(@NonNull ShowBankCardBinding cardBinding) {
            super(cardBinding.getRoot());
            this.cardBinding = cardBinding;
            cardBinding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt("bankId",bankLists.get(getAdapterPosition()).getId());
            Navigation.findNavController(v).navigate(R.id.confirmWithdrawFragment,bundle);
        }
    }

    private String getSecretAccountNo(String accountNo) {
        int length = accountNo.length();
        return "********" + accountNo.substring(length - 3, length);
    }
}
