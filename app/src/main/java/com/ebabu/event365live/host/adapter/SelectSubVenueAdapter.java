package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.SubVenueDao;

import java.util.List;

public class SelectSubVenueAdapter extends RecyclerView.Adapter<SelectSubVenueAdapter.FuelAdapterViewHolder> {

    List<SubVenueDao> list;

    public SelectSubVenueAdapter() {
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public FuelAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_sub_venue_item, parent, false);
        return new FuelAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelAdapterViewHolder holder, int position) {
        holder.venueName.setText(list.get(position).getSubVenueName() + " ( " + list.get(position).getSubVenueCapacity() + " ) ");
        if (list.get(position).getIsBooked().equalsIgnoreCase("yes")) {
            holder.checkBox.setEnabled(false);
            holder.checkBox.setAlpha(0.7f);
            holder.venueName.setAlpha(0.7f);
        }
        if (list.get(position).isSelected()) {
            holder.checkBox.setChecked(true);
        }
        holder.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            list.get(position).setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return (list == null) ? 0 : list.size();
    }

    public class FuelAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView venueName;
        CheckBox checkBox;

        public FuelAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            venueName = itemView.findViewById(R.id.tvSubVenueName);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    public void refresh(List<SubVenueDao> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
