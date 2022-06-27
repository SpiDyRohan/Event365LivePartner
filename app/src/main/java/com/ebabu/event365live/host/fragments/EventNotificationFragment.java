package com.ebabu.event365live.host.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.EditEventDetails;
import com.ebabu.event365live.host.activity.Login;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentEventNotificationBinding;
import com.ebabu.event365live.host.entities.NotificationDAO;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventNotificationFragment extends Fragment {

    private FragmentEventNotificationBinding binding;
    private int dateDiff = -1;
    MyLoader loader;

    @Inject
    ApiInterface apiInterface;

    @Inject
    RequestManager requestManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_event_notification, container, false);
        App.getAppComponent().inject(this);
        init();
        return binding.getRoot();
    }

    private void init() {
        loader = new MyLoader(requireContext());
        loader.show("");
        fetchNotifications();
    }

    private void fetchNotifications() {
        apiInterface.getNotifications(50, 1, "1,2",1).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loader.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject OBJ = new JSONObject(response.body().toString());
                            JSONObject data = OBJ.getJSONObject(API.DATA);
                            JSONArray notificationJArray = data.getJSONArray("NotificationList");
                            for (int i = 0; i < notificationJArray.length(); i++) {
                                JSONObject jsonObject = notificationJArray.getJSONObject(i);
                                NotificationDAO dao = new Gson().fromJson(jsonObject.toString(), NotificationDAO.class);
                                setView(dao);
                            }
                            if (notificationJArray.length() == 0) handleEmptyState();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleEmptyState() {
        View itemView = getLayoutInflater().inflate(R.layout.notification_empty_state_layout, binding.parentLayout, false);
        binding.parentLayout.addView(itemView);
    }

    void setView(NotificationDAO dao) {
        View itemView = getLayoutInflater().inflate(R.layout.layout_notification_list, binding.parentLayout, false);
        if (dao.getSender() != null && dao.getSender().size() > 0) {
            NotificationDAO.User user = dao.getSender().get(0);
            ((TextView) itemView.findViewById(R.id.textView56)).setText(user.getName());
            if (user.getProfilePic() == null || user.getProfilePic().length() == 0)
                ((CircleImageView) itemView.findViewById(R.id.circleImageView3)).setImageBitmap(DrawableUtils.getTempProfilePic(requireContext(), user.getName()));
            else
                requestManager.load(user.getProfilePic()).into((CircleImageView) itemView.findViewById(R.id.circleImageView3));
        }
        ((TextView) itemView.findViewById(R.id.textView55)).setText(dao.getMsg());
        TextView dateTv = itemView.findViewById(R.id.date_tv);
        TextView timeTV = itemView.findViewById(R.id.textView57);
        int currDiff = (int) StringUtils.getDateDiffTemp(dao.getTime());

        if (dateDiff != currDiff) {
            if (currDiff == 0)
                dateTv.setText("Today");
            else if (currDiff == -1)
                dateTv.setText("Yesterday");

            else dateTv.setText(StringUtils.getDateByPatternLocal("dd MMM yyyy", dao.getTime()));
            dateDiff = currDiff;
        } else
            dateTv.setVisibility(View.GONE);

        currDiff = (int) StringUtils.getDateDiffInDays(dao.getTime());

        if (currDiff != 0) {
            timeTV.setText(Math.abs(currDiff - 1) + " day ago");
        } else {
            long min = Math.abs(StringUtils.getDateDiffInMin(dao.getTime()));
            if (min < 60)
                timeTV.setText(min + " minutes ago");
            else
                timeTV.setText((min / 60) + " hours ago");
        }

        itemView.setOnClickListener(v -> {
            if (dao.getType() != null) {
                if (dao.getType().equalsIgnoreCase("eventOfInterest") || dao.getType().equalsIgnoreCase("eventFav") || dao.getType().equalsIgnoreCase("hostTicketBooked") || dao.getType().equalsIgnoreCase("Invited") || dao.getType().equalsIgnoreCase("checkIn") || dao.getType().equalsIgnoreCase("eventReview")) {
                    navigateToScreen(EditEventDetails.class, dao.getEventId());
                } else if ((dao.getType().equalsIgnoreCase("editMember"))) {
                    Intent intent = new Intent(requireContext(), Login.class);
                    startActivity(intent);
                }
            }
        });

        binding.parentLayout.addView(itemView);
    }

    private void navigateToScreen(Class<?> className, int getEventId) {
        Intent intent = new Intent(requireContext(), className);
        intent.putExtra(API.ID, getEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
