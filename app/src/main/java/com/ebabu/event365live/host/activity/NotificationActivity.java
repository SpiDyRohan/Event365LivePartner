package com.ebabu.event365live.host.activity;

import android.os.Bundle;
import android.util.Log;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.NotificationAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityNotificationAcitivtyBinding;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationAcitivtyBinding binding;
    private String eventCount, rsvpCount, transactionCount, organizationCount;

    @Inject
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_acitivty);
        App.getAppComponent().inject(this);
        binding.ivBackBtn.setOnClickListener(v -> onBackPressed());
        getNotificationCountRequest();
        Utility.setSharedPreferencesInteger(this, API.NOTIFICATION_COUNT, 0);
    }

    private void getNotificationCountRequest() {
        Call<JsonElement> countCall = apiInterface.getNotificationCount();
        countCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject jsonObject1 = new JSONObject(response.body().toString());
                            eventCount = jsonObject1.getJSONObject("data").getJSONObject("eventCount").getString("count");
                            rsvpCount = jsonObject1.getJSONObject("data").getJSONObject("rsvpCount").getString("count");
                            transactionCount = jsonObject1.getJSONObject("data").getJSONObject("transactionCount").getString("count");
                            organizationCount = jsonObject1.getJSONObject("data").getJSONObject("organizationCount").getString("count");
                            setUpViewPager();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == API.SESSION_EXPIRE) {
                    Utility.sessionExpired(NotificationActivity.this);
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Dialogs.toast(getApplicationContext(), binding.getRoot(), "Something went wrong");
                Log.d("nflkasnklfa", "onFailure: " + t.getMessage());
            }
        });
    }

    private void setUpViewPager() {
        NotificationAdapter notificationAdapter = new NotificationAdapter(this);
        binding.viewPager.setAdapter(notificationAdapter);
        binding.viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    tab.setText(getResources().getStringArray(R.array.notification_tab)[position]);
                    switch (position) {
                        case 0:
                            if (Integer.parseInt(eventCount) > 0) {
                                BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                                badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
                                badgeDrawable.setVisible(true);
                                badgeDrawable.setNumber(Integer.parseInt(eventCount));
                            }
                            break;
                        case 1:
                            if (Integer.parseInt(rsvpCount) > 0) {
                                BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                                badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
                                badgeDrawable.setVisible(true);
                                badgeDrawable.setNumber(Integer.parseInt(rsvpCount));
                            }
                            break;
                        case 2:
                            if (Integer.parseInt(transactionCount) > 0) {
                                BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                                badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
                                badgeDrawable.setVisible(true);
                                badgeDrawable.setNumber(Integer.parseInt(transactionCount));
                            }
                            break;
                        case 3:
                            if (Integer.parseInt(organizationCount) > 0) {
                                BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                                badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.redColor));
                                badgeDrawable.setVisible(true);
                                badgeDrawable.setNumber(Integer.parseInt(organizationCount));
                            }
                            break;
                    }
                }
        ).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                BadgeDrawable badgeDrawable = binding.tabLayout.getTabAt(position).getOrCreateBadge();
                badgeDrawable.setVisible(false);
            }
        });
    }
}
