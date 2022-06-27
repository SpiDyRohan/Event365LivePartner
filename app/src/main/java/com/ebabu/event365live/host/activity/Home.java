package com.ebabu.event365live.host.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.ui.home.HomeFragment;
import com.ebabu.event365live.host.activity.ui.notifications.NotificationsFragment;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.events.EventTypeEvent;
import com.ebabu.event365live.host.fragments.MyProfile;
import com.ebabu.event365live.host.fragments.ProfileFragment;
import com.ebabu.event365live.host.fragments.SettingFragment;
import com.ebabu.event365live.host.utils.Utility;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class Home extends BaseActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    LinkedList<Integer> tabStackTrace = new LinkedList<>();
    BottomNavigationView navView;
    ViewPager myViewPager;

    View internetView;

    private NotificationsFragment notificationsFragment;
    private SettingFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        myViewPager = findViewById(R.id.my_view_pager);
        navView = findViewById(R.id.nav_view);

        tabStackTrace.add(0);

        internetView = findViewById(R.id.no_internet_layout);

        setFragmentsWithViewPager();
        if (getIntent().hasExtra("profilePosition")) {
            navView.setSelectedItemId(R.id.navigation_profile);
            myViewPager.setCurrentItem(3, false);
        }
        navView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.navigation_home:

                    int alreadyHomePosition = tabStackTrace.indexOf(0);
                    if (alreadyHomePosition == -1)
                        tabStackTrace.add(0);
                    else {
                        tabStackTrace.remove(alreadyHomePosition);
                        tabStackTrace.add(0);
                    }

                    myViewPager.setCurrentItem(0, false);
                    break;

               /* case R.id.navigation_dashboard:

                    int alreadyDashboardPosition = tabStackTrace.indexOf(1);

                    if (alreadyDashboardPosition == -1)
                        tabStackTrace.add(1);
                    else {
                        tabStackTrace.remove(alreadyDashboardPosition);
                        tabStackTrace.add(1);
                    }

                    myViewPager.setCurrentItem(1, false);
                    break;*/

                case R.id.navigation_notifications:
                    int alreadyNotificationPosition = tabStackTrace.indexOf(2);

                    if (alreadyNotificationPosition == -1)
                        tabStackTrace.add(2);
                    else {
                        tabStackTrace.remove(alreadyNotificationPosition);
                        tabStackTrace.add(2);
                    }

                    myViewPager.setCurrentItem(1, false);
                    break;

                case R.id.navigation_profile:
                    int alreadyProfilePosition = tabStackTrace.indexOf(3);

                    if (alreadyProfilePosition == -1)
                        tabStackTrace.add(3);
                    else {
                        tabStackTrace.remove(alreadyProfilePosition);
                        tabStackTrace.add(3);
                    }
                    myViewPager.setCurrentItem(2, false);
                    break;

                case R.id.navigation_setting:

                    int alreadySettingPosition = tabStackTrace.indexOf(4);

                    if (alreadySettingPosition == -1)
                        tabStackTrace.add(4);
                    else {
                        tabStackTrace.remove(alreadySettingPosition);
                        tabStackTrace.add(4);
                    }

                    myViewPager.setCurrentItem(3, false);
                    break;
            }
            return true;
        });

    }




    private void setFragmentsWithViewPager() {
        boolean isVenuer = Utility.getSharedPreferencesBoolean(this, API.IS_VENUE_OWNER);
        isVenuer=true;
        notificationsFragment = new NotificationsFragment();
        settingFragment = new SettingFragment();
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), getString(R.string.title_home));
        adapter.addFragment(notificationsFragment, getString(R.string.title_notifications));
        adapter.addFragment(isVenuer ? new ProfileFragment() : new MyProfile(), "fragment_profile");
        adapter.addFragment(settingFragment, "fragment_setting");

        myViewPager.setAdapter(adapter);
        // TODO: 31/12/19  myViewPager.setOffscreenPageLimit(5);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        if (tabStackTrace.size() == 1 && myViewPager.getCurrentItem() != 0) {
            myViewPager.setCurrentItem(0, false);
            navView.getMenu().getItem(0).setChecked(true);
        } else if (tabStackTrace.size() == 1)
            super.onBackPressed();
        else {
            tabStackTrace.removeLast();
            myViewPager.setCurrentItem(tabStackTrace.getLast(), true);
            navView.getMenu().getItem(tabStackTrace.getLast()).setChecked(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventSelectListener(EventTypeEvent event) {
        navView.setSelectedItemId(R.id.navigation_notifications);
        notificationsFragment.myViewPager.setCurrentItem(event.getNum(), false);
    }


    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                internetView.setVisibility(View.GONE);
            else
                internetView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 4001) { //Coming from WithdrawActivity
            int alreadyHomePosition = tabStackTrace.indexOf(0);
            if (alreadyHomePosition == -1)
                tabStackTrace.add(0);
            else {
                tabStackTrace.remove(alreadyHomePosition);
                tabStackTrace.add(0);
            }
            navView.setSelectedItemId(R.id.navigation_home);
            myViewPager.setCurrentItem(0, false);
        }
    }

    private void checkSessionOfGoogleFb() {
        account = GoogleSignIn.getLastSignedInAccount(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        if (account != null) {
            Log.d("fnlanflknalf", "login with google: ");
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            });
        } else {
            if (LoginManager.getInstance() != null) {
                Log.d("fnlanflknalf", "login with fb: ");
                LoginManager.getInstance().logOut();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkSessionOfGoogleFb();
    }
}


