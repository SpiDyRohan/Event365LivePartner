package com.ebabu.event365live.host.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ebabu.event365live.host.R;

public class PermissionUtils{
    private PermissionUtils() {}
    private PermissionGrantListener listener;
    private int statusCode;
    private Activity activity;
    private Fragment fragment;
    private String []permisssion;
    private StringBuilder permissionStr;

    public PermissionUtils(Fragment fragment){
       this.fragment=fragment;
    }

    public PermissionUtils(Activity activity){
        this.activity=activity;
    }

    public PermissionUtils setCode(int statusCode){
       this.statusCode=statusCode;
       return this;
    }

    public PermissionUtils onAlreadyGranted(PermissionGrantListener listener){
        this.listener=listener;
        return this;
    }

    public PermissionUtils setPermissions(String[] permissions){
       this.permisssion=permissions;

       String s=permisssion[0];
       s="<b>"+s.substring(s.lastIndexOf(".")+1)+"</b>";
       permissionStr=new StringBuilder(s);

       for(int i=1;i<permisssion.length;i++) {
           s=permisssion[i];
           s="<b>"+s.substring(s.lastIndexOf(".")+1)+"</b>";
           permissionStr.append(" and ").append(s);
       }

       return this;
    }

    public PermissionUtils build(){

       try {
           checkPermission();
       }catch (Exception e){ e.printStackTrace(); }


       return this;
    }

    private void checkPermission(){

       boolean allGranted=true;
       for(String s:permisssion){
           if(ContextCompat.checkSelfPermission(activity!=null?activity:fragment.getContext(), s) != PackageManager.PERMISSION_GRANTED){
               allGranted=false;
               break;
           }
       }

       if(allGranted)
           listener.call();
       else {
           if(activity!=null)
               ActivityCompat.requestPermissions(activity, permisssion, statusCode);
           else
               fragment.requestPermissions(permisssion,statusCode);
       }
    }

    public void handle(int requestCode, int[] grantResults) {

        if(requestCode==statusCode) {
            boolean allGranted=true;

            for(int isGranted:grantResults){
                if(isGranted!=PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted)
                listener.call();

                else {
                if(activity!=null)
                    crossCheckPermissionForActivity();
                else
                    crossCheckPermissionForFragment();
            }
        }
    }

    private void crossCheckPermissionForFragment() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), Manifest.permission.CAMERA))
            permissionsPermanentlyRevokedShowSettingDialog();
        else {



            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
            builder.setTitle(fragment.getContext().getString(R.string.app_name))
                    .setMessage(Html.fromHtml(permissionStr+" permission(s) are mandatory for this action."))
                    .setPositiveButton("Ok", null)
                    .show();
        }
    }

    private void crossCheckPermissionForActivity() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA))
            permissionsPermanentlyRevokedShowSettingDialog();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getApplicationContext().getString(R.string.app_name))
                    .setMessage(Html.fromHtml(permissionStr+" permission(s) are mandatory for this action."))
                    .setPositiveButton("Ok", null)
                    .show();
        }
    }

    private void permissionsPermanentlyRevokedShowSettingDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity!=null?activity:fragment.getContext());
        builder.setTitle(activity!=null?activity.getApplicationContext().getString(R.string.app_name) : fragment.getContext().getString(R.string.app_name))
                .setMessage(R.string.no_camera_permission_setting_warning)
                .setPositiveButton("Setting", (dialogInterface, i) -> {
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + (activity!=null?activity.getPackageName():fragment.getActivity().getPackageName())));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(activity!=null)
                        activity.startActivity(myAppSettings);
                    else fragment.startActivity(myAppSettings);
                })
                .setNegativeButton("Cancel",null)
                .show();
    }

    public interface PermissionGrantListener{
            void call();
        }

}




