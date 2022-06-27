package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.ebabu.event365live.host.MainActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.SearchVenueAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivitySearchVenueBinding;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.SearchVenueViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class SearchVenueActivity extends MainActivity {

    ActivitySearchVenueBinding binding;
    SearchVenueAdapter adapter;
    MyLoader loader;
    private static LatLng getCurrentLatLng;
    SearchVenueViewModel venueViewModel;
    private String search = "";
    int miles = 5;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_venue);
        venueViewModel = ViewModelProviders.of(this).get(SearchVenueViewModel.class);
        loader = new MyLoader(this);
        binding.backArrow.setOnClickListener(v -> onBackPressed());
        getLocation();
        from = getIntent().getStringExtra("from");
        binding.rb.setLayoutManager(new LinearLayoutManager(this));
        binding.rb.setHasFixedSize(true);
        binding.rb.setNestedScrollingEnabled(false);
        adapter = new SearchVenueAdapter(venueDAO -> {
            if ("create_venue".equalsIgnoreCase(from)) {
                startActivityForResult(new Intent(this, VenueDetail.class).putExtra("id", venueDAO.getId()).putExtra("from", "create_venue"), 101);
            } else {
                startActivity(new Intent(SearchVenueActivity.this, VenueDetail.class)
                        .putExtra("id", venueDAO.getId())
                );
            }
        });
        binding.rb.setAdapter(adapter);

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() != 0) {
                    search = editable.toString();
                    fetchVenues();
                }
            }
        });

        binding.seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                binding.tvShowDistance.setText(progress + " Miles");
                miles = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.btnApply.setOnClickListener(view -> {
            fetchVenues();
        });
    }

    private void getLocation() {
        getCurrentLocationInstance(currentLatLng -> {
            if (currentLatLng != null) {
                loader.dismiss();
                getCurrentLatLng = currentLatLng;
                Utility.setLocation(SearchVenueActivity.this,binding.tvShowCurrentLocation,getCurrentLatLng.latitude, getCurrentLatLng.longitude);
                fetchVenues();
            }
        });

        binding.tvShowCurrentLocation.setOnClickListener(view -> Utility.launchSelectAddressFrag(SearchVenueActivity.this));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("fnaslkfnasl", "onActivityResult: ");
        if (requestCode == Constants.AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Log.d("fnaslkfnasl", "RESULT_OK: ");
                Place place = Autocomplete.getPlaceFromIntent(data);
                getCurrentLatLng = place.getLatLng();
                if (getCurrentLatLng != null) {
                    Utility.saveCurrentLocation(this, getCurrentLatLng.latitude, getCurrentLatLng.longitude);
                    Utility.setLocation(SearchVenueActivity.this,binding.tvShowCurrentLocation,getCurrentLatLng.latitude, getCurrentLatLng.longitude);
                    fetchVenues();
                }
            }
        } else if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                VenueDAO selectedVenue = (VenueDAO) data.getParcelableExtra("venue");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("venue", selectedVenue);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    private void fetchVenues() {
        Log.d("nfklasnfla", "fetchVenues: ");
        loader.show("Please wait...");
        venueViewModel.getFilterVenues(String.valueOf(getCurrentLatLng.latitude), String.valueOf(getCurrentLatLng.longitude), search, miles).observe(this, response -> {
            loader.dismiss();
            Log.e("code", response.getCode() + "");
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(SearchVenueActivity.this);
            } else if (response.isSuccess() && (response.getFilterVenueList() == null || response.getFilterVenueList().size() == 0)) {
                response.setCode(0);
                response.setSuccess(false);
                adapter.refresh(response.getFilterVenueList());
                Log.e("*********", "fired");
                binding.noDataLayout.setVisibility(View.VISIBLE);
            } else if (response.getFilterVenueList() != null) {
                adapter.refresh(response.getFilterVenueList());
                binding.noDataLayout.setVisibility(View.GONE);
            } else {
                response.setCode(0);
                response.setSuccess(false);
                adapter.refresh(response.getFilterVenueList());
                Log.e("*********", "fired");
                binding.noDataLayout.setVisibility(View.VISIBLE);
            }
        });
    }
}