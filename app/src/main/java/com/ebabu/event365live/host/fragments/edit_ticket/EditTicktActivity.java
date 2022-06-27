package com.ebabu.event365live.host.fragments.edit_ticket;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityEditTicktBinding;
import com.ebabu.event365live.host.entities.UpdateTicketModal;
import com.ebabu.event365live.host.fragments.edit_ticket.adapter.EditTicketAdapter;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTicktActivity extends BaseActivity {

    public static UpdateTicketModal updateTicketModal;
    public static List<UpdateTicketModal.FreeTicket> regularTicketList;
    public static List<UpdateTicketModal.RegularNormal> rsvpVipTicketList;
    public static List<UpdateTicketModal.VipTableSeating> tableSeatingTicketList;
    public static List<Integer> cancelTicketIdList;
    @Inject
    ApiInterface apiInterface;
    private ActivityEditTicktBinding binding;
    private EditTicketAdapter editTicketAdapter;
    private MyLoader myLoader;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_tickt);
        App.getAppComponent().inject(this);
        myLoader = new MyLoader(this);
        eventId = getIntent().getIntExtra("eventId", -1);

        regularTicketList = new ArrayList<>();
        rsvpVipTicketList = new ArrayList<>();
        tableSeatingTicketList = new ArrayList<>();
        cancelTicketIdList = new ArrayList<>();

        if (eventId != -1) {
            getEditEventDetails(eventId);
        }


    }

    private void initView() {

        Log.v("Token", "--->--> " + Utility.getSharedPreferencesString(this, API.AUTHORIZATION));

        binding.backArrow.setOnClickListener(v -> onBackPressed());

        editTicketAdapter = new EditTicketAdapter(getSupportFragmentManager(), this);
        binding.homeViewPager.setAdapter(editTicketAdapter);
        editTicketAdapter.notifyDataSetChanged();
        binding.tabLayout.setupWithViewPager(binding.homeViewPager);
        binding.tabLayout.getTabAt(0).select();


        binding.homeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.tabLayout.getTabAt(0).select();
                } else if (position == 1) {
                    binding.tabLayout.getTabAt(1).select();
                } else if (position == 2) {
                    binding.tabLayout.getTabAt(2).select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void getEditEventDetails(int eventId) {
        myLoader.show("");
        Call<JsonElement> eventTicketRequest = apiInterface.getEventTicketsRequest(eventId);
        eventTicketRequest.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                myLoader.dismiss();
                UpdateTicketModal updateTicketModal2 = new Gson().fromJson(response.body().toString(), UpdateTicketModal.class);
                if (response.isSuccessful()) {

                    updateTicketModal = updateTicketModal2;
                    initView();
                } else {
                    Toast.makeText(EditTicktActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                    initView();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                myLoader.dismiss();
                Toast.makeText(EditTicktActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                initView();
            }
        });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }

    public void setEditTicketData(int tab, int editPos) {

        JsonArray array = new JsonArray();
        JsonArray cancelTicketIdObj = new JsonArray();
        JsonObject obj = null;
        JsonObject finalObj = new JsonObject();


        if (tab == 1) {
            if (regularTicketList != null && !regularTicketList.isEmpty()) {
//                for (int i = 0; i < regularTicketList.size(); i++) {
                int i;
                if (editPos == -1) {
                    i = regularTicketList.size() - 1;
                } else {
                    i = editPos;
                }
                obj = new JsonObject();
                if (regularTicketList.get(i).getId() != null && !regularTicketList.get(i).getId().equalsIgnoreCase("")) {
                    obj.addProperty("id", Integer.parseInt(regularTicketList.get(i).getId()));
                }

                obj.addProperty("ticketName", regularTicketList.get(i).getTicketName());
                obj.addProperty("noOfTables", regularTicketList.get(i).getNoOfTables());
                obj.addProperty("pricePerTable", regularTicketList.get(i).getPricePerTable());
                obj.addProperty("description", regularTicketList.get(i).getDescription());
                obj.addProperty("parsonPerTable", regularTicketList.get(i).getParsonPerTable());

                if (regularTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_FREE)) {
                    obj.addProperty("ticketType", "freeNormal");
                } else if (regularTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_PAID)) {
                    obj.addProperty("ticketType", "regularPaid");
                    obj.addProperty("discount", regularTicketList.get(i).getDiscount());
                }

                if (regularTicketList.get(i).getPricePerTicket() == null || regularTicketList.get(i).getPricePerTicket().equalsIgnoreCase("")) {
                    obj.addProperty("pricePerTicket", "0");
                } else {
                    obj.addProperty("pricePerTicket", regularTicketList.get(i).getPricePerTicket());
                }

                obj.addProperty("totalQuantity", regularTicketList.get(i).getTotalQuantity());

                if (regularTicketList.get(i).getTicketType() == null ||
                        regularTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_FREE) ||
                        regularTicketList.get(i).getTicketType().equalsIgnoreCase("freeNormal")) {
                    obj.addProperty("cancellationChargeInPer", "0");
                } else if (regularTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_PAID) ||
                        regularTicketList.get(i).getTicketType().equalsIgnoreCase("regularPaid")) {
                    obj.addProperty("cancellationChargeInPer", regularTicketList.get(i).getCancellationChargeInPer());
                }

                obj.addProperty("sellingStartDate", regularTicketList.get(i).getSellingStartDate());
                obj.addProperty("sellingStartTime", regularTicketList.get(i).getSellingStartTime());
                obj.addProperty("sellingEndDate", regularTicketList.get(i).getSellingEndDate());
                obj.addProperty("sellingEndTime", regularTicketList.get(i).getSellingEndTime());

                array.add(obj);
                Log.d("fnaklfa", i + " Regular ==== " + array.toString());
