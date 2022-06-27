package com.ebabu.event365live.host.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyImagePicker {

    private Activity activity;
    private Fragment fragment;
    private int requestCode;
    private String currentPhotoPath;

    private OnImagePickedListener listener;

    public MyImagePicker(Activity activity) {
        this.activity = activity;
    }

    public MyImagePicker(Fragment fragment) {
        this.fragment = fragment;
    }

    public MyImagePicker setCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public MyImagePicker onImagePicked(OnImagePickedListener listener) {
        this.listener = listener;
        return this;
    }

    public MyImagePicker choose() {
        Log.e("called", "yes");
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(activity != null ? activity.getPackageManager() : fragment.getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity != null ? activity : fragment.getContext(),
                        "com.ebabu.event365live.host.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePictureIntent}
                );

        if (activity != null)
            activity.startActivityForResult(chooserIntent, requestCode);
        else
            fragment.startActivityForResult(chooserIntent, requestCode);

        return this;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity != null ?
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                : fragment.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void handle(Intent data) {

        if (data != null && data.getData() != null) {
            try {
                setPicBrowsedByGallery(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            listener.getPath(currentPhotoPath);
    }

    private void setPicBrowsedByGallery(Intent data) throws Exception {
        Uri selectedImage = data.getData();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            currentPhotoPath = pickGalleryImageAboveOreo(selectedImage);
        else
            currentPhotoPath = PathUtil.getPath(activity != null ? activity : fragment.getContext(), selectedImage);

        Log.e("handeled*********", currentPhotoPath);
        listener.getPath(currentPhotoPath);
    }

    public interface OnImagePickedListener {
        void getPath(String path);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String pickGalleryImageAboveOreo(Uri selectedImage) throws Exception {
        String id = DocumentsContract.getDocumentId(selectedImage);

        InputStream inputStream =
                activity != null ? activity.getContentResolver().openInputStream(selectedImage)
                        : fragment.getContext().getContentResolver().openInputStream(selectedImage);

        File file = new File(
                activity != null ? activity.getCacheDir().getAbsolutePath() + "/" + id
                        : fragment.getContext().getCacheDir().getAbsolutePath() + "/" + id
        );
        PathUtil.writeFile(inputStream, file);
        return file.getAbsolutePath();
    }
}
