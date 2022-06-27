package com.ebabu.event365live.host.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.GalleryAdapter;
import com.ebabu.event365live.host.adapter.ReviewAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityEventDetailsBinding;
import com.ebabu.event365live.host.entities.EventCategoryDAO;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.ReviewDao;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.fragments.SeeMoreReviewsDialogFrag;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.ImageViewer;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.EventDetailsViewModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

public class EventDetails extends BaseActivity implements OnMapReadyCallback {

    List<Integer> colors = Arrays.asList(
            R.color.chip2,
            R.color.purple,
            R.color.chip3,
            R.color.skyBluePurple,
            R.color.indigo,
            R.color.blue,
            R.color.skyBlue,
            R.color.quantum_vanillared800,
            R.color.chip2,
            R.color.purple,
            R.color.chip3,
            R.color.skyBluePurple,
            R.color.indigo,
            R.color.blue,
            R.color.skyBlue,
            R.color.quantum_vanillared800,
            R.color.orange
    );
    ActivityEventDetailsBinding binding;
    MyLoader loader;
    EventDetailsViewModel viewModel;
    private SeeMoreReviewsDialogFrag seeMoreReviewsDialogFrag;

    GalleryAdapter galleryAdapter;
    ReviewAdapter reviewAdapter;
    private GoogleMap mMap;
    EventDAO dao;

