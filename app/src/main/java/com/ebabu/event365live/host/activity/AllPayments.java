package com.ebabu.event365live.host.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivityAllPaymentsBinding;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.fragments.PaymentFragment;
import com.ebabu.event365live.host.utils.Utility;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AllPayments extends BaseActivity {

    ActivityAllPaymentsBinding binding;
    String name="";
    int eventId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_all_payments);
        
        binding.backArrow.setOnClickListener(v->onBackPressed());
        eventId=getIntent().getIntExtra("id",-1);
        setupAllViewRSVP();
        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    if(!name.equalsIgnoreCase(binding.searchEt.getText().toString().trim())) {
                        name=binding.searchEt.getText().toString().trim();
                        search(name);
                    }

                return false;
            }
        });
        
    }

    private void search(String query) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        EventBus.getDefault().post(new CheckedInEvent(query));

        Utility.setSharedPreferencesString(AllPayments.this,"payment_query",query);

    }

    private void setupAllViewRSVP() {

        PaymentFragment paidFragment=new PaymentFragment();
        Bundle paidBundle=new Bundle();
        paidBundle.putString("type","regular");
        paidBundle.putInt("id",eventId);
        paidFragment.setArguments(paidBundle);

        PaymentFragment vipFragment=new PaymentFragment();
        Bundle vipBundle=new Bundle();
        vipBundle.putString("type","vip");
        vipBundle.putInt("id",eventId);
        vipFragment.setArguments(vipBundle);

        PaymentFragment seatingsFragment=new PaymentFragment();
        Bundle seatingBundle=new Bundle();
        seatingBundle.putString("type","tableSeatingBoth");
        seatingBundle.putInt("id",eventId);
        seatingsFragment.setArguments(seatingBundle);

        PaymentFragment cancelledFrag=new PaymentFragment();
        Bundle cancelBundle=new Bundle();
        cancelBundle.putString("type","cancelled");
        cancelBundle.putInt("id",eventId);
        cancelledFrag.setArguments(cancelBundle);


        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(paidFragment, "Regular");
        adapter.addFragment(vipFragment, "VIP");
        adapter.addFragment(seatingsFragment, "Table Seatings");
        adapter.addFragment(cancelledFrag, "Cancelled");

        binding.myViewPager.setAdapter(adapter);
        binding.myViewPager.setOffscreenPageLimit(4);
        binding.tabs.setupWithViewPager(binding.myViewPager);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(()->{
            if(isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utility.setSharedPreferencesString(AllPayments.this,"payment_query","");
    }
}
