package com.ebabu.event365live.host.bankdetails;

import android.os.Bundle;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.TransactionHistoryFragment;
import com.ebabu.event365live.host.databinding.ActivityWithdrawBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class WithdrawActivity extends AppCompatActivity {

    private ActivityWithdrawBinding withdrawBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        withdrawBinding = DataBindingUtil.setContentView(this,R.layout.activity_withdraw);
    }

    @Override
    public void onBackPressed() {
        if(TransactionHistoryFragment.isFromSuccessWithdraw){
            setResult(4001);
            finish();
            TransactionHistoryFragment.isFromSuccessWithdraw = false;
        }else {
            super.onBackPressed();
        }
    }
}
