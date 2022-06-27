package com.ebabu.event365live.host.viewmodels;

import android.app.Application;
import android.view.View;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.CategoryResponse;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.SubCategoryResponse;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.repositories.EventRepository;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import okhttp3.RequestBody;

public class CreateEventViewModel extends AndroidViewModel {
    @Inject
    ApiInterface apiInterface;
    EventRepository repository;
    private MutableLiveData<String> daysSelected = new MutableLiveData<>();
    private LinkedHashSet<String> days = new LinkedHashSet<>();
    private LinkedHashSet<String> months = new LinkedHashSet<>();

    public CreateEventViewModel(@NonNull Application application) {
        super(application);
        App.getAppComponent().inject(this);
        repository = EventRepository.getInstance();
    }

    public void selectDays(View view) {
        Chip chip = (Chip) view;
        if (chip.isChecked())
            days.add(chip.getText().toString());
        else
            days.remove(chip.getText().toString());

        daysSelected.setValue(days.toString().substring(1, days.toString().length() - 1));
    }

    public void selectdays(View view)
    {
        Chip chip = (Chip) view;
        if (chip.isChecked())
            days.add(chip.getText().toString());
        else
            days.remove(chip.getText().toString());

        daysSelected.setValue(days.toString().substring(1, days.toString().length() - 1));
    }
    public void selectdays1(View view) {
        Chip chip = (Chip) view;
        if (chip.isChecked())
            days.add(chip.getText().toString());
        else
            days.remove(chip.getText().toString());
        daysSelected.setValue(days.toString().substring(1, days.toString().length() - 1));
    }

    public void selectMonths(View view) {

        Chip chip = (Chip) view;
        if (chip.isChecked())
            months.add(chip.getText().toString());
        else
            months.remove(chip.getText().toString());
        daysSelected.setValue(months.toString().substring(1, months.toString().length() - 1));
    }
    public void selectmonths(View view) {

        Chip chip = (Chip) view;
        if (chip.isChecked()) {
            months.add(chip.getText().toString());
        }
        else
        {
            months.remove(chip.getText().toString());
        }
        daysSelected.setValue(months.toString().substring(1, months.toString().length() - 1));
    }

    public LiveData<String> getDaysSelected()
    {
        return daysSelected;
    }

    public void reset(long Type) {
        if (Type == 2 || Type == 1) {
            daysSelected.setValue(days.toString().substring(1, days.toString().length() - 1));
        } else if (Type == 3)
            daysSelected.setValue(months.toString().substring(1, months.toString().length() - 1));

    }

    public LiveData<CategoryResponse> fetchCategories() {
        return repository.getCategories(apiInterface);
    }

    public LiveData<SubCategoryResponse> fetchSubCategories(int id) {
        return repository.getSubCategories(apiInterface, id);
    }

    public LiveData<UserResponse> getEditEventDetails(int id) {
        return repository.getEditEventDetails(apiInterface, id);
    }

    public LiveData<UserResponse> getEventDates() {
        return repository.getEventDates(apiInterface);
    }

    public LiveData<UserResponse> getEventsByDate(JsonObject jsonObject) {
        return repository.getEventsByDate(apiInterface, jsonObject);
    }

    public LiveData<MyResponse> cancelTicket(int userid, String qrCode,int book_id) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userid);
        jsonObject.addProperty("QRkey", qrCode);
        jsonObject.addProperty("ticketBookedId", book_id);

        return repository.cancelTicket(apiInterface, jsonObject);
    }

    public LiveData<MyResponse> editEventRequest(Map<String,RequestBody> editEventAllObj, List<ImageDAO> img) {
        return repository.updateCreatedEvent(apiInterface,editEventAllObj,img);
    }

}
