package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.DaysAdapter;
import com.ebabu.event365live.host.adapter.SubVenueAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityVenueDetailSelectBinding;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.SubVenueDao;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.VenueViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class VenueDetail extends BaseActivity {

    ActivityVenueDetailSelectBinding binding;

    @Inject
    RequestManager requestManager;

    VenueViewModel model;
    VenueDAO dao;
    MyLoader loader;
    int id;
    private int eventId;
    SubVenueAdapter adapter;
    DaysAdapter daysAdapter;
    String from;
    List<SubVenueDao> selectedSubVenues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_venue_detail_select);
        from = getIntent().getStringExtra("from");
        id = getIntent().getIntExtra("id", -1);
        eventId = getIntent().getIntExtra("eventId", -1);
        loader = new MyLoader(this);
        loader.show("");
        binding.backArrow.setOnClickListener(v -> onBackPressed());
        model = ViewModelProviders.of(this).get(VenueViewModel.class);
        binding.rb.setLayoutManager(new LinearLayoutManager(this));
        binding.rb.setHasFixedSize(true);
        binding.rb.setNestedScrollingEnabled(false);
        adapter = new SubVenueAdapter();
        binding.rb.setAdapter(adapter);
        binding.rbHours.setLayoutManager(new LinearLayoutManager(this));
        binding.rbHours.setHasFixedSize(true);
        binding.rbHours.setNestedScrollingEnabled(false);
        daysAdapter = new DaysAdapter();
        binding.rbHours.setAdapter(daysAdapter);
        fetchVenue();

        boolean isVenuer = Utility.getSharedPreferencesBoolean(this, API.IS_VENUE_OWNER);
        isVenuer=true;
       /* if (isVenuer){
            binding.editVenue.setVisibility(View.VISIBLE);
            binding.delVenue.setVisibility(View.VISIBLE);
        }else {
            binding.editVenue.setVisibility(View.INVISIBLE);
            binding.delVenue.setVisibility(View.INVISIBLE);
        }*/

        if (!"create_venue".equalsIgnoreCase(from))
            binding.btnSelect.setVisibility(View.GONE);

        binding.delVenue.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.really_want_delete))
                    .setTitle(getString(R.string.alert))
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        model.deleteVenue(id).observe(VenueDetail.this, new Observer<MyResponse>() {
                            @Override
                            public void onChanged(MyResponse myResponse) {
                                loader.dismiss();
                                if (myResponse.isSuccess()) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.venue_deleted), Toast.LENGTH_LONG).show();
                                    Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUE_DELETED, true);
                                    finish();
                                } else {
                                    Dialogs.toast(getApplicationContext(), binding.getRoot(), myResponse.getMessage());
                                }
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.editVenue.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, RegisterVanue.class)
                            .putExtra("venue", dao)
                            .putExtra("totalVenues", 1)
                    , 222);
        });

        binding.btnSelect.setOnClickListener(view -> {
            if (dao.getSubVenues() != null && dao.getSubVenues().size() > 0) {
                fetchSubVenue();
            } else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("venue", dao);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                /*Dialogs.NoSubVenueDialog(this, v -> {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("venue", dao);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                });*/
            }
        });
    }

    private void fetchSubVenue() {
        model.getSubVenueDetail(id,Utility.getSharedPreferencesString(this,Constants.startDate24),Utility.getSharedPreferencesString(this,Constants.endDate24)).observe(this, response -> {
            loader.dismiss();
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(VenueDetail.this);
            } else if (!response.isSuccess()) {
                Dialogs.toast(getApplicationContext(), binding.getRoot(), response.getMessage());
            } else {
                Dialogs.availableSubVenueDialog(this, response.getSubVenueDaoList(), v -> {
                    JsonArray jsonArray = new JsonArray();
                    JsonObject object = new JsonObject();
                    for (int i = 0; i < response.getSubVenueDaoList().size(); i++) {
                        if (response.getSubVenueDaoList().get(i).isSelected()) {
                            selectedSubVenues.add(new SubVenueDao(id, response.getSubVenueDaoList().get(i).getSubVenueEventId(), response.getSubVenueDaoList().get(i).getSubVenueCapacity(), response.getSubVenueDaoList().get(i).getId(), "reserve", response.getSubVenueDaoList().get(i).getSubVenueName()));
                            dao.setSelectedSubVenues(selectedSubVenues);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("venueId", id);
                            jsonObject.addProperty("subVenueId", response.getSubVenueDaoList().get(i).getId());
                            if (eventId != -1) {
                                jsonObject.addProperty("eventId", eventId);
                                if (response.getSubVenueDaoList().get(i).getSubVenueEventId() != 0) {
                                    jsonObject.addProperty("id", response.getSubVenueDaoList().get(i).getSubVenueEventId());
                                    jsonObject.addProperty("status", "booked");
                                } else {
                                    jsonObject.addProperty("status", "reserve");
                                }
                            } else {
                                jsonObject.addProperty("status", "reserve");
                            }
                            jsonArray.add(jsonObject);
                        }
                    }
                    object.add("subVenues", jsonArray);
                    object.addProperty("type", "1");
                    object.addProperty("venueId", id);
                    object.addProperty("eventStartDateTime", Utility.getSharedPreferencesString(this,Constants.startDate24));
                    object.addProperty("eventEndDateTime",  Utility.getSharedPreferencesString(this,Constants.endDate24));
                    lockSubVenue(object);
                });
            }
        });
    }

    private void lockSubVenue(JsonObject object) {
        loader.show("");
        model.lockSubVenue(object).observe(this, response -> {
            loader.dismiss();
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(VenueDetail.this);
            } else if (!response.isSuccess()) {
                Dialogs.toast(getApplicationContext(), binding.getRoot(), response.getMessage());
            } else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("venue", dao);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    private void fetchVenue() {
        model.getVenueDetail(id,Utility.getSharedPreferencesString(this,Constants.startDate24),Utility.getSharedPreferencesString(this,Constants.endDate24)).observe(this, response -> {
            loader.dismiss();
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(VenueDetail.this);
            } else if (!response.isSuccess()) {
                Dialogs.toast(getApplicationContext(), binding.getRoot(), response.getMessage());
            } else {
                dao = response.getVenueDAO();
                if (dao != null) {
                    if( dao.getSubVenues()==null && dao.getSubVenues().size()<=0) {
                        if (dao.getIsBooked().equalsIgnoreCase("Yes"))
                            binding.btnSelect.setVisibility(View.INVISIBLE);
                        else
                            binding.btnSelect.setVisibility(View.VISIBLE);
                    }

                    if (Utility.getSharedPreferencesInteger(this, API.USER_ID)==dao.getUserId()){
                        binding.editVenue.setVisibility(View.VISIBLE);
                        binding.delVenue.setVisibility(View.VISIBLE);
                    }else {
                        binding.editVenue.setVisibility(View.INVISIBLE);
                        binding.delVenue.setVisibility(View.INVISIBLE);
                    }

                    binding.nameTv.setText(dao.getName());
                    binding.tvVenueType.setText(TextUtils.isEmpty(dao.getVenueType()) ? "NA" : dao.getVenueType());
                    binding.tvTotalVenueCapacity.setText(String.valueOf(dao.getVenueCapacity()));
                    binding.tvAddress.setText(dao.getVenueAddress());
                    binding.tvShortDesc.setText(TextUtils.isEmpty(dao.getShortDescription()) ? "NA" : dao.getShortDescription());
                    binding.websiteUrlTv.setText(TextUtils.isEmpty(dao.getWebsiteURL()) ? "NA" : dao.getWebsiteURL());
                    binding.imgLayout.removeAllViews();

                    if (dao.getVenueImages() != null && dao.getVenueImages().size() > 0) {
                        for (ImageDAO img : dao.getVenueImages()) {
                            createViewDynamically(img.getVenueImages());
                        }
                        requestManager
                                .load(dao.getVenueImages().get(0).getVenueImages())
                                .into(binding.ivCard);
                    }

                    if (dao.getDaysAvailable().size() > 0) {
                        daysAdapter.refresh(dao.getDaysAvailable());
                        binding.rbHours.setVisibility(View.VISIBLE);
                        binding.tvOperation.setVisibility(View.GONE);
                    }else{
                        binding.rbHours.setVisibility(View.GONE);
                        binding.tvOperation.setVisibility(View.VISIBLE);
                    }

                    if (dao.getSubVenues() != null && dao.getSubVenues().size() > 0) {
                        binding.rb.setVisibility(View.VISIBLE);
                        binding.tvNoSubVenue.setVisibility(View.GONE);
                        adapter.refresh(dao.getSubVenues());
                    } else {
                        binding.rb.setVisibility(View.GONE);
                        binding.tvNoSubVenue.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 222) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("load", false)) {
                    fetchVenue();
                    Log.d("fnalsnfkla", "onActivityResult: load");
                }
            }
        }
    }

    private void createViewDynamically(String path) {
        Log.e("loading paht", path);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = vi.inflate(R.layout.my_dynamic_image, null);
        ImageView img = v.findViewById(R.id.myImg);


        requestManager
                .load(path)
                .into(img);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams((int) getResources().getDimension(R.dimen._72sdp), (int) getResources().getDimension(R.dimen._72sdp));
        binding.imgLayout.addView(v, lp);

        final int pos = binding.imgLayout.indexOfChild(v);
        img.setOnClickListener(view -> requestManager
                .load(dao.getVenueImages().get(pos).getVenueImages())
                .into(binding.ivCard));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }
}
