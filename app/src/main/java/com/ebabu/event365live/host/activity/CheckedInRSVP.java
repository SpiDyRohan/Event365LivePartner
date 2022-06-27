package com.ebabu.event365live.host.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityCheckedInRsvpBinding;
import com.ebabu.event365live.host.databinding.DialogCheckinViaTicketBinding;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.fragments.CheckedInRSVPFragment;
import com.ebabu.event365live.host.fragments.NoCheckedInRSVPFragment;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CheckedInRSVP extends BaseActivity {

    ActivityCheckedInRsvpBinding binding;
    private Disposable _disposable;

    @Inject
    ApiInterface apiInterface;
    private MyLoader loader;

    int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_checked_in_rsvp);
        eventId = getIntent().getIntExtra("id", -1);
        loader = new MyLoader(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putInt("id", eventId);

        CheckedInRSVPFragment checkedFrag = new CheckedInRSVPFragment();
        NoCheckedInRSVPFragment noCheckedInRSVPFragment = new NoCheckedInRSVPFragment();
        noCheckedInRSVPFragment.setArguments(bundle);
        checkedFrag.setArguments(bundle);

        adapter.addFragment(checkedFrag, getString(R.string.checked_in_rsvp));
        adapter.addFragment(noCheckedInRSVPFragment, getString(R.string.not_checkedin_rsvp));
        binding.myViewPager.setAdapter(adapter);

        binding.tabs.setupWithViewPager(binding.myViewPager);

        binding.backArrow.setOnClickListener(v -> onBackPressed());

        binding.imageView7.setOnClickListener(v ->
                startActivityForResult(new Intent(CheckedInRSVP.this, BarCodeScanner.class)
                        .putExtra("id", eventId), 101
                ));

        binding.ivCheckIn.setOnClickListener(view -> {
            checkInDialog(this);
        });
        _disposable =
                RxTextView.textChangeEvents(binding.inputTxtDebounce)
                        .debounce(2, TimeUnit.SECONDS) // default Scheduler is Computation
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<TextViewTextChangeEvent>() {
                            @Override
                            public void accept(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                                search(textViewTextChangeEvent.text().toString());
                            }
                        });

        binding.inputTxtDebounce.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    if (binding.inputTxtDebounce.getText().toString().trim().length() > 0)
                        search(binding.inputTxtDebounce.getText().toString().trim());

                return false;
            }
        });

    }

    private void search(String query) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        EventBus.getDefault().post(new CheckedInEvent(query));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected) {
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
            }
        });
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
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
    protected void onDestroy() {
        super.onDestroy();
        _disposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            String qr = data.getStringExtra("data");

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("eventId", eventId);
            jsonObject.addProperty("QRkey", qr);
            jsonObject.addProperty("type", "QRkey");
            loader.show("");
            apiInterface.checkIn(jsonObject).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    loader.dismiss();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jObj = new JSONObject(response.body().toString());
                            Toast.makeText(CheckedInRSVP.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CheckedInRSVP.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        try {
                            JSONObject jObj = new JSONObject(response.errorBody().string());
                            Toast.makeText(CheckedInRSVP.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    loader.dismiss();
                    Toast.makeText(CheckedInRSVP.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public void checkInDialog(Context context) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogCheckinViaTicketBinding checkinViaTicketBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_checkin_via_ticket, null, false);
        builder.setView(checkinViaTicketBinding.getRoot());

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        checkinViaTicketBinding.tvNoBtn.setOnClickListener(view -> dialog.dismiss());

        checkinViaTicketBinding.tvSubmitBtn.setOnClickListener(view -> {
            String ticketNumber = checkinViaTicketBinding.ticketEt.getText().toString().trim();
            if (TextUtils.isEmpty(ticketNumber)) {
                Dialogs.toast(context, checkinViaTicketBinding.cv, "Please enter ticket number");
                checkinViaTicketBinding.ticketEt.requestFocus();
                return;
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("eventId", eventId);
                jsonObject.addProperty("ticketNumber", ticketNumber);
                jsonObject.addProperty("type", "ticketNumber");

                loader.show("");
                apiInterface.checkIn(jsonObject).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        loader.dismiss();
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jObj = new JSONObject(response.body().toString());
                                Toast.makeText(CheckedInRSVP.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(CheckedInRSVP.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                JSONObject jObj = new JSONObject(response.errorBody().string());
                                Toast.makeText(CheckedInRSVP.this, jObj.getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        loader.dismiss();
                        dialog.dismiss();
                        Toast.makeText(CheckedInRSVP.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
