package com.ebabu.event365live.host.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.EventDAO;
import com.ebabu.event365live.host.entities.EventType;
import com.ebabu.event365live.host.utils.StringUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder>
{

    List<EventDAO> list;
    private EventClickListener listener;
    EventType type;
    String from;

    public EventAdapter(EventType type, EventClickListener listener, String from) {
        this.listener = listener;
        this.type = type;
        this.from = from;
        App.getAppComponent().inject(this);
    }

    @Inject
    RequestManager requestManager;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if (from.equalsIgnoreCase("home"))
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_event_list, parent, false);
        else
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_upcoming_past, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        EventDAO dao = list.get(position);

        if(dao.getImageShow().isEmpty())
            holder.img.setImageResource(R.drawable.e_dummy);
        else{
            requestManager
                    .load(dao.getImageShow())
                    .apply(new RequestOptions().override(300, 300))
                    .into(holder.img);
        }

        if (type == EventType.PAST || type == EventType.HOME) {
            holder.deleteBtn.setVisibility(View.GONE);
            holder.editBtn.setVisibility(View.GONE);
        }

        holder.titleTV.setText(StringUtils.capitalizeWord(dao.getName()));
        String date = StringUtils.getDateLocal(dao.getStartDate());
        String time;

        long daysToGo = StringUtils.getDateDiffLocal(dao.getStartDate());
        Log.e("days to go", daysToGo + "");
        if (daysToGo <= 0) {
            long diff = StringUtils.getDateDiffLocal(dao.getEndDate());
            Log.e("diff.........", diff + "");
            if (diff >= 0)
                time = "Ongoing";
            else
                time = "Ended at " + StringUtils.getTimeLocal(dao.getEndDate());
        } else if (daysToGo > 0)
            time = "Starts at " + StringUtils.getTimeLocal(dao.getStartDate()) + " - " + (daysToGo) + " days to go";
        else
            time = "Starts at " + StringUtils.getTimeLocal(dao.getStartDate());


        /*if(type==EventType.PAST) {
            long daysToGo= StringUtils.getDateDiff(dao.getEndDate());
            if(daysToGo>0)
                time="Ongoing";
            else
            time = "Ended at " + StringUtils.getTime(dao.getEndDate());
        }
        else
        time="Starts at "+ StringUtils.getTime(dao.getStartDate());*/

        /*if(type!= EventType.PAST){
            long daysToGo= StringUtils.getDateDiff(dao.getStartDate());
            if(daysToGo>0)
                time=time.concat(" - "+daysToGo+" days to go");
        }*/

        if (type != EventType.PAST) {
            holder.time_tv.setText(time);
        } else {
            holder.time_tv.setVisibility(View.GONE);
        }


        holder.dateTV.setText(date.split(" ")[0]);
        try {
            holder.month_tv.setText(date.split(" ")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(v -> listener.eventClicked(list.get(position)));
        holder.img.setOnClickListener(v -> listener.eventClicked(list.get(position)));
        holder.deleteBtn.setOnClickListener(v -> listener.deleteEvent(list.get(position).getId()));
        holder.editBtn.setOnClickListener(v -> listener.editEvent(list.get(position).getId()));

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleTV, time_tv, dateTV, month_tv;
        LinearLayout deleteBtn, editBtn;
        ImageView img;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            titleTV = itemView.findViewById(R.id.textView15);
            time_tv = itemView.findViewById(R.id.time_tv);
            dateTV = itemView.findViewById(R.id.date_tv);
            month_tv = itemView.findViewById(R.id.month_tv);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            editBtn = itemView.findViewById(R.id.edit_btn);
            img = itemView.findViewById(R.id.img);

        }
    }

    public void refresh(List<EventDAO> list) {
        this.list = list;

        for(int m=0;m<list.size();m++){
            list.get(m).setImageShow("");
            if (list.get(m).getEventImages() != null && list.get(m).getEventImages().size() > 0) {
                for (int i = 0; i < list.get(m).getEventImages().size(); i++) {
                    if(i==0)
                        list.get(m).setImageShow(list.get(m).getEventImages().get(i).getEventImage());
                    if (list.get(m).getEventImages().get(i).isPrimary())
                        list.get(m).setImageShow(list.get(m).getEventImages().get(i).getEventImage());
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface EventClickListener {
        void eventClicked(EventDAO dao);

        default void deleteEvent(int id) {
        }

        default void editEvent(int id) {

        }
    }
}
