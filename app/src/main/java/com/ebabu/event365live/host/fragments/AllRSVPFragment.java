package com.ebabu.event365live.host.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.ProfileActivity;
import com.ebabu.event365live.host.adapter.AllRSVPAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentAllRsvBinding;
import com.ebabu.event365live.host.entities.CheckedInDao;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllRSVPFragment extends Fragment {
    int eventId;
    private String personName="";
    private int limit=30,page=1;

    private List<CheckedInDao> list;
    MyLoader loader;
    private boolean isFetching;
    String rsvpType;

    @Inject  ApiInterface apiInterface;
    @Inject  Context mContext;
    FragmentAllRsvBinding binding;

    AllRSVPAdapter allRSVPAdapter;
    private boolean wasNetworkError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        rsvpType=getArguments().getString("type");
        eventId=getArguments().getInt("id");
        list=new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_all_rsv, container, false);

        loader=new MyLoader(getContext());
        personName = Utility.getSharedPreferencesString(mContext,"query");

        allRSVPAdapter=new AllRSVPAdapter(id ->
                startActivity(new Intent(getActivity(), ProfileActivity.class)
                        .putExtra("id",id))
        );
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rv.setHasFixedSize(true);
        binding.rv.setAdapter(allRSVPAdapter);


        fetchData();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isFetching) {
                    page=1;
                    list.clear();
                    fetchData();
                }
            }
        });

        return binding.getRoot();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTextSearchedEvent(CheckedInEvent event) {
        if(!event.getQuery().equalsIgnoreCase(personName)){
            personName=event.getQuery();
            page=1;
            list.clear();
            fetchData();
        }
    }

    private void fetchData(){

        if (!Utility.isNetworkAvailable(getContext())) {
            if(binding.swipe.isRefreshing())binding.swipe.setRefreshing(false);
            wasNetworkError=true;
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.textView48.setText(getString(R.string.check_network));
            binding.rv.setVisibility(View.GONE);
            Toast.makeText(getContext(),getString(R.string.check_network),Toast.LENGTH_LONG).show();
            return;
        }

        if(!binding.swipe.isRefreshing())
            loader.show("");
        isFetching=true;
        Call<JsonElement> call= apiInterface.fetchAllRSVP(
                eventId,
                personName,
                rsvpType,
                limit,
                page
        );

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loader.dismiss();
                isFetching=false;
                if(wasNetworkError){
                    wasNetworkError=false;
                    binding.textView48.setText(getString(R.string.no_data_found));
                    binding.noDataLayout.setVisibility(View.GONE);
                    binding.rv.setVisibility(View.VISIBLE);
                }
                if(binding.swipe.isRefreshing()) binding.swipe.setRefreshing(false);

                if(response.isSuccessful()){
                    try {
                        JSONObject OBJ= new JSONObject(response.body().toString());
                        JSONArray data=OBJ.getJSONObject(API.DATA).getJSONArray("rspvType");

                        if(data.length()>0)page++;

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
                            Utility.sessionExpired(mContext);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                allRSVPAdapter.refresh(list);
                if(list.size()==0)binding.noDataLayout.setVisibility(View.VISIBLE);
                else{
                    binding.noDataLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });

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
