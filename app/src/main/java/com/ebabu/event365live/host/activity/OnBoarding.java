package com.ebabu.event365live.host.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.IntroAdapter;
import com.ebabu.event365live.host.databinding.ActivityOnBoardingBinding;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.IntroPageTransformer;
import com.ebabu.event365live.host.utils.Utility;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

public class OnBoarding extends AppCompatActivity {

    ActivityOnBoardingBinding binding;
    int imageCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding);

        binding.arrowBtn.setOnClickListener(v -> {
            int index = binding.viewpager.getCurrentItem();
            if (index < 2)
                binding.viewpager.setCurrentItem(++index, true);

        });
        binding.skipTV.setOnClickListener(v -> {
            Utility.setSharedPreferencesBoolean(getApplicationContext(), Constants.ON_BOARDING_DONE, true);
            startActivity(new Intent(this, Login.class));
        });

        binding.tab.setupWithViewPager(binding.viewpager, true);

        binding.viewpager.setAdapter(new IntroAdapter(getSupportFragmentManager()));

        binding.viewpager.setPageTransformer(false, new IntroPageTransformer());

        binding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                imageCounter = position;
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    binding.arrowBtn.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.arrowBtn.setVisibility(View.INVISIBLE);
                        }
                    });
                    binding.skipTV.setText(getString(R.string.get_started));
                } else {
                    binding.skipTV.setText("Skip");
                    binding.arrowBtn.animate().alpha(1).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            binding.arrowBtn.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}
