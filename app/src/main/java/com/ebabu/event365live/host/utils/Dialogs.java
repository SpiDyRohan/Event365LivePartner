package com.ebabu.event365live.host.utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.SelectSubVenueAdapter;
import com.ebabu.event365live.host.databinding.DialogLayoutBinding;
import com.ebabu.event365live.host.databinding.DialogLoginAttemptsBinding;
import com.ebabu.event365live.host.databinding.DialogSelectSubVenueBinding;
import com.ebabu.event365live.host.entities.SubVenueDao;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

public class Dialogs {

    private static AlertDialog dialog;

    public static void showActionDialog(Context context, String title, String subtitle, String btnText, View.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogLayoutBinding dialogLogoutBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_layout, null, false);
        builder.setView(dialogLogoutBinding.getRoot());

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;


        dialog.show();

        dialogLogoutBinding.title.setText(title);
        dialogLogoutBinding.subtitle.setText(subtitle);
        dialogLogoutBinding.btnText.setText(btnText);

        dialogLogoutBinding.loaderBtn.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onClick(v);
        });
    }

    public static void toast(Context ctx, View v, String title) {
        Snackbar snack = Snackbar.make(v, title, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        view.setBackground(ctx.getResources().getDrawable(R.drawable.error_gradient));
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snack.show();
    }

    public static void itemNotFoundView(LayoutInflater inflater, LinearLayout rootLayout, String subTitle, View.OnClickListener listener) {
        View view = inflater.inflate(R.layout.layout_no_data, rootLayout, false);
        if (subTitle != null)
            ((TextView) view.findViewById(R.id.textView50)).setText(subTitle);

        LinearLayout retry_btn = view.findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(v -> listener.onClick(retry_btn));
        if (rootLayout.getChildCount() == 1)
            rootLayout.addView(view);
    }

    public static boolean validate(Context context, TextInputEditText et) {
        if (TextUtils.isEmpty(et.getText().toString().trim())) {
            et.requestFocus();
            Dialogs.toast(context, et, "Please enter " + et.getHint().toString());
            return false;
        }
        return true;

    }

    public static void setDate(Context context, String previousDate, TextView textView, OnStartDateSelected listener) {
        String preArr[] = null;
        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog dialog;
        if (previousDate.length() > 0)
            preArr = previousDate.split("/");


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                String myFormat = "yyyy/MM/dd"; //In which you need put here
                String myFormat = "MM/dd/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                if (listener != null)
                    listener.onSet(sdf.format(myCalendar.getTime()));
                else
                    textView.setText(sdf.format(myCalendar.getTime()));
            }
        };

        dialog = new DatePickerDialog(context, date,
                (previousDate.length() > 0) ? Integer.valueOf(preArr[0]) : myCalendar.get(Calendar.YEAR),
                (previousDate.length() > 0) ? Integer.valueOf(preArr[1]) - 1 : myCalendar.get(Calendar.MONTH),
                (previousDate.length() > 0) ? Integer.valueOf(preArr[2]) : myCalendar.get(Calendar.DAY_OF_MONTH)
        );

        if (previousDate.length() > 0) {
            try {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                Date d = sdf.parse(previousDate);
                long millis = d.getTime();
                dialog.getDatePicker().setMinDate(millis);
            } catch (Exception ex) {
            }
        } else dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.show();
    }

    public static void setMinMaxDate(Context context, String fromDate, String endDate, TextView textView, OnStartDateSelected listener) {

        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog dialog;

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "MM/dd/yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                if (listener != null)
                    listener.onSet(sdf.format(myCalendar.getTime()));
                else
                    textView.setText(sdf.format(myCalendar.getTime()));
            }
        };

        dialog = new DatePickerDialog(context, date, 0, 0, 0);

        if (fromDate.length() > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                Date d = sdf.parse(fromDate);
                assert d != null;
                long millis = d.getTime();
                dialog.getDatePicker().setMinDate(millis);
            } catch (Exception ex) {
            }


        } else {
            try {
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            } catch (Exception e) {

            }
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date d = sdf.parse(endDate);
            long millis = d.getTime();
            dialog.getDatePicker().setMaxDate(millis);
        } catch (Exception ex) {
        }

        dialog.show();
    }

    public static void setTime(Context context, TextView tv, OnStartDateSelected listener) {
        Calendar myCalendar = Calendar.getInstance();
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                String zone = selectedHour >= 12 ? "pm" : "am";
                int hour = selectedHour > 12 ? selectedHour - 12 : selectedHour;
                String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((selectedMinute < 10) ? "0" + selectedMinute : selectedMinute) + " " + zone;
                if (listener != null)
                    listener.onSet(time);
                else
                    tv.setText(time);
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public interface OnStartDateSelected {
        void onSet(String s);
    }

    public static void loginAttemptsDialog(Context context, View.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogLoginAttemptsBinding dialogLogoutBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_login_attempts, null, false);
        builder.setView(dialogLogoutBinding.getRoot());

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        dialogLogoutBinding.ivCross.setOnClickListener(view -> dialog.dismiss());

        dialogLogoutBinding.loaderBtn.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onClick(v);
        });
    }

    public static void availableSubVenueDialog(Context context, List<SubVenueDao> subVenueDaos, View.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogSelectSubVenueBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_select_sub_venue, null, false);
        builder.setView(binding.getRoot());

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        binding.rv.setLayoutManager(new LinearLayoutManager(context));
        binding.rv.setHasFixedSize(true);
        binding.rv.setNestedScrollingEnabled(false);
        SelectSubVenueAdapter adapter = new SelectSubVenueAdapter();
        binding.rv.setAdapter(adapter);
        adapter.refresh(subVenueDaos);

        binding.btnCancel.setOnClickListener(view -> dialog.dismiss());

        binding.btnProcess.setOnClickListener(view -> {
            boolean isSelected = false;
            for (int i = 0; i < subVenueDaos.size(); i++) {
                if (subVenueDaos.get(i).isSelected()) {
                    isSelected = true;
                }
            }

            if (isSelected) {
                dialog.dismiss();
                listener.onClick(view);
            } else
                Dialogs.toast(context, binding.getRoot(), "Please select the sub venue");
        });

    }

}
