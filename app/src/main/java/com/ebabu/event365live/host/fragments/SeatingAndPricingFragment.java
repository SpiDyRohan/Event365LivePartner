package com.ebabu.event365live.host.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentSeatingAndPricingBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.FreeTicketDao;
import com.ebabu.event365live.host.entities.RSVPTicketDao;
import com.ebabu.event365live.host.entities.TableAndSeatingDao;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.StringUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SeatingAndPricingFragment extends Fragment {

    FragmentSeatingAndPricingBinding binding;
    private LayoutInflater layoutInflater;
    private int ticketCounterViewId = 1000;
    private int cancellationCharges = 100;
    private int delTicketId = 2000, switchId = 200;
    private CreateEventDAO createEventDAO;
    private int seatingCounterId = 100;
    private String selectedType;
    private List<FreeTicketDao> freeTicketDaoList;
    private List<RSVPTicketDao> rsvpTicketDaoList;
    private List<RSVPTicketDao> vipTicketDaoList;
    private List<TableAndSeatingDao> rsvpTableAndSeatingDAOList, vipTableAndSeatingDAOList;
    private int counter, seatCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_seating_and_pricing, container, false);
        layoutInflater = getLayoutInflater();


        createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();

        Log.d("fnaklsnsaf", "SeatingAndPricingFragment: " + createEventDAO.getPaidType());

        setSellingTimeData();

        freeTicketDaoList = createEventDAO.getFreeTicketDaoList();
        if (freeTicketDaoList == null) freeTicketDaoList = new ArrayList<>();

        rsvpTicketDaoList = createEventDAO.getRsvpTicketDaoList();

        if (rsvpTicketDaoList == null) rsvpTicketDaoList = new ArrayList<>();
        vipTicketDaoList = createEventDAO.getVipTicketDaoList();
        if (vipTicketDaoList == null) vipTicketDaoList = new ArrayList<>();

        rsvpTableAndSeatingDAOList = createEventDAO.getRsvpTableAndSeatingDaos();
        if (rsvpTableAndSeatingDAOList == null) rsvpTableAndSeatingDAOList = new ArrayList<>();

        vipTableAndSeatingDAOList = createEventDAO.getVipTableAndSeatingDaos();
        if (vipTableAndSeatingDAOList == null) vipTableAndSeatingDAOList = new ArrayList<>();

        List<String> typeList = StringUtils.getEventType(getContext(), createEventDAO.getPaidType());


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.pricing_spinner, typeList);


        dataAdapter.setDropDownViewResource(R.layout.pricing_spinner_layout);
        binding.spinner.setAdapter(dataAdapter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String s = "";
                switch (typeList.get(i)) {
                    case "Free RSVP Tickets":
                        s = "Free";
                        break;
                    case "Paid RSVP Tickets":
                        s = "Paid";
                        break;
                    case "VIP RSVP Tickets":
                        s = "VIP";
                        break;
                }
                setHeader(s);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());
        binding.nextBtn.setOnClickListener(v -> {

            saveTicketLastRowData();
            saveSeatLastRowData();

            if (selectedType.equalsIgnoreCase("Free") && createEventDAO.isFreeTicketEnabled() == 1) {
                if (!validateTicketView()) return;
            } else if (selectedType.equalsIgnoreCase("Paid") && createEventDAO.isRSVPTicketEnabled() == 1) {
                if (!validateTicketView()) {
                    return;
                }
                if (createEventDAO.isRSVPSeatEnabled() == 1) {
                    if (!validateSeatingTicketView()) {
                        return;
                    }
                }
            } else if (selectedType.equalsIgnoreCase("VIP") && createEventDAO.isVIPTicketEnabled() == 1) {
                if (!validateTicketView()) {
                    return;
                }
                if (createEventDAO.isVipSeatEnabled() == 1) {
                    if (!validateSeatingTicketView()) {
                        return;
                    }
                }
            }

            if (createEventDAO.getPaidType().equalsIgnoreCase("Free")) {
                if (createEventDAO.isFreeTicketEnabled() == 0) {
                    Dialogs.toast(getContext(), v, getString(R.string.minimumTicket));
                    return;
                }
            } else if (createEventDAO.getPaidType().equalsIgnoreCase("Paid")) {
                if (createEventDAO.isVIPTicketEnabled() == 0 &&
                        createEventDAO.isRSVPTicketEnabled() == 0 &&
                        createEventDAO.isRSVPSeatEnabled() == 0 &&
                        createEventDAO.isVipSeatEnabled() == 0) {
                    Dialogs.toast(getContext(), v, getString(R.string.minimumTicket));
                    return;
                }
                if (createEventDAO.isRSVPTicketEnabled() == 1 && rsvpTicketDaoList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one Paid ticket or disable it!");
                    binding.spinner.setSelection(0);
                    return;
                }

                if (createEventDAO.isRSVPSeatEnabled() == 1 && rsvpTableAndSeatingDAOList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one Paid Seating or disable it!");
                    binding.spinner.setSelection(0);
                    return;
                }

                if (createEventDAO.isVIPTicketEnabled() == 1 && vipTicketDaoList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one VIP ticket or disable it!");
                    binding.spinner.setSelection(1);
                    return;
                }
                if (createEventDAO.isVipSeatEnabled() == 1 && vipTableAndSeatingDAOList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one VIP Seating or disable it!");
                    binding.spinner.setSelection(1);
                    return;
                }
            } else if (createEventDAO.getPaidType().equalsIgnoreCase("Both")) {
                if (createEventDAO.isFreeTicketEnabled() == 0 && createEventDAO.isVIPTicketEnabled() == 0 && createEventDAO.isRSVPTicketEnabled() == 0) {
                    Dialogs.toast(getContext(), v, getString(R.string.minimumTicket));
                    return;
                }

                if (createEventDAO.isFreeTicketEnabled() == 1 && freeTicketDaoList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one Free ticket or disable it!");
                    binding.spinner.setSelection(0);
                    return;
                }

                if (createEventDAO.isRSVPTicketEnabled() == 1 && rsvpTicketDaoList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one Paid ticket or disable it!");
                    binding.spinner.setSelection(1);
                    return;
                }
                if (createEventDAO.isVIPTicketEnabled() == 1 && vipTicketDaoList.size() == 0) {
                    Dialogs.toast(getContext(), v, "Please choose atleast one VIP ticket or disable it!");
                    binding.spinner.setSelection(2);
                    return;
                }
            }


            String startDate = binding.textView39.getText().toString();
            if (startDate.equalsIgnoreCase(getString(R.string.start_date))) {
                Dialogs.toast(getContext(), v, "Choose Start Date please!");
                return;
            }

            String endDate = binding.endDateTv.getText().toString();
            if (endDate.equalsIgnoreCase(getString(R.string.end_date))) {
                Dialogs.toast(getContext(), v, "Choose End Date please!");
                return;
            }

            String startTime = binding.startTimeTv.getText().toString();
            if (startTime.equalsIgnoreCase(getString(R.string.start_time))) {
                Dialogs.toast(getContext(), v, "Choose Start Time please!");
                return;
            }

            String endTime = binding.endTimeTv.getText().toString();
            if (endTime.equalsIgnoreCase(getString(R.string.end_time))) {
                Dialogs.toast(getContext(), v, "Choose End Time please!");
                return;
            }

            createEventDAO.setSellStartTime(startTime);
            createEventDAO.setSellEndTime(endTime);
            createEventDAO.setSellStartDate(startDate);
            createEventDAO.setSellEndDate(endDate);

            createEventDAO.setFreeTicketDaoList(freeTicketDaoList);
            createEventDAO.setRsvpTicketDaoList(rsvpTicketDaoList);
            createEventDAO.setVipTicketDaoList(vipTicketDaoList);

            createEventDAO.setVipTableAndSeatingDaos(vipTableAndSeatingDAOList);
            createEventDAO.setRsvpTableAndSeatingDaos(rsvpTableAndSeatingDAOList);

            createEventDAO.setSeatingType(selectedType);

            Navigation.findNavController(v)
                    .navigate(
                            SeatingAndPricingFragmentDirections.actionSeatingAndPricingFragmentToCreateEventFullEvent()
                                    .setEventDAO(createEventDAO)
                    );

        });


        binding.imageView22.setOnClickListener(v -> Dialogs.setMinMaxDate(getContext(), "", createEventDAO.getEndDate(), binding.textView39, date -> {
            binding.textView39.setText(date);

            binding.startTimeTv.setText(getString(R.string.start_time));
            binding.endDateTv.setText(getString(R.string.end_date));
            binding.endTimeTv.setText(getString(R.string.end_time));
        }));

        binding.textView39.setOnClickListener(v -> {
            Dialogs.setMinMaxDate(getContext(), "", createEventDAO.getEndDate(), binding.textView39, date -> {
                binding.textView39.setText(date);
                binding.startTimeTv.setText(getString(R.string.start_time));
                binding.endDateTv.setText(getString(R.string.end_date));
                binding.endTimeTv.setText(getString(R.string.end_time));
            });
        });

        binding.endDateImg.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView39.getText().toString(), createEventDAO.getEndDate(), binding.textView39, date -> {
                    binding.endDateTv.setText(date);
                });
        });

        binding.endDateTv.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView39.getText().toString(), createEventDAO.getEndDate(), binding.textView39, date -> {
                    binding.endDateTv.setText(date);

                });
        });

        binding.startTimeImg.setOnClickListener(this::setStartTime);
        binding.startTimeTv.setOnClickListener(this::setStartTime);
        binding.endTimeImg.setOnClickListener(this::setEndTime);
        binding.endTimeTv.setOnClickListener(this::setEndTime);


        return binding.getRoot();
    }

    private void setStartTime(View v) {

        Dialogs.setTime(getContext(), binding.endTimeTv, time -> {
            if (!binding.textView39.getText().equals(createEventDAO.getEndDate()))
                binding.startTimeTv.setText(time);
            else if (compareTime(time, createEventDAO.getEndTime()))
                binding.startTimeTv.setText(time);

            else {
                Dialogs.toast(getContext(), v, "Sell start time must be earlier than Event end time");
                binding.startTimeTv.setText(getString(R.string.start_time));
            }
        });
    }

    private boolean compareTime(String startTime, String endTime) {

        int startHour = getHour(startTime);
        int endHour = getHour(endTime);

        if (endHour > startHour) return true;

        else if (endHour == startHour) {
            int startMin = Integer.valueOf(startTime.substring(3, 5));
            int endMin = Integer.valueOf(endTime.substring(3, 5));
            return endMin > startMin;
        }
        return false;
    }

    private int getHour(String time) {
        int startHour = Integer.valueOf(time.substring(0, 2));
        return time.matches("(.*)PM") && startHour < 12 ? (startHour + 12) : startHour;
    }

    private void setEndTime(View v) {
        if (binding.startTimeTv.getText().toString().equalsIgnoreCase(getString(R.string.start_time)))
            Toast.makeText(getContext(), "Select start time first!", Toast.LENGTH_LONG).show();
        else

            Dialogs.setTime(getContext(), binding.endTimeTv, time -> {
                if (!binding.textView39.getText().equals(binding.endDateTv.getText().toString())) {

                    if (binding.endDateTv.getText().equals(createEventDAO.getEndDate())) {
                        if (compareTime(time, createEventDAO.getEndTime()))
                            binding.endTimeTv.setText(time);
                        else {
                            Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event end time");
                            binding.endTimeTv.setText(getString(R.string.end_time));
                        }
                    } else
                        binding.endTimeTv.setText(time);
                } else {
                    String startTime = binding.startTimeTv.getText().toString();

                    if (compareTime(startTime, time))
                        if (binding.endDateTv.getText().equals(createEventDAO.getStartDate())) {
                            if (compareTime(time, createEventDAO.getStartTime()))
                                binding.endTimeTv.setText(time);
                            else {
                                Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event Start time");
                                binding.endTimeTv.setText(getString(R.string.end_time));
                            }
                        } else
                            binding.endTimeTv.setText(time);
                    else {
                        Dialogs.toast(getContext(), v, "Start time must be earlier than End time");
                        binding.endTimeTv.setText(getString(R.string.end_time));
                    }
                }
            });
    }

    private boolean validateTicketView() {
        for (int i = 0; i < binding.ticketParent.getChildCount(); i++) {
            View view = binding.ticketParent.getChildAt(i);
            if (!validateTicket(view, true)) return false;
        }
        return true;
    }

    private boolean validateSeatingTicketView() {
        for (int i = 0; i < binding.seatingParentLayout.getChildCount(); i++) {
            View view = binding.seatingParentLayout.getChildAt(i);
            if (!validateSeating(view, true)) return false;
        }
        return true;
    }

    private void setSellingTimeData() {
        if (createEventDAO.getSellEndDate() != null) {
            binding.startTimeTv.setText(createEventDAO.getSellStartTime());
            binding.endTimeTv.setText(createEventDAO.getSellEndTime());
            binding.endDateTv.setText(createEventDAO.getSellEndDate());
            binding.textView39.setText(createEventDAO.getSellStartDate());
        }
    }

    private boolean validateSeating(View view, boolean showMsg) {
        if (((EditText) view.findViewById(R.id.event_name_et)).getText().toString().trim().length() == 0) {
            if (showMsg) {
                Dialogs.toast(getContext(), view, "Enter Category Name Please!");
                ((EditText) view.findViewById(R.id.event_name_et)).requestFocus();
            }
            return false;
        }
        if (((EditText) view.findViewById(R.id.price_per_table_et)).getText().toString().trim().length() == 0) {
            if (showMsg) {
                Dialogs.toast(getContext(), view, "Enter Price Per Table!");
                ((EditText) view.findViewById(R.id.price_per_table_et)).requestFocus();
            }
            return false;
        }

        return true;
    }

    //todo MAIN function starts here when a new choice from spiner is fired
    private void setHeader(String s) {
        int total, seatingTotal = 0;

        if (selectedType != null && binding.ticketParent.getChildCount() != 0) {
            saveTicketLastRowData();
            saveSeatLastRowData();
        }
        selectedType = s;

        if (selectedType.equalsIgnoreCase("Free")) total = freeTicketDaoList.size();
        else if (selectedType.equalsIgnoreCase("VIP")) {
            total = vipTicketDaoList.size();
            seatingTotal = vipTableAndSeatingDAOList.size();
        } else {
            total = rsvpTicketDaoList.size();
            seatingTotal = rsvpTableAndSeatingDAOList.size();
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
            binding.seatingLayout.setVisibility(View.VISIBLE);
            seatCounter = 0;
            do {
                getSeatingView();
                seatCounter++;
            }
            while (seatCounter < seatingTotal);
        }

        for (int i = 0; i < binding.ticketParent.getChildCount(); i++) {

            View view = binding.ticketParent.getChildAt(i);
            ((TextView) view.findViewById(R.id.textView45)).setText(selectedType + " RSVP Ticket");

            if (s.equalsIgnoreCase("Free")) {
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

    private void saveTicketLastRowData() {
        if (selectedType.equalsIgnoreCase("Free") && freeTicketDaoList.size() != binding.ticketParent.getChildCount())
            saveDataFromView();
        else if (selectedType.equalsIgnoreCase("VIP") && vipTicketDaoList.size() != binding.ticketParent.getChildCount())
            saveDataFromView();
        else if (selectedType.equalsIgnoreCase("Paid") && rsvpTicketDaoList.size() != binding.ticketParent.getChildCount())
            saveDataFromView();
    }

    private void saveSeatLastRowData() {
        if (selectedType.equalsIgnoreCase("VIP") && vipTableAndSeatingDAOList.size() != binding.seatingParentLayout.getChildCount())
            saveSeatDataFromView();
        else if (selectedType.equalsIgnoreCase("Paid") && rsvpTableAndSeatingDAOList.size() != binding.seatingParentLayout.getChildCount())
            saveSeatDataFromView();
    }

    private void saveDataFromView() {
        View view = binding.ticketParent.getChildAt(binding.ticketParent.getChildCount() - 1);

        if (validateTicket(view, false)) {

            if (selectedType.equalsIgnoreCase("Free")) {

                FreeTicketDao dao = new FreeTicketDao();
                dao.setId(view.getTag().toString());
                dao.setDesc(((EditText) view.findViewById(R.id.short_desc_et)).getText().toString());
                dao.setTicketName(((EditText) view.findViewById(R.id.event_name_et)).getText().toString());
                dao.setTotalTicketsQuantity(((EditText) view.findViewById(R.id.ticket_qty_et)).getText().toString());
                freeTicketDaoList.add(dao);


            } else {
                RSVPTicketDao dao = new RSVPTicketDao();
                dao.setUid(view.getTag().toString());
                dao.setDesc(((EditText) view.findViewById(R.id.short_desc_et)).getText().toString());
                dao.setTicketName(((EditText) view.findViewById(R.id.event_name_et)).getText().toString());
                dao.setTotalTicketsQuantity(((EditText) view.findViewById(R.id.ticket_qty_et)).getText().toString());



                try {
                    dao.setPriceTicket(Double.parseDouble(((EditText) view.findViewById(R.id.price_ticket_et)).getText().toString()));
                    dao.setCancellationChargesTicket(Integer.parseInt(((TextView) view.findViewById(R.id.priceCancellationTicket)).getText().toString()));

                } catch (Exception e) {
                }

                if (selectedType.equalsIgnoreCase("VIP"))
                    vipTicketDaoList.add(dao);

                else
                    rsvpTicketDaoList.add(dao);
            }
        }
    }

    private void saveSeatDataFromView() {

        View view = binding.seatingParentLayout.getChildAt(
                binding.seatingParentLayout.getChildCount() - 1
        );

        if (validateSeating(view, false)) {

            TableAndSeatingDao dao = new TableAndSeatingDao();
            dao.setId(view.getTag().toString());
            dao.setDesc(((EditText) view.findViewById(R.id.short_desc_et)).getText().toString());
            dao.setCategoryName(((EditText) view.findViewById(R.id.event_name_et)).getText().toString());
            dao.setNoOfTables(Integer.parseInt(((TextView) view.findViewById(R.id.no_of_table_tv)).getText().toString()));
            dao.setPersonPerTable(Integer.parseInt(((TextView) view.findViewById(R.id.person_per_table_tv)).getText().toString()));

            try {
                dao.setPricePerTable(Double.parseDouble(((EditText) view.findViewById(R.id.price_per_table_et)).getText().toString()));

                dao.setCancellationChargesTable(Integer.parseInt(((TextView) view.findViewById(R.id.priceCancellationTable)).getText().toString()));

            } catch (Exception e) {
            }

            if (selectedType.equalsIgnoreCase("VIP"))
                vipTableAndSeatingDAOList.add(dao);
            else rsvpTableAndSeatingDAOList.add(dao);

        }
    }

    private void getTicketView() {
        View view = layoutInflater.inflate(R.layout.layout_ticket, binding.ticketParent, false);
        TextView headerTitle = view.findViewById(R.id.header_title);
        headerTitle.setText(selectedType + " RSVP Ticket");
        SwitchMaterial switchMaterial = view.findViewById(R.id.switchMaterial);
        RelativeLayout topView = view.findViewById(R.id.top_view);
        LinearLayout hidableView = view.findViewById(R.id.linearLayout6);
        RelativeLayout shadowView = view.findViewById(R.id.shadowView);
        shadowView.setOnClickListener(v -> {
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
        headerTitle.setText("Disable " + selectedType + " RSVP Ticket");

        deleteImg.setId(delTicketId++);
        deleteImg.setTag(id);
        if (counter == 0) {
            MaterialCardView enableDisableCard = view.findViewById(R.id.enable_disable_card);
            enableDisableCard.setVisibility(View.VISIBLE);

            switchMaterial.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.INVISIBLE);
            switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    headerTitle.setText("Disable " + selectedType + " RSVP Ticket");
                    enableDisableTicket(b);
                    DisableEnableAllTicketViews(b);
                    // TODO: 30/10/19  Dialogs.expand(hidableView);
                } else {
                    headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                    enableDisableTicket(b);
                    DisableEnableAllTicketViews(b);
                }
            });
        }

        if (selectedType.equalsIgnoreCase("Free")) {
            if (createEventDAO.isFreeTicketEnabled() == 0) {
                switchMaterial.setChecked(false);
                headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                shadowView.setVisibility(View.VISIBLE);
            }

        } else if (selectedType.equalsIgnoreCase("Paid")) {
            if (createEventDAO.isRSVPTicketEnabled() == 0) {
                switchMaterial.setChecked(false);
                headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
                shadowView.setVisibility(View.VISIBLE);
            }
        } else if (createEventDAO.isVIPTicketEnabled() == 0) {
            switchMaterial.setChecked(false);
            headerTitle.setText("Enable " + selectedType + " RSVP Ticket");
            shadowView.setVisibility(View.VISIBLE);
        }

        deleteImg.setOnClickListener(v -> {
            binding.ticketParent.removeView(view);
            deleteView(v);
        });

        EditText nameEt = view.findViewById(R.id.event_name_et);

        EditText priceTicketEt = view.findViewById(R.id.price_ticket_et);
        EditText ticketQtyEt = view.findViewById(R.id.ticket_qty_et);
        EditText desc_et = view.findViewById(R.id.short_desc_et);


        TextView cancellationChargesText = view.findViewById(R.id.priceCancellationTicket);

        if (selectedType.equalsIgnoreCase("Paid") && rsvpTicketDaoList.size() >= counter + 1) {
            RSVPTicketDao dao = rsvpTicketDaoList.get(counter);
            nameEt.setText(StringUtils.capitalizeWord(dao.getTicketName()));
            deleteImg.setTag(dao.getUid());
            priceTicketEt.setText(dao.getPriceTicket() == 0.0 ? "" : String.valueOf(dao.getPriceTicket()));
            ticketQtyEt.setText(dao.getTotalTicketsQuantity());
            desc_et.setText(dao.getDesc());

            cancellationChargesText.setText(String.valueOf(dao.getCancellationChargesTicket()));

            if (rsvpTicketDaoList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);
            }
        } else if (selectedType.equalsIgnoreCase("VIP") && vipTicketDaoList.size() >= counter + 1) {
            RSVPTicketDao dao = vipTicketDaoList.get(counter);
            nameEt.setText(StringUtils.capitalizeWord(dao.getTicketName()));
            deleteImg.setTag(dao.getUid());
            priceTicketEt.setText(dao.getPriceTicket() == 0.0 ? "" : String.valueOf(dao.getPriceTicket()));
            ticketQtyEt.setText(dao.getTotalTicketsQuantity());
            desc_et.setText(dao.getDesc());

            cancellationChargesText.setText(String.valueOf(dao.getCancellationChargesTicket()));

            if (vipTicketDaoList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);
            }

        } else if (selectedType.equalsIgnoreCase("Free") && freeTicketDaoList.size() >= counter + 1) {

            FreeTicketDao dao = freeTicketDaoList.get(counter);
            deleteImg.setTag(dao.getId());
            nameEt.setText(StringUtils.capitalizeWord(dao.getTicketName()));
            ticketQtyEt.setText(dao.getTotalTicketsQuantity());
            desc_et.setText(dao.getDesc());

            if (createEventDAO.isFreeTicketEnabled() == 0) shadowView.setVisibility(View.VISIBLE);

            if (freeTicketDaoList.size() > counter + 1) {
                hidableView.setVisibility(View.GONE);
            }
        }

        if (selectedType.equalsIgnoreCase("Free")) {
            view.findViewById(R.id.textView46).setVisibility(View.GONE);
            view.findViewById(R.id.price_ticket_et).setVisibility(View.GONE);
            view.findViewById(R.id.view7).setVisibility(View.GONE);

            view.findViewById(R.id.cancellation_charge_label).setVisibility(View.GONE);
            view.findViewById(R.id.cancellation_charge_sub_label).setVisibility(View.GONE);
            view.findViewById(R.id.layout_charges).setVisibility(View.GONE);
        }

        view.findViewById(R.id.increasePriceTicket).setOnClickListener(v -> {
            String s = cancellationChargesText.getText().toString();
            cancellationChargesText.setText((Integer.valueOf(s) + 1) + "");
        });

        view.findViewById(R.id.decreasePriceTicket).setOnClickListener(v -> {
            String s = cancellationChargesText.getText().toString();
            if (!s.equalsIgnoreCase("10")) {
                cancellationChargesText.setText((Integer.valueOf(s) - 1) + "");
            }
        });

        view.findViewById(R.id.add_ticket_layout).setOnClickListener(v -> {

            if (!validateTicket(view, true)) return;
            if (selectedType.equalsIgnoreCase("Free") && freeTicketDaoList.size() < counter)
                saveDataFromView();
            else if (selectedType.equalsIgnoreCase("Paid") && rsvpTicketDaoList.size() < counter)
                saveDataFromView();
            else if (selectedType.equalsIgnoreCase("VIP") && vipTicketDaoList.size() < counter)
                saveDataFromView();

            hidableView.setVisibility(View.GONE);
            getTicketView();
            counter++;
        });
        binding.ticketParent.addView(view);
    }

    private void getSeatingView() {

        if (seatCounter != 0) {
            if (selectedType.equalsIgnoreCase("VIP") && createEventDAO.isVipSeatEnabled() == 0) return;
            else if (createEventDAO.isRSVPSeatEnabled() == 0) return;

            View view = binding.seatingParentLayout.getChildAt(binding.seatingParentLayout.getChildCount() - 1);
            if (!validateSeating(view, true)) return;
        }

        View view = layoutInflater.inflate(R.layout.seating_layout, binding.seatingParentLayout, false);
        TextView headerTitle = view.findViewById(R.id.header_title);
        headerTitle.setText("Disable " + selectedType + " Tables and Seating");
        long id = System.currentTimeMillis();
        view.setTag(id);
        SwitchMaterial switchMaterial = view.findViewById(R.id.switchMaterial);
        LinearLayout addSeatBtn = view.findViewById(R.id.add_seat_layout);

        ConstraintLayout hidableView = view.findViewById(R.id.innerLayout);
        RelativeLayout topView = view.findViewById(R.id.top_view);
        RelativeLayout shadowView = view.findViewById(R.id.shadowView);
        shadowView.setOnClickListener(v -> {
        });

        topView.setOnClickListener(v -> {
            if (hidableView.getVisibility() == View.VISIBLE)
                hidableView.setVisibility(View.GONE);
            else
                hidableView.setVisibility(View.VISIBLE);
        });


        headerTitle.setText("Disable " + selectedType + " Table and Seating");
        addSeatBtn.setOnClickListener(v -> {

            if (!validateSeating(view, true)) return;
            if (selectedType.equalsIgnoreCase("Paid") &&
                    rsvpTableAndSeatingDAOList.size() < seatCounter)
                saveSeatDataFromView();

            else if (selectedType.equalsIgnoreCase("VIP") &&
                    vipTableAndSeatingDAOList.size() < seatCounter)
                saveSeatDataFromView();

            hidableView.setVisibility(View.GONE);
            getSeatingView();
            seatCounter++;
        });

        view.setId(seatingCounterId++);
        ImageView deleteImg = view.findViewById(R.id.del_btn);

        deleteImg.setId(switchId++);
        deleteImg.setTag(id);

        if (seatCounter == 0) {

            MaterialCardView enableDisableCard = view.findViewById(R.id.enable_disable_card);
            enableDisableCard.setVisibility(View.VISIBLE);
            switchMaterial.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.INVISIBLE);

            switchMaterial.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    enableDisableSeat(b);
                    headerTitle.setText("Disable " + selectedType + " Table and Seating");
                    DisableEnableAllSeatViews(b);
                } else {
                    enableDisableSeat(b);
                    headerTitle.setText("Enable " + selectedType + " Table and Seating");
                    DisableEnableAllSeatViews(b);
                }

            });

            if (selectedType.equalsIgnoreCase("VIP") && createEventDAO.isVipSeatEnabled() == 0)
                switchMaterial.setChecked(false);

            else if (selectedType.equalsIgnoreCase("Paid") && createEventDAO.isRSVPSeatEnabled() == 0)
                switchMaterial.setChecked(false);
        }


        if (selectedType.equalsIgnoreCase("VIP")) {
            if (createEventDAO.isVipSeatEnabled() == 0) {
                switchMaterial.setChecked(false);
                headerTitle.setText("Enable " + selectedType + " Table and Seating");
                shadowView.setVisibility(View.VISIBLE);
            }

        } else if (selectedType.equalsIgnoreCase("Paid")) {
            if (createEventDAO.isRSVPSeatEnabled() == 0) {
                switchMaterial.setChecked(false);
                headerTitle.setText("Enable " + selectedType + " Table and Seating");
                shadowView.setVisibility(View.VISIBLE);
            }
        }


        deleteImg.setOnClickListener(v -> {
            binding.seatingParentLayout.removeView(view);
            deleteSeatingView(v);
        });

        ImageView add1 = view.findViewById(R.id.add1);
        ImageView add2 = view.findViewById(R.id.add2);
        ImageView remove1 = view.findViewById(R.id.remove1);
        ImageView remove2 = view.findViewById(R.id.remove2);

        TextView totalTable = view.findViewById(R.id.no_of_table_tv);
        TextView personPerTable = view.findViewById(R.id.person_per_table_tv);
        EditText category = view.findViewById(R.id.event_name_et);
        EditText pricePerTable = view.findViewById(R.id.price_per_table_et);

        EditText desc = view.findViewById(R.id.short_desc_et);

        TextView priceCancellationTable = view.findViewById(R.id.priceCancellationTable);

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


        view.findViewById(R.id.increasePriceTable).setOnClickListener(v -> {
            String s = priceCancellationTable.getText().toString();
            priceCancellationTable.setText((Integer.valueOf(s) + 1) + "");
        });

        view.findViewById(R.id.decreasePriceTable).setOnClickListener(v -> {

            String s = priceCancellationTable.getText().toString();
            if (!s.equalsIgnoreCase("10")) {
                priceCancellationTable.setText((Integer.valueOf(s) - 1) + "");
            }
        });

        TableAndSeatingDao dao = null;

        if (selectedType.equalsIgnoreCase("VIP") && vipTableAndSeatingDAOList.size() > seatCounter)
            dao = vipTableAndSeatingDAOList.get(seatCounter);

        else if (selectedType.equalsIgnoreCase("Paid") && rsvpTableAndSeatingDAOList.size() > seatCounter)
            dao = rsvpTableAndSeatingDAOList.get(seatCounter);

        if (dao != null) {
            Log.e("setting", dao.toString());
            deleteImg.setTag(dao.getId());
            totalTable.setText(dao.getNoOfTables() + "");
            personPerTable.setText(dao.getPersonPerTable() + "");
            category.setText(dao.getCategoryName());
            pricePerTable.setText(dao.getPricePerTable() + "");
            desc.setText(dao.getDesc());

            priceCancellationTable.setText(String.valueOf(dao.getCancellationChargesTable()));

            if (selectedType.equalsIgnoreCase("VIP") && vipTableAndSeatingDAOList.size() > seatCounter + 1) {
                hidableView.setVisibility(View.GONE);
            } else if (selectedType.equalsIgnoreCase("Paid") && rsvpTableAndSeatingDAOList.size() > seatCounter + 1) {
                hidableView.setVisibility(View.GONE);
            }
        }

        binding.seatingParentLayout.addView(view);
    }

    private void DisableEnableAllTicketViews(boolean b) {
        for (int i = 0; i < binding.ticketParent.getChildCount(); i++) {
            binding.ticketParent.getChildAt(i).findViewById(R.id.shadowView).setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }

    private void DisableEnableAllSeatViews(boolean b) {
        for (int i = 0; i < binding.seatingParentLayout.getChildCount(); i++) {
            binding.seatingParentLayout.getChildAt(i).findViewById(R.id.shadowView).setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }

    private void enableDisableTicket(boolean b) {
        if (selectedType.equalsIgnoreCase("Free"))
            createEventDAO.setFreeTicketEnabled(b ? 1 : 0);
        else if (selectedType.equalsIgnoreCase("Paid"))
            createEventDAO.setRSVPTicketEnabled(b ? 1 : 0);
        else
            createEventDAO.setVIPTicketEnabled(b ? 1 : 0);
    }

    private void enableDisableSeat(boolean b) {
        if (selectedType.equalsIgnoreCase("Paid"))
            createEventDAO.setRSVPSeatEnabled(b ? 1 : 0);
        else
            createEventDAO.setVipSeatEnabled(b ? 1 : 0);

    }

    private boolean validateTicket(View view, boolean showErrorMsg) {
        if (((EditText) view.findViewById(R.id.event_name_et)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
                Dialogs.toast(getContext(), view, "Enter Ticket Name Please!");
                view.findViewById(R.id.event_name_et).requestFocus();
            }
            return false;
        }

        if (!selectedType.equalsIgnoreCase("Free")) {
            if (((EditText) view.findViewById(R.id.price_ticket_et)).getText().toString().trim().length() == 0) {
                if (showErrorMsg) {
                    Dialogs.toast(getContext(), view, "Fill Ticket Price or Choose Free Ticket Type!");
                    view.findViewById(R.id.price_ticket_et).requestFocus();
                }
                return false;
            }
        }

        if (((EditText) view.findViewById(R.id.ticket_qty_et)).getText().toString().trim().length() == 0) {
            if (showErrorMsg) {
                Dialogs.toast(getContext(), view, "Ticket Quantity is Mandatory!");
                ((EditText) view.findViewById(R.id.ticket_qty_et)).requestFocus();
            }
            return false;
        }
        return true;
    }

    private void deleteView(View view) {
        if (selectedType.equalsIgnoreCase("Free"))
            freeTicketDaoList.remove(new FreeTicketDao(view.getTag().toString()));
        else if (selectedType.equalsIgnoreCase("Paid"))
            rsvpTicketDaoList.remove(new RSVPTicketDao(view.getTag().toString()));
        else
            vipTicketDaoList.remove(new RSVPTicketDao(view.getTag().toString()));
    }

    private void deleteSeatingView(View view) {
        if (selectedType.equalsIgnoreCase("Paid"))
            rsvpTableAndSeatingDAOList.remove(new TableAndSeatingDao(view.getTag().toString()));
        else
            vipTableAndSeatingDAOList.remove(new TableAndSeatingDao(view.getTag().toString()));
    }
}












