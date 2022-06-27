package com.ebabu.event365live.host.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.LayoutImageViewerBinding;
import com.ebabu.event365live.host.entities.EventImages;

import java.util.List;

public class ImageViewer {

    private static ImageViewer imageViewer;
    private ImageViewer(){}

    public static ImageViewer getInstance(){
        return imageViewer==null?imageViewer=new ImageViewer():imageViewer;
    }

    private AlertDialog dialog;

    public void showImageViewer(Context context, List<EventImages> images) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutImageViewerBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_image_viewer, null, false);
        builder.setView(binding.getRoot());

        dialog = builder.create();

        //making dialog fullscreen
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        binding.closeIcon.setOnClickListener(v-> dialog.dismiss());

        binding.viewpager.setAdapter(new CustomPagerAdapter(context,images));
        binding.tab.setupWithViewPager(binding.viewpager,true);


    }

    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;
        List<EventImages> images;

        CustomPagerAdapter(Context context,List<EventImages> images) {
            mContext = context;
            this.images=images;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_pager_image, collection, false);
            ImageView img=layout.findViewById(R.id.img);
            Glide.with(mContext).load(images.get(position).getEventImage()).into(img);
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

    }

}
