package com.ebabu.event365live.host.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.fragments.EventNotificationFragment;
import com.ebabu.event365live.host.fragments.ProfileNotificationFragment;
import com.ebabu.event365live.host.fragments.RSVPNotificationFragment;
import com.ebabu.event365live.host.fragments.TransactionNotificationFragment;

public class NotificationAdapter extends FragmentStateAdapter {

    private final String[] tabs;

    public NotificationAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        tabs = fragmentActivity.getResources().getStringArray(R.array.notification_tab);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = new EventNotificationFragment();
        switch (position) {
            case 0:
                fragment = new EventNotificationFragment();
                break;
            case 1:
                fragment = new RSVPNotificationFragment();
                break;
            case 2:
                fragment = new TransactionNotificationFragment();
                break;
            case 3:
                fragment = new ProfileNotificationFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return tabs.length;
    }
}

