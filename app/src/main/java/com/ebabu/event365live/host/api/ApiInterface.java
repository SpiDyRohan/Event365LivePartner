package com.ebabu.event365live.host.api;

import com.google.android.datatransport.Event;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST(API.USER_LOGOUT)
    Call<JsonElement> userLogout();

    @POST(API.VERIFY_RESET_PASSWORD)
    Call<JsonElement> verifyResetPassword(@Body JsonObject OBJ);

    @POST(API.RESET_PASSWORD)
    Call<JsonElement> resetPassword(@Body JsonObject OBJ);

    @POST(API.RESEND_OTP_EMAIL)
    Call<JsonElement> resendOTPForMail(@Body JsonObject OBJ);

    @POST(API.FORGOT_PASSWORD)
    Call<JsonElement> forgotPassword(@Body JsonObject OBJ);

    @POST(API.SIGNUP)
    Call<JsonElement> signup(@Body JsonObject OBJ);

    @POST(API.LOGIN)
    Call<JsonElement> login(@Body JsonObject OBJ);

    @POST(API.SOCIAL_LOGIN)
    Call<JsonElement> socialLogin(@Body JsonObject OBJ);

    @POST(API.UPDATE_SOCIAL_LOGIN_DATA)
    Call<JsonElement> updateSocialLoginData(@Body JsonObject OBJ);

    @POST(API.SEND_PHONE_OTP)
    Call<JsonElement> sendPhoneOTP(@Body JsonObject OBJ);

    @POST(API.VERIFY_PHONE)
    Call<JsonElement> verifyPhone(@Body JsonObject OBJ);

    @POST(API.VERIFY_MAIL)
    Call<JsonElement> verifyEmail(@Body JsonObject OBJ);

    @POST(API.INVITE_RSVP + "/{id}")
    Call<JsonElement> inviteRSVP(
            @Path("id") int eventId,
            @Body JsonObject OBJ
    );

    @GET(API.NOTIFICATION_COUNT)
    Call<JsonElement> getNotificationCount();

    @PUT(API.EVENT_AVAILABILITY)
    Call<JsonElement> changeEventAvailability(@Body JsonObject OBJ);

    @PUT(API.CANCEL_TICKET)
    Call<JsonElement> cancelTicket(@Body JsonObject OBJ);

    @GET(API.BALANCE_INFO)
    Call<JsonElement> getBalanceInfo();

    @GET(API.WITHDRAW_INFO)
    Call<JsonElement> getWithdrawInfo();

    @GET(API.GET_BANK_LIST)
    Call<JsonElement> getBankList(@Query("limit") int limit, @Query("page") int currentPage);

    @POST(API.ADD_BANK_DETAILS)
    Call<JsonElement> addBankDetailsRequest(@Body JsonObject obj);

    @POST(API.WITHDRAW_REQUEST)
    Call<JsonElement> amountWithdrawRequest(@Body JsonObject obj);

    @GET(API.TRANSACTION_HISTORY)
    Call<JsonElement> getTransactionHistoryRequest(@Query("limit") int limit, @Query("page") int currentPage, @Query("transStatus") String transStatus);

    @GET(API.GET_EDIT_EVENT_DETAILS + "/{id}")
    Call<JsonElement> getEditEventDetails(
            @Path("id") int id
    );

    @POST(API.PAID_EVENT_PAYMENT_DONE)
    Call<JsonElement> paidEventPayment(@Body JsonObject obj);

    @Multipart
    @POST(API.POST_EVENT)
    Call<JsonElement> postEventPaid(
            @Part("amount") RequestBody amount,
            @Part("eventType") RequestBody eventType,
            @Part("categoryId") RequestBody categoryId,
            @Part("subCategoryId") RequestBody subCategoryId,
            @Part("name") RequestBody name,
            @Part("eventOccurrenceType") RequestBody eventOccurrenceType,
            @Part("occurredOn") RequestBody occurredOn,
            @Part("start") RequestBody startDate,
            @Part("end") RequestBody endDate,
            @Part("venueId") RequestBody venueId,
            @Part("venueName") RequestBody venueName,
            @Part("venueAddress") RequestBody venueAddress,
            @Part("venueLatitude") RequestBody venueLatitude,
            @Part("venueLongitude") RequestBody venueLongitude,
            @Part("countryCode") RequestBody countryCode,
            @Part("city") RequestBody city,
            @Part("description") RequestBody description,
            @Part("description2") RequestBody description2,
            @Part("paidType") RequestBody paidType,
            @Part("vipSeatings") RequestBody vipSeatings,
            @Part("vipTableSeatings") RequestBody vipTableSeatings,
            @Part("regularSeatings") RequestBody regularSeatings,
            @Part("regularTableSeatings") RequestBody regularTableSeatings,
            @Part("free") RequestBody free,
            @Part("ticketInfoURL") RequestBody ticketInfoURL,
            @Part("eventHelpLine") RequestBody eventHelpline,
            @Part("sellingStart") RequestBody sellingStartDate,
            @Part("sellingEnd") RequestBody sellingEndDate,
            @Part("paymentId") RequestBody paymentId,
            @Part("hostMobile") RequestBody hostMobile,
            @Part("hostAddress") RequestBody hostAddress,
            @Part("websiteUrl") RequestBody websiteUrl,
            @Part("otherWebsiteUrl") RequestBody otherWebsiteUrl,
            @Part("isEventPaid") RequestBody isEventPaid,
            @Part("subVenueEvent") RequestBody subvenue,
            @Part MultipartBody.Part[] image
    );

    @Multipart
    @POST(API.POST_EVENT)
    Call<JsonElement> postEventFree(
            RequestBody type, RequestBody id, @Part("eventType") RequestBody eventType,
            @Part("categoryId") RequestBody categoryId,
            @Part("subCategoryId") RequestBody subCategoryId,
            @Part("name") RequestBody name,
            @Part("eventOccurrenceType") RequestBody eventOccurrenceType,
            @Part("occurredOn") RequestBody occurredOn,
            @Part("start") RequestBody startDate,
            @Part("end") RequestBody endDate,
            @Part("venueId") RequestBody venueId,
            @Part("venueName") RequestBody venueName,
            @Part("venueAddress") RequestBody venueAddress,
            @Part("venueLatitude") RequestBody venueLatitude,
            @Part("venueLongitude") RequestBody venueLongitude,
            @Part("countryCode") RequestBody countryCode,
            @Part("city") RequestBody city,
            @Part("description") RequestBody description,
            @Part("description2") RequestBody description2,
            @Part("paidType") RequestBody paidType,
            @Part("vipSeatings") RequestBody vipSeatings,
            @Part("vipTableSeatings") RequestBody vipTableSeatings,
            @Part("regularSeatings") RequestBody regularSeatings,
            @Part("regularTableSeatings") RequestBody regularTableSeatings,
            @Part("free") RequestBody free,
            @Part("ticketInfoURL") RequestBody ticketInfoURL,
            @Part("eventHelpLine") RequestBody eventHelpline,
            @Part("ticketSellingDays") RequestBody ticketsellindays,
            @Part("eventDay") RequestBody eventDay,
            @Part("sellingStart") RequestBody sellingStartTime,
            @Part("sellingEnd") RequestBody sellingEndTime,
            @Part("sellingStart") RequestBody sellingStartDate,
            @Part("sellingEnd") RequestBody sellingEndDate,
            @Part("hostMobile") RequestBody hostMobile,
            @Part("hostAddress") RequestBody hostAddress,
            @Part MultipartBody.Part[] image
    );

            @Multipart
            @POST ("/api/organiser/postEvent")
                    Call<Event> postEventTicket (
                            @PartMap Map<String, RequestBody> eventType,@Part MultipartBody.Part[] image);
                    Call<ResponseBody> upload(@Part RequestBody description,
                                             @Part RequestBody categoryId,
                                             @Part RequestBody subCategoryId,
                                             @Part RequestBody eventOccurrenceType,
                                             @Part RequestBody occurredOn,
                                             @Part RequestBody startDate,
                                             @Part RequestBody endDate,
                                             @Part RequestBody venueId,
                                             @Part RequestBody venueName,
                                             @Part RequestBody venueAddress,
                                             @Part RequestBody venueLatitude,
                                             @Part RequestBody venueLongitude,
                                             @Part RequestBody countryCode,
                                             @Part RequestBody city,
                                             @Part RequestBody description2,
                                             @Part RequestBody paidType,
                                             @Part RequestBody vipData,
                                             @Part RequestBody tableSeatings,
                                             @Part RequestBody rsvpData,
                                             @Part RequestBody regularPaid,
                                             @Part RequestBody regularFree,
                                             @Part RequestBody ticketInfoURL,
                                             @Part RequestBody eventHelpline,
                                             @Part RequestBody sellingStartDate,
                                             @Part RequestBody sellingEndDate,
                                             @Part RequestBody isEventPaid,
                                             @Part RequestBody subVenue,
                                             @Part MultipartBody.Part file);
