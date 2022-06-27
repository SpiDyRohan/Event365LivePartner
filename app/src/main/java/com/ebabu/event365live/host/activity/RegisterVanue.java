package com.ebabu.event365live.host.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.GalleryPicAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityRegisterVanueBinding;
import com.ebabu.event365live.host.entities.ContactEnum;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.MDaysAvailable;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.PathUtil;
import com.ebabu.event365live.host.utils.PermissionUtils;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonElement;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterVanue extends BaseActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 606;
    private static final int PLACE_PICKER_REQUEST_CODE = 607;
    private static final int RC_HANDLE_CAMERA_PERM = 101;
    HashSet<String> days = new HashSet<>();
    HashSet<MDaysAvailable> daysAvailables = new HashSet<>();
    HashSet<String> contactVia = new HashSet<>();

    ArrayList<ImageDAO> imageList = new ArrayList<>();

    List<ImageDAO> previousImages;

    ActivityRegisterVanueBinding binding;
    private String currentPhotoPath;
    private Bitmap rotatedBitmap;

    private boolean mLocationPermissionGranted;
    boolean canAllow = true;

    int totalVenues;

    LatLng latLongt;

    String deletedIds = "";

    @Inject
    ApiInterface apiInterface;

    @Inject
    RequestManager requestManager;

    MyLoader loader;

    VenueDAO dao;
    private PermissionUtils permissionUtils;
    private String cityName="",stateName="",stateNameShort="",countryName="";
    private GalleryPicAdapter galleryPicAdapter;
    private View view;
    private LayoutInflater layoutInflater;

    //newly added
    private CompositeDisposable disposable;
    private boolean isShortDescValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_vanue);
        App.getAppComponent().inject(this);
        layoutInflater = getLayoutInflater();
        loader = new MyLoader(this);
        setSpinner();
        totalVenues = getIntent().getIntExtra("totalVenues", 0);
        setGalleryAdapter();

        binding.cbSubVenue.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.tvAddMoreSubVenue.setVisibility(View.VISIBLE);
                if (dao == null || dao.getSubVenues() == null || dao.getSubVenues().size() == 0) {
                    getSubVenueView();
                }
            } else {
                binding.tvAddMoreSubVenue.setVisibility(View.GONE);
                if (binding.subVenueParentLayout.getChildCount() > 0) {
                    binding.subVenueParentLayout.removeAllViews();
                }
            }
        });

        binding.tvAddMoreSubVenue.setOnClickListener(view1 -> getSubVenueView());

        binding.ivSubVenueWarning.setOnClickListener(view1 -> {
            if (binding.llSubVenueOptional.getVisibility() == View.VISIBLE) {
                binding.llSubVenueOptional.setVisibility(View.GONE);
            } else {
                binding.llSubVenueOptional.setVisibility(View.VISIBLE);
            }
        });

        previousImages = new ArrayList<>();
        if (totalVenues != 0) {
            binding.contactChipGroup.setVisibility(View.GONE);
            binding.view6.setVisibility(View.GONE);
            binding.view5.setVisibility(View.GONE);
            binding.switchMaterial.setVisibility(View.GONE);
            binding.textView23.setVisibility(View.GONE);
            binding.textView22.setVisibility(View.GONE);
            canAllow = false;
        }
        dao = (VenueDAO) getIntent().getParcelableExtra("venue");
        if (dao != null) {
            binding.venueNameEt.setText(dao.getName());
            binding.websiteUrlEt.setText(dao.getWebsiteURL());
            binding.shortDescEt.setText(dao.getShortDescription());
            binding.venueCapacityEt.setText("" + dao.getVenueCapacity());
            binding.textView20.setText("Update Venue");
            binding.btnTv.setText("Update");
            /*if (dao.getVenueType().equals("Indoor Venue")) {
                binding.indoorChip.setChecked(true);
            } else {
                binding.outdoorChip.setChecked(true);
            }*/

            if (dao.getVenueType().equalsIgnoreCase("both")) {
                binding.indoorChip.setChecked(true);
                binding.outdoorChip.setChecked(true);
            } else if (dao.getVenueType().equalsIgnoreCase("Indoor Venue"))
                binding.indoorChip.setChecked(true);
            else if (dao.getVenueType().equalsIgnoreCase("Outdoor Venue"))
                binding.outdoorChip.setChecked(true);


            binding.switchVenueAvailable.setChecked(dao.isVenueAvailableToOtherHosts());

            binding.addrsEt.setText(dao.getVenueAddress());
            latLongt = new LatLng(Double.valueOf(dao.getLatitude()), Double.valueOf(dao.getLongitude()));

            if (dao.getSubVenues() != null && dao.getSubVenues().size() > 0) {
                binding.cbSubVenue.setChecked(true);
                for (int i = 0; i < dao.getSubVenues().size(); i++) {
                    view = layoutInflater.inflate(R.layout.sub_venue_layout, binding.subVenueParentLayout, false);
                    TextView textView = (TextView) view.findViewById(R.id.tvSubVenue);
                    EditText subVenueName = (EditText) view.findViewById(R.id.etSubVenueName);
                    EditText subVenueCapacity = (EditText) view.findViewById(R.id.etSubVenueCapacity);
                    ImageView ivDelete = (ImageView) view.findViewById(R.id.ivDelete);
                    textView.setText("Sub - Venue " + (i + 1));
                    textView.setTag(dao.getSubVenues().get(i).getId());
                    subVenueName.setText(dao.getSubVenues().get(i).getSubVenueName());
                    subVenueCapacity.setText("" + dao.getSubVenues().get(i).getSubVenueCapacity());
                    ivDelete.setOnClickListener(view -> {
                        LinearLayout linearParent = (LinearLayout) view.getParent().getParent();
                        binding.subVenueParentLayout.removeView(linearParent);
                        if (binding.subVenueParentLayout.getChildCount() == 0) {
                            binding.cbSubVenue.setChecked(false);
                            binding.tvAddMoreSubVenue.setVisibility(View.GONE);
                        }
                    });
                    binding.subVenueParentLayout.addView(view, i);
                }
            }

            if (dao.getDaysAvailable().size() > 0) {
                try {
                    for (int i = 0; i < dao.getDaysAvailable().size(); i++) {
                        MDaysAvailable available = dao.getDaysAvailable().get(i);
                        int day = available.getWeekDayName();
                        switch (day) {
                            case 1:
                                binding.chipMonday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner1, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner2, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipMonday);
                                break;
                            case 2:
                                binding.chipTuesday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner3, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner4, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipTuesday);
                                break;
                            case 3:
                                binding.chipWednesday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner5, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner6, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipWednesday);
                                break;
                            case 4:
                                binding.chipThursday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner7, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner8, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipThursday);
                                break;
                            case 5:
                                binding.chipFriday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner9, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner10, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipFriday);
                                break;
                            case 6:
                                binding.chipSaturday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner11, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner12, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipSaturday);
                                break;
                            case 7:
                                binding.chipSunday.setChecked(true);
                                selectSpinnerItemByValue(binding.spinner13, dao.getDaysAvailable().get(i).getFromTime());
                                selectSpinnerItemByValue(binding.spinner14, dao.getDaysAvailable().get(i).getToTime());
                                setAvailability(binding.chipSunday);
                                break;
                        }
                    }

                    Log.e("days", days.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            previousImages = dao.getVenueImages();

            galleryPicAdapter.refresh(previousImages);
        }

        if (API.GET_VENEUS.equals(getIntent().getStringExtra(Constants.FROM)))
            binding.textView20.setText(getString(R.string.add_new_venue));


        getLocationPermission();
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        binding.addImage.setOnClickListener(v -> checkForPermission());

        binding.backArrow.setOnClickListener(v -> onBackPressed());


        binding.addrsEt.setOnClickListener(v -> {
            if (mLocationPermissionGranted) {

                List<Place.Field> fields = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.LAT_LNG
                );
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .build(this);
                startActivityForResult(intent, PLACE_PICKER_REQUEST_CODE);

            } else
                getLocationPermission();
        });

        binding.createAccBtn.setOnClickListener(v -> {
            String venueName = binding.venueNameEt.getText().toString().trim();
            String address = binding.addrsEt.getText().toString().trim();
            String webSite = binding.websiteUrlEt.getText().toString().trim();
            String shortDesc = binding.shortDescEt.getText().toString().trim();
            String capacity = binding.venueCapacityEt.getText().toString().trim();
            String venueType = "";
            boolean isVenueAvailableToOtherHosts = binding.switchVenueAvailable.isChecked();
            /*int id = binding.venueTypeChipGroup.getCheckedChipId();
            switch (id) {
                case R.id.indoor_chip:
                    venueType = "Indoor Venue";
                    break;
                case R.id.outdoor_chip:
                    venueType = "Outdoor Venue";
                    break;
            }*/

            if(binding.indoorChip.isChecked() && binding.outdoorChip.isChecked())
                venueType="both";
            else if(binding.indoorChip.isChecked())
                venueType = "Indoor Venue";
            else if(binding.outdoorChip.isChecked())
                venueType = "Outdoor Venue";


            if (TextUtils.isEmpty(venueName)) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.enter_venue_name));
                binding.venueNameEt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(address)) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.addrs_error));
                binding.addrsEt.requestFocus();
                return;
            }

           /* if (TextUtils.isEmpty(shortDesc)) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.short_desc_error));
                binding.shortDescEt.requestFocus();
                return;
            }

            if (shortDesc.length() < 200) {
                binding.shortDescEt.requestFocus();
                Dialogs.toast(getApplicationContext(), v, getString(R.string.shortDescMin));
                return;
            }*/

            if (shortDesc.length() > 2500) {
                binding.shortDescEt.requestFocus();
                Dialogs.toast(getApplicationContext(), v, getString(R.string.shortDescMax));
                return ;
            }

            //newly added
            disposable = new CompositeDisposable();

            //code we require
            disposable.add(
                    RxTextView.textChanges(binding.shortDescEt)
                            .subscribe(charSequence -> {
                                if (charSequence.length() >= 1) {
                                    if (!isShortDescValid) {
                                        isShortDescValid= true;
                                        binding.shortDescEtImg.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    if (isShortDescValid) {
                                        isShortDescValid = false;
                                        binding.shortDescEtImg.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }));


            /*if (days.size() == 0) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.availability_error));
                return;
            }*/

            if (binding.chipMonday.isChecked()) {
                String startTime = binding.spinner1.getSelectedItem().toString();
                String endTime = binding.spinner2.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.MONDAY, binding.spinner1.getSelectedItem().toString().trim(), binding.spinner2.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.MONDAY, binding.spinner1.getSelectedItem().toString().trim(), binding.spinner2.getSelectedItem().toString().trim()));
            }

            if (binding.chipTuesday.isChecked()) {
                String startTime = binding.spinner3.getSelectedItem().toString();
                String endTime = binding.spinner4.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.TUESDAY, binding.spinner3.getSelectedItem().toString().trim(), binding.spinner4.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.TUESDAY, binding.spinner3.getSelectedItem().toString().trim(), binding.spinner4.getSelectedItem().toString().trim()));
            }


            if (binding.chipWednesday.isChecked()) {
                String startTime = binding.spinner5.getSelectedItem().toString();
                String endTime = binding.spinner6.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.WEDNESDAY, binding.spinner5.getSelectedItem().toString().trim(), binding.spinner6.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.WEDNESDAY, binding.spinner5.getSelectedItem().toString().trim(), binding.spinner6.getSelectedItem().toString().trim()));
            }


            if (binding.chipThursday.isChecked()) {
                String startTime = binding.spinner7.getSelectedItem().toString();
                String endTime = binding.spinner8.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.THURSDAY, binding.spinner7.getSelectedItem().toString().trim(), binding.spinner8.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.THURSDAY, binding.spinner7.getSelectedItem().toString().trim(), binding.spinner8.getSelectedItem().toString().trim()));
            }

            if (binding.chipFriday.isChecked()) {
                String startTime = binding.spinner9.getSelectedItem().toString();
                String endTime = binding.spinner10.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }

                daysAvailables.add(new MDaysAvailable(Constants.FRIDAY, binding.spinner9.getSelectedItem().toString().trim(), binding.spinner10.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.FRIDAY, binding.spinner9.getSelectedItem().toString().trim(), binding.spinner10.getSelectedItem().toString().trim()));
            }

            if (binding.chipSaturday.isChecked()) {
                String startTime = binding.spinner11.getSelectedItem().toString();
                String endTime = binding.spinner12.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.SATURDAY, binding.spinner11.getSelectedItem().toString().trim(), binding.spinner12.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.SATURDAY, binding.spinner11.getSelectedItem().toString().trim(), binding.spinner12.getSelectedItem().toString().trim()));
            }

            if (binding.chipSunday.isChecked()) {
                String startTime = binding.spinner13.getSelectedItem().toString();
                String endTime = binding.spinner14.getSelectedItem().toString();
                int startHour = Integer.valueOf(startTime.substring(0, 2));
                int endHour = Integer.valueOf(endTime.substring(0, 2));

                if (endHour == startHour || endHour < startHour) {
                    Dialogs.toast(getApplicationContext(), v, "Start time must be less than End time");
                    return;
                }
                daysAvailables.add(new MDaysAvailable(Constants.SUNDAY, binding.spinner13.getSelectedItem().toString().trim(), binding.spinner14.getSelectedItem().toString().trim()));
            } else {
                daysAvailables.remove(new MDaysAvailable(Constants.SUNDAY, binding.spinner13.getSelectedItem().toString().trim(), binding.spinner14.getSelectedItem().toString().trim()));
            }

           /* if (TextUtils.isEmpty(capacity)) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.venue_capacity_error));
                binding.venueCapacityEt.requestFocus();
                return;
            }

            if (Integer.parseInt(capacity) <= 0) {
                Dialogs.toast(getApplicationContext(), v, getString(R.string.venue_capacity_zero_error));
                binding.venueCapacityEt.requestFocus();
                return;
            }*/

            if (binding.cbSubVenue.isChecked() && binding.subVenueParentLayout.getChildCount() != 0) {
                if (!validateSubVenue()) return;
                //if (!validateSubVenue(view, true)) return;
            }

           /* if (binding.cbSubVenue.isChecked() && binding.subVenueParentLayout.getChildCount() != 0) {
                int totalSubVenueCapacity = 0;
                for (int i = 0; i < binding.subVenueParentLayout.getChildCount(); i++) {
                    View view = binding.subVenueParentLayout.getChildAt(i);
                    String subVenueCapacity = ((EditText) view.findViewById(R.id.etSubVenueCapacity)).getText().toString().trim();
                    totalSubVenueCapacity = totalSubVenueCapacity + Integer.parseInt(subVenueCapacity);
                }
                if (Integer.parseInt(capacity) < totalSubVenueCapacity) {
                    Dialogs.toast(getApplicationContext(), v, getString(R.string.sub_venue_capacity_error));
                    return;
                }
            }*/


            if (canAllow && contactVia.size() == 0) {
                Log.e("canAllow", canAllow + "");
                Dialogs.toast(getApplicationContext(), v, getString(R.string.choose_contact_via));
                return;
            }

           /* if (imageList.size() == 0 && previousImages.size() == 0) {
                Dialogs.toast(getApplicationContext(), v, "Please upload picture of Venue!");
                return;
            }*/

            JSONArray daysJsonArray = new JSONArray();


            for (MDaysAvailable mDaysAvailable : daysAvailables) {
                JSONObject daysJsonObject = new JSONObject();
                try {
                    daysJsonObject.put("weekDayName", StringUtils.getDayMonthNum(mDaysAvailable.getWeekDay()));
                    daysJsonObject.put("fromTime", mDaysAvailable.getFromTime());
                    daysJsonObject.put("toTime", mDaysAvailable.getToTime());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                daysJsonArray.put(daysJsonObject);
            }


            JSONArray subVenueArray = null;
            subVenueArray = new JSONArray();
            for (int i = 0; i < binding.subVenueParentLayout.getChildCount(); i++) {
                View view = binding.subVenueParentLayout.getChildAt(i);
                String subVenueName = ((EditText) view.findViewById(R.id.etSubVenueName)).getText().toString().trim();
                String subVenueCapacity = ((EditText) view.findViewById(R.id.etSubVenueCapacity)).getText().toString().trim();
                TextView textView = (TextView) view.findViewById(R.id.tvSubVenue);
                JSONObject subVenueJsonObject = new JSONObject();
                try {
                    if (textView.getTag() != null) {
                        int subVenueId = (int) textView.getTag();
                        subVenueJsonObject.put("id", subVenueId);
                    }
                    subVenueJsonObject.put("subVenueName", subVenueName);
                    subVenueJsonObject.put("subVenueCapacity", subVenueCapacity);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                subVenueArray.put(subVenueJsonObject);
            }

            JSONArray contactJsonArray = new JSONArray();
            for (String s : contactVia) contactJsonArray.put(s);
            RequestBody venue = RequestBody.create(MediaType.parse("multipart/form-data"), venueName);
            RequestBody shortDescVenue = RequestBody.create(MediaType.parse("multipart/form-data"), shortDesc);
            RequestBody lattitude = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(latLongt.latitude));
            RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(latLongt.longitude));
            RequestBody addrs = RequestBody.create(MediaType.parse("multipart/form-data"), address);
            RequestBody websiteName = RequestBody.create(MediaType.parse("multipart/form-data"), webSite);
            RequestBody daysBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), daysJsonArray.toString());
            RequestBody contactViaBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), contactJsonArray.toString());
            RequestBody deletedImagesBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), deletedIds);
            RequestBody subVenueRequestBody = null;
