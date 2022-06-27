package com.ebabu.event365live.host.activity.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.CalendarActivity;
import com.ebabu.event365live.host.activity.EditEventDetails;
import com.ebabu.event365live.host.activity.Home;
import com.ebabu.event365live.host.activity.NotificationActivity;
import com.ebabu.event365live.host.activity.ui.EventActivity;
import com.ebabu.event365live.host.adapter.EventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentHomeBinding;
import com.ebabu.event365live.host.entities.EventType;
import com.ebabu.event365live.host.entities.UserData;
import com.ebabu.event365live.host.events.EventCreatePermissionEvent;
import com.ebabu.event365live.host.events.EventTypeEvent;
import com.ebabu.event365live.host.events.InternetConnectionEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Inject
    RequestManager requestManager;

    @Inject
    ApiInterface apiInterface;

    Context context;
    MyLoader loader;
    EventAdapter relatedEventAdapter;
    LocalBroadcastManager localBroadcastManager;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        App.getAppComponent().inject(this);

        binding.setFrag(this);
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

        binding.profileImage.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), Home.class);
            intent.putExtra("profilePosition",3);
            startActivity(intent);
        });

        relatedEventAdapter = new EventAdapter(EventType.HOME, dao -> {
            Log.d("fnllkasnfkla", "homeFrag: " + dao.toString());
            startActivity(new Intent(getActivity(), EditEventDetails.class).putExtra(API.DATA, dao));
        }, "home");

        context = container.getContext();
        loader = new MyLoader(context);
        loader.show("");
        binding.relatedRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.relatedRv.setHasFixedSize(true);
        binding.relatedRv.setAdapter(relatedEventAdapter);
        new LinearSnapHelper().attachToRecyclerView(binding.relatedRv);

        homeViewModel.getHomeData().observe(getViewLifecycleOwner(), response -> {
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setSuccess(true);
                response.setUserData(null);
                response.setCode(0);
                Utility.sessionExpired(getContext());

            } else if (!response.isSuccess()) {
                response.setCode(0);
                response.setSuccess(true);
                Dialogs.toast(getContext(), binding.getRoot(), response.getMessage());
                Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
            } else {
                UserData userData = response.getUserData();

                if (userData != null) {
                    binding.hostTv.setText(userData.getUser().getUserType());
                    binding.textHome.setText(userData.getUser().getName());
                    binding.pastEventTv.setText(String.valueOf(userData.getCountPastEvent()));
                    binding.upcomingTv.setText(String.valueOf(userData.getCountUpcomingEvent()));
                    binding.guestListTv.setText(String.valueOf(userData.getCountRSVP()));

                    Utility.setSharedPreferencesBoolean(getContext(), API.IS_MANAGE_USER, userData.isManageUser());
                    Utility.setSharedPreferencesBoolean(getContext(), API.IS_CREATE_EVENT, userData.isCreateEvent());
                    Utility.setSharedPreferencesString(getContext(), Constants.CUSTOMER_ID, userData.getUser().getCustomerId());
                    Utility.setSharedPreferencesInteger(getContext(), Constants.USER_ID, userData.getUser().getId());
                    Utility.setSharedPreferencesString(getContext(), Constants.LAST_LOGIN_TIME, userData.getUser().getLastLoginTime());

                    if (userData.getUser().getProfilePic() != null && userData.getUser().getProfilePic().length() > 10)
                        requestManager.load(userData.getUser().getProfilePic()).into(binding.profileImage);
                    else
                        requestManager.load(DrawableUtils.getTempProfilePic(getContext(), userData.getUser().getName())).into(binding.profileImage);

                    binding.addEventImg.setVisibility(View.GONE);
                    binding.createEventTv.setVisibility(View.GONE);

                    if (userData.getUser().getRoles().contains("event_management")) {
                        binding.addEventImg.setVisibility(View.VISIBLE);
                        binding.createEventTv.setVisibility(View.VISIBLE);
                    }

                    EventBus.getDefault().post(new EventCreatePermissionEvent(userData.isCreateEvent(), userData.isManageUser()));

                    if (userData.getUpcomingEvent() != null && userData.getUpcomingEvent().size() > 0)
                        binding.noDataLayout.setVisibility(View.GONE);
                    else
                        binding.noDataLayout.setVisibility(View.VISIBLE);

                    relatedEventAdapter.refresh(userData.getUpcomingEvent());
                }
            }
        });

        homeViewModel.getIfFetching().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loader.show(" ");
            } else loader.dismiss();
        });

        binding.createEventTv.setOnClickListener(v -> createEvent());
        binding.addEventImg.setOnClickListener(v -> createEvent());
        binding.calendar.setOnClickListener(v -> startActivity(new Intent(getActivity(), CalendarActivity.class)));
        binding.imageView7.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NotificationActivity.class));
            binding.counterTv.setText("0");
        });
        binding.counterTv.setText(String.valueOf(Utility.getNotificationSharedPrefInteger(context,API.NOTIFICATION_COUNT)));
        registerReceiver();
        return binding.getRoot();
    }

    private void registerReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter actionReceiver = new IntentFilter();
        actionReceiver.addAction("notificationsActions");
        localBroadcastManager.registerReceiver(onNotificationReceived, actionReceiver);
    }

    public void createEvent() {
        startActivity(new Intent(getActivity(), EventActivity.class));
    }

    public void showEvents(View v) {
        int index = 0;
        if (v.getId() == R.id.past_event_tv || v.getId() == R.id.textView9)
            index = 1;
        EventBus.getDefault().post(new EventTypeEvent(index));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        localBroadcastManager.unregisterReceiver(onNotificationReceived);
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotificationCountRequest();
        EventBus.getDefault().register(this);

        if (Utility.getSharedPreferencesBoolean(getContext(), API.HOT_RELOAD)) {
            homeViewModel.fetchEvents(true);
            Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, false);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void internetListener(InternetConnectionEvent event) {
        if (event.isConnected()) {
            homeViewModel.fetchEvents(false);
        } else
            Dialogs.toast(getContext(), binding.getRoot(), "Connection is lost!");

    }

    private void getNotificationCountRequest() {

        Call<JsonElement> countCall = apiInterface.getNotificationCount();
        countCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response.body().toString());
                        String eventCount = jsonObject1.getJSONObject("data").getJSONObject("eventCount").getString("count");
                        String rsvpCount = jsonObject1.getJSONObject("data").getJSONObject("rsvpCount").getString("count");
                        String transactionCount = jsonObject1.getJSONObject("data").getJSONObject("transactionCount").getString("count");
                        String organizationCount = jsonObject1.getJSONObject("data").getJSONObject("organizationCount").getString("count");
                        int totalCount = Integer.parseInt(eventCount) + Integer.parseInt(rsvpCount) + Integer.parseInt(transactionCount) + Integer.parseInt(organizationCount);
                        if (totalCount > 0) {
                            binding.notificationImg.setVisibility(View.VISIBLE);
                          //  binding.counterTv.setText(String.valueOf(totalCount));
                        } else binding.notificationImg.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

            }
        });

    }

    private BroadcastReceiver onNotificationReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                binding.counterTv.setText(String.valueOf(Utility.getNotificationSharedPrefInteger(context,API.NOTIFICATION_COUNT)));
            }
        }
    };
}