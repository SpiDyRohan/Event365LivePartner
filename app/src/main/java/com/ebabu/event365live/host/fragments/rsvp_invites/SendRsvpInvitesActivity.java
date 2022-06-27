package com.ebabu.event365live.host.fragments.rsvp_invites;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivitySendRsvpInvitesBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class SendRsvpInvitesActivity extends AppCompatActivity {

    private ActivitySendRsvpInvitesBinding binding;
    private int eventID;
    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_send_rsvp_invites);
        eventID = getIntent().getIntExtra("id", -1);

        binding.filterImg.setOnClickListener(view -> {
            int pos = binding.viewPager.getCurrentItem();
            Fragment activeFragment = adapter.getItem(pos);
            if(pos == 2)
                ((PastAttendeesFragment)activeFragment).filterAttendeesDialog();
        });

        setupAllViewRSVP();
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    private void setupAllViewRSVP() {

        AllContactSFragment allContactFragment = new AllContactSFragment();
        Bundle allBundle = new Bundle();
        allBundle.putString("type", "all");
        allBundle.putInt("id", eventID);
        allContactFragment.setArguments(allBundle);

        MyContactsFragment myContactsFragment = new MyContactsFragment();
        Bundle freeBundle = new Bundle();
        freeBundle.putString("type", "my");
        freeBundle.putInt("id", eventID);
        myContactsFragment.setArguments(freeBundle);

        PastAttendeesFragment paidFragment = new PastAttendeesFragment();
        Bundle paidBundle = new Bundle();
        paidBundle.putString("type", "past");
        paidBundle.putInt("id", eventID);
        paidFragment.setArguments(paidBundle);


        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(allContactFragment, "All Contacts");
        adapter.addFragment(myContactsFragment, "My Contacts");
        adapter.addFragment(paidFragment, "Past Attendees");

        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(3);
        binding.tabs.setupWithViewPager(binding.viewPager);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.tabs.getTabAt(0).select();
                    binding.filterImg.setVisibility(View.GONE);
                } else if (position == 1) {
                    binding.tabs.getTabAt(1).select();
                    binding.filterImg.setVisibility(View.GONE);
                } else if (position == 2) {
                    binding.tabs.getTabAt(2).select();
                    binding.filterImg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
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

    public void backBtnOnClick(View view) {
        finish();
    }

}