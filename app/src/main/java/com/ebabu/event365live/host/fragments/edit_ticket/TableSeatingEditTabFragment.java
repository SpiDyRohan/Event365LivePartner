package com.ebabu.event365live.host.fragments.edit_ticket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentTableSeatingEditTabBinding;
import com.ebabu.event365live.host.entities.UpdateTicketModal;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class TableSeatingEditTabFragment extends Fragment {
    private FragmentTableSeatingEditTabBinding binding;
    private LayoutInflater layoutInflater;
    private UpdateTicketModal updateTicketModal;
    private String ticketName, pricePerTicket, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime;
    private int cancellationCharge = 10, personPerTable = 1, noOfTables = 1;
    private Context context;
    private List<UpdateTicketModal.VipTableSeating> tableSeatingTicketList;
    private int editPos = -1;

    private Handler repeatUpdateHandler = new Handler();
    private Handler repeatUpdateHandlerTable = new Handler();
    private Handler repeatUpdateHandlerPerson = new Handler();

    private boolean mAutoIncrement = false, mAutoIncrementTable = false, mAutoIncrementPerson = false;
    private boolean mAutoDecrement = false, mAutoDecrementTable = false, mAutoDecrementPerson = false;
    private double discountPercentage = 0.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_table_seating_edit_tab, container, false);
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
        updateTicketModal = EditTicktActivity.updateTicketModal;

        try {
            if (updateTicketModal != null && updateTicketModal.getData().getTableSeating() != null
                    && updateTicketModal.getData().getTableSeating().getRegularTableSeating() != null) {
                tableSeatingTicketList = updateTicketModal.getData().getTableSeating().getRegularTableSeating();
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
        if (tableSeatingTicketList != null) {
            getTicketView();
        }
    }

    private void initView() {

        binding.mcvRegularForm.setVisibility(View.GONE);
        binding.llRegularTickets.setVisibility(View.VISIBLE);
        binding.llBottom.setVisibility(View.VISIBLE);


        setSellingTimeData();
        minimumCancellationCharge();
        totalTableIncDec();
        personOnTableIncDec();

        if (updateTicketModal == null) {
            updateTicketModal = new UpdateTicketModal();
        }

        if (tableSeatingTicketList == null) tableSeatingTicketList = new ArrayList<>();

        binding.ivCross.setOnClickListener(view -> {
            clearFields();
            editPos = -1;
            binding.mcvRegularForm.setVisibility(View.GONE);
            binding.llRegularTickets.setVisibility(View.VISIBLE);
            binding.llBottom.setVisibility(View.VISIBLE);
        });

        binding.addTicketLayout.setOnClickListener(v -> {

            binding.mcvRegularForm.setVisibility(View.VISIBLE);
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
            binding.llRegularTickets.setVisibility(View.GONE);
            binding.llBottom.setVisibility(View.GONE);

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

        binding.nextBtn.setOnClickListener(v -> {

            if (isValid(v)) {

                UpdateTicketModal.VipTableSeating ticketDao = new UpdateTicketModal.VipTableSeating();

                ticketDao.setTicketName(ticketName);
                ticketDao.setPricePerTicket(pricePerTicket);
                ticketDao.setDescription(description);
                ticketDao.setNoOfTables(noOfTables);
                ticketDao.setTicketType("regularTableSeating");
                ticketDao.setParsonPerTable(personPerTable);
                ticketDao.setSellingStartDate(sellingStartDate);
                ticketDao.setSellingEndDate(sellingEndDate);
                ticketDao.setSellingStartTime(sellingStartTime);
                ticketDao.setSellingEndTime(sellingEndTime);
                ticketDao.setCancellationChargeInPer(cancellationCharge);
                if(discountPercentage != 0.0)
                    ticketDao.setDiscount(discountPercentage);
                if (editPos != -1) {
                    ticketDao.setId(tableSeatingTicketList.get(editPos).getId());
                    tableSeatingTicketList.set(editPos, ticketDao);
                    editPos = -1;
                } else {
                    tableSeatingTicketList.add(ticketDao);
                }

                EditTicktActivity.tableSeatingTicketList = tableSeatingTicketList;
                getTicketView();
                clearFields();

                binding.mcvRegularForm.setVisibility(View.GONE);
                binding.llRegularTickets.setVisibility(View.VISIBLE);
                binding.llBottom.setVisibility(View.VISIBLE);

                ((EditTicktActivity) context).setEditTicketData(3, editPos);

                if (editPos != -1) {
                    editPos = -1;
                }
            }


           /* if (isValid(v)) {
                Toast.makeText(context, "Update Click", Toast.LENGTH_SHORT).show();

                binding.mcvRegularForm.setVisibility(View.GONE);
                binding.llRegularTickets.setVisibility(View.VISIBLE);
                binding.llBottom.setVisibility(View.VISIBLE);
            }*/

        });


        binding.imageView22.setOnClickListener(v -> Dialogs.setMinMaxDate(getContext(), "", updateTicketModal.
                getData().getEvent().getSellingEnd(), binding.tvStartDate, date -> {
            binding.tvStartDate.setText(date);

            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }));

        binding.tvStartDate.setOnClickListener(v -> {
            Dialogs.setMinMaxDate(getContext(), "", updateTicketModal.getData().getEvent().getSellingEnd(), binding.tvStartDate, date -> {
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
                Dialogs.setMinMaxDate(getContext(), binding.tvStartDate.getText().toString(), updateTicketModal.getData().getEvent().getSellingEnd(), binding.tvStartDate, date -> {
                    binding.tvEndDate.setText(date);
                });
        });

        binding.tvEndDate.setOnClickListener(v -> {
            if (binding.tvStartDate.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.tvStartDate.getText().toString(), updateTicketModal.getData().getEvent().getSellingEnd(), binding.tvStartDate, date -> {
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
            decrementCancellation();
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

    @SuppressLint("ClickableViewAccessibility")
    private void totalTableIncDec() {

        binding.ivAddTable.setOnClickListener(view -> {
            incrementNoTable();
        });

        binding.ivAddTable.setOnLongClickListener(view -> {
            mAutoDecrementTable = false;
            mAutoIncrementTable = true;
            repeatUpdateHandlerTable.post(new RptUpdaterTable());
            return false;
        });

        binding.ivAddTable.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrementTable) {
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
            repeatUpdateHandlerTable.post(new RptUpdaterTable());
            return false;
        });

        binding.ivRemoveTable.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrementTable) {
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
            repeatUpdateHandlerPerson.post(new RptUpdaterPerson());
            return false;
        });

        binding.ivAddPerson.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoIncrementPerson) {
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
            repeatUpdateHandlerPerson.post(new RptUpdaterPerson());
            return false;
        });

        binding.ivRemovePerson.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                        && mAutoDecrementPerson) {
                    mAutoDecrementPerson = false;
                }
                return false;
            }
        });

    }

    private void incrementCancellation() {
        if (cancellationCharge < 100) {
            cancellationCharge++;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private void decrementCancellation() {
        if (cancellationCharge > 10) {
            cancellationCharge--;
            binding.tvMinimumCharges.setText(cancellationCharge + "");
        }
    }

    private void incrementNoTable() {
        noOfTables++;
        binding.tvNoOfTable.setText(noOfTables + "");
    }

    private void decrementNoTable() {
        if (noOfTables > 1) {
            noOfTables--;
            binding.tvNoOfTable.setText(noOfTables + "");
        }
    }

    private void incrementPerson() {
        if (personPerTable < 100) {
            personPerTable++;
            binding.tvPersonPerTable.setText(personPerTable + "");
        }
    }

    private void decrementPerson() {
        if (personPerTable > 1) {
            personPerTable--;
            binding.tvPersonPerTable.setText(personPerTable + "");
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
        } */else if (cancellationCharge < 10) {
            Dialogs.toast(getContext(), v, "Minimum of 10% is Mandatory");
            return false;
        } else if (sellingStartDate.equalsIgnoreCase(getString(R.string.start_date))) {
            Dialogs.toast(getContext(), v, "Select Start Date please!");
            return false;
        } else if (sellingEndDate.equalsIgnoreCase(getString(R.string.end_date))) {
            Dialogs.toast(getContext(), v, "Select End Date please!");
            return false;
        } else if (sellingStartTime.equalsIgnoreCase(getString(R.string.start_time))) {
            Dialogs.toast(getContext(), v, "Select Start Time please!");
            return false;
        } else if (sellingEndTime.equalsIgnoreCase(getString(R.string.end_time))) {
            Dialogs.toast(getContext(), v, "Select End Time please!");
            return false;
        }

        return true;
    }

    private void setStartTime(View v) {

        Dialogs.setTime(getContext(), binding.tvEndTime, time -> {
            if (!binding.tvStartDate.getText().equals(updateTicketModal.getData().getEvent().getSellingEnd())) {
                binding.tvStartTime.setText(time);
            } else if (compareTime(time, updateTicketModal.getData().getEvent().getSellingEnd())) {
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
        if (binding.tvStartTime.getText().toString().equalsIgnoreCase(getString(R.string.start_time)))
            Toast.makeText(getContext(), "Select start time first!", Toast.LENGTH_LONG).show();
        else

            Dialogs.setTime(getContext(), binding.tvEndTime, time -> {
                if (!binding.tvStartDate.getText().equals(binding.tvEndDate.getText().toString())) {

                    if (binding.tvEndDate.getText().equals(updateTicketModal.getData().getEvent().getSellingEnd())) {
                        if (compareTime(time, updateTicketModal.getData().getEvent().getSellingEnd())) {
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
                        if (binding.tvEndDate.getText().equals(updateTicketModal.getData().getEvent().getSellingStart())) {
                            if (compareTime(time, updateTicketModal.getData().getEvent().getSellingStart())) {
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
        if (updateTicketModal != null && updateTicketModal.getData().getEvent().getSellingEnd() != null) {
            binding.tvStartTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), true));
            binding.tvEndTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), true));
            binding.tvEndDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), false));
            binding.tvStartDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), false));
        }
    }

    private void getTicketView() {
        binding.llRegularTickets.removeAllViews();

        if (tableSeatingTicketList != null && tableSeatingTicketList.size() > 0) {
            for (int i = 0; i < tableSeatingTicketList.size(); i++) {

                if (!tableSeatingTicketList.get(i).getIsCancelled()) {


                    com.ebabu.event365live.host.databinding.LayoutTicketRegularBinding regularBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_ticket_regular, binding.llRegularTickets, false);

                    regularBinding.tvTotalTickets.setVisibility(View.GONE);
                    regularBinding.tvPersonPerTable.setVisibility(View.VISIBLE);
                    regularBinding.tvTotalTable.setVisibility(View.VISIBLE);

                    regularBinding.tvRegularTicketName.setText(StringUtils.capitalizeWord(tableSeatingTicketList.get(i).getTicketName()));
                    regularBinding.tvPersonPerTable.setText("Person Per Tables - " + tableSeatingTicketList.get(i).getParsonPerTable());
                    regularBinding.tvTotalTable.setText("Total Tables - " + tableSeatingTicketList.get(i).getNoOfTables());
                    String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";

                    ticketTypeText = "Table & Seatings";
                    ticketPriceText = "$ " + tableSeatingTicketList.get(i).getPricePerTicket() + " / Ticket";
                    minimumChargeText = "Cancellation Charge : " + tableSeatingTicketList.get(i).getCancellationChargeInPer() + "%";

                    regularBinding.tvTicketType.setText(ticketTypeText);
                    regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                    regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");
                    if (tableSeatingTicketList.get(i).isTicketBooked()) {
                        regularBinding.ivEditRegular.setVisibility(View.GONE);
                        regularBinding.delBtn.setVisibility(View.INVISIBLE);
                    } else {
                        regularBinding.ivEditRegular.setVisibility(View.VISIBLE);
                        regularBinding.delBtn.setVisibility(View.VISIBLE);
                    }

                    if(tableSeatingTicketList.get(i).getDiscount()!=0.0){
                        regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable,String.valueOf(tableSeatingTicketList.get(i).getDiscount()))+"%)");
                    }  else {
                        regularBinding.discountLabel.setVisibility(View.GONE);
                    }

                    int finalI = i;
                    regularBinding.delBtn.setOnClickListener(view -> {

                        EditTicktActivity.cancelTicketIdList.add(Integer.parseInt(tableSeatingTicketList.get(finalI).getId()));

                        tableSeatingTicketList.remove(finalI);
                        try {
                            EditTicktActivity.tableSeatingTicketList = tableSeatingTicketList;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ((EditTicktActivity) context).setDeleteTicketData();
                        getTicketView();
                    });

                    regularBinding.ivEditRegular.setOnClickListener(view -> {
                        if (tableSeatingTicketList.get(finalI).isTicketBooked()) {
                            Toast.makeText(context, "Ticket is booked, you can not edit this ticket.", Toast.LENGTH_SHORT).show();
                        } else {

                            binding.mcvRegularForm.setVisibility(View.VISIBLE);
                            binding.llRegularTickets.setVisibility(View.GONE);
                            binding.llBottom.setVisibility(View.GONE);


                            editPos = finalI;
                            setEditData(tableSeatingTicketList.get(finalI));
                        }
                    });

                    binding.llRegularTickets.addView(regularBinding.getRoot());
                }
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

        if (updateTicketModal != null && updateTicketModal.getData().getEvent().getSellingEnd() != null) {
            binding.tvStartTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), true));
            binding.tvEndTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), true));
            binding.tvEndDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), false));
            binding.tvStartDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), false));
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

    private void setEditData(UpdateTicketModal.VipTableSeating editData) {

        cancellationCharge = editData.getCancellationChargeInPer();
        noOfTables = editData.getNoOfTables();
        personPerTable = editData.getParsonPerTable();

        /*if (editData != null && updateTicketModal.getData().getEvent().getSellingEnd() != null) {
            binding.tvStartTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), true));
            binding.tvEndTime.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), true));
            binding.tvEndDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingEnd(), false));
            binding.tvStartDate.setText(Utility.getDateTime(updateTicketModal.getData().getEvent().getSellingStart(), false));
        } else {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }*/

        if (editData != null && editData.getSellingEndTime() != null) {
            binding.tvStartTime.setText(Utility.getTime12HourFormat(editData.getSellingStartTime()));
            binding.tvEndTime.setText(Utility.getTime12HourFormat(editData.getSellingEndTime()));
            binding.tvEndDate.setText(Utility.getDateTimeOnEdit(editData.getSellingEndDate(), false));
            binding.tvStartDate.setText(Utility.getDateTimeOnEdit(editData.getSellingStartDate(), false));
        } else {
            binding.tvStartDate.setText(getString(R.string.start_date));
            binding.tvEndDate.setText(getString(R.string.end_date));
            binding.tvStartTime.setText(getString(R.string.start_time));
            binding.tvEndTime.setText(getString(R.string.end_time));
        }

        binding.etCategoryName.setText(StringUtils.capitalizeWord(editData.getTicketName()));
        binding.etPricePerTicket.setText(editData.getPricePerTicket());
        binding.etShortDesc.setText(editData.getDescription());

        binding.tvMinimumCharges.setText(cancellationCharge + "");
        binding.tvNoOfTable.setText(noOfTables + "");
        binding.tvPersonPerTable.setText(personPerTable + "");
        if(editData.getDiscount()==0.0){
            binding.llAddDiscount.setVisibility(View.VISIBLE);
            binding.llEnterDiscount.setVisibility(View.GONE);
        } else {
            binding.llAddDiscount.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.VISIBLE);
            binding.etDiscount.setText(""+editData.getDiscount());
        }
    }

    class RptUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                incrementCancellation();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            } else if (mAutoDecrement) {
                decrementCancellation();
                repeatUpdateHandler.postDelayed(new RptUpdater(), 50);
            }
        }
    }

    class RptUpdaterTable implements Runnable {
        public void run() {
            if (mAutoIncrementTable) {
                incrementNoTable();
                repeatUpdateHandlerTable.postDelayed(new RptUpdaterTable(), 50);
            } else if (mAutoDecrementTable) {
                decrementNoTable();
                repeatUpdateHandlerTable.postDelayed(new RptUpdaterTable(), 50);
            }
        }
    }

    class RptUpdaterPerson implements Runnable {
        public void run() {
            if (mAutoIncrementPerson) {
                incrementPerson();
                repeatUpdateHandlerPerson.postDelayed(new RptUpdaterPerson(), 50);
            } else if (mAutoDecrementPerson) {
                decrementPerson();
                repeatUpdateHandlerPerson.postDelayed(new RptUpdaterPerson(), 50);
            }
        }
    }

}