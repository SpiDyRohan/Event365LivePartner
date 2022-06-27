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
import com.ebabu.event365live.host.databinding.ActivityViewAllRsvpBinding;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.fragments.AllRSVPFragment;
import com.ebabu.event365live.host.utils.Utility;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class ViewAllRSVPActivity extends BaseActivity {

    private ActivityViewAllRsvpBinding binding;
    int id;

    private Disposable _disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id=getIntent().getIntExtra("id",-1);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_view_all_rsvp);

        setupAllViewRSVP();

        _disposable =
                RxTextView.textChangeEvents(binding.searchEt)
                        .debounce(2, TimeUnit.SECONDS) // default Scheduler is Computation
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<TextViewTextChangeEvent>() {
                            @Override
                            public void accept(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                                search(textViewTextChangeEvent.text().toString());
                                Utility.setSharedPreferencesString(ViewAllRSVPActivity.this,"query",textViewTextChangeEvent.text().toString());
                            }
                        });

        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    if( binding.searchEt.getText().toString().trim().length()>0)
                        search(binding.searchEt.getText().toString().trim());

                return false;
            }
        });
    }




    private void setupAllViewRSVP(){

        AllRSVPFragment allRSVPFragment=new AllRSVPFragment();
            Bundle allBundle=new Bundle();
            allBundle.putString("type","all");
            allBundle.putInt("id",id);
            allRSVPFragment.setArguments(allBundle);

        AllRSVPFragment free=new AllRSVPFragment();
        Bundle freeBundle=new Bundle();
        freeBundle.putString("type","free");
        freeBundle.putInt("id",id);
        free.setArguments(freeBundle);

        AllRSVPFragment paidFragment=new AllRSVPFragment();
        Bundle paidBundle=new Bundle();
        paidBundle.putString("type","regular");
        paidBundle.putInt("id",id);
        paidFragment.setArguments(paidBundle);

        AllRSVPFragment vipFragment=new AllRSVPFragment();
        Bundle vipBundle=new Bundle();
        vipBundle.putString("type","vip");
        vipBundle.putInt("id",id);
        vipFragment.setArguments(vipBundle);

        AllRSVPFragment pendingFragment=new AllRSVPFragment();
        Bundle pendingBundle=new Bundle();
        pendingBundle.putString("type","pending");
        pendingBundle.putInt("id",id);
        pendingFragment.setArguments(pendingBundle);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(allRSVPFragment, "All");
        adapter.addFragment(free, "Free RSVP");
        adapter.addFragment(paidFragment, "Paid RSVP");
        adapter.addFragment(vipFragment, "VIP RSVP");
        adapter.addFragment(pendingFragment, "Pending");

        binding.myViewPager.setAdapter(adapter);
        binding.myViewPager.setOffscreenPageLimit(5);
        binding.tabs.setupWithViewPager(binding.myViewPager);
    }

    public void backBtnOnClick(View view) {
        finish();
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


    private void search(String query) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        EventBus.getDefault().post(new CheckedInEvent(query));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _disposable.dispose();
    }
}
