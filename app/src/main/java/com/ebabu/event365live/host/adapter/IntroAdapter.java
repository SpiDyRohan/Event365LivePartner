package com.ebabu.event365live.host.adapter;

import android.graphics.Color;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ebabu.event365live.host.fragments.IntroFragment;

public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter (FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position){

        switch(position){

            case 0:
                return IntroFragment.newInstance(Color.parseColor("#ffffff"), position); // blue
            case 1:
                return IntroFragment.newInstance(Color.parseColor("#ffffff"), position); //ORANGE

            default:
                return IntroFragment.newInstance(Color.parseColor("#ffffff"), position); // green
        }
    }

    @Override
    public int getCount(){
        return 3;
    }
}
