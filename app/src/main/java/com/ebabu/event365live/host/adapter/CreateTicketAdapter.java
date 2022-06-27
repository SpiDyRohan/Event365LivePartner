package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.fragments.ticket.RegularTicketFragment;
import com.ebabu.event365live.host.fragments.ticket.RsvpVipTabFragment;
import com.ebabu.event365live.host.fragments.ticket.TableSeatingTabFragment;

public class CreateTicketAdapter extends FragmentStatePagerAdapter {

    private RegularTicketFragment regularTicketFragment;
    private RsvpVipTabFragment rsvpVipTabFragment;
    private TableSeatingTabFragment tableSeatingTabFragment;
    private Bundle bundle;
    private String[] tabName = {"Regular", "RSVP / VIP", "Table & Seatings"};
    private Context context;

    public CreateTicketAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                if (regularTicketFragment == null)
                    regularTicketFragment = new RegularTicketFragment();
                regularTicketFragment.setArguments(getBundle());
                fragment = regularTicketFragment;
                break;

            case 1:
                if (rsvpVipTabFragment == null)
                    rsvpVipTabFragment = new RsvpVipTabFragment();
                fragment = rsvpVipTabFragment;
                break;

            case 2:
                if (tableSeatingTabFragment == null)
                    tableSeatingTabFragment = new TableSeatingTabFragment();
                fragment = tableSeatingTabFragment;

                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return tabName.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return context.getString(R.string.regular);
        }else if(position == 1) {
            return context.getString(R.string.rsvp_vip);
        }else if(position == 2) {
            return context.getString(R.string.table_and_seatings);
        }
        return "";
    }

    public Bundle getBundle() {
        if (bundle == null) {
            bundle = new Bundle();
        }
//        Log.d("fnlkanfla", "Bundle: " + nearByNoAuthModal.size());
//        bundle.putParcelableArrayList(Constants.nearByData, nearByNoAuthModal);
        return bundle;
    }
}

