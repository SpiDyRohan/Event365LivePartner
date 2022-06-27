package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.ProfileActivity;
import com.ebabu.event365live.host.adapter.CheckedInRSVPAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentCheckedInRsvBinding;
import com.ebabu.event365live.host.entities.CheckedInDao;
import com.ebabu.event365live.host.entities.TicketBookedRelation;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckedInRSVPFragment extends Fragment {

    private FragmentCheckedInRsvBinding inRsvBinding;
    private CheckedInRSVPAdapter inRSVPAdapter;
    private List<CheckedInDao>list;
    private int limit=30,page=0,eventId;
    @Inject
    ApiInterface apiInterface;
    private MyLoader loader;
    private String personName="";
    private boolean wasNetworkError=false,isFetching;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        loader=new MyLoader(getContext());
        list=new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inRsvBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_checked_in_rsv, container, false);
        eventId=getArguments().getInt("id");

        setupCheckInRSVPList();
        fetchData();

        return inRsvBinding.getRoot();
    }

    private void setupCheckInRSVPList(){

        inRSVPAdapter = new CheckedInRSVPAdapter(id->{
            startActivity(new Intent(getActivity(),ProfileActivity.class)
                .putExtra("id",id)
            );
        });
        inRsvBinding.recyclerCheckedInRSVPList.setAdapter(inRSVPAdapter);

        inRsvBinding.recyclerCheckedInRSVPList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!isFetching) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() -1){
                        fetchData();
                    }
                }

            }
        });


        inRsvBinding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isFetching) {
                    page=0;
                    list.clear();
                    fetchData();
                }
            }
        });

    }

    private void fetchData(){

        if (!Utility.isNetworkAvailable(getContext())) {
            if(inRsvBinding.swipe.isRefreshing())inRsvBinding.swipe.setRefreshing(false);
            wasNetworkError=true;
            inRsvBinding.noDataLayout.setVisibility(View.VISIBLE);
            inRsvBinding.textView48.setText(getString(R.string.check_network));
            inRsvBinding.recyclerCheckedInRSVPList.setVisibility(View.GONE);
            Toast.makeText(getContext(),getString(R.string.check_network),Toast.LENGTH_LONG).show();
            return;
        }

        if(!inRsvBinding.swipe.isRefreshing())
        loader.show("");
        isFetching=true;
        Call<JsonElement> call= apiInterface.fetchCheckedInUser(
                                eventId,
                                personName,
                                true,
                                limit,
                                ++page
                                );

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call,@NonNull Response<JsonElement> response) {
                loader.dismiss();
                isFetching=false;
                if(wasNetworkError){
                    wasNetworkError=false;
                    inRsvBinding.textView48.setText(getString(R.string.no_data_found));
                    inRsvBinding.noDataLayout.setVisibility(View.GONE);
                    inRsvBinding.recyclerCheckedInRSVPList.setVisibility(View.VISIBLE);
                }
                if(inRsvBinding.swipe.isRefreshing()) inRsvBinding.swipe.setRefreshing(false);
                if(response.isSuccessful()){
                    try {
                        JSONObject OBJ= new JSONObject(response.body().toString());
                        JSONArray data=OBJ.getJSONArray(API.DATA);
                        for(int i=0;i<data.length();i++){
                            String s=data.getJSONObject(i).toString();
                            list.add(new Gson().fromJson(s, CheckedInDao.class));
                        }

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        list.clear();
                    }
                }
                else{
                    try {

                        JSONObject OBJ= new JSONObject(response.errorBody().string());
                        String message=OBJ.getString(API.MESSAGE);
                        int code=OBJ.getInt(API.CODE);
                        if(code== API.SESSION_EXPIRE){
                            Utility.sessionExpired(getContext());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                List<TicketBookedRelation> ticketBookedRelations = new ArrayList<>();
                int size = 0;
                for (int i = 0; i < list.size(); i++) {
                    //ticketBookedRelations.clear();
                    size = size + list.get(i).getTicket_number_booked_rel().size();
                    for (int j = 0; j < list.get(i).getTicket_number_booked_rel().size(); j++) {
                        TicketBookedRelation ticketBookedRelation = new TicketBookedRelation(
                                list.get(i).getTicket_number_booked_rel().get(j).getId(),
                                list.get(i).getTicket_number_booked_rel().get(j).getStatus(),
                                list.get(i).getUsers().getName(), list.get(i).getUsers().getProfilePic(),
                                list.get(i).getUserId(),list.get(i).getTicketType(),list.get(i).getPricePerTicket());

                        ticketBookedRelations.add(ticketBookedRelation);
                    }
                   // list.get(i).setTicket_number_booked_rel(ticketBookedRelations);
                }
                inRSVPAdapter.refresh(ticketBookedRelations);
                if(list.size()==0)
                    inRsvBinding.noDataLayout.setVisibility(View.VISIBLE);
                else
                    inRsvBinding.noDataLayout.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call,@NonNull Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                isFetching=false;
                if(inRsvBinding.swipe.isRefreshing()) inRsvBinding.swipe.setRefreshing(false);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTextSearchedEvent(CheckedInEvent event) {
        if(!event.getQuery().equals(personName)){
            personName=event.getQuery();
            page=1;
            list.clear();
            fetchData();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
