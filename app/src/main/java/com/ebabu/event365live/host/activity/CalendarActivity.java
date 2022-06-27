package com.ebabu.event365live.host.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.CreateEventViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class CalendarActivity extends BaseActivity {

    MaterialCalendarView mcv;
    Calendar myCalendar = Calendar.getInstance();
    List<CalendarDay> enableDates;
    List<CalendarDay> disableDates;
    CreateEventViewModel viewModel;
    MyLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        viewModel= ViewModelProviders.of(this).get(CreateEventViewModel.class);
        loader=new MyLoader(this);
        loader.show("");
        enableDates=new ArrayList<>();
        disableDates=new ArrayList<>();
        mcv=findViewById(R.id.calendarView);

        mcv.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return true;
            }
            @Override
            public void decorate(DayViewFacade view) {
                view.setDaysDisabled(true);
            }
        });

        viewModel.getEventDates().observe(this, new Observer<UserResponse>() {
            @Override
            public void onChanged(UserResponse response) {
                loader.dismiss();
                if(response.getCode()== API.SESSION_EXPIRE){
                    response.setSuccess(true);
                    response.setUserData(null);
                    response.setCode(0);
                    Utility.sessionExpired(getApplicationContext());
                }
                else if(!response.isSuccess())
                    Dialogs.toast(getApplicationContext(),mcv,response.getMessage());

                else {
                    try {
                        CalendarDay today=CalendarDay.today();

                        JSONArray jsonArray = new JSONArray(response.getMessage());
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject=jsonArray.getJSONObject(i);
                            String date=jsonObject.getString("start");
                            String[] dateArr=StringUtils.getDateByPattern("yyyy MM dd",date).split(" ");
                            CalendarDay calendarDay=CalendarDay.from(LocalDate.of(Integer.parseInt(dateArr[0]),Integer.parseInt(dateArr[1]),Integer.parseInt(dateArr[2])));
                            if(calendarDay.isBefore(today))
                                disableDates.add(calendarDay);
                            else enableDates.add(calendarDay);
                        }
                        Log.e(disableDates.toString(),enableDates.toString());
                        mcv.addDecorator(new EnableDecorator(enableDates));
                        mcv.addDecorator(new DisableDecorator(disableDates));

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });


        findViewById(R.id.back_arrow).setOnClickListener(v->onBackPressed());

        mcv.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                widget.clearSelection();
                startActivity(new Intent(CalendarActivity.this,EventListActivity.class)
                .putExtra("date",date));
            }
        });

        mcv.state().edit()

                .setMinimumDate(LocalDate.of(myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),1))
                .setMaximumDate(LocalDate.of(myCalendar.get(Calendar.YEAR)+1,myCalendar.get(Calendar.MONTH),28))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();


    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(()->{
            if(isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }

    public class EnableDecorator implements DayViewDecorator {


        private final HashSet<CalendarDay> dates;

        public EnableDecorator(Collection<CalendarDay> dates) {

            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.enable_event));
        }
    }

    public class DisableDecorator implements DayViewDecorator {


        private final HashSet<CalendarDay> dates;

        public DisableDecorator(Collection<CalendarDay> dates) {
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);

        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.disable_event));
        }
    }
}
