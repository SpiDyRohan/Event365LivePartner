package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentPaymentMethodBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.FreeTicketDao;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.RSVPTicketDao;
import com.ebabu.event365live.host.entities.TableAndSeatingDao;
import com.ebabu.event365live.host.stripe.GetEphemeralKey;
import com.ebabu.event365live.host.stripe.StripeConnect;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.Stripe;
import com.stripe.android.StripeError;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.Customer;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.ShippingInformation;
import com.stripe.android.view.AddPaymentMethodActivityStarter;
import com.stripe.android.view.PaymentMethodsActivityStarter;
import com.stripe.android.view.ShippingInfoWidget;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentMethodFragment extends Fragment {

    @Inject
    ApiInterface apiInterface;
    private FragmentPaymentMethodBinding binding;
    private CreateEventDAO createEventDAO;
    private MyLoader myLoader;
    private String amount = "0",msgAPI = "",eventID="";;
    private Stripe mStripe;
    private PaymentSession paymentSession;
    private boolean isPaymentMethodAvailable;
    private boolean willEventAvailable = true;
    private List<String> imageList;
    private PaymentMethod getPaymentMethod;
    private boolean isClcikCardAdd = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            createEventDAO = getArguments().getParcelable("eventDAO");
            amount = getArguments().getString("amount");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_method, container, false);

        App.getAppComponent().inject(this);

        myLoader = new MyLoader(getContext());
        imageList = new ArrayList<>();
        for (ImageDAO imageDAO : createEventDAO.getImageDAOList()) {
            imageList.add(imageDAO.getVenueImages());
        }

        binding.cvAddNewCard.setOnClickListener(view -> {
//            Toast.makeText(getActivity(), "Add New Card Click", Toast.LENGTH_SHORT).show();
            isClcikCardAdd = true;
            new AddPaymentMethodActivityStarter(this)
                    .startForResult(new AddPaymentMethodActivityStarter.Args.Builder()
                            .setShouldAttachToCustomer(true)
                            .build()
                    );
        });

        binding.cvPayNow.setVisibility(View.GONE);
        binding.cvPayPal.setVisibility(View.GONE);

        binding.cvPayNow.setOnClickListener(view -> selectPayemtMethod("paynow"));
        binding.cvStripe.setOnClickListener(view -> selectPayemtMethod("stripe"));
        binding.cvPayPal.setOnClickListener(view -> selectPayemtMethod("paypal"));

        CreateEventDetailsPostRequest();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStripe = StripeConnect.paymentAuth(getActivity());

        createStripeSession();
        paymentSession = new PaymentSession(this, createPaymentSessionConfig());

        setupPaymentSession();
    }

    private PaymentSessionConfig createPaymentSessionConfig() {

        return new PaymentSessionConfig.Builder()
                .setHiddenShippingInfoFields(
                        ShippingInfoWidget.CustomizableShippingField.Phone
                )
                //  .setBillingAddressFields(BillingAddressFields.PostalCode)
                .setShippingInfoRequired(true)
                .setShippingMethodsRequired(false)
                .build();

    }
    private void createStripeSession() {
        try {
            myLoader.show("");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CustomerSession.initCustomerSession(requireActivity(), new GetEphemeralKey(getActivity(), apiInterface));

        CustomerSession.PaymentMethodsRetrievalListener listener = new CustomerSession.PaymentMethodsRetrievalListener() {

            public void onError(int i, @NotNull String s, @org.jetbrains.annotations.Nullable StripeError stripeError) {
                Log.d("fnalkfnskla", "PaymentMethodsRetrievalListener: " + stripeError);
            }
            @Override
            public void onPaymentMethodsRetrieved(@NotNull List<PaymentMethod> list) {
                myLoader.dismiss();
                Log.d("fnalkfnskla", "onPaymentMethodsRetrieved: " + list.size());
                if (list.size() > 0) {
                    isPaymentMethodAvailable = true;
                } else {
                    isPaymentMethodAvailable = false;
                }
            }
        };


        CustomerSession.CustomerRetrievalListener customerRetrievalListener = new CustomerSession.CustomerRetrievalListener()
        {
            
            public void onError(int i, @NotNull String s, @org.jetbrains.annotations.Nullable StripeError stripeError) {
                Toast.makeText(getActivity(), "" + s, Toast.LENGTH_SHORT).show();
                Log.d("fanlkfnakl", "retrieveCurrentCustomer: " + s);
            }

            @Override
            public void onCustomerRetrieved(@NotNull Customer customer) {
                try {
                    // SessionValidation.getPrefsHelper().savePref(Constants.customer, new Gson().toJson(customer));

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.d("fnalkfnskla", "NullPointerException: " + e.getMessage());
                }
            }
        };
        CustomerSession customerSession = CustomerSession.getInstance();
        customerSession.getPaymentMethods(PaymentMethod.Type.Card, listener);
        customerSession.retrieveCurrentCustomer(customerRetrievalListener);
        customerSession.setCustomerShippingInformation(new ShippingInformation(), customerRetrievalListener);


    }

    private void selectPayemtMethod(String type) {

        binding.ivPayNow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg));
        binding.ivStripe.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg));
        binding.ivPayPal.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg));

        if (type.equalsIgnoreCase("paynow")) {
            binding.ivPayNow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg_select));
        } else if (type.equalsIgnoreCase("stripe")) {
            launchPaymentMethodsActivity();
            binding.ivPayNow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg_select));
        } else if (type.equalsIgnoreCase("paypal")) {
            binding.ivPayPal.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.radio_bg_select));
        }

    }

    public void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.popBackStackImmediate(fragment.getClass().getName(), 0);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.my_nav_host_fragment, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    private void launchPaymentMethodsActivity() {

        isClcikCardAdd = false;

        if (isPaymentMethodAvailable) {
            new PaymentMethodsActivityStarter(this).startForResult(new PaymentMethodsActivityStarter.Args.Builder()
                    .build());
            return;
        }
        //new AddPaymentMethodActivityStarter(this).startForResult();
        new AddPaymentMethodActivityStarter(this)
                .startForResult(new AddPaymentMethodActivityStarter.Args.Builder()
                        .setShouldAttachToCustomer(true)
                        .build()
                );

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            paymentSession.handlePaymentData(requestCode, resultCode, data);
            Log.v("AbCdEf", "onActivityResult handlePaymentData");
        }

        mStripe.onPaymentResult(requestCode, data, new ApiResultCallback<PaymentIntentResult>() {
            @Override
            public void onSuccess(PaymentIntentResult paymentIntentResult) {
                Log.v("AbCdEf", "onActivityResult onPaymentResult");
                myLoader.dismiss();
                // String paymentMethodId = paymentIntentResult.getIntent().getPaymentMethodId();
                String paymentId = paymentIntentResult.getIntent().getId();
                createEventDAO.setPaymentId(paymentId);


//                CreateEventFullEvent minPaymentFragment = new CreateEventFullEvent();
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("eventDAO", createEventDAO);
//                minPaymentFragment.setArguments(bundle);
//                showFragment(minPaymentFragment, "MinPaymentFragment");

                if(Utility.isNetworkAvailable(getContext()))
                    paymentIsDoneAPI();
                //CreateEventDetailsPostRequest();
//                paymentConfirmRequest(paymentId, getPaymentMethod.id);
            }

            @Override
            public void onError(@NotNull Exception e) {
                myLoader.dismiss();
                Log.v("AbCdEf", "Payment failed");
            }
        });
        if (requestCode == PaymentMethodsActivityStarter.REQUEST_CODE) {

            Log.v("AbCdEf", "onActivityResult PaymentMethodsActivityStarter");

            final PaymentMethodsActivityStarter.Result result =
                    PaymentMethodsActivityStarter.Result.fromIntent(data);
            getPaymentMethod = result != null ?
                    result.paymentMethod : null;
            //setupPaymentSession();
            if (getPaymentMethod != null) {
                //  paymentSession.presentPaymentMethodSelection(paymentMethod.id);
                getClientSecret(getPaymentMethod.id);
            }
        } else if (requestCode == AddPaymentMethodActivityStarter.REQUEST_CODE) {

            try {
                AddPaymentMethodActivityStarter.Result result = AddPaymentMethodActivityStarter.Result.fromIntent(data);

                if(result!=null){
                    AddPaymentMethodActivityStarter.Result.Success success = (AddPaymentMethodActivityStarter.Result.Success) AddPaymentMethodActivityStarter.Result.Success.fromIntent(data);

                    Log.v("AbCdEf", "onActivityResult AddPaymentMethodActivityStarter customerId " + success.component1().customerId);
                    Log.v("AbCdEf", "onActivityResult AddPaymentMethodActivityStarter " + success.getPaymentMethod().id);
//            setupPaymentSession();
                    if (success != null && success.getPaymentMethod() != null) {
                        //paymentSession.presentPaymentMethodSelection(paymentMethod.id);
                        getClientSecret(success.getPaymentMethod().id);
                    }

                    if (!isClcikCardAdd) {
                        if (success != null && success.getPaymentMethod() != null) {
                            //paymentSession.presentPaymentMethodSelection(paymentMethod.id);
                            getClientSecret(success.getPaymentMethod().id);
                        }
                    }
                }

            }catch (Exception e){e.printStackTrace();}

        }
    }

    private void setupPaymentSession() {
        paymentSession.init(new PaymentSession.PaymentSessionListener() {
            @Override
            public void onCommunicatingStateChanged(boolean b) {
                if (b) {
                    myLoader.show("Please Wait...");
                } else {
                    myLoader.dismiss();
                }
            }

            @Override
            public void onError(int i, @NotNull String s) {
                myLoader.dismiss();

            }

            @Override
            public void onPaymentSessionDataChanged(@NotNull PaymentSessionData paymentSessionData) {
                myLoader.dismiss();
                getPaymentMethod = paymentSessionData.getPaymentMethod();
                if (getPaymentMethod != null) {
                    //TODO hit book ticket api and get qr code along with post it to ticketPaymentRequest API

                }
            }
        });
    }

    private void CreateEventDetailsPostRequest() {

        myLoader.show("");
        RequestBody amount = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(this.amount));
        RequestBody eventType = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getEventType()));
        RequestBody categoryId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCategoryId()));

        //RequestBody paymentId = RequestBody.create(MediaType.parse("multipart/form-data"), TextUtils.isEmpty(createEventDAO.getPaymentId()) ? "" : createEventDAO.getPaymentId());
        RequestBody paymentId = RequestBody.create(MediaType.parse("multipart/form-data"), TextUtils.isEmpty(createEventDAO.getPaymentId()) ? "" : "");
        RequestBody hostMobile = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostMobile()));
        RequestBody hostAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostAddress()));
        RequestBody websiteUrl = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrl()));
        RequestBody websiteUrlOther = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrlOther()));

        RequestBody subCategoryId = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSubCategoryId());
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getName());
        RequestBody eventOccurrenceType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEventOccuranceType());
        JsonArray occorArray;
        if (createEventDAO.getOccurredOn().length() > 0) {
            String[] arr = createEventDAO.getOccurredOn().substring(1, createEventDAO.getOccurredOn().length() - 1).split(",");
            occorArray = new JsonArray(arr.length);
            for (String s : arr) occorArray.add(Integer.valueOf(s));
        } else {
            occorArray = new JsonArray(1);
            occorArray.add(0);
        }

        RequestBody eventOccuranceOn = RequestBody.create(MediaType.parse("multipart/form-data"), occorArray.toString());
        RequestBody startDate = null;
        RequestBody endDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            Date startDateUtil = sdf.parse(createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());

            //startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(startDateUtil));
            startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));
