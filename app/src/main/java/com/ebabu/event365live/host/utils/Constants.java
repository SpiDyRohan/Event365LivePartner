package com.ebabu.event365live.host.utils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class Constants {
    public static final DecimalFormat FORMATOR_DECIMAL = new DecimalFormat("#,##,##,##,##,##,###.00");
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_PASSWORD_REGEX =
            Pattern.compile("^(?=.*?[A-Za-z])(?=.*?[0-9]).{8,}$", Pattern.CASE_INSENSITIVE);

    public static final String iso8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

    public static final String FROM="from";
    public static final String SIGNUP="signup";
    public static final String SUNDAY="Sunday";
    public static final String MONDAY="Monday";
    public static final String TUESDAY="Tuesday";
    public static final String WEDNESDAY="Wednesday";
    public static final String THURSDAY="Thursday";
    public static final String FRIDAY="Friday";
    public static final String SATURDAY="Saturday";

    public static final String USER_TYPE="venuer";
    public static final String ON_BOARDING_DONE = "onboarding_done";
    public static final String IS_VENUER = "is_venuer";
    public static final String IS_VENUE_DELETED = "is_venue_deleted";
    public static final String CUSTOMER_ID = "customerId";
    public static final String USER_ID = "userId";
    public static final String LAST_LOGIN_TIME = "lastLoginTime";
    public static final String NOTIFICATION_COUNT = "notifications_count";
    public static final String USER_LOGIN_DATA = "USER_LOGIN_DATA";

    public static final String TICKET_TYPE_VIP = "VIP";
    public static final String TICKET_TYPE_RSVP = "RSVP";
    public static final String TICKET_TYPE_PAID = "Paid";
    public static final String TICKET_TYPE_FREE = "Free";


    public static final String[]days={
            MONDAY,TUESDAY,WEDNESDAY,THURSDAY, FRIDAY,SATURDAY,SUNDAY
    };

    public static final int CURRENT_FUSED_LOCATION_REQUEST = 9001;
    public static String currentLat = "currentLat";
    public static String currentLng = "currentLng";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 4001;

    public static String startDate24 = "startDate24";
    public static String endDate24 = "endDate24";

}
