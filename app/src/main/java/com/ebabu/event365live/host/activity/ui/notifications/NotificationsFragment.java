package com.ebabu.event365live.host.activity.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.NotificationActivity;
import com.ebabu.event365live.host.activity.ui.EventActivity;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.FragmentNotificationsBinding;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.fragments.PastEventFragment;
import com.ebabu.event365live.host.fragments.UpcomingEventFragment;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class NotificationsFragment extends Fragment {
    FragmentNotificationsBinding binding;
    public ViewPager myViewPager;
    static String name="";
    static EditText searchTxt;


    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);
        searchTxt=binding.searchEt;
        binding.notificationImg.setVisibility(View.GONE);
        myViewPager=binding.myViewPager;

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(name.length()>0) {
                    if (binding.searchEt.getText().toString().trim().length()==0) {
                        name = "";
                        EventBus.getDefault().post(new CheckedInEvent(name));
                    }
                }
            }
        });

        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) ||
                        (actionId == EditorInfo.IME_ACTION_SEARCH)) {

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);

                    if (!name.equalsIgnoreCase(binding.searchEt.getText().toString().trim())) {
                        name = binding.searchEt.getText().toString().trim();
                        EventBus.getDefault().post(new CheckedInEvent(name));
                    }

                }
                return false;
            }
        });

       /* if(!Utility.getSharedPreferencesBoolean(getContext(), API.IS_CREATE_EVENT))
            binding.imageView24.setVisibility(View.INVISIBLE);*/

        binding.imageView24.setVisibility(View.GONE);
        showHideCreateEvent();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new UpcomingEventFragment(), getString(R.string.upcoming_events));
        adapter.addFragment(new PastEventFragment(), getString(R.string.past_events));
        binding.myViewPager.setAdapter(adapter);

        binding.tabs.setupWithViewPager(binding.myViewPager);

        binding.backArrow.setOnClickListener(v-> getActivity().onBackPressed());

        //create event
        binding.imageView24.setOnClickListener(v-> startActivity(new Intent(getActivity(), EventActivity.class)));
        binding.imageView7.setOnClickListener(v-> startActivity(new Intent(getActivity(), NotificationActivity.class)));

        return binding.getRoot();
    }

    public void showHideCreateEvent(){
        try {
            if(!TextUtils.isEmpty(Utility.getSharedPreferencesString(getContext(), Constants.USER_LOGIN_DATA))){
                JSONObject userObject=new JSONObject(Utility.getSharedPreferencesString(getContext(), Constants.USER_LOGIN_DATA));

                if (userObject.getString("roles").contains("event_management"))
                    binding.imageView24.setVisibility(View.VISIBLE);
            }else{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showHideCreateEvent();
                    }
                }, 2000);
            }
        }catch (Exception e){e.printStackTrace();}
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
    public void onResume() {
        super.onResume();
        name="";
        binding.searchEt.setText("");
    }

    public static void clearSearch(){
        name="";
        searchTxt.setText("");
    }
}