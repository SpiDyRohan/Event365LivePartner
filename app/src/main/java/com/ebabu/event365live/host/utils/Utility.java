package com.ebabu.event365live.host.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.Login;
import com.ebabu.event365live.host.api.API;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.WIFI_SERVICE;

public class Utility {

    private static long mLastClickTime = 0;
    public static String timeZone = "";

    public static void setSharedPreferencesString(Context context, String name, String value) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public static void setSharedPreferencesBoolean(Context context, String name, Boolean value) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    public static void setSharedPreferencesInteger(Context context, String name, int value) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.apply();
    }

    public static int getSharedPreferencesInteger(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        return settings.getInt(name, -1);
    }

    public static int getNotificationSharedPrefInteger(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        return settings.getInt(name, 0);
    }

    public static Boolean getSharedPreferencesBoolean(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        return settings.getBoolean(name, false);
    }

    public static Boolean isLogin(Context context) {
        return getSharedPreferencesString(context, API.AUTHORIZATION).length() > 0 && getSharedPreferencesBoolean(context, API.SESSION_ACTIVE);
    }

    // String value GET on SharedPreferences
    public static String getSharedPreferencesString(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.app_name), 0);
        return settings.getString(name, "");
    }

    public static void sessionExpired(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.alert))
                .setMessage(context.getString(R.string.session_expired))
                .setCancelable(false)
                .setPositiveButton("Ok", (dialogInterface, i) -> logOut(context))
                .show();
    }

    public static void logOut(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(context.getString(R.string.app_name));
        editor.clear();
        editor.apply();
        Utility.setSharedPreferencesBoolean(context, Constants.ON_BOARDING_DONE, true);
        Utility.setSharedPreferencesBoolean(context, API.HOT_RELOAD, true);
        Utility.setSharedPreferencesBoolean(context, API.HOT_RELOAD_EVENTS, true);
        Utility.setSharedPreferencesBoolean(context, API.HOT_RELOAD_PROFILE, true);
        Utility.setSharedPreferencesBoolean(context, Constants.IS_VENUE_DELETED, true);
        Utility.setSharedPreferencesBoolean(context, API.HOT_RELOAD_EVENTS, true);
        Utility.setSharedPreferencesInteger(context, API.NOTIFICATION_COUNT, 0);

        Intent intent = new Intent(context, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Increase Clicking Time
    public static boolean buttonClickTime() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void inviteFriend(Context context, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        i.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(i, "Share via:"));
    }

    public static String getCity(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String data = "";
            if (addresses != null && addresses.size() > 0) {
                Address obj = addresses.get(0);
                data = obj.getLocality();
            }
            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static String getState(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String data = "";
            if (addresses != null && addresses.size() > 0) {
                Address obj = addresses.get(0);
                data = obj.getAdminArea();
            }
            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static String getCountry(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String data = "";
            if (addresses != null && addresses.size() > 0) {
                Address obj = addresses.get(0);
                data = obj.getCountryName();
            }
            return data;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static void setLocation(Context context, TextView tvShowCurrentLocation, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                tvShowCurrentLocation.setText(city + "," + country);
            }
            tvShowCurrentLocation.setSelected(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCountryCode(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String code = "91";
            if (addresses != null && addresses.size() > 0) {
                Address obj = addresses.get(0);
                code = obj.getCountryCode();
            }

            return code;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static String getPostalCode(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String data = "";
            if (addresses != null && addresses.size() > 0) {
                Address obj = addresses.get(0);
                data = obj.getPostalCode();
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static String getDateTime(String setDate, boolean needTime) {
        int getDate = 0, getYear = 0, getMonth = 0;
        String time = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
           /* timeFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            inputFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            outputFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));*/
            Calendar inputFormatCalendar = inputFormat.getCalendar();
            time = timeFormat.format(inputFormatCalendar.getTime());

            Date date = inputFormat.parse(setDate);
            Calendar calendar = outputFormat.getCalendar();
            calendar.setTime(date);

            getDate = calendar.get(Calendar.DATE);
            getYear = calendar.get(Calendar.YEAR);
            getMonth = calendar.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (needTime) return time;
        else return getMonth + "/" + getDate + "/" + getYear;
    }

    public static String getDateTimeLocal(String setDate, boolean needTime) {
        int getDate = 0, getYear = 0, getMonth = 0;
        String time = "", date = "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
            outputFormat.setTimeZone(TimeZone.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            timeFormat.setTimeZone(TimeZone.getDefault());

            Date value = inputFormat.parse(setDate);

            Calendar inputFormatCalendar = inputFormat.getCalendar();

            time = timeFormat.format(value);
            date = outputFormat.format(value);

           /* Calendar calendar = outputFormat.getCalendar();
            calendar.setTime(date);

            getDate = calendar.get(Calendar.DATE);
            getYear = calendar.get(Calendar.YEAR);
            getMonth = calendar.get(Calendar.MONTH) + 1;*/
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (needTime) return time;
        else return date;
    }

    public static String getDateFormat(String setDate, boolean needTime) {
        try {

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
            Date newDate = inputFormat.parse(setDate);
            setDate = outputFormat.format(newDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return setDate;
    }

    public static String getDateTimeOnEdit(String setDate, boolean needTime) {
        int getDate = 0, getYear = 0, getMonth = 0;
        String time = "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            /*timeFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            inputFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            outputFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));*/
            Calendar inputFormatCalendar = inputFormat.getCalendar();
            time = timeFormat.format(inputFormatCalendar.getTime());

            Date date = inputFormat.parse(setDate);
            Calendar calendar = outputFormat.getCalendar();
            calendar.setTime(date);

            getDate = calendar.get(Calendar.DATE);
            getYear = calendar.get(Calendar.YEAR);
            getMonth = calendar.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (getYear == 0 && getMonth == 0) {
            return setDate;
        } else {
            return getMonth + "/" + getDate + "/" + (needTime ? time : getYear);
        }
    }

    public static String localFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        //sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date); //get UTC date from local dateTime
    }

    public static String localFormatUTC(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date); //get UTC date from local dateTime
    }

    public static String localToUTC24(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String localToUTC(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String getDateMonthYearName(String dateFormat, boolean isYearRequired) {
        String getDate = "0";
        String getMonth = "";
        String getYear = "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            //inputFormat.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            Date date = inputFormat.parse(dateFormat);
            Calendar calendar = inputFormat.getCalendar();

            Date getActualTimeDate = calendar.getTime();
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-d", Locale.ENGLISH);
            String gotDate = outputFormat.format(getActualTimeDate);
            String[] dateArray = gotDate.split("-");

            getDate = dateArray[0];
            getMonth = dateArray[1];
            getYear = dateArray[2];

            Log.d("fasfasfsafa", gotDate + " getDateMonthName: " + getDate + "-" + getMonth + "-" + getYear);


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getMonth + "/" + (isYearRequired ? getYear : "") + "/" + getDate;
    }

    public static int dpToPx(float dp, Context context) {
        return dpToPx(dp, context.getResources());
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static String getTime12HourFormat(String time) {
        if (time == null) return "";

        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(time);
            date = new SimpleDateFormat("hh:mm a").format(utilDate);
        } catch (Exception e) {
            e.printStackTrace();
            date = time;
        }
        return date;
    }

    public static String getCurrentLocation(Context context) {
        String currentLocation = "";
        String lat = getSharedPreferencesString(context, Constants.currentLat);
        String lng = getSharedPreferencesString(context, Constants.currentLng);
        if (lat != null && lng != null)
            currentLocation = lat + " " + lng;
        return currentLocation;
    }

    public static void saveCurrentLocation(Context context, double lat, double lng) {
        setSharedPreferencesString(context, Constants.currentLat, String.valueOf(lat));
        setSharedPreferencesString(context, Constants.currentLng, String.valueOf(lng));
    }

    public static void launchSelectAddressFrag(Activity activity) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setTypeFilter(TypeFilter.CITIES)
                .build(activity);

        activity.startActivityForResult(intent, Constants.AUTOCOMPLETE_REQUEST_CODE);
    }

    public static String getIPaddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        return ipAddress;
    }

    public static String convertLocalToUTC(String date, String time) {
        String dateTime = date + " " + time;
        String convertedDate = "";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a"); // According to your Server TimeStamp
            formatter.setTimeZone(TimeZone.getDefault());
            Date value = formatter.parse(dateTime);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a"); //this format changeable according to your choice
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            convertedDate = dateFormatter.format(value);

        } catch (Exception e) {
            //If exception occurred return the local value
            convertedDate = dateTime;
        }
        System.out.println(convertedDate);
        return convertedDate;
    }

    public static String getDate(String dateTime) {
        String convertedDate = "";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
            Date value = formatter.parse(dateTime);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            convertedDate = dateFormatter.format(value);

        } catch (Exception e) {
            //If exception occurred return the local value
            convertedDate = dateTime;
        }
        System.out.println(convertedDate);
        return convertedDate;
    }

    public static String getTime(String dateTime) {
        String convertedTime = "";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
            Date value = formatter.parse(dateTime);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("H:mm a");
            convertedTime = dateFormatter.format(value);

        } catch (Exception e) {
            //If exception occurred return the local value
            convertedTime = dateTime;
        }
        System.out.println(convertedTime);
        return convertedTime;
    }

}

