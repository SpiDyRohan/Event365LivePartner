package com.ebabu.event365live.host.fragments.ticket;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentRsvpVipTabBinding;
import com.ebabu.event365live.host.databinding.LayoutTicketRegularBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.RsvpVipTicketDao;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class RsvpVipTabFragment extends Fragment {
    private FragmentRsvpVipTabBinding binding;
    private LayoutInflater layoutInflater;
    private CreateEventDAO createEventDAO;
    private String ticketType = "RSVP", ticketName, pricePerTicket, ticketQuantity, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime;
    private int cancellationCharge = 10;
    private double discountPercentage = 0.0;
    private Context context;
    private int editPos = -1;
    private Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false,mAutoDecrement = false;
    private List<RsvpVipTicketDao> rsvpVipTicketList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rsvp_vip_tab, container, false);
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
            if (createEventDAO != null && createEventDAO.getRsvpVipTicketDaoList() != null) {
                rsvpVipTicketList = createEventDAO.getRsvpVipTicketDaoList();
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
        if (rsvpVipTicketList != null) {
            getTicketView();
        }
    }

    private void initView() {

        setSellingTimeData();
        minimumCancellationCharge();

        if (createEventDAO == null) {
            createEventDAO = new CreateEventDAO();
        }

        if (rsvpVipTicketList == null) rsvpVipTicketList = new ArrayList<>();


        binding.rbVip.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ticketType = Constants.TICKET_TYPE_VIP;
                binding.llMinimum.setVisibility(View.VISIBLE);
                binding.llPricePerTicket.setVisibility(View.VISIBLE);
                binding.llTicketSelling.setVisibility(View.VISIBLE);
                binding.llAddDiscount.setVisibility(View.VISIBLE);
            }
        });

        binding.rbRsvp.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ticketType = Constants.TICKET_TYPE_RSVP;
                binding.llMinimum.setVisibility(View.GONE);
                binding.llPricePerTicket.setVisibility(View.GONE);
                binding.llTicketSelling.setVisibility(View.GONE);
                binding.llAddDiscount.setVisibility(View.GONE);
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
            if (isValid(v)) {

                RsvpVipTicketDao ticketDao = new RsvpVipTicketDao();

                ticketDao.setTicketType(ticketType);
                ticketDao.setTicketName(ticketName);
                ticketDao.setTicketQuantity(ticketQuantity);
                ticketDao.setDescription(description);
                if (ticketType.equals(Constants.TICKET_TYPE_VIP)) {
                    ticketDao.setPricePerTicket(pricePerTicket);
                    ticketDao.setSellingStartDate(sellingStartDate);
                    ticketDao.setSellingEndDate(sellingEndDate);
                    ticketDao.setSellingStartTime(sellingStartTime);
                    ticketDao.setSellingEndTime(sellingEndTime);
                    if (discountPercentage != 0.0)
                        ticketDao.setDiscountPercentage(discountPercentage);
                    ticketDao.setCancellationCharge(cancellationCharge);
                }
                long id = System.currentTimeMillis();
                ticketDao.setId(id);

                if (editPos != -1) {
                    rsvpVipTicketList.set(editPos, ticketDao);
                    editPos = -1;
                } else {
                    rsvpVipTicketList.add(ticketDao);
                }

                CreateTicketFragment.rsvpVipTicketList = rsvpVipTicketList;
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

        if (ticketType.equals(Constants.TICKET_TYPE_RSVP)) {
            binding.llMinimum.setVisibility(View.GONE);
            binding.llPricePerTicket.setVisibility(View.GONE);
            binding.llTicketSelling.setVisibility(View.GONE);
        }

        binding.startTimeImg.setOnClickListener(this::setStartTime);
        binding.tvStartTime.setOnClickListener(this::setStartTime);
        binding.endTimeImg.setOnClickListener(this::setEndTime);
        binding.tvEndTime.setOnClickListener(this::setEndTime);
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
        ticketName = binding.etTicketName.getText().toString().trim();
        pricePerTicket = binding.etPriceTicket.getText().toString().trim();
        ticketQuantity = binding.etTicketQty.getText().toString().trim();
        description = binding.etShortDesc.getText().toString().trim();

        sellingStartDate = binding.tvStartDate.getText().toString();
        sellingEndDate = binding.tvEndDate.getText().toString();
        sellingStartTime = binding.tvStartTime.getText().toString();
        sellingEndTime = binding.tvEndTime.getText().toString();
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

        if (ticketType.equals(Constants.TICKET_TYPE_VIP) && (pricePerTicket == null || pricePerTicket.equals(""))) {
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

        if (ticketType.equals(Constants.TICKET_TYPE_VIP)) {
            if (cancellationCharge < 10) {
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

        if (rsvpVipTicketList != null && rsvpVipTicketList.size() > 0) {
            for (int i = 0; i < rsvpVipTicketList.size(); i++) {
                LayoutTicketRegularBinding regularBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_ticket_regular, binding.llRegularTickets, false);

                regularBinding.tvRegularTicketName.setText(rsvpVipTicketList.get(i).getTicketName());
                regularBinding.tvTotalTickets.setText("Total Tickets - " + rsvpVipTicketList.get(i).getTicketQuantity());
                String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";
                if (rsvpVipTicketList.get(i).getTicketType().equals("VIP")) {
                    ticketTypeText = "VIP Tickets";
                    ticketPriceText = "$ " + rsvpVipTicketList.get(i).getPricePerTicket() + " / Ticket";
                    minimumChargeText = "Cancellation Charge : " + rsvpVipTicketList.get(i).getCancellationCharge() + "%";
                    if (rsvpVipTicketList.get(i).getDiscountPercentage() != 0.0) {
                        regularBinding.discountLabel.setVisibility(View.VISIBLE);
                        regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(rsvpVipTicketList.get(i).getDiscountPercentage())) + "%)");
                    } else {
                        regularBinding.discountLabel.setVisibility(View.GONE);
                    }
                } else if (rsvpVipTicketList.get(i).getTicketType().equals(Constants.TICKET_TYPE_RSVP)) {
                    ticketTypeText = "RSVP Tickets";
                    ticketPriceText = "$ 00.00 / Ticket";
                    minimumChargeText = "Cancellation Charge : 0%";
                    regularBinding.discountLabel.setVisibility(View.GONE);
                }

                regularBinding.tvTicketType.setText(ticketTypeText);
                regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");

                int finalI = i;
                regularBinding.delBtn.setOnClickListener(view -> {
                    rsvpVipTicketList.remove(finalI);
                    try {
                        CreateTicketFragment.rsvpVipTicketList = rsvpVipTicketList;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getTicketView();
                });

                regularBinding.ivEditRegular.setOnClickListener(view -> {
                    editPos = finalI;
                    setEditData(rsvpVipTicketList.get(finalI));
                });

                binding.llRegularTickets.addView(regularBinding.getRoot());

            }
        }

    }

    private void clearFields() {

        binding.nestedScroll.scrollTo(5, 10);

        // ticketType = "";
        ticketName = "";
        pricePerTicket = "";
        ticketQuantity = "";
        description = "";
        sellingStartDate = "";
        sellingEndDate = "";
        sellingStartTime = "";
        sellingEndTime = "";
        cancellationCharge = 10;

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


        binding.etTicketName.setText("");
        binding.etPriceTicket.setText("");
        binding.etTicketQty.setText("");
        binding.etShortDesc.setText("");

        binding.tvMinimumCharges.setText("10");

//        binding.rbRsvp.setChecked(false);
//        binding.rbVip.setChecked(false);
    }

    private void setEditData(RsvpVipTicketDao editData) {

        if (editData.getTicketType().equals(Constants.TICKET_TYPE_VIP)) {
            ticketType = Constants.TICKET_TYPE_VIP;
            binding.llMinimum.setVisibility(View.VISIBLE);
            binding.llPricePerTicket.setVisibility(View.VISIBLE);
            binding.llTicketSelling.setVisibility(View.VISIBLE);
            binding.rbVip.setChecked(true);
        } else if (editData.getTicketType().equals(Constants.TICKET_TYPE_RSVP)) {
            ticketType = Constants.TICKET_TYPE_RSVP;
            binding.llMinimum.setVisibility(View.GONE);
            binding.llPricePerTicket.setVisibility(View.GONE);
            binding.llTicketSelling.setVisibility(View.GONE);
            binding.rbRsvp.setChecked(true);
        }

        cancellationCharge = editData.getCancellationCharge();

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

        binding.etTicketName.setText(editData.getTicketName());
        binding.etPriceTicket.setText(editData.getPricePerTicket());
        binding.etTicketQty.setText(editData.getTicketQuantity());
        binding.etShortDesc.setText(editData.getDescription());
        if (editData.getDiscountPercentage() == 0.0) {
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
        } else {
            binding.llAddDiscount.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.VISIBLE);
            binding.etDiscount.setText("" + editData.getDiscountPercentage());
        }
        binding.tvMinimumCharges.setText(cancellationCharge + "");

    }

}