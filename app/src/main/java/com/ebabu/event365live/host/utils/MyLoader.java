package com.ebabu.event365live.host.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.databinding.DataBindingUtil;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.LoaderLayoutBinding;

public class MyLoader {

    private Dialog dialog;
    private LoaderLayoutBinding binding;

    public MyLoader(Context context) {
        try {
            dialog = new Dialog(context);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.loader_layout, null, false);
            dialog.setContentView(binding.getRoot());
            dialog.getWindow().setLayout(-1, -2);
            dialog.setCancelable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show loader
    public void show(String message) {
        //binding.txtMessage.setText(message);
       try {
           dialog.show();

       }catch (Exception e){
           e.printStackTrace();
       }
    }

    // Stop loader
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