//            if (subVenueArray.length() > 0) {
            subVenueRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), subVenueArray.toString());
//            }
            RequestBody venueCapacity = RequestBody.create(MediaType.parse("multipart/form-data"), capacity.trim().length()>0 ? capacity : "0");
            RequestBody venueTypeRb = RequestBody.create(MediaType.parse("multipart/form-data"), venueType);
            RequestBody isVenueAvailableToOtherHostsRb = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(isVenueAvailableToOtherHosts));
            RequestBody countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), binding.cpp.getSelectedCountryCodeWithPlus());

            RequestBody cityBodyPart = null;
            if (!TextUtils.isEmpty(cityName))
                cityBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), cityName);

            RequestBody stateBodyPart = null;
            if (!TextUtils.isEmpty(stateName))
                stateBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), stateName);

            RequestBody stateShortBodyPart = null;
            if (!TextUtils.isEmpty(stateNameShort))
                stateShortBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), stateNameShort);

            RequestBody countryBodyPart = null;
            if (!TextUtils.isEmpty(countryName))
                countryBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), countryName);

            // TODO: 1/10/19 PREPARE IMAGE ARRAY
            MultipartBody.Part[] image = null;
            if (imageList.size() > 0) {
                image = new MultipartBody.Part[imageList.size()];
                try {
                    for (int index = 0; index < imageList.size(); index++) {
                        File file = new File(imageList.get(index).getVenueImages());
                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part fileToUploadPart = MultipartBody.Part.createFormData("venueImages", file.getName(), requestFile);
                        image[index] = fileToUploadPart;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Call<JsonElement> call;
            if (dao != null) {
                call = apiInterface.updateVenue(
                        dao.getId(),
                        venue, addrs, daysBodyPart, null, lattitude, longitude, websiteName, countryCode, cityBodyPart,stateShortBodyPart,stateBodyPart,countryBodyPart, shortDescVenue, subVenueRequestBody, venueCapacity, venueTypeRb, isVenueAvailableToOtherHostsRb, deletedImagesBodyPart, image);
            } else {
                call = apiInterface.addVenue(
                        venue, addrs, daysBodyPart, contactViaBodyPart, lattitude, longitude, websiteName, countryCode, cityBodyPart,stateShortBodyPart,stateBodyPart,countryBodyPart,shortDescVenue, subVenueRequestBody, venueCapacity, venueTypeRb, isVenueAvailableToOtherHostsRb, image);
            }
            loader.show("");
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    loader.dismiss();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().toString());
                            Toast.makeText(RegisterVanue.this, jsonObject.getString(API.MESSAGE), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (dao != null) {
                            //user is updated, saved to true so that MyVenues screen automatically updated
                            Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUE_DELETED, true);
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra("load", true);
                            setResult(Activity.RESULT_OK, returnIntent);
                        } else {
                            if (!API.GET_VENEUS.equals(getIntent().getStringExtra(Constants.FROM))) {
                                Intent intent = new Intent(RegisterVanue.this, Home.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("load", true);
                                setResult(Activity.RESULT_OK, returnIntent);
                            }
                        }
                        finish();
                    } else if (response.code() == 406) {
                        try {
                            JSONObject errorObj = null;
                            errorObj = new JSONObject(response.errorBody().string());
                            String msg = errorObj.getString("message");
                            Dialogs.toast(getApplicationContext(), binding.getRoot(), msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() == API.SESSION_EXPIRE) {
                        Utility.sessionExpired(RegisterVanue.this);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<JsonElement> call, Throwable t) {
                    t.printStackTrace();
                    loader.dismiss();
                    Dialogs.toast(getApplicationContext(), binding.getRoot(), "Something went wrong");
                }
            });
        });

        binding.switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.contactChipGroup.setVisibility(View.VISIBLE);
                binding.textView23.setVisibility(View.VISIBLE);
                binding.view6.setVisibility(View.VISIBLE);
            } else {
                binding.contactChipGroup.setVisibility(View.GONE);
                binding.view6.setVisibility(View.GONE);
                binding.textView23.setVisibility(View.GONE);
            }
            canAllow = b;
        });
    }


    private void getLocationPermission() {

        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    private void checkForPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        permissionUtils = new PermissionUtils(this)
                .setCode(RC_HANDLE_CAMERA_PERM)
                .setPermissions(permissions)
                .onAlreadyGranted(this::choose)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_CAMERA_PERM: {
                permissionUtils.handle(requestCode, grantResults);
                break;
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void choose() {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.ebabu.event365live.host.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePictureIntent}
                );
        startActivityForResult(chooserIntent, 222);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 222:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        try {
                            setPicCaptureByGallery(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        setPicCapturedByCamera();
                    }
                }
                break;

            case PLACE_PICKER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

                    cityName="";stateName="";stateNameShort="";countryName="";
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    binding.addrsEt.setText(place.getAddress());
                    latLongt = place.getLatLng();
                    String code = Utility.getCountryCode(RegisterVanue.this, latLongt.latitude, latLongt.longitude);
                    binding.cpp.setCountryForNameCode(code);

                    List<AddressComponent> adCompo=place.getAddressComponents().asList();
                    if(adCompo!=null && adCompo.size()>0){
                        for(int i=0;i<adCompo.size();i++){
                            if(adCompo.get(i).getTypes()!=null && adCompo.get(i).getTypes().size()>0){
                                if(adCompo.get(i).getTypes().toString().trim().contains("locality"))
                                    cityName=adCompo.get(i).getName();

                                if(adCompo.get(i).getTypes().toString().trim().contains("administrative_area_level_1"))
                                    stateName=adCompo.get(i).getName();

                                if(adCompo.get(i).getTypes().toString().trim().contains("administrative_area_level_1"))
                                    stateNameShort=adCompo.get(i).getShortName();

                                if(adCompo.get(i).getTypes().toString().trim().contains("country"))
                                    countryName=adCompo.get(i).getName();
                            }
                        }
                    }

                    if(TextUtils.isEmpty(cityName))
                        cityName = Utility.getCity(RegisterVanue.this, latLongt.latitude, latLongt.longitude);
                    if(TextUtils.isEmpty(stateName))
                        stateName = Utility.getState(RegisterVanue.this, latLongt.latitude, latLongt.longitude);
                    if(TextUtils.isEmpty(stateNameShort))
                        stateNameShort = Utility.getState(RegisterVanue.this, latLongt.latitude, latLongt.longitude);
                    if(TextUtils.isEmpty(countryName))
                        countryName = Utility.getCountry(RegisterVanue.this, latLongt.latitude, latLongt.longitude);
                    Log.d("MUKEEEBABU",cityName+"  "+stateName+"  "+stateNameShort+"  "+countryName);
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                } else if (resultCode == RESULT_CANCELED) {
                }
                break;

        }
    }

    private void setPicCaptureByGallery(Intent data) throws Exception {
        Uri selectedImage = data.getData();
        String filePath;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = DocumentsContract.getDocumentId(selectedImage);
            InputStream inputStream = getContentResolver().openInputStream(selectedImage);

            File file = new File(getCacheDir().getAbsolutePath() + "/" + id);
            PathUtil.writeFile(inputStream, file);
            filePath = file.getAbsolutePath();
        } else
            filePath = PathUtil.getPath(this, selectedImage);

        ImageDAO dao = new ImageDAO(filePath);
        if (!imageList.contains(dao)) {
            imageList.add(dao);
            galleryPicAdapter.refresh(imageList);
        } else {
            Toast.makeText(getApplicationContext(), "Picture is already in list", Toast.LENGTH_LONG).show();
        }
    }

    private void setPicCapturedByCamera() {
        ImageDAO dao = new ImageDAO(currentPhotoPath);
        if (!imageList.contains(dao)) {
            imageList.add(new ImageDAO(currentPhotoPath));
            galleryAddPic();
            setPic();
        } else {
            Toast.makeText(getApplicationContext(), "Picture is already in list", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        /*int targetW = currentImageView.getWidth();
        int targetH = currentImageView.getHeight();*/

        int targetW = binding.myImg.getWidth();
        int targetH = binding.myImg.getHeight();

        Log.e("target W " + targetW, "target H" + targetH);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.e("photo W " + photoW, "photo H" + photoH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        try {
            ExifInterface ei = new ExifInterface(currentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        galleryPicAdapter.refresh(imageList);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void setCanContact(View v) {
        Chip chip = (Chip) v;
        switch (chip.getId()) {
            case R.id.in_app_chip:
                if (chip.isChecked()) contactVia.add(ContactEnum.in_app_only.toString());
                else contactVia.remove(ContactEnum.in_app_only.toString());
                break;

            case R.id.phone_call_chip:
                if (chip.isChecked()) contactVia.add(ContactEnum.phone_calls.toString());
                else contactVia.remove(ContactEnum.phone_calls.toString());
                break;

            case R.id.mail_chip:
                if (chip.isChecked()) contactVia.add(ContactEnum.email.toString());
                else contactVia.remove(ContactEnum.email.toString());
                break;
        }

    }

    public void setAvailability(View v) {
        Chip chip = (Chip) v;
        if (!chip.isChecked())
            if (binding.chipAllday.isChecked()) binding.chipAllday.setChecked(false);

        switch (chip.getId()) {
            case R.id.chip_friday:
                if (chip.isChecked()) {
                    days.add(Constants.FRIDAY);
                } else {
                    days.remove(Constants.FRIDAY);
                }
                break;

            case R.id.chip_saturday:
                if (chip.isChecked()) {
                    days.add(Constants.SATURDAY);
                } else {
                    days.remove(Constants.SATURDAY);
                }

                break;
            case R.id.chip_sunday:
                if (chip.isChecked()) {
                    days.add(Constants.SUNDAY);
                } else {
                    days.remove(Constants.SUNDAY);
                }
                break;

            case R.id.chip_thursday:
                if (chip.isChecked()) {
                    days.add(Constants.THURSDAY);
                } else {
                    days.remove(Constants.THURSDAY);
                }
                break;

            case R.id.chip_wednesday:
                if (chip.isChecked()) {
                    days.add(Constants.WEDNESDAY);
                } else {
                    days.remove(Constants.WEDNESDAY);
                }
                break;

            case R.id.chip_tuesday:
                if (chip.isChecked()) {
                    days.add(Constants.TUESDAY);
                } else {
                    days.remove(Constants.TUESDAY);
                }
                break;

            case R.id.chip_monday:
                if (chip.isChecked()) {
                    days.add(Constants.MONDAY);
                } else {
                    days.remove(Constants.MONDAY);
                }
                break;

        }
        Log.e("my list", days.toString());
    }

    @Override
    public void onBackPressed() {
        if (Utility.getSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUER)) {
            Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.IS_VENUER, false);
            Intent intent = new Intent(this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else
            super.onBackPressed();
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

    private void setGalleryAdapter() {
        galleryPicAdapter = new GalleryPicAdapter(false, (s, position) -> {
            if (imageList.size() > 0) {
                imageList.remove(position);
                galleryPicAdapter.notifyItemRemoved(position);
            } else {
                ImageDAO dao = new ImageDAO(s);
                imageList.remove(dao);
                int selectedImgListIndex = imageList.indexOf(dao);
                galleryPicAdapter.notifyItemRemoved(selectedImgListIndex);
                if (previousImages != null) {
                    int index = previousImages.indexOf(dao);
                    if (index != -1) {
                        ImageDAO uploadedDao = previousImages.get(index);
                        if (deletedIds.length() == 0)
                            deletedIds = deletedIds.concat(String.valueOf(uploadedDao.getId()));
                        else
                            deletedIds = deletedIds.concat("," + uploadedDao.getId());
                        previousImages.remove(uploadedDao);
                    }
                }
            }
        });
        binding.galleryRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.galleryRv.setHasFixedSize(true);
        binding.galleryRv.setAdapter(galleryPicAdapter);
    }

    private void setSpinner() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_array, R.layout.spinner_item); //change the last argument here to your xml above.
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner1.setAdapter(typeAdapter);
        binding.spinner2.setAdapter(typeAdapter);
        binding.spinner3.setAdapter(typeAdapter);
        binding.spinner4.setAdapter(typeAdapter);
        binding.spinner5.setAdapter(typeAdapter);
        binding.spinner6.setAdapter(typeAdapter);
        binding.spinner7.setAdapter(typeAdapter);
        binding.spinner8.setAdapter(typeAdapter);
        binding.spinner9.setAdapter(typeAdapter);
        binding.spinner10.setAdapter(typeAdapter);
        binding.spinner11.setAdapter(typeAdapter);
        binding.spinner12.setAdapter(typeAdapter);
        binding.spinner13.setAdapter(typeAdapter);
        binding.spinner14.setAdapter(typeAdapter);
        binding.spinner1.setSelection(8);
        binding.spinner2.setSelection(20);
        binding.spinner3.setSelection(8);
        binding.spinner4.setSelection(20);
        binding.spinner5.setSelection(8);
        binding.spinner6.setSelection(20);
        binding.spinner7.setSelection(8);
        binding.spinner8.setSelection(20);
        binding.spinner9.setSelection(8);
        binding.spinner10.setSelection(20);
        binding.spinner11.setSelection(8);
        binding.spinner12.setSelection(20);
        binding.spinner13.setSelection(8);
        binding.spinner14.setSelection(20);
    }

    private void getSubVenueView() {
        view = layoutInflater.inflate(R.layout.sub_venue_layout, binding.subVenueParentLayout, false);
        binding.subVenueParentLayout.addView(view);
        TextView textView = (TextView) view.findViewById(R.id.tvSubVenue);
        ImageView ivDelete = (ImageView) view.findViewById(R.id.ivDelete);

        for (int i = 1; i <= binding.subVenueParentLayout.getChildCount(); i++) {
            textView.setText("Sub - Venue " + i);
        }

        ivDelete.setOnClickListener(v -> {
            LinearLayout linearParent = (LinearLayout) v.getParent().getParent();
            binding.subVenueParentLayout.removeView(linearParent);
            if (binding.subVenueParentLayout.getChildCount() == 0) {
                binding.cbSubVenue.setChecked(false);
                binding.tvAddMoreSubVenue.setVisibility(View.GONE);
            }
        });
    }

    private boolean validateSubVenue() {
        boolean returnValue=true;

        for (int i = 0; i < binding.subVenueParentLayout.getChildCount(); i++) {
            View viewNew = binding.subVenueParentLayout.getChildAt(i);

            if (((EditText) viewNew.findViewById(R.id.etSubVenueName)).getText().toString().trim().length() == 0) {
                Dialogs.toast(this, viewNew.findViewById(R.id.etSubVenueName), "Enter Sub Venue Name Please!");
                viewNew.findViewById(R.id.etSubVenueName).requestFocus();
                returnValue=false;
                break;
            }

            if (((EditText) viewNew.findViewById(R.id.etSubVenueCapacity)).getText().toString().trim().length() == 0) {
                Dialogs.toast(this, viewNew.findViewById(R.id.etSubVenueCapacity), "Enter Sub Venue Capacity Please!");
                viewNew.findViewById(R.id.etSubVenueCapacity).requestFocus();
                returnValue=false;
                break;
            }
        }
        return returnValue;
    }

    private boolean validateSubVenue(View view, boolean showErrorMsg) {
        Log.d("MUKEEEBABU",""+binding.subVenueParentLayout.getChildCount());

        if (((EditText) view.findViewById(R.id.etSubVenueName)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
               Dialogs.toast(this, view.findViewById(R.id.etSubVenueName), "Enter Sub Venue Name Please!");
                view.findViewById(R.id.etSubVenueName).requestFocus();
            }
            return false;
        }

        if (((EditText) view.findViewById(R.id.etSubVenueCapacity)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
                Dialogs.toast(this, view.findViewById(R.id.etSubVenueCapacity), "Enter Sub Venue Capacity Please!");
                view.findViewById(R.id.etSubVenueCapacity).requestFocus();
            }
            return false;
        }

        return true;
    }

    public void selectSpinnerItemByValue(Spinner spnr, String value) {
        String[] strings = getResources().getStringArray(R.array.time_array);
        for (int position = 0; position < strings.length; position++) {
            if ((strings[position] + ":00").equals(value)) {
                spnr.setSelection(position);
                return;
            }
        }
    }

}


