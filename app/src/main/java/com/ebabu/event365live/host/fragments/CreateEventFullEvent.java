package com.ebabu.event365live.host.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.VenuePicInEventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentCreateEventFullEventBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.FreeTicketDao;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.RSVPTicketDao;
import com.ebabu.event365live.host.entities.RegularTicketDao;
import com.ebabu.event365live.host.entities.RsvpVipTicketDao;
import com.ebabu.event365live.host.entities.TableAndSeatingDao;
import com.ebabu.event365live.host.entities.TableSeatingTicketDao;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.datatransport.Event;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEventFullEvent extends Fragment {

    final int[] oldPos = new int[1];
    final int[] newPos = new int[1];
    @Inject
    ApiInterface apiInterface;

    private CreateEventDAO createEventDAO;
    private VenuePicInEventAdapter galleryAdapter;
    private FragmentCreateEventFullEventBinding binding;
    private MyLoader loader;
    private List<String> imageList;

    // ImageView Drag & Drop
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            oldPos[0] = viewHolder.getAdapterPosition();
            newPos[0] = target.getAdapterPosition();
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            moveItem(oldPos[0], newPos[0]);
        }
    };
    private boolean willEventAvailable = true;

    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_event_full_event, container, false);
        App.getAppComponent().inject(this);
        loader = new MyLoader(getContext());
        if (getArguments() != null) {
            createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();
        }
        if (createEventDAO != null) {
            if (createEventDAO.getEventOccuranceType().equalsIgnoreCase("oneTime"))
                binding.typeSpinner.setText("One Time");
            else
                binding.typeSpinner.setText(createEventDAO.getEventOccuranceType());
        }
        binding.sellSwitch.setVisibility(View.INVISIBLE);
        binding.sellSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            willEventAvailable = isChecked;
        });
        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());

        String info = createEventDAO.getTicketInfoWhenTicketsNotSelected();
        binding.catId.setText(createEventDAO.getCatIDSubID());
        if (info != null || createEventDAO.getHelplineNumber() != null) {
            //Only Event is posted But Ticket is not Created
            binding.switchLayout.setVisibility(View.GONE);
            binding.linearLayout2.setVisibility(View.GONE);
            binding.ticketInfoLayout.setVisibility(View.VISIBLE);
            binding.ticketParent.setVisibility(View.GONE);
            binding.contactChipGroup.setVisibility(View.GONE);
            if (info != null) {
                if (createEventDAO.getHelplineNumber() != null)
                    info = info + "\n" + createEventDAO.getHelplineNumber();
            } else info = createEventDAO.getHelplineNumber();
            binding.ticketInfoTv.setText(info);
        } else {
            binding.paidType.setText(createEventDAO.getPaidType() + " Event Type");
            //Event is not posted, Ticket is created
            if (createEventDAO.isFreeTicketEnabled() == 1 && createEventDAO.getFreeTicketDaoList() != null) {
                for (FreeTicketDao dao : createEventDAO.getFreeTicketDaoList()) {
                    View itemView = inflater.inflate(R.layout.ticket_detail_layout, binding.ticketParent, false);
                    ((Chip) itemView.findViewById(R.id.title_chip)).setText("Free Tickets");
                    ((TextView) itemView.findViewById(R.id.ticket_title)).setText(dao.getTicketName());
                    itemView.findViewById(R.id.price_per_table).setVisibility(View.INVISIBLE);
                    ((TextView) itemView.findViewById(R.id.no_of_table_tv)).setText("Total Ticket - " + dao.getTotalTicketsQuantity());
                    binding.ticketParent.addView(itemView);
                }
            }

            if (createEventDAO.isRSVPTicketEnabled() == 1 && createEventDAO.getRsvpTicketDaoList() != null) {
                for (RSVPTicketDao dao : createEventDAO.getRsvpTicketDaoList()) {
                    View itemView = inflater.inflate(R.layout.ticket_detail_layout, binding.ticketParent, false);
                    ((Chip) itemView.findViewById(R.id.title_chip)).setText("RSVP Tickets");
                    ((TextView) itemView.findViewById(R.id.ticket_title)).setText(dao.getTicketName());
                    ((TextView) itemView.findViewById(R.id.price_per_table)).setText(dao.getPriceTicket() + " / Ticket");
                    ((TextView) itemView.findViewById(R.id.no_of_table_tv)).setText("Total Tickets - " + dao.getTotalTicketsQuantity());


                    ((TextView) itemView.findViewById(R.id.ticket_cancellation_charges)).setText("( Cancellation Charges : " + dao.getCancellationChargesTicket() + " % )");


                    binding.ticketParent.addView(itemView);
                }
            }

            if (createEventDAO.isRSVPSeatEnabled() == 1 && createEventDAO.getRsvpTableAndSeatingDaos() != null) {
                for (TableAndSeatingDao dao : createEventDAO.getRsvpTableAndSeatingDaos()) {
                    View itemView = inflater.inflate(R.layout.ticket_detail_layout, binding.ticketParent, false);
                    ((Chip) itemView.findViewById(R.id.title_chip)).setText("RSVP Table and Seatings");
                    ((TextView) itemView.findViewById(R.id.ticket_title)).setText(dao.getCategoryName());
                    ((TextView) itemView.findViewById(R.id.price_per_table)).setText(dao.getPricePerTable() + " / Table");
                    ((TextView) itemView.findViewById(R.id.no_of_table_tv)).setText("No of Tables - " + dao.getNoOfTables());


                    ((TextView) itemView.findViewById(R.id.ticket_cancellation_charges)).setText("( Cancellation Charges : " + dao.getCancellationChargesTable() + " % )");

                    binding.ticketParent.addView(itemView);
                }
            }

            if (createEventDAO.isVIPTicketEnabled() == 1 && createEventDAO.getVipTicketDaoList() != null) {
                for (RSVPTicketDao dao : createEventDAO.getVipTicketDaoList()) {
                    View itemView = inflater.inflate(R.layout.ticket_detail_layout, binding.ticketParent, false);
                    ((Chip) itemView.findViewById(R.id.title_chip)).setText("VIP Tickets");
                    ((TextView) itemView.findViewById(R.id.ticket_title)).setText(dao.getTicketName());
                    ((TextView) itemView.findViewById(R.id.price_per_table)).setText(dao.getPriceTicket() + " / Ticket");
                    ((TextView) itemView.findViewById(R.id.no_of_table_tv)).setText("Total Tickets - " + dao.getTotalTicketsQuantity());

                    ((TextView) itemView.findViewById(R.id.ticket_cancellation_charges)).setText("( Cancellation Charges : " + dao.getCancellationChargesTicket() + " % )");

                    binding.ticketParent.addView(itemView);
                }
            }

            if (createEventDAO.isVipSeatEnabled() == 1 && createEventDAO.getVipTableAndSeatingDaos() != null) {
                for (TableAndSeatingDao dao : createEventDAO.getVipTableAndSeatingDaos()) {
                    View itemView = inflater.inflate(R.layout.ticket_detail_layout, binding.ticketParent, false);
                    ((Chip) itemView.findViewById(R.id.title_chip)).setText("VIP Table and Seatings");
                    ((TextView) itemView.findViewById(R.id.ticket_title)).setText(dao.getCategoryName());
                    ((TextView) itemView.findViewById(R.id.price_per_table)).setText(dao.getPricePerTable() + " / Table");
                    ((TextView) itemView.findViewById(R.id.no_of_table_tv)).setText("No of Tables - " + dao.getNoOfTables());

                    ((TextView) itemView.findViewById(R.id.ticket_cancellation_charges)).setText("( Cancellation Charges : " + dao.getCancellationChargesTable() + " % )");

                    binding.ticketParent.addView(itemView);
                }
            }
        }

        if (createEventDAO.getVenueImageList() != null && createEventDAO.getVenueImageList().size() > 0) {
            VenuePicInEventAdapter venuePicInEventAdapter = new VenuePicInEventAdapter(false);
            binding.venueRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.venueRv.setHasFixedSize(true);
            binding.venueRv.setAdapter(venuePicInEventAdapter);
            venuePicInEventAdapter.refresh(createEventDAO.getVenueImageList());
        }

        if (createEventDAO != null && createEventDAO.getImageDAOList().size() > 0) {
            galleryAdapter = new VenuePicInEventAdapter(true);
            binding.horizontalView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.horizontalView.setHasFixedSize(true);
            binding.horizontalView.setAdapter(galleryAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(binding.horizontalView);
            imageList = new ArrayList<>();
            for (ImageDAO imageDAO : createEventDAO.getImageDAOList()) {
                imageList.add(imageDAO.getVenueImages());
            }
            galleryAdapter.refresh(imageList);
        }
        Log.e("eventDAO", createEventDAO.toString());

        if (createEventDAO.getLat() == null) {
            binding.locVenueTv.setText(getString(R.string.venue_name));
            binding.venueLocEt.setText(createEventDAO.getVenueName());
        } else {
            binding.locVenueTv.setText(getString(R.string.venue_addrs));
            binding.venueLocEt.setText(createEventDAO.getVenueAddress());
        }

        binding.submitTicket.setOnClickListener(view -> CreateEventDetailsPostRequest());
        binding.setDao(createEventDAO);
        return binding.getRoot();
    }

    private void CreateEventDetailsPostRequest() {
        loader.show("");
        RequestBody eventType = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getEventType()));
        RequestBody categoryId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCategoryId()));

        RequestBody paymentId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getPaymentId()));
        RequestBody hostMobile = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostMobile()));
        RequestBody hostAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostAddress()));
        RequestBody websiteUrl = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrl()));

        RequestBody subCategoryId = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSubCategoryId());
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getName());
        RequestBody eventOccurrenceType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEventOccuranceType());
        JsonArray occorArray;
        if (createEventDAO.getOccurredOn().length() > 0) {
            String[] arr = createEventDAO.getOccurredOn().substring(1, createEventDAO.getOccurredOn().length() - 1).split(",");
            Log.d("fmkalsm", "CreateEventDetailsPostRequest: " + arr.toString());
            occorArray = new JsonArray(arr.length);
            for (String s : arr) occorArray.add(Integer.valueOf(s));
        } else {
            occorArray = new JsonArray(1);
            occorArray.add(0);
        }

        RequestBody eventOccuranceOn = RequestBody.create(MediaType.parse("multipart/form-data"), occorArray.toString());
        RequestBody startDate=null;
        RequestBody endDate=null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            Date startDateUtil = sdf.parse(createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());
            startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));

            Date endDateUtil = sdf.parse(createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
            endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc());
        RequestBody description2 = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc2());
        RequestBody paidType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getPaidType());
        startDate = RequestBody.create(MediaType.parse("multipart/form-data"),createEventDAO.getStartDate());
        endDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEndDate());

        RequestBody ticketInfoURL = null, helpLine = null;
        RequestBody sellingStartDate = null, sellingEndDate = null;
        RequestBody willEventAvailableRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(willEventAvailable));


        JsonArray regularFree = null;
        JsonArray rsvpTicket = null;
        JsonArray regularPaidTicket = null;
        JsonArray vipTicket = null;
        JsonArray tableSeating = null;

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
                if (!TextUtils.isEmpty(createEventDAO.getSellStartDate()) && !TextUtils.isEmpty(createEventDAO.getSellEndDate())) {
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

            if (createEventDAO.getFreeRegularTicketDaoList() != null && createEventDAO.getFreeRegularTicketDaoList().size() > 0) {
                regularFree = new JsonArray();
                regularPaidTicket = new JsonArray();

                for (RegularTicketDao dao : createEventDAO.getFreeRegularTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("totalQuantity", dao.getTicketQuantity());
                    object.addProperty("description", dao.getDescription().trim());
                    object.addProperty("discount",dao.getDiscountPercentage());
                    object.addProperty("ticketSellingDays",dao.getTicketsellingdays());
                    object.addProperty("eventDay",dao.getEventDay());
                    object.addProperty("sellingStartDate", dao.getSellingStartDate().trim());
                    object.addProperty("sellingStartTime", dao.getSellingStartTime().trim());
                    object.addProperty("sellingEndDate", dao.getSellingEndDate().trim());
                    object.addProperty("sellingEndTime", dao.getSellingEndTime().trim());

                    if (dao.getTicketType().equalsIgnoreCase("Paid")) {
                        object.addProperty("cancellationChargeInPer", dao.getCancellationCharge() + "");
                        object.addProperty("pricePerTicket", dao.getPricePerTicket() + "");
                        if (dao.getDiscountPercentage() != 0.0)
                            object.addProperty("discount", dao.getDiscountPercentage());
                        regularPaidTicket.add(object);
                    } else if (dao.getTicketType().equalsIgnoreCase("Free")) {
                        regularFree.add(object);
                    }
                }
            }

            if (createEventDAO.getRsvpVipTicketDaoList() != null && createEventDAO.getRsvpVipTicketDaoList().size() > 0) {
                rsvpTicket = new JsonArray();
                vipTicket = new JsonArray();

                for (RsvpVipTicketDao dao : createEventDAO.getRsvpVipTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("totalQuantity", dao.getTicketQuantity());
                    object.addProperty("description", dao.getDescription().trim());

                    object.addProperty("sellingStartDate", dao.getSellingStartDate());
                    object.addProperty("sellingStartTime", dao.getSellingStartTime());
                    object.addProperty("sellingEndDate", dao.getSellingEndDate());
                    object.addProperty("sellingEndTime", dao.getSellingEndTime());

                    if (dao.getTicketType().equalsIgnoreCase("VIP")) {
                        object.addProperty("pricePerTicket", dao.getPricePerTicket());
                        object.addProperty("cancellationChargeInPer", dao.getCancellationCharge() + "");
                        if (dao.getDiscountPercentage() != 0.0)
                            object.addProperty("discount", dao.getDiscountPercentage());
                        vipTicket.add(object);
                    } else if (dao.getTicketType().equalsIgnoreCase("RSVP")) {
                        rsvpTicket.add(object);
                    }
                }
            }

            if (createEventDAO.getTableSeatingTicketDaoList() != null && createEventDAO.getTableSeatingTicketDaoList().size() > 0) {
                tableSeating = new JsonArray();

                for (TableSeatingTicketDao dao : createEventDAO.getTableSeatingTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("pricePerTicket", dao.getPricePerTicket());
                    object.addProperty("description", dao.getDescription());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("noOfTables", dao.getNoOfTables() + "");
                    object.addProperty("cancellationChargeInPer", dao.getCancellationCharge());
                    object.addProperty("sellingStartDate", dao.getSellingStartDate().trim());
                    object.addProperty("sellingStartTime", dao.getSellingStartTime().trim());
                    object.addProperty("sellingEndDate", dao.getSellingEndDate().trim());
                    object.addProperty("sellingEndTime", dao.getSellingEndTime().trim());
                    if (dao.getDiscountPercentage() != 0.0) {
                        object.addProperty("discount", dao.getDiscountPercentage());
                    }
//                    object.addProperty("pricePerTable", dao.getPricePerTable());
//                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    tableSeating.add(object);
                }
            }
        }

        RequestBody venueAddress = null, venueLat = null, venueLongt = null, veneuName = null, venueId = null, countryCode = null, cityName = null, subVenue = null;
        RequestBody regularPaidBody = null;
        if (regularPaidTicket != null)
            regularPaidBody = RequestBody.create(MediaType.parse("multipart/form-data"), regularPaidTicket.toString());

        RequestBody regularFreeJsonBody = null;
        if (regularFree != null)
            regularFreeJsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), regularFree.toString());
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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "0");

        // Map object
        Map<String, RequestBody> map = new HashMap<>();
        map.put("eventType", eventType);
        map.put("categoryId", categoryId);
        map.put("name", name);
        map.put("subCategoryId", subCategoryId);
        map.put("eventOccurrenceType", eventOccurrenceType);
        map.put("occurredOn", eventOccuranceOn);
        map.put("start", startDate);
        map.put("end", endDate);
        map.put("description", description);
        map.put("description2", description2);
        map.put("paidType", paidType);
        map.put("isEventPaid", isEventPaid);
        map.put("venueId", venueId);
        map.put("SubVenue", subVenue);
        map.put("free", regularFreeJsonBody);
        map.put("regularPaid", regularPaidBody);

        if (createEventDAO.getSellingStartDate() != null && !createEventDAO.getSellingStartDate().equals("null") )
        {
            sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSellingStartDate());
            map.put("sellingStart", sellingStartDate);
        }

        if (createEventDAO.getSellingEndDate() != null && !createEventDAO.getSellingEndDate().equals("null") )
        {
            sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSellingEndDate());
            map.put("sellingEnd", sellingEndDate);
        }

        if (createEventDAO.getHelplineNumber() != null && !createEventDAO.getHelplineNumber().equals("null")) {
            helpLine = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getHelplineNumber().toString());
            map.put("eventHelpLine", helpLine);
        }

        if (createEventDAO.getTicketInfoWhenTicketsNotSelected() != null && !createEventDAO.getTicketInfoWhenTicketsNotSelected().equals("null")) {
            ticketInfoURL = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getTicketInfoWhenTicketsNotSelected());
            map.put("ticketInfoURL", ticketInfoURL);
        }

        if (createEventDAO.getCityName() != null && !createEventDAO.getCityName().equals("null")) {
            cityName = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getCityName());
            map.put("city", cityName);
        }

        if (createEventDAO.getCountryCode() != null && !createEventDAO.getCountryCode().equals("null")) {
            countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getCountryCode());
            map.put("countryCode", countryCode);
        }

        if (createEventDAO.getLongt() != null && !createEventDAO.getLongt().equals("null")) {
            venueLongt = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getLongt());
            map.put("venueLongitude", venueLongt);
        }
        String venueid= String.valueOf(createEventDAO.getVenueId());
        if (venueid!=null && !createEventDAO.getHelplineNumber().equals("null"))
        {
            venueId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueId()));
            map.put("venueId", venueId);
        }

        if (createEventDAO.getVenueAddress() != null && !createEventDAO.getVenueAddress().equals("null")) {
            venueAddress = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getVenueAddress());
            map.put("venueAddress", venueAddress);
        }

        if (createEventDAO.getLat() != null && !createEventDAO.getLat().equals("null")) {
            venueLat = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getLat());
            map.put("venueLatitude", venueLat);
        }
        RequestBody vipTicketBody = null;
        if (createEventDAO.getVipTicketDaoList() != null && !createEventDAO.getVipTicketDaoList().equals("null")) {
            vipTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVipTicketDaoList()));
            map.put("vipSeatings", vipTicketBody);
        }
        RequestBody tableSeatingBody = null;
        if (createEventDAO.getTableSeatingTicketDaoList() != null && !createEventDAO.getTableSeatingTicketDaoList().equals("null")) {
            tableSeatingBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getTableSeatingTicketDaoList()));
            map.put("tableSeatings", tableSeatingBody);
        }
        RequestBody rsvpTicketBody = null;
        if (createEventDAO.getRsvpVipTicketDaoList() != null && !createEventDAO.getRsvpTicketDaoList().equals("null")) {
            rsvpTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getRsvpTicketDaoList()));
            map.put("regularSeatings", rsvpTicketBody);
        }


