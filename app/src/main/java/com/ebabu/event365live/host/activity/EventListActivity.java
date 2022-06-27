package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.EventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.EventType;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.CreateEventViewModel;
import com.google.gson.JsonObject;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EventListActivity extends BaseActivity {

    CalendarDay day;
    CreateEventViewModel viewModel;
    RecyclerView recyclerView;
    MyLoader loader;
    EventAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        viewModel= ViewModelProviders.of(this).get(CreateEventViewModel.class);
        loader=new MyLoader(this);
        loader.show("");
        recyclerView=findViewById(R.id.rv);

        findViewById(R.id.back_arrow).setOnClickListener(v->onBackPressed());

        adapter=new EventAdapter(EventType.PAST,new EventAdapter.EventClickListener() {

            @Override
            public void eventClicked(EventDAO d) {
                try {
                    startActivity(new Intent(EventListActivity.this, EditEventDetails.class)
                            .putExtra(API.DATA, d));
                }catch (Exception e){
                    Log.e("got issue",d.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void deleteEvent(int id) {

            }
        },"");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);


        day=getIntent().getParcelableExtra("date");

        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("date",day.getDate().toString());

        viewModel.getEventsByDate(jsonObject).observe(this, new Observer<UserResponse>() {
            @Override
            public void onChanged(UserResponse response) {
                loader.dismiss();

                if(!response.isSuccess()){
                    if(response.getCode()== API.SESSION_EXPIRE){
                        response.setSuccess(true);
                        response.setUserData(null);
                        response.setCode(0);
                        Utility.sessionExpired(getApplicationContext());
                    }
                    else
                    Dialogs.toast(getApplicationContext(),recyclerView,response.getMessage());
                }
                else
                    adapter.refresh(response.getEventList());

            }
        });
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
