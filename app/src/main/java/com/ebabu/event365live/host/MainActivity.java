package com.ebabu.event365live.host;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedCurrentLocationListener;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private LatLng currentLocation;
    private boolean shouldCheckPermission = false;
    private GetCurrentLocation getCurrentLocation;
    private Location lastLocation;
    private boolean isConnected=true;
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        if (!isLocationPermissionGiven()) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.CURRENT_FUSED_LOCATION_REQUEST);
            Log.d("nflasnlfas", "onCreate: ");
        } else {
            Log.d("nflasnlfas", "else : ");
            if (!isGpsOn()) {
                Log.d("nflasnlfas", "else if: ");
                displayLocationSettingsRequest();
            } else {
                displayLocationSettingsRequest();
                Log.d("nflasnlfas", "only else: ");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldCheckPermission)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.CURRENT_FUSED_LOCATION_REQUEST);
    }

    private void gpsAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.gps_enable_alert_dialog, null, false);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
        dialog.show();

        view.findViewById(R.id.btnQuit).setOnClickListener(v -> {
            finish();
        });

        view.findViewById(R.id.btnTurnOn).setOnClickListener(v -> {

            if (shouldCheckPermission) {
                Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(i, 1001);
            } else {
                displayLocationSettingsRequest();
            }
            dialog.dismiss();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedCurrentLocationListener != null) {
            fusedCurrentLocationListener.removeLocationUpdates(mLocationCallback);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.CURRENT_FUSED_LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayLocationSettingsRequest();
            } else {
                if (!(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))) {
                    shouldCheckPermission = true;
                    gpsAlertDialog();

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.CURRENT_FUSED_LOCATION_REQUEST);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                fusedCurrentLocationListener.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } else {
                shouldCheckPermission = false;
                gpsAlertDialog();
            }
        } else if (requestCode == 1001) {
            shouldCheckPermission = true;
        }
    }

    private void displayLocationSettingsRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Log.d("nflankfnlanlfa", locationResult.getLastLocation().getLatitude() + " onLocationResult: " + locationResult.getLastLocation().getLongitude());
                    if (lastLocation == null) {
                        if (TextUtils.isEmpty(Utility.getCurrentLocation(getApplicationContext()))) {
                            Utility.saveCurrentLocation(getApplicationContext(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        }
                        currentLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                        getCurrentLocation.getCurrentLocationListener(currentLocation);
                    }

                    if (fusedCurrentLocationListener != null) {
                        fusedCurrentLocationListener.removeLocationUpdates(mLocationCallback);
                    }
                }
            }
        };
        fusedCurrentLocationListener = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fusedCurrentLocationListener.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;
                if (location != null) {
                    if (TextUtils.isEmpty(Utility.getCurrentLocation(getApplicationContext()))) {
                        Utility.saveCurrentLocation(getApplicationContext(), location.getLatitude(), location.getLongitude());

                    }
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    getCurrentLocation.getCurrentLocationListener(currentLocation);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();

                Log.d("nflankfnlanlfa", "onFailure: " + e.getMessage());
            }
        });

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                fusedCurrentLocationListener.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("nflankfnlanlfa", "addOnFailureListener:======== " + e.getMessage());
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean isLocationPermissionGiven() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }

    private boolean isGpsOn() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public interface GetCurrentLocation {
        void getCurrentLocationListener(LatLng latLng);
    }

    public void getCurrentLocationInstance(GetCurrentLocation getCurrentLocation) {
        this.getCurrentLocation = getCurrentLocation;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkConnectivity();
            if (!isConnected)
                onNetworkChanged(false);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkConnectivity() {
        ConnectivityManager.NetworkCallback connectivityCallback
                = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isConnected = true;
                onNetworkChanged(true);
                //EventBus.getDefault().post(new InternetConnectionEvent(isConnected));
            }

            @Override
            public void onLost(Network network) {
                isConnected = false;
                onNetworkChanged(false);
                //EventBus.getDefault().post(new InternetConnectionEvent(isConnected));
            }
        };

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);
        monitoringConnectivity = true;


    }

    public abstract void onNetworkChanged(boolean isConnected);
}
