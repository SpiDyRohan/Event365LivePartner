package com.ebabu.event365live.host.fragments.ticket;
import static com.ebabu.event365live.host.utils.DateUtils.convertLocalToUTC;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.VenuePicInEventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentFullEventDetailBinding;
import com.ebabu.event365live.host.databinding.LayoutTicketDetailFullBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.RegularTicketDao;
import com.ebabu.event365live.host.entities.RsvpVipTicketDao;
import com.ebabu.event365live.host.entities.TableSeatingTicketDao;
import com.ebabu.event365live.host.entities.UpdateEventDao;
import com.ebabu.event365live.host.fragments.CreateEventFragmentArgs;
import com.ebabu.event365live.host.utils.ConcaveRoundedCornerTreatment;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.datatransport.Event;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import androidx.annotation.Nullable;
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

public class FullEventDetailFragment extends Fragment {
    final int[] oldPos = new int[1];
    final int[] newPos = new int[1];
    @Inject
    ApiInterface apiInterface;
    private CreateEventDAO createEventDAO;
    private VenuePicInEventAdapter galleryAdapter;
    private FragmentFullEventDetailBinding binding;
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
    private LayoutInflater inflater;
    private boolean willEventAvailable = true;

    @SuppressLint("SimpleDateFormat")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_full_event_detail, container, false);
        this.inflater = inflater;
        App.getAppComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();
        }
        initView();
    }

    private void initView() {

        loader = new MyLoader(getContext());
        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());
        binding.submitTicket.setOnClickListener(view -> CreateEventDetailsPostRequest());
        try {
            if (createEventDAO != null) {
                if (createEventDAO.getEventOccuranceType().equals("oneTime"))
                    binding.typeSpinner.setText("One Time");
                else
                    binding.typeSpinner.setText(createEventDAO.getEventOccuranceType());
            }

            binding.catId.setText(createEventDAO.getCatIDSubID());

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (createEventDAO.getFreeRegularTicketDaoList() != null) {
            for (RegularTicketDao dao : createEventDAO.getFreeRegularTicketDaoList()) {

                LayoutTicketDetailFullBinding regularBinding = DataBindingUtil.inflate(inflater, R.layout.layout_ticket_detail_full, binding.ticketParent, false);


                regularBinding.tvRegularTicketName.setText(dao.getTicketName());
                regularBinding.tvTotalTickets.setText("Total Tickets - " + dao.getTicketQuantity());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";
                if (dao.getTicketType().equals("Paid")) {
                    ticketTypeText = "Paid Tickets";
                    ticketPriceText = "$ " + dao.getPricePerTicket() + " / Ticket";
                    minimumChargeText = "Cancellation Charge : " + dao.getCancellationCharge() + "%";
                    regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(dao.getDiscountPercentage())) + "%)");
                } else if (dao.getTicketType().equals("Free")) {
                    ticketTypeText = "Free Tickets";
                    ticketPriceText = "$ 00.00 / Ticket";
                    minimumChargeText = "Cancellation Charge : 0%";
                    regularBinding.discountLabel.setVisibility(View.GONE);
                }

                regularBinding.tvTicketType.setText(ticketTypeText);
                regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");

              /*  DateUtils.convertUTCToLocal(dao.getSellingStartDate(),dao.getSellingStartTime(),regularBinding.tvStartDate,regularBinding.tvStartTime);
                DateUtils.convertUTCToLocal(dao.getSellingEndDate(),dao.getSellingEndTime(),regularBinding.tvEndDate,regularBinding.tvEndTime);*/

                regularBinding.tvStartDate.setText(dao.getSellingStartDate());
                regularBinding.tvStartTime.setText(dao.getSellingStartTime());

                regularBinding.tvEndDate.setText(dao.getSellingEndDate());
                regularBinding.tvEndTime.setText(dao.getSellingEndTime());


                binding.ticketParent.addView(regularBinding.getRoot());
            }
        }

        if (createEventDAO.getRsvpVipTicketDaoList() != null) {
            for (RsvpVipTicketDao dao : createEventDAO.getRsvpVipTicketDaoList()) {
                LayoutTicketDetailFullBinding rspVipBinding = DataBindingUtil.inflate(inflater, R.layout.layout_ticket_detail_full, binding.ticketParent, false);

                rspVipBinding.tvRegularTicketName.setText(dao.getTicketName());
                rspVipBinding.tvTotalTickets.setText("Total Tickets - " + dao.getTicketQuantity());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";
                if (dao.getTicketType().equals("VIP")) {
                    rspVipBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(dao.getDiscountPercentage())) + "%)");
                    rspVipBinding.linearLayoutDate.setVisibility(View.VISIBLE);
                    ticketTypeText = "VIP Tickets";
                    ticketPriceText = "$ " + dao.getPricePerTicket() + " / Ticket";
                    minimumChargeText = "Cancellation Charge : " + dao.getCancellationCharge() + "%";


                   /* DateUtils.convertUTCToLocal(dao.getSellingStartDate(),dao.getSellingStartTime(),rspVipBinding.tvStartDate,rspVipBinding.tvStartTime);
                    DateUtils.convertUTCToLocal(dao.getSellingEndDate(),dao.getSellingEndTime(),rspVipBinding.tvEndDate,rspVipBinding.tvEndTime);*/

                    rspVipBinding.tvStartDate.setText(dao.getSellingStartDate());
                    rspVipBinding.tvEndDate.setText(dao.getSellingEndDate());
                    rspVipBinding.tvStartTime.setText(dao.getSellingStartTime());
                    rspVipBinding.tvEndTime.setText(dao.getSellingEndTime());

                } else if (dao.getTicketType().equals("RSVP")) {
                    rspVipBinding.linearLayoutDate.setVisibility(View.GONE);
                    rspVipBinding.discountLabel.setVisibility(View.GONE);
                    ticketTypeText = "RSVP Tickets";
                    ticketPriceText = "$ 00.00 / Ticket";
                    minimumChargeText = "Cancellation Charge : 0%";
                }

                rspVipBinding.tvTicketType.setText(ticketTypeText);
                rspVipBinding.tvPerTicketPrice.setText(ticketPriceText);
                rspVipBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");

                binding.ticketParent.addView(rspVipBinding.getRoot());
            }
        }

        if (createEventDAO.getTableSeatingTicketDaoList() != null) {
            for (TableSeatingTicketDao dao : createEventDAO.getTableSeatingTicketDaoList()) {
                LayoutTicketDetailFullBinding tabSeatBinding = DataBindingUtil.inflate(inflater, R.layout.layout_ticket_detail_full, binding.ticketParent, false);


                tabSeatBinding.tvTotalTickets.setVisibility(View.GONE);
                tabSeatBinding.tvPersonPerTable.setVisibility(View.VISIBLE);
                tabSeatBinding.tvTotalTable.setVisibility(View.VISIBLE);
                tabSeatBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(dao.getDiscountPercentage())) + "%)");
                tabSeatBinding.tvRegularTicketName.setText(dao.getTicketName());
                tabSeatBinding.tvPersonPerTable.setText("Person Per Tables - " + dao.getPersonPerTable());
                tabSeatBinding.tvTotalTable.setText("Total Tables - " + dao.getNoOfTables());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";

                ticketTypeText = "Table & Seatings";
                ticketPriceText = "$ " + dao.getPricePerTicket() + " / Ticket";
                minimumChargeText = "Cancellation Charge : " + dao.getCancellationCharge() + "%";

                tabSeatBinding.tvTicketType.setText(ticketTypeText);
                tabSeatBinding.tvPerTicketPrice.setText(ticketPriceText);
                tabSeatBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");

               /* DateUtils.convertUTCToLocal(dao.getSellingStartDate(),dao.getSellingStartTime(),tabSeatBinding.tvStartDate,tabSeatBinding.tvStartTime);
                DateUtils.convertUTCToLocal(dao.getSellingEndDate(),dao.getSellingEndTime(),tabSeatBinding.tvEndDate,tabSeatBinding.tvEndTime);*/

                tabSeatBinding.tvStartDate.setText(dao.getSellingStartDate());
                tabSeatBinding.tvEndDate.setText(dao.getSellingEndDate());
                tabSeatBinding.tvStartTime.setText(dao.getSellingStartTime());
                tabSeatBinding.tvEndTime.setText(dao.getSellingEndTime());

                binding.ticketParent.addView(tabSeatBinding.getRoot());
            }
        }

        setTicketPreview();

        binding.setDao(createEventDAO);
    }

    private void setTicketPreview() {
        binding.tvTicketDateTime.setText(createEventDAO.getStartDate() + " | " + createEventDAO.getStartTime() + " - " + createEventDAO.getEndTime());
        Glide.with(this).load(createEventDAO.getImageDAOList().get(0).getVenueImages()).into(binding.ivBackground);
        float cornerSize = binding.ivBackground.getResources().getDimension(R.dimen._12sdp);
        binding.ivBackground.setShapeAppearanceModel(ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerSize)
                .setTopRightCorner(new ConcaveRoundedCornerTreatment())
                .setTopLeftCorner(new ConcaveRoundedCornerTreatment())
                .setBottomRightCorner(new ConcaveRoundedCornerTreatment())
                .setBottomLeftCorner(new ConcaveRoundedCornerTreatment())
                .build());
    }

    private void CreateEventDetailsPostRequest() {
        loader.show("");
        RequestBody eventType = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getEventType()));
        RequestBody categoryId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getCategoryId()));
        RequestBody cityName = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getCityName()));
        RequestBody paymentId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getPaymentId()));
        RequestBody hostMobile = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getHostMobile()));
        RequestBody hostAddress = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getHostAddress()));
        RequestBody websiteUrl = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getWebsiteUrl()));

        RequestBody subCategoryId = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getSubCategoryId());
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getName());
        RequestBody eventOccurrenceType = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getEventOccuranceType());
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

        RequestBody eventOccuranceOn = RequestBody.create(MediaType.parse("text/plain"), occorArray.toString());
        RequestBody startDate = null;
        RequestBody endDate = null;
        RequestBody sellingStartDate = null,sellingEndDate=null;

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            Date startDateUtil = sdf.parse(createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());
            startDate = RequestBody.create(MediaType.parse("text/plain"), Utility.localFormatUTC(startDateUtil));

            Date endDateUtil = sdf.parse(createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
            endDate = RequestBody.create(MediaType.parse("text/plain"), Utility.localFormatUTC(endDateUtil));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getDesc());
        RequestBody description2 = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getDesc2());
        RequestBody paidType = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getPaidType());