//        if (imageList != null)
//        {
//            for (int index=0;index<imageList.size();index++)
//            {
//            File file = new File(imageList.get(index));
//            RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
//            map.put("file\"; filename=\"pp.png\"", fileBody);
//            }
//        }

        Call<Event> call = apiInterface.postEventTicket(map,image);
        call.enqueue(new Callback<Event>()
        {
           @Override
           public void onResponse(Call<Event> call, Response<Event> response)
           {
              loader.dismiss();
              if (response.isSuccessful())
              {
                 String msg = "";
                 try
                 {
                   msg = new JSONObject(response.body().toString()).getString("message");
                 }
                 catch (JSONException e)
                 {
                   e.printStackTrace();
                 }
                App.createEventDAO = null;
                Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);
                Dialogs.showActionDialog(getContext(),
                getString(R.string.app_name),
                msg,
                "Done",
                v1 -> getActivity().finish());
                }
                    else
                    {
                     Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                    }
           }
           @Override
           public void onFailure(Call<Event> call, Throwable t)
           {
             t.printStackTrace();
             loader.dismiss();
             Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
           }
        });
//        Call call = apiInterface.postEventTicket(eventType,
//                categoryId, subCategoryId,
//                name,
//                eventOccurrenceType, eventOccuranceOn,
//                startDate, endDate,
//                venueId, veneuName, venueAddress,
//                venueLat, venueLongt,
//                countryCode,
//                cityName,
//                description,
//                description2,
//                paidType,
//                vipTicketBody, tableSeatingBody, rsvpTicketBody, regularPaidBody, regularFreeJsonBody,
//                ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,
//                isEventPaid,
//                subVenue,
//                image);
//
//        call.enqueue(new Callback() {
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) {
//                loader.dismiss();
//                if (response.isSuccessful()) {
//                    String msg = "";
//                    try {
//                        msg = new JSONObject(response.body().toString()).getString("message");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    App.createEventDAO = null;
//
//                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
//                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);
//                    Dialogs.showActionDialog(getContext(),
//                            getString(R.string.app_name),
//                            msg,
//                            "Done",
//                            v1 -> getActivity().finish()
//                    );
//
//                } else {
//                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call call, Throwable t) {
//                t.printStackTrace();
//                loader.dismiss();
//                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
//            }
//        });
    }

    private void moveItem(int oldPos, int newPos) {
        if (newPos == 0) {
            String temp = imageList.get(oldPos);
            imageList.set(oldPos, imageList.get(newPos));
            imageList.set(newPos, temp);
            galleryAdapter.notifyItemChanged(oldPos);
            galleryAdapter.notifyItemChanged(newPos);
        }
    }
}
