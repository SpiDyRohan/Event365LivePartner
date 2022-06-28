package com.ebabu.event365live.host.fragments.ticket;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.getIntent;
import static android.content.Intent.getIntentOld;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentRegularTicketBinding;
import com.ebabu.event365live.host.databinding.LayoutTicketRegularBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.RegularTicketDao;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegularTicketFragment extends Fragment
{
    int cancellationCharge = 10;
    private FragmentRegularTicketBinding binding;
    private LayoutInflater layoutInflater;
    private CreateEventDAO createEventDAO;
    private String ticketType = "Paid";
    private String ticketName;
    private String value_monst,value_monet,value_tuesst,value_tueset,value_wednesst,value_wedneset,value_thursst,value_thurset,value_frist,value_friet,value_saturst,value_saturet,value_sunst,value_sunet,value_Monthst,value_Monthet;
    private String pricePerTicket;
    private String ticketQuantity;
    private String description;
    private String sellingStartDate;
    private String StartDate,EndDate,getTodayDate;
    private String sellingEndDate;
    private String sellingStartTime;
    private String sellingEndTime;
    private int eventDay;
    private String text,evnttype;
    private int ticketsellingdays;
    private Context context;
    private int editPos = -1;
    private double discountPercentage = 0.0;
    private List<RegularTicketDao> regularFreeTicketList;
    private boolean mAutoIncrement = false, mAutoDecrement = false;
    private Spinner spinner2;
    int t1Hour,t1Minute;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

           // First spinner
            ArrayList<String> dayList = new ArrayList<>();
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_regular_ticket, container, false);
            SharedPreferences shrd = context.getSharedPreferences("demo", MODE_PRIVATE);
            String value = shrd.getString("str", "Save a note and it will show up here");
            String startDate = shrd.getString("monthlydate", "");
            if (value != null && !value.equals(""))
            {
                String[] daysSplit = value.split(",");
                if (daysSplit.length > 0)
                {
                    for (int i = 0; i < daysSplit.length; i++)
                    {
                        dayList.add(daysSplit[i]);
                    }
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, dayList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (value != null)
            {
                int spinnerPosition = adapter.getPosition(value);
                binding.dayspinner.setSelection(spinnerPosition);
            }
            binding.dayspinner.setAdapter(adapter);
            binding.dayspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    text = adapterView.getItemAtPosition(i).toString();
                    SharedPreferences day = context.getSharedPreferences("demo", MODE_PRIVATE);
                    String eventtype1=day.getString("evnttype",null);
                    SharedPreferences.Editor editor = day.edit();
                    editor.putString("day", text);
                    editor.apply();
                    binding.daylinearlayout.setVisibility(View.VISIBLE);
                    binding.daylinearlayout2.setVisibility(View.VISIBLE);
                    if (eventtype1.contains("Daily")||eventtype1.contains("Weekly"))
                    {
                        clearFields();
                        StartDate = "2025-10-20";
                        EndDate = "2025-10-20";
                        if (text.contains("Monday"))
                        {
                            ticketsellingdays = 1;
                            binding.monday.setVisibility(View.VISIBLE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.monthlayout.setVisibility(View.GONE);
                            SharedPreferences monday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_monst = monday.getString("mondayst", "Save a note and it will show up here");
                            value_monet = monday.getString("mondayet", "Save a note and it will show up here");
                            binding.strttimemon1.setText(value_monst);
                            binding.endtimemon1.setText(value_monet);
                            sellingStartTime=value_monst;
                            sellingEndTime=value_monet;
                        }
                        if (text.contains("Tuesday")) {
                            ticketsellingdays = 2;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.VISIBLE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.VISIBLE);
                            binding.daylinearlayout2.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            SharedPreferences tuesday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_tuesst = tuesday.getString("tuesdayst", "Save a note and it will show up here");
                            value_tueset = tuesday.getString("tuesdayet", "Save a note and it will show up here");
                            binding.strttimetues1.setText(value_tuesst);
                            binding.endtimetues1.setText(value_tueset);
                            sellingStartTime=value_tuesst;
                            sellingEndTime=value_tueset;
                        }
                        if (text.contains("Wednesday")) {
                            ticketsellingdays = 3;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.VISIBLE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.VISIBLE);
                            binding.daylinearlayout2.setVisibility(View.VISIBLE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.linearLayout.setVisibility(View.GONE);
                            SharedPreferences wednesday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_wednesst = wednesday.getString("wednesdayst", "Save a note and it will show up here");
                            value_wedneset = wednesday.getString("wednesdayet", "Save a note and it will show up here");
                            binding.strttimewednes1.setText(value_wednesst);
                            binding.endtimewednes1.setText(value_wedneset);
                            sellingStartTime=value_wednesst;
                            sellingEndTime=value_wedneset;
                        }
                        if (text.contains("Thursday")) {
                            ticketsellingdays = 4;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.VISIBLE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.VISIBLE);
                            binding.daylinearlayout2.setVisibility(View.VISIBLE);
                            SharedPreferences thursday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_thursst = thursday.getString("thursdayst", "Save a note and it will show up here");
                            value_thurset = thursday.getString("thursdayet", "Save a note and it will show up here");
                            binding.strttimethurs1.setText(value_thursst);
                            binding.endtimethurs1.setText(value_thurset);
                            sellingStartTime=value_thursst;
                            sellingEndTime=value_thurset;
                        }
                        if (text.contains("Friday")) {
                            ticketsellingdays = 5;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.VISIBLE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.VISIBLE);
                            binding.daylinearlayout2.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            SharedPreferences friday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_frist = friday.getString("fridayst", "Save a note and it will show up here");
                            value_friet = friday.getString("fridayet", "Save a note and it will show up here");
                            binding.strttimefri1.setText(value_frist);
                            binding.endtimefri1.setText(value_friet);
                            sellingStartTime=value_frist;
                            sellingEndTime=value_friet;
                        }
                        if (text.contains("Saturday")) {
                            ticketsellingdays = 6;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.VISIBLE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.VISIBLE);
                            binding.daylinearlayout2.setVisibility(View.VISIBLE);
                            SharedPreferences saturday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_saturst = saturday.getString("saturdayst", "Save a note and it will show up here");
                            value_saturet = saturday.getString("saturdayet", "Save a note and it will show up here");
                            binding.strttimesatur1.setText(value_saturst);
                            binding.endtimesatur1.setText(value_saturet);
                            sellingStartTime=value_saturst;
                            sellingEndTime=value_saturet;
                        }
                        if (text.contains("Sunday")) {
                            ticketsellingdays = 7;
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.daylinearlayout.setVisibility(View.GONE);
                            binding.daylinearlayout2.setVisibility(View.GONE);
                            SharedPreferences sunday = context.getSharedPreferences("demo", MODE_PRIVATE);
                            value_sunst = sunday.getString("sundayst", "Save a note and it will show up here");
                            value_sunet = sunday.getString("sundayet", "Save a note and it will show up here");
                            binding.strttimesun1.setText(value_sunst);
                            binding.endtimesun1.setText(value_sunet);
                            sellingStartTime=value_sunst;
                            sellingEndTime=value_sunet;
                        }
                        createEventDAO.setStartDate(StartDate);
                        createEventDAO.setEndDate(EndDate);
                    }
                    if (eventtype1.contains("Monthly"))
                    {
                        binding.monthlayout.setVisibility(View.VISIBLE);
                        binding.dayspinner.setVisibility(View.GONE);
                        binding.dayspinner2.setVisibility(View.GONE);
                        SharedPreferences Month = context.getSharedPreferences("demo", MODE_PRIVATE);
                        value_Monthst = Month.getString("monthst", null);
                        value_Monthet = Month.getString("monthet", null);
                        binding.strttimemonth1.setText(value_Monthst);
                        binding.endtimemonth1.setText(value_Monthet);
                        sellingStartTime=value_Monthst;
                        sellingEndTime=value_Monthet;
                        createEventDAO.setStartDate(startDate);
                        createEventDAO.setEndDate(startDate);
//                        createEventDAO.setStartDate(StartDate);
//                        createEventDAO.setEndDate(StartDate);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            spinner2 = binding.dayspinner2.findViewById(R.id.dayspinner2);
            List<String> list = new ArrayList<String>();
            list.add("Instantly");
            list.add("1 Day Before");
            list.add("2 Day Before");
            list.add("3 Day Before");
            list.add("4 Day Before");
            list.add("5 Day Before");
            list.add("6 Day Before");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner2.setAdapter(arrayAdapter);
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    spinner2.setSelection(i);
                    String text1 = adapterView.getItemAtPosition(i).toString();
                    if (text1.contains("Instantly"))
                    {eventDay = 0;}
                    if (text1.contains("1 Day Before"))
                    {eventDay = 1;}
                    if (text1.contains("2 Day Before"))
                    {eventDay = 2;}
                    if (text1.contains("3 Day Before"))
                    {eventDay = 3;}
                    if (text1.contains("4 Day Before"))
                    {eventDay = 4;}
                    if (text1.contains("5 Day Before"))
                    {eventDay = 5;}
                    if (text1.contains("6 Day Before"))
                    {eventDay = 6;}
                    if (text1.contains("7 Day Before"))
                    {eventDay = 7;}
                    else
                    {}
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView)
                {

                }
            });
            spinner2.setAdapter(arrayAdapter);
        return binding.getRoot();
    }
    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("demo", MODE_PRIVATE);
        String eventtype1=sharedPreferences.getString("evnttype",null);
        if (eventtype1.contains("One Time"))
        {
            binding.linearLayout.setVisibility(View.VISIBLE);
            binding.daylinearlayout.setVisibility(View.GONE);
            binding.daylinearlayout2.setVisibility(View.GONE);
        }
        if (eventtype1.contains("Daily"))
        {
            binding.daylinearlayout.setVisibility(View.VISIBLE);
            binding.daylinearlayout2.setVisibility(View.VISIBLE);
        }
        if (eventtype1.contains("Weekly"))
        {
            binding.daylinearlayout.setVisibility(View.VISIBLE);
            binding.daylinearlayout2.setVisibility(View.VISIBLE);
        }
        if (eventtype1.contains("Monthly"))
        {
            binding.daylinearlayout.setVisibility(View.VISIBLE);
            binding.daylinearlayout2.setVisibility(View.VISIBLE);
            binding.monthlayout.setVisibility(View.VISIBLE);

        }
        layoutInflater = getLayoutInflater();
        createEventDAO = CreateTicketFragment.createEventDAO;

        try {
            if (createEventDAO != null && createEventDAO.getFreeRegularTicketDaoList() != null) {
                regularFreeTicketList = createEventDAO.getFreeRegularTicketDaoList();
                getTicketView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (regularFreeTicketList != null) {
            getTicketView();
        }
    }

    private void initView() {

        setSellingTimeData();
        minimumCancellationCharge();

        if (createEventDAO == null) {
            createEventDAO = new CreateEventDAO();
        }

        if (regularFreeTicketList == null) regularFreeTicketList = new ArrayList<>();


        binding.rbPaidTicket.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ticketType = Constants.TICKET_TYPE_PAID;
                binding.llMinimum.setVisibility(View.VISIBLE);
                binding.llPricePerTicket.setVisibility(View.VISIBLE);
                binding.llAddDiscount.setVisibility(View.VISIBLE);
                // binding.llEnterDiscount.setVisibility(View.VISIBLE);
            }
        });

        binding.rbFreeTicket.setOnClickListener(view -> {
            if(CreateTicketFragment.regularTicketList != null && CreateTicketFragment.regularTicketList.size()==1){
                Dialogs.toast(getContext(), binding.rbFreeTicket, "Partner can add maximum one free ticket at a time for same event");
                binding.rbFreeTicket.setChecked(false);
                binding.rbPaidTicket.setChecked(true);
            }else{
                binding.rbFreeTicket.setChecked(true);
                binding.rbPaidTicket.setChecked(false);
                ticketType = Constants.TICKET_TYPE_FREE;
                binding.llMinimum.setVisibility(View.GONE);
                binding.llPricePerTicket.setVisibility(View.GONE);
                binding.llAddDiscount.setVisibility(View.GONE);
                binding.llEnterDiscount.setVisibility(View.GONE);
            }
        });



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
            if (isValid(v))
            {
                RegularTicketDao ticketDao = new RegularTicketDao();
                ticketDao.setTicketType(ticketType);
                ticketDao.setTicketName(ticketName);
                ticketDao.setPricePerTicket(pricePerTicket);
                ticketDao.setTicketQuantity(ticketQuantity);
                ticketDao.setDescription(description);
                ticketDao.setSellingStartDate(sellingStartDate);
                ticketDao.setSellingStartTime(sellingStartTime);
                ticketDao.setSellingEndDate(sellingEndDate);
                ticketDao.setSellingEndTime(sellingEndTime);
                ticketDao.setCancellationCharge(cancellationCharge + "");
                ticketDao.setEventDay(eventDay+"");
                ticketDao.setTicketsellingdays(ticketsellingdays+"");
                if (evnttype.contains("Monthly"))
                {
                    ticketDao.setSellingStartDate(sellingStartDate);
                    ticketDao.setSellingEndDate(sellingEndDate);
                }
                if (evnttype.contains("Weekly"))
                {
                    Date today = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                    getTodayDate = df.format(today);
                    sellingStartDate=getTodayDate;
                    sellingEndDate=getTodayDate;
                    createEventDAO.setSellingStartDate(sellingStartDate);
                    createEventDAO.setSellingEndDate(sellingEndDate);
                    ticketDao.setSellingStartDate(sellingStartDate);
                    ticketDao.setSellingEndDate(sellingEndDate);
                }
                if (evnttype.contains("oneTime"))
                {
                    sellingStartTime=createEventDAO.getStartTime();
                    sellingStartDate=createEventDAO.getStartDate();
                    sellingEndDate=createEventDAO.getEndDate();
                    sellingEndTime=createEventDAO.getEndTime();
                    createEventDAO.setSellingStartDate(StartDate);
                    createEventDAO.setSellingEndDate(EndDate);
                    ticketDao.setSellingStartTime(sellingStartTime);
                    ticketDao.setSellingStartDate(sellingStartDate);
                    ticketDao.setSellingEndTime(sellingEndTime);
                    ticketDao.setSellingEndDate(sellingEndDate);
                }
                if (discountPercentage != 0.0)
                ticketDao.setDiscountPercentage(discountPercentage);
                long id = System.currentTimeMillis();
                ticketDao.setId(id);
                if (editPos != -1)
                {
                    regularFreeTicketList.set(editPos, ticketDao);
                    editPos = -1;
                }
                else
                {
                    regularFreeTicketList.add(ticketDao);
                }
//                createEventDAO.setSellingStartDate(StartDate);
//                createEventDAO.setSellingEndDate(StartDate);
//                createEventDAO.setStartTime(sellingStartTime);
//                createEventDAO.setEndTime(sellingEndTime);
                createEventDAO.setEventDay(String.valueOf(eventDay));
                createEventDAO.setTicketsellingdays(String.valueOf(ticketsellingdays));
                CreateTicketFragment.regularTicketList = regularFreeTicketList;
                getTicketView();
                clearFields();
            }
        });

        binding.nextBtn.setOnClickListener(v -> {

            boolean ticketAdd = false;
            if (regularFreeTicketList != null && !regularFreeTicketList.isEmpty())
                ticketAdd = true;
            if (CreateTicketFragment.rsvpVipTicketList != null && !CreateTicketFragment.rsvpVipTicketList.isEmpty())
                ticketAdd = true;
            if (CreateTicketFragment.tableSeatingTicketList != null && !CreateTicketFragment.tableSeatingTicketList.isEmpty())
                ticketAdd = true;

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
        binding.strttimemon1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimemon1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimemon1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimemon1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimetues1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimetues1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimetues1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimetues1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimewednes1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimewednes1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimewednes1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimewednes1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimethurs1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimethurs1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimethurs1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimethurs1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimefri1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimefri1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimefri1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimefri1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimesatur1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimesatur1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimesatur1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.endtimesatur1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimetues1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        binding.strttimetues1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.endtimetues1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        // Clock
                        t1Hour=i;
                        t1Minute=i1;
                        // Calender
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        String zone = t1Hour >= 12 ? "pm" : "am";
                        int hour = t1Hour > 12 ? t1Hour - 12 : t1Hour;
                        String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((t1Minute < 10) ? "0" + t1Minute : t1Minute) + " " + zone;
                        binding.endtimetues1.setText(DateFormat.format("hh:mm aa", calendar));
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
        binding.strttimemonth1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              TimePickerDialog timePickerDialog= new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
              {
                  @Override
                  public void onTimeSet(TimePicker timePicker, int i, int i1)
                  {
                    t1Hour=i;
                    t1Minute=i1;
                    Calendar calendar=Calendar.getInstance();
                    calendar.set(0,0,0,t1Hour,t1Minute);
                      String zone = t1Hour >= 12 ? "pm" : "am";
                      int hour = t1Hour > 12 ? t1Hour - 12 : t1Hour;
                      String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((t1Minute < 10) ? "0" + t1Minute : t1Minute) + " " + zone;
                      binding.strttimemonth1.setText(time);
                  }
              },12,0,false
              );
              timePickerDialog.updateTime(t1Hour,t1Minute);
              timePickerDialog.show();
            }
        });
        binding.endtimemonth1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog= new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        t1Hour=i;
                        t1Minute=i1;
                        Calendar calendar=Calendar.getInstance();
                        calendar.set(0,0,0,t1Hour,t1Minute);
                        String zone = t1Hour >= 12 ? "pm" : "am";
                        int hour = t1Hour > 12 ? t1Hour - 12 : t1Hour;
                        String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((t1Minute < 10) ? "0" + t1Minute : t1Minute) + " " + zone;
                        binding.endtimemonth1.setText(time);
                    }
                },12,0,false
                );
                timePickerDialog.updateTime(t1Hour,t1Minute);
                timePickerDialog.show();
            }
        });
    }

    class RptUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                increment();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            } else if (mAutoDecrement) {
                decrement();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            }
        }
    }

    private Handler repeatUpdateHandler = new Handler();

    private void minimumCancellationCharge() {

        binding.ivPlusCharge.setOnClickListener(view -> {
            increment();
        });

        binding.ivPlusCharge.setOnLongClickListener(view -> {
            mAutoDecrement = false;
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        binding.ivPlusCharge.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });


        binding.ivMinusCharge.setOnClickListener(view -> {
            decrement();
        });

        binding.ivMinusCharge.setOnLongClickListener(view -> {
            mAutoIncrement = false;
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RptUpdater());
            return false;
        });

        binding.ivMinusCharge.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrement) {
                    mAutoDecrement = false;
                }
                return false;
            }
        });
    }

    private void increment() {
        if (cancellationCharge < 100) {
            cancellationCharge++;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private void decrement() {
        if (cancellationCharge > 10) {
            cancellationCharge--;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private boolean isValid(View v) {
        SharedPreferences eventtype=context.getSharedPreferences("demo", MODE_PRIVATE);
        evnttype=eventtype.getString("evnttype",null);
        Date c = Calendar.getInstance().getTime();
        ticketName = binding.etTicketName.getText().toString().trim();
        pricePerTicket = binding.etPriceTicket.getText().toString().trim();
        ticketQuantity = binding.etTicketQty.getText().toString().trim();
        description = binding.etShortDesc.getText().toString().trim();
           if (evnttype.contains("oneTime"))
           {
               sellingStartDate = binding.tvStartDate.getText().toString();
               sellingEndDate = binding.tvEndDate.getText().toString();
           }
           if (evnttype.contains("daily")||evnttype.contains("weekly"))
            {
             sellingStartDate = String.valueOf(c);
             sellingEndDate = String.valueOf(c);
             if (sellingStartTime==null||sellingEndTime==null)
             {
                if (text.contains("Monday"))
                {
                    sellingStartTime=binding.strttimemon1.getText().toString();
                    sellingEndTime=binding.endtimemon1.getText().toString();
                }
                if (text.contains("Tuesday"))
                {
                    sellingStartTime=binding.strttimetues1.getText().toString();
                    sellingEndTime=binding.endtimetues1.getText().toString();
                }
                if (text.contains("Wednesday"))
                {
                    sellingStartTime=binding.strttimewednes1.getText().toString();
                    sellingEndTime=binding.endtimewednes1.getText().toString();
                }
                if (text.contains("Thursday"))
                {
                    sellingStartTime=binding.strttimethurs1.getText().toString();
                    sellingEndTime=binding.endtimethurs1.getText().toString();
                }
                if (text.contains("Friday"))
                {
                    sellingStartTime=binding.strttimefri1.getText().toString();
                    sellingEndTime=binding.endtimefri1.getText().toString();
                }
                if (text.contains("Saturday"))
                {
                    sellingStartTime=binding.strttimesatur1.getText().toString();
                    sellingEndTime=binding.endtimesatur1.getText().toString();
                }
                if (text.contains("Sunday"))
                {
                    sellingStartTime=binding.strttimesun1.getText().toString();
                    sellingEndTime=binding.endtimesun1.getText().toString();
                }
             }
            }
           if (evnttype.contains("monthly"))
            {
            sellingStartTime = binding.strttimemonth1.getText().toString();
            sellingEndTime= binding.endtimemonth1.getText().toString();
            sellingStartDate = StartDate;
            sellingEndDate = StartDate;
            }
            if (binding.etDiscount.getText().toString().length() != 0) {
                discountPercentage = Double.parseDouble(binding.etDiscount.getText().toString());
                if (discountPercentage > 100) {
                    Dialogs.toast(getContext(), v, "Discount should not be more than 100");
                    return false;
                }
            }

            if (ticketType == null || ticketType.equals("")) {
                Dialogs.toast(getContext(), v, "Choose Ticket Type please!");
                return false;
            } else if (ticketName == null || ticketName.equals("")) {
                Dialogs.toast(getContext(), v, "Enter Ticket Name please!");
                binding.etTicketName.requestFocus();
                return false;
            }

            if (ticketType.equals(Constants.TICKET_TYPE_PAID) && (pricePerTicket == null || pricePerTicket.equals(""))) {
                Dialogs.toast(getContext(), v, "Enter Price Per Ticket please!");
                binding.etPriceTicket.requestFocus();
                return false;
            }

            if (ticketQuantity == null || ticketQuantity.equals("")) {
                Dialogs.toast(getContext(), v, "Enter Ticket Quantity please!");
                binding.etTicketQty.requestFocus();
                return false;
            } /*else if (description == null || description.equals("")) {
            Dialogs.toast(getContext(), v, "Enter Short Description please!");
            binding.etShortDesc.requestFocus();
            return false;
        }*/

            if (ticketType.equals(Constants.TICKET_TYPE_PAID)) {
                if (cancellationCharge < 10) {
                    Dialogs.toast(getContext(), v, "Minimum of 10% is Mandatory");
                    return false;
                }
            }
            if (evnttype.contains("OneTime")) {
                if (sellingStartDate.equals(getString(R.string.start_date))) {
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
            }
            if (binding.etDiscount.getText().toString().length() != 0)
            {
                discountPercentage = Double.parseDouble(binding.etDiscount.getText().toString());
                if (discountPercentage > 100) {
                    Dialogs.toast(getContext(), v, "Discount should not be more than 100");
                    return false;
                }
            }

            if (ticketType == null || ticketType.equals("")) {
                Dialogs.toast(getContext(), v, "Choose Ticket Type please!");
                return false;
            } else if (ticketName == null || ticketName.equals("")) {
                Dialogs.toast(getContext(), v, "Enter Ticket Name please!");
                binding.etTicketName.requestFocus();
                return false;
            }

            if (ticketType.equals(Constants.TICKET_TYPE_PAID) && (pricePerTicket == null || pricePerTicket.equals("")))
            {
                Dialogs.toast(getContext(), v, "Enter Price Per Ticket please!");
                binding.etPriceTicket.requestFocus();
                return false;
            }

            if (ticketQuantity == null || ticketQuantity.equals("")) {
                Dialogs.toast(getContext(), v, "Enter Ticket Quantity please!");
                binding.etTicketQty.requestFocus();
                return false;
            } /*else if (description == null || description.equals("")) {
            Dialogs.toast(getContext(), v, "Enter Short Description please!");
            binding.etShortDesc.requestFocus();
            return false;
        }*/

            if (ticketType.equals(Constants.TICKET_TYPE_PAID))
            {
                if (cancellationCharge < 10)
                {
                    Dialogs.toast(getContext(), v, "Minimum of 10% is Mandatory");
                    return false;
                }
            }
            if (evnttype.contains("Daily"))
            {
                if (text.contains("Monday"))
                {
                    Toast.makeText(context, "Selling start time: "+sellingStartTime, Toast.LENGTH_SHORT).show();
                    if (sellingStartTime.equals(getString(R.string.start_time)))
                    {
                     Dialogs.toast(getContext(), v, "Select Start Time please!");
                     return false;
                    }
                    else if (sellingEndTime.equals(getString(R.string.end_time)))
                    {
                      Dialogs.toast(getContext(), v, "Select End Time please!");
                      return false;
                    }
                }
                if (text.contains("Tuesday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time))) {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
                if (text.contains("Wednesday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time))) {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
                if (text.contains("Thursday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time)))
                    {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
                if (text.contains("Friday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time))) {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
                if (text.contains("Saturday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time))) {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
                if (text.contains("Sunday"))
                {
                    if (sellingStartTime.equals(getString(R.string.start_time))) {
                        Dialogs.toast(getContext(), v, "Select Start Time please!");
                        return false;
                    } else if (sellingEndTime.equals(getString(R.string.end_time))) {
                        Dialogs.toast(getContext(), v, "Select End Time please!");
                        return false;}
                }
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
        return time.matches("(.*)pm") && startHour < 12 ? (startHour + 12) : startHour;
    }

    private void setEndTime(View v) {
        if (binding.tvStartTime.getText().toString().equals(getString(R.string.start_time)))
            Toast.makeText(getContext(), "Select start time first!", Toast.LENGTH_LONG).show();
        else

            Dialogs.setTime(getContext(), binding.tvEndTime, time ->
            {
                if (!binding.tvStartDate.getText().equals(binding.tvEndDate.getText().toString()))
                {
                    if (binding.tvEndDate.getText().equals(createEventDAO.getEndDate()))
                    {
                        if (compareTime(time, createEventDAO.getEndTime()))
                        {
                            binding.tvEndTime.setText(time);
                        }
                        else
                        {
                            Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event end time");
                            binding.tvEndTime.setText(getString(R.string.end_time));
                        }
                    }
                    else
                        binding.tvEndTime.setText(time);
                }
                else
                {
                    String startTime = binding.tvStartTime.getText().toString();
                    if (compareTime(startTime, time))
                        if (binding.tvEndDate.getText().equals(createEventDAO.getStartDate()))
                        {
                            if (compareTime(createEventDAO.getStartTime(),time))
                            {
                                binding.tvEndTime.setText(time);
                            }
                            else
                            {
                                Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event Start time");
                                binding.tvEndTime.setText(getString(R.string.end_time));
                            }
                        }
                    else
                            binding.tvEndTime.setText(time);
                    else
                    {
                        Dialogs.toast(getContext(), v, "Start time must be earlier than End time");
                        binding.tvEndTime.setText(getString(R.string.end_time));
                    }
                }
            });
    }

    private void setSellingTimeData()
    {
        if (createEventDAO != null && createEventDAO.getEndDate() != null)
        {
            binding.tvStartTime.setText(createEventDAO.getStartTime());
            binding.tvEndTime.setText(createEventDAO.getEndTime());
            binding.tvEndDate.setText(createEventDAO.getEndDate());
            binding.tvStartDate.setText(createEventDAO.getStartDate());
        }
    }

    private void getTicketView()
    {
        binding.llRegularTickets.removeAllViews();
        if (regularFreeTicketList != null && regularFreeTicketList.size() > 0)
        {
            for (int i = 0; i < regularFreeTicketList.size(); i++)
            {
                LayoutTicketRegularBinding regularBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_ticket_regular, binding.llRegularTickets, false);
                regularBinding.tvRegularTicketName.setText(regularFreeTicketList.get(i).getTicketName());
                regularBinding.tvTotalTickets.setText("Total Tickets - " + regularFreeTicketList.get(i).getTicketQuantity());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";
                if (regularFreeTicketList.get(i).getTicketType().equals(Constants.TICKET_TYPE_PAID))
                {
                    ticketTypeText = "Paid Tickets";
                    ticketPriceText = "$ " + regularFreeTicketList.get(i).getPricePerTicket() + " / Ticket";
                    minimumChargeText = "Cancellation Charge : " + regularFreeTicketList.get(i).getCancellationCharge() + "%";
                    if (regularFreeTicketList.get(i).getDiscountPercentage() != 0.0)
                    {
                        regularBinding.discountLabel.setVisibility(View.VISIBLE);
                        regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(regularFreeTicketList.get(i).getDiscountPercentage())) + "%)");
                    }
                    else
                    {
                        regularBinding.discountLabel.setVisibility(View.GONE);
                    }
                }
                else if (regularFreeTicketList.get(i).getTicketType().equals(Constants.TICKET_TYPE_FREE)) {
                    ticketTypeText = "Free Tickets";
                    ticketPriceText = "$ 00.00 / Ticket";
                    minimumChargeText = "Cancellation Charge : 0%";
                    regularBinding.discountLabel.setVisibility(View.GONE);
                }
                regularBinding.tvTicketType.setText(ticketTypeText);
                regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");
                int finalI = i;
                regularBinding.delBtn.setOnClickListener(view -> {
                    regularFreeTicketList.remove(finalI);
                    try {
                        CreateTicketFragment.regularTicketList = regularFreeTicketList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getTicketView();
                });

                regularBinding.ivEditRegular.setOnClickListener(view -> {
                    editPos = finalI;
                    setEditData(regularFreeTicketList.get(finalI));
                });
                binding.llRegularTickets.addView(regularBinding.getRoot());
            }
        }
    }
    private void clearFields()
    {
        binding.nestedScroll.scrollTo(5, 10);
        //   ticketType = "";
        ticketName = "";
        pricePerTicket = "";
        ticketQuantity = "";
        description = "";
        StartDate="";
        EndDate="";
        sellingStartDate = "";
        sellingEndDate = "";
        sellingStartTime = "";
        sellingEndTime = "";
        cancellationCharge = 10;
        if (createEventDAO != null && createEventDAO.getEndTime() != null)
        {
            binding.tvStartTime.setText(createEventDAO.getStartTime());
            binding.tvEndTime.setText(createEventDAO.getEndTime());
            binding.tvEndDate.setText(createEventDAO.getEndDate());
            binding.tvStartDate.setText(createEventDAO.getStartDate());
        }
        else
        {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }
        binding.etTicketName.setText("");
        binding.etPriceTicket.setText("");
        binding.etTicketQty.setText("");
        binding.etShortDesc.setText("");
        binding.tvMinimumCharges.setText("10");
        binding.rbFreeTicket.setChecked(false);
        binding.rbPaidTicket.setChecked(true);
    }
    private void setEditData(RegularTicketDao editData)
    {
        if (editData.getTicketType().equals(Constants.TICKET_TYPE_PAID))
        {
            ticketType = Constants.TICKET_TYPE_PAID;
            binding.llMinimum.setVisibility(View.VISIBLE);
            binding.llPricePerTicket.setVisibility(View.VISIBLE);
            binding.rbPaidTicket.setChecked(true);
        }
        else if (editData.getTicketType().equals(Constants.TICKET_TYPE_FREE))
        {
            ticketType = Constants.TICKET_TYPE_FREE;
            binding.llMinimum.setVisibility(View.GONE);
            binding.llPricePerTicket.setVisibility(View.GONE);
            binding.rbFreeTicket.setChecked(true);
        }
        cancellationCharge = Integer.parseInt(editData.getCancellationCharge());
        if (editData != null && editData.getSellingEndTime() != null)
        {
            binding.tvStartTime.setText(editData.getSellingStartTime());
            binding.tvEndTime.setText(editData.getSellingEndTime());
            binding.tvEndDate.setText(editData.getSellingEndDate());
            binding.tvStartDate.setText(editData.getSellingStartDate());
        }
        else
        {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }
        binding.etTicketName.setText(editData.getTicketName());
        binding.etPriceTicket.setText(editData.getPricePerTicket());
        binding.etTicketQty.setText(editData.getTicketQuantity());
        binding.etShortDesc.setText(editData.getDescription());
        if (editData.getDiscountPercentage() == 0.0)
        {
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
        }
        else
        {
            binding.llAddDiscount.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.VISIBLE);
            binding.etDiscount.setText("" + editData.getDiscountPercentage());
        }
        binding.tvMinimumCharges.setText(cancellationCharge + "");
    }
}