//        if (eventOccurrenceType.equals("oneTime")||eventOccurrenceType.equals("monthly"))
//        {
            startDate = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());
            endDate = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
//        }
//        if (eventOccurrenceType.equals("daily")||eventOccurrenceType.equals("weekly"))
//        {
//            startDate = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());
//            endDate = RequestBody.create(MediaType.parse("text/plain"), createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
//        }
        RequestBody ticketInfoURL = null, helpLine = null;
//        RequestBody sellingStartDate = null, sellingEndDate = null;
        RequestBody willEventAvailableRequestBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(willEventAvailable));


        JsonArray regularFree = null;
        JsonArray rsvpTicket = null;
        JsonArray regularPaidTicket = null;
        JsonArray vipTicket = null;
        JsonArray tableSeating = null;

        String helplineNumber = createEventDAO.getHelplineNumber();
        String info = createEventDAO.getTicketInfoWhenTicketsNotSelected();

        if (info != null || helplineNumber != null) {
            if (info != null)
                ticketInfoURL = RequestBody.create(MediaType.parse("text/plain"), info);
            if (helplineNumber != null)
                helpLine = RequestBody.create(MediaType.parse("text/plain"), helplineNumber);

        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                if (!TextUtils.isEmpty(createEventDAO.getSellStartDate()) && !TextUtils.isEmpty(createEventDAO.getSellEndDate()))
                {
                    Date startDateUtil = sdf.parse(createEventDAO.getSellingStartDate() + " " + createEventDAO.getSellStartTime());
                    //sellingStartDate = RequestBody.create(MediaType.parse("text/plain"), startDateUtil.toString());
                    sellingStartDate = RequestBody.create(MediaType.parse("text/plain"), Utility.localFormat(startDateUtil));
                    Date endDateUtil = sdf.parse(createEventDAO.getSellingEndDate() + " " + createEventDAO.getSellEndTime());
                    //sellingEndDate = RequestBody.create(MediaType.parse("text/plain"), endDateUtil.toString());
                    sellingEndDate = RequestBody.create(MediaType.parse("text/plain"), Utility.localFormat(endDateUtil));
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
                    object.addProperty("ticketSellingDays", dao.getTicketsellingdays());
                    object.addProperty("eventDay", dao.getEventDay());
                    object.addProperty("sellingStartDate", dao.getSellingStartDate());
                    object.addProperty("sellingStartTime", dao.getSellingStartTime());
                    object.addProperty("sellingEndDate", dao.getSellingEndDate());
                    object.addProperty("sellingEndTime", dao.getSellingEndTime());

                    if (dao.getTicketType().equals("Paid")) {
                        object.addProperty("cancellationChargeInPer", dao.getCancellationCharge() + "");
                        object.addProperty("pricePerTicket", dao.getPricePerTicket() + "");
                        if (dao.getDiscountPercentage() != 0.0)
                            object.addProperty("discount", dao.getDiscountPercentage());
                        regularPaidTicket.add(object);
                    } else if (dao.getTicketType().equals("Free")) {
                        regularFree.add(object);
                    }
                }
            }

            if (createEventDAO.getRsvpVipTicketDaoList() != null && createEventDAO.getRsvpVipTicketDaoList().size() > 0) {
                rsvpTicket = new JsonArray();
                vipTicket = new JsonArray();

                for (RsvpVipTicketDao dao : createEventDAO.getRsvpVipTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTicketQuantity());
                    object.addProperty("description", dao.getDescription().trim());

                    if (dao.getTicketType().equals("VIP")) {
                        object.addProperty("pricePerTicket", dao.getPricePerTicket());
                        object.addProperty("cancellationChargeInPer", dao.getCancellationCharge() + "");
                        object.addProperty("sellingStartDate", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),false));
                        object.addProperty("sellingStartTime", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),true));
                        object.addProperty("sellingEndDate", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),false));
                        object.addProperty("sellingEndTime", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),true));

                        if (dao.getDiscountPercentage() != 0.0)
                            object.addProperty("discount", dao.getDiscountPercentage());
                        vipTicket.add(object);
                    } else if (dao.getTicketType().equals("RSVP")) {
                        rsvpTicket.add(object);
                    }
                }
            }

            if (createEventDAO.getTableSeatingTicketDaoList() != null && createEventDAO.getTableSeatingTicketDaoList().size() > 0) {
                tableSeating = new JsonArray();

                for (TableSeatingTicketDao dao : createEventDAO.getTableSeatingTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("pricePerTicket", dao.getPricePerTicket());
                    object.addProperty("description", dao.getDescription());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("noOfTables", dao.getNoOfTables() + "");
                    object.addProperty("cancellationChargeInPer", dao.getCancellationCharge());
                    object.addProperty("sellingStartDate", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),false));
                    object.addProperty("sellingStartTime", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),true));
                    object.addProperty("sellingEndDate", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),false));
                    object.addProperty("sellingEndTime", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),true));
                    object.addProperty("sellingStartDate", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),false));
                    object.addProperty("sellingStartTime", convertLocalToUTC(dao.getSellingStartDate(),dao.getSellingStartTime(),true));
                    object.addProperty("sellingEndDate", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),false));
                    object.addProperty("sellingEndTime", convertLocalToUTC(dao.getSellingEndDate(),dao.getSellingEndTime(),true));
                    if (dao.getDiscountPercentage() != 0.0)
                        object.addProperty("discount", dao.getDiscountPercentage());
