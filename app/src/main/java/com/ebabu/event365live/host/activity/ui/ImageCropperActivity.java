package com.ebabu.event365live.host.activity.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivityImageCropperBinding;
import com.ebabu.event365live.host.utils.ConcaveRoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ImageCropperActivity extends AppCompatActivity {

    private ActivityImageCropperBinding binding;
    private String uri;

    @Inject
    RequestManager requestManager;

    @Inject
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_cropper);
        App.getAppComponent().inject(this);
        init();
    }

    private void init() {
        binding.backArrow.setOnClickListener(view -> onBackPressed());
        uri = getIntent().getStringExtra("uri");
        requestManager.load(uri).into(binding.cropImageView);
        requestManager.load(uri).into(binding.ivBackground);
        setShapeBackground();
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void setShapeBackground() {
        float cornerSize = binding.ivBackground.getResources().getDimension(R.dimen._16sdp);
        binding.ivBackground.setShapeAppearanceModel(ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerSize)
                .setTopRightCorner(new ConcaveRoundedCornerTreatment())
                .setTopLeftCorner(new ConcaveRoundedCornerTreatment())
                .setBottomRightCorner(new ConcaveRoundedCornerTreatment())
                .setBottomLeftCorner(new ConcaveRoundedCornerTreatment())
                .build());
    }
}