//@Multipart
//@POST(API.POST_EVENT)
//Call<JsonElement> postEventTicket(
//            @Part("eventType") RequestBody eventType,
//            @Part("categoryId") RequestBody categoryId,
//            @Part("subCategoryId") RequestBody subCategoryId,
//            @Part("name") RequestBody name,
//            @Part("eventOccurrenceType") RequestBody eventOccurrenceType,
//            @Part("occurredOn") RequestBody occurredOn,
//            @Part("start") RequestBody startDate,
//            @Part("end") RequestBody endDate,
//            @Part("venueId") RequestBody venueId,
//            @Part("venueName") RequestBody venueName,
//            @Part("venueAddress") RequestBody venueAddress,
//            @Part("venueLatitude") RequestBody venueLatitude,
//            @Part("venueLongitude") RequestBody venueLongitude,
//            @Part("countryCode") RequestBody countryCode,
//            @Part("city") RequestBody city,
//            @Part("description") RequestBody description,
//            @Part("description2") RequestBody description2,
//            @Part("paidType") RequestBody paidType,
//            @Part("vipSeatings") RequestBody vipData,
//            @Part("tableSeatings") RequestBody tableSeatings,
//            @Part("regularSeatings") RequestBody rsvpData,
//            @Part("regularPaid") RequestBody regularPaid,
//            @Part("free") RequestBody regularFree,
//            @Part("ticketInfoURL") RequestBody ticketInfoURL,
//            @Part("eventHelpLine") RequestBody eventHelpline,
//            @Part("sellingStart") RequestBody sellingStartDate,
//            @Part("sellingEnd") RequestBody sellingEndDate,
//            @Part("isEventPaid") RequestBody isEventPaid,
//            @Part("SubVenue") RequestBody subVenue,
//            @Part MultipartBody.Part[] image);