//                    object.addProperty("pricePerTable", dao.getPricePerTable());
//                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    tableSeating.add(object);
                }
            }
        }

        RequestBody venueAddress = null, venueLat = null, venueLongt = null, veneuName = null, venueId = null, countryCode = null, subVenue = null;

        if (createEventDAO.getLat() == null)
        {
            venueId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getVenueId()));
            if (createEventDAO.getSelectedSubVenues() != null && createEventDAO.getSelectedSubVenues().size() > 0)
            {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < createEventDAO.getSelectedSubVenues().size(); i++)
                {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("venueId", createEventDAO.getSelectedSubVenues().get(i).getVenueId());
                    jsonObject.addProperty("subVenueId", createEventDAO.getSelectedSubVenues().get(i).getSubVenueId());
                    jsonObject.addProperty("status", createEventDAO.getSelectedSubVenues().get(i).getStatus());
                    jsonArray.add(jsonObject);
                }
                subVenue = RequestBody.create(MediaType.parse("text/plain"), jsonArray.toString());
            }
        }
        else
        {
            veneuName = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getVenueName()));
            venueAddress = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getVenueAddress()));
            venueLat = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getLat()));
            venueLongt = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getLongt()));
            cityName = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getCityName()));
            countryCode = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(createEventDAO.getCountryCode()));
        }

        RequestBody regularFreeJsonBody = null;
        if (regularFree != null)
            regularFreeJsonBody = RequestBody.create(MediaType.parse("text/plain"), regularFree.toString());
        else
            regularFreeJsonBody=RequestBody.create(MediaType.parse("text/plain"),"regularfreejsonbody");

        RequestBody regularPaidBody = null;
        if (regularPaidTicket != null)
            regularPaidBody = RequestBody.create(MediaType.parse("text/plain"), regularPaidTicket.toString());
        else
            regularPaidBody=RequestBody.create(MediaType.parse("text/plain"),"regularpaidticket");

        RequestBody isEventPaid = RequestBody.create(MediaType.parse("text/plain"), "0");
        UpdateEventDao dao;
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
        map.put("free", regularFreeJsonBody);
        map.put("regularPaid", regularPaidBody);

         if (createEventDAO.getSelectedSubVenues()!=null && !createEventDAO.getSelectedSubVenues().equals("null"))
         {
             subVenue =RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getSelectedSubVenues()));
             map.put("SubVenue", subVenue);
         }

        if (createEventDAO.getSellingStartDate() != null && !createEventDAO.getSellingStartDate().equals("null") )
        {
            sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSellingStartDate());
            map.put("sellingStart", sellingStartDate);
        }

        if (createEventDAO.getSellingEndDate() != null && !createEventDAO.getSellingEndDate().equals("null") )
        {
            sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSellingEndDate());
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
        if (venueid!=null && venueid.equals("null"))
        {
            venueId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueId()));
            map.put("venueId", venueId);
        }

//        if (createEventDAO.getVenueAddress() != null && !createEventDAO.getVenueAddress().equals("null")) {
//            venueAddress = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getVenueAddress());
//            map.put("venueAddress", venueAddress);
//        }

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
        MultipartBody.Part[] image = null;
        if (imageList!=null) {
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
        Call<JsonElement> call = apiInterface.postEventTicket(map,image);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response)
            {
                loader.dismiss();
                if (response.isSuccessful())
                {
                    String msg = "";
                    try
                    {
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

                }
                else
                {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!..");
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
//        Call call = apiInterface.postEventTicket(
//                eventType,
//                categoryId,
//                subCategoryId,
//                name,
//                eventOccurrenceType,
//                eventOccuranceOn,
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