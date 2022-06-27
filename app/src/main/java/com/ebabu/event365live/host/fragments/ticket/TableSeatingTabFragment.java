package com.ebabu.event365live.host.fragments.ticket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentTableSeatingTabBinding;
import com.ebabu.event365live.host.databinding.LayoutTicketRegularBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.TableSeatingTicketDao;
import com.ebabu.event365live.host.utils.Dialogs;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class TableSeatingTabFragment extends Fragment {
    private FragmentTableSeatingTabBinding binding;
    private LayoutInflater layoutInflater;
    private CreateEventDAO createEventDAO;
    private String ticketName, pricePerTicket, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime;
    private int cancellationCharge = 10, personPerTable = 1, noOfTables = 1;
    private Context context;
    private List<TableSeatingTicketDao> tableSeatingTicketList;
    private int editPos = -1;
    private double discountPercentage = 0.0;

    private Handler repeatUpdateHandler = new Handler();
    private Handler repeatUpdateHandlerTable = new Handler();
    private Handler repeatUpdateHandlerPerson = new Handler();
    private boolean mAutoIncrement = false, mAutoIncrementTable = false, mAutoIncrementPerson = false,mAutoDecrement = false, mAutoDecrementTable = false, mAutoDecrementPerson = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_table_seating_tab, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutInflater = getLayoutInflater();
        createEventDAO = CreateTicketFragment.createEventDAO;

        try {
            if(createEventDAO != null && createEventDAO.getTableSeatingTicketDaoList() != null){
                tableSeatingTicketList = createEventDAO.getTableSeatingTicketDaoList();
                getTicketView();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tableSeatingTicketList != null) {
            getTicketView();
        }
    }

    private void initView() {

        setSellingTimeData();
        minimumCancellationCharge();
        totalTableIncDec();
        personOnTableIncDec();

        if (createEventDAO == null) {
            createEventDAO = new CreateEventDAO();
        }

        if (tableSeatingTicketList == null) tableSeatingTicketList = new ArrayList<>();

        binding.llAddDiscount.setOnClickListener(v -> {
            binding.llAddDiscount.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.VISIBLE);
        });

        binding.ivDeleteDiscount.setOnClickListener(v -> {
            discountPercentage = 0.0;
            binding.etDiscount.setText("");
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
        });

        binding.addTicketLayout.setOnClickListener(v -> {
            if (isValid(v)) {

                TableSeatingTicketDao ticketDao = new TableSeatingTicketDao();

                ticketDao.setTicketName(ticketName);
                ticketDao.setPricePerTicket(pricePerTicket);
                ticketDao.setDescription(description);
                ticketDao.setNoOfTables(noOfTables + "");
                ticketDao.setPersonPerTable(personPerTable + "");
                ticketDao.setSellingStartDate(sellingStartDate);
                ticketDao.setSellingEndDate(sellingEndDate);
                ticketDao.setSellingStartTime(sellingStartTime);
                ticketDao.setSellingEndTime(sellingEndTime);
                ticketDao.setCancellationCharge(cancellationCharge + "");
                if(discountPercentage != 0.0)
                ticketDao.setDiscountPercentage(discountPercentage);
                long id = System.currentTimeMillis();
                ticketDao.setId(id);

                if(editPos != -1){
                    tableSeatingTicketList.set(editPos, ticketDao);
                    editPos = -1;
                }else {
                    tableSeatingTicketList.add(ticketDao);
                }

                CreateTicketFragment.tableSeatingTicketList = tableSeatingTicketList;
                getTicketView();
                clearFields();
            }
        });

        binding.nextBtn.setOnClickListener(v -> {

            boolean ticketAdd=false;
            if (CreateTicketFragment.regularTicketList != null && !CreateTicketFragment.regularTicketList.isEmpty())
                ticketAdd=true;
            if (CreateTicketFragment.rsvpVipTicketList != null && !CreateTicketFragment.rsvpVipTicketList.isEmpty())
                ticketAdd=true;
            if (CreateTicketFragment.tableSeatingTicketList != null && !CreateTicketFragment.tableSeatingTicketList.isEmpty())
                ticketAdd=true;

            if (!ticketAdd) {
                Dialogs.toast(getContext(), v, "Create at least 1 ticket please!");
                return;
            }

            createEventDAO.setFreeRegularTicketDaoList(CreateTicketFragment.regularTicketList);
            createEventDAO.setRsvpVipTicketDaoList(CreateTicketFragment.rsvpVipTicketList);
            createEventDAO.setTableSeatingTicketDaoList(CreateTicketFragment.tableSeatingTicketList);

//            FullEventDetailFragment detailFragment = new FullEventDetailFragment();
//            Bundle bundle = new Bundle();
//            bundle.putParcelable("eventDAO", createEventDAO);
//            detailFragment.setArguments(bundle);
//            showFragment(detailFragment, detailFragment.getClass().getName());

            CreateTicketFragment.fullEventDetail(v, createEventDAO);
        });


        binding.imageView22.setOnClickListener(v -> Dialogs.setMinMaxDate(getContext(), "", createEventDAO.getEndDate(), binding.tvStartDate, date -> {
            binding.tvStartDate.setText(date);

            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }));

        binding.tvStartDate.setOnClickListener(v -> {
            Dialogs.setMinMaxDate(getContext(), "", createEventDAO.getEndDate(), binding.tvStartDate, date -> {
                binding.tvStartDate.setText(date);
                binding.tvStartTime.setText(getString(R.string.start_time));
                binding.tvEndDate.setText(getString(R.string.end_date));
                binding.tvEndTime.setText(getString(R.string.end_time));
            });
        });

        binding.endDateImg.setOnClickListener(v -> {
            if (binding.tvStartDate.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.tvStartDate.getText().toString(), createEventDAO.getEndDate(), binding.tvStartDate, date -> {
                    binding.tvEndDate.setText(date);
                });
        });

        binding.tvEndDate.setOnClickListener(v -> {
            if (binding.tvStartDate.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.tvStartDate.getText().toString(), createEventDAO.getEndDate(), binding.tvStartDate, date -> {
                    binding.tvEndDate.setText(date);

                });
        });


        binding.startTimeImg.setOnClickListener(this::setStartTime);
        binding.tvStartTime.setOnClickListener(this::setStartTime);
        binding.endTimeImg.setOnClickListener(this::setEndTime);
        binding.tvEndTime.setOnClickListener(this::setEndTime);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void minimumCancellationCharge() {

        binding.ivPlusCharge.setOnClickListener(view -> {
            incrementCancellation();
        });

        binding.ivPlusCharge.setOnLongClickListener(view -> {
            mAutoDecrement = false;
            mAutoIncrement = true;
            repeatUpdateHandler.post( new RptUpdater() );
            return false;
        });

        binding.ivPlusCharge.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement ){
                    mAutoIncrement = false;
                }
                return false;
            }
        });

        binding.ivMinusCharge.setOnClickListener(view -> {
            decrementCancellation();
        });

        binding.ivMinusCharge.setOnLongClickListener(view -> {
            mAutoIncrement = false;
            mAutoDecrement = true;
            repeatUpdateHandler.post( new RptUpdater() );
            return false;
        });

        binding.ivMinusCharge.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement ){
                    mAutoDecrement = false;
                }
                return false;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void totalTableIncDec() {

        binding.ivAddTable.setOnClickListener(view -> {
            incrementNoTable();
        });

        binding.ivAddTable.setOnLongClickListener(view -> {
            mAutoDecrementTable = false;
            mAutoIncrementTable = true;
            repeatUpdateHandlerTable.post( new RptUpdaterTable() );
            return false;
        });

        binding.ivAddTable.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoIncrementTable ){
                    mAutoIncrementTable = false;
                }
                return false;
            }
        });

        binding.ivRemoveTable.setOnClickListener(view -> {
            decrementNoTable();
        });

        binding.ivRemoveTable.setOnLongClickListener(view -> {
            mAutoIncrementTable = false;
            mAutoDecrementTable = true;
            repeatUpdateHandlerTable.post( new RptUpdaterTable() );
            return false;
        });

        binding.ivRemoveTable.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoDecrementTable ){
                    mAutoDecrementTable = false;
                }
                return false;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void personOnTableIncDec() {

        binding.ivAddPerson.setOnClickListener(view -> {
            incrementPerson();
        });

        binding.ivAddPerson.setOnLongClickListener(view -> {
            mAutoDecrementPerson = false;
            mAutoIncrementPerson = true;
            repeatUpdateHandlerPerson.post( new RptUpdaterPerson() );
            return false;
        });

        binding.ivAddPerson.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoIncrementPerson ){
                    mAutoIncrementPerson = false;
                }
                return false;
            }
        });

        binding.ivRemovePerson.setOnClickListener(view -> {
            decrementPerson();
        });

        binding.ivRemovePerson.setOnLongClickListener(view -> {
            mAutoIncrementPerson = false;
            mAutoDecrementPerson = true;
            repeatUpdateHandlerPerson.post( new RptUpdaterPerson() );
            return false;
        });

        binding.ivRemovePerson.setOnTouchListener( new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                        && mAutoDecrementPerson ){
                    mAutoDecrementPerson = false;
                }
                return false;
            }
        });

    }

    private void incrementCancellation(){
        if (cancellationCharge < 100) {
            cancellationCharge++;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private void decrementCancellation(){
        if (cancellationCharge > 10) {
            cancellationCharge--;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private void incrementNoTable(){
        noOfTables++;
        binding.tvNoOfTable.setText(noOfTables + "");
    }

    private void decrementNoTable(){
        if (noOfTables > 1) {
            noOfTables--;
            binding.tvNoOfTable.setText(noOfTables + "");
        }
    }

    private void incrementPerson(){
        if (personPerTable < 100) {
            personPerTable++;
            binding.tvPersonPerTable.setText(personPerTable + "");
        }
    }

    private void decrementPerson(){
        if (personPerTable > 1) {
            personPerTable--;
            binding.tvPersonPerTable.setText(personPerTable + "");
        }
    }

    class RptUpdater implements Runnable {
        public void run() {
            if( mAutoIncrement ){
                incrementCancellation();
                repeatUpdateHandler.postDelayed( new RptUpdater(), 50 );
            } else if( mAutoDecrement ){
                decrementCancellation();
                repeatUpdateHandler.postDelayed( new RptUpdater(), 50 );
            }
        }
    }

    class RptUpdaterTable implements Runnable {
        public void run() {
            if( mAutoIncrementTable ){
                incrementNoTable();
                repeatUpdateHandlerTable.postDelayed( new RptUpdaterTable(), 50 );
            } else if( mAutoDecrementTable ){
                decrementNoTable();
                repeatUpdateHandlerTable.postDelayed( new RptUpdaterTable(), 50 );
            }
        }
    }

    class RptUpdaterPerson implements Runnable {
        public void run() {
            if( mAutoIncrementPerson ){
                incrementPerson();
                repeatUpdateHandlerPerson.postDelayed( new RptUpdaterPerson(), 50 );
            } else if( mAutoDecrementPerson ){
                decrementPerson();
                repeatUpdateHandlerPerson.postDelayed( new RptUpdaterPerson(), 50 );
            }
        }
    }

    private boolean isValid(View v) {
        ticketName = binding.etCategoryName.getText().toString().trim();
        pricePerTicket = binding.etPricePerTicket.getText().toString().trim();
        description = binding.etShortDesc.getText().toString().trim();

        sellingStartDate = binding.tvStartDate.getText().toString();
        sellingEndDate = binding.tvEndDate.getText().toString();
        sellingStartTime = binding.tvStartTime.getText().toString();
        sellingEndTime = binding.tvEndTime.getText().toString();
        if(binding.etDiscount.getText().toString().length() !=0) {
            discountPercentage = Double.parseDouble(binding.etDiscount.getText().toString());
            if(discountPercentage >100){
                Dialogs.toast(getContext(), v, "Discount should not be more than 100");
                return false;
            }
        }
        if (ticketName == null || ticketName.equalsIgnoreCase("")) {
            Dialogs.toast(getContext(), v, "Enter Category Name please!");
            binding.etCategoryName.requestFocus();
            return false;
        } else if (pricePerTicket == null || pricePerTicket.equalsIgnoreCase("")) {
            Dialogs.toast(getContext(), v, "Enter Price Per Ticket please!");
            binding.etPricePerTicket.requestFocus();
            return false;
        } else if (noOfTables <= 0) {
            Dialogs.toast(getContext(), v, "Select No of Tables please!");
            return false;
        } else if (personPerTable <= 0) {
            Dialogs.toast(getContext(), v, "Select Person Per Table please!");
            return false;
        } /*else if (description == null || description.equals("")) {
            Dialogs.toast(getContext(), v, "Enter Short Description please!");
            binding.etShortDesc.requestFocus();
            return false;
        }*/ else if (cancellationCharge < 10) {
            Dialogs.toast(getContext(), v, "Minimum of 10% is Mandatory");
            return false;
        } else if (sellingStartDate.equals(getString(R.string.start_date))) {
            Dialogs.toast(getContext(), v, "Select Start Date please!");
            return false;
        } else if (sellingEndDate.equals(getString(R.string.end_date))) {
            Dialogs.toast(getContext(), v, "Select End Date please!");
            return false;
        } else if (sellingStartTime.equals(getString(R.string.start_time))) {
            Dialogs.toast(getContext(), v, "Select Start Time please!");
            return false;
        } else if (sellingEndTime.equals(getString(R.string.end_time))) {
            Dialogs.toast(getContext(), v, "Select End Time please!");
            return false;
        }

        return true;
    }

    private void setStartTime(View v) {

        Dialogs.setTime(getContext(), binding.tvEndTime, time -> {
            if (!binding.tvStartDate.getText().equals(createEventDAO.getEndDate())) {
                binding.tvStartTime.setText(time);
            } else if (compareTime(time, createEventDAO.getEndTime())) {
                binding.tvStartTime.setText(time);
            } else {
                Dialogs.toast(getContext(), v, "Sell start time must be earlier than Event end time");
                binding.tvStartTime.setText(getString(R.string.start_time));
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
        if (binding.tvStartTime.getText().toString().equals(getString(R.string.start_time)))
            Toast.makeText(getContext(), "Select start time first!", Toast.LENGTH_LONG).show();
        else

            Dialogs.setTime(getContext(), binding.tvEndTime, time -> {
                if (!binding.tvStartDate.getText().equals(binding.tvEndDate.getText().toString())) {

                    if (binding.tvEndDate.getText().equals(createEventDAO.getEndDate())) {
                        if (compareTime(time, createEventDAO.getEndTime())) {
                            binding.tvEndTime.setText(time);
                        } else {
                            Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event end time");
                            binding.tvEndTime.setText(getString(R.string.end_time));
                        }
                    } else
                        binding.tvEndTime.setText(time);
                } else {
                    String startTime = binding.tvStartTime.getText().toString();

                    if (compareTime(startTime, time))
                        if (binding.tvEndDate.getText().equals(createEventDAO.getStartDate())) {
                            if (compareTime(time, createEventDAO.getStartTime())) {
                                binding.tvEndTime.setText(time);
                            } else {
                                Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event Start time");
                                binding.tvEndTime.setText(getString(R.string.end_time));
                            }
                        } else
                            binding.tvEndTime.setText(time);
                    else {
                        Dialogs.toast(getContext(), v, "Start time must be earlier than End time");
                        binding.tvEndTime.setText(getString(R.string.end_time));
                    }
                }
            });
    }

    private void setSellingTimeData() {
        if (createEventDAO != null && createEventDAO.getEndDate() != null) {
            binding.tvStartTime.setText(createEventDAO.getStartTime());
            binding.tvEndTime.setText(createEventDAO.getEndTime());
            binding.tvEndDate.setText(createEventDAO.getEndDate());
            binding.tvStartDate.setText(createEventDAO.getStartDate());
        }
    }

    private void getTicketView() {
        binding.llRegularTickets.removeAllViews();

        if (tableSeatingTicketList != null && tableSeatingTicketList.size() > 0) {
            for (int i = 0; i < tableSeatingTicketList.size(); i++) {
                LayoutTicketRegularBinding regularBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_ticket_regular, binding.llRegularTickets, false);

                regularBinding.tvTotalTickets.setVisibility(View.GONE);
                regularBinding.tvPersonPerTable.setVisibility(View.VISIBLE);
                regularBinding.tvTotalTable.setVisibility(View.VISIBLE);

                regularBinding.tvRegularTicketName.setText(tableSeatingTicketList.get(i).getTicketName());
                regularBinding.tvPersonPerTable.setText("Person Per Tables - " + tableSeatingTicketList.get(i).getPersonPerTable());
                regularBinding.tvTotalTable.setText("Total Tables - " + tableSeatingTicketList.get(i).getNoOfTables());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";

                ticketTypeText = "Table & Seatings";
                ticketPriceText = "$ " + tableSeatingTicketList.get(i).getPricePerTicket() + " / Ticket";
                minimumChargeText = "Cancellation Charge : " + tableSeatingTicketList.get(i).getCancellationCharge() + "%";

                regularBinding.tvTicketType.setText(ticketTypeText);
                regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");
                if(tableSeatingTicketList.get(i).getDiscountPercentage() !=0.0) {
                    regularBinding.discountLabel.setVisibility(View.VISIBLE);
                    regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(tableSeatingTicketList.get(i).getDiscountPercentage())) + "%)");
                } else {
                    regularBinding.discountLabel.setVisibility(View.GONE);
                }
                int finalI = i;
                regularBinding.delBtn.setOnClickListener(view -> {
                    tableSeatingTicketList.remove(finalI);
                    try{
                        CreateTicketFragment.tableSeatingTicketList = tableSeatingTicketList;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    getTicketView();
                });

                regularBinding.ivEditRegular.setOnClickListener(view -> {
                    editPos = finalI;
                    setEditData(tableSeatingTicketList.get(finalI));
                });

                binding.llRegularTickets.addView(regularBinding.getRoot());

            }
        }
    }

    private void clearFields() {

        binding.nestedScroll.scrollTo(5, 10);

        ticketName = "";
        pricePerTicket = "";
        description = "";
        sellingStartDate = "";
        sellingEndDate = "";
        sellingStartTime = "";
        sellingEndTime = "";
        cancellationCharge = 10;
        noOfTables = 1;
        personPerTable = 1;

        if (createEventDAO != null && createEventDAO.getEndTime() != null) {
            binding.tvStartTime.setText(createEventDAO.getStartTime());
            binding.tvEndTime.setText(createEventDAO.getEndTime());
            binding.tvEndDate.setText(createEventDAO.getEndDate());
            binding.tvStartDate.setText(createEventDAO.getStartDate());
        } else {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }

        binding.etCategoryName.setText("");
        binding.etPricePerTicket.setText("");
        binding.etShortDesc.setText("");

        binding.tvMinimumCharges.setText("10");
        binding.tvNoOfTable.setText("1");
        binding.tvPersonPerTable.setText("1");
    }

    private void setEditData(TableSeatingTicketDao editData) {

        cancellationCharge = Integer.parseInt(editData.getCancellationCharge());
        noOfTables = Integer.parseInt(editData.getNoOfTables());
        personPerTable = Integer.parseInt(editData.getPersonPerTable());

        if (editData != null && editData.getSellingEndTime() != null) {
            binding.tvStartTime.setText(editData.getSellingStartTime());
            binding.tvEndTime.setText(editData.getSellingEndTime());
            binding.tvEndDate.setText(editData.getSellingEndDate());
            binding.tvStartDate.setText(editData.getSellingStartDate());
        } else {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }

        binding.etCategoryName.setText(editData.getTicketName());
        binding.etPricePerTicket.setText(editData.getPricePerTicket());
        binding.etShortDesc.setText(editData.getDescription());
        if(editData.getDiscountPercentage()==0.0){
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
        } else {
            binding.llAddDiscount.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.VISIBLE);
            binding.etDiscount.setText(""+editData.getDiscountPercentage());
        }
        binding.tvMinimumCharges.setText(cancellationCharge + "");
        binding.tvNoOfTable.setText(noOfTables + "");
        binding.tvPersonPerTable.setText(personPerTable + "");

    }

}