package com.ebabu.event365live.host.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.VerifiyCode;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentMyProfileBinding;
import com.ebabu.event365live.host.databinding.LayoutEditPhoneBinding;
import com.ebabu.event365live.host.entities.ContactEnum;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.MyImagePicker;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.PermissionUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.ProfileViewModel;
import com.ebabu.event365live.host.viewmodels.SMSVerificationViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.JsonElement;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class MyProfile extends Fragment {

    private boolean isValidPhone = false;

    private MyLoader loader;
    private HashSet<String> contactVia = new HashSet<>();

    @Inject
    RequestManager requestManager;
    @Inject
    ApiInterface apiInterface;

    private String currentPhotoPath;
    private Bitmap rotatedBitmap;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    FragmentMyProfileBinding binding;
    private boolean canAllow = true;

    int id = -1;

    private AlertDialog editDialog;
    private UserDAO userDAO;

    private SMSVerificationViewModel viewModel;

    private MyImagePicker imagePicker;
    private static final int IMAGE_PICKER_CODE = 222;
    private static final int PLACE_PICKER_REQUEST_CODE = 810;
    private String phone;
    private PermissionUtils permissionUtils;
    LayoutEditPhoneBinding dialogBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        App.getAppComponent().inject(this);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_profile, container, false);
        viewModel = ViewModelProviders.of(this).get(SMSVerificationViewModel.class);
        viewModel.init(getContext(), apiInterface);

        Places.initialize(getContext(), getString(R.string.google_maps_key));

        if (getArguments() != null)
            id = getArguments().getInt("id");

        if (id != -1) {
            binding.phoneEditBtn.setVisibility(View.GONE);
            binding.updateTV.setText(getString(R.string.send_message));
            binding.floatingActionButton.setVisibility(View.GONE);
            binding.fullNameEt.setFocusable(false);
            binding.mailEt.setFocusable(false);
            binding.phoneEt.setFocusable(false);
            binding.websiteUrlEt.setFocusable(false);
            binding.shortDescEt.setFocusable(false);
            binding.addrsEt.setFocusable(false);
            binding.addrsEt.setClickable(false);
            binding.cityEt.setFocusable(false);
            binding.stateEt.setFocusable(false);
            binding.zipEt.setFocusable(false);
            binding.textView22.setVisibility(View.GONE);
            binding.switchMaterial.setVisibility(View.GONE);
        }

        loader = new MyLoader(getContext());
        loader.show("");

        ProfileViewModel viewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        Observer<UserResponse> observer = userResponse -> {
            loader.dismiss();
            if (userResponse.getCode() == API.SESSION_EXPIRE) {
                userResponse.setCode(0);
                Utility.sessionExpired(getContext());
            } else {
                userDAO = userResponse.getUserDAO();

                if (userDAO != null) {
                    binding.setUser(userDAO);
                    String pic = userDAO.getProfilePic();
                    if (pic != null && pic.length() > 5)
                        requestManager
                                .load(pic)
                                .into(binding.circleImageView);
                    else
                        requestManager.load(DrawableUtils.getTempProfilePic(getContext(), userDAO.getName())).into(binding.circleImageView);


                    binding.mailChip.setChecked(false);
                    binding.inAppChip.setChecked(false);
                    binding.phoneCallChip.setChecked(false);

                    binding.switchMaterial.setChecked(userDAO.isContactVia());

                    if (userDAO.getCanContact() != null) {
                        String[] contactArr = userDAO.getCanContact().split(",");
                        for (String s : contactArr) setCanContact(s);
                    }
                } else if (id != -1) {
                    Toast.makeText(getContext(), "No user found with id " + id + " !", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        };

        if (id != -1)
            viewModel.getCustomerProfile(id).observe(getViewLifecycleOwner(), observer);
        else
            viewModel.getProfile().observe(getViewLifecycleOwner(), observer);

        binding.floatingActionButton.setOnClickListener(v -> checkForPermission());
        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());

        //TODO : need to check
        binding.updateBtn.setOnClickListener(this::updateProfile);

        binding.addrsEt.setOnClickListener(v -> {
            if (id == -1)
                chooseLocation();
        });

        binding.switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.view5.setVisibility(View.VISIBLE);
                binding.textView23.setVisibility(View.VISIBLE);
                binding.contactChipGroup.setVisibility(View.VISIBLE);
            } else {
                binding.view5.setVisibility(View.GONE);
                binding.contactChipGroup.setVisibility(View.GONE);
                binding.textView23.setVisibility(View.GONE);
            }
            canAllow = b;
        });


        binding.inAppChip.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                contactVia.add(ContactEnum.in_app_only.toString());
            else contactVia.remove(ContactEnum.in_app_only.toString());
        });

        binding.mailChip.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                contactVia.add(ContactEnum.email.toString());
            else contactVia.remove(ContactEnum.email.toString());
        });

        binding.phoneCallChip.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                contactVia.add(ContactEnum.phone_calls.toString());
            else contactVia.remove(ContactEnum.phone_calls.toString());
        });


        binding.phoneEditBtn.setOnClickListener(v -> {
            showPhoneUpdateDialog();
        });

        return binding.getRoot();
    }

    private void showPhoneUpdateDialog() {

        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.layout_edit_phone, null, false);
        builder.setView(dialogBinding.getRoot());

        dialogBinding.btnDecline.setOnClickListener(v -> editDialog.dismiss());
        dialogBinding.btnUpdate.setOnClickListener(v -> {

            if (!isValidPhone) {
                Toast.makeText(getContext(), getString(R.string.invalid_mob), Toast.LENGTH_LONG).show();
                return;
            }
            loader.show("");
            requestForOTP(dialogBinding.mobEt.getText().toString().trim());

        });

        dialogBinding.ccp.registerCarrierNumberEditText(dialogBinding.mobEt);
        dialogBinding.ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            isValidPhone = isValidNumber;
            if (isValidNumber) dialogBinding.mobImg.setVisibility(View.VISIBLE);
            else dialogBinding.mobImg.setVisibility(View.INVISIBLE);
        });

        editDialog = builder.create();
        editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editDialog.setCanceledOnTouchOutside(false);
        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            editDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        editDialog.show();
    }

    private void requestForOTP(String s) {
        phone = s;
        viewModel.verify(userDAO.getId(), s, dialogBinding.ccp.getSelectedCountryCodeWithPlus());
    }

    private void updateProfile(View v) {

        String name = "", mail = "";

        if (Dialogs.validate(getContext(), binding.fullNameEt))
            name = binding.fullNameEt.getText().toString().trim();
        else return;

        if (Dialogs.validate(getContext(), binding.mailEt)) {
            if (Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(binding.mailEt.getText().toString().trim()).matches())
                mail = binding.mailEt.getText().toString().trim();
            else {
                Dialogs.toast(getContext(), binding.getRoot(), getString(R.string.enter_valid_mail));
                return;
            }
        }


        if (!Dialogs.validate(getContext(), binding.addrsEt))
            return;


        if (canAllow && contactVia.size() == 0) {
            Dialogs.toast(getContext(), v, getString(R.string.choose_contact_via));
            return;
        }


        JSONArray contactJsonArray = new JSONArray();
        for (String s : contactVia) contactJsonArray.put(s);

        RequestBody contactViaBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), contactJsonArray.toString());

        RequestBody nameBody = RequestBody.create(MediaType.parse("multipart/form-data"), name);
        RequestBody mailBody = RequestBody.create(MediaType.parse("multipart/form-data"), mail);
        RequestBody stateBody = RequestBody.create(MediaType.parse("multipart/form-data"), binding.stateEt.getText().toString());
        RequestBody zipBody = RequestBody.create(MediaType.parse("multipart/form-data"), binding.zipEt.getText().toString());
        RequestBody countryCode = null, contactBody = null;
        if (userDAO.getCountryCode() != null)
            countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), userDAO.getCountryCode());
        if (userDAO.getPhoneNo() != null)
            contactBody = RequestBody.create(MediaType.parse("multipart/form-data"), userDAO.getPhoneNo());
        RequestBody url = RequestBody.create(MediaType.parse("multipart/form-data"), binding.websiteUrlEt.getText().toString());
        RequestBody shortInfo = RequestBody.create(MediaType.parse("multipart/form-data"), binding.shortDescEt.getText().toString());
        RequestBody addrs = RequestBody.create(MediaType.parse("multipart/form-data"), binding.addrsEt.getText().toString());
        RequestBody city = RequestBody.create(MediaType.parse("multipart/form-data"), binding.cityEt.getText().toString());
        RequestBody canContactBodyPart = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(canAllow));

        MultipartBody.Part imagePart = null;

        if (currentPhotoPath != null && currentPhotoPath.length() > 0) {
            File file = new File(currentPhotoPath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            imagePart = MultipartBody.Part.createFormData("profilePic", file.getName(), requestFile);
        }

        loader.show("");

        Call<JsonElement> call = apiInterface.updateProfile(
                nameBody, addrs, city, stateBody, countryCode, zipBody, contactBody, mailBody, url, shortInfo, contactViaBodyPart, canContactBodyPart, imagePart);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                loader.dismiss();
                if (response.isSuccessful()) {
                    try {
                        Utility.setSharedPreferencesBoolean(getContext(), "hotReloadProfile", true);
                        Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        Toast.makeText(getContext(), jsonObject.getString(API.MESSAGE), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        Dialogs.toast(getContext(), binding.getRoot(), jsonObject.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
            }
        });


    }

    private void checkForPermission() {
        final String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        permissionUtils = new PermissionUtils(this)
                .setCode(RC_HANDLE_CAMERA_PERM)
                .setPermissions(permissions)
                .onAlreadyGranted(this::choose)
                .build();
    }

    private void chooseLocation() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(getContext());
        startActivityForResult(intent, PLACE_PICKER_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_HANDLE_CAMERA_PERM)
            permissionUtils.handle(requestCode, grantResults);
    }

    private void choose() {
        imagePicker = new MyImagePicker(this)
                .setCode(IMAGE_PICKER_CODE)
                .onImagePicked(path -> {
                    currentPhotoPath = path;
                    requestManager.load(path).into(binding.circleImageView);
                })
                .choose();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_CODE)
            imagePicker.handle(data);
        else if (requestCode == 555 && resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            binding.phoneEt.setText(result);
            Toast.makeText(getContext(), "Phone is updated successfully!", Toast.LENGTH_LONG).show();
            Utility.setSharedPreferencesBoolean(getContext(), "hotReloadProfile", true);
        } else if (requestCode == PLACE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            binding.addrsEt.setText(place.getAddress());

            LatLng latLongt = place.getLatLng();
            binding.cityEt.setText(Utility.getCity(getContext(), latLongt.latitude, latLongt.longitude));
            binding.stateEt.setText(Utility.getState(getContext(), latLongt.latitude, latLongt.longitude));
            binding.zipEt.setText(Utility.getPostalCode(getContext(), latLongt.latitude, latLongt.longitude));
        }
    }

    private void setCanContact(String s) {
        switch (s) {
            case "in_app_only":
                binding.inAppChip.setChecked(true);
                break;
            case "phone_calls":
                binding.phoneCallChip.setChecked(true);
                break;
            case "email":
                binding.mailChip.setChecked(true);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(SuccessEvent event) {
        event.getMsg().observe(this, s -> {
            loader.dismiss();
            Log.e("data", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String msg = jsonObject.getString(API.MESSAGE);
                if (jsonObject.getBoolean(API.SUCCESS)) {
                    editDialog.dismiss();
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getContext(), VerifiyCode.class);
                    intent.putExtra(Constants.FROM, "profile");
                    intent.putExtra(API.ID, userDAO.getId());
                    intent.putExtra(API.COUNTRY_CODE, dialogBinding.ccp.getSelectedCountryCodeWithPlus());
                    intent.putExtra(API.PHONE_NO, phone);
                    startActivityForResult(intent, 555);

                } else {
                    editDialog.dismiss();
                    Dialogs.toast(getContext(), binding.updateBtn, msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
                editDialog.dismiss();
                Dialogs.toast(getContext(), binding.updateBtn, getString(R.string.commonUnknowError));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
