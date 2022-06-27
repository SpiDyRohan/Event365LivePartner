
package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.MDaysAvailable;

import java.util.List;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.FuelAdapterViewHolder> {

    List<MDaysAvailable> list;
    public DaysAdapter() {
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public FuelAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subvenue_item, parent, false);
        return new FuelAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelAdapterViewHolder holder, int position) {
        holder.venueHeading.setVisibility(View.GONE);
        String dayStr = "";
        switch (list.get(position).getWeekDayName()) {
            case 1:
                dayStr = "Monday";
                break;
            case 2:
                dayStr = "Tuesday";
                break;
            case 3:
                dayStr = "Wednesday";
                break;
            case 4:
                dayStr = "Thursday";
                break;
            case 5:
                dayStr = "Friday";
                break;
            case 6:
                dayStr = "Saturday";
                break;
            case 7:
                dayStr = "Sunday";
                break;
        }
        holder.venueName.setText(dayStr + " from " + list.get(position).getFromTime() + " to " + list.get(position).getToTime());
    }

    @Override
    public int getItemCount() {
        return (list == null) ? 0 : list.size();
    }

    public class FuelAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView venueHeading, venueName;

        public FuelAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            venueHeading = itemView.findViewById(R.id.tvSubVenueHeading);
            venueName = itemView.findViewById(R.id.tvSubVenueName);
        }
    }

    public void refresh(List<MDaysAvailable> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
