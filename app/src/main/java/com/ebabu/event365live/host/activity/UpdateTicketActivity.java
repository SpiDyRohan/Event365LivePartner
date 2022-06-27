package com.ebabu.event365live.host.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityUpdateTicketBinding;
import com.ebabu.event365live.host.entities.UpdateTicketModal;
import com.ebabu.event365live.host.utils.AddTicketModal;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateTicketActivity extends BaseActivity {
    private MyLoader myLoader;
    @Inject
    ApiInterface apiInterface;
    private ActivityUpdateTicketBinding binding;
    private String selectedType;
    private LayoutInflater layoutInflater;

    private List<UpdateTicketModal.FreeTicket> freeTicketList = null;
    private List<UpdateTicketModal.RegularNormal> vipNormalList = null;
    private List<UpdateTicketModal.VipTableSeating> vipTableSeatingList = null;
    private List<UpdateTicketModal.RegularNormal> regularNormalList = null;
    private List<UpdateTicketModal.VipTableSeating> regularTableSeatingList = null;
    private int ticketCounterViewId = 1000;
    private int delTicketId = 2000, switchId = 200;
    private int seatingCounterId = 100, counter, total, seatCounter;
    private UpdateTicketModal updateTicketModal;
    private String[] typeList = {"Free RSVP Tickets", "Paid RSVP Tickets", "VIP RSVP Tickets"};
    private UpdateTicketModal.Event event;
    private List<Integer> cancelTicketIdList;
    private List<AddTicketModal> addTicketModals;
    private String getTicketType = "";
    private int freeDisableFlag = -1;
    private int eventId;
    private boolean isAllFreeTicketsCancelled, isAllRegularTicketsCancelled, isAllRegularSeatingTicketsCancelled, isAllVipNormalTicketsCancelled, isAllVipSeatingTicketsCancelled;

    LinearLayout linearLayout;
    LinearLayout addSeatBtn;
    private boolean clickByUpdateBtn;

    int freeTicketCount, regularNormalTicketCount, vipNormalTicketCount, regularSeatingTicketCount, vipSeatingTicketCount;
    boolean isRequiredFormFill;


    private View view;
    private RelativeLayout showCancelTicket;
    private View seatingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_ticket);
        layoutInflater = getLayoutInflater();
        App.getAppComponent().inject(this);
        updateTicketModal = new UpdateTicketModal();
        myLoader = new MyLoader(this);
        eventId = getIntent().getIntExtra("eventId", -1);
        if (eventId != -1) {
            cancelTicketIdList = new ArrayList<>();
            addTicketModals = new ArrayList<>();
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(UpdateTicketActivity.this,
                    R.layout.pricing_spinner, typeList);
            dataAdapter.setDropDownViewResource(R.layout.pricing_spinner_layout);
            binding.spinner.setAdapter(dataAdapter);
            getEditEventDetails(eventId);
        }
        binding.backArrow.setOnClickListener(v -> finish());
        binding.textView20.setText(getString(R.string.edit_ticket));
        binding.nextBtn.setOnClickListener(v -> {

            clickByUpdateBtn = true;

            if (selectedType != null && binding.ticketParent.getChildCount() != 0) {
                if (addTicketModals.isEmpty()) {
                    if (!validateTicket(view, true))
                        return;
                } else {
                    if (!validateTicket(view, false))
                        return;
                }
                saveTicketLastRowData();
                saveSeatLastRowData();
            }

            JsonArray array = new JsonArray();
            JsonArray cancelTicketIdObj = new JsonArray();
            JsonObject obj = null;
            JsonObject finalObj = new JsonObject();

            for (int i = 0; i < addTicketModals.size(); i++) {
                if (addTicketModals.get(i).isTicketDisable()) {
                    Log.d("fnaklfa", i + " ==== " + addTicketModals.get(i).isTicketDisable());
                    continue;
                }
                obj = new JsonObject();
                obj.addProperty("ticketName", addTicketModals.get(i).getTicketName());
                obj.addProperty("noOfTables", addTicketModals.get(i).getNoOfTables());
                obj.addProperty("pricePerTable", addTicketModals.get(i).getPricePerTable());
                obj.addProperty("description", addTicketModals.get(i).getDescription());
                obj.addProperty("parsonPerTable", addTicketModals.get(i).getParsonPerTable());
                obj.addProperty("ticketType", addTicketModals.get(i).getTicketType());
                obj.addProperty("pricePerTicket", addTicketModals.get(i).getPricePerTicket());
                obj.addProperty("totalQuantity", addTicketModals.get(i).getTotalQuantity());

                obj.addProperty("cancellationChargeInPer", addTicketModals.get(i).getCancellationChargeInPer());

                array.add(obj);
                finalObj.add("ticketCreate", array);
            }
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
                Toast.makeText(UpdateTicketActivity.this, "Please do some changes in tickets or add new tickets", Toast.LENGTH_SHORT).show();
                return;
            }

            if (array.size() == 0 || array.size() > 0 || cancelTicketIdObj.size() == 0 || cancelTicketIdObj.size() > 0) {
                if (cancelTicketIdObj.size() > 0 || array.size() > 0) {
                    updateTicketRequest(finalObj);
                }
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
                UpdateTicketModal updateTicketModal = new Gson().fromJson(response.body().toString(), UpdateTicketModal.class);
                if (response.isSuccessful()) {
                    init(updateTicketModal);
                    setHeader(typeList[0]);
                    binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (selectedType != null && binding.ticketParent.getChildCount() != 0) {
                                saveTicketLastRowData();
                                saveSeatLastRowData();
                            }

                            setHeader(typeList[position]);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                } else {
                    Toast.makeText(UpdateTicketActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                myLoader.dismiss();
                Toast.makeText(UpdateTicketActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void init(UpdateTicketModal updateTicketModal) {
        freeTicketList = updateTicketModal.getData().getFreeNormal();
        vipNormalList = updateTicketModal.getData().getNormalTicket().getVipNormal();
        vipTableSeatingList = updateTicketModal.getData().getTableSeating().getVipTableSeating();
        regularNormalList = updateTicketModal.getData().getNormalTicket().getRegularNormal();
        regularTableSeatingList = updateTicketModal.getData().getTableSeating().getRegularTableSeating();
        event = updateTicketModal.getData().getEvent();

        if (freeTicketList == null) {
            freeTicketList = new ArrayList<>();
        }
        if (vipNormalList == null) vipNormalList = new ArrayList<>();

        if (vipTableSeatingList == null) vipTableSeatingList = new ArrayList<>();

        if (regularNormalList == null) regularNormalList = new ArrayList<>();

        if (regularTableSeatingList == null) regularTableSeatingList = new ArrayList<>();


        binding.textView39.setText(Utility.getDateTime(event.getStart(), false));
        binding.startTimeTv.setText(Utility.getDateTime(event.getStart(), true));
        binding.endDateTv.setText(Utility.getDateTime(event.getEnd(), false));
        binding.endTimeTv.setText(Utility.getDateTime(event.getEnd(), true));

        binding.imageView22.setOnClickListener(v -> Dialogs.setMinMaxDate(this, "", updateTicketModal.getData().getEvent().getStart(), binding.textView39, date -> {
            binding.textView39.setText(date);

            binding.startTimeTv.setText(getString(R.string.start_time));
            binding.endDateTv.setText(getString(R.string.end_date));
            binding.endTimeTv.setText(getString(R.string.end_time));
        }));
        binding.textView39.setOnClickListener(v -> {
            Dialogs.setMinMaxDate(this, "", event.getStart(), binding.textView39, date -> {
                binding.textView39.setText(date);
                binding.startTimeTv.setText(getString(R.string.start_time));
                binding.endDateTv.setText(getString(R.string.end_date));
                binding.endTimeTv.setText(getString(R.string.end_time));
            });
        });
        binding.endDateImg.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(this, "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(this, binding.textView39.getText().toString(), event.getStart(), binding.textView39, date -> {
                    binding.endDateTv.setText(date);
                });
        });
        binding.endDateTv.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(this, "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(this, binding.textView39.getText().toString(), event.getStart(), binding.textView39, date -> {
                    binding.endDateTv.setText(date);
                });
        });

        binding.startTimeImg.setOnClickListener(this::setStartTime);
        binding.startTimeTv.setOnClickListener(this::setStartTime);
        binding.endTimeImg.setOnClickListener(this::setEndTime);
        binding.endTimeTv.setOnClickListener(this::setEndTime);
    }

    private void setStartTime(View v) {
        Dialogs.setTime(this, binding.endTimeTv, time -> {
            if (!binding.textView39.getText().equals(event.getStart()))
                binding.startTimeTv.setText(time);
            else if (compareTime(time, event.getStart()))
                binding.startTimeTv.setText(time);
            else {
                Dialogs.toast(this, v, "Sell start time must be earlier than Event start time");
                binding.startTimeTv.setText(getString(R.string.start_time));
            }
        });
    }

    private void setEndTime(View v) {
        if (binding.startTimeTv.getText().toString().equalsIgnoreCase(getString(R.string.start_time)))
            Toast.makeText(this, "Select start time first!", Toast.LENGTH_LONG).show();
        else
            Dialogs.setTime(this, binding.endTimeTv, time -> {
                if (!binding.textView39.getText().equals(binding.endDateTv.getText().toString())) {

                    if (binding.endDateTv.getText().equals(event.getStart())) {
                        if (compareTime(time, event.getStart()))
                            binding.endTimeTv.setText(time);
                        else {
                            Dialogs.toast(this, v, "Sell end time must be earlier than Event Start time");
                            binding.endTimeTv.setText(getString(R.string.end_time));
                        }
                    } else
                        binding.endTimeTv.setText(time);
                } else {
                    String startTime = binding.startTimeTv.getText().toString();

                    if (compareTime(startTime, time))
                        if (binding.endDateTv.getText().equals(event.getStart())) {
                            if (compareTime(time, event.getStart()))
                                binding.endTimeTv.setText(time);
                            else {
                                Dialogs.toast(this, v, "Sell end time must be earlier than Event Start time");
                                binding.endTimeTv.setText(getString(R.string.end_time));
                            }
                        } else
                            binding.endTimeTv.setText(time);
                    else {
                        Dialogs.toast(this, v, "Start time must be earlier than End time");
                        binding.endTimeTv.setText(getString(R.string.end_time));
                    }
                }
            });
    }

    private int getHour(String time) {
        int startHour = Integer.parseInt(time.substring(0, 2));
        return time.matches("(.*)PM") && startHour < 12 ? (startHour + 12) : startHour;
    }

    private boolean compareTime(String startTime, String endTime) {

        int startHour = getHour(startTime);
        int endHour = getHour(endTime);

        if (endHour > startHour) return true;

        else if (endHour == startHour) {
            int startMin = Integer.parseInt(startTime.substring(3, 5));
            int endMin = Integer.parseInt(endTime.substring(3, 5));
            return endMin > startMin;
        }
        return false;
    }

    private boolean validateTicket(View view, boolean showErrorMsg) {
        if (((EditText) view.findViewById(R.id.event_name_et)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
                Dialogs.toast(this, view, "Enter Ticket Name Please!");
                view.findViewById(R.id.event_name_et).requestFocus();
            }
            return false;
        }

        if (!selectedType.equalsIgnoreCase("Free")) {
            if (((EditText) view.findViewById(R.id.price_ticket_et)).getText().toString().trim().length() == 0) {
                if (showErrorMsg) {
                    Dialogs.toast(this, view, "Fill Ticket Price or Choose Free Ticket Type!");
                    view.findViewById(R.id.price_ticket_et).requestFocus();
                }
                return false;
            }
        }
        if (((EditText) view.findViewById(R.id.ticket_qty_et)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
                Dialogs.toast(this, view, "Ticket Quantity is Mandatory!");
                ((EditText) view.findViewById(R.id.ticket_qty_et)).requestFocus();
            }
            return false;
        }
        return true;
    }

    private boolean validateSeating(View view, boolean showMsg) {
        if (((EditText) view.findViewById(R.id.event_name_et)).getText().toString().trim().length() == 0) {
            if (showMsg) {
                Dialogs.toast(this, view, "Enter Category Name Please!");
                ((EditText) view.findViewById(R.id.event_name_et)).requestFocus();
            }
            return false;
        }
        if (((EditText) view.findViewById(R.id.price_per_table_et)).getText().toString().trim().length() == 0) {
            if (showMsg) {
                Dialogs.toast(this, view, "Enter Price Per Table!");
                ((EditText) view.findViewById(R.id.price_per_table_et)).requestFocus();
            }
            return false;
        }
        return true;
    }

    private void setHeader(String s) {


        switch (s) {
            case "Free RSVP Tickets":
                //countCancelledFreeTickets();

                selectedType = "Free";
                isRequiredFormFill = false;
                break;
            case "Paid RSVP Tickets":
                countCancelledRegularTickets();
                countCancelledRegularSeatingTickets();
                selectedType = "Paid";
                isRequiredFormFill = false;
                // linearLayout.callOnClick();
                break;
            case "VIP RSVP Tickets":
                selectedType = "VIP";
                countCancelledVipTickets();
                countCancelledVipSeatingTickets();
                isRequiredFormFill = false;
                // linearLayout.callOnClick();
                break;
        }


        int total, seatingTotal = 0;
        if (selectedType.equalsIgnoreCase("Free")) total = freeTicketList.size();
        else if (selectedType.equalsIgnoreCase("VIP")) {
            total = vipNormalList.size();
            seatingTotal = vipTableSeatingList.size();
        } else {
            total = regularNormalList.size();
            seatingTotal = regularTableSeatingList.size();
        }
        if (binding.ticketParent.getChildCount() > 0) binding.ticketParent.removeAllViews();
        if (binding.seatingParentLayout.getChildCount() > 0)
            binding.seatingParentLayout.removeAllViews();

        counter = 0;
        do {
            getTicketView();
            counter++;
        }
        while (counter < total);

        if (s.equalsIgnoreCase("Free"))
            binding.seatingLayout.setVisibility(View.GONE);
        else {
            if (!selectedType.equalsIgnoreCase("Free")) {
                binding.seatingLayout.setVisibility(View.VISIBLE);
                seatCounter = 0;
                do {
                    getSeatingView();
                    seatCounter++;
                }
                while (seatCounter < seatingTotal);
            }
        }

        for (int i = 0; i < binding.ticketParent.getChildCount(); i++) {
            View view = binding.ticketParent.getChildAt(i);
            ((TextView) view.findViewById(R.id.textView45)).setText(selectedType + " RSVP Ticket");
            if (selectedType.equalsIgnoreCase("Free")) {
                view.findViewById(R.id.textView46).setVisibility(View.GONE);
                view.findViewById(R.id.price_ticket_et).setVisibility(View.GONE);
                view.findViewById(R.id.view7).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.textView46).setVisibility(View.VISIBLE);
                view.findViewById(R.id.price_ticket_et).setVisibility(View.VISIBLE);
                view.findViewById(R.id.view7).setVisibility(View.VISIBLE);
            }
        }
    }

    private void getTicketView() {
        countCancelledFreeTickets();
        freeTicketCount = 0;
        regularNormalTicketCount = 0;
        vipNormalTicketCount = 0;
        view = layoutInflater.inflate(R.layout.layout_ticket, binding.ticketParent, false);
        TextView headerTitle = view.findViewById(R.id.header_title);
        TextView cancelBtn = view.findViewById(R.id.cancelBtn);
        showCancelTicket = view.findViewById(R.id.showCancelTicket);
        showCancelTicket.setTag("demo");
        headerTitle.setTag("demo");
        headerTitle.setText("Disable " + selectedType + " RSVP Ticket");
        SwitchMaterial switchMaterial = view.findViewById(R.id.switchMaterial);
        switchMaterial.setVisibility(View.GONE);
        RelativeLayout topView = view.findViewById(R.id.top_view);
        LinearLayout hidableView = view.findViewById(R.id.linearLayout6);
        RelativeLayout shadowView = view.findViewById(R.id.shadowView);
        shadowView.setTag(false);
        EditText nameEt = view.findViewById(R.id.event_name_et);
        EditText priceTicketEt = view.findViewById(R.id.price_ticket_et);
        EditText ticketQtyEt = view.findViewById(R.id.ticket_qty_et);
        EditText desc_et = view.findViewById(R.id.short_desc_et);
        TextView tvAddTicketBtn = view.findViewById(R.id.tvAddTicketBtn);

        TextView cancellationChargesText = view.findViewById(R.id.priceCancellationTicket);
        ImageView increasePrice = view.findViewById(R.id.increasePriceTicket);
        ImageView decreasePrice = view.findViewById(R.id.decreasePriceTicket);

        shadowView.setOnClickListener(v -> {
        });

        tvAddTicketBtn.setOnClickListener(v -> {
            Log.d("fnklsafklsa", "tvAddTicketBtn: ");
            getTicketView();
        });

        topView.setOnClickListener(v -> {
            if (hidableView.getVisibility() == View.VISIBLE)
                hidableView.setVisibility(View.GONE);
            else
                hidableView.setVisibility(View.VISIBLE);
        });
        long id = System.currentTimeMillis();

        view.setTag(id);

        view.setId(ticketCounterViewId++);
        ImageView deleteImg = view.findViewById(R.id.del_btn);
        TextView titleTv = view.findViewById(R.id.textView45);
        titleTv.setText(selectedType + " RSVP Ticket");
        deleteImg.setId(delTicketId++);
        deleteImg.setTag(id);
        switchMaterial.setTag(id);

        if (counter == 0) {
            MaterialCardView enableDisableCard = view.findViewById(R.id.enable_disable_card);
            enableDisableCard.setVisibility(View.VISIBLE);

            switchMaterial.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.INVISIBLE);

            switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    headerTitle.setText("Disable " + selectedType + " RSVP Ticket");
                    enableDisableTicket(b, showCancelTicket);
                    DisableEnableAllTicketViews(b);
                    // TODO: 30/10/19  Dialogs.expand(hidableView);
                    setDisableEnableFlag(switchMaterial.getTag().toString(), false);
                } else {
                    headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                    enableDisableTicket(b, showCancelTicket);
                    DisableEnableAllTicketViews(b);
                    setDisableEnableFlag(switchMaterial.getTag().toString(), true);

                }
            });
        }

        if (counter > 0) {
            if (deleteImg.getVisibility() == View.VISIBLE) {
            }
        }

        if (selectedType.equalsIgnoreCase("Paid") && regularNormalList.size() >= counter + 1) {
            UpdateTicketModal.RegularNormal dao = regularNormalList.get(counter);
            cancelBtn.setTag(dao.getId());
            nameEt.setText(dao.getTicketName());
            deleteImg.setTag(dao.getId());
            switchMaterial.setTag(dao.getId());
            priceTicketEt.setText(dao.getPricePerTicket() == 0.0 ? "" : String.valueOf(dao.getPricePerTicket()));
            ticketQtyEt.setText(String.valueOf(dao.getTotalQuantity()));
            desc_et.setText(dao.getDescription());
            shadowView.setTag(dao.getIsCancelled());

            cancellationChargesText.setText(String.valueOf(dao.getCancellationChargeInPer()));
            setCancelTicket(dao.getIsCancelled(), cancelBtn, showCancelTicket, dao.isTempCancel(), dao.isAddedNetTicket(), deleteImg, shadowView);

            if (cancelBtn.getVisibility() == View.VISIBLE) {
                /* existing ticket should not editable*/
                Log.d("nfalksnklfa", "getTicketView: Free");
                nameEt.setEnabled(false);
                priceTicketEt.setEnabled(false);
                ticketQtyEt.setEnabled(false);
                desc_et.setEnabled(false);

                increasePrice.setEnabled(false);
                decreasePrice.setEnabled(false);

            } else {
                Log.d("nfalksnklfa", "not visible: Free");
                nameEt.setEnabled(true);
                priceTicketEt.setEnabled(true);
                ticketQtyEt.setEnabled(true);
                desc_et.setEnabled(true);

                increasePrice.setEnabled(true);
                decreasePrice.setEnabled(true);
            }


            if (regularNormalList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);

                if (tvAddTicketBtn.getVisibility() == View.VISIBLE) {
                    tvAddTicketBtn.setVisibility(View.GONE);
                }
            }

            cancelBtn.setOnClickListener(v -> {
                if (dao.isTempCancel()) {
                    //dao.setIsCancelled(false);
                    dao.setTempCancel(false);
                    cancelBtn.setBackgroundResource(R.drawable.custom_cancel_btn);
                    cancelBtn.setText("Cancel");
                    cancelTicketIdList.remove(Integer.valueOf((String) cancelBtn.getTag()));
                    return;
                }
                //dao.setIsCancelled(true);
                dao.setTempCancel(true);
                cancelBtn.setBackgroundResource(R.drawable.revert_cancel_custom_btn);
                cancelBtn.setText("Revert");
                cancelTicketIdList.add(Integer.parseInt((String) cancelBtn.getTag()));
            });

            increasePrice.setOnClickListener(v -> {
                String s = cancellationChargesText.getText().toString();
                cancellationChargesText.setText((Integer.valueOf(s) + 1) + "");
            });

            decreasePrice.setOnClickListener(v -> {
                String s = cancellationChargesText.getText().toString();
                if (!s.equalsIgnoreCase("10")) {
                    cancellationChargesText.setText((Integer.valueOf(s) - 1) + "");
                }
            });
        } else if (selectedType.equalsIgnoreCase("VIP") && vipNormalList.size() >= counter + 1) {
            UpdateTicketModal.RegularNormal dao = vipNormalList.get(counter);
            cancelBtn.setTag(dao.getId());
            nameEt.setText(dao.getTicketName());
            deleteImg.setTag(dao.getId());
            switchMaterial.setTag(dao.getId());
            priceTicketEt.setText(dao.getPricePerTicket() == 0.0 ? "" : String.valueOf(dao.getPricePerTicket()));
            ticketQtyEt.setText(String.valueOf(dao.getTotalQuantity()));
            desc_et.setText(dao.getDescription());
            shadowView.setTag(dao.getIsCancelled());
            cancellationChargesText.setText(String.valueOf(dao.getCancellationChargeInPer()));
            setCancelTicket(dao.getIsCancelled(), cancelBtn, showCancelTicket, dao.isTempCancel(), dao.isAddedNetTicket(), deleteImg, shadowView);

            if (cancelBtn.getVisibility() == View.VISIBLE) {
                /* existing ticket should not editable*/
                nameEt.setEnabled(false);
                priceTicketEt.setEnabled(false);
                ticketQtyEt.setEnabled(false);
                desc_et.setEnabled(false);
                increasePrice.setEnabled(false);
                decreasePrice.setEnabled(false);
            } else {
                nameEt.setEnabled(true);
                priceTicketEt.setEnabled(true);
                ticketQtyEt.setEnabled(true);
                desc_et.setEnabled(true);
                increasePrice.setEnabled(true);
                decreasePrice.setEnabled(true);
            }

            if (vipNormalList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);
                if (tvAddTicketBtn.getVisibility() == View.VISIBLE) {
                    tvAddTicketBtn.setVisibility(View.GONE);
                }
            }
            cancelBtn.setOnClickListener(v -> {
                if (dao.isTempCancel()) {
                    //dao.setIsCancelled(false);
                    dao.setTempCancel(false);
                    cancelBtn.setBackgroundResource(R.drawable.custom_cancel_btn);
                    cancelBtn.setText("Cancel");
                    cancelTicketIdList.remove(Integer.valueOf((String) cancelBtn.getTag()));
                    return;
                }
                //dao.setIsCancelled(true);
                dao.setTempCancel(true);
                cancelBtn.setBackgroundResource(R.drawable.revert_cancel_custom_btn);
                cancelBtn.setText("Revert");
                cancelTicketIdList.add(Integer.parseInt((String) cancelBtn.getTag()));

            });

            increasePrice.setOnClickListener(v -> {
                String s = cancellationChargesText.getText().toString();
                cancellationChargesText.setText((Integer.valueOf(s) + 1) + "");
            });

            decreasePrice.setOnClickListener(v -> {
                String s = cancellationChargesText.getText().toString();
                if (!s.equalsIgnoreCase("10")) {
                    cancellationChargesText.setText((Integer.valueOf(s) - 1) + "");
                }
            });

        } else if (selectedType.equalsIgnoreCase("Free") && freeTicketList.size() >= counter + 1) {
            UpdateTicketModal.FreeTicket dao = freeTicketList.get(counter);
            deleteImg.setTag(dao.getId());
            switchMaterial.setTag(dao.getId());
            cancelBtn.setTag(dao.getId());
            nameEt.setText(dao.getTicketName());
            ticketQtyEt.setText(String.valueOf(dao.getTotalQuantity()));
            desc_et.setText(dao.getDescription());
            shadowView.setTag(dao.getIsCancelled());
            setCancelTicket(dao.getIsCancelled(), cancelBtn, showCancelTicket, dao.isTempCancel(), dao.isAddedNewTicket(), deleteImg, shadowView);
            if (freeTicketList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);
                if (tvAddTicketBtn.getVisibility() == View.VISIBLE) {
                    tvAddTicketBtn.setVisibility(View.GONE);
                }
            }

            if (dao.getIsCancelled() && UpdateTicketModal.isAllFreeTicketCanceled) {
                //deleteImg.setVisibility(View.GONE);
                view.findViewById(R.id.add_ticket_layout).setVisibility(View.INVISIBLE);

                Log.d("fasfafafa", deleteImg.getVisibility() + " if: " + UpdateTicketModal.isAllFreeTicketCanceled);
            } else
                Log.d("fasfafafa", deleteImg.getVisibility() + " getTicketView: " + UpdateTicketModal.isAllFreeTicketCanceled);


            if (cancelBtn.getVisibility() == View.VISIBLE) {
                /* existing ticket should not editable*/
                nameEt.setEnabled(false);
                priceTicketEt.setEnabled(false);
                ticketQtyEt.setEnabled(false);
                desc_et.setEnabled(false);
            } else {
                nameEt.setEnabled(true);
                priceTicketEt.setEnabled(true);
                ticketQtyEt.setEnabled(true);
                desc_et.setEnabled(true);
            }

            cancelBtn.setOnClickListener(v -> {
                if (dao.isTempCancel()) {
                    dao.setTempCancel(false);
                    cancelBtn.setBackgroundResource(R.drawable.custom_cancel_btn);
                    cancelBtn.setText("Cancel");
                    cancelTicketIdList.remove(Integer.valueOf((String) cancelBtn.getTag()));
                    return;
                }
                dao.setTempCancel(true);
                cancelBtn.setBackgroundResource(R.drawable.revert_cancel_custom_btn);
                cancelBtn.setText("Revert");
                cancelTicketIdList.add(Integer.parseInt((String) cancelBtn.getTag()));

            });
        }

        if (selectedType.equalsIgnoreCase("Free")) {
            view.findViewById(R.id.textView46).setVisibility(View.GONE);
            view.findViewById(R.id.price_ticket_et).setVisibility(View.GONE);
            view.findViewById(R.id.view7).setVisibility(View.GONE);
            view.findViewById(R.id.cancellation_charge_label).setVisibility(View.GONE);
            view.findViewById(R.id.cancellation_charge_sub_label).setVisibility(View.GONE);
            view.findViewById(R.id.layout_charges).setVisibility(View.GONE);
        }

        deleteImg.setOnClickListener(v -> {
            binding.ticketParent.removeView(view);
            deleteView(v, hidableView);
            if (isNewTicketAdded(selectedType)) {
                getPaidVisibleCancelledTickets();
            }
        });

        linearLayout = view.findViewById(R.id.add_ticket_layout);
        view.findViewById(R.id.add_ticket_layout).setOnClickListener(v -> {

            if (!validateTicket(view, true)) {
                isRequiredFormFill = true;
                return;
            } else {
                isRequiredFormFill = false;
            }


            String event_name_et = nameEt.getText().toString();
            String short_desc_et = desc_et.getText().toString();
            String totalTicketQty = ticketQtyEt.getText().toString();
            String price_ticket_et = priceTicketEt.getText().toString();

            Log.d("nflsanfkla", +freeTicketList.size() + " ==== " + isAllFreeTicketsCancelled + " getTicketView: " + isAllRegularTicketsCancelled + " == " + isAllVipNormalTicketsCancelled);


            if (selectedType.equalsIgnoreCase("Free") && freeTicketList.size() < counter || isAllFreeTicketsCancelled) {
                getTicketType = "freeNormal";
                saveDataFromView();
            } else if (selectedType.equalsIgnoreCase("Paid") && regularNormalList.size() < counter || isAllRegularTicketsCancelled) {
                getTicketType = "regularNormal";
                saveDataFromView();
            } else if (selectedType.equalsIgnoreCase("VIP") && vipNormalList.size() < counter || isAllVipNormalTicketsCancelled) {
                getTicketType = "vipNormal";
                saveDataFromView();
            }
//            hidableView.setVisibility(View.GONE);
//            Log.d("fnlsaknfklsa", "before: " + addTicketModals.size());
//            counter++;
//            getTicketView();
//            Log.d("fnlsaknfklsa", "middle: " + addTicketModals.size());
//
//            Log.d("fnlsaknfklsa", "last: " + addTicketModals.size());


            if (!clickByUpdateBtn) {
                //Log.d("fnlsaknfklsa", "In.....: " );
                counter++;
                getTicketView();
                hidableView.setVisibility(View.GONE);
            } else {
                // Log.d("fnlsaknfklsa", "Elseee.....: " );
                if (freeTicketList.size() > counter + 1)
                    hidableView.setVisibility(View.VISIBLE);
                clickByUpdateBtn = false;
            }


        });

        binding.ticketParent.addView(view);

        Log.d("fnkasnfa", selectedType + " added: " + binding.ticketParent.getChildCount());

        if (selectedType.equalsIgnoreCase("Free")) {
            if (updateTicketModal.getFreeTicketEnabled() == 0) {
                switchMaterial.setChecked(false);
                // disableView(switchMaterial,showCancelTicket,false);
                Log.d("fnakls", "getTicketView: " + showCancelTicket.getTag());

                if (!showCancelTicket.getTag().equals("previousTicket")) {
                    //  switchMaterial.setChecked(false);
                    //headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                    shadowView.setVisibility(View.VISIBLE);
                }
            }

        } else if (selectedType.equalsIgnoreCase("Paid")) {
            if (updateTicketModal.getRSVPTicketEnabled() == 0) {
                switchMaterial.setChecked(false);
                headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                shadowView.setVisibility(View.VISIBLE);
            }
        } else if (updateTicketModal.getVIPTicketEnabled() == 0) {
            switchMaterial.setChecked(false);
            headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
            shadowView.setVisibility(View.VISIBLE);
        }
    }

    private void getSeatingView() {
        regularSeatingTicketCount = 0;
        vipSeatingTicketCount = 0;
        UpdateTicketModal.VipTableSeating dao = null;
        if (seatCounter != 0) {
            View view = binding.seatingParentLayout.getChildAt(binding.seatingParentLayout.getChildCount() - 1);
            if (!validateSeating(view, true)) return;
        }
        seatingView = layoutInflater.inflate(R.layout.seating_layout, binding.seatingParentLayout, false);
        TextView cancelBtn = seatingView.findViewById(R.id.cancelBtn);
        RelativeLayout showCancelTicket = seatingView.findViewById(R.id.showCancelTicket);
        showCancelTicket.setTag("demo");
        TextView headerTitle = seatingView.findViewById(R.id.header_title);
        headerTitle.setText("Disable " + selectedType + " Tables and Seating");
        SwitchMaterial switchMaterial = seatingView.findViewById(R.id.switchMaterial);
        long id = System.currentTimeMillis();
        seatingView.setTag(id);
        addSeatBtn = seatingView.findViewById(R.id.add_seat_layout);
        ConstraintLayout hidableView = seatingView.findViewById(R.id.innerLayout);
        RelativeLayout topView = seatingView.findViewById(R.id.top_view);
        RelativeLayout shadowView = seatingView.findViewById(R.id.shadowView);
        shadowView.setTag(false);
        ImageView deleteImg = seatingView.findViewById(R.id.del_btn);
        TextView tvAddTicketBtn = seatingView.findViewById(R.id.tvAddTicketBtn);
        TextView priceCancellationText = seatingView.findViewById(R.id.priceCancellationTable);


        tvAddTicketBtn.setOnClickListener(v -> {
            getSeatingView();
        });


        shadowView.setOnClickListener(v -> {
        });

        topView.setOnClickListener(v -> {
            if (hidableView.getVisibility() == View.VISIBLE)
                hidableView.setVisibility(View.GONE);
            else
                hidableView.setVisibility(View.VISIBLE);
        });


        deleteImg.setOnClickListener(v -> {
            binding.seatingParentLayout.removeView(seatingView);
            deleteSeatingView(v);
            if (isNewTicketAdded(selectedType)) {
                getSeatingVisibleCancelledTickets();
            }
        });

        addSeatBtn.setOnClickListener(v -> {
            if (!validateSeating(seatingView, true)) {
                isRequiredFormFill = true;
                return;
            }
            isRequiredFormFill = false;
            clickByUpdateBtn = false;


            Log.d("nflsanfkla", isAllRegularSeatingTicketsCancelled + " saveSeatDataFromView: " + isAllVipSeatingTicketsCancelled);
            if (selectedType.equalsIgnoreCase("Paid") && regularTableSeatingList.size() < seatCounter || isAllRegularSeatingTicketsCancelled)
                saveSeatDataFromView(seatingView);

            else if (selectedType.equalsIgnoreCase("VIP") && vipTableSeatingList.size() < seatCounter || isAllVipSeatingTicketsCancelled)
                saveSeatDataFromView(seatingView);

            hidableView.setVisibility(View.GONE);
            seatCounter++;
            getSeatingView();
        });

        seatingView.setId(seatingCounterId++);
        deleteImg.setId(switchId++);
        deleteImg.setTag(id);
        switchMaterial.setTag(id);

        if (seatCounter == 0) {
            MaterialCardView enableDisableCard = seatingView.findViewById(R.id.enable_disable_card);
            enableDisableCard.setVisibility(View.VISIBLE);
            switchMaterial.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.INVISIBLE);

            switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    headerTitle.setText("Disable " + selectedType + " Tables and Seating");
                    enableDisableSeat(b, showCancelTicket);
                    DisableEnableAllSeatViews(b);
                    setDisableEnableFlag(switchMaterial.getTag().toString(), false);
                } else {
                    headerTitle.setText("Enable " + selectedType + " Tables and Seating");
                    enableDisableSeat(b, showCancelTicket);
                    DisableEnableAllSeatViews(b);
                    setDisableEnableFlag(switchMaterial.getTag().toString(), true);
                }
            });

        }

        ImageView add1 = seatingView.findViewById(R.id.add1);
        ImageView add2 = seatingView.findViewById(R.id.add2);
        ImageView remove1 = seatingView.findViewById(R.id.remove1);
        ImageView remove2 = seatingView.findViewById(R.id.remove2);

        TextView totalTable = seatingView.findViewById(R.id.no_of_table_tv);
        TextView personPerTable = seatingView.findViewById(R.id.person_per_table_tv);
        EditText category = seatingView.findViewById(R.id.event_name_et);
        EditText pricePerTable = seatingView.findViewById(R.id.price_per_table_et);

        EditText desc = seatingView.findViewById(R.id.short_desc_et);

        add1.setOnClickListener(v -> {
            String s = totalTable.getText().toString();
            totalTable.setText((Integer.valueOf(s) + 1) + "");
        });

        add2.setOnClickListener(v -> {
            String s = personPerTable.getText().toString();
            personPerTable.setText((Integer.valueOf(s) + 1) + "");
        });

        remove1.setOnClickListener(v -> {
            String s = totalTable.getText().toString();
            if (!s.equalsIgnoreCase("1")) {
                totalTable.setText((Integer.valueOf(s) - 1) + "");
            }
        });

        remove2.setOnClickListener(v -> {
            String s = personPerTable.getText().toString();
            if (!s.equalsIgnoreCase("1")) {
                personPerTable.setText((Integer.valueOf(s) - 1) + "");
            }
        });


        if (selectedType.equalsIgnoreCase("VIP") && vipTableSeatingList.size() > seatCounter) {
            dao = vipTableSeatingList.get(seatCounter);
        } else if (selectedType.equalsIgnoreCase("Paid") && regularTableSeatingList.size() > seatCounter) {
            dao = regularTableSeatingList.get(seatCounter);
        }

        if (dao != null) {
            cancelBtn.setTag(dao.getId());
            Log.e("setting", dao.toString());
            deleteImg.setTag(dao.getId());
            switchMaterial.setTag(dao.getId());
            totalTable.setText(dao.getNoOfTables() + "");
            personPerTable.setText(dao.getParsonPerTable() + "");
            category.setText(dao.getTicketName());
            pricePerTable.setText(dao.getPricePerTable() + "");
            desc.setText(dao.getDescription());
            shadowView.setTag(dao.getIsCancelled());
            priceCancellationText.setText(String.valueOf(dao.getCancellationChargeInPer()));
            Log.d("fasm", "getSeatingView: " + dao.getIsCancelled());
            //setupDisableSeatingView(headerTitle, switchMaterial, showCancelTicket);
            setCancelTicket(dao.getIsCancelled(), cancelBtn, showCancelTicket, dao.isTempCancel(), dao.isAddedNewTicket(), deleteImg, shadowView);
            if (selectedType.equalsIgnoreCase("VIP") && vipTableSeatingList.size() > seatCounter + 1) {
                hidableView.setVisibility(View.GONE);
                if (tvAddTicketBtn.getVisibility() == View.VISIBLE) {
                    tvAddTicketBtn.setVisibility(View.GONE);
                }

            } else if (selectedType.equalsIgnoreCase("Paid") && regularTableSeatingList.size() > seatCounter + 1) {
                hidableView.setVisibility(View.GONE);
                if (tvAddTicketBtn.getVisibility() == View.VISIBLE) {
                    tvAddTicketBtn.setVisibility(View.GONE);
                }
            }

            if (cancelBtn.getVisibility() == View.VISIBLE) {
                /* existing ticket should not editable*/
                category.setEnabled(false);
                pricePerTable.setEnabled(false);
                add1.setEnabled(false);
                add2.setEnabled(false);
                remove1.setEnabled(false);
                remove2.setEnabled(false);
                desc.setEnabled(false);
            } else {
                category.setEnabled(true);
                pricePerTable.setEnabled(true);
                add1.setEnabled(true);
                add2.setEnabled(true);
                remove1.setEnabled(true);
                remove2.setEnabled(true);
                desc.setEnabled(true);
            }

            UpdateTicketModal.VipTableSeating finalDao = dao;
            cancelBtn.setOnClickListener(v -> {
                if (finalDao.isTempCancel()) {
                    finalDao.setTempCancel(false);
                    cancelBtn.setBackgroundResource(R.drawable.custom_cancel_btn);
                    cancelBtn.setText("Cancel");
                    cancelTicketIdList.remove(Integer.valueOf((String) cancelBtn.getTag()));
                    return;
                }
                //dao.setIsCancelled(true);
                finalDao.setTempCancel(true);
                cancelBtn.setBackgroundResource(R.drawable.revert_cancel_custom_btn);
                cancelBtn.setText("Revert");
                cancelTicketIdList.add(Integer.parseInt((String) cancelBtn.getTag()));

            });
        }


        binding.seatingParentLayout.addView(seatingView);

