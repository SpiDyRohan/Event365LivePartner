package com.ebabu.event365live.host.fragments.rsvp_invites;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.InviteRsvpAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentAllContactSBinding;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class AllContactSFragment extends Fragment {
    private FragmentAllContactSBinding binding;
    private Context context;
    @Inject
    ApiInterface apiInterface;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 606;
    private static final int PLACE_PICKER_REQUEST_CODE= 607;
    private boolean mLocationPermissionGranted;
    private InviteRsvpAdapter rsvpAdapter;
    private int limit=30,page=1;
    String city="",country="",name="",choose=null;
    private LatLng latLongt;
    MyLoader loader;
    private boolean wasNetworkError,isFetching;
    boolean loadAgain=true,fire=false;
    List<UserDAO> list=new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    int eventID;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_contact_s, container, false);
        App.getAppComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventID = getArguments().getInt("id", -1);
        loader=new MyLoader(context);

        linearLayoutManager=new LinearLayoutManager(context);
        binding.rv.setLayoutManager(linearLayoutManager);


        setupInviteContactList();

       // binding.filterImg.setOnClickListener(this::showMenu);

        getLocationPermission();
        Places.initialize(context.getApplicationContext(), getString(R.string.google_maps_key));

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isFetching) {
                    loadAgain=true;
                    page=1;
                    list.clear();
                    rsvpAdapter.notifyDataSetChanged();
                    fetchCustomers();
                }
            }
        });

        binding.inviteBtn.setOnClickListener(v->{
            boolean selected=false;
            JsonArray jsonArray = new JsonArray();

            for(UserDAO userDAO:list)
                if(userDAO.isChecked())
                    jsonArray.add(userDAO.getId());

            if(jsonArray.size()>0) {
                JsonObject jsonObject=new JsonObject();
                jsonObject.add("id",jsonArray);

                loader.show("");

                apiInterface.inviteRSVP(eventID,jsonObject).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        loader.dismiss();
                        if(response.isSuccessful()) {

                            try {
                                JSONObject obj=new JSONObject(response.body().toString());

                                Dialogs.showActionDialog(context,
                                        getString(R.string.app_name),
                                        obj.getString(API.MESSAGE),
                                        "Done",
                                        v1-> getActivity().finish()
                                );

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            if(response.code()== API.SESSION_EXPIRE)
                                Utility.sessionExpired(context);

                            else {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                    Dialogs.toast(context,v, jsonObject.getString(API.MESSAGE));
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

            }else {
                Toast.makeText(context,"Select atleast one contact please!",Toast.LENGTH_LONG).show();
            }
        });


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(!isFetching && loadAgain) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        fetchCustomers();

                }
            }
        });

        /*binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(!isFetching) {
                    Log.e("***************","fired");
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == list.size() -1){
                        fetchCustomers();
                    }
                }

            }
        });*/

        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    //if( binding.searchEt.getText().toString().trim().length()>0) {
                    if(!name.equals(v.getText().toString())){
                        name = v.getText().toString();
                        list.clear();
                        page=1;
                        fetchCustomers();
                    }
                    else
                        Utility.hideKeyboard(getActivity());
                //}
                return false;
            }
        });

        fetchCustomers();

    }

    private void fetchCustomers() {

        if (!Utility.isNetworkAvailable(context)) {
            if(binding.swipe.isRefreshing())binding.swipe.setRefreshing(false);
            wasNetworkError=true;
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.textView48.setText(getString(R.string.check_network));
            binding.rv.setVisibility(View.GONE);
            Toast.makeText(context,getString(R.string.check_network),Toast.LENGTH_LONG).show();
            return;
        }


        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("countryCode",country);
        jsonObject.addProperty("city",city);
        jsonObject.addProperty("name",name);


        if(!binding.swipe.isRefreshing())
            loader.show("");
        isFetching=true;
        Call<JsonElement> call= apiInterface.fetchCustomers(
                limit,
                page,
                jsonObject
        );
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
                        JSONArray dataArray= OBJ.getJSONObject("data").getJSONArray("users");
                        if(dataArray.length()>0)
                            for(int i=0;i<dataArray.length();i++) {
                                String s = dataArray.getJSONObject(i).toString();
                                list.add(new Gson().fromJson(s, UserDAO.class));
                            }
                        else
                            loadAgain=false;

                        page++;
                        if(!fire)fire=true;

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context.getApplicationContext(),getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
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
                if(list.size()==0)binding.noDataLayout.setVisibility(View.VISIBLE);
                else
                    binding.noDataLayout.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void chooseAddress(TypeFilter typeFilter){
        if(mLocationPermissionGranted){

            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.PLUS_CODE,
                    Place.Field.LAT_LNG
            );
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY,
                    fields
            )
                    .setTypeFilter(typeFilter)
                    .build(context);
            startActivityForResult(intent, PLACE_PICKER_REQUEST_CODE);

        }
        else
            getLocationPermission();
    }

    private void setupInviteContactList(){
        rsvpAdapter = new InviteRsvpAdapter();
        binding.rv.setAdapter(rsvpAdapter);
    }

    private void getLocationPermission() {

        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Place place = Autocomplete.getPlaceFromIntent(data);
                if(choose.equals("country")) {
                    latLongt = place.getLatLng();

                   /* String code = Utility.getCountryCode(getContext(),latLongt.latitude, latLongt.longitude);
                    binding.cpp.setCountryForNameCode(code);
                    if(!country.equals(binding.cpp.getSelectedCountryCodeWithPlus())){
                        country=binding.cpp.getSelectedCountryCodeWithPlus();
                        list.clear();*/
                        fetchCustomers();
                }
                else{
                    if(!city.equals(place.getName())){
                        city=place.getName();
                        list.clear();
                        fetchCustomers();
                    }
                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("WIG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }


}