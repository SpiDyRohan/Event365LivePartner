package com.ebabu.event365live.host.bankdetails.adapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TransactionHistoryVPAdapter extends FragmentPagerAdapter {

    private String[] transactionName = {"Pending","Completed"};
    private List<Fragment> fragments = new ArrayList<>();

    public TransactionHistoryVPAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return transactionName.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return transactionName[position];
    }

    public void addFrag(Fragment fragment){
        fragments.add(fragment);
    }





}