//                    }
//                }
            }
        }

        if (tab == 2) {
            if (rsvpVipTicketList != null && !rsvpVipTicketList.isEmpty()) {
//                for (int i = 0; i < rsvpVipTicketList.size(); i++) {

                int i;
                if (editPos == -1) {
                    i = rsvpVipTicketList.size() - 1;
                } else {
                    i = editPos;
                }
                obj = new JsonObject();

                if (rsvpVipTicketList.get(i).getId() != null && !rsvpVipTicketList.get(i).getId().equalsIgnoreCase("")) {
                    obj.addProperty("id", Integer.parseInt(rsvpVipTicketList.get(i).getId()));
                }

                obj.addProperty("ticketName", rsvpVipTicketList.get(i).getTicketName());
                obj.addProperty("noOfTables", rsvpVipTicketList.get(i).getNoOfTables());
                obj.addProperty("pricePerTable", rsvpVipTicketList.get(i).getPricePerTable());
                obj.addProperty("description", rsvpVipTicketList.get(i).getDescription());
                obj.addProperty("parsonPerTable", rsvpVipTicketList.get(i).getParsonPerTable());

                if (rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_RSVP)) {
                    obj.addProperty("ticketType", "regularNormal");
                } else if (rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_VIP)) {
                    obj.addProperty("ticketType", "vipNormal");
                    obj.addProperty("discount", rsvpVipTicketList.get(i).getDiscount());
                } else {
                    obj.addProperty("ticketType", rsvpVipTicketList.get(i).getTicketType());
                }

                if (rsvpVipTicketList.get(i).getPricePerTicket() == null || rsvpVipTicketList.get(i).getPricePerTicket().equals("")) {
                    obj.addProperty("pricePerTicket", "0");
                } else {
                    obj.addProperty("pricePerTicket", rsvpVipTicketList.get(i).getPricePerTicket());
                }

                obj.addProperty("totalQuantity", rsvpVipTicketList.get(i).getTotalQuantity());


                if (rsvpVipTicketList.get(i).getTicketType() == null ||
                        rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_RSVP) ||
                        rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase("regularNormal")) {
                    obj.addProperty("cancellationChargeInPer", "0");
                } else if (rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_VIP) ||
                        rsvpVipTicketList.get(i).getTicketType().equalsIgnoreCase("vipNormal")) {
                    obj.addProperty("cancellationChargeInPer", rsvpVipTicketList.get(i).getCancellationChargeInPer());

                    obj.addProperty("sellingStartDate", rsvpVipTicketList.get(i).getSellingStartDate());
                    obj.addProperty("sellingStartTime", rsvpVipTicketList.get(i).getSellingStartTime());
                    obj.addProperty("sellingEndDate", rsvpVipTicketList.get(i).getSellingEndDate());
                    obj.addProperty("sellingEndTime", rsvpVipTicketList.get(i).getSellingEndTime());
                }

                array.add(obj);

                Log.d("fnaklfa", i + " RsvpVip ==== " + array.toString());
            }
