package com.ebabu.event365live.host.utils;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.ebabu.event365live.host.R;

public class IntroPageTransformer implements ViewPager.PageTransformer  {
    @Override
    public void transformPage(View page, float position) {
        int pagePosition = (int) page.getTag();

        int pageWidth = page.getWidth();

        float pageWidthTimesPosition = pageWidth * position;

        if(position==0.0f){

        }

        if (position <= -1.0f || position >= 1.0f) {
            // The page is not visible. This is a good place to stop
            // any potential work / animations you may have running.
        } else if (position == 0.0f) {

        } else {
            View view = null;

            switch (pagePosition) {
                case 0: view = page.findViewById(R.id.intro1_img); break;
                case 1: view = page.findViewById(R.id.intro2_img); break;
                case 2: view = page.findViewById(R.id.intro3_img); break;

            }
            if (view != null) {
                view.setAlpha(1 - Math.abs(position));
                view.setTranslationX(-pageWidthTimesPosition * 1.5f);

                /*title.setTranslationY(-pageWidthTimesPosition / 2f);
                title.setAlpha(1.0f - absPosition);*/
            }
        }
    }
}
