package com.ebabu.event365live.host.utils;

import android.content.Context;
import android.util.Log;

import com.ebabu.event365live.host.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static String datePattern = "dd MMM";
    private static String timePattern = "hh:mm a";

    public static String reviewCount(int review) {
        return String.valueOf(review);
    }

    public static long getDateDiff(String datetime) {
        if (datetime == null) return 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            Date date1 = format.parse(datetime);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            Date date2 = calendar.getTime();
            long diff = date1.getTime() - date2.getTime();

            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getDateDiffLocal(String datetime) {
        if (datetime == null) return 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date dateUtil = format.parse(datetime);
            DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            outputFormat.setTimeZone(TimeZone.getDefault());

            String d = outputFormat.format(dateUtil);
            Date date1 = outputFormat.parse(d);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            Date date2 = calendar.getTime();
            long diff = date1.getTime() - date2.getTime();

            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getDateDiffTemp(String datetime) {
        if (datetime == null) return 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            Date date1 = format.parse(datetime);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 24);
            Date date2 = calendar.getTime();
            long diff = date1.getTime() - date2.getTime();

            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getDateDiffInDays(String datetime) {
        if (datetime == null) return 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            Date date1 = format.parse(datetime);

            Date date2 = new Date();
            long diff = date1.getTime() - date2.getTime();

            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getDateDiffInMin(String datetime) {
        if (datetime == null) return 0;

        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));
            Date date1 = format.parse(datetime);
            Date date2 = new Date();

            long diff = date1.getTime() - date2.getTime();
            return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS) + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDate(String dateTime) {
        if (dateTime == null) return "";
        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            Date utilDate = format.parse(dateTime);
            date = new SimpleDateFormat(datePattern).format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDateLocal(String dateTime) {
        if (dateTime == null) return "";
        String date = "";
        try {

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date utilDate = format.parse(dateTime);
            DateFormat outputFormat = new SimpleDateFormat(datePattern);
            outputFormat.setTimeZone(TimeZone.getDefault());

            date = outputFormat.format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDateByPattern(String datePattern, String dateTime) {
        if (dateTime == null) return "";
        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);
            date = new SimpleDateFormat(datePattern).format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getDateByPatternLocal(String datePattern, String dateTime) {
        if (dateTime == null) return "";
        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);
            date = new SimpleDateFormat(datePattern).format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTime(String dateTime) {

        if (dateTime == null) return "";

        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);

            date = new SimpleDateFormat(timePattern).format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeLocal(String dateTime) {

        if (dateTime == null) return "";

        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);
            DateFormat outputFormat = new SimpleDateFormat(timePattern);
            outputFormat.setTimeZone(TimeZone.getDefault());
            date = outputFormat.format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeReview(String dateTime) {

        if (dateTime == null) return "";

        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);

            date = new SimpleDateFormat(timePattern).format(utilDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeWithZone(String dateTime) {
        if (dateTime == null) return "";

        String date = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
            //format.setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getDisplayName()));

            Date utilDate = format.parse(dateTime);
            date = new SimpleDateFormat("hh:mm a").format(utilDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getAddress(String city, String state) {
        StringBuilder name = new StringBuilder();
        name.append((city != null && city.length() > 0) ? city + ", " : "").append(state == null ? "" : state);
        return name.toString().trim();
    }

    public static int getDayMonthNum(String s) {
        int num = -1;
        Log.e("got", s);
        switch (s) {
            case "January":
                num = 1;
                break;

            case "February":
                num = 2;
                break;
            case "March":
                num = 3;
                break;

            case "April":
                num = 4;
                break;

            case "May":
                num = 5;
                break;

            case "June":
                num = 6;
                break;
            case "July":
                num = 7;
                break;
            case "August":
                num = 8;
                break;
            case "September":
                num = 9;
                break;
            case "October":
                num = 10;
                break;
            case "November":
                num = 11;
                break;
            case "December":
                num = 12;
                break;
            case "Monday":
                num = 1;
                break;
            case "Tuesday":
                num = 2;
                break;
            case "Wednesday":
                num = 3;
                break;
            case "Thursday":
                num = 4;
                break;
            case "Friday":
                num = 5;
                break;
            case "Saturday":
                num = 6;
                break;
            case "Sunday":
                num = 7;
                break;
        }
        return num;
    }

    public static List<String> getEventType(Context context, String type) {
        List<String> eventList;

        if (type.equalsIgnoreCase("Free"))
            eventList = Arrays.asList(context.getResources().getStringArray(R.array.free_array));
        else if (type.equalsIgnoreCase("Paid"))
            eventList = Arrays.asList(context.getResources().getStringArray(R.array.paid_array));
        else
            eventList = Arrays.asList(context.getResources().getStringArray(R.array.both_array));

        return eventList;
    }

    public static String eventType(int i) {
        return i == 0 ? "Public" : "Private";
    }

    public static boolean displayOccuredOn(String type) {
        try {
            return type.equalsIgnoreCase("oneTime");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean displayLocLayout(String lat) {
        return lat != null;
    }

    public static String capitalizeWord(String name) {
        name = name.trim();
        StringBuilder stringBuffer = new StringBuilder();
        String getName = name;
        try {
            if (name.contains("  "))
                getName = name.replace("  ", " ");

            if (name.contains(" ")) {
                String[] arrayName = getName.split(" ");
                for (String s : arrayName) {
                    stringBuffer.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
                }
                return stringBuffer.toString().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.append(getName.substring(0, 1).toUpperCase()).append(getName.substring(1)).toString().trim();
    }

}
