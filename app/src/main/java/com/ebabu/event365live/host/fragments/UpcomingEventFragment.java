package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.EditEventDetails;
import com.ebabu.event365live.host.activity.UpdateEventDetail;
import com.ebabu.event365live.host.activity.ui.notifications.NotificationsFragment;
import com.ebabu.event365live.host.adapter.EventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.FragmentUpcomingEventBinding;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.EventType;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.events.InternetConnectionEvent;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.UpcomingEventsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class UpcomingEventFragment extends Fragment {

    FragmentUpcomingEventBinding binding;
    List<EventDAO> list, temporaryList;
    EventAdapter adapter;
    UpcomingEventsViewModel viewModel;
    MyLoader loader;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upcoming_event, container, false);
        loader = new MyLoader(getContext());
        temporaryList = new ArrayList();
        loader.show("");


        adapter = new EventAdapter(EventType.UPCOMING, new EventAdapter.EventClickListener() {
            @Override
            public void eventClicked(EventDAO dao) {
                Log.d("fnllkasnfkla", "onCreateView: "+dao.toString());

                startActivity(new Intent(getActivity(), EditEventDetails.class).putExtra(API.DATA, dao));
            }

            @Override
            public void editEvent(int id) {
                Intent intent = new Intent(getActivity(), UpdateEventDetail.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", id);
                startActivity(intent);
            }

            @Override
            public void deleteEvent(int id) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(getString(R.string.really_want_delete))
                        .setTitle(getString(R.string.alert))
                        .setPositiveButton("Delete", (dialogInterface, i) -> {
                            viewModel.deleteEvent(id).observe(UpcomingEventFragment.this, new Observer<MyResponse>() {
                                @Override
                                public void onChanged(MyResponse myResponse) {
                                    loader.dismiss();
                                    if (myResponse.isSuccess()) {
                                        Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                                        Toast.makeText(getContext(), "Event is deleted successfully!", Toast.LENGTH_LONG).show();
                                        list.remove(new EventDAO(id));
                                        adapter.refresh(list);
                                    } else {
                                        Dialogs.toast(getContext(), binding.getRoot(), myResponse.getMessage());
                                    }
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }, "");

        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rv.setHasFixedSize(true);

        binding.rv.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(UpcomingEventsViewModel.class);


        viewModel.sessionExpired().observe(this, aBoolean -> {
            if (aBoolean) {
                viewModel.resetState();
                Utility.sessionExpired(getContext());
            }
        });

        fetchUpcomingEvents();

        return binding.getRoot();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTextSearchedEvent(CheckedInEvent event) {
        if (event.getQuery().length() > 0) {
            temporaryList.clear();
            for (EventDAO dao : list) {
                if (dao.getName().startsWith(event.getQuery()))
                    temporaryList.add(dao);
            }
            if (temporaryList.size() == 0)
                Dialogs.itemNotFoundView(getLayoutInflater(), binding.rootLayout, "Oops! You don't have upcoming event. Create an event now.", v -> {
                    loader.show("");
                    NotificationsFragment.clearSearch();
                    viewModel.fetchData(true);
                });
            else if (binding.rootLayout.getChildCount() > 1) {
                binding.rootLayout.removeViewAt(1);
            }
            adapter.refresh(temporaryList);
        } else {
            if(list.size()>0) {
                if (binding.rootLayout.getChildCount() > 1)
                    binding.rootLayout.removeViewAt(1);
            }
            adapter.refresh(list);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (Utility.getSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS)) {
            NotificationsFragment.clearSearch();
            viewModel.fetchData(true);
            Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void internetListener(InternetConnectionEvent event) {
        if (event.isConnected()) {
            NotificationsFragment.clearSearch();
            viewModel.fetchData(false);
        }else
            Dialogs.toast(getContext(), binding.getRoot(), "Connection is lost!");

    }

    private void fetchUpcomingEvents(){
        viewModel.getUpComingEvents().observe(getViewLifecycleOwner(), new Observer<List<EventDAO>>() {
            @Override
            public void onChanged(List<EventDAO> list) {

                UpcomingEventFragment.this.list = list;
                loader.dismiss();
                if (list.size() == 0) {
                    Dialogs.itemNotFoundView(inflater, binding.rootLayout, "Oops! You don't have upcoming event. Create an event now.", v -> {
                        loader.show("");
                        NotificationsFragment.clearSearch();
                        viewModel.fetchData(true);
                    });
                } else {
                    if (binding.rootLayout.getChildCount() > 1) {
                        binding.rootLayout.removeViewAt(1);
                    }
                }
                adapter.refresh(list);
            }
        });

    }
}