//        if (updateTicketModal.getRSVPSeatEnabled() == 0 || updateTicketModal.getVipSeatEnabled() == 0) {
//            switchMaterial.setChecked(false);
//            enableDisableTicket(false, showCancelTicket);
//            DisableEnableAllTicketViews(false);
//        }

        if (selectedType.equalsIgnoreCase("VIP") && updateTicketModal.getVipSeatEnabled() == 0) {
            switchMaterial.setChecked(false);
            enableDisableSeat(false, showCancelTicket);
            DisableEnableAllSeatViews(false);
        } else if (selectedType.equalsIgnoreCase("Paid") && updateTicketModal.getRSVPSeatEnabled() == 0) {
            switchMaterial.setChecked(false);
            enableDisableSeat(false, showCancelTicket);
            DisableEnableAllSeatViews(false);

        }

    }

    private void setCancelTicket(boolean isCancelled, TextView cancelBtn, RelativeLayout showCancelTicket, boolean isTemporaryCancelTicket, boolean isAddedTicket, ImageView deleteImg, RelativeLayout shadowView) {
        Log.d("fnlan", "setCancelTicket: " + isCancelled);

        if (isCancelled) {
            showCancelTicket.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.GONE);
            shadowView.setVisibility(View.VISIBLE);
            shadowView.setOnClickListener(v -> {
            });

        } else {
            if (isAddedTicket) return;
            showCancelTicket.setVisibility(View.GONE);
            showCancelTicket.setTag("previousTicket");
            cancelBtn.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.INVISIBLE);
            if (isTemporaryCancelTicket) {
                cancelBtn.setBackgroundResource(R.drawable.revert_cancel_custom_btn);
                cancelBtn.setText("Revert");
            }
        }
    }

    private void enableDisableTicket(boolean b, RelativeLayout showCancelTicket) {
        if (selectedType.equalsIgnoreCase("Free")) {
            Log.e("called", ".....");
            updateTicketModal.setFreeTicketEnabled(b ? 1 : 0);
        } else if (selectedType.equalsIgnoreCase("Paid"))
            updateTicketModal.setRSVPTicketEnabled(b ? 1 : 0);
        else
            updateTicketModal.setVIPTicketEnabled(b ? 1 : 0);
        //}

    }

    private void enableDisableSeat(boolean b, RelativeLayout showCancelTicket) {
        if (selectedType.equalsIgnoreCase("Paid"))
            updateTicketModal.setRSVPSeatEnabled(b ? 1 : 0);
        else
            updateTicketModal.setVipSeatEnabled(b ? 1 : 0);
    }

    private void deleteView(View view, LinearLayout hidableView) {
        if (selectedType.equalsIgnoreCase("Free")) {
            freeTicketList.remove(new UpdateTicketModal.FreeTicket(view.getTag().toString()));
            deleteAddedTicket(view);
        } else if (selectedType.equalsIgnoreCase("Paid")) {
            deleteAddedTicket(view);
            regularNormalList.remove(new UpdateTicketModal.RegularNormal(view.getTag().toString()));
        } else {
            deleteAddedTicket(view);
            vipNormalList.remove(new UpdateTicketModal.RegularNormal(view.getTag().toString()));
        }
    }

    private void deleteSeatingView(View view) {
        if (selectedType.equalsIgnoreCase("Paid")) {
            regularTableSeatingList.remove(new UpdateTicketModal.VipTableSeating(view.getTag().toString()));
            deleteAddedTicket(view);
        } else {
            vipTableSeatingList.remove(new UpdateTicketModal.VipTableSeating(view.getTag().toString()));
            deleteAddedTicket(view);
        }
    }

    private void DisableEnableAllTicketViews(boolean b) {
        for (int i = 0; i < binding.ticketParent.getChildCount(); i++) {
            if (binding.ticketParent.getChildAt(i).findViewById(R.id.cancelBtn).isShown() || binding.ticketParent.getChildAt(i).findViewById(R.id.showCancelTicket).getTag().equals("previousTicket")) {
                continue;
            } else if ((boolean) binding.ticketParent.getChildAt(i).findViewById(R.id.shadowView).getTag()) {
                continue;
            }
            binding.ticketParent.getChildAt(i).findViewById(R.id.shadowView).setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }

    private void DisableEnableAllSeatViews(boolean b) {
        for (int i = 0; i < binding.seatingParentLayout.getChildCount(); i++) {
            if (binding.seatingParentLayout.getChildAt(i).findViewById(R.id.cancelBtn).isShown() || binding.seatingParentLayout.getChildAt(i).findViewById(R.id.showCancelTicket).getTag().equals("previousTicket")) {
                continue;
            } else if ((boolean) binding.seatingParentLayout.getChildAt(i).findViewById(R.id.shadowView).getTag()) {
                continue;
            }
            binding.seatingParentLayout.getChildAt(i).findViewById(R.id.shadowView).setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }

    private void saveSeatDataFromView(View currentView) {
        Log.d("fnasklnsla", regularTableSeatingList.size() + " saveSeatDataFromView: " + vipTableSeatingList.size());
        View view = binding.seatingParentLayout.getChildAt(
                binding.seatingParentLayout.getChildCount() - 1);
        if (validateSeating(view, false)) {
            String event_name_et = ((EditText) view.findViewById(R.id.event_name_et)).getText().toString();
            String price_per_table_et = ((TextView) view.findViewById(R.id.price_per_table_et)).getText().toString();
            String no_of_table_tv = ((TextView) view.findViewById(R.id.no_of_table_tv)).getText().toString();
            String person_per_table_tv = ((TextView) view.findViewById(R.id.person_per_table_tv)).getText().toString();
            String short_desc_et = ((EditText) view.findViewById(R.id.short_desc_et)).getText().toString();
            String cancellationCharges = ((TextView) view.findViewById(R.id.priceCancellationTicket)).getText().toString();

            UpdateTicketModal.VipTableSeating dao = new UpdateTicketModal.VipTableSeating();
            dao.setId(view.getTag().toString());
            dao.setTicketName(event_name_et);
            dao.setPricePerTable(Double.valueOf(price_per_table_et));
            dao.setNoOfTables(Integer.valueOf((no_of_table_tv)));
            dao.setParsonPerTable(Integer.valueOf(person_per_table_tv));
            dao.setCancellationChargeInPer(Integer.valueOf(cancellationCharges));
            dao.setDescription((short_desc_et));
            dao.setAddedNewTicket(true);
            dao.setTicketDisable(true);

            if (selectedType.equalsIgnoreCase("VIP")) {
                /* price per ticket gets in regular normal also total qty or vip normal*/
                getTicketType = "vipTableSeating";
                addTicketModals.add(setAddTicketToList((currentView.getTag().toString()), event_name_et, Integer.valueOf(no_of_table_tv), Integer.valueOf(price_per_table_et), short_desc_et, Integer.valueOf(person_per_table_tv), getTicketType, 0, 0, Integer.valueOf(cancellationCharges)));
                vipTableSeatingList.add(dao);
            } else {
                getTicketType = "regularTableSeating";
                addTicketModals.add(setAddTicketToList((currentView.getTag().toString()), event_name_et, Integer.valueOf(no_of_table_tv), Integer.valueOf(price_per_table_et),
                        short_desc_et, Integer.valueOf(person_per_table_tv), getTicketType, 0, 0, Integer.valueOf(cancellationCharges)));
                regularTableSeatingList.add(dao);
            }
        }
    }

    private void saveDataFromView() {
        View view = binding.ticketParent.getChildAt(binding.ticketParent.getChildCount() - 1);

        String event_name_et = ((EditText) view.findViewById(R.id.event_name_et)).getText().toString();
        String short_desc_et = ((EditText) view.findViewById(R.id.short_desc_et)).getText().toString();
        String totalTicketQty = ((EditText) view.findViewById(R.id.ticket_qty_et)).getText().toString();
        String price_ticket_et = ((EditText) view.findViewById(R.id.price_ticket_et)).getText().toString();
        String cancellationChargesText = ((TextView) view.findViewById(R.id.priceCancellationTicket)).getText().toString();


        if (validateTicket(view, false)) {
            if (selectedType.equalsIgnoreCase("Free")) {
                UpdateTicketModal.FreeTicket dao = new UpdateTicketModal.FreeTicket();
                dao.setId(view.getTag().toString());


                Log.d("fnlskanfal", " before: " + event_name_et + "\n" + totalTicketQty);

                //String price_ticket_et = view.findViewById(R.id.event_name_et).toString();


                dao.setDescription(short_desc_et);
                dao.setTicketName(event_name_et);


                dao.setTotalQuantity(Integer.parseInt(totalTicketQty));
                dao.setAddedNewTicket(true);
                dao.setIsCancelled(false);
                dao.setTicketDisable(freeDisableFlag);
                Log.d(">>>>>>>>>", "free before " + dao.isTicketDisable());
                //dao.setTicketDisable(true);
                Log.d(">>>>>>>>>", "free after " + dao.isTicketDisable());


                Log.d("fnlskanfal", selectedType + " ticketType FREEEE: " + getTicketType);


                freeTicketList.add(dao);

                if (!showCancelTicket.getTag().equals("previousTicket")) {
                    addTicketModals.add(setAddTicketToList(view.getTag().toString(), event_name_et, 0, 0, short_desc_et, 0, getTicketType, 0, Integer.valueOf(totalTicketQty), 0));
                }


            } else {
                UpdateTicketModal.RegularNormal dao = new UpdateTicketModal.RegularNormal();
                dao.setId(view.getTag().toString());

                dao.setDescription(short_desc_et);
                dao.setTicketName(event_name_et);
                dao.setTotalQuantity(Integer.valueOf(totalTicketQty));
                dao.setCancellationChargeInPer(Integer.valueOf(cancellationChargesText));
                dao.setAddedNewTicket(true);
                dao.setIsCancelled(false);
                dao.setTicketDisable(true);
                dao.setPricePerTicket(Double.valueOf(price_ticket_et));

                Log.d("fnlskanfal", price_ticket_et + " Seating : " + dao.getPricePerTicket());

                if (selectedType.equalsIgnoreCase("VIP"))
                    vipNormalList.add(dao);
                else
                    regularNormalList.add(dao);
                Log.d("fnlskanfal", selectedType + " TYPEEEE: " + getTicketType);

                if (!showCancelTicket.getTag().equals("previousTicket")) {
                    addTicketModals.add(setAddTicketToList(view.getTag().toString(), event_name_et, 0, 0, short_desc_et,
                            0, getTicketType, Integer.parseInt(price_ticket_et), Integer.valueOf(totalTicketQty), Integer.valueOf(cancellationChargesText)));
                }


            }
        }
    }

    private void saveTicketLastRowData() {

        Log.d("fnkasnfa", selectedType + " saveTicketLastRowData: " + freeTicketList.size() + " --- " + binding.ticketParent.getChildCount());
        if (selectedType.equalsIgnoreCase("Free") && freeTicketList.size() != binding.ticketParent.getChildCount()) {
            getTicketType = "freeNormal";
            saveDataFromView();
        } else if (selectedType.equalsIgnoreCase("VIP") && vipNormalList.size() != binding.ticketParent.getChildCount()) {
            getTicketType = "vipNormal";
            saveDataFromView();
        } else if (selectedType.equalsIgnoreCase("Paid") && regularNormalList.size() != binding.ticketParent.getChildCount()) {
            getTicketType = "regularNormal";
            saveDataFromView();
        }


    }

    private AddTicketModal setAddTicketToList(String addedTicketId, String ticketName, Integer noOfTables, Integer pricePerTable, String description,
                                              Integer parsonPerTable, String ticketType, Integer pricePerTicket, Integer totalQuantity, Integer cancellationChargeInPer) {
        AddTicketModal addTicketModal = new AddTicketModal();
        addTicketModal.setAddedTicketId(addedTicketId);
        addTicketModal.setTicketName(ticketName);
        addTicketModal.setNoOfTables(noOfTables);
        addTicketModal.setPricePerTable(pricePerTable);
        addTicketModal.setDescription(description);
        addTicketModal.setParsonPerTable(parsonPerTable);
        addTicketModal.setTicketType(ticketType);
        addTicketModal.setPricePerTicket(pricePerTicket);
        addTicketModal.setTotalQuantity(totalQuantity);
        addTicketModal.setCancellationChargeInPer(cancellationChargeInPer);
        addTicketModal.setTicketDisable(false);
        return addTicketModal;
    }

    private void deleteAddedTicket(View view) {
        Log.d(view.getTag().toString(), "before: " + addTicketModals.toString());
        addTicketModals.remove(new AddTicketModal(view.getTag().toString()));
        Log.d("fanfklas", "deleteAddedTicket: " + addTicketModals.size());
    }

    private void setDisableEnableFlag(String viewTag, boolean isTicketDisable) {
        Log.d("fnaklfnlsa", " before: " + addTicketModals.size());


        for (int i = 0; i < addTicketModals.size(); i++) {
            // Log.d("nflksankfla", isTicketDisable+" outer: "+addTicketModals.get(i).isTicketDisable());
//            if (viewTag.equalsIgnoreCase(addTicketModals.get(i).getAddedTicketId())) {

            AddTicketModal presentTicketData = addTicketModals.get(i);
            AddTicketModal updateAddTicketDataObj = new AddTicketModal();
            updateAddTicketDataObj.setAddedTicketId(presentTicketData.getAddedTicketId());
            updateAddTicketDataObj.setTicketName(presentTicketData.getTicketName());
            updateAddTicketDataObj.setNoOfTables(presentTicketData.getNoOfTables());
            updateAddTicketDataObj.setPricePerTable(presentTicketData.getPricePerTable());
            updateAddTicketDataObj.setDescription(presentTicketData.getDescription());
            updateAddTicketDataObj.setParsonPerTable(presentTicketData.getParsonPerTable());
            updateAddTicketDataObj.setTicketType(presentTicketData.getTicketType());
            updateAddTicketDataObj.setPricePerTicket(presentTicketData.getPricePerTicket());
            updateAddTicketDataObj.setTotalQuantity(presentTicketData.getTotalQuantity());
            updateAddTicketDataObj.setCancellationChargeInPer(presentTicketData.getCancellationChargeInPer());
            updateAddTicketDataObj.setTicketDisable(isTicketDisable);
            addTicketModals.set(i, updateAddTicketDataObj);
            Log.d("nflksankfla", isTicketDisable + " inner: " + addTicketModals.get(i).isTicketDisable() + " -- " + i);

        }
    }

    private void updateTicketRequest(JsonObject jsonObject) {
        myLoader.show("");
        Call<JsonElement> updateEventTickets = apiInterface.updateEventTickets(eventId, jsonObject);
        updateEventTickets.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                myLoader.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(UpdateTicketActivity.this, "Tickets has been updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UpdateTicketActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                myLoader.dismiss();
                Toast.makeText(UpdateTicketActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNewTicketAdded(String selectedType) {
        boolean isEmptyTicketAddedList = false;
        int freeNormal = 0, paidNormal = 0, seatingTicket = 0;

        for (int i = 0; i < addTicketModals.size(); i++) {
            String ticketType = addTicketModals.get(i).getTicketType();
            Log.d("fnaslknfkla", i + " loope " + ticketType);
            if (selectedType.equalsIgnoreCase("Free") && ticketType.equalsIgnoreCase("Free Normal")) {
                freeNormal++;
            } else if (selectedType.equalsIgnoreCase("VIP") && ticketType.equalsIgnoreCase("VIP Normal")) {
                paidNormal++;
            } else if (selectedType.equalsIgnoreCase("Paid") && ticketType.equalsIgnoreCase("Regular Normal")) {
                paidNormal++;
            } else if (selectedType.equalsIgnoreCase("VIP") && ticketType.equalsIgnoreCase("Vip Table Seating")) {
                seatingTicket++;
            } else if (selectedType.equalsIgnoreCase("Paid") && ticketType.equalsIgnoreCase("Regular Table Seating")) {
                seatingTicket++;
            }
        }


        if (selectedType.equalsIgnoreCase("Free") && freeNormal == 0) {
            isEmptyTicketAddedList = true;
            Log.d("fnaslknfkla", freeNormal + " freeNormal: " + paidNormal + "  --- " + seatingTicket + " === " + isEmptyTicketAddedList);
            return isEmptyTicketAddedList;
        }
        if (selectedType.equalsIgnoreCase("Paid") && paidNormal == 0) {
            isEmptyTicketAddedList = true;
            Log.d("fnaslknfkla", freeNormal + " paidNormal: " + paidNormal + "  --- " + seatingTicket + " === " + isEmptyTicketAddedList);
            return isEmptyTicketAddedList;

        }
        if (selectedType.equalsIgnoreCase("VIP") && seatingTicket == 0) {
            isEmptyTicketAddedList = true;
            Log.d("fnaslknfkla", freeNormal + " seatingTicket: " + paidNormal + "  --- " + seatingTicket + " === " + isEmptyTicketAddedList);
            return isEmptyTicketAddedList;

        }
        Log.d("fnaslknfkla", freeNormal + " isNewTicketAdded: " + paidNormal + "  --- " + seatingTicket + " === " + isEmptyTicketAddedList);

        return isEmptyTicketAddedList;
    }

    private void getPaidVisibleCancelledTickets() {
        for (int j = 0; j < binding.ticketParent.getChildCount(); j++) {
            View view1 = binding.ticketParent.getChildAt(j);
            LinearLayout hidableView = view1.findViewById(R.id.linearLayout6);
            TextView tvAddTicketBtn = view1.findViewById(R.id.tvAddTicketBtn);
            if (hidableView.getVisibility() == View.GONE && tvAddTicketBtn.getVisibility() == View.GONE) {
                hidableView.setVisibility(View.VISIBLE);
                tvAddTicketBtn.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void getSeatingVisibleCancelledTickets() {
        for (int j = 0; j < binding.seatingParentLayout.getChildCount(); j++) {
            View view1 = binding.seatingParentLayout.getChildAt(j);
            ConstraintLayout hidableView = view1.findViewById(R.id.innerLayout);
            TextView tvAddTicketBtn = view1.findViewById(R.id.tvAddTicketBtn);
            if (hidableView.getVisibility() == View.GONE && tvAddTicketBtn.getVisibility() == View.GONE) {
                hidableView.setVisibility(View.VISIBLE);
                tvAddTicketBtn.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void countCancelledFreeTickets() {
        int cancelledFreTicketCount = 0;
        for (int i = 0; i < freeTicketList.size(); i++) {
            boolean isCancelled = freeTicketList.get(i).getIsCancelled();
            if (isCancelled) {
                cancelledFreTicketCount++;
            }
        }
        Log.d("famnskfan", cancelledFreTicketCount + " countCancelledFreeTickets: " + (freeTicketList.size() - 1));
        if (cancelledFreTicketCount == freeTicketList.size()) {
            isAllFreeTicketsCancelled = true;
            Log.d("famnskfan", cancelledFreTicketCount + " if: " + isAllFreeTicketsCancelled);
        } else {
            isAllFreeTicketsCancelled = false;
            Log.d("famnskfan", cancelledFreTicketCount + " else " + isAllFreeTicketsCancelled);
        }


        isAllRegularTicketsCancelled = false;
        isAllRegularSeatingTicketsCancelled = false;
        isAllVipNormalTicketsCancelled = false;
        isAllVipSeatingTicketsCancelled = false;
    }

    private void countCancelledRegularTickets() {
        int cancelledRegularTicketCount = 0;
        for (int i = 0; i < regularNormalList.size(); i++) {
            boolean isCancelled = regularNormalList.get(i).getIsCancelled();
            if (isCancelled) {
                cancelledRegularTicketCount++;
            }
        }
        if (cancelledRegularTicketCount == regularNormalList.size()) {
            isAllRegularTicketsCancelled = true;
        } else isAllRegularTicketsCancelled = false;
        isAllFreeTicketsCancelled = false;
        isAllRegularSeatingTicketsCancelled = false;
        isAllVipNormalTicketsCancelled = false;
        isAllVipSeatingTicketsCancelled = false;

    }

    private void countCancelledRegularSeatingTickets() {

        int cancelledRegularSeatingTicketCount = 0;
        for (int i = 0; i < regularTableSeatingList.size(); i++) {
            boolean isCancelled = regularTableSeatingList.get(i).getIsCancelled();
            if (isCancelled) {
                cancelledRegularSeatingTicketCount++;
            }
        }
        if (cancelledRegularSeatingTicketCount == regularTableSeatingList.size()) {
            isAllRegularSeatingTicketsCancelled = true;
        } else isAllRegularSeatingTicketsCancelled = false;
        isAllFreeTicketsCancelled = false;
        isAllRegularTicketsCancelled = false;
        isAllVipNormalTicketsCancelled = false;
        isAllVipSeatingTicketsCancelled = false;

    }

    private void countCancelledVipTickets() {
        int cancelledVipTicketCount = 0;
        for (int i = 0; i < vipNormalList.size(); i++) {
            boolean isCancelled = vipNormalList.get(i).getIsCancelled();
            if (isCancelled) {
                cancelledVipTicketCount++;
            }
        }
        if (cancelledVipTicketCount == vipNormalList.size()) {
            isAllVipNormalTicketsCancelled = true;
        } else isAllVipNormalTicketsCancelled = false;
        isAllFreeTicketsCancelled = false;
        isAllRegularTicketsCancelled = false;
        isAllRegularSeatingTicketsCancelled = false;
        isAllVipSeatingTicketsCancelled = false;

    }

    private void countCancelledVipSeatingTickets() {
        int cancelledVipSeatingTicketCount = 0;
        for (int i = 0; i < vipTableSeatingList.size(); i++) {
            boolean isCancelled = vipTableSeatingList.get(i).getIsCancelled();
            if (isCancelled) {
                cancelledVipSeatingTicketCount++;
            }
        }
        if (cancelledVipSeatingTicketCount == vipTableSeatingList.size()) {
            isAllVipSeatingTicketsCancelled = true;
        } else isAllVipSeatingTicketsCancelled = false;
        isAllFreeTicketsCancelled = false;
        isAllRegularTicketsCancelled = false;
        isAllRegularSeatingTicketsCancelled = false;
        isAllVipNormalTicketsCancelled = false;

    }


    private void saveSeatLastRowData() {
        if (selectedType.equalsIgnoreCase("VIP") && vipTableSeatingList.size() != binding.seatingParentLayout.getChildCount()) {
            getTicketType = "vipTableSeating";
            saveSeatDataFromView(seatingView);
        } else if (selectedType.equalsIgnoreCase("Paid") && regularTableSeatingList.size() != binding.seatingParentLayout.getChildCount()) {
            getTicketType = "regularTableSeating";
            saveSeatDataFromView(seatingView);
        }

    }

    @Override
    protected void onDestroy() {
        myLoader.dismiss();
        super.onDestroy();
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
}
