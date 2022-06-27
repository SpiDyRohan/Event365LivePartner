package com.ebabu.event365live.host.utils;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static String convertLocalToUTC(String date, String time, boolean needTime) {
        String dateTime = date + " " + time;
        String convertedDate = "";
        String convertedTime = "";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            formatter.setTimeZone(TimeZone.getDefault()); /*Local Time*/
            Date value = formatter.parse(dateTime);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC")); /*Server Time*/
            convertedDate = dateFormatter.format(value);

            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
            timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC")); /*Server Time*/
            convertedTime = timeFormatter.format(value);

            if (needTime) {
                return convertedTime;
            }
            return convertedDate;

        } catch (Exception e) {
            //If exception occurred return the local value
            if (needTime) {
                return time;
            }
            return date;
        }
    }

   /* public static void convertUTCToLocal(String date, String time, TextView tvDate, TextView tvTime) {
        String dateTime = date + " " + time;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC")); *//*Server Time*//*
            Date value = formatter.parse(dateTime);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            dateFormatter.setTimeZone(TimeZone.getDefault()); *//*Local Time*//*
            tvDate.setText(dateFormatter.format(value));

            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm aa");
            timeFormatter.setTimeZone(TimeZone.getDefault()); *//*Local Time*//*
            tvTime.setText(timeFormatter.format(value));

        } catch (Exception e) {
            tvDate.setText(date);
            tvTime.setText(time);
        }
    }*/
}