//    @Part("willEventAvailable")RequestBody willEventAvailable,

    @Multipart
    @POST(API.EDIT_EVENT)
    Call<JsonElement> editEventRequest(
            @PartMap Map<String, RequestBody> editEventAllObj
            , @Part MultipartBody.Part[] img);

    @GET(API.GET_EVENT_TICKETS)
    Call<JsonElement> getEventTicketsRequest(@Path("id") int eventId);

    @POST(API.UPDATE_EVENT_TICKETS)
    Call<JsonElement> updateEventTickets(@Path("id") int eventId, @Body JsonObject obj);

    @Multipart
    @POST(API.ADD_VENUE)
    Call<JsonElement> addVenue(
            @Part("venueName") RequestBody venueName,
            @Part("venueAddress") RequestBody address,
            @Part("daysAvailable") RequestBody daysAvailable,
            @Part("contactVia") RequestBody contactVia,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("websiteURL") RequestBody websiteUrl,
            @Part("countryCode") RequestBody countryCode,
            @Part("city") RequestBody city,
            @Part("state") RequestBody state,
            @Part("fullState") RequestBody fullState,
            @Part("country") RequestBody country,
            @Part("shortDescription") RequestBody shortDescription,
            @Part("subVenues") RequestBody subVenues,
            @Part("venueCapacity") RequestBody venueCapacity,
            @Part("venueType") RequestBody venueType,
            @Part("isVenueAvailableToOtherHosts") RequestBody isVenueAvailableToOtherHosts,
            @Part MultipartBody.Part[] image
    );

    @Multipart
    @PATCH(API.VENUE + "/{id}")
    Call<JsonElement> updateVenue(
            @Path("id") int id,
            @Part("venueName") RequestBody venueName,
            @Part("venueAddress") RequestBody address,
            @Part("daysAvailable") RequestBody daysAvailable,
            @Part("contactVia") RequestBody contactVia,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("websiteURL") RequestBody websiteUrl,
            @Part("countryCode") RequestBody countryCode,
            @Part("city") RequestBody city,
            @Part("state") RequestBody state,
            @Part("fullState") RequestBody fullState,
            @Part("country") RequestBody country,
            @Part("shortDescription") RequestBody shortDescription,
            @Part("subVenues") RequestBody subVenues,
            @Part("venueCapacity") RequestBody venueCapacity,
            @Part("venueType") RequestBody venueType,
            @Part("isVenueAvailableToOtherHosts") RequestBody isVenueAvailableToOtherHosts,
            @Part("imageIds") RequestBody imageIds,
            @Part MultipartBody.Part[] image

    );

    @GET(API.USER_PAYMENT_DETAIL)
    Call<JsonElement> getPaymentDetails(@Query("QRkey") String qr, @Query("ticketId") int ticket_id);

    @Multipart
    @POST(API.UPDATE_PROFILE)
    Call<JsonElement> updateProfile(
            @Part("name") RequestBody name,
            @Part("address") RequestBody address,
            @Part("city") RequestBody city,
            @Part("state") RequestBody state,
            @Part("countryCode") RequestBody countryCode,
            @Part("zip") RequestBody zip,
            @Part("phoneNo") RequestBody contact,
            @Part("email") RequestBody email,
            @Part("URL") RequestBody URL,
            @Part("shortInfo") RequestBody shortInfo,
            @Part("contactVia") RequestBody contactVia,
            @Part("isContactVia") RequestBody isContactVia,
            @Part MultipartBody.Part image
    );

    @POST(API.ADD_USER)
    Call<JsonElement> addUser(@Body JsonObject OBJ);

    @PUT(API.CHECK_IN)
    Call<JsonElement> checkIn(@Body JsonObject OBJ);

    @DELETE(API.USER + "/{id}")
    Call<JsonElement> deleteUser(@Path("id") int userId);

    @PUT(API.USER + "/{id}")
    Call<JsonElement> updateUser(@Path("id") int userId, @Body JsonObject OBJ);

    @GET(API.GET_PROFILE)
    Call<JsonElement> getProfile();

    @GET(API.FETCH_CHECKED_IN_USER)
    Call<JsonElement> fetchCheckedInUser(
            @Query("eventId") int eventId,
            @Query("search") String search,
            @Query("checkedIn") boolean checkedIn,
            @Query("limit") int limit,
            @Query("page") int page
    );

    @POST(API.FETCH_CUSTOMERS)
    Call<JsonElement> fetchCustomers(
            @Query("limit") int limit,
            @Query("page") int page,
            @Body JsonObject obj
    );

    @GET(API.FETCH_NOTIFICATIONS)
    Call<JsonElement> getNotifications(
            @Query("limit") int limit,
            @Query("page") int page,
            @Query("notificationType") String notificationType,
            @Query("notificationTab") int notificationTab
    );

    @POST(API.CONTACT_LIST_ATTENDEES)
    Call<JsonElement> fetchAttendees(@Body JsonObject OBJ);

    @GET(API.ALL_RSVP_SEARCH)
    Call<JsonElement> fetchAllRSVP(
            @Query("eventId") int eventId,
            @Query("search") String search,
            @Query("rspvType") String type,
            @Query("limit") int limit,
            @Query("page") int page
    );

    @GET(API.FETCH_ALL_PAYMENTS)
    Call<JsonElement> fetchAllPayment(
            @Query("eventId") int eventId,
            @Query("search") String search,
            @Query("rspvType") String type,
            @Query("limit") int limit,
            @Query("page") int page
    );

    @DELETE(API.VENUE + "/{id}")
    Call<JsonElement> deleteVenue(@Path("id") int venueId);

    @GET(API.HOME)
    Call<JsonElement> getUserHomeDetail();

    @GET(API.USER_VENEUS)
    Call<JsonElement> getUserVenues();

    @GET(API.GET_VENEUS)
    Call<JsonElement> getFilterVenues(
            @Query("latitude") String lat,
            @Query("longitude") String log,
            @Query("searchValue") String search,
            @Query("miles") int miles
    );

    @GET(API.SUB_VENUE_DETAIL)
    Call<JsonElement> subVenueDetail(
            @Query("venueId") int venueId,
            @Query("eventStartDateTime") String startDate,
            @Query("eventEndDateTime") String endDate);

    @POST(API.LOCK_SUB_VENUE)
    Call<JsonElement> lockSubVenue(@Body JsonObject OBJ);

    @GET(API.GET_EVENTS)
    Call<JsonElement> getEvents();

    @GET(API.GET_EVENT_DATES)
    Call<JsonElement> getEventDates();

    @GET(API.GET_MORE_EVENT_DETAIL + "/{id}")
    Call<JsonElement> getMoreEventDetail(@Path("id") int eventId);

    @GET(API.CREATE_EVENT + "/{id}")
    Call<JsonElement> getEventDetail(@Path("id") int eventId);

    @DELETE(API.CREATE_EVENT + "/{id}")
    Call<JsonElement> deleteEvent(@Path("id") int eventId);

    @POST(API.GET_EVENTS_BY_DATE)
    Call<JsonElement> getEventsByDate(@Body JsonObject OBJ);

    @GET(API.USERS)
    Call<JsonElement> getUsers();

    @GET(API.GET_CATEGORIES)
    Call<JsonElement> getCategories();

    @GET(API.SUB_CATEGORIES + "/{id}")
    Call<JsonElement> getSubCategories(@Path("id") int categoryId);

    @GET(API.GET_CUSTOMER_USER + "/{id}")
    Call<JsonElement> getCustomerUserProfile(@Path("id") int userId);

    @GET(API.VENUE + "/{id}")
    Call<JsonElement> getVenueDetail(@Path("id") int venueId,
                                     @Query("eventStartDateTime") String startDate,
                                     @Query("eventEndDateTime") String endDate);

    @GET(API.GET_USER_BY_VANUER + "/{id}")
    Call<JsonElement> getManagedUser(@Path("id") int userId);

    @GET(API.VENUE_IMAGES + "/{id}")
    Call<JsonElement> getVenueImages(@Path("id") int venueId);

    @GET(API.HOST_DETAIL)
    Call<JsonElement> getHostDetail();

    @POST(API.GET_EPHEMERAL_KEY)
    Call<JsonElement> GetEphemeralKey(@Body JsonObject OBJ);

    @POST(API.PAID_EVENT_PRICE)
    Call<JsonElement> paidEventPrice(@Body JsonObject OBJpos);

    @POST(API.GET_CLIENT_SECRET)
    Call<JsonElement> getClientSecret(@Body JsonObject OBJ);

}