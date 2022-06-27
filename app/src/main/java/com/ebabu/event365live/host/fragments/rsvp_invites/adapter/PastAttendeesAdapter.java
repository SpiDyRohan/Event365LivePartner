package com.ebabu.event365live.host.fragments.rsvp_invites.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.PastAttendeesInviteLayoutBinding;
import com.ebabu.event365live.host.fragments.rsvp_invites.model.AttendeesData;
import com.ebabu.event365live.host.utils.Utility;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class PastAttendeesAdapter extends RecyclerView.Adapter<PastAttendeesAdapter.InviteRsvpHolder> {

    @Inject
    RequestManager requestManager;
    List<AttendeesData> list;
    private Context context;
    private LayoutInflater layoutInflater;

    public PastAttendeesAdapter(Context context) {
        this.context = context;
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public InviteRsvpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        PastAttendeesInviteLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.past_attendees_invite_layout, parent, false);
        return new InviteRsvpHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull InviteRsvpHolder holder, int position) {
        PastAttendeesInviteLayoutBinding binding = holder.binding;
        AttendeesData dao = list.get(position);
        binding.tvEventName.setText(dao.getName());
        binding.tvEventDate.setText(Utility.getDateFormat(dao.getStart(), false));

        if(dao.isSelectAll()){
            binding.tvSelectAll.setText("Deselect All");
        }else {
            binding.tvSelectAll.setText("Select All");
        }

        binding.tvSelectAll.setOnClickListener(view -> {
            for (int i = 0; i < dao.getTicketBooked().size() ; i++) {
                if(dao.isSelectAll()) {
                    list.get(position).getTicketBooked().get(i).setChecked(false);
                }else {
                    list.get(position).getTicketBooked().get(i).setChecked(true);
                }
            }

            if(dao.isSelectAll()) {
                dao.setSelectAll(false);
            }else {
                dao.setSelectAll(true);
            }
            notifyDataSetChanged();

        });

        layoutInflater = LayoutInflater.from(context);
        Log.v("PastAttend","page>adapter "  + " > " +list.size() );
        Log.v("PastAttend","page>adapter "  + " > " +dao.getName() );
        binding.llInviteData.removeAllViews();
        for (int i = 0; i < dao.getTicketBooked().size() ; i++) {

            View pastAttendees = layoutInflater.inflate(R.layout.past_attendees_layout, binding.llInviteData, false);


            ImageView imageView = pastAttendees.findViewById(R.id.img);
            TextView nameTv = pastAttendees.findViewById(R.id.name_tv);
            TextView thumbTv = pastAttendees.findViewById(R.id.thumb_tv);
            CheckBox checkBox = pastAttendees.findViewById(R.id.checkbox);

            nameTv.setText(dao.getTicketBooked().get(i).getUsers().getName());

            requestManager.load(R.mipmap.circle_color_icon).into(imageView);thumbTv.
                    setText(String.valueOf(dao.getTicketBooked().get(i).getUsers().getName().charAt(0)).toUpperCase());


            checkBox.setChecked(dao.getTicketBooked().get(i).isChecked());

            int finalI = i;
            checkBox.setOnClickListener(v -> {
                list.get(position).getTicketBooked().get(finalI).setChecked(checkBox.isChecked());
                if(dao.isSelectAll()){
                    if(!checkBox.isChecked()){
                        binding.tvSelectAll.setText("Select All");
                        dao.setSelectAll(false);
                    }
                }
            });

            binding.llInviteData.addView(pastAttendees);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void refresh(List<AttendeesData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    class InviteRsvpHolder extends RecyclerView.ViewHolder {
        PastAttendeesInviteLayoutBinding binding;

        InviteRsvpHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