//            startDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());

            Date endDateUtil = sdf.parse(createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
            //endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(endDateUtil));
            endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
//            endDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc());
        RequestBody description2 = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc2());
        RequestBody paidType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getPaidType());

        RequestBody ticketInfoURL = null, helpLine = null;
        RequestBody sellingStartDate = null, sellingEndDate = null;
        RequestBody willEventAvailableRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(willEventAvailable));

        JsonArray free = null;
        JsonArray paidTicket = null;
        JsonArray paidSeat = null;
        JsonArray vipTicket = null;
        JsonArray vipSeat = null;

        String helplineNumber = createEventDAO.getHelplineNumber();
        String info = createEventDAO.getTicketInfoWhenTicketsNotSelected();

        if (info != null || helplineNumber != null) {
            if (info != null)
                ticketInfoURL = RequestBody.create(MediaType.parse("multipart/form-data"), info);
            if (helplineNumber != null)
                helpLine = RequestBody.create(MediaType.parse("multipart/form-data"), helplineNumber);

        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                if (!TextUtils.isEmpty(createEventDAO.getSellStartDate()) && !TextUtils.isEmpty(createEventDAO.getSellEndDate())){
                    Date startDateUtil = sdf.parse(createEventDAO.getSellStartDate() + " " + createEventDAO.getSellStartTime());
                //sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), startDateUtil.toString());
                sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));
                Date endDateUtil = sdf.parse(createEventDAO.getSellEndDate() + " " + createEventDAO.getSellEndTime());
                //sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), endDateUtil.toString());
                sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
            }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (createEventDAO.isFreeTicketEnabled() == 1 && createEventDAO.getFreeTicketDaoList() != null && createEventDAO.getFreeTicketDaoList().size() > 0) {
                free = new JsonArray(createEventDAO.getFreeTicketDaoList().size());
                for (FreeTicketDao dao : createEventDAO.getFreeTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    free.add(object);
                }
            }

            if (createEventDAO.isRSVPTicketEnabled() == 1 && createEventDAO.getRsvpTicketDaoList() != null && createEventDAO.getRsvpTicketDaoList().size() > 0) {
                paidTicket = new JsonArray(createEventDAO.getRsvpTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getRsvpTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    paidTicket.add(object);
                }
            }

            if (createEventDAO.isVIPTicketEnabled() == 1 && createEventDAO.getVipTicketDaoList() != null && createEventDAO.getVipTicketDaoList().size() > 0) {
                vipTicket = new JsonArray(createEventDAO.getVipTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getVipTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    vipTicket.add(object);
                }
            }

            if (createEventDAO.isVipSeatEnabled() == 1 && createEventDAO.getVipTableAndSeatingDaos() != null && createEventDAO.getVipTableAndSeatingDaos().size() > 0) {
                vipSeat = new JsonArray(createEventDAO.getVipTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getVipTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    vipSeat.add(object);
                }
            }

            if (createEventDAO.isRSVPSeatEnabled() == 1 && createEventDAO.getRsvpTableAndSeatingDaos() != null && createEventDAO.getRsvpTableAndSeatingDaos().size() > 0) {
                paidSeat = new JsonArray(createEventDAO.getRsvpTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getRsvpTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    paidSeat.add(object);
                }
            }
        }

        RequestBody venueAddress = null, venueLat = null, venueLongt = null, veneuName = null, venueId = null, countryCode = null, cityName = null, subVenue = null;

        if (createEventDAO.getLat() == null) {
            venueId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueId()));
            if (createEventDAO.getSelectedSubVenues() != null && createEventDAO.getSelectedSubVenues().size() > 0) {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < createEventDAO.getSelectedSubVenues().size(); i++) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("venueId", createEventDAO.getSelectedSubVenues().get(i).getVenueId());
                    jsonObject.addProperty("subVenueId", createEventDAO.getSelectedSubVenues().get(i).getSubVenueId());
                    jsonObject.addProperty("status", createEventDAO.getSelectedSubVenues().get(i).getStatus());
                    jsonArray.add(jsonObject);
                }
                subVenue = RequestBody.create(MediaType.parse("multipart/form-data"), jsonArray.toString());
            }
        } else {
            veneuName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueName()));
            venueAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueAddress()));
            venueLat = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLat()));
            venueLongt = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLongt()));
            cityName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCityName()));
            countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));
        }

        countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));

        RequestBody freeJsonBody = null;
        if (free != null)
            freeJsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), free.toString());

        RequestBody vipTicketBody = null;
        if (vipTicket != null)
            vipTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipTicket.toString());

        RequestBody vipSeatingBody = null;
        if (vipSeat != null)
            vipSeatingBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipSeat.toString());

        RequestBody paidTicketBody = null;
        if (paidTicket != null)
            paidTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidTicket.toString());

        RequestBody paidSeatBody = null;
        if (paidSeat != null)
            paidSeatBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidSeat.toString());


        MultipartBody.Part[] image = null;
        if (imageList.size() > 0) {
            image = new MultipartBody.Part[imageList.size()];
            try {
                for (int index = 0; index < imageList.size(); index++) {
                    File file = new File(imageList.get(index));
                    Log.e("What i am uploading!", file.toString());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part fileToUploadPart = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                    image[index] = fileToUploadPart;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Call call;

        Log.v("AbCdEf", "EventDaoData> " + createEventDAO.toString());

        if (createEventDAO.getPaidType().equalsIgnoreCase("Paid")) {
            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
            call = apiInterface.postEventPaid(amount,eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,

                    // new parameters
                    paymentId,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);

        } else {

            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "0");

            call = apiInterface.postEventFree(eventType, categoryId, eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);

        }


        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                myLoader.dismiss();
                if (response.isSuccessful()) {
                    try {
                        msgAPI = new JSONObject(response.body().toString()).getString("message");
                        eventID = new JSONObject(response.body().toString()).getJSONObject("data").getString("eventId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   /* App.createEventDAO = null;
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);
                    Dialogs.showActionDialog(getContext(),
                            getString(R.string.app_name),
                            msg,
                            "Done",
                            v1 -> getActivity().finish()
                    );*/

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                    getActivity().finish();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                myLoader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                getActivity().finish();
            }
        });
    }

   /* private void CreateEventDetailsPostRequestOld() {

        Log.v("AbCdEf", "onActivityResult EventPost");

        myLoader.show("");
        RequestBody eventType = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getEventType()));
        RequestBody categoryId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCategoryId()));

        RequestBody paymentId = RequestBody.create(MediaType.parse("multipart/form-data"), TextUtils.isEmpty(createEventDAO.getPaymentId()) ? "" : createEventDAO.getPaymentId());
        RequestBody hostMobile = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostMobile()));
        RequestBody hostAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostAddress()));
        RequestBody websiteUrl = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrl()));
        RequestBody websiteUrlOther = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrlOther()));

        RequestBody subCategoryId = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSubCategoryId());
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getName());
        RequestBody eventOccurrenceType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEventOccuranceType());
        JsonArray occorArray;
        if (createEventDAO.getOccurredOn().length() > 0) {
            String[] arr = createEventDAO.getOccurredOn().substring(1, createEventDAO.getOccurredOn().length() - 1).split(",");
            occorArray = new JsonArray(arr.length);
            for (String s : arr) occorArray.add(Integer.valueOf(s));
        } else {
            occorArray = new JsonArray(1);
            occorArray.add(0);
        }

        RequestBody eventOccuranceOn = RequestBody.create(MediaType.parse("multipart/form-data"), occorArray.toString());
        RequestBody startDate = null;
        RequestBody endDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            Date startDateUtil = sdf.parse(createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());

            //startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(startDateUtil));
            startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));
//            startDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());

            Date endDateUtil = sdf.parse(createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
            //endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(endDateUtil));
            endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
//            endDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc());
        RequestBody description2 = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc2());
        RequestBody paidType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getPaidType());

        RequestBody ticketInfoURL = null, helpLine = null;
        RequestBody sellingStartDate = null, sellingEndDate = null;
        RequestBody willEventAvailableRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(willEventAvailable));


        JsonArray free = null;
        JsonArray paidTicket = null;
        JsonArray paidSeat = null;
        JsonArray vipTicket = null;
        JsonArray vipSeat = null;

        String helplineNumber = createEventDAO.getHelplineNumber();
        String info = createEventDAO.getTicketInfoWhenTicketsNotSelected();

        if (info != null || helplineNumber != null) {
            if (info != null)
                ticketInfoURL = RequestBody.create(MediaType.parse("multipart/form-data"), info);
            if (helplineNumber != null)
                helpLine = RequestBody.create(MediaType.parse("multipart/form-data"), helplineNumber);

        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                Date startDateUtil = sdf.parse(createEventDAO.getSellStartDate() + " " + createEventDAO.getSellStartTime());
                //sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), startDateUtil.toString());
                sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));
                Date endDateUtil = sdf.parse(createEventDAO.getSellEndDate() + " " + createEventDAO.getSellEndTime());
                //sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), endDateUtil.toString());
                sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (createEventDAO.isFreeTicketEnabled() == 1 && createEventDAO.getFreeTicketDaoList() != null && createEventDAO.getFreeTicketDaoList().size() > 0) {
                free = new JsonArray(createEventDAO.getFreeTicketDaoList().size());
                for (FreeTicketDao dao : createEventDAO.getFreeTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    free.add(object);
                }
            }

            if (createEventDAO.isRSVPTicketEnabled() == 1 && createEventDAO.getRsvpTicketDaoList() != null && createEventDAO.getRsvpTicketDaoList().size() > 0) {
                paidTicket = new JsonArray(createEventDAO.getRsvpTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getRsvpTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    paidTicket.add(object);
                }
            }


            if (createEventDAO.isVIPTicketEnabled() == 1 && createEventDAO.getVipTicketDaoList() != null && createEventDAO.getVipTicketDaoList().size() > 0) {
                vipTicket = new JsonArray(createEventDAO.getVipTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getVipTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    vipTicket.add(object);
                }
            }


            if (createEventDAO.isVipSeatEnabled() == 1 && createEventDAO.getVipTableAndSeatingDaos() != null && createEventDAO.getVipTableAndSeatingDaos().size() > 0) {
                vipSeat = new JsonArray(createEventDAO.getVipTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getVipTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    vipSeat.add(object);
                }
            }

            if (createEventDAO.isRSVPSeatEnabled() == 1 && createEventDAO.getRsvpTableAndSeatingDaos() != null && createEventDAO.getRsvpTableAndSeatingDaos().size() > 0) {
                paidSeat = new JsonArray(createEventDAO.getRsvpTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getRsvpTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    paidSeat.add(object);
                }
            }
        }

        RequestBody venueAddress = null, venueLat = null, venueLongt = null, veneuName = null, venueId = null, countryCode = null, cityName = null, subVenue = null;

        if (createEventDAO.getLat() == null) {
            venueId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueId()));
            if (createEventDAO.getSelectedSubVenues() != null && createEventDAO.getSelectedSubVenues().size() > 0) {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < createEventDAO.getSelectedSubVenues().size(); i++) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("venueId", createEventDAO.getSelectedSubVenues().get(i).getVenueId());
                    jsonObject.addProperty("subVenueId", createEventDAO.getSelectedSubVenues().get(i).getSubVenueId());
                    jsonObject.addProperty("status", createEventDAO.getSelectedSubVenues().get(i).getStatus());
                    jsonArray.add(jsonObject);
                }
                subVenue = RequestBody.create(MediaType.parse("multipart/form-data"), jsonArray.toString());
            }
        } else {
            veneuName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueName()));
            venueAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueAddress()));
            venueLat = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLat()));
            venueLongt = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLongt()));
            cityName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCityName()));
            countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));
        }

        countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));

        RequestBody freeJsonBody = null;
        if (free != null)
            freeJsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), free.toString());

        RequestBody vipTicketBody = null;
        if (vipTicket != null)
            vipTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipTicket.toString());

        RequestBody vipSeatingBody = null;
        if (vipSeat != null)
            vipSeatingBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipSeat.toString());

        RequestBody paidTicketBody = null;
        if (paidTicket != null)
            paidTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidTicket.toString());

        RequestBody paidSeatBody = null;
        if (paidSeat != null)
            paidSeatBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidSeat.toString());


        MultipartBody.Part[] image = null;
        if (imageList.size() > 0) {
            image = new MultipartBody.Part[imageList.size()];
            try {
                for (int index = 0; index < imageList.size(); index++) {
                    File file = new File(imageList.get(index));
                    Log.e("What i am uploading!", file.toString());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part fileToUploadPart = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                    image[index] = fileToUploadPart;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Call call;

        Log.v("AbCdEf", "EventDaoData> " + createEventDAO.toString());

        if (createEventDAO.getPaidType().equalsIgnoreCase("Paid")) {
            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
            call = apiInterface.postEventPaid(eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,

                    // new parameters
                    paymentId,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);

        } else {

            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "0");

            call = apiInterface.postEventFree(eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);

        }


        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {

                myLoader.dismiss();
                if (response.isSuccessful()) {
                    String msg = "";
                    try {
                        msg = new JSONObject(response.body().toString()).getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    App.createEventDAO = null;
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);
                    Dialogs.showActionDialog(getContext(),
                            getString(R.string.app_name),
                            msg,
                            "Done",
                            v1 -> getActivity().finish()
                    );

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                myLoader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }*/

    private void paymentIsDoneAPI() {

        myLoader.show("");

        if(eventID.equalsIgnoreCase("")){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    myLoader.dismiss();
                    paymentIsDoneAPI();
                }
            }, 4000);
        }else {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("paymentId", createEventDAO.getPaymentId());
            jsonObject.addProperty("eventId", eventID);

            Call<JsonElement> call = apiInterface.paidEventPayment(jsonObject);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    myLoader.dismiss();
                    if (response.isSuccessful()) {
                        try {
                            App.createEventDAO = null;
                            Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                            Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);
                            Dialogs.showActionDialog(getContext(),
                                    getString(R.string.app_name),
                                    msgAPI,
                                    "Done",
                                    v1 -> getActivity().finish()
                            );

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                        getActivity().finish();
                    }

                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    t.printStackTrace();
                    myLoader.dismiss();
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                    getActivity().finish();
                }
            });
        }
    }

    private void getClientSecret(String paymentMethodId) {

        Log.v("AbCdEf", "onActivityResult getClientSecret");

        myLoader.show("");

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("amount", amount);
        jsonObject.addProperty("currency", "usd");
        jsonObject.addProperty("paymentMethod", paymentMethodId);

        Call call = apiInterface.getClientSecret(jsonObject);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                myLoader.dismiss();

                Log.e("got result", "yes");
                Log.e("got result", "getClientSecret response> " + response);

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());

//                        String clientSecretId = OBJ.getJSONObject("data").getString("client_secret");
                        String clientSecretId = OBJ.getString("data");
                        Log.e("AbCdEf", "clientSecretId> " + clientSecretId);

                        createPaymentIntent(clientSecretId, paymentMethodId);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                myLoader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

    private void createPaymentIntent(String clientSecret, String paymentMethodId) {
        myLoader.show("Please Wait...");
        mStripe.confirmPayment(this,
                ConfirmPaymentIntentParams.createWithPaymentMethodId(paymentMethodId, clientSecret));
    }

}