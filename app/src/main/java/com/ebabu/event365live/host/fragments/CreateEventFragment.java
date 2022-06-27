package com.ebabu.event365live.host.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.CheckedInRSVP;
import com.ebabu.event365live.host.activity.MyVenues;
import com.ebabu.event365live.host.adapter.GalleryPicAdapter;
import com.ebabu.event365live.host.adapter.VenuePicInEventAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentCreateEventBinding;
import com.ebabu.event365live.host.databinding.LayoutPostEventDialogBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.EventImages;
import com.ebabu.event365live.host.entities.FreeTicketDao;
import com.ebabu.event365live.host.entities.HostDetailResponse;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.ebabu.event365live.host.entities.RSVPTicketDao;
import com.ebabu.event365live.host.entities.SubVenue;
import com.ebabu.event365live.host.entities.SubVenueDao;
import com.ebabu.event365live.host.entities.TableAndSeatingDao;
import com.ebabu.event365live.host.entities.UpdateEventDao;
import com.ebabu.event365live.host.entities.VenueDAO;
import com.ebabu.event365live.host.fragments.ticket.RegularTicketFragment;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.FileUtils;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.PermissionUtils;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.CreateEventViewModel;
import com.ebabu.event365live.host.viewmodels.VenueViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEventFragment extends Fragment {
    private static final int RC_HANDLE_CAMERA_PERM = 101;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 606;
    private static final int PLACE_PICKER_REQUEST_CODE = 607;
    private static String eventOccuranceType;
    final int[] oldPos = new int[1];
    final int[] newPos = new int[1];
    @Inject
    RequestManager requestManager;
    MyLoader loader;
    List<String> deleteVenueImgList;
    UpdateEventDao updateEventDao;
    int day;
    int month;
    int year;
    int value;
    String value1;
    String currentPhotoPath, venueName, startDate, endDate, startTime, endTime, startSellingDate, endSellingDate, startSellingTime, endSellingTime, sortDescription, startTimecl, endTimecl;
    StringBuilder deletedPreviousImagesIds;
    TextView start_time_tv_week, end_time_tv2_week,
             start_time_tv_mon, end_time_tv2_mon,
             start_time_tv_tues, end_time_tv2_tues,
             start_time_tv_wednes, end_time_tv2_wednes,
             start_time_tv_thurs, end_time_tv2_thurs,
             start_time_tv_fri, end_time_tv2_fri,
             start_time_tv_satur, end_time_tv2_satur,
             start_time_tv_sun, end_time_tv2_sun;
    int t1Hour, t1Minute;
    @Inject
    ApiInterface apiInterface;
    TextView textView;
    boolean[] selectweekDay;
    ArrayList<Integer> dayList = new ArrayList<>();
    String[] weekdayArray = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private ArrayList<String> imageList = new ArrayList<>();
    private Activity activity;
    private LatLng latLongt;
    private List<ImageDAO> imageDaoList;
    private FragmentCreateEventBinding binding;
    private CreateEventViewModel viewModel;
    private VenueViewModel venueViewModel;
    private boolean mLocationPermissionGranted;
    private CreateEventDAO createEventDAO;
    private StringBuilder selection;
    private String eventType = "";
    private VenueDAO selectedVenue;
    private VenuePicInEventAdapter venuePicInEventAdapter;
    private GalleryPicAdapter galleryPicAdapter;
    private String startEventDateFromApi, selectedStartEventDate = "", getTodayDate = "";
    private String stcl, etcl;
    private Context mContext;
    private PermissionUtils permissionUtils;
    private CompositeDisposable disposable;
    private boolean isVenuer, update, isEventDetailValid, isAdditionInfoValid;
    private AlertDialog alertDialog;
    private LayoutPostEventDialogBinding eventDialogBinding;
    private List<ImageDAO> previousImages;
    private List<SubVenueDao> updateSelectedSubVenues = new ArrayList<>();
    // ImageView Drag & Drop
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            oldPos[0] = viewHolder.getAdapterPosition();
            newPos[0] = target.getAdapterPosition();
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            moveItem(oldPos[0], newPos[0]);
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        update = getArguments().getBoolean("update");
        loader = new MyLoader(getContext());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("FragmentLiveDataObserve")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_event, container, false);
        viewModel = ViewModelProviders.of(this).get(CreateEventViewModel.class);
        selectweekDay = new boolean[weekdayArray.length];
        binding.typeSubSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you Sure?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int j = 0; j < dayList.size(); j++)
                                {
                                    stringBuilder.append(weekdayArray[dayList.get(j)]);
                                    if (j != dayList.size() - 1)
                                    {
                                        stringBuilder.append(", ");
                                    }
                                }
                                binding.typeSubSpinner.setText(stringBuilder.toString()+",");
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                for (int j = 0; j < weekdayArray.length; j++) {
                                    selectweekDay[j] = false;
                                    dayList.clear();
                                    textView.setText("");
                                }
                            }
                        });
                builder.show();
            }
        });
        start_time_tv_week = binding.startTimeTvWeek.findViewById(R.id.start_time_tv_week);
        end_time_tv2_week = binding.endTimeTv2Week.findViewById(R.id.end_time_tv2_week);

        start_time_tv_mon = binding.startTimeTvMon.findViewById(R.id.start_time_tv_mon);
        end_time_tv2_mon = binding.endTimeTv2Mon.findViewById(R.id.end_time_tv2_mon);

        start_time_tv_tues = binding.startTimeTvTues.findViewById(R.id.start_time_tv_tues);
        end_time_tv2_tues = binding.endTimeTv2Tues.findViewById(R.id.end_time_tv2_tues);

        start_time_tv_wednes = binding.startTimeTvWednes.findViewById(R.id.start_time_tv_wednes);
        end_time_tv2_wednes = binding.endTimeTv2Wednes.findViewById(R.id.end_time_tv2_wednes);

        start_time_tv_thurs = binding.startTimeTvThurs.findViewById(R.id.start_time_tv_thurs);
        end_time_tv2_thurs = binding.endTimeTv2Thurs.findViewById(R.id.end_time_tv2_thurs);

        start_time_tv_fri = binding.startTimeTvFri.findViewById(R.id.start_time_tv_fri);                         //Start Friday
        end_time_tv2_fri = binding.endTimeTv2Fri.findViewById(R.id.end_time_tv2_fri);                            //End Friday

        start_time_tv_satur = binding.startTimeTvSatur.findViewById(R.id.start_time_tv_satur);                   //Start Saturday
        end_time_tv2_satur = binding.endTimeTv2Satur.findViewById(R.id.end_time_tv2_satur);                      //End Saturday

        start_time_tv_sun = binding.startTimeTvSun.findViewById(R.id.start_time_tv_sun);                         //Start Sunday
        end_time_tv2_sun = binding.endTimeTv2Sun.findViewById(R.id.end_time_tv2_sun);                            //End Sunday

        start_time_tv_week.setOnClickListener(new View.OnClickListener() {                                                                                                       //Start week
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        // Clock
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        String zone = t1Hour >= 12 ? "pm" : "am";
                        int hour = t1Hour > 12 ? t1Hour - 12 : t1Hour;
                        String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((t1Minute < 10) ? "0" + t1Minute : t1Minute) + " " + zone;
                        start_time_tv_week.setText(time);
                        stcl = String.valueOf(time);
                        SharedPreferences stcl1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stcl1.edit();
                        editor.putString("monthst", stcl);
                        editor.apply();
                        createEventDAO.setStartTime(time);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_week.setText(DateFormat.format("hh:mm aa", calendar));
                        String zone = t1Hour >= 12 ? "pm" : "am";
                        int hour = t1Hour > 12 ? t1Hour - 12 : t1Hour;
                        String time = ((hour < 10) ? "0" + hour : hour) + ":" + ((t1Minute < 10) ? "0" + t1Minute : t1Minute) + " " + zone;
                        end_time_tv2_week.setText(time);
                        etcl = String.valueOf(time);
                        SharedPreferences etcl1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etcl1.edit();
                        editor.putString("monthet", etcl);
                        editor.apply();
                        createEventDAO.setEndTime(time);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                               //End Week
        start_time_tv_mon.setOnClickListener(new View.OnClickListener() {                                                                                                 // Start Monday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_mon.setText(DateFormat.format("hh:mm aa", calendar));
                        String stmon = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences stmon1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stmon1.edit();
                        editor.putString("mondayst", stmon);
                        editor.apply();
                        createEventDAO.setStartTime(stmon);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_mon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_mon.setText(DateFormat.format("hh:mm aa", calendar));
                        String etmon = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etmon1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etmon1.edit();
                        editor.putString("mondayet", etmon);
                        editor.apply();
                        createEventDAO.setEndTime(etmon);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                            // End Monday
        start_time_tv_tues.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Tuesday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_tues.setText(DateFormat.format("hh:mm aa", calendar));
                        String sttues = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences sttues1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sttues1.edit();
                        editor.putString("tuesdayst", sttues);
                        editor.apply();
                        createEventDAO.setStartTime(sttues);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_tues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_tues.setText(DateFormat.format("hh:mm aa", calendar));
                        String ettues = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences ettues1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = ettues1.edit();
                        editor.putString("tuesdayet", ettues);
                        editor.apply();
                        createEventDAO.setEndTime(ettues);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                             //End Tuesday
        start_time_tv_wednes.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Wednesday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_wednes.setText(DateFormat.format("hh:mm aa", calendar));
                        String stwednes = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences stwednes1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stwednes1.edit();
                        editor.putString("wednesdayst", stwednes);
                        editor.apply();
                        createEventDAO.setStartTime(stwednes);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_wednes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_wednes.setText(DateFormat.format("hh:mm aa", calendar));
                        String etwednes = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etwednes1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etwednes1.edit();
                        editor.putString("wednesdayet", etwednes);
                        editor.apply();
                        createEventDAO.setEndTime(etwednes);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                             //End Wednesday
        start_time_tv_thurs.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Thursday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_thurs.setText(DateFormat.format("hh:mm aa", calendar));
                        String stthurs = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences stthurs1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stthurs1.edit();
                        editor.putString("thursdayst", stthurs);
                        editor.apply();
                        createEventDAO.setStartTime(stthurs);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_thurs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_thurs.setText(DateFormat.format("hh:mm aa", calendar));
                        String etthurs = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etthurs1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etthurs1.edit();
                        editor.putString("thursdayet", etthurs);
                        editor.apply();
                        createEventDAO.setEndTime(etthurs);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                             //End Thurssday
        start_time_tv_fri.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Friday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_fri.setText(DateFormat.format("hh:mm aa", calendar));
                        String stfri = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences stfri1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stfri1.edit();
                        editor.putString("fridayst", stfri);
                        editor.apply();
                        createEventDAO.setStartTime(stfri);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_fri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_fri.setText(DateFormat.format("hh:mm aa", calendar));
                        String etfri = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etfri1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etfri1.edit();
                        editor.putString("fridayet", etfri);
                        editor.apply();
                        createEventDAO.setEndTime(etfri);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                             //End Friday
        start_time_tv_satur.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Saturday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_satur.setText(DateFormat.format("hh:mm aa", calendar));
                        String stsatur = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences stsatur1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stsatur1.edit();
                        editor.putString("saturdayst", stsatur);
                        editor.apply();
                        createEventDAO.setStartTime(stsatur);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_satur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_satur.setText(DateFormat.format("hh:mm aa", calendar));
                        String etsatur = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etsatur1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etsatur1.edit();
                        editor.putString("saturdayet", etsatur);
                        editor.apply();
                        createEventDAO.setEndTime(etsatur);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });                                                                                             //End Saturday
        start_time_tv_sun.setOnClickListener(new View.OnClickListener() {                                                                                              // Start Sunday
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        start_time_tv_sun.setText(DateFormat.format("hh:mm aa", calendar));
                        String stsun = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        Toast.makeText(getActivity(), "" + stsun, Toast.LENGTH_SHORT).show();
                        SharedPreferences stsun1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = stsun1.edit();
                        editor.putString("sundayst", stsun);
                        editor.apply();
                        createEventDAO.setStartTime(stsun);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });
        end_time_tv2_sun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        t1Hour = i;
                        t1Minute = i1;
                        // Calender
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0, 0, 0, t1Hour, t1Minute);
                        end_time_tv2_sun.setText(DateFormat.format("hh:mm aa", calendar));
                        String etsun = String.valueOf(DateFormat.format("hh:mm aa", calendar));
                        SharedPreferences etsun1 = activity.getSharedPreferences("demo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = etsun1.edit();
                        editor.putString("sundayet", etsun);
                        editor.apply();
                        createEventDAO.setEndTime(etsun);
                    }
                }, 12, 0, false
                );
                timePickerDialog.updateTime(t1Hour, t1Minute);
                timePickerDialog.show();
            }
        });

        App.getAppComponent().inject(this);
        mContext = container.getContext();
        disposable = new CompositeDisposable();
        binding.setViewmodel(viewModel);
        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());
        previousImages = new ArrayList<>();
        selection = new StringBuilder();
        setGalleryAdapter();
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        getTodayDate = df.format(today);
        if (update) {
            loader.show("");
            deleteVenueImgList = new ArrayList<>();
            deletedPreviousImagesIds = new StringBuilder();
            binding.textView20.setText(R.string.edit_event);
            binding.sellSwitch.setVisibility(View.INVISIBLE);
            binding.deteteEvent.setVisibility(View.VISIBLE);

            viewModel.getEditEventDetails(getArguments().getInt("id")).observe(getViewLifecycleOwner(),
                    userResponse -> {
                        loader.dismiss();
                        if (userResponse.getCode() == API.SESSION_EXPIRE) {
                            userResponse.setCode(0);
                            Utility.sessionExpired(getContext());
                        } else if (!userResponse.isSuccess()) {
                            loader.dismiss();
                            Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        } else {
                            updateEventDao = userResponse.getUpdateEventDao();
                            setViewData();
                            fetchVenueImages();
                        }
                    }
            );
        } else {
            binding.sellSwitch.setVisibility(View.VISIBLE);
            createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();
            binding.textView20.setText("Create an Event");
            if (createEventDAO.getOccorredOnDays() != null) {
                List<String> days = Arrays.asList(createEventDAO.getOccorredOnDays().split(", "));
                ChipGroup cg;
                int type;

                if (createEventDAO.getEventOccuranceType().equalsIgnoreCase("monthly")) {
                    cg = binding.monthChipGroup;
                    type = 2;
                } else {
                    cg = binding.daysChipGroup;
                    type = 1;
                }

                for (int i = 0; i < cg.getChildCount(); i++) {
                    Chip chip = (Chip) cg.getChildAt(i);

                    if (days.contains(chip.getText().toString()))
                    {
                        chip.setChecked(true);
                        if (type == 1)
                            viewModel.selectDays(chip);
                        else viewModel.selectMonths(chip);
                    }
                }
            }
            if (createEventDAO.getStartDate() != null)
            {
                binding.endDateTv.setText(createEventDAO.getEndDate());
                binding.endTimeTv.setText(createEventDAO.getEndTime());
                binding.startTimeTv.setText(createEventDAO.getStartTime());
                binding.textView39.setText(createEventDAO.getStartDate());
            }
            try
            {
                binding.eventNameEt.setText(createEventDAO.getName());
                binding.venueNameEt.setText(createEventDAO.getVenueName());
                binding.venueLocEt.setText(createEventDAO.getVenueName());

                if (!TextUtils.isEmpty(createEventDAO.getDesc())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        binding.additionEt.setText(Html.fromHtml(createEventDAO.getDesc(), Html.FROM_HTML_MODE_COMPACT));
                    else
                        binding.additionEt.setText(Html.fromHtml(createEventDAO.getDesc()));
                }
                binding.additionEt2.setText(createEventDAO.getDesc2());
                latLongt = new LatLng(Double.parseDouble(createEventDAO.getLat()), Double.parseDouble(createEventDAO.getLongt()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        binding.textView23.setVisibility(View.GONE);
        binding.contactChipGroup.setVisibility(View.GONE);
        binding.view19.setVisibility(View.GONE);
        eventType = "Free";
//code we require
        disposable.add(
                RxTextView.textChanges(binding.additionEt)
                        .subscribe(charSequence -> {
                            if (charSequence.length() >= 50) {
                                if (!isEventDetailValid) {
                                    isEventDetailValid = true;
                                    binding.additionEtImg.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (isEventDetailValid) {
                                    isEventDetailValid = false;
                                    binding.additionEtImg.setVisibility(View.INVISIBLE);
                                }
                            }
                        }));

//code we require
        disposable.add(
                RxTextView.textChanges(binding.additionEt2)
                        .subscribe(charSequence -> {
                            if (charSequence.length() >= 1) {
                                if (!isAdditionInfoValid) {
                                    isAdditionInfoValid = true;
                                    binding.additionEt2Img.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (isAdditionInfoValid) {
                                    isAdditionInfoValid = false;
                                    binding.additionEt2Img.setVisibility(View.INVISIBLE);
                                }
                            }
                        }));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.event_array, R.layout.my_spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        binding.typeSpinner.setAdapter(adapter);
        if (update)
        {
            binding.typeSpinner.setEnabled(true);
        }
        else
            binding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String evnttype = adapterView.getItemAtPosition(i).toString();
                    SharedPreferences shrd = activity.getSharedPreferences("demo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shrd.edit();
                    editor.putString("evnttype", evnttype);
                    editor.apply();
                    viewModel.reset(l);
                    if (l == 0)
                    {
                        eventOccuranceType = "oneTime";
                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.commonlayout.setVisibility(View.GONE);
                        binding.sunday.setVisibility(View.GONE);
                        binding.monday.setVisibility(View.GONE);
                        binding.tuesday.setVisibility(View.GONE);
                        binding.wednesday.setVisibility(View.GONE);
                        binding.thursday.setVisibility(View.GONE);
                        binding.friday.setVisibility(View.GONE);
                        binding.saturday.setVisibility(View.GONE);
                        binding.txtviewsun.setVisibility(View.GONE);
                        binding.txtviewmon.setVisibility(View.GONE);
                        binding.txtviewtues.setVisibility(View.GONE);
                        binding.txtviewwednes.setVisibility(View.GONE);
                        binding.txtviewthurs.setVisibility(View.GONE);
                        binding.txtviewfri.setVisibility(View.GONE);
                        binding.txtviewsatur.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                    }
                    else if (l == 2 || l == 3 || l == 1)
                    {
                        binding.typeSubLayout.setVisibility(View.VISIBLE);
                        if (l == 3)
                        {
                            if (binding.monthChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 31; index++)
                                {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(String.valueOf(index + 1));
                                    chip.setOnCheckedChangeListener((compoundButton, b) ->
                                    {
                                        viewModel.selectMonths(chip);
                                    });
                                    binding.monthChipGroup.addView(chip);
                                }
                            }
                            eventOccuranceType = "monthly";
                            binding.monthChipGroup.setSingleSelection(true);
                            binding.commonlayout.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.txtviewsun.setVisibility(View.GONE);
                            binding.txtviewmon.setVisibility(View.GONE);
                            binding.txtviewtues.setVisibility(View.GONE);
                            binding.txtviewwednes.setVisibility(View.GONE);
                            binding.txtviewthurs.setVisibility(View.GONE);
                            binding.txtviewfri.setVisibility(View.GONE);
                            binding.txtviewsatur.setVisibility(View.GONE);
                            binding.monthScrollView.setVisibility(View.VISIBLE);
                            binding.daysDaysScrollView.setVisibility(View.GONE);
                        }
                        else
                        {
                            if (l == 1)
                            {
                                eventOccuranceType = "daily";
                                binding.daysChipGroup.setSingleSelection(false);
                            }
                            else
                            {
                                binding.daysChipGroup.setSingleSelection(true);
                                eventOccuranceType = "weekly";
                            }
                            binding.daysDaysScrollView.setVisibility(View.VISIBLE);
                            binding.monthScrollView.setVisibility(View.GONE);
                            if (binding.daysChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 7; index++)
                                {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(Constants.days[index]);
                                    chip.setOnCheckedChangeListener((compoundButton, b) ->
                                    {
                                        binding.linearLayout.setVisibility(View.GONE);
                                        if (chip.isChecked())
                                        {
                                            viewModel.selectDays(chip);
                                        }
                                        if (!chip.isChecked())
                                        {
                                            binding.typeSubSpinner.setText(getString(R.string.select_days));
                                            chip.clearFocus();
                                        }
                                        if (chip.getText().toString() == "Sunday")
                                        {
                                            if (b)
                                            {
                                                binding.sunday.setVisibility(View.VISIBLE);
                                                binding.txtviewsun.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.sunday.setVisibility(View.GONE);
                                                binding.txtviewsun.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Monday")
                                        {
                                            if (b)
                                            {
                                                binding.monday.setVisibility(View.VISIBLE);
                                                binding.txtviewmon.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.monday.setVisibility(View.GONE);
                                                binding.txtviewmon.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Tuesday")
                                        {
                                            if (b)
                                            {
                                                binding.tuesday.setVisibility(View.VISIBLE);
                                                binding.txtviewtues.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.tuesday.setVisibility(View.GONE);
                                                binding.txtviewtues.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Wednesday")
                                        {
                                            if (b)
                                            {
                                                binding.wednesday.setVisibility(View.VISIBLE);
                                                binding.txtviewwednes.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.wednesday.setVisibility(View.GONE);
                                                binding.txtviewwednes.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Thursday")
                                        {
                                            if (b)
                                            {
                                                binding.thursday.setVisibility(View.VISIBLE);
                                                binding.txtviewthurs.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.thursday.setVisibility(View.GONE);
                                                binding.txtviewthurs.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Friday")
                                        {
                                            if (b)
                                            {
                                                binding.friday.setVisibility(View.VISIBLE);
                                                binding.txtviewfri.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.friday.setVisibility(View.GONE);
                                                binding.txtviewfri.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Saturday")
                                        {
                                            if (b)
                                            {
                                                binding.saturday.setVisibility(View.VISIBLE);
                                                binding.txtviewsatur.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.saturday.setVisibility(View.GONE);
                                                binding.txtviewsatur.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                    binding.daysChipGroup.addView(chip);
                                }
                            }
                        }
                    }
                    createEventDAO.setEventOccuranceType(eventOccuranceType);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        if (createEventDAO != null) {
            if (createEventDAO.getImageDAOList() != null)
                imageDaoList = createEventDAO.getImageDAOList();
            else
                imageDaoList = new ArrayList<>();
            galleryPicAdapter.refresh(imageDaoList);
        }
        isVenuer = Utility.getSharedPreferencesBoolean(mContext, API.IS_VENUE_OWNER);
//        if (isVenuer) {
        venueViewModel = ViewModelProviders.of(this).get(VenueViewModel.class);
        venuePicInEventAdapter = new VenuePicInEventAdapter(false);
        binding.venueRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        binding.venueRv.setHasFixedSize(true);
        binding.venueRv.setAdapter(venuePicInEventAdapter);
        binding.locVenueTv.setText("Select a Venue");
        binding.venueLocEt.setHint("Select a Venue");
        binding.imageView23.setVisibility(View.INVISIBLE);
        binding.venueNameLayout.setVisibility(View.GONE);


        if (createEventDAO != null) {
            selectedVenue = new VenueDAO();
            selectedVenue.setId(createEventDAO.getVenueId());
            if (createEventDAO.getVenueImageList() != null && createEventDAO.getVenueImageList().size() > 0)
                venuePicInEventAdapter.refresh(createEventDAO.getVenueImageList());
        }
        fetchVenueImages();
        getLocationPermission();
        Places.initialize(mContext, getString(R.string.google_maps_key));
        viewModel.getDaysSelected().observe(this, new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                binding.typeSubSpinner.setTextColor(ContextCompat.getColor(getActivity(), R.color.grayLight));
                if (eventOccuranceType.equalsIgnoreCase("weekly"))
                    binding.linearLayout.setVisibility(View.GONE);
                binding.parentLayout.setVisibility(View.GONE);
                createEventDAO.setOccorredOnDays(s);
                if (s.length() > 0)
                {
                    binding.typeSubSpinner.setText(s);
                    binding.typeSubSpinner.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));
                }
                else if (binding.typeSpinner.getSelectedItemPosition() == 1)
                {
                    binding.typeSubSpinner.setText("Select Days");
                }
                else if (binding.typeSpinner.getSelectedItemPosition() == 2)
                {
                    binding.typeSubSpinner.setText(getString(R.string.select_days));
                }
                else
                {
                    binding.typeSubSpinner.setText(getString(R.string.select_months));
                }
            }
        });
        binding.parentLayout.setOnTouchListener((view, motionEvent) ->
        {
            binding.parentLayout.setVisibility(View.GONE);
            return true;
        });
        binding.deteteEvent.setOnClickListener(v ->
        {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.txt_delete_event_msg))
                    .setPositiveButton("Delete", (dialogInterface, i) ->
                    {
                        if (Utility.isNetworkAvailable(getContext()))
                            deleteAPI();
                        else
                            Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        binding.typeSubSpinner.setOnClickListener(v ->
        {
            if (eventOccuranceType.contains("monthly"))
            {
                if (!binding.typeSubSpinner.getText().toString().equals("Select Dates"))
                {
                    SharedPreferences sharedPreferences= activity.getSharedPreferences("demo",MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    String currentdate=getTodayDate;
                    String[] sarr = currentdate.split("/");
                    int date= Integer.parseInt(sarr[1]);
                    day = Integer.parseInt(binding.typeSubSpinner.getText().toString());
                    month = Calendar.getInstance().get(Calendar.MONTH);
                    year = Calendar.getInstance().get(Calendar.YEAR);
                    if (day > date)
                    {
                        month = month + 1;
                    }
                    if (day <= date)
                    {
                        month = month + 2;
                    }
                    startDate = year + "-" + month + "-" + day;
                    editor.putString("monthlydate",startDate);
                    editor.apply();
                    createEventDAO.setStartDate(startDate);
                    createEventDAO.setEndDate(startDate);
                }
            }
            if (update)
            {
                binding.parentLayout.setVisibility(View.VISIBLE);
                return;
            }
            binding.parentLayout.setVisibility(View.VISIBLE);
        });
        binding.venueLocEt.setOnClickListener(v ->
        {
            if (!Utility.isNetworkAvailable(mContext))
                Dialogs.toast(mContext, v, getString(R.string.check_network));
            else {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                if (update) {
                    startActivityForResult(new Intent(getActivity(), MyVenues.class).putExtra("from", "create_venue").putExtra("eventId", updateEventDao.getId()), 101);
                } else {
                    startActivityForResult(new Intent(getActivity(), MyVenues.class).putExtra("from", "create_venue"), 101);
                }

            }
        });
        binding.imageView23.setOnClickListener(v -> searchForPlace());
        binding.addImage.setOnClickListener(v ->
        {
            checkForPermission();
        });

        binding.endDateImg.setOnClickListener(v ->
        {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(mContext, "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setDate(mContext, binding.textView39.getText().toString(), binding.endDateTv, date ->
                {
                    binding.endDateTv.setText(date);
                    binding.endTimeTv.setText(getString(R.string.end_time));
                    clearVenue();
                });
        });
        binding.endDateTv.setOnClickListener(v ->
        {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(mContext, "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setDate(mContext, binding.textView39.getText().toString(), binding.endDateTv, date -> {
                    binding.endDateTv.setText(date);
                    binding.endTimeTv.setText(getString(R.string.end_time));
                    clearVenue();
                });
        });

        binding.imageView22.setOnClickListener(v -> Dialogs.setDate(mContext, "", binding.textView39, date ->
        {
            binding.textView39.setText(date);
            binding.endDateTv.setText(getString(R.string.end_date));
            binding.endTimeTv.setText(getString(R.string.end_time));
            clearVenue();
        }));
        binding.textView39.setOnClickListener(v -> Dialogs.setDate(mContext, "", binding.textView39, date -> {
            binding.textView39.setText(date);
            binding.endDateTv.setText(getString(R.string.end_date));
            binding.endTimeTv.setText(getString(R.string.end_time));
            clearVenue();
        }));

        binding.startTimeImg.setOnClickListener(v -> Dialogs.setTime(mContext, binding.startTimeTv, time -> {
            binding.startTimeTv.setText(time);
            binding.endTimeTv.setText(getString(R.string.end_time));
            startTime=time;
            clearVenue();
        }));
        binding.startTimeTv.setOnClickListener(v -> Dialogs.setTime(mContext, binding.startTimeTv, time -> {
            binding.startTimeTv.setText(time);
            binding.endTimeTv.setText(getString(R.string.end_time));
            clearVenue();
        }));

        binding.endTimeImg.setOnClickListener(this::setEndTime);
        binding.endTimeTv.setOnClickListener(this::setEndTime);

        binding.contactChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = binding.getRoot().findViewById(checkedId);

            if (chip != null && createEventDAO.getPaidType() != null && !createEventDAO.getPaidType().equalsIgnoreCase(chip.getText().toString())) {
                if (createEventDAO.getFreeTicketDaoList() != null && createEventDAO.getFreeTicketDaoList().size() > 0)
                    createEventDAO.getFreeTicketDaoList().clear();

                if (createEventDAO.getRsvpTicketDaoList() != null && createEventDAO.getRsvpTicketDaoList().size() > 0)
                    createEventDAO.getRsvpTicketDaoList().clear();

                if (createEventDAO.getVipTicketDaoList() != null && createEventDAO.getVipTicketDaoList().size() > 0)
                    createEventDAO.getVipTicketDaoList().clear();

                if (createEventDAO.getRsvpTableAndSeatingDaos() != null && createEventDAO.getRsvpTableAndSeatingDaos().size() > 0)
                    createEventDAO.getRsvpTableAndSeatingDaos().clear();

                if (createEventDAO.getVipTableAndSeatingDaos() != null && createEventDAO.getVipTableAndSeatingDaos().size() > 0)
                    createEventDAO.getVipTableAndSeatingDaos().clear();
            }
            eventType = (chip != null) ? chip.getText().toString() : "";
        });
        binding.createTicket.setOnClickListener(V ->
        {
            String msg = binding.typeSubSpinner.getText().toString();
            SharedPreferences shrd = activity.getSharedPreferences("demo", MODE_PRIVATE);
            SharedPreferences.Editor editor = shrd.edit();
            editor.putString("str", msg);
            editor.apply();
            if (validateAndSetData(V)) {
                Navigation.findNavController(V).navigate(
                        CreateEventFragmentDirections.actionCreateEventFragmentToCreateTicketFragment().setEventDAO(createEventDAO));
            }
        });
        binding.postEvent.setOnClickListener(v ->
        {
            if (validateAndSetData(v)) {
                confirmPaidFreeDialog(v);
            }
        });

        return binding.getRoot();
    }

    private void setViewData() {
        binding.sellTimeCv.setVisibility(View.GONE);
        binding.textView23.setVisibility(View.GONE);
        binding.contactChipGroup.setVisibility(View.GONE);
        binding.view19.setVisibility(View.GONE);
        binding.updateBtn.setVisibility(View.VISIBLE);
        binding.buttonLayout.setVisibility(View.GONE);

        imageDaoList = new ArrayList<>();
        String eventName = updateEventDao.getName();
        binding.eventNameEt.setText(eventName);
        String occuranceType = updateEventDao.getEventOccurrenceType();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]
                {
                        occuranceType
                });
        binding.typeSpinner.setAdapter(dataAdapter);
        if (update)
        {
            binding.typeSubLayout.setVisibility(View.VISIBLE);
            binding.typeSpinner.setEnabled(true);
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add("OneTime");
            arrayList.add("Daily");
            arrayList.add("Weekly");
            arrayList.add("Monthly");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.typeSpinner.setAdapter(arrayAdapter);
            binding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @SuppressLint("ResourceType")
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    if (l == 0)
                    {
                        eventOccuranceType = "oneTime";
                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.commonlayout.setVisibility(View.GONE);
                        binding.sunday.setVisibility(View.GONE);
                        binding.monday.setVisibility(View.GONE);
                        binding.tuesday.setVisibility(View.GONE);
                        binding.wednesday.setVisibility(View.GONE);
                        binding.thursday.setVisibility(View.GONE);
                        binding.friday.setVisibility(View.GONE);
                        binding.saturday.setVisibility(View.GONE);
                        binding.txtviewsun.setVisibility(View.GONE);
                        binding.txtviewmon.setVisibility(View.GONE);
                        binding.txtviewtues.setVisibility(View.GONE);
                        binding.txtviewwednes.setVisibility(View.GONE);
                        binding.txtviewthurs.setVisibility(View.GONE);
                        binding.txtviewfri.setVisibility(View.GONE);
                        binding.txtviewsatur.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                    }
                    else if (l == 2 || l == 3 || l == 1)
                    {
                        binding.typeSubLayout.setVisibility(View.VISIBLE);
                        if (l == 3)
                        {
                            if (binding.monthChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 31; index++)
                                {
                                    Chip chip1 = new Chip(mContext);
                                    chip1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip1.setCheckable(true);
                                    chip1.setCheckedIconVisible(true);
                                    chip1.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip1.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip1.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip1.setText(String.valueOf(index + 1));
                                    chip1.setOnCheckedChangeListener((compoundButton, b) ->
                                    {
                                        if (chip1.isChecked())
                                        {
                                            binding.typeSubSpinner.setText(chip1.getText().toString());
                                        }
                                        if (!chip1.isChecked())
                                        {
                                            binding.typeSubSpinner.setText(getString(R.string.select_days));
                                        }
                                    });
                                    binding.monthChipGroup.addView(chip1);
                                }
                            }
                            eventOccuranceType = "monthly";
                            binding.monthChipGroup.setSingleSelection(true);
                            binding.commonlayout.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.txtviewsun.setVisibility(View.GONE);
                            binding.txtviewmon.setVisibility(View.GONE);
                            binding.txtviewtues.setVisibility(View.GONE);
                            binding.txtviewwednes.setVisibility(View.GONE);
                            binding.txtviewthurs.setVisibility(View.GONE);
                            binding.txtviewfri.setVisibility(View.GONE);
                            binding.txtviewsatur.setVisibility(View.GONE);
                            binding.monthScrollView.setVisibility(View.VISIBLE);
                            binding.daysDaysScrollView.setVisibility(View.GONE);
                        }
                        else
                        {
                            if (l == 1)
                            {
                                eventOccuranceType = "daily";
                                binding.daysChipGroup.setSingleSelection(false);
                            }
                            else
                            {
                                binding.daysChipGroup.setSingleSelection(true);
                                eventOccuranceType = "weekly";
                            }
                            binding.daysDaysScrollView.setVisibility(View.VISIBLE);
                            binding.monthScrollView.setVisibility(View.GONE);
                            if (binding.daysChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 7; index++)
                                {
                                    Chip chip2 = new Chip(mContext);
                                    chip2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip2.setCheckable(true);
                                    chip2.setCheckedIconVisible(true);
                                    chip2.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip2.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip2.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip2.setText(Constants.days[index]);
                                    chip2.setOnCheckedChangeListener((compoundButton, b) ->
                                    {
                                        binding.linearLayout.setVisibility(View.GONE);
                                        if (chip2.isChecked())
                                        {
                                            String val= chip2.getText().toString();
                                            binding.typeSubSpinner.setText(val+", ");
                                        }
                                        if (!chip2.isChecked())
                                        {
                                            binding.typeSubSpinner.setText(getString(R.string.select_days));
                                        }
                                        if (chip2.getText().toString() == "Sunday")
                                        {
                                            if (b)
                                            {
                                                binding.sunday.setVisibility(View.VISIBLE);
                                                binding.txtviewsun.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.sunday.setVisibility(View.GONE);
                                                binding.txtviewsun.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Monday")
                                        {
                                            if (b)
                                            {
                                                binding.monday.setVisibility(View.VISIBLE);
                                                binding.txtviewmon.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.monday.setVisibility(View.GONE);
                                                binding.txtviewmon.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Tuesday")
                                        {
                                            if (b)
                                            {
                                                binding.tuesday.setVisibility(View.VISIBLE);
                                                binding.txtviewtues.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.tuesday.setVisibility(View.GONE);
                                                binding.txtviewtues.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Wednesday")
                                        {
                                            if (b)
                                            {
                                                binding.wednesday.setVisibility(View.VISIBLE);
                                                binding.txtviewwednes.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.wednesday.setVisibility(View.GONE);
                                                binding.txtviewwednes.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Thursday")
                                        {
                                            if (b)
                                            {
                                                binding.thursday.setVisibility(View.VISIBLE);
                                                binding.txtviewthurs.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.thursday.setVisibility(View.GONE);
                                                binding.txtviewthurs.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Friday")
                                        {
                                            if (b)
                                            {
                                                binding.friday.setVisibility(View.VISIBLE);
                                                binding.txtviewfri.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.friday.setVisibility(View.GONE);
                                                binding.txtviewfri.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip2.getText().toString() == "Saturday")
                                        {
                                            if (b)
                                            {
                                                binding.saturday.setVisibility(View.VISIBLE);
                                                binding.txtviewsatur.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                binding.saturday.setVisibility(View.GONE);
                                                binding.txtviewsatur.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                    binding.daysChipGroup.addView(chip2);
                                }
                            }
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView)
                {

                }
            });
        }
        else
        {
            binding.typeSubLayout.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder();
            if (occuranceType.equalsIgnoreCase("weekly"))
            {
                binding.commonlayout.setVisibility(View.VISIBLE);
                if (updateEventDao.getEventOccurrence().get(0).getOccurredOn() == 0)
                    builder.append(Constants.days[updateEventDao.getEventOccurrence().get(0).getOccurredOn()]);
                else
                    builder.append(Constants.days[updateEventDao.getEventOccurrence().get(0).getOccurredOn() - 1]);
            }
            else if (occuranceType.equalsIgnoreCase("daily"))
            {
                if (updateEventDao.getEventOccurrence().size() == 1)
                {
                    long l = 0;
                    viewModel.reset(l);
                    if (l == 0)
                    {
                        eventOccuranceType = "oneTime";
                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.commonlayout.setVisibility(View.GONE);
                        binding.sunday.setVisibility(View.GONE);
                        binding.monday.setVisibility(View.GONE);
                        binding.tuesday.setVisibility(View.GONE);
                        binding.wednesday.setVisibility(View.GONE);
                        binding.thursday.setVisibility(View.GONE);
                        binding.friday.setVisibility(View.GONE);
                        binding.saturday.setVisibility(View.GONE);
                        binding.txtviewsun.setVisibility(View.GONE);
                        binding.txtviewmon.setVisibility(View.GONE);
                        binding.txtviewtues.setVisibility(View.GONE);
                        binding.txtviewwednes.setVisibility(View.GONE);
                        binding.txtviewthurs.setVisibility(View.GONE);
                        binding.txtviewfri.setVisibility(View.GONE);
                        binding.txtviewsatur.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                    }
                    else if (l == 2 || l == 3 || l == 1)
                    {
                        binding.typeSubLayout.setVisibility(View.VISIBLE);
                        if (l == 3)
                        {
                            if (binding.monthChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 31; index++)
                                {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(String.valueOf(index + 1));
                                    chip.setOnCheckedChangeListener((compoundButton, b) ->
                                    {
                                        viewModel.selectMonths(chip);
                                        if (!chip.isChecked())
                                        {
                                            binding.typeSubSpinner.setText(getString(R.string.select_days));
                                        }
                                    });
                                    binding.monthChipGroup.addView(chip);
                                }
                            }
                            eventOccuranceType = "monthly";
                            binding.monthChipGroup.setSingleSelection(true);
                            binding.commonlayout.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.txtviewsun.setVisibility(View.GONE);
                            binding.txtviewmon.setVisibility(View.GONE);
                            binding.txtviewtues.setVisibility(View.GONE);
                            binding.txtviewwednes.setVisibility(View.GONE);
                            binding.txtviewthurs.setVisibility(View.GONE);
                            binding.txtviewfri.setVisibility(View.GONE);
                            binding.txtviewsatur.setVisibility(View.GONE);
                            binding.monthScrollView.setVisibility(View.VISIBLE);
                            binding.daysDaysScrollView.setVisibility(View.GONE);
                        } else {
                            if (l == 1) {
                                eventOccuranceType = "daily";
                                binding.daysChipGroup.setSingleSelection(false);
                            } else {
                                binding.daysChipGroup.setSingleSelection(true);
                                eventOccuranceType = "weekly";
                            }
                            binding.daysDaysScrollView.setVisibility(View.VISIBLE);
                            binding.monthScrollView.setVisibility(View.GONE);
                            if (binding.daysChipGroup.getChildCount() == 0)
                            {
                                for (int index = 0; index < 7; index++) {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(Constants.days[index]);
                                    chip.setOnCheckedChangeListener((compoundButton, b) -> {
                                        binding.linearLayout.setVisibility(View.GONE);
                                        if (chip.getText().toString() == "Sunday") {
                                            if (b) {
                                                binding.sunday.setVisibility(View.VISIBLE);
                                                binding.txtviewsun.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.sunday.setVisibility(View.GONE);
                                                binding.txtviewsun.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Monday") {
                                            if (b) {
                                                binding.monday.setVisibility(View.VISIBLE);
                                                binding.txtviewmon.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.monday.setVisibility(View.GONE);
                                                binding.txtviewmon.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Tuesday") {
                                            if (b) {
                                                binding.tuesday.setVisibility(View.VISIBLE);
                                                binding.txtviewtues.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.tuesday.setVisibility(View.GONE);
                                                binding.txtviewtues.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Wednesday") {
                                            if (b) {
                                                binding.wednesday.setVisibility(View.VISIBLE);
                                                binding.txtviewwednes.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.wednesday.setVisibility(View.GONE);
                                                binding.txtviewwednes.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Thursday") {
                                            if (b) {
                                                binding.thursday.setVisibility(View.VISIBLE);
                                                binding.txtviewthurs.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.thursday.setVisibility(View.GONE);
                                                binding.txtviewthurs.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Friday") {
                                            if (b) {
                                                binding.friday.setVisibility(View.VISIBLE);
                                                binding.txtviewfri.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.friday.setVisibility(View.GONE);
                                                binding.txtviewfri.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Saturday") {
                                            if (b) {
                                                binding.saturday.setVisibility(View.VISIBLE);
                                                binding.txtviewsatur.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.saturday.setVisibility(View.GONE);
                                                binding.txtviewsatur.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        viewModel.selectDays(chip);
                                    });
                                    binding.daysChipGroup.addView(chip);
                                }
                            }
                        }
                    }
                    createEventDAO.setEventOccuranceType(eventOccuranceType);
                }
                else
                {
                    for (int i = 0; i < updateEventDao.getEventOccurrence().size(); i++)
                    {
                        try
                        {
                            if (i == 0)
                            {
                                builder.append(Constants.days[updateEventDao.getEventOccurrence().get(i).getOccurredOn()]);
                            }
                            else
                            {
                                builder.append(",").append(Constants.days[updateEventDao.getEventOccurrence().get(i).getOccurredOn()]);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                if (updateEventDao.getEventOccurrence().size() == 0) {
                    long l = 0;
                    viewModel.reset(l);
                    if (l == 0) {
                        eventOccuranceType = "oneTime";
                        binding.linearLayout.setVisibility(View.VISIBLE);
                        binding.commonlayout.setVisibility(View.GONE);
                        binding.sunday.setVisibility(View.GONE);
                        binding.monday.setVisibility(View.GONE);
                        binding.tuesday.setVisibility(View.GONE);
                        binding.wednesday.setVisibility(View.GONE);
                        binding.thursday.setVisibility(View.GONE);
                        binding.friday.setVisibility(View.GONE);
                        binding.saturday.setVisibility(View.GONE);
                        binding.txtviewsun.setVisibility(View.GONE);
                        binding.txtviewmon.setVisibility(View.GONE);
                        binding.txtviewtues.setVisibility(View.GONE);
                        binding.txtviewwednes.setVisibility(View.GONE);
                        binding.txtviewthurs.setVisibility(View.GONE);
                        binding.txtviewfri.setVisibility(View.GONE);
                        binding.txtviewsatur.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                        binding.typeSubLayout.setVisibility(View.GONE);
                    } else if (l == 2 || l == 3 || l == 1) {
                        binding.typeSubLayout.setVisibility(View.VISIBLE);
                        if (l == 3) {
                            if (binding.monthChipGroup.getChildCount() == 0) {
                                for (int index = 0; index < 31; index++) {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(String.valueOf(index + 1));
                                    chip.setOnCheckedChangeListener((compoundButton, b) -> {
                                        viewModel.selectMonths(chip);
                                    });
                                    binding.monthChipGroup.addView(chip);
                                }
                            }
                            eventOccuranceType = "monthly";
                            binding.monthChipGroup.setSingleSelection(true);
                            binding.commonlayout.setVisibility(View.VISIBLE);
                            binding.linearLayout.setVisibility(View.GONE);
                            binding.sunday.setVisibility(View.GONE);
                            binding.monday.setVisibility(View.GONE);
                            binding.tuesday.setVisibility(View.GONE);
                            binding.wednesday.setVisibility(View.GONE);
                            binding.thursday.setVisibility(View.GONE);
                            binding.friday.setVisibility(View.GONE);
                            binding.saturday.setVisibility(View.GONE);
                            binding.txtviewsun.setVisibility(View.GONE);
                            binding.txtviewmon.setVisibility(View.GONE);
                            binding.txtviewtues.setVisibility(View.GONE);
                            binding.txtviewwednes.setVisibility(View.GONE);
                            binding.txtviewthurs.setVisibility(View.GONE);
                            binding.txtviewfri.setVisibility(View.GONE);
                            binding.txtviewsatur.setVisibility(View.GONE);
                            binding.monthScrollView.setVisibility(View.VISIBLE);
                            binding.daysDaysScrollView.setVisibility(View.GONE);
                        } else {
                            if (l == 1) {
                                eventOccuranceType = "daily";
                                binding.daysChipGroup.setSingleSelection(false);
                            } else {
                                binding.daysChipGroup.setSingleSelection(true);
                                eventOccuranceType = "weekly";
                            }
                            binding.daysDaysScrollView.setVisibility(View.VISIBLE);
                            binding.monthScrollView.setVisibility(View.GONE);
                            if (binding.daysChipGroup.getChildCount() == 0) {
                                for (int index = 0; index < 7; index++) {
                                    Chip chip = new Chip(mContext);
                                    chip.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    chip.setCheckable(true);
                                    chip.setCheckedIconVisible(true);
                                    chip.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                                    chip.setCheckedIcon(mContext.getResources().getDrawable(R.drawable.tick));
                                    chip.setChipIcon(mContext.getResources().getDrawable(R.drawable.tick_unchecked));
                                    chip.setText(Constants.days[index]);
                                    chip.setOnCheckedChangeListener((compoundButton, b) -> {
                                        binding.linearLayout.setVisibility(View.GONE);
                                        if (chip.getText().toString() == "Sunday") {
                                            if (b) {
                                                binding.sunday.setVisibility(View.VISIBLE);
                                                binding.txtviewsun.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.sunday.setVisibility(View.GONE);
                                                binding.txtviewsun.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Monday") {
                                            if (b) {
                                                binding.monday.setVisibility(View.VISIBLE);
                                                binding.txtviewmon.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.monday.setVisibility(View.GONE);
                                                binding.txtviewmon.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Tuesday") {
                                            if (b) {
                                                binding.tuesday.setVisibility(View.VISIBLE);
                                                binding.txtviewtues.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.tuesday.setVisibility(View.GONE);
                                                binding.txtviewtues.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Wednesday") {
                                            if (b) {
                                                binding.wednesday.setVisibility(View.VISIBLE);
                                                binding.txtviewwednes.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.wednesday.setVisibility(View.GONE);
                                                binding.txtviewwednes.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Thursday") {
                                            if (b) {
                                                binding.thursday.setVisibility(View.VISIBLE);
                                                binding.txtviewthurs.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.thursday.setVisibility(View.GONE);
                                                binding.txtviewthurs.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Friday") {
                                            if (b) {
                                                binding.friday.setVisibility(View.VISIBLE);
                                                binding.txtviewfri.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.friday.setVisibility(View.GONE);
                                                binding.txtviewfri.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        if (chip.getText().toString() == "Saturday") {
                                            if (b) {
                                                binding.saturday.setVisibility(View.VISIBLE);
                                                binding.txtviewsatur.setVisibility(View.VISIBLE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            } else {
                                                binding.saturday.setVisibility(View.GONE);
                                                binding.txtviewsatur.setVisibility(View.GONE);
                                                binding.commonlayout.setVisibility(View.GONE);
                                            }
                                        }
                                        viewModel.selectDays(chip);
                                    });
                                    binding.daysChipGroup.addView(chip);
                                }
                            }
                        }
                    }
                    createEventDAO.setEventOccuranceType(eventOccuranceType);


                } else {
                    for (int i = 0; i < updateEventDao.getEventOccurrence().size(); i++) {
                        if (i == 0) {
                            builder.append(updateEventDao.getEventOccurrence().get(i).getOccurredOn());
                        } else {
                            builder.append(",").append(updateEventDao.getEventOccurrence().get(i).getOccurredOn());
                        }
                    }
                }
            }
            binding.typeSubSpinner.setText(builder.toString());
        }
        Log.e(updateEventDao.getStart(), updateEventDao.getEnd());
        binding.textView39.setText(Utility.getDateMonthYearName(updateEventDao.getStart(), true));
        binding.endDateTv.setText(Utility.getDateMonthYearName(updateEventDao.getEnd(), true));
        binding.startTimeTv.setText(StringUtils.getTime(updateEventDao.getStart()));
        binding.endTimeTv.setText(StringUtils.getTime(updateEventDao.getEnd()));
        selectedVenue = updateEventDao.getVenue();
        selectedVenue.setId(updateEventDao.getVenueId());
        binding.venueNameEt.setText(selectedVenue.getName());
        binding.venueLocEt.setText(selectedVenue.getName());
        if (updateEventDao.getSubVenueEvent() != null && updateEventDao.getSubVenueEvent().size() > 0) {
            binding.tvSubVenue.setVisibility(View.VISIBLE);
            StringBuilder subVenue = new StringBuilder();
            for (int i = 0; i < updateEventDao.getSubVenueEvent().size(); i++) {
                SubVenue sub = updateEventDao.getSubVenueEvent().get(i).getSubVenues().get(0);
                subVenue.append(sub.getSubVenueName()).append(" ( ").append(sub.getSubVenueCapacity()).append(" ) ").append(",");
            }
            subVenue.deleteCharAt(subVenue.length() - 1).toString();
            binding.tvSubVenue.setText(subVenue.toString());
        } else {
            binding.tvSubVenue.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(updateEventDao.getDescription())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                binding.additionEt.setText(Html.fromHtml(updateEventDao.getDescription(), Html.FROM_HTML_MODE_COMPACT));
            else
                binding.additionEt.setText(Html.fromHtml(updateEventDao.getDescription()));
        }

        binding.additionEt.setEnabled(true);
        binding.additionEt2.setText(updateEventDao.getDescription2());
        binding.sellSwitch.setChecked(updateEventDao.isIs_availability());

        for (EventImages images : updateEventDao.getEventImages()) {
            ImageDAO imageDAO = new ImageDAO(images.getId(), images.getEventImage(), images.isPrimary());
            previousImages.add(imageDAO);
            galleryPicAdapter.refresh(previousImages);
        }

        if (imageDaoList != null && imageDaoList.size() > 0 || previousImages != null && previousImages.size() > 0) {
            binding.tvShowNote.setVisibility(View.GONE);
        } else {
            binding.tvShowNote.setVisibility(View.VISIBLE);
        }

        if (imageDaoList != null && imageDaoList.size() == 5 || previousImages != null && previousImages.size() == 5)
            binding.addImage.setVisibility(View.GONE);
        else
            binding.addImage.setVisibility(View.VISIBLE);

        binding.imageView22.setOnClickListener(v -> {
            Dialogs.setMinMaxDate(getContext(), "", updateEventDao.getStart(), binding.textView39, date ->
            {
                binding.textView39.setText(date);
                binding.startTimeTv.setText(getString(R.string.start_time));
                binding.endDateTv.setText(getString(R.string.end_date));
                binding.endTimeTv.setText(getString(R.string.end_time));
                clearVenue();
                setSellingDate();
            });
        });
        binding.textView39.setOnClickListener(v ->
        {
            Dialogs.setMinMaxDate(getContext(), getTodayDate, updateEventDao.getStart(), binding.textView39, date ->
            {
                selectedStartEventDate = date;
                binding.textView39.setText(date);
                binding.startTimeTv.setText(getString(R.string.start_time));
                binding.endDateTv.setText(getString(R.string.end_date));
                binding.endTimeTv.setText(getString(R.string.end_time));
                binding.textView59.setText(getString(R.string.start_date));
                binding.endDateTv2.setText(getString(R.string.end_date));
                binding.startTimeTv2.setText(getString(R.string.start_time));
                binding.endTimeTv2.setText(getString(R.string.end_time));
                clearVenue();
                //setSellingDate();
            });
        });
        binding.endDateImg.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView39.getText().toString(), updateEventDao.getStart(), binding.textView39, date -> {
                    setSellingDate();
                    binding.endDateTv.setText(date);
                    clearVenue();
                });
        });
        binding.endDateTv.setOnClickListener(v -> {
            if (binding.textView39.getText().equals(getString(R.string.start_date)))
                Toast.makeText(getContext(), "Select start date first!", Toast.LENGTH_LONG).show();
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView39.getText().toString(), updateEventDao.getStart(), binding.textView39, date -> {
                    binding.endDateTv.setText(date);
                    //setSellingDate();

                    clearVenue();
                });
        });
        setSellingDate();
        binding.updateBtn.setOnClickListener(v -> {
            if (eventOccuranceType.contains("oneTime")) {
                startDate = binding.textView39.getText().toString();
                endDate = binding.endDateTv.getText().toString();
                startTime = binding.startTimeTv.getText().toString();
                endTime = binding.endTimeTv.getText().toString();
            }
//            if (eventOccuranceType.contains("daily") || eventOccuranceType.contains("weekly"))
//            {
//                startDate = getTodayDate;
//                endDate = getTodayDate;
//                if (binding.typeSubSpinner.getText().toString().contains("Sunday"))
//                {
//                    startTime=binding.startTimeTvSun.getText().toString();
//                    endTime=binding.endTimeTv2Sun.getText().toString();
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Monday"))
//                {
//                    startTime=binding.startTimeTvMon.getText().toString();
//                    endTime=binding.endTimeTv2Mon.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Tuesday"))
//                {
//                    startTime=binding.startTimeTvTues.getText().toString();
//                    endTime=binding.endTimeTv2Tues.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Wednesday"))
//                {
//                    startTime=binding.startTimeTvWednes.getText().toString();
//                    endTime=binding.endTimeTv2Wednes.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Thursday"))
//                {
//                    startTime=binding.startTimeTvThurs.getText().toString();
//                    endTime=binding.endTimeTv2Thurs.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Friday"))
//                {
//                    startTime=binding.startTimeTvFri.getText().toString();
//                    endTime=binding.endTimeTv2Fri.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                if (binding.typeSubSpinner.getText().toString().contains("Saturday"))
//                {
//                    startTime=binding.startTimeTvSatur.getText().toString();
//                    endTime=binding.endTimeTv2Satur.getText().toString();
//                    createEventDAO.setStartTime(startTime);
//                    createEventDAO.setEndTime(endTime);
//                }
//                Log.d("Start Time",startTime);
//            }
//            venueName = binding.venueNameEt.getText().toString();
            venueName = binding.venueLocEt.getText().toString().trim();

            startSellingDate = binding.typeSubSpinner.getText().toString();
            endSellingDate = binding.typeSubSpinner.getText().toString();
            startSellingTime = binding.startTimeTv2.getText().toString();
            endSellingTime = binding.endTimeTv2.getText().toString();
            sortDescription = binding.additionEt.getText().toString().trim();

            JsonArray occuranceOnList = new JsonArray();

            Date startDateUtil = null, endDateUtil = null;
            Date startSellingDateUtil = null, endSellingDateUtil = null;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
//            if(binding.typeSubLayout.isShown()){
//                for(int i =0 ;i<dao.getEventOccurrence().size();i++){
//                    occuranceOnList.add(dao.getEventOccurrence().get(i).getOccurredOn());
//                }
//            }
            try {
                startDateUtil = sdf.parse(startDate + " " + startTime);
                endDateUtil = sdf.parse(endDate + " " + endTime);

                startSellingDateUtil = sdf1.parse(startSellingDate + " " + startSellingTime);
                endSellingDateUtil = sdf1.parse(endSellingDate + " " + endSellingTime);

            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("fnalsnfklsa", e.getMessage() + " ParseException: ");
            }

            String additionalInfo2 = binding.additionEt2.getText().toString().trim();
            if (TextUtils.isEmpty(binding.eventNameEt.getText().toString())) {
                Dialogs.toast(mContext, v, getString(R.string.event_name));
                binding.eventNameEt.requestFocus();
                return;
            } else if (venueName == null || TextUtils.isEmpty(venueName)) {
                Dialogs.toast(mContext, v, getString(R.string.venueNameRequired));
                binding.venueLocEt.requestFocus();
                return;
            } else if (startDate.equalsIgnoreCase(getString(R.string.start_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartDateRequired));
                return;
            } else if (endDate.equalsIgnoreCase(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
                return;
            } else if (startTime.equalsIgnoreCase(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
                return;
            } else if (endTime.equalsIgnoreCase(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
                return;
            } else if (imageDaoList.size() == 0 && previousImages.size() == 0) {
                Dialogs.toast(mContext, v, getString(R.string.galleryImageRequired));
                return;
            }
            if (sortDescription.length() < 50) {
                binding.additionEt.requestFocus();
                Dialogs.toast(mContext, v, getString(R.string.eventDetailMin));
                return;
            }
            if (sortDescription.length() > 4000) {
                binding.additionEt.requestFocus();
                Dialogs.toast(mContext, v, getString(R.string.eventDetailMax));
                return;
            }
            /*else if (!additionalInfo2.isEmpty() && additionalInfo2.length() < 200) {
                binding.additionEt2.requestFocus();
                Dialogs.toast(mContext, v, getString(R.string.addOnMin));
                return;
            }*/

           /* else if (startSellingDate.equals(getString(R.string.start_date))) {
                Dialogs.toast(mContext, v, getString(R.string.select_selling_date));
                return;
            } else if (endSellingDate.equals(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.selling_end_date));
                return;
            } else if (startSellingTime.equals(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.selling_start_time));
                return;
            } else if (endSellingTime.equals(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.selling_end_time));
                return;
            }*/

            if (!TextUtils.isEmpty(updateEventDao.getHostMobile()) && !TextUtils.isEmpty(updateEventDao.getHostAddress()))
                postEventUpdate(startDateUtil, endDateUtil, additionalInfo2);
            else if (!TextUtils.isEmpty(updateEventDao.getOtherWebsiteUrl()))
                postEventUpdate(startDateUtil, endDateUtil, additionalInfo2);
            else if (Utility.isNetworkAvailable(getActivity()))
                updateEventAPI(startDateUtil, endDateUtil, additionalInfo2, "", "", "", "", "");
            else
                Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_LONG).show();
        });

    }

    private void deleteAPI() {
        loader.show("");
        Call call = apiInterface.deleteEvent(updateEventDao.getId());
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                loader.dismiss();

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());
                        Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD_EVENTS, true);
                        Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD_PAST_EVENTS, true);
                        Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD, true);
                        Toast.makeText(mContext, OBJ.getString(API.MESSAGE), Toast.LENGTH_SHORT).show();
                        activity.setResult(RESULT_CANCELED);
                        activity.onBackPressed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

    public void updateEventAPI(Date startDateUtil, Date endDateUtil, String additionalInfo2, String hostCountryCode, String mobile, String email, String webUrl, String webUrlOther) {
        loader.show("Please Wait...");

        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        requestBodyMap.put("name", RequestBody.create(MediaType.parse("multipart/form-data"), binding.eventNameEt.getText().toString()));

        if (update) {
            if (previousImages != null && previousImages.size() > 0) {
                if (previousImages.get(0).getId() > 0) {
                    int id = previousImages.get(0).getId();
                    requestBodyMap.put("imageId", RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(id)));
                }
            }
        }

//            if (isVenuer)
        requestBodyMap.put("venueId", RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(selectedVenue.getId())));
        RequestBody subVenue = null;
        int userId = Utility.getSharedPreferencesInteger(getActivity(), API.USER_ID);
        JsonArray jsonArray = new JsonArray();
        if (updateSelectedSubVenues != null && updateSelectedSubVenues.size() > 0) {
            for (int i = 0; i < updateSelectedSubVenues.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("userId", userId);
                jsonObject.addProperty("venueId", updateSelectedSubVenues.get(i).getVenueId());
                jsonObject.addProperty("subVenueId", updateSelectedSubVenues.get(i).getSubVenueId());
                if (update) {
                    jsonObject.addProperty("eventId", updateEventDao.getId());
                    if (updateSelectedSubVenues.get(i).getSubVenueEventId() != 0) {
                        jsonObject.addProperty("id", updateSelectedSubVenues.get(i).getSubVenueEventId());
                    }
                    jsonObject.addProperty("status", "booked");
                } else {
                    jsonObject.addProperty("status", updateSelectedSubVenues.get(i).getStatus());
                }
                jsonArray.add(jsonObject);
            }
        }
        subVenue = RequestBody.create(MediaType.parse("multipart/form-data"), jsonArray.toString());
//            else {
//                requestBodyMap.put("venueName", RequestBody.create(MediaType.parse("multipart/form-data"), venueName));
//
//                requestBodyMap.put("venueAddress", RequestBody.create(MediaType.parse("multipart/form-data"), venueAddress));
//
//                requestBodyMap.put("venueLatitude", RequestBody.create(MediaType.parse("multipart/form-data"),
//                        latLongt == null ? selectedVenue.getLatitude() : String.valueOf(latLongt.latitude)));
//                requestBodyMap.put("venueLongitude", RequestBody.create(MediaType.parse("multipart/form-data"), latLongt == null ? selectedVenue.getLongitude() : String.valueOf(latLongt.longitude)));
//                requestBodyMap.put("countryCode", RequestBody.create(MediaType.parse("multipart/form-data"), binding.cpp.getDefaultCountryCodeWithPlus()));
//                requestBodyMap.put("city", RequestBody.create(MediaType.parse("multipart/form-data"), latLongt == null ? Utility.getCity(activity, Double.parseDouble(selectedVenue.getLatitude()), Double.parseDouble(selectedVenue.getLongitude())) : Utility.getCity(activity, latLongt.latitude, latLongt.longitude)));
//                requestBodyMap.put("state", RequestBody.create(MediaType.parse("multipart/form-data"), latLongt == null ? Utility.getState(activity, Double.parseDouble(selectedVenue.getLatitude()), Double.parseDouble(selectedVenue.getLongitude())) : Utility.getState(activity, latLongt.latitude, latLongt.longitude)));
//            }

       /* requestBodyMap.put("start", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(startDateUtil)));
        requestBodyMap.put("end", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(endDateUtil)));*/

//        requestBodyMap.put("start", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil)));
//        requestBodyMap.put("end", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil)));

           /* requestBodyMap.put("sellingStart", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(startSellingDateUtil)));
            requestBodyMap.put("sellingEnd", RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(endSellingDateUtil)));*/

        requestBodyMap.put("imageIds", RequestBody.create(MediaType.parse("multipart/form-data"), deletedPreviousImagesIds.toString()));
        requestBodyMap.put("id", RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(updateEventDao.getId())));

        requestBodyMap.put("description", RequestBody.create(MediaType.parse("multipart/form-data"), sortDescription));
        requestBodyMap.put("description2", RequestBody.create(MediaType.parse("multipart/form-data"), additionalInfo2));
        requestBodyMap.put("subVenueEvent", subVenue);
        requestBodyMap.put("countryCode", RequestBody.create(MediaType.parse("multipart/form-data"), hostCountryCode));
        requestBodyMap.put("hostMobile", RequestBody.create(MediaType.parse("multipart/form-data"), mobile));
        requestBodyMap.put("hostAddress", RequestBody.create(MediaType.parse("multipart/form-data"), email));
        requestBodyMap.put("websiteUrl", RequestBody.create(MediaType.parse("multipart/form-data"), webUrl));
        requestBodyMap.put("otherWebsiteUrl", RequestBody.create(MediaType.parse("multipart/form-data"), webUrlOther));

        viewModel.editEventRequest(requestBodyMap, imageDaoList).observe(getViewLifecycleOwner(), myResponse -> {
            loader.dismiss();
            if (myResponse.isSuccess()) {
                //success event edit http request
                Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD_EVENTS, true);
                Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD_PAST_EVENTS, true);
                Utility.setSharedPreferencesBoolean(activity, API.HOT_RELOAD, true);
                Toast.makeText(mContext, myResponse.getMessage(), Toast.LENGTH_SHORT).show();
                activity.setResult(RESULT_OK);
                activity.onBackPressed();

            } else {
                //failed event edit http request
                Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean validateAndSetData(View V) {
        selection = new StringBuilder();
        if (eventOccuranceType=="Monthly")
        {
//            startDate = binding.textView39.getText().toString();
//            endDate = binding.endDateTv.getText().toString();
            startTime = binding.startTimeTvWeek.getText().toString();
            endTime = binding.endTimeTvWeek.getText().toString();
//            createEventDAO.setStartDate(startDate);
//            createEventDAO.setEndDate(startDate);
            createEventDAO.setStartTime(startTime);
            createEventDAO.setEndTime(endTime);
        }
        if (eventOccuranceType == "daily"||eventOccuranceType == "weekly")
        {
            startDate = "20/10/2025";
            endDate = "20/10/2025";
            createEventDAO.setStartDate(startDate);
            createEventDAO.setEndDate(endDate);
            value1=binding.typeSubSpinner.getText().toString();
            if (binding.typeSubSpinner.getText().toString().contains("Sunday"))
            {
                startTime=binding.startTimeTvSun.getText().toString();
                endTime=binding.endTimeTv2Sun.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Monday"))
            {
                startTime=binding.startTimeTvMon.getText().toString();
                endTime=binding.endTimeTv2Mon.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Tuesday"))
            {
                startTime=binding.startTimeTvTues.getText().toString();
                endTime=binding.endTimeTv2Tues.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Wednesday"))
            {
                startTime=binding.startTimeTvWednes.getText().toString();
                endTime=binding.endTimeTv2Wednes.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Thursday"))
            {
                startTime=binding.startTimeTvThurs.getText().toString();
                endTime=binding.endTimeTv2Thurs.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Friday"))
            {
                startTime=binding.startTimeTvFri.getText().toString();
                endTime=binding.endTimeTv2Fri.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
            if (binding.typeSubSpinner.getText().toString().contains("Saturday"))
            {
                startTime=binding.startTimeTvSatur.getText().toString();
                endTime=binding.endTimeTv2Satur.getText().toString();
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndTime(endTime);
            }
        }
        if (eventOccuranceType.equals("oneTime"))
        {
            startDate = binding.textView39.getText().toString();
            endDate = binding.endDateTv.getText().toString();
            startTime=binding.startTimeTv.getText().toString();
            endTime=binding.endTimeTv.getText().toString();
            createEventDAO.setStartDate(startDate);
            createEventDAO.setStartTime(startTime);
            createEventDAO.setEndDate(endDate);
            createEventDAO.setEndTime(endTime);
        }

        String name = binding.eventNameEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            binding.eventNameEt.requestFocus();
            Dialogs.toast(mContext, V, getString(R.string.enter_name_of_event));
            return false;
        }
        String selectedAvailability = binding.typeSubSpinner.getText().toString();
        if ((selectedAvailability.equalsIgnoreCase("Select Days") ||
                selectedAvailability.equalsIgnoreCase("Select Months")) &&
                !eventOccuranceType.equalsIgnoreCase("oneTime")) {
            Dialogs.toast(mContext, V, eventOccuranceType.equalsIgnoreCase("monthly") ? "Select months please!" : "Select Days please!");
            return false;
        }
        if (eventOccuranceType.equalsIgnoreCase("daily")) {
            String sarr[] = selectedAvailability.split(", ");
            for (int i = 0; i < sarr.length; i++) {
                if (i == 0) {
                    selection.append("[").append(StringUtils.getDayMonthNum(sarr[i]) + "");
                } else
                    selection.append(",").append(StringUtils.getDayMonthNum(sarr[i]) + "");
                if ((i + 1) == sarr.length)
                    selection.append("]");
            }
        }
        if (eventOccuranceType.equalsIgnoreCase("monthly")) {
            String sarr[] = selectedAvailability.split(", ");
            for (int i = 0; i < sarr.length; i++) {
                if (i == 0) {
                    selection.append("[").append(sarr[i]);
                } else
                    selection.append(",").append(sarr[i]);
                if ((i + 1) == sarr.length)
                    selection.append("]");
            }
        }
        if (eventOccuranceType.equalsIgnoreCase("weekly")) {
            binding.commonlayout.setVisibility(View.VISIBLE);
            if (selectedAvailability.equalsIgnoreCase("Select Day")) {
                Dialogs.toast(mContext, V, "Select Day please!");
                return false;
            }
            switch (selectedAvailability) {
                case "Sunday":
                    selection.append("[7]");
                    break;
                case "Monday":
                    selection.append("[1]");
                    break;
                case "Tuesday":
                    selection.append("[2]");
                    break;
                case "Wednesday":
                    selection.append("[3]");
                    break;
                case "Thursday":
                    selection.append("[4]");
                    break;
                case "Friday":
                    selection.append("[5]");
                    break;
                case "Saturday":
                    selection.append("[6]");
                    break;
            }
        }
        if (eventOccuranceType.contains("oneTime")) {
            if (startDate.equalsIgnoreCase(getString(R.string.start_date))) {
                Dialogs.toast(mContext, V, getString(R.string.eventStartDateRequired));
                return false;
            }
            if (endDate.equalsIgnoreCase(getString(R.string.end_date))) {
                Dialogs.toast(mContext, V, getString(R.string.eventEndDateRequired));
                return false;
            }
            if (startTime.equalsIgnoreCase(getString(R.string.start_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                return false;
            }
            if (endTime.equalsIgnoreCase(getString(R.string.end_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                return false;
            }
            if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                return false;
            }
        }
        if (eventOccuranceType == "Daily") {
            if (value1.contains("Monday")) {
                Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
            if (value1.contains("Tuesday")) {
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Toast.makeText(mContext, "" + getString(R.string.venueNoAvailabilty), Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }

            if (value1.contains("Wednesday")) {
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
            if (value1.contains("Thursday")) {
                Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
            if (value1.contains("Friday")) {
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
            if (value1.contains("Saturday")) {
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
            if (value1.contains("Sunday")) {
                if (value1.equalsIgnoreCase(getString(R.string.start_time))) {
                    Toast.makeText(mContext, "" + value1, Toast.LENGTH_SHORT).show();
                    Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                    return false;
                }
                if (value1.equalsIgnoreCase(getString(R.string.end_time))) {
                    Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                    return false;
                }
                if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                    Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                    return false;
                }
            }
        }
        if (eventOccuranceType == "Weekly") {
            if (startTimecl.equalsIgnoreCase(getString(R.string.start_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                return false;
            }
            if (endTimecl.equalsIgnoreCase(getString(R.string.end_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                return false;
            }
            if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                return false;
            }
        }
        if (eventOccuranceType == "Monthly") {
            if (startTimecl.equalsIgnoreCase(getString(R.string.start_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventStartTimeRequired));
                return false;
            }
            if (endTimecl.equalsIgnoreCase(getString(R.string.end_time))) {
                Dialogs.toast(mContext, V, getString(R.string.eventEndTimeRequired));
                return false;
            }
            if (selectedVenue == null || binding.venueLocEt.getText().toString().trim().length() == 0) {
                Dialogs.toast(mContext, V, getString(R.string.venueNoAvailabilty));
                return false;
            }
        }

//
//        venueName = binding.venueNameEt.getText().toString().trim();
//        if (!isVenuer && venueName.length() == 0) {
//            Dialogs.toast(mContext, V, getString(R.string.venueNameRequired));
//            binding.venueNameEt.requestFocus();
//            return false;
//        }


//        if (!isVenuer && latLongt == null) {
//            Dialogs.toast(mContext, V, getString(R.string.venueLocationRequired));
//            return false;
//        }
        if (imageDaoList.size() == 0) {
            Dialogs.toast(mContext, V, getString(R.string.galleryImageRequired));
            return false;
        }
        String additionalInfo = binding.additionEt.getText().toString().trim();
        if (additionalInfo.length() < 50) {
            binding.additionEt.requestFocus();
            Dialogs.toast(mContext, V, getString(R.string.eventDetailMin));
            return false;
        }
        if (additionalInfo.length() > 4000) {
            binding.additionEt.requestFocus();
            Dialogs.toast(mContext, V, getString(R.string.eventDetailMax));
            return false;
        }

        String additionalInfo2 = binding.additionEt2.getText().toString().trim();
        /*if (!additionalInfo2.isEmpty() && additionalInfo2.length() < 200) {
            binding.additionEt2.requestFocus();
            Dialogs.toast(mContext, V, getString(R.string.addOnMin));
            return false;
        }
        if (!additionalInfo2.isEmpty() && additionalInfo2.length() > 2500) {
            binding.additionEt2.requestFocus();
            Dialogs.toast(mContext, V, getString(R.string.addOnMax));
            return false;
        }*/

        if (eventType.length() == 0) {
            Dialogs.toast(mContext, V, "Choose event type!");
            return false;
        }

        createEventDAO.setName(name);
        createEventDAO.setEventOccuranceType(eventOccuranceType);
        createEventDAO.setOccurredOn(selection.toString());
//        if (eventOccuranceType.contains("oneTime"))
//        {
//        createEventDAO.setStartDate(startDate);
//        createEventDAO.setEndDate(endDate);
//        }
//        if (eventOccuranceType.contains("monthly"))
//        {
//            createEventDAO.setStartDate(startDate);
//            createEventDAO.setEndDate(startDate);
//        }
//        if (eventOccuranceType.contains("weekly"))
//        {
//            createEventDAO.setStartDate(startDate);
//            createEventDAO.setEndDate(endDate);
//        }
//        if (eventOccuranceType.contains("daily"))
//        {
//            createEventDAO.setStartDate(startDate);
//            createEventDAO.setEndDate(endDate);
//        }
        //        if (!isVenuer) {
        createEventDAO.setVenueName(venueName);
//            createEventDAO.setLat(String.valueOf(latLongt.latitude));
//            createEventDAO.setLongt(String.valueOf(latLongt.longitude));
//        }

        createEventDAO.setDesc(additionalInfo);
        createEventDAO.setDesc2(additionalInfo2);
//        createEventDAO.setVenueAddress(binding.venueLocEt.getText().toString().trim());
        createEventDAO.setPaidType(eventType);
        createEventDAO.setImageList(imageList);
        createEventDAO.setImageDAOList(imageDaoList);


        return true;
    }

    private int getHour(String time) {
        int startHour = Integer.valueOf(time.substring(0, 2));
        return time.matches("(.*)PM") && startHour < 12 ? (startHour + 12) : startHour;
    }

    private void setEndTime(View v) {
        if (binding.startTimeTv.getText().toString().equals(getString(R.string.start_time)))
            Toast.makeText(mContext, "Select start time first!", Toast.LENGTH_LONG).show();
        else
            Dialogs.setTime(mContext, binding.endTimeTv, time -> {
                if (!binding.textView39.getText().equals(binding.endDateTv.getText().toString())) {
                    binding.endTimeTv.setText(time);
                } else {
                    String startTime = binding.startTimeTv.getText().toString();

                    clearVenue();

                    int startHour = Integer.valueOf(startTime.substring(0, 2));
                    int endHour = Integer.valueOf(time.substring(0, 2));

                    if ((startTime.matches("(.*)PM") || startTime.matches("(.*)pm")) && startHour < 12)
                        startHour += 12;

                    if ((time.matches("(.*)PM") || time.matches("(.*)pm")) && endHour < 12)
                        endHour += 12;

                    if (endHour > startHour)
                        binding.endTimeTv.setText(time);

                    else if (endHour == startHour) {
                        int startMin = Integer.valueOf(startTime.substring(3, 5));
                        int endMin = Integer.valueOf(time.substring(3, 5));
                        if (endMin > startMin)
                            binding.endTimeTv.setText(time);
                        else {
                            Dialogs.toast(mContext, v, "Start time must be greater than End time");
                            binding.endTimeTv.setText(getString(R.string.end_time));
                        }
                    } else {
                        Dialogs.toast(mContext, v, "Start time must be greater than End time");
                        binding.endTimeTv.setText(getString(R.string.end_time));
                    }
                 endTime=time;
                }
            });
    }

    private void checkForPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        permissionUtils = new PermissionUtils(this)
                .setCode(RC_HANDLE_CAMERA_PERM)
                .setPermissions(permissions)
                .onAlreadyGranted(this::choose)
                .build();
    }

    private void searchForPlace() {
        if (mLocationPermissionGranted) {

            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .build(mContext);
            startActivityForResult(intent, PLACE_PICKER_REQUEST_CODE);

        } else
            getLocationPermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 222:
                if (resultCode == RESULT_OK) {
                    binding.additionEt.requestFocus();
                    if (data != null && data.getData() != null) {
                        try {
                            setPicBrowsedByGallery(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        pickImageFromCamera();
                }
                break;

            case PLACE_PICKER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    binding.venueLocEt.setText(place.getAddress());
                    latLongt = place.getLatLng();
                    String code = Utility.getCountryCode(mContext, latLongt.latitude, latLongt.longitude);
                    binding.cpp.setCountryForNameCode(code);

                    if (!update) {
                        createEventDAO.setCountryCode(code);
                        createEventDAO.setCityName(Utility.getCity(mContext, latLongt.latitude, latLongt.longitude));
                    }

                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.e("WIG", status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {

                }

                break;


            case 101:
                if (resultCode == RESULT_OK) {
                    binding.additionEt.requestFocus();
                    selectedVenue = (VenueDAO) data.getParcelableExtra("venue");
                    if (selectedVenue != null) {
                        binding.venueLocEt.setText(selectedVenue.getName());
                        if (selectedVenue.getSelectedSubVenues() != null && selectedVenue.getSelectedSubVenues().size() > 0) {
                            binding.tvSubVenue.setVisibility(View.VISIBLE);
                            StringBuilder subVenue = new StringBuilder();
                            for (int i = 0; i < selectedVenue.getSelectedSubVenues().size(); i++) {
                                subVenue.append(selectedVenue.getSelectedSubVenues().get(i).getSubVenueName()).append(" ( ").append(selectedVenue.getSelectedSubVenues().get(i).getSubVenueCapacity()).append(" ) ").append(" ,");
                            }
                            subVenue.deleteCharAt(subVenue.length() - 1).toString();
                            binding.tvSubVenue.setText(subVenue.toString());
                            if (update) {
                                updateSelectedSubVenues.addAll(selectedVenue.getSelectedSubVenues());
                            } else if (createEventDAO != null) {
                                createEventDAO.setVenueName(selectedVenue.getName());
                                createEventDAO.setVenueId(selectedVenue.getId());
                                createEventDAO.setVenueAddress(selectedVenue.getVenueAddress());
                                createEventDAO.setSelectedSubVenues(selectedVenue.getSelectedSubVenues());
                            }
                        } else {
                            binding.tvSubVenue.setVisibility(View.GONE);
                            if (createEventDAO != null) {
                                createEventDAO.setVenueName(selectedVenue.getName());
                                createEventDAO.setVenueId(selectedVenue.getId());
                                createEventDAO.setVenueAddress(selectedVenue.getVenueAddress());
                            }
                        }

                        fetchVenueImages();
                    }
                }
                break;
        }
    }

    private void setGalleryAdapter() {
        galleryPicAdapter = new GalleryPicAdapter(true, (s, position) -> {
            if (previousImages != null && previousImages.size() > 0) {
                ImageDAO dao = new ImageDAO(s);
                imageDaoList.remove(dao);
                int index = previousImages.indexOf(dao);
                int selectedImgListIndex = imageDaoList.indexOf(dao);
                galleryPicAdapter.notifyItemRemoved(selectedImgListIndex);
                if (index != -1) {
                    ImageDAO uploadedDao = previousImages.remove(index);
                    if (TextUtils.isEmpty(deletedPreviousImagesIds)) {
                        deletedPreviousImagesIds.append(uploadedDao.getId());
                    } else
                        deletedPreviousImagesIds.append(",").append(uploadedDao.getId());
                }
            } else {
                imageDaoList.remove(position);
                galleryPicAdapter.notifyItemRemoved(position);
            }
            if (imageDaoList != null && imageDaoList.size() == 5 || previousImages != null && previousImages.size() == 5) {
                binding.addImage.setVisibility(View.GONE);
            } else {
                binding.addImage.setVisibility(View.VISIBLE);
            }

            galleryPicAdapter.notifyDataSetChanged();
        });
        binding.galleryRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.galleryRv.setHasFixedSize(true);
        binding.galleryRv.setAdapter(galleryPicAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.galleryRv);
    }

    private void setPicBrowsedByGallery(Intent data) {
        if (data != null && data.getData() != null) {
            String filePath = FileUtils.getPath(activity, data.getData());

            if (filePath != null && filePath.length() > 0) {
                ImageDAO dao = new ImageDAO(filePath);
                if (!imageDaoList.contains(dao)) {
                    if (update) {
                        previousImages.add(dao);
                        imageDaoList.add(dao);
                        galleryPicAdapter.refresh(previousImages);
                    } else {
                        imageDaoList.add(dao);
                        galleryPicAdapter.refresh(imageDaoList);
                    }
                } else {
                    Toast.makeText(mContext, "Picture is already in list", Toast.LENGTH_LONG).show();
                }
                if (imageDaoList != null && imageDaoList.size() > 0 || previousImages != null && previousImages.size() > 0) {
                    binding.tvShowNote.setVisibility(View.GONE);
                } else {
                    binding.tvShowNote.setVisibility(View.VISIBLE);
                }
                if (imageDaoList.size() == 5)
                    binding.addImage.setVisibility(View.GONE);
                else
                    binding.addImage.setVisibility(View.VISIBLE);

            }
        } else {
            Toast.makeText(activity, "Please select different image", Toast.LENGTH_SHORT).show();
        }

    }

    private void pickImageFromCamera() {
        ImageDAO dao = new ImageDAO(currentPhotoPath);
        if (!imageDaoList.contains(dao)) {
            if (update) {
                previousImages.add(new ImageDAO(currentPhotoPath));
                imageDaoList.add(new ImageDAO(currentPhotoPath));
                galleryPicAdapter.refresh(previousImages);
            } else {
                imageDaoList.add(new ImageDAO(currentPhotoPath));
                galleryPicAdapter.refresh(imageDaoList);
            }
            if (imageDaoList.size() == 5)
                binding.addImage.setVisibility(View.GONE);
            else
                binding.addImage.setVisibility(View.VISIBLE);
            galleryAddPic();
            setPic();
        } else {
            Toast.makeText(mContext, "Picture is already in list", Toast.LENGTH_LONG).show();
        }
        if (imageDaoList != null && imageDaoList.size() > 0 || previousImages != null && previousImages.size() > 0) {
            binding.tvShowNote.setVisibility(View.GONE);
        } else {
            binding.tvShowNote.setVisibility(View.VISIBLE);
        }
    }

    private void fetchVenueImages() {
        //System.out.println("selected venue"+selectedVenue.toString());
        if (selectedVenue == null) return;
        Log.d("fnabslfbaklsj", "fetchVenueImages: " + selectedVenue.getId());
        venueViewModel.getVenueImages(selectedVenue.getId()).observe(getViewLifecycleOwner(), response -> {
            if (response.getCode() == API.SESSION_EXPIRE) {
                response.setCode(0);
                Utility.sessionExpired(mContext);
            } else if (response.isSuccess() && response.getVenueImages().size() > 0) {
                binding.venueLayout.setVisibility(View.VISIBLE);
                if (createEventDAO != null)
                    createEventDAO.setVenueImageList(response.getVenueImages());
                venuePicInEventAdapter.refresh(response.getVenueImages());
                venuePicInEventAdapter.notifyDataSetChanged();
                Log.d("hnfklasnfklas", venuePicInEventAdapter.getItemCount() + " fetchVenueImages: " + response.getVenueImages().size());
            } else {
                binding.venueLayout.setVisibility(View.GONE);
                if (createEventDAO != null)
                    createEventDAO.setVenueImageList(null);
            }
        });
    }

    private void galleryAddPic() {
        try {
            Log.e("fire.....", "yes");
            Intent mediaScanIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            File f = new File(currentPhotoPath);
            Uri photoURI = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", f);
            mediaScanIntent.setData(photoURI);
            mContext.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPic() {
        int targetW = binding.myImg.getWidth();
        int targetH = binding.myImg.getHeight();

        Log.e("target W " + targetW, "target H" + targetH);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.e("photo W " + photoW, "photo H" + photoH);

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        try {
            ExifInterface ei = new ExifInterface(currentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    Bitmap rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        requestManager.load(currentPhotoPath).into(createViewDynamically(currentPhotoPath));
//        galleryPicAdapter.refresh(imageDaoList);
        //currentImageView.setImageBitmap(rotatedBitmap);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void getLocationPermission() {

        mLocationPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_CAMERA_PERM: {
                permissionUtils.handle(requestCode, grantResults);
                break;
            }

            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    private void choose() {
        Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickIntent.setType("image/*");
        //pickIntent.setAction(Intent.ACTION_GET_CONTENT);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        "com.ebabu.event365live.host.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
        }

        String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
        Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
        chooserIntent.putExtra
                (
                        Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePictureIntent}
                );
        startActivityForResult(chooserIntent, 222);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("called", "onstart");
    }

    @Override
    public void onDestroyView() {
        if (disposable != null) disposable.dispose();
        super.onDestroyView();
    }

    private void setSellingDate() {

        startEventDateFromApi = Utility.getDateMonthYearName(updateEventDao.getStart(), true);
        binding.textView59.setText(Utility.getDateMonthYearName(updateEventDao.getSellingStart(), true));
        binding.endDateTv2.setText(Utility.getDateMonthYearName(updateEventDao.getSellingEnd(), true));
        binding.startTimeTv2.setText(StringUtils.getTime(updateEventDao.getSellingStart()));
        binding.endTimeTv2.setText(StringUtils.getTime(updateEventDao.getSellingEnd()));

        binding.imageView52.setOnClickListener(v -> Dialogs.setMinMaxDate(getContext(), getTodayDate, binding.endDateTv.getText().toString(), binding.textView59, date -> {

            if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
                return;
            } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
                return;
            } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
                return;
            }
            binding.textView59.setText(date);
            binding.startTimeTv2.setText(getString(R.string.start_time));
            binding.endDateTv2.setText(getString(R.string.end_date));
            binding.endTimeTv2.setText(getString(R.string.end_time));
        }));

        binding.textView59.setOnClickListener(v -> {

            if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
                return;
            } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
                return;
            } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
                return;
            }

            Dialogs.setMinMaxDate(getContext(), getTodayDate, binding.endDateTv.getText().toString(), binding.textView59, new Dialogs.OnStartDateSelected() {
                @Override
                public void onSet(String date) {

                    binding.textView59.setText(date);
                    binding.startTimeTv2.setText(getString(R.string.start_time));
                    binding.endDateTv2.setText(getString(R.string.end_date));
                    binding.endTimeTv2.setText(getString(R.string.end_time));
                }
            });


        });

        binding.endDateImg2.setOnClickListener(v -> {


            if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
                return;
            } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
                return;
            } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
                return;
            }


            if (binding.textView59.getText().equals(getString(R.string.start_date)))
                Dialogs.toast(mContext, v, "Select start date first!");
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView59.getText().toString(), binding.endDateTv.getText().toString(), binding.textView59, date -> {

                    if (compareDate(binding.textView39.getText().toString(), date)) {
                        Dialogs.toast(getContext(), v, "Selling end date should not be grater than of end event date");
                        binding.endDateTv2.setText(getString(R.string.end_date));
                        return;
                    }

                    binding.endDateTv2.setText(date);
                });
        });
        binding.endDateTv2.setOnClickListener(v -> {

            if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
                return;
            } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
                return;
            } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
                Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
                return;
            }

            if (binding.textView59.getText().equals(getString(R.string.start_date)))
                Dialogs.toast(getContext(), v, "Select start date first!");
            else
                Dialogs.setMinMaxDate(getContext(), binding.textView59.getText().toString(), binding.endDateTv.getText().toString(), binding.textView59, date -> {
                    if (compareDate(binding.endDateTv.getText().toString(), date)) {
                        Dialogs.toast(getContext(), v, "Selling end date should not be grater than of end event date");
                        //binding.textView59.setText(getString(R.string.start_date));
                        //binding.startTimeTv2.setText(getString(R.string.start_time));
                        binding.endDateTv2.setText(getString(R.string.end_date));
                        //binding.endTimeTv2.setText(getString(R.string.end_time));
                        return;
                    }
                    binding.endDateTv2.setText(date);

                });
        });

        binding.startTimeImg2.setOnClickListener(this::setSellingStartTime);
        binding.startTimeTv2.setOnClickListener(this::setSellingStartTime);
        binding.endTimeImg2.setOnClickListener(this::setSellingEndTime);
        binding.endTimeTv2.setOnClickListener(this::setSellingEndTime);
    }

    public void clearVenue() {
        binding.venueLocEt.setText("");
        binding.tvSubVenue.setText("");
        binding.tvSubVenue.setVisibility(View.GONE);
    }

    private boolean compareDate(String dateOne, String dateTwo) {
        boolean flag = true;// selling date should be not grater than of start event date
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        try {
            Date d1 = sdf.parse(dateOne);
            Date d2 = sdf.parse(dateTwo);
            if (d1 != null && d2 != null) {
                if (!d1.before(d2)) {
                    flag = false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private void setSellingStartTime(View v) {

        if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
            Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
            return;
        } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
            Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
            return;
        } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
            Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
            return;
        }
        Dialogs.setTime(getContext(), binding.endTimeTv2, time -> {
            if (!binding.textView59.getText().equals(binding.startTimeTv2.getText().toString()))
                binding.startTimeTv2.setText(time);
            else if (compareSellingTime(time, binding.startTimeTv.getText().toString()))
                binding.startTimeTv2.setText(time);
            else {
                Dialogs.toast(getContext(), v, "Sell start time must be earlier than Event end time");
                binding.startTimeTv2.setText(getString(R.string.start_time));
            }
        });
    }

    private boolean compareSellingTime(String startTime, String endTime) {
        int startHour = getSellingHour(startTime);
        int endHour = getSellingHour(endTime);

        if (endHour > startHour) return true;

        else if (endHour == startHour) {
            int startMin = Integer.parseInt(startTime.substring(3, 5));
            int endMin = Integer.parseInt(endTime.substring(3, 5));
            return endMin > startMin;
        }
        return false;
    }

    private int getSellingHour(String time) {
        int startHour = Integer.parseInt(time.substring(0, 2));
        return time.matches("(.*)PM") && startHour < 12 ? (startHour + 12) : startHour;
    }

    private void setSellingEndTime(View v) {

        if (binding.endDateTv.getText().equals(getString(R.string.end_date))) {
            binding.endTimeTv2.setText(getString(R.string.end_time));
            Dialogs.toast(mContext, v, getString(R.string.eventEndDateRequired));
            return;
        } else if (binding.startTimeTv.getText().equals(getString(R.string.start_time))) {
            binding.endTimeTv2.setText(getString(R.string.end_time));
            Dialogs.toast(mContext, v, getString(R.string.eventStartTimeRequired));
            return;
        } else if (binding.endTimeTv.getText().equals(getString(R.string.end_time))) {
            binding.endTimeTv2.setText(getString(R.string.end_time));
            Dialogs.toast(mContext, v, getString(R.string.eventEndTimeRequired));
            return;
        }

        if (binding.startTimeTv.getText().toString().equals(getString(R.string.start_time)))
            Toast.makeText(getContext(), "Select start time first!", Toast.LENGTH_LONG).show();
        else
            Dialogs.setTime(getContext(), binding.endTimeTv2, time -> {

                if (compareSellingTime(time, binding.endTimeTv.getText().toString())) {
                    binding.endTimeTv2.setText(time);
                } else {
                    Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event end time");
                    binding.endTimeTv2.setText(getString(R.string.end_time));
                }

//                if (binding.endDateTv2.getText().equals(updateEventDao.getEnd())) {
//
//                    if (compareSellingTime(time, binding.endTimeTv.getText().toString()))
//                        binding.endTimeTv2.setText(time);
//                    else {
//                        Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event end time");
//                        binding.endTimeTv2.setText(getString(R.string.end_time));
//                    }
//                } else
//                    binding.endTimeTv2.setText(time);

//                if (!binding.textView59.getText().equals(binding.endDateTv2.getText().toString())) {
//
//
//                }

//                else {
//                    String startTime = binding.startTimeTv2.getText().toString();
//
//                    if (compareSellingTime(startTime, time))
//                        if (binding.endDateTv2.getText().equals(updateEventDao.getStart())) {
//                            if (compareTime(time, updateEventDao.getStart()))
//                                binding.endTimeTv2.setText(time);
//                            else {
//                                Dialogs.toast(getContext(), v, "Sell end time must be earlier than Event Start time");
//                                binding.endTimeTv2.setText(getString(R.string.end_time));
//                            }
//                        } else
//                            binding.endTimeTv2.setText(time);
//                    else {
//                        Dialogs.toast(getContext(), v, "Start time must be earlier than End time");
//                        binding.endTimeTv2.setText(getString(R.string.end_time));
//                    }
//                }
            });
    }

    private void moveItem(int oldPos, int newPos) {
        if (previousImages.size() > 0) {
            ImageDAO temp = previousImages.get(oldPos);
            previousImages.set(oldPos, previousImages.get(newPos));
            previousImages.set(newPos, temp);
            galleryPicAdapter.notifyItemChanged(oldPos);
            galleryPicAdapter.notifyItemChanged(newPos);
        } else if (imageDaoList.size() > 0) {
            ImageDAO temp = imageDaoList.get(oldPos);
            imageDaoList.set(oldPos, imageDaoList.get(newPos));
            imageDaoList.set(newPos, temp);
            galleryPicAdapter.notifyItemChanged(oldPos);
            galleryPicAdapter.notifyItemChanged(newPos);
        }
    }

    private void confirmPaidFreeDialog(View V) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_confirm_paid_free, null);

        TextView tvNext = (TextView) view.findViewById(R.id.tvNextBtn);
        ImageView ivClose = (ImageView) view.findViewById(R.id.ivClose);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGrpFreePaid);
        RadioButton radioBtnYes = (RadioButton) view.findViewById(R.id.radioBtnYes);
        RadioButton radioBtnNo = (RadioButton) view.findViewById(R.id.radioBtnNo);
        eventType = "Free";

        imageList = new ArrayList<>();
        for (ImageDAO imageDAO : createEventDAO.getImageDAOList()) {
            imageList.add(imageDAO.getVenueImages());
        }


        tvNext.setOnClickListener(v -> {

            if (eventType == null || eventType.equalsIgnoreCase("")) {
                Toast.makeText(activity, "Please select yes or no", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventType.equalsIgnoreCase("Paid")) {
                createEventDAO.setPaidType(eventType);
                alertDialog.dismiss();
                postEvent(V, "Paid");
            } else if (eventType.equalsIgnoreCase("Free")) {
                createEventDAO.setPaidType(eventType);
                alertDialog.dismiss();
                postEvent(V, "Free");

                //CreateEventDetailsPostRequest();

//                Navigation.findNavController(V).navigate(
//                        CreateEventFragmentDirections.actionCreateEventFragmentToCreateEventFullEvent()
//                                .setEventDAO(createEventDAO));
            }
        });

        ivClose.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioBtnYes) {
                    eventType = "Free";
                } else {
                    eventType = "Paid";
                }
            }
        });


        builder.setView(view);
        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private boolean isValidPhone = false;

    private void postEvent(View v, String type) {
        isValidPhone = false;

        createEventDAO.setHelplineNumber(null);
        createEventDAO.setTicketInfoWhenTicketsNotSelected(null);

        BottomSheetDialog dialog = new BottomSheetDialog(mContext, R.style.BottomSheetDialog);
        eventDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_post_event_dialog, null, false);
        dialog.setContentView(eventDialogBinding.getRoot());
        dialog.show();

        eventDialogBinding.ccp.registerCarrierNumberEditText(eventDialogBinding.etMobileNumber);
        eventDialogBinding.ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            isValidPhone = isValidNumber;
        });

        eventDialogBinding.cbAutoFill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    getHostDetail();
                }
            }
        });

        eventDialogBinding.tvConfirmBtn.setOnClickListener(v1 -> {
            String code = eventDialogBinding.ccp.getSelectedCountryCodeWithPlus();
            String mobileNumber = eventDialogBinding.etMobileNumber.getText().toString().trim();
            String address = eventDialogBinding.etAddress.getText().toString().trim();
            String website = eventDialogBinding.etWebsite.getText().toString().trim();
            String websiteOther = eventDialogBinding.etWebsiteOther.getText().toString().trim();

            if (eventDialogBinding.cbContact.isChecked() || eventDialogBinding.cbURL.isChecked()) {

                /*if (eventDialogBinding.cbContact.isChecked() && code.length() == 0) {
                    Toast.makeText(mContext, "Please select mobile country code.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && mobileNumber.length() == 0) {
                    Toast.makeText(mContext, "Please enter your mobile number.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && !isValidPhone) {
                    Toast.makeText(mContext, "Please enter valid mobile number.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && address.length() == 0) {
                    Toast.makeText(mContext, "Please enter your email address.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(eventDialogBinding.cbContact.isChecked() && !Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(address).matches()) {
                    Toast.makeText(mContext, "Please enter valid email address.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && (website.length()>0 && !URLUtil.isValidUrl(website))) {
                    Toast.makeText(mContext, "Please enter valid website URL in contact details.", Toast.LENGTH_LONG).show();
                    return;
                }*/
                if (eventDialogBinding.cbContact.isChecked()) {
                    boolean isValidForm = false;

                    if (!TextUtils.isEmpty(mobileNumber)) {
                        if (isValidPhone)
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid mobile number.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(address)) {
                        if (Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(address).matches())
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid email address.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(website)) {
                        if (URLUtil.isValidUrl(website))
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid website URL in contact details.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!isValidForm) {
                        Toast.makeText(mContext, "Please enter contact details.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (eventDialogBinding.cbURL.isChecked() && websiteOther.length() == 0) {
                    Toast.makeText(mContext, "Please enter valid website URL.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbURL.isChecked() && !URLUtil.isValidUrl(websiteOther)) {
                    Toast.makeText(mContext, "Please enter valid website URL.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!eventDialogBinding.cbContact.isChecked()) {
                    mobileNumber = "";
                    address = "";
                    website = "";
                }

                if (!eventDialogBinding.cbURL.isChecked())
                    websiteOther = "";

                if (!Utility.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_LONG).show();
                    return;
                }

                createEventDAO.setCountryCode(code);
                createEventDAO.setHostMobile(mobileNumber);
                createEventDAO.setHostAddress(address);
                createEventDAO.setWebsiteUrl(website);
                createEventDAO.setWebsiteUrlOther(websiteOther);

                dialog.dismiss();

                if (type.equalsIgnoreCase("Free"))
                    CreateEventDetailsPostRequest();
                else
                    paidEventPrice(v);
            } else
                Toast.makeText(mContext, "Please select contact details OR website link.", Toast.LENGTH_LONG).show();

        });
        eventDialogBinding.ivClose.setOnClickListener(v1 -> dialog.dismiss());

    }

    private void postEventUpdate(Date startDateUtil, Date endDateUtil, String additionalInfo2) {
        isValidPhone = false;

        BottomSheetDialog dialog = new BottomSheetDialog(mContext, R.style.BottomSheetDialog);
        eventDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.layout_post_event_dialog, null, false);
        dialog.setContentView(eventDialogBinding.getRoot());
        dialog.show();

        eventDialogBinding.ccp.registerCarrierNumberEditText(eventDialogBinding.etMobileNumber);
        eventDialogBinding.ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            isValidPhone = isValidNumber;
        });

        eventDialogBinding.title.setText(R.string.edit_event);

        if (!TextUtils.isEmpty(updateEventDao.getHostMobile()) && !TextUtils.isEmpty(updateEventDao.getHostAddress())) {
            eventDialogBinding.cbContact.setChecked(true);
            eventDialogBinding.ccp.setCountryForPhoneCode(Integer.parseInt(updateEventDao.getCountryCode().trim()));
            eventDialogBinding.etMobileNumber.setText(updateEventDao.getHostMobile());
            eventDialogBinding.etAddress.setText(updateEventDao.getHostAddress());
            eventDialogBinding.etWebsite.setText(updateEventDao.getWebsiteUrl());
        }

        if (!TextUtils.isEmpty(updateEventDao.getOtherWebsiteUrl())) {
            eventDialogBinding.cbURL.setChecked(true);
            eventDialogBinding.etWebsiteOther.setText(updateEventDao.getOtherWebsiteUrl());
        }

        eventDialogBinding.cbAutoFill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    getHostDetail();
                }
            }
        });

        eventDialogBinding.tvConfirmBtn.setOnClickListener(v1 -> {
            String code = eventDialogBinding.ccp.getSelectedCountryCodeWithPlus();
            String mobileNumber = eventDialogBinding.etMobileNumber.getText().toString().trim();
            String address = eventDialogBinding.etAddress.getText().toString().trim();
            String website = eventDialogBinding.etWebsite.getText().toString().trim();
            String websiteOther = eventDialogBinding.etWebsiteOther.getText().toString().trim();

            if (eventDialogBinding.cbContact.isChecked() || eventDialogBinding.cbURL.isChecked()) {
               /* if (eventDialogBinding.cbContact.isChecked() && code.length() == 0) {
                    Toast.makeText(mContext, "Please select mobile country code.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && mobileNumber.length() == 0) {
                    Toast.makeText(mContext, "Please enter your mobile number.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && !isValidPhone) {
                    Toast.makeText(mContext, "Please enter valid mobile number.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (eventDialogBinding.cbContact.isChecked() && address.length() == 0) {
                    Toast.makeText(mContext, "Please enter your email address.", Toast.LENGTH_LONG).show();
                    return;
                }

                if(eventDialogBinding.cbContact.isChecked() && !Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(address).matches()) {
                    Toast.makeText(mContext, "Please enter valid email address.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbContact.isChecked() && (website.length()>0 && !URLUtil.isValidUrl(website))) {
                    Toast.makeText(mContext, "Please enter valid website URL in contact details.", Toast.LENGTH_LONG).show();
                    return;
                }*/

                if (eventDialogBinding.cbContact.isChecked()) {
                    boolean isValidForm = false;

                    if (!TextUtils.isEmpty(mobileNumber)) {
                        if (isValidPhone)
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid mobile number.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(address)) {
                        if (Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(address).matches())
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid email address.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!TextUtils.isEmpty(website)) {
                        if (URLUtil.isValidUrl(website))
                            isValidForm = true;
                        else {
                            Toast.makeText(mContext, "Please enter valid website URL in contact details.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    if (!isValidForm) {
                        Toast.makeText(mContext, "Please enter contact details.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (eventDialogBinding.cbURL.isChecked() && websiteOther.length() == 0) {
                    Toast.makeText(mContext, "Please enter valid website URL.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (eventDialogBinding.cbURL.isChecked() && !URLUtil.isValidUrl(websiteOther)) {
                    Toast.makeText(mContext, "Please enter valid website URL.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!eventDialogBinding.cbContact.isChecked()) {
                    mobileNumber = "";
                    address = "";
                    website = "";
                }

                if (!eventDialogBinding.cbURL.isChecked())
                    websiteOther = "";

                if (!Utility.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, R.string.check_network, Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();
                updateEventAPI(startDateUtil, endDateUtil, additionalInfo2, code, mobileNumber, address, website, websiteOther);

            } else
                Toast.makeText(mContext, "Please select contact details OR website link.", Toast.LENGTH_LONG).show();

        });

        eventDialogBinding.ivClose.setOnClickListener(v1 -> dialog.dismiss());

    }

    private void paidEventPrice(View v) {
        loader.show("");

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("lastLoginTime", Utility.getSharedPreferencesString(getActivity(), Constants.LAST_LOGIN_TIME));

        Call call = apiInterface.paidEventPrice(jsonObject);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                loader.dismiss();

                Log.e("got result", "yes");
                Log.e("got result", "paidEventPrice response> " + response);

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());

                        String amount = OBJ.getJSONObject("data").getString("amount");
                        if (Integer.parseInt(amount) > 0)
                            Navigation.findNavController(v).navigate(CreateEventFragmentDirections.actionCreateEventFragmentToMinPaymentFragment().setEventDAO(createEventDAO));
                        else {
                            // createEventDAO.setPaidType("Free");
                            CreateEventDetailsPostRequest();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

    private void getHostDetail() {
        loader.show("");

        Call call = apiInterface.getHostDetail();

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                loader.dismiss();

                if (response.isSuccessful()) {

                    HostDetailResponse hostDetailResponse = new HostDetailResponse();

                    if (response.isSuccessful()) {
                        hostDetailResponse.setSuccess(true);
                        hostDetailResponse.setCode(response.code());
                        try {
                            JSONObject OBJ = new JSONObject(response.body().toString());
                            Gson gson = new Gson();
                            hostDetailResponse = gson.fromJson(OBJ.toString(), HostDetailResponse.class);

                            if (!TextUtils.isEmpty(hostDetailResponse.getData().getCountryCode()))
                                eventDialogBinding.ccp.setCountryForPhoneCode(Integer.parseInt(hostDetailResponse.getData().getCountryCode().trim()));

                            if (!TextUtils.isEmpty(hostDetailResponse.getData().getHostMobile()))
                                eventDialogBinding.etMobileNumber.setText(hostDetailResponse.getData().getHostMobile());

                            if (!TextUtils.isEmpty(hostDetailResponse.getData().getHostAddress()))
                                eventDialogBinding.etAddress.setText(hostDetailResponse.getData().getHostAddress());

                            if (!TextUtils.isEmpty(hostDetailResponse.getData().getWebsiteUrl()))
                                eventDialogBinding.etWebsite.setText(hostDetailResponse.getData().getWebsiteUrl());

                            if (!TextUtils.isEmpty(hostDetailResponse.getData().getOtherWebsiteUrl()))
                                eventDialogBinding.etWebsiteOther.setText(hostDetailResponse.getData().getOtherWebsiteUrl());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            hostDetailResponse.setCode(response.code());
                            JSONObject OBJ = new JSONObject(response.errorBody().string());
                            hostDetailResponse.setSuccess(OBJ.getBoolean(API.SUCCESS));
                            hostDetailResponse.setMessage(OBJ.getString(API.MESSAGE));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

    private void CreateEventDetailsPostRequest() {
        loader.show("");
        RequestBody amount = RequestBody.create(MediaType.parse("multipart/form-data"), "0");
        RequestBody eventType = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getEventType()));
        RequestBody categoryId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCategoryId()));

        RequestBody paymentId = RequestBody.create(MediaType.parse("multipart/form-data"), TextUtils.isEmpty(createEventDAO.getPaymentId()) ? "" : createEventDAO.getPaymentId());
        RequestBody hostMobile = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostMobile()));
        RequestBody hostAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getHostAddress()));
        RequestBody websiteUrl = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrl()));
        RequestBody websiteUrlOther = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getWebsiteUrlOther()));

        RequestBody subCategoryId = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getSubCategoryId());
        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getName());
        RequestBody eventOccurrenceType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getEventOccuranceType());
        JsonArray occorArray;
        if (createEventDAO.getOccurredOn().length() > 0) {
            String[] arr = createEventDAO.getOccurredOn().substring(1, createEventDAO.getOccurredOn().length() - 1).split(",");
            occorArray = new JsonArray(arr.length);
            for (String s : arr) occorArray.add(Integer.valueOf(s));
        } else {
            occorArray = new JsonArray(1);
            occorArray.add(0);
        }

        RequestBody eventOccuranceOn = RequestBody.create(MediaType.parse("multipart/form-data"), occorArray.toString());
        RequestBody startDate = null;
        RequestBody endDate = null;

        try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startDateUtil = sdf.parse(createEventDAO.getStartDate() + " " + createEventDAO.getStartTime());
                //startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(startDateUtil));
                startDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));

                Date endDateUtil = sdf.parse(createEventDAO.getEndDate() + " " + createEventDAO.getEndTime());
                //endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localToUTC(endDateUtil));
                endDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc());
        RequestBody description2 = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getDesc2());
        RequestBody paidType = RequestBody.create(MediaType.parse("multipart/form-data"), createEventDAO.getPaidType());

        RequestBody ticketInfoURL = null, helpLine = null;
        RequestBody sellingStartDate = null, sellingEndDate = null;
        RequestBody sellingStartTime = null, sellingEndTime = null;
//        RequestBody willEventAvailableRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(willEventAvailable));


        JsonArray free = null;
        JsonArray paidTicket = null;
        JsonArray paidSeat = null;
        JsonArray vipTicket = null;
        JsonArray vipSeat = null;

        String helplineNumber = createEventDAO.getHelplineNumber();
        String info = createEventDAO.getTicketInfoWhenTicketsNotSelected();

        if (info != null || helplineNumber != null) {
            if (info != null)
                ticketInfoURL = RequestBody.create(MediaType.parse("multipart/form-data"), info);
            if (helplineNumber != null)
                helpLine = RequestBody.create(MediaType.parse("multipart/form-data"), helplineNumber);

        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                if (!TextUtils.isEmpty(createEventDAO.getSellStartDate()) && !TextUtils.isEmpty(createEventDAO.getSellEndDate())) {
                    Date startDateUtil = sdf.parse(createEventDAO.getSellStartDate()+" "+createEventDAO.getSellStartTime());
                    // sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), startDateUtil.toString());
                    sellingStartDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(startDateUtil));
                    Date endDateUtil = sdf.parse(createEventDAO.getSellEndDate() + " " + createEventDAO.getSellEndTime());
                    sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), endDateUtil.toString());
                    sellingEndDate = RequestBody.create(MediaType.parse("multipart/form-data"), Utility.localFormat(endDateUtil));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (createEventDAO.isFreeTicketEnabled() == 1 && createEventDAO.getFreeTicketDaoList() != null && createEventDAO.getFreeTicketDaoList().size() > 0) {
                free = new JsonArray(createEventDAO.getFreeTicketDaoList().size());
                for (FreeTicketDao dao : createEventDAO.getFreeTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", StringUtils.capitalizeWord(dao.getTicketName()));
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("startSellingDate", startSellingDate);
                    object.addProperty("endSellingDate", endSellingDate);
                    object.addProperty("startSellingTime", startSellingTime);
                    object.addProperty("endSellingTime", endSellingTime);
                    free.add(object);
                }
            }

            if (createEventDAO.isRSVPTicketEnabled() == 1 && createEventDAO.getRsvpTicketDaoList() != null && createEventDAO.getRsvpTicketDaoList().size() > 0) {
                paidTicket = new JsonArray(createEventDAO.getRsvpTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getRsvpTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    paidTicket.add(object);
                }
            }


            if (createEventDAO.isVIPTicketEnabled() == 1 && createEventDAO.getVipTicketDaoList() != null && createEventDAO.getVipTicketDaoList().size() > 0) {
                vipTicket = new JsonArray(createEventDAO.getVipTicketDaoList().size());

                for (RSVPTicketDao dao : createEventDAO.getVipTicketDaoList()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getTicketName());
                    object.addProperty("totalQuantity", dao.getTotalTicketsQuantity());
                    object.addProperty("description", dao.getDesc().trim());
                    object.addProperty("pricePerTicket", dao.getPriceTicket());
                    vipTicket.add(object);
                }
            }


            if (createEventDAO.isVipSeatEnabled() == 1 && createEventDAO.getVipTableAndSeatingDaos() != null && createEventDAO.getVipTableAndSeatingDaos().size() > 0) {
                vipSeat = new JsonArray(createEventDAO.getVipTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getVipTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    vipSeat.add(object);
                }
            }

            if (createEventDAO.isRSVPSeatEnabled() == 1 && createEventDAO.getRsvpTableAndSeatingDaos() != null && createEventDAO.getRsvpTableAndSeatingDaos().size() > 0) {
                paidSeat = new JsonArray(createEventDAO.getRsvpTableAndSeatingDaos().size());

                for (TableAndSeatingDao dao : createEventDAO.getRsvpTableAndSeatingDaos()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ticketName", dao.getCategoryName());
                    object.addProperty("noOfTables", dao.getNoOfTables());
                    object.addProperty("totalQuantity", dao.getNoOfTables());
                    object.addProperty("personPerTable", dao.getPersonPerTable());
                    object.addProperty("pricePerTable", dao.getPricePerTable());
                    object.addProperty("pricePerTicket", dao.getPricePerTable() / dao.getPersonPerTable());
                    object.addProperty("description", dao.getDesc().trim());
                    paidSeat.add(object);
                }
            }
        }

        RequestBody venueAddress = null, venueLat = null, venueLongt = null, veneuName = null, venueId = null, countryCode = null, cityName = null, subVenue = null;

        if (createEventDAO.getLat() == null) {
            venueId = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueId()));
            if (createEventDAO.getSelectedSubVenues() != null && createEventDAO.getSelectedSubVenues().size() > 0) {
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < createEventDAO.getSelectedSubVenues().size(); i++) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("venueId", createEventDAO.getSelectedSubVenues().get(i).getVenueId());
                    jsonObject.addProperty("subVenueId", createEventDAO.getSelectedSubVenues().get(i).getSubVenueId());
                    jsonObject.addProperty("status", createEventDAO.getSelectedSubVenues().get(i).getStatus());
                    jsonArray.add(jsonObject);
                }
                subVenue = RequestBody.create(MediaType.parse("multipart/form-data"), jsonArray.toString());
            }
        } else {
            veneuName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueName()));
            venueAddress = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getVenueAddress()));
            venueLat = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLat()));
            venueLongt = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getLongt()));
            cityName = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCityName()));
            countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));
        }

        countryCode = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(createEventDAO.getCountryCode()));

        RequestBody freeJsonBody = null;
        if (free != null)
            freeJsonBody = RequestBody.create(MediaType.parse("multipart/form-data"), free.toString());

        RequestBody vipTicketBody = null;
        if (vipTicket != null)
            vipTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipTicket.toString());

        RequestBody vipSeatingBody = null;
        if (vipSeat != null)
            vipSeatingBody = RequestBody.create(MediaType.parse("multipart/form-data"), vipSeat.toString());

        RequestBody paidTicketBody = null;
        if (paidTicket != null)
            paidTicketBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidTicket.toString());

        RequestBody paidSeatBody = null;
        if (paidSeat != null)
            paidSeatBody = RequestBody.create(MediaType.parse("multipart/form-data"), paidSeat.toString());

        MultipartBody.Part[] image = null;
        if (imageList.size() > 0) {
            image = new MultipartBody.Part[imageList.size()];
            try {
                for (int index = 0; index < imageList.size(); index++) {
                    File file = new File(imageList.get(index));
                    Log.e("What i am uploading!", file.toString());
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part fileToUploadPart = MultipartBody.Part.createFormData("images", file.getName(), requestFile);
                    image[index] = fileToUploadPart;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Call call;

        if (createEventDAO.getPaidType().equalsIgnoreCase("Paid"))
        {
            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
            call = apiInterface.postEventPaid(amount, eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate,
                    // new parameters
                    paymentId,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);
        }
        else
        {
            RequestBody isEventPaid = RequestBody.create(MediaType.parse("multipart/form-data"), "0");
            call = apiInterface.postEventFree(eventType,
                    categoryId, subCategoryId,
                    name,
                    eventOccurrenceType, eventOccuranceOn,
                    startDate, endDate,
                    venueId, veneuName, venueAddress,
                    venueLat, venueLongt,
                    countryCode,
                    cityName,
                    description,
                    description2,
                    paidType,
                    vipTicketBody, vipSeatingBody, paidTicketBody, paidSeatBody, freeJsonBody, ticketInfoURL, helpLine, sellingStartDate, sellingEndDate, sellingStartTime, sellingEndTime,
                    hostMobile,
                    hostAddress,
                    websiteUrl,
                    websiteUrlOther,
                    isEventPaid,
                    subVenue,
                    image);

        }


        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                loader.dismiss();
                if (response.isSuccessful()) {
                    String msg = "";
                    try {
                        msg = new JSONObject(response.body().toString()).getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD, true);
                    Utility.setSharedPreferencesBoolean(getContext(), API.HOT_RELOAD_EVENTS, true);

                    App.createEventDAO = null;

                    Dialogs.showActionDialog(getContext(),
                            getString(R.string.app_name),
                            msg,
                            "Done",
                            v1 -> getActivity().finish()
                    );

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t)
            {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!update)
            setData();
    }

    private void setData() {
        try {

            selection = new StringBuilder();
            if (eventOccuranceType.contains("oneTime"))
            {
                startDate = binding.textView39.getText().toString();
                endDate = binding.endDateTv.getText().toString();
                startTime = binding.startTimeTv.getText().toString();
                endTime = binding.endTimeTv.getText().toString();
                createEventDAO.setStartDate(startDate);
                createEventDAO.setStartTime(startTime);
                createEventDAO.setEndDate(endDate);
                createEventDAO.setEndTime(endTime);
            }

            String name = binding.eventNameEt.getText().toString().trim();
            String selectedAvailability = binding.typeSubSpinner.getText().toString();

            //if(eventOccuranceType!=null) {
            if (eventOccuranceType.equalsIgnoreCase("daily")) {
                String sarr[] = selectedAvailability.split(", ");
                for (int i = 0; i < sarr.length; i++) {
                    if (i == 0) {
                        selection.append("[").append(StringUtils.getDayMonthNum(sarr[i]) + "");
                    } else
                        selection.append(",").append(StringUtils.getDayMonthNum(sarr[i]) + "");

                    if ((i + 1) == sarr.length)
                        selection.append("]");
                }
            }
            if (eventOccuranceType.equalsIgnoreCase("monthly")) {
                String sarr[] = selectedAvailability.split(", ");
                for (int i = 0; i < sarr.length; i++) {
                    if (i == 0) {
                        selection.append("[").append(sarr[i]);
                    } else
                        selection.append(",").append(sarr[i]);

                    if ((i + 1) == sarr.length)
                        selection.append("]");
                }
            }

            if (eventOccuranceType.equalsIgnoreCase("weekly")) {
                switch (selectedAvailability) {
                    case "Sunday":
                        selection.append("[7]");
                        break;
                    case "Monday":
                        selection.append("[1]");
                        break;
                    case "Tuesday":
                        selection.append("[2]");
                        break;
                    case "Wednesday":
                        selection.append("[3]");
                        break;
                    case "Thursday":
                        selection.append("[4]");
                        break;
                    case "Friday":
                        selection.append("[5]");
                        break;
                    case "Saturday":
                        selection.append("[6]");
                        break;
                }
            }
            //  }

            venueName = binding.venueLocEt.getText().toString().trim();
            String additionalInfo = binding.additionEt.getText().toString().trim();
            String additionalInfo2 = binding.additionEt2.getText().toString().trim();

            createEventDAO.setName(name);
            createEventDAO.setEventOccuranceType(eventOccuranceType);
            createEventDAO.setOccurredOn(selection.toString());

            createEventDAO.setVenueName(venueName);
//                createEventDAO.setLat(String.valueOf(latLongt.latitude));
//                createEventDAO.setLongt(String.valueOf(latLongt.longitude));
//            }

            createEventDAO.setDesc(additionalInfo);
            createEventDAO.setDesc2(additionalInfo2);
//            createEventDAO.setVenueAddress(binding.venueLocEt.getText().toString().trim());
            createEventDAO.setPaidType(eventType);
            createEventDAO.setImageList(imageList);
            createEventDAO.setImageDAOList(imageDaoList);

            if (App.createEventDAO != null) {
                App.createEventDAO = createEventDAO;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}