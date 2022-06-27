package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.MyVenues;
import com.ebabu.event365live.host.activity.ProfileActivity;
import com.ebabu.event365live.host.databinding.FragmentProfileBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false);

/*        Window window = getActivity().getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(getContext(),R.color.whiteLight));*/

        binding.profileSettings.setOnClickListener(v->startActivity(new Intent(getActivity(), ProfileActivity.class)));
        //binding.venueLocation.setOnClickListener(v->startActivity(new Intent(getActivity(), MyVenues.class)));

        binding.venueLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.setSharedPreferencesString(getContext(), Constants.startDate24,"");
                Utility.setSharedPreferencesString(getContext(),Constants.endDate24,"");
                startActivity(new Intent(getActivity(), MyVenues.class).putExtra("from", "profile"));
            }
        });

        return binding.getRoot();
    }

}
