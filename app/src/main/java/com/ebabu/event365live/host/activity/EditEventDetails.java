package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityEditEventDetailsBinding;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.fragments.edit_ticket.EditTicktActivity;
import com.ebabu.event365live.host.fragments.rsvp_invites.SendRsvpInvitesActivity;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.EventDetailsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jakewharton.rxbinding2.view.RxView;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class EditEventDetails extends BaseActivity {

    ActivityEditEventDetailsBinding binding;
    EventDetailsViewModel viewModel;

    EventDAO dao;

    @Inject
    RequestManager requestManager;

    Disposable disposable;

    MyLoader loader;
    int eventIdFromNotification;

    boolean eventTypePast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_event_details);
        App.getAppComponent().inject(this);
        setSupportActionBar(binding.toolbar);
        loader = new MyLoader(this);
        loader.show("");

        eventIdFromNotification = getIntent().getIntExtra(API.ID, -1);
        eventTypePast = getIntent().getBooleanExtra("eventTypePast", false);
        if (eventTypePast) {
            binding.content.editEventDetail.setVisibility(View.GONE);
            binding.content.editTicketBtn.setVisibility(View.GONE);
        }

        try {
            if (!TextUtils.isEmpty(Utility.getSharedPreferencesString(this, Constants.USER_LOGIN_DATA))) {
                JSONObject userObject = new JSONObject(Utility.getSharedPreferencesString(this, Constants.USER_LOGIN_DATA));
                if (!userObject.getString("roles").contains("event_management")) {
                    binding.content.editEventDetail.setVisibility(View.GONE);
                    binding.content.editTicketBtn.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewModel = ViewModelProviders.of(this).get(EventDetailsViewModel.class);

        if (eventIdFromNotification == -1) {
            dao = (EventDAO) getIntent().getSerializableExtra(API.DATA);
            binding.content.setDao(dao);
            if (dao.getEventImages() != null && dao.getEventImages().size() > 0) {

                if (dao.getEventImages().size() == 1) {
                    requestManager
                            .load(dao.getEventImages().get(0).getEventImage())
                            .into(binding.eventImg);
                } else {
                    for (int i = 0; i < dao.getEventImages().size(); i++) {
                        if (dao.getEventImages().get(i).isPrimary()) {
                            requestManager
                                    .load(dao.getEventImages().get(i).getEventImage())
                                    .into(binding.eventImg);
                        }
                    }
                }
            }
        }


        viewModel.getOnChangeColor().observe(this, shouldChange -> {
            if (shouldChange) {
                binding.backArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_back_arrow_black));
                binding.toolbarTitle.setTextColor(getResources().getColor(R.color.black));
            } else {
                binding.backArrow.setImageDrawable(getResources().getDrawable(R.drawable.back_arrow_white));
                binding.toolbarTitle.setTextColor(getResources().getColor(R.color.white));
            }
        });

        showEventDetails(eventIdFromNotification != -1 ? eventIdFromNotification : dao.getId());

        binding.backArrow.setOnClickListener(v -> onBackPressed());
        binding.content.eventDetailCv.setOnClickListener(v -> startActivity(new Intent(this, EventDetails.class).putExtra(API.DATA, dao)));
        binding.content.editTicketBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EditTicktActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("eventId", dao.getId()));
        });

        binding.content.checkedInRsvpLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckedInRSVP.class);
            intent.putExtra("id", dao.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        binding.content.viewAllRSVPLayout.setOnClickListener(v -> {
            Intent viewAllRsvpIntent = new Intent(EditEventDetails.this, ViewAllRSVPActivity.class);
            viewAllRsvpIntent.putExtra("id", dao.getId());
            viewAllRsvpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(viewAllRsvpIntent);
        });

        binding.content.inviterRsvpLayout.setOnClickListener(v -> {
            if (Utility.getSharedPreferencesBoolean(this, API.CONTACT_ACCESS_GRANTED)) {
                openInviteActivity();
                return;
            }
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.message))
                    .setMessage(getString(R.string.contact_warning_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.agree), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Utility.setSharedPreferencesBoolean(this, API.CONTACT_ACCESS_GRANTED, true);
                        openInviteActivity();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Utility.setSharedPreferencesBoolean(this, API.CONTACT_ACCESS_GRANTED, false);
                    })
                    .show();
        });

        binding.content.showPaymentDetailsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPayments.class);
            intent.putExtra("id", dao.getId());
            startActivity(intent);
        });

        disposable = RxView.clicks(binding.content.switchMaterial).throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread()).
                        subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                loader.show("");
                                viewModel.changeEventAvailability(dao.getId(), binding.content.switchMaterial.isChecked()).observe(EditEventDetails.this, new Observer<MyResponse>() {
                                    @Override
                                    public void onChanged(MyResponse myResponse) {
                                        loader.dismiss();
                                        if (myResponse.isSuccess())
                                            Toast.makeText(EditEventDetails.this, myResponse.getMessage(), Toast.LENGTH_LONG).show();
                                        else
                                            Dialogs.toast(EditEventDetails.this, binding.getRoot(), myResponse.getMessage());

                                    }
                                });
                            }
                        });

        binding.content.editEventDetail.setOnClickListener(v -> {
            Intent intent = new Intent(EditEventDetails.this, UpdateEventDetail.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("id", dao.getId());
            startActivityForResult(intent, 1234);
        });
    }

    private void openInviteActivity() {
        Intent inviteRsvpIntent = new Intent(EditEventDetails.this, SendRsvpInvitesActivity.class);
        inviteRsvpIntent.putExtra("id", dao.getId());
        inviteRsvpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(inviteRsvpIntent);
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
    protected void onStop() {
        super.onStop();
        disposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1234) {
            Log.d("fasnfklas", "onActivityResult: " + dao.getId());
            showEventDetails(dao.getId());
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode != 1234)
                finish();
        }
    }

    private void showEventDetails(int eventId) {
        viewModel.getEventDetail(eventId).observe(this, response -> {
            loader.dismiss();
            if (response.getCode() == 404) {
                Toast.makeText(getApplicationContext(), response.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            } else if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(EditEventDetails.this);
            } else if (!response.isSuccess()) {
                loader.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            } else {
                try {
                    dao = response.getEventDAO();
                    if (dao.getName() != null)
                        binding.content.titleTv.setText(dao.getName());
                    binding.content.pastEventTv.setText(String.valueOf(dao.getTotalRSVP()));
                    binding.content.guestListTv.setText(String.valueOf(dao.getCheckedInRSVP()));
                    binding.content.guestListTv.setText(String.valueOf(dao.getCheckedInRSVP()));
                    if (dao.getTotalPayment() > 0)
                        binding.content.upcomingTv.setText("$" + Constants.FORMATOR_DECIMAL.format(dao.getTotalPayment()));
                    else
                        binding.content.upcomingTv.setText("$0");
                    binding.content.ratingTv.setText(String.valueOf(dao.getRatingCount()));
                    binding.content.ratingBar.setRating(Float.parseFloat(String.valueOf(dao.getRating())));
                    binding.content.switchMaterial.setChecked(dao.isEventAvailable());
                    if (!eventTypePast) {
                        try {
                            if (!TextUtils.isEmpty(Utility.getSharedPreferencesString(this, Constants.USER_LOGIN_DATA))) {
                                JSONObject userObject = new JSONObject(Utility.getSharedPreferencesString(this, Constants.USER_LOGIN_DATA));
                                if (userObject.getString("roles").contains("event_management"))
                                    binding.content.editTicketBtn.setVisibility(dao.isExternalTicket() ? View.GONE : View.VISIBLE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    if (dao.getEventImages() != null && dao.getEventImages().size() > 0) {

                        if (dao.getEventImages().size() == 1) {
                            requestManager
                                    .load(dao.getEventImages().get(0).getEventImage())
                                    .into(binding.eventImg);
                        } else {
                            for (int i = 0; i < dao.getEventImages().size(); i++) {
                                if (dao.getEventImages().get(i).isPrimary()) {
                                    requestManager
                                            .load(dao.getEventImages().get(i).getEventImage())
                                            .into(binding.eventImg);
                                }
                            }
                        }
                    }
                    if (dao.getCoupan() != null && dao.getCoupan().getCouponCode() != null) {
                        binding.content.couponCodeLayout.setVisibility(View.VISIBLE);
                        binding.content.couponTv.setText(dao.getCoupan().getCouponCode());
                    } else {
                        binding.content.couponCodeLayout.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
