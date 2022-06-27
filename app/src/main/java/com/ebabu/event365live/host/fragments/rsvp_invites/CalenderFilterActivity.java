package com.ebabu.event365live.host.fragments.rsvp_invites;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivityCalenderBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class CalenderFilterActivity extends AppCompatActivity {

    CalendarDay startDate, endDate, calenderDayStartDate, calenderDayEndDate;
    String selectedDate = "", selectedEndDate = "";
    private ActivityCalenderBinding calenderBinding;
    private CalendarDay calenderDate;
    private String filterStartDate, filterEndDate,selectedCalenderDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calenderBinding = DataBindingUtil.setContentView(this, R.layout.activity_calender);

        init();

        calenderBinding.calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return false;
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setDaysDisabled(true);

            }
        });

        if (selectedCalenderDate != null)
            calenderBinding.calendarView.setSelectedDate(LocalDate.parse(selectedCalenderDate));


        try {

            if (PastAttendeesFragment.startDate != null && !PastAttendeesFragment.startDate.equals("") && PastAttendeesFragment.endDate != null && PastAttendeesFragment.endDate != null) {

                selectedDate = PastAttendeesFragment.startDate;
                selectedEndDate = PastAttendeesFragment.endDate;

                filterStartDate = selectedDate;
                filterEndDate = selectedEndDate;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (selectedDate != null && !selectedDate.equals("") && selectedEndDate != null && selectedEndDate != null) {

            try {
                // calenderBinding.calendarView.setSelectedDate(LocalDate.parse(selectedDate));

                calenderDayStartDate = CalendarDay.from(LocalDate.parse(selectedDate));
                calenderDayEndDate = CalendarDay.from(LocalDate.parse(selectedEndDate));

                calenderBinding.calendarView.selectRange(calenderDayStartDate, calenderDayEndDate);
//            setEvent(dates);
                setEvent(calenderBinding.calendarView.getSelectedDates());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        calenderBinding.calendarView.setOnDateChangedListener((widget, date, selected) -> {

        });


        calenderBinding.calendarView.setOnRangeSelectedListener((widget, dates) -> {

            int date = dates.size() - 1;
            startDate = dates.get(0);
            endDate = dates.get(date);

            String startMonth = "", startDay = "", endMonth = "", endDay = "";

            if((startDate.getMonth()+"").length() == 1){
                startMonth = "0"+startDate.getMonth();
            }else {
                startMonth = ""+startDate.getMonth();
            }

            if((startDate.getDay()+"").length() == 1){
                startDay = "0"+startDate.getDay();
            }else {
                startDay = ""+startDate.getDay();
            }

            if((endDate.getMonth()+"").length() == 1){
                endMonth = "0"+endDate.getMonth();
            }else {
                endMonth = ""+endDate.getMonth();
            }

            if((endDate.getDay()+"").length() == 1){
                endDay = "0"+endDate.getDay();
            }else {
                endDay = ""+endDate.getDay();
            }

            selectedDate = startDate.getYear() + "-" + startMonth + "-" + startDay;
            selectedEndDate = endDate.getYear() + "-" + endMonth + "-" + endDay;

            filterStartDate = selectedDate;
            filterEndDate = selectedEndDate;

            calenderDate = null;

            setEvent(dates);
        });

        LocalDate date = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        calenderBinding.calendarView.state().edit()
//                .setMinimumDate(date)
//                .setMaximumDate(LocalDate.parse(date.toString()).plusYears(1))
                .setMinimumDate(LocalDate.parse(date.toString()).minusYears(50))
                .setMaximumDate(date)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
    }

    private void init() {
        calenderBinding.ivClose.setOnClickListener(view -> onBackPressed());
    }

    public void backBtnOnClick(View view) {
        finish();
    }

    public void submitOnClick(View view) {
        if (TextUtils.isEmpty(filterStartDate) || TextUtils.isEmpty(filterEndDate)) {
            Toast.makeText(this, "Please select filter date", Toast.LENGTH_SHORT).show();
            return;
        }

        PastAttendeesFragment.startDate = filterStartDate;
        PastAttendeesFragment.endDate = filterEndDate;

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    void setEvent(List<CalendarDay> dateList) {

        calenderBinding.calendarView.removeDecorators();

        List<CalendarDay> datesLeft = new ArrayList<>();
        List<CalendarDay> datesCenter = new ArrayList<>();
        List<CalendarDay> datesRight = new ArrayList<>();
        List<CalendarDay> datesIndependent = new ArrayList<>();

        for (int i = 0; i < dateList.size(); i++) {

            boolean right = false;
            boolean left = false;

            if (i == 0) {
                left = true;
            } else if (i == dateList.size() - 1) {
                right = true;
            } else {
                left = true;
                right = true;
            }

            if (left && right) {
                datesCenter.add(dateList.get(i));
            } else if (left) {
                datesLeft.add(dateList.get(i));
            } else if (right) {
                datesRight.add(dateList.get(i));
            } else {
                datesIndependent.add(dateList.get(i));
            }
        }

        setDecor(datesCenter, R.drawable.calender_bg_center, Color.BLACK);
        setDecor(datesLeft, R.drawable.calender_bg_left, Color.WHITE);
        setDecor(datesRight, R.drawable.calender_bg_right, Color.WHITE);
//            setDecor(datesIndependent, R.drawable.g_independent);

    }

    void setDecor(List<CalendarDay> calendarDayList, int drawable, int textColor) {

        calenderBinding.calendarView.addDecorators(new EventDecorator(CalenderFilterActivity.this
                , drawable
                , calendarDayList, textColor));
    }

    public class EventDecorator implements DayViewDecorator {
        Context context;
        private int drawable;
        private HashSet<CalendarDay> dates;
        private int textColor;

        public EventDecorator(Context context, int drawable, List<CalendarDay> calendarDays1, int textColor) {
            this.context = context;
            this.drawable = drawable;
            this.textColor = textColor;
            this.dates = new HashSet<>(calendarDays1);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // apply drawable to dayView
            view.setSelectionDrawable(context.getResources().getDrawable(drawable));
            // white text color
            view.addSpan(new ForegroundColorSpan(textColor));
        }
    }
}
