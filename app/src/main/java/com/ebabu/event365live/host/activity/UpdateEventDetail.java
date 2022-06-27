package com.ebabu.event365live.host.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.fragments.CreateEventFragment;
import com.ebabu.event365live.host.fragments.ticket.RegularTicketFragment;

import androidx.fragment.app.Fragment;

public class UpdateEventDetail extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event_detail);
        Fragment createEventFragment = new CreateEventFragment();
        Bundle bundle=new Bundle();
        bundle.putBoolean("update",true);
        bundle.putInt("id",getIntent().getIntExtra("id",-1));
        createEventFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_layout, createEventFragment).commit();

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
}
