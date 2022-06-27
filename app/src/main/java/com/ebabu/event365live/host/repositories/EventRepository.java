package com.ebabu.event365live.host.repositories;

import android.util.Log;

import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.entities.Category;
import com.ebabu.event365live.host.entities.CategoryResponse;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.SubCategory;
import com.ebabu.event365live.host.entities.SubCategoryResponse;
import com.ebabu.event365live.host.entities.UpdateEventDao;
import com.ebabu.event365live.host.entities.UserResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private MutableLiveData<List<EventDAO>> upComingEventData = new MutableLiveData<>();
    private MutableLiveData<List<EventDAO>> pastEventData = new MutableLiveData<>();
    private MutableLiveData<CategoryResponse> categoryResponseMutable = new MutableLiveData<>();
    private MutableLiveData<SubCategoryResponse> subCategoryResponseMutable = new MutableLiveData<>();

    public MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>();

    private List<EventDAO> upComingList = new ArrayList<>();
    private List<EventDAO> pastList = new ArrayList<>();

    private List<Category> categoryList = new ArrayList<>();
    private List<SubCategory> subCategoryList = new ArrayList<>();

    private static EventRepository eventRepository;

    private EventRepository() {
    }

    public static EventRepository getInstance() {
        return eventRepository == null ? eventRepository = new EventRepository() : eventRepository;
    }

    public MutableLiveData<List<EventDAO>> getUpComingEventData(ApiInterface apiInterface, boolean hotReload) {
        if (!hotReload && upComingEventData.getValue() != null && upComingEventData.getValue().size() != 0)
            return upComingEventData;

        Call<JsonElement> call = apiInterface.getEvents();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                upComingList.clear();

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);

                        JSONArray eventArray = data.getJSONArray(API.UPCOMING_EVENTS);
                        if (eventArray.length() > 0) {
                            for (int i = 0; i < eventArray.length(); i++) {
                                String s = eventArray.getJSONObject(i).toString();
                                upComingList.add(new Gson().fromJson(s, EventDAO.class));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.code() == API.SESSION_EXPIRE)
                            sessionExpired.setValue(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                upComingEventData.setValue(upComingList);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                upComingEventData.setValue(upComingList);
            }
        });
        return upComingEventData;

    }

    public MutableLiveData<List<EventDAO>> getPastEventData(ApiInterface apiInterface, String auth, boolean hotReload) {

        if (!hotReload && pastEventData.getValue() != null) return pastEventData;
        Call<JsonElement> call = apiInterface.getEvents();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                pastList.clear();
                if(response.body() == null){
                    return;
                }
                Log.d("fnsalknfsa", "passsssttttt: "+response.body().toString());

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);

                        JSONArray eventArray2 = data.getJSONArray(API.PAST_EVENTS);
                        if (eventArray2.length() > 0) {
                            for (int i = 0; i < eventArray2.length(); i++) {
                                String s = eventArray2.getJSONObject(i).toString();
                                pastList.add(new Gson().fromJson(s, EventDAO.class));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.code() == API.SESSION_EXPIRE)
                            sessionExpired.setValue(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                pastEventData.setValue(pastList);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                pastEventData.setValue(pastList);
            }
        });
        return pastEventData;
    }

    public MutableLiveData<UserResponse> getEventDetail(ApiInterface apiInterface, int id) {
        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
        UserResponse userResponse = new UserResponse();

        Call<JsonElement> call = apiInterface.getEventDetail(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                Log.d("faskfnkslafa", "eveveveveve: "+response.body());

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);
                        EventDAO dao = new Gson().fromJson(data.toString(), EventDAO.class);
                        userResponse.setEventDAO(dao);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("faskfnkslafa", "Exception: "+e.getMessage());
                    }
                }

                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("faskfnkslafa", "onFailure: "+t.getMessage());
                userResponse.setSuccess(false);
                userResponse.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(userResponse);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getEventDates(ApiInterface apiInterface) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
        UserResponse userResponse = new UserResponse();

        Call<JsonElement> call = apiInterface.getEventDates();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONArray data = OBJ.getJSONArray(API.DATA);
                        userResponse.setMessage(data.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

                userResponse.setSuccess(false);
                userResponse.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(userResponse);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getEventsByDate(ApiInterface apiInterface, JsonObject jsonObject) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
        UserResponse userResponse = new UserResponse();
        List<EventDAO> list = new ArrayList<>();

        Call<JsonElement> call = apiInterface.getEventsByDate(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONArray data = OBJ.getJSONArray(API.DATA);
                        if (data.length() > 0) {
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                list.add(new Gson().fromJson(s, EventDAO.class));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    userResponse.setEventList(list);
                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

                userResponse.setSuccess(false);
                userResponse.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(userResponse);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getMoreEventDetail(ApiInterface apiInterface, int id) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
        UserResponse userResponse = new UserResponse();

        Call<JsonElement> call = apiInterface.getMoreEventDetail(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);

                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);
                        EventDAO dao = new Gson().fromJson(data.toString(), EventDAO.class);
                        userResponse.setEventDAO(dao);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

                userResponse.setSuccess(false);
                userResponse.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(userResponse);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<UserResponse> getEditEventDetails(ApiInterface apiInterface, int id) {

        MutableLiveData<UserResponse> mutableLiveData = new MutableLiveData<>();
        UserResponse userResponse = new UserResponse();

        Call<JsonElement> call = apiInterface.getEditEventDetails(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.isSuccessful()) {
                    userResponse.setSuccess(true);
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        JSONObject data = OBJ.getJSONObject(API.DATA);
                        UpdateEventDao dao = new Gson().fromJson(data.toString(), UpdateEventDao.class);
                        Log.d("fnaslkfnklas", dao.getVenueId()+" edit_eventsssss: "+" ---- "+dao.getVenue().getId()+" ==== "+dao.toString());

                        userResponse.setUpdateEventDao(dao);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        userResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        userResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        userResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mutableLiveData.setValue(userResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

                userResponse.setSuccess(false);
                userResponse.setMessage(t.getMessage());
                t.printStackTrace();

                mutableLiveData.setValue(userResponse);
            }
        });
        return mutableLiveData;
    }

    public MutableLiveData<CategoryResponse> getCategories(ApiInterface apiInterface) {
        Call<JsonElement> call = apiInterface.getCategories();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                CategoryResponse categoryResponse = new CategoryResponse();
                Log.e("got result", "yes");
                categoryList.clear();
                if (response.isSuccessful()) {
                    categoryResponse.setSuccess(true);
                    categoryResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (OBJ.getJSONArray(API.DATA).length() > 0) {
                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                categoryList.add(new Gson().fromJson(s, Category.class));
                            }
                        }
                        categoryResponse.setCategories(categoryList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        categoryResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        categoryResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        categoryResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                categoryResponseMutable.setValue(categoryResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                CategoryResponse response = new CategoryResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                categoryList.clear();
                categoryResponseMutable.setValue(response);
            }
        });
        return categoryResponseMutable;
    }


    public MutableLiveData<SubCategoryResponse> getSubCategories(ApiInterface apiInterface, int id) {
        subCategoryList.clear();
        Call<JsonElement> call = apiInterface.getSubCategories(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                subCategoryList.clear();
                SubCategoryResponse categoryResponse = new SubCategoryResponse();
                if (response.isSuccessful()) {
                    categoryResponse.setSuccess(true);
                    categoryResponse.setCode(response.code());
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        if (OBJ.getJSONArray(API.DATA).length() > 0) {
                            JSONArray data = OBJ.getJSONArray(API.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                String s = data.getJSONObject(i).toString();
                                subCategoryList.add(new Gson().fromJson(s, SubCategory.class));
                            }
                        }
                        categoryResponse.setCategories(subCategoryList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        categoryResponse.setCode(response.code());
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        categoryResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        categoryResponse.setMessage(OBJ.getString(API.MESSAGE));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                subCategoryResponseMutable.setValue(categoryResponse);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                SubCategoryResponse response = new SubCategoryResponse();
                response.setSuccess(false);
                response.setMessage(t.getMessage());
                t.printStackTrace();
                categoryList.clear();
                subCategoryResponseMutable.setValue(response);
            }
        });
        return subCategoryResponseMutable;
    }

    public MutableLiveData<MyResponse> deleteEvent(ApiInterface apiInterface, int id) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.deleteEvent(id);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.DATA));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponse.setCode(OBJ.getInt(API.CODE));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                } else {
                    try {
                        MyResponse myResponse = new MyResponse();
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
            }
        });
        return myResponseMutableLiveData;
    }

    public MutableLiveData<MyResponse> changeAvailabilityStatus(ApiInterface apiInterface, JsonObject jsonObject) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.changeEventAvailability(jsonObject);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponse.setCode(OBJ.getInt(API.CODE));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                } else {
                    try {
                        MyResponse myResponse = new MyResponse();
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
            }
        });
        return myResponseMutableLiveData;
    }

    public MutableLiveData<MyResponse> cancelTicket(ApiInterface apiInterface, JsonObject jsonObject) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();

        Call<JsonElement> call = apiInterface.cancelTicket(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        MyResponse myResponse = new MyResponse();
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponse.setCode(OBJ.getInt(API.CODE));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                } else {
                    try {
                        MyResponse myResponse = new MyResponse();
                        JSONObject OBJ = new JSONObject(response.errorBody().string());
                        myResponse.setMessage(OBJ.getString(API.MESSAGE));
                        myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                        myResponseMutableLiveData.setValue(myResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                t.printStackTrace();
                myResponseMutableLiveData.setValue(new MyResponse("Something went wrong!"));
            }
        });
        return myResponseMutableLiveData;
    }

    public MutableLiveData<MyResponse> updateCreatedEvent(ApiInterface apiInterface, Map<String, RequestBody> editEventAllObj, List<ImageDAO> img) {
        MutableLiveData<MyResponse> myResponseMutableLiveData = new MutableLiveData<>();
        MyResponse myResponse = new MyResponse();
        MultipartBody.Part[] image = null;
        if (img.size() > 0) {
            image = new MultipartBody.Part[img.size()];
            try {
                for (int index = 0; index < img.size(); index++) {
                    File file = new File(img.get(index).getVenueImages());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part fileToUploadPart = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                    image[index] = fileToUploadPart;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Call<JsonElement> myResponseCall = apiInterface.editEventRequest(editEventAllObj, img.size()>0 ? image : null);
        myResponseCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                try {
                    JSONObject OBJ = new JSONObject(response.body().toString());
                    myResponse.setMessage(OBJ.getString(API.MESSAGE));
                    myResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                    myResponse.setCode(OBJ.getInt(API.CODE));
                    myResponseMutableLiveData.setValue(myResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                    myResponse.setMessage("Something went wrong");
                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                myResponse.setMessage("Something went wrong");
            }
        });

        return myResponseMutableLiveData;
    }

}
