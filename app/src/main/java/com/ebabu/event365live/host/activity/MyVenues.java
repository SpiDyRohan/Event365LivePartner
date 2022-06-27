package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.PreviousVenueAdapter;
import com.ebabu.event365live.host.adapter.VenueAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityMyVenuesBinding;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.VenueViewModel;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

public class MyVenues extends BaseActivity {

    ActivityMyVenuesBinding binding;
    VenueAdapter adapter;
    PreviousVenueAdapter previousVenueAdapter;
    VenueViewModel venueViewModel;
    MyLoader loader;
    String from;
    private int totalVenues;
    private int eventId;
    boolean isVenuer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_venues);
        venueViewModel = ViewModelProviders.of(this).get(VenueViewModel.class);
        loader = new MyLoader(this);
        loader.show("");
        from = getIntent().getStringExtra("from");
        eventId = getIntent().getIntExtra("eventId", -1);
        isVenuer = Utility.getSharedPreferencesBoolean(this, API.IS_VENUE_OWNER);
        isVenuer=true;
        if (isVenuer) {
            binding.addVanue.setVisibility(View.VISIBLE);
            binding.textView20.setText("My Venues");
            binding.tvMyVenues.setText("My Venues");
        } else {
            binding.addVanue.setVisibility(View.GONE);
            binding.textView20.setText("Venues");
            binding.tvMyVenues.setText("Venues");
        }

        fetchVenues();

        binding.rb.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rb.setHasFixedSize(true);
        binding.rb.setNestedScrollingEnabled(false);
        adapter = new VenueAdapter(venueDAO -> {
            if ("create_venue".equalsIgnoreCase(from)) {
                startActivityForResult(new Intent(this, VenueDetail.class).putExtra("id", venueDAO.getId()).putExtra("from", "create_venue").putExtra("eventId", eventId), 101);
            } else {
                startActivity(new Intent(MyVenues.this, VenueDetail.class)
                        .putExtra("id", venueDAO.getId())
                );
            }
        });
        binding.rb.setAdapter(adapter);


        binding.rbPrev.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rbPrev.setHasFixedSize(true);
        binding.rbPrev.setNestedScrollingEnabled(false);
        previousVenueAdapter = new PreviousVenueAdapter(venueDAO -> {
            if ("create_venue".equalsIgnoreCase(from)) {
                startActivityForResult(new Intent(this, VenueDetail.class).putExtra("id", venueDAO.getId()).putExtra("from", "create_venue"), 101);
            } else {
                startActivity(new Intent(MyVenues.this, VenueDetail.class)
                        .putExtra("id", venueDAO.getId())
                );
            }
        });
        binding.rbPrev.setAdapter(previousVenueAdapter);

        binding.backArrow.setOnClickListener(v -> onBackPressed());
        binding.addVanue.setOnClickListener(v -> startActivityForResult(new Intent(this, RegisterVanue.class)
                        .putExtra(Constants.FROM, API.GET_VENEUS)
                        .putExtra("totalVenues", totalVenues)
                , 222));

//        binding.llSearch.setOnClickListener(view -> {
//            startActivity(new Intent(this, SearchVenueActivity.class));
//        });

        binding.inputTxtDebounce.setOnClickListener(view -> {
            if ("create_venue".equalsIgnoreCase(from)) {
                startActivityForResult(new Intent(MyVenues.this, SearchVenueActivity.class).putExtra("from", "create_venue"), 102);
            } else {
                startActivity(new Intent(MyVenues.this, SearchVenueActivity.class));
            }
        });
    }

    private void fetchVenues() {
        Log.d("nfklasnfla", "fetchVenues: ");
        venueViewModel.getMyVenues(isVenuer,from).observe(this, response -> {
            loader.dismiss();
            Log.e("code", response.getCode() + "");
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(MyVenues.this);
            } else if (response.isSuccess() && (response.getMyVenueList() == null || response.getMyVenueList().size() == 0) && (response.getPreviouslyUsedVenueList() == null || response.getPreviouslyUsedVenueList().size() == 0)) {
                response.setCode(0);
                response.setSuccess(false);
                adapter.refresh(response.getMyVenueList());
                binding.tvPreviouslyUsed.setVisibility(View.GONE);
                binding.tvMyVenues.setVisibility(View.GONE);
                binding.rb.setVisibility(View.GONE);
                binding.rbPrev.setVisibility(View.GONE);
                previousVenueAdapter.refresh(response.getPreviouslyUsedVenueList());
                Log.e("*********", "fired");
                binding.noDataLayout.setVisibility(View.VISIBLE);
            } else {
                if (response.getMyVenueList() != null) {
                    totalVenues = response.getMyVenueList().size();
                    adapter.refresh(response.getMyVenueList());
                    if (response.getPreviouslyUsedVenueList() != null && response.getPreviouslyUsedVenueList().size() > 0) {
                        binding.tvPreviouslyUsed.setVisibility(View.VISIBLE);
                        binding.rbPrev.setVisibility(View.VISIBLE);
                        previousVenueAdapter.refresh(response.getPreviouslyUsedVenueList());
                    }
                    binding.noDataLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 222) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra("load", false)) {
                    venueViewModel.getAllVenues(true, isVenuer,from);
                }
            }
        } else if (requestCode == 101 || requestCode == 102) {
            if (resultCode == RESULT_OK) {
                VenueDAO selectedVenue = (VenueDAO) data.getParcelableExtra("venue");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("venue", selectedVenue);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.getSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUE_DELETED)) {
            Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUE_DELETED, false);
            venueViewModel.getAllVenues(true, isVenuer,from);
        }
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
