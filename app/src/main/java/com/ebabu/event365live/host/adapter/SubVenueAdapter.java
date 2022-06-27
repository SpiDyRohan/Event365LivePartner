
package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.SubVenueDao;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubVenueAdapter extends RecyclerView.Adapter<SubVenueAdapter.FuelAdapterViewHolder> {

    List<SubVenueDao> list;

    public SubVenueAdapter() {
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
        holder.venueHeading.setText("Sub - Venue " + (position + 1));
        holder.venueName.setText(list.get(position).getSubVenueName() + " ( " + list.get(position).getSubVenueCapacity() + " ) " );
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

    public void refresh(List<SubVenueDao> list) {
        this.list = list;
        notifyDataSetChanged();
    }
}
