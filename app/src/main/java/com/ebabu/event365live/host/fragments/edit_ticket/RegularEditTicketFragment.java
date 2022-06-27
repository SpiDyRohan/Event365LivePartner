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

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentRegularEditTicketBinding;
import com.ebabu.event365live.host.databinding.LayoutTicketRegularBinding;
import com.ebabu.event365live.host.entities.UpdateTicketModal;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;

public class RegularEditTicketFragment extends Fragment {

    int cancellationCharge = 10;
    private FragmentRegularEditTicketBinding binding;
    private LayoutInflater layoutInflater;
    private UpdateTicketModal updateTicketModal;
    private String ticketType = "Paid", ticketName, pricePerTicket, ticketQuantity, description, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime;
    private Context context;
    private int editPos = -1;
    private double discountPercentage = 0.0;

    private int maxTicketQuantity = 0;
    private CompositeDisposable disposable;
    private boolean isShortDesc2Valid;
    private List<UpdateTicketModal.FreeTicket> regularFreeTicketList;
    private Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_regular_edit_ticket, container, false);

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
        regularFreeTicketList = new ArrayList<>();
        try {
            if (updateTicketModal != null && updateTicketModal.getData().getFreeNormal() != null) {
                regularFreeTicketList.addAll(updateTicketModal.getData().getFreeNormal());
            }

            if (updateTicketModal != null && updateTicketModal.getData().getRegularPaid() != null) {
                regularFreeTicketList.addAll(updateTicketModal.getData().getRegularPaid());
            }

            if (regularFreeTicketList != null && !regularFreeTicketList.isEmpty()) {
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

        //newly added
        disposable = new CompositeDisposable();

        //code we require
        disposable.add(
                RxTextView.textChanges(binding.etShortDesc2)
                        .subscribe(charSequence -> {
                            if (charSequence.length() >= 200) {
                                if (!isShortDesc2Valid) {
                                    isShortDesc2Valid= true;
                                    binding.shortDesc2EtImg.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (isShortDesc2Valid) {
                                    isShortDesc2Valid = false;
                                    binding.shortDesc2EtImg.setVisibility(View.INVISIBLE);
                                }
                            }
                        }));

        binding.mcvRegularForm.setVisibility(View.GONE);
        binding.llRegularTickets.setVisibility(View.VISIBLE);
        binding.llBottom.setVisibility(View.VISIBLE);

        setSellingTimeData();
        minimumCancellationCharge();

        if (updateTicketModal == null) {
            updateTicketModal = new UpdateTicketModal();
        }

        if (regularFreeTicketList == null) regularFreeTicketList = new ArrayList<>();

        binding.ivCross.setOnClickListener(view -> {
            clearFields();
            editPos = -1;
            binding.mcvRegularForm.setVisibility(View.GONE);
            binding.llRegularTickets.setVisibility(View.VISIBLE);
            binding.llBottom.setVisibility(View.VISIBLE);
        });

        binding.rbPaidTicket.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ticketType = Constants.TICKET_TYPE_PAID;
                binding.llMinimum.setVisibility(View.VISIBLE);
                binding.llPricePerTicket.setVisibility(View.VISIBLE);
                discountPercentage = 0.0;
                binding.etDiscount.setText("");
                binding.llAddDiscount.setVisibility(View.VISIBLE);
                binding.llEnterDiscount.setVisibility(View.GONE);
            }
        });

        binding.rbFreeTicket.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                ticketType = Constants.TICKET_TYPE_FREE;
                binding.llMinimum.setVisibility(View.GONE);
                binding.llPricePerTicket.setVisibility(View.GONE);
                discountPercentage = 0.0;
                binding.etDiscount.setText("");
                binding.llEnterDiscount.setVisibility(View.GONE);
                binding.llAddDiscount.setVisibility(View.GONE);
            }
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

                UpdateTicketModal.FreeTicket ticketDao = new UpdateTicketModal.FreeTicket();

                ticketDao.setTicketType(ticketType);
                ticketDao.setTicketName(ticketName);
                ticketDao.setPricePerTicket(pricePerTicket);
                ticketDao.setTotalQuantity(Integer.parseInt(ticketQuantity));
                ticketDao.setDescription(description);
                ticketDao.setSellingStartDate(sellingStartDate);
                ticketDao.setSellingEndDate(sellingEndDate);
                ticketDao.setSellingStartTime(sellingStartTime);
                ticketDao.setSellingEndTime(sellingEndTime);
                ticketDao.setCancellationChargeInPer(cancellationCharge + "");
                if(discountPercentage != 0.0)
                    ticketDao.setDiscount(discountPercentage);

                if (editPos != -1) {
                    ticketDao.setId(regularFreeTicketList.get(editPos).getId());
                    regularFreeTicketList.set(editPos, ticketDao);

                } else {
                    regularFreeTicketList.add(ticketDao);
                }
                EditTicktActivity.regularTicketList = regularFreeTicketList;

                getTicketView();
                clearFields();

                binding.mcvRegularForm.setVisibility(View.GONE);
                binding.llRegularTickets.setVisibility(View.VISIBLE);
                binding.llBottom.setVisibility(View.VISIBLE);

                ((EditTicktActivity) context).setEditTicketData(1, editPos);

                if (editPos != -1) {
                    editPos = -1;
                }

            }


           /* if (isValid(v)) {
                Toast.makeText(context, "Update Click", Toast.LENGTH_SHORT).show();

                binding.mcvRegularForm.setVisibility(View.GONE);
                binding.llRegularTickets.setVisibility(View.VISIBLE);
                binding.llBottom.setVisibility(View.VISIBLE);

                ((EditTicktActivity)context).setEditTicketData();

            }*/


        });


        binding.imageView22.setOnClickListener(v -> Dialogs.setMinMaxDate(getContext(), "", updateTicketModal.getData().getEvent().getSellingStart(), binding.tvStartDate, date -> {
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
        description = binding.etShortDesc2.getText().toString().trim();

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

        if (ticketType == null || ticketType.equalsIgnoreCase("")) {
            Dialogs.toast(getContext(), v, "Choose Ticket Type please!");
            return false;
        } else if (ticketName == null || ticketName.equalsIgnoreCase("")) {
            Dialogs.toast(getContext(), v, "Enter Ticket Name please!");
            binding.etTicketName.requestFocus();
            return false;
        }

        if (ticketType.equalsIgnoreCase(Constants.TICKET_TYPE_PAID) && (pricePerTicket == null || pricePerTicket.equalsIgnoreCase(""))) {
            Dialogs.toast(getContext(), v, "Enter Price Per Ticket please!");
            binding.etPriceTicket.requestFocus();
            return false;
        }

        if (ticketQuantity == null || ticketQuantity.equalsIgnoreCase("") || ticketQuantity.equalsIgnoreCase("0")) {
            Dialogs.toast(getContext(), v, "Enter Ticket Quantity please!");
            binding.etTicketQty.requestFocus();
            return false;
        }

        try {
            if (Integer.parseInt(ticketQuantity) < maxTicketQuantity) {
                Dialogs.toast(getContext(), v, "The ticket quantity can’t be reduced");
                binding.etTicketQty.requestFocus();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

/*
        if (description == null || description.equals("")) {
            Dialogs.toast(getContext(), v, "Enter Short Description please!");
            binding.etShortDesc2.requestFocus();
            return false;
        }
*/

        //newly added
        if (description.length() > 200) {
            binding.etShortDesc2.requestFocus();
            Dialogs.toast(getContext(), v, getString(R.string.shortDescMax2));
            return false;
        }

        if (ticketType.equalsIgnoreCase(Constants.TICKET_TYPE_PAID)) {
            if (cancellationCharge < 10) {
                Dialogs.toast(getContext(), v, "Minimum of 10% is Mandatory");
                return false;
            }
        }

        if (sellingStartDate.equalsIgnoreCase(getString(R.string.start_date))) {
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
            binding.tvStartDate.setText(Utility.getDateTimeLocal(updateTicketModal.getData().getEvent().getSellingStart(), false));
            binding.tvStartTime.setText(Utility.getDateTimeLocal(updateTicketModal.getData().getEvent().getSellingStart(), true));

            binding.tvEndTime.setText(Utility.getDateTimeLocal(updateTicketModal.getData().getEvent().getSellingEnd(), true));
            binding.tvEndDate.setText(Utility.getDateTimeLocal(updateTicketModal.getData().getEvent().getSellingEnd(), false));
        }
    }

    private void getTicketView() {
        binding.llRegularTickets.removeAllViews();

        if (regularFreeTicketList != null && regularFreeTicketList.size() > 0) {
            for (int i = 0; i < regularFreeTicketList.size(); i++) {

                if (!regularFreeTicketList.get(i).getIsCancelled()) {

                    LayoutTicketRegularBinding regularBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_ticket_regular, binding.llRegularTickets, false);

                    regularBinding.tvRegularTicketName.setText(StringUtils.capitalizeWord(regularFreeTicketList.get(i).getTicketName()));
                    regularBinding.tvTotalTickets.setText("Total Tickets - " + regularFreeTicketList.get(i).getTotalQuantity());
                    String ticketTypeText = "", ticketPriceText = "", minimumChargeText = "";
                    if (regularFreeTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_PAID) ||
                            regularFreeTicketList.get(i).getTicketType().equalsIgnoreCase("regularPaid")) {
                        ticketTypeText = "Paid Tickets";
                        ticketPriceText = "$ " + regularFreeTicketList.get(i).getPricePerTicket() + " / Ticket";
                        minimumChargeText = "Cancellation Charge : " + regularFreeTicketList.get(i).getCancellationChargeInPer() + "%";
                        if(regularFreeTicketList.get(i).getDiscount() !=0.0) {
                            regularBinding.discountLabel.setText(getResources().getString(R.string.discount_added_lable, String.valueOf(regularFreeTicketList.get(i).getDiscount())) + "%)");
                        } else {
                            regularBinding.discountLabel.setVisibility(View.GONE);
                        }
                    } else if (regularFreeTicketList.get(i).getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_FREE) || regularFreeTicketList.get(i).getTicketType().equalsIgnoreCase("freeNormal")) {
                        ticketTypeText = "Free Tickets";
                        ticketPriceText = "$ 00.00 / Ticket";
                        minimumChargeText = "Cancellation Charge : 0%";
                        regularBinding.discountLabel.setVisibility(View.GONE);
                    }

                    regularBinding.tvTicketType.setText(ticketTypeText);
                    regularBinding.tvPerTicketPrice.setText(ticketPriceText);
                    regularBinding.minimumChargesLabel.setText("(" + minimumChargeText + ")");

                    if(regularFreeTicketList.get(i).isTicketBooked()){
                        regularBinding.ivEditRegular.setVisibility(View.GONE);
                        regularBinding.delBtn.setVisibility(View.INVISIBLE);
                    }else {
                        regularBinding.ivEditRegular.setVisibility(View.VISIBLE);
                        regularBinding.delBtn.setVisibility(View.VISIBLE);
                    }

                    int finalI = i;
                    regularBinding.delBtn.setOnClickListener(view -> {

                        EditTicktActivity.cancelTicketIdList.add(Integer.parseInt(regularFreeTicketList.get(finalI).getId()));

                        regularFreeTicketList.remove(finalI);
                        try {
                            EditTicktActivity.regularTicketList = regularFreeTicketList;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ((EditTicktActivity) context).setDeleteTicketData();
                        getTicketView();
                    });

                    regularBinding.ivEditRegular.setOnClickListener(view -> {
                        if (regularFreeTicketList.get(finalI).isTicketBooked()) {
                            Toast.makeText(context, "Ticket is booked, you can not edit this ticket.", Toast.LENGTH_SHORT).show();
                        } else {
                            editPos = finalI;
                            binding.mcvRegularForm.setVisibility(View.VISIBLE);
                            binding.llRegularTickets.setVisibility(View.GONE);
                            binding.llBottom.setVisibility(View.GONE);

                            setEditData(regularFreeTicketList.get(finalI));
                        }
                    });

                    binding.llRegularTickets.addView(regularBinding.getRoot());
                }
            }
        }
    }

    private void clearFields() {

        maxTicketQuantity = 0;
        binding.nestedScroll.scrollTo(5, 10);

        //   ticketType = "";
        ticketName = "";
        pricePerTicket = "";
        ticketQuantity = "";
        description = "";
        sellingStartDate = "";
        sellingEndDate = "";
        sellingStartTime = "";
        sellingEndTime = "";
        cancellationCharge = 10;

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

        binding.etTicketName.setText("");
        binding.etPriceTicket.setText("");
        binding.etTicketQty.setText("");
        binding.etShortDesc2.setText("");

        binding.tvMinimumCharges.setText("10");

    }

    private void setEditData(UpdateTicketModal.FreeTicket editData) {

        if (editData.getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_PAID) ||
                editData.getTicketType().equalsIgnoreCase("regularPaid")) {
            ticketType = Constants.TICKET_TYPE_PAID;
            binding.llMinimum.setVisibility(View.VISIBLE);
            binding.llPricePerTicket.setVisibility(View.VISIBLE);
            binding.rbPaidTicket.setChecked(true);
            binding.rbFreeTicket.setVisibility(View.GONE);
            if(editData.getDiscount()==0.0){
                binding.llAddDiscount.setVisibility(View.VISIBLE);
                binding.llEnterDiscount.setVisibility(View.GONE);
            } else {
                binding.llAddDiscount.setVisibility(View.GONE);
                binding.llEnterDiscount.setVisibility(View.VISIBLE);
                binding.etDiscount.setText(""+editData.getDiscount());
            }
        } else if (editData.getTicketType().equalsIgnoreCase(Constants.TICKET_TYPE_FREE) ||
                editData.getTicketType().equalsIgnoreCase("freeNormal")) {
            ticketType = Constants.TICKET_TYPE_FREE;
            binding.llMinimum.setVisibility(View.GONE);
            binding.llPricePerTicket.setVisibility(View.GONE);
            binding.rbFreeTicket.setChecked(true);
            binding.rbPaidTicket.setVisibility(View.GONE);
            binding.llEnterDiscount.setVisibility(View.GONE);
            binding.llAddDiscount.setVisibility(View.GONE);
        }

        if (editData.getCancellationChargeInPer() != null) {
            cancellationCharge = Integer.parseInt(editData.getCancellationChargeInPer());
        }

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

        binding.etTicketName.setText(StringUtils.capitalizeWord(editData.getTicketName()));
        binding.etPriceTicket.setText(editData.getPricePerTicket());
        binding.etTicketQty.setText(editData.getTotalQuantity() + "");
        binding.etShortDesc2.setText(editData.getDescription());

        maxTicketQuantity = editData.getTotalQuantity();

        binding.tvMinimumCharges.setText(cancellationCharge + "");

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

}