//                }
        }

        if (tab == 3) {
            if (tableSeatingTicketList != null && !tableSeatingTicketList.isEmpty()) {
                //  for (int i = 0; i < tableSeatingTicketList.size(); i++) {
                int i;
                if (editPos == -1) {
                    i = tableSeatingTicketList.size() - 1;
                } else {
                    i = editPos;
                }

                obj = new JsonObject();

                if (tableSeatingTicketList.get(i).getId() != null && !tableSeatingTicketList.get(i).getId().equalsIgnoreCase("")) {
                    obj.addProperty("id", Integer.parseInt(tableSeatingTicketList.get(i).getId()));
                }

                obj.addProperty("ticketName", tableSeatingTicketList.get(i).getTicketName());
                obj.addProperty("noOfTables", tableSeatingTicketList.get(i).getNoOfTables());
                obj.addProperty("pricePerTable", tableSeatingTicketList.get(i).getPricePerTable());
                obj.addProperty("description", tableSeatingTicketList.get(i).getDescription());
                obj.addProperty("parsonPerTable", tableSeatingTicketList.get(i).getParsonPerTable());
                obj.addProperty("ticketType", tableSeatingTicketList.get(i).getTicketType());
                obj.addProperty("pricePerTicket", tableSeatingTicketList.get(i).getPricePerTicket());
                obj.addProperty("totalQuantity", tableSeatingTicketList.get(i).getTotalQuantity());
                obj.addProperty("sellingStartDate", tableSeatingTicketList.get(i).getSellingStartDate());
                obj.addProperty("sellingStartTime", tableSeatingTicketList.get(i).getSellingStartTime());
                obj.addProperty("sellingEndDate", tableSeatingTicketList.get(i).getSellingEndDate());
                obj.addProperty("sellingEndTime", tableSeatingTicketList.get(i).getSellingEndTime());
                obj.addProperty("discount", tableSeatingTicketList.get(i).getDiscount());
                obj.addProperty("cancellationChargeInPer", tableSeatingTicketList.get(i).getCancellationChargeInPer());

                array.add(obj);
                Log.d("fnaklfa", i + " TabSeat ==== " + array.toString());

                //  }
            }
        }

        finalObj.add("ticketCreate", array);

        if (obj == null) {
            finalObj.add("ticketCreate", array);
        }
        for (Integer cancelTicketId : cancelTicketIdList) {
            Log.d("fnaklfa", "== : " + cancelTicketId);
            cancelTicketIdObj.add(cancelTicketId);
        }

        finalObj.add("ticketDisabled", cancelTicketIdObj);
        Log.d("fnaklfa", array.size() + " ==== " + cancelTicketIdObj.size() + " onCreate: " + finalObj.toString());


        if (array.size() == 0 && cancelTicketIdObj.size() == 0) {

            Toast.makeText(EditTicktActivity.this, "Please do some changes in tickets or add new tickets", Toast.LENGTH_SHORT).show();
            return;
        }

        if (array.size() == 0 || array.size() > 0 || cancelTicketIdObj.size() == 0 || cancelTicketIdObj.size() > 0) {
            if (cancelTicketIdObj.size() > 0 || array.size() > 0) {

                boolean isAdd = false;
                if(editPos == -1){
                    isAdd = true;
                }

                updateTicketRequest(finalObj, isAdd);
            }
        }

    }

    public void setDeleteTicketData() {

        JsonArray array = new JsonArray();
        JsonArray cancelTicketIdObj = new JsonArray();
        JsonObject obj = null;
        JsonObject finalObj = new JsonObject();

        if (obj == null) {
            finalObj.add("ticketCreate", array);
        }
        for (Integer cancelTicketId : cancelTicketIdList) {
            Log.d("fnaklfa", "== : " + cancelTicketId);
            cancelTicketIdObj.add(cancelTicketId);
        }

        finalObj.add("ticketDisabled", cancelTicketIdObj);
        Log.d("fnaklfa", array.size() + " ==== " + cancelTicketIdObj.size() + " onCreate: " + finalObj.toString());


        if (array.size() == 0 && cancelTicketIdObj.size() == 0) {

            Toast.makeText(EditTicktActivity.this, "Please do some changes in tickets or add new tickets", Toast.LENGTH_SHORT).show();
            return;
        }

        if (array.size() == 0 || array.size() > 0 || cancelTicketIdObj.size() == 0 || cancelTicketIdObj.size() > 0) {
            if (cancelTicketIdObj.size() > 0 || array.size() > 0) {
                updateTicketRequest(finalObj, false);
            }
        }

    }

    private void updateTicketRequest(JsonObject jsonObject, boolean isAdd) {
        myLoader.show("");
        Call<JsonElement> updateEventTickets = apiInterface.updateEventTickets(eventId, jsonObject);
        updateEventTickets.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
//                    Toast.makeText(EditTicktActivity.this, "Tickets has been updated successfully", Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject object = new JSONObject(response.body().toString());

                        Toast.makeText(EditTicktActivity.this, object.getString("message"), Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    cancelTicketIdList = new ArrayList<>();
                    if(isAdd){
                        finish();
                    }

                } else {
                    Toast.makeText(EditTicktActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                Toast.makeText(EditTicktActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

}