    @Inject
    RequestManager requestManager;
    Context mContext;
    private int getEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_details);
        setSupportActionBar(binding.toolbar);
        App.getAppComponent().inject(this);
        mContext = EventDetails.this;
        Collections.shuffle(colors);

        binding.content.nameTv.setOnClickListener(v -> startActivity(new Intent(EventDetails.this, ProfileActivity.class)));
        binding.content.hostedImg.setOnClickListener(v -> startActivity(new Intent(EventDetails.this, ProfileActivity.class)));

        loader = new MyLoader(this);
        loader.show("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        viewModel = ViewModelProviders.of(this).get(EventDetailsViewModel.class);
        getDynamicLink();

        binding.backArrow.setOnClickListener(v -> onBackPressed());

        //  binding.content.shareBtn.setOnClickListener(v -> Toast.makeText(EventDetails.this, "Work in progress..", Toast.LENGTH_SHORT).show());
        binding.content.addToBtn.setOnClickListener(v -> insertEvent());

        binding.content.shareBtn.setOnClickListener(v -> createDynamicLinks(dao.getName(), dao.getDescription(), dao.getEventImages().get(0).getEventImage(), dao.getId()));

        binding.content.seeMoreTv.setOnClickListener(v -> {
            addReviewDialogLaunch();
        });


    }

    private void insertEvent() {

        String sd[] = StringUtils.getDateByPattern("dd,MM,yyyy", dao.getStartDate()).split(",");
        String ed[] = StringUtils.getDateByPattern("dd,MM,yyyy", dao.getEndDate()).split(",");

        String st[] = StringUtils.getDateByPattern("hh,mm,a", dao.getStartDate()).split(",");
        String et[] = StringUtils.getDateByPattern("hh,mm,a", dao.getEndDate()).split(",");

        Log.e("start time", st[0] + " " + st[1]);

        if (sd.length < 3 || ed.length < 3 || st.length < 2 || et.length < 2) return;

        int hour = Integer.valueOf(st[0]);
        if (st[2].equalsIgnoreCase("pm")) hour = hour + 12;


        String des = "";
        if (dao.getDescription() != null)
            des = dao.getDescription();

        if (dao.getDescription2() != null)
            des = des.concat("\n" + dao.getDescription2());

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Integer.valueOf(sd[2]), Integer.valueOf(sd[1]) - 1, Integer.valueOf(sd[0]), hour, Integer.valueOf(st[1]));
        Calendar endTime = Calendar.getInstance();
        endTime.set(Integer.valueOf(ed[2]), Integer.valueOf(ed[1]) - 1, Integer.valueOf(ed[0]), Integer.valueOf(et[0]), Integer.valueOf(et[1]));

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, dao.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, des)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, dao.getEventAddress())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                //.putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")
                ;
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.appBar.addOnOffsetChangedListener(viewModel);
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
    protected void onPause() {
        super.onPause();
        binding.appBar.removeOnOffsetChangedListener(viewModel);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void getDynamicLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        if (pendingDynamicLinkData.getLink() != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("fnalksnfa", "getDynamicLinks: " + deepLink.toString());
                            Log.d("fnalksnfa", "getDynamicLinks: " + deepLink.getLastPathSegment());
                            if (deepLink.getLastPathSegment() != null) {
                                getEventId = Integer.parseInt(deepLink.getLastPathSegment());
                                init(getEventId);

                                return;
                            }
                            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        finish();
                    } else {
                        dao = (EventDAO) getIntent().getSerializableExtra(API.DATA);
                        init(dao.getId());
                    }
                });
    }

    private void createDynamicLinks(String eventTitle, String eventDes, String eventImg, int eventId) {

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://365live.com/user/event/" + eventId))
                .setDomainUriPrefix("https://365live.page.link")

                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.ebabu.event365live.host")
                        .setMinimumVersion(1)
                        .build()
                )
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.eventuser.app")
                        .setAppStoreId("1492460553")
                        .setMinimumVersion("1.5")
                        .build()
                )
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(eventTitle)
                        .setDescription(eventDes)
                        .setImageUrl(Uri.parse(eventImg))
                        .build()
                )
                .buildShortDynamicLink()
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        Utility.inviteFriend(EventDetails.this, flowchartLink.toString());
                    }
                });

    }

    private void init(int eventId) {
        viewModel.getMoreEventDetail(eventId).observe(this, response -> {
            loader.dismiss();
            Log.e("code", response.getCode() + "");
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(EventDetails.this);
            } else if (!response.isSuccess()) {
                Snackbar.make(binding.getRoot(), response.getMessage(), Snackbar.LENGTH_LONG).show();
                //Dialogs.toast(getApplicationContext(),binding.content.nameTv,response.getMessage());
            } else {
                dao = response.getEventDAO();
                if (dao != null) {
                    binding.content.setEvent(dao);
                    if (dao.getDescription() == null || dao.getDescription().length() == 0) {
                        binding.content.descLayout.setVisibility(View.GONE);
                    }

                    binding.content.titleTv.setText(dao.getName());

                    if (dao.getEventImages().size() > 0)
                        requestManager.load(dao.getEventImages().get(0).getEventImage()).into(binding.eventImg);

                    UserDAO user = dao.getHost();
                    if (user != null) {
                        if (user.getProfilePic() != null && user.getProfilePic().length() > 10)
                            requestManager.load(user.getProfilePic()).into(binding.content.hostedImg);
                        else
                            requestManager.load(DrawableUtils.getTempProfilePic(this, user.getName())).into(binding.content.hostedImg);
                    }

                    if (dao.getEventImages() == null || dao.getEventImages().size() == 0) {
                        binding.content.gallaryTv.setVisibility(View.GONE);
                        binding.content.galleryRv.setVisibility(View.GONE);
                    }
                    galleryAdapter.refresh(dao.getEventImages());

                    List<ReviewDao> list = dao.getReviews();

                    if (list.size() == 0) {
                        binding.content.reviewsTv.setVisibility(View.GONE);
                        binding.content.reviewRv.setVisibility(View.GONE);
                        binding.content.seeMoreTv.setVisibility(View.GONE);
                        binding.content.seeMoreLine.setVisibility(View.GONE);
                    }
                    reviewAdapter.refresh(list);

                    VenueDAO venueDAO = dao.getVenue();


                    /*as far no need to show google map with location*/

//                    if (venueDAO != null) {
//                        if (venueDAO.getLatitude() != null && venueDAO.getLongitude() != null && mMap != null) {
//                            LatLng latLng = new LatLng(Double.valueOf(venueDAO.getLatitude()), Double.valueOf(venueDAO.getLongitude()));
//                            mMap.addMarker(
//                                    new MarkerOptions()
//                                            .position(latLng)
//                                            .title(venueDAO.getVenueAddress())
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon))
//                            );
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
//                        }
//                    }
                    int colorCounter = 0;

                    Chip ch = new Chip(mContext);
                    ch.setText(dao.getCategoryName());

                    ch.setTextColor(ContextCompat.getColor(mContext, R.color.whiteLight));
                    ch.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, colors.get(colorCounter++))));
                    binding.content.categoryCGroup.addView(ch);

                    for (EventCategoryDAO ecd : dao.getSubCategories()) {
                        Chip chip = new Chip(mContext);
                        chip.setText(ecd.getSubCategoryName());
                        chip.setTextColor(ContextCompat.getColor(mContext, R.color.whiteLight));
                        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(mContext, colors.get(colorCounter++))));
                        binding.content.categoryCGroup.addView(chip);
                    }

                }
            }
        });

        viewModel.getOnChangeColor().observe(this, shouldChange -> {
            if (shouldChange) {
                binding.backArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_back_arrow_black));
                binding.toolbarTitle.setTextColor(getResources().getColor(R.color.black));
            } else {
                binding.backArrow.setImageDrawable(getResources().getDrawable(R.drawable.back_arrow_white));
                binding.toolbarTitle.setTextColor(getResources().getColor(R.color.white));
            }
        });

        galleryAdapter = new GalleryAdapter((position, list) -> ImageViewer.getInstance().showImageViewer(EventDetails.this, list));
        reviewAdapter = new ReviewAdapter();
        binding.content.galleryRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.content.galleryRv.setHasFixedSize(true);
        binding.content.galleryRv.setAdapter(galleryAdapter);

        binding.content.reviewRv.setLayoutManager(new LinearLayoutManager(this));
        binding.content.reviewRv.setHasFixedSize(true);
        binding.content.reviewRv.setAdapter(reviewAdapter);


    }

    public void addReviewDialogLaunch() {
        if (seeMoreReviewsDialogFrag == null) {
            seeMoreReviewsDialogFrag = new SeeMoreReviewsDialogFrag();
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        seeMoreReviewsDialogFrag.show(fragmentTransaction, "SeeMoreDialogFragment");
    }

}
