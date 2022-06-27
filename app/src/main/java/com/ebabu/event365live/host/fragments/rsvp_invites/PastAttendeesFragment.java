package com.ebabu.event365live.host.fragments.rsvp_invites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentPastAttendeesBinding;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.fragments.rsvp_invites.adapter.PastAttendeesAdapter;
import com.ebabu.event365live.host.fragments.rsvp_invites.model.AttendeesData;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.PastEventViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

import static android.app.Activity.RESULT_OK;

public class PastAttendeesFragment extends Fragment {
    @Inject
    ApiInterface apiInterface;
    String searchKey = "";
    MyLoader loader;
    boolean loadAgain = true;
    List<AttendeesData> list = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    boolean fire = false;
    int eventID;
    private FragmentPastAttendeesBinding binding;
    private Context context;
    private PastAttendeesAdapter rsvpAdapter;
    private int userId, contactStatus = 2,page = 1;
    public static String startDate = "", endDate = "";
    public static final int FILTER_CALENDER_REQUEST_CODE = 1231;
    private AlertDialog alertDialog;
    List<EventDAO> pastEventList;
    PastEventViewModel viewModel;
    private boolean isEventFilerOpen = false,wasNetworkError,isFetching;
    public JsonArray filterEventId;
    public static ArrayList<Integer> eventIdsList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_past_attendees, container, false);
        App.getAppComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventID = getArguments().getInt("id", -1);
        loader = new MyLoader(context);

        linearLayoutManager = new LinearLayoutManager(context);
        binding.rv.setLayoutManager(linearLayoutManager);

        setupInviteContactList();

        Places.initialize(context.getApplicationContext(), getString(R.string.google_maps_key));

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isFetching) {
                    loadAgain = true;
                    page = 1;
                    list.clear();
                    rsvpAdapter.notifyDataSetChanged();
                    fetchCustomers();
                }
            }
        });

        binding.inviteBtn.setOnClickListener(v -> {
            boolean selected = false;
            JsonArray jsonArray = new JsonArray();

            for (AttendeesData userDAO : list) {
                for (AttendeesData.TicketBooked booked : userDAO.getTicketBooked()) {
                    if (booked.isChecked()) {
                        jsonArray.add(booked.getUserId());
                    }
                }
            }

            if (jsonArray.size() > 0) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("id", jsonArray);

                loader.show("");

                apiInterface.inviteRSVP(eventID, jsonObject).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        loader.dismiss();
                        if (response.isSuccessful()) {

                            try {
                                JSONObject obj = new JSONObject(response.body().toString());

                                Dialogs.showActionDialog(context,
                                        getString(R.string.app_name),
                                        obj.getString(API.MESSAGE),
                                        "Done",
                                        v1 -> getActivity().finish()
                                );

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (response.code() == API.SESSION_EXPIRE)
                                Utility.sessionExpired(context);

                            else {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                    Dialogs.toast(context, v, jsonObject.getString(API.MESSAGE));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        loader.dismiss();
                        t.printStackTrace();
                    }
                });

                /*loader.dismiss();
                Dialogs.showActionDialog(InviteRSVPActivity.this,
                        getString(R.string.app_name),
                        "Congrats! invitation has been send to selected people.",
                        "Done",
                        v1->{
                            rsvpAdapter.notifyDataSetChanged();
                        }
                );*/

            } else {
                Toast.makeText(context, "Select atleast one contact please!", Toast.LENGTH_LONG).show();
            }
        });


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!isFetching && loadAgain) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        fetchCustomers();

                }
            }
        });


        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    //if( binding.searchEt.getText().toString().trim().length()>0) {
                    if (!searchKey.equalsIgnoreCase(v.getText().toString())) {
                        searchKey = v.getText().toString();
                        list.clear();
                        page = 1;
                        fetchCustomers();
                    } else
                        Utility.hideKeyboard(getActivity());
                //}
                return false;
            }
        });

        fetchCustomers();

        fetchPastData();

    }

    private void fetchCustomers() {

        if (!Utility.isNetworkAvailable(context)) {
            if (binding.swipe.isRefreshing()) binding.swipe.setRefreshing(false);
            wasNetworkError = true;
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.textView48.setText(getString(R.string.check_network));
            binding.rv.setVisibility(View.GONE);
            Toast.makeText(context, getString(R.string.check_network), Toast.LENGTH_LONG).show();
            return;
        }


        if (!binding.swipe.isRefreshing())
            loader.show("");
        isFetching = true;

        userId = Utility.getSharedPreferencesInteger(context, Constants.USER_ID);

        String eventIds = "";

        if(!eventIdsList.isEmpty()){
            filterEventId = new JsonArray();
            for (int i = 0; i < eventIdsList.size(); i++){
                filterEventId.add(eventIdsList.get(i));
            }
        }

        JsonObject object = new JsonObject();
        object.addProperty("userId", userId);
        object.addProperty("page", page);
        object.addProperty("contactStatus", contactStatus);
        object.addProperty("searchKey", searchKey);
        object.addProperty("startDate", startDate);
        object.addProperty("endDate", endDate);

        if(!eventIdsList.isEmpty()) {
            object.add("eventId", filterEventId);
        }else {
            object.addProperty("eventId", eventIds);
        }

//        Call<JsonElement> call = apiInterface.fetchAttendees(userId, page, contactStatus, searchKey, startDate, endDate, eventIds);
        Call<JsonElement> call = apiInterface.fetchAttendees(object);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loader.dismiss();

                isFetching = false;
                if (wasNetworkError) {
                    wasNetworkError = false;
                    binding.textView48.setText(getString(R.string.no_data_found));
                    binding.noDataLayout.setVisibility(View.GONE);
                    binding.rv.setVisibility(View.VISIBLE);
                }
                if (binding.swipe.isRefreshing())
                    binding.swipe.setRefreshing(false);

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONArray dataArray = OBJ.getJSONArray("data");
                        if (dataArray.length() > 0)
                            for (int i = 0; i < dataArray.length(); i++) {
                                String s = dataArray.getJSONObject(i).toString();
                                list.add(new Gson().fromJson(s, AttendeesData.class));
                            }
                        else
                            loadAgain = false;

                        page++;
                        if (!fire) fire = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context.getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        int code = OBJ.getInt(API.CODE);
                        if (code == API.SESSION_EXPIRE) {
                            Utility.sessionExpired(context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rsvpAdapter.refresh(list);
                if (list.size() == 0) binding.noDataLayout.setVisibility(View.VISIBLE);
                else
                    binding.noDataLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setupInviteContactList() {
        rsvpAdapter = new PastAttendeesAdapter(context);
        binding.rv.setAdapter(rsvpAdapter);
    }

    public void filterAttendeesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_attendees_filter, null);

        ImageView ivClose = (ImageView) view.findViewById(R.id.ivClose);
        TextView tvReset = (TextView) view.findViewById(R.id.tvReset);
        RelativeLayout rlFilterByDate = (RelativeLayout) view.findViewById(R.id.rlFilterByDate);
        RelativeLayout rlFilterByEvent = (RelativeLayout) view.findViewById(R.id.rlFilterByEvent);
        TextView tvFilterByEventName = (TextView) view.findViewById(R.id.tvFilterByEventName);
        LinearLayout llEvents = (LinearLayout) view.findViewById(R.id.llEvents);
        ScrollView svEventData = (ScrollView) view.findViewById(R.id.svEventData);

        rlFilterByEvent.setOnClickListener(view1 -> {
            if(isEventFilerOpen){
                isEventFilerOpen= false;
                svEventData.setVisibility(View.GONE);
                tvFilterByEventName.setText("Filter by Event");
            }else {
                isEventFilerOpen = true;
                if(pastEventList!=null) {
                    svEventData.setVisibility(View.VISIBLE);
                    tvFilterByEventName.setText("Filter by Event Name");
                }else
                    Toast.makeText(getContext(),getString(R.string.no_event_found),Toast.LENGTH_LONG).show();
            }
        });

        try{

            llEvents.removeAllViews();
            if(pastEventList!=null) {
                for (int i = 0; i < pastEventList.size(); i++) {

                    View pastAttendees = getLayoutInflater().inflate(R.layout.layout_item_filter_event, llEvents, false);
                    TextView tvEventName = pastAttendees.findViewById(R.id.tvEventName);
                    tvEventName.setText(pastEventList.get(i).getName());
                    llEvents.addView(pastAttendees);

                    if (eventIdsList.contains(pastEventList.get(i).getId())) {
                        tvEventName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    } else {
                        tvEventName.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }

                    int finalI = i;
                    tvEventName.setOnClickListener(view1 -> {

                        if (eventIdsList.contains(pastEventList.get(finalI).getId())) {
                            for (int j = 0; j < eventIdsList.size(); j++) {
                                if (eventIdsList.get(j) == pastEventList.get(finalI).getId()) {
                                    eventIdsList.remove(j);
                                }
                            }
                            tvEventName.setTextColor(ContextCompat.getColor(context, R.color.black));
                        } else {
                            eventIdsList.add(pastEventList.get(finalI).getId());
                            tvEventName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        }

                        list.clear();
                        page = 1;
                        // alertDialog.dismiss();
                        fetchCustomers();
                    });
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        tvReset.setOnClickListener(view1 -> {
            list.clear();
            searchKey = "";
            startDate = "";
            endDate = "";
            page = 1;
            eventIdsList.clear();
            alertDialog.dismiss();
            fetchCustomers();
        });

        ivClose.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        rlFilterByDate.setOnClickListener(view1 -> {

            Intent calenderIntent = new Intent(context, CalenderFilterActivity.class);
            calenderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(calenderIntent, FILTER_CALENDER_REQUEST_CODE);

        });

        builder.setView(view);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if (requestCode == FILTER_CALENDER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                list.clear();
                searchKey = "";
                page = 1;
                alertDialog.dismiss();
                fetchCustomers();
            }
        }
    }

    private void fetchPastData(){

        viewModel= ViewModelProviders.of(this).get(PastEventViewModel.class);

        viewModel.getPastEvents().observe(getViewLifecycleOwner(), new Observer<List<EventDAO>>() {
            @Override
            public void onChanged(List<EventDAO> list) {
                Log.d("fnaslknflsa", "onChanged: "+list.size());

                loader.dismiss();
                if(list.size()==0){
                    pastEventList=null;
                } else{
                    pastEventList = list;
                }

                //adapter.refresh(list);
            }
        });
    }

}