package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.VenueDAO;

import java.util.List;

import javax.inject.Inject;

public class SearchVenueAdapter extends RecyclerView.Adapter<SearchVenueAdapter.FuelAdapterViewHolder> {

    List<VenueDAO> list;
    VenueClickListener listener;

    @Inject
    RequestManager requestManager;

    public SearchVenueAdapter(VenueClickListener listener) {
        this.listener = listener;
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public FuelAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_search_venue, parent, false);
        return new FuelAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelAdapterViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> listener.onVenueClicked(list.get(position)));
        holder.venueName.setText(list.get(position).getName());
        holder.tvDesc.setText(list.get(position).getShortDescription());
        holder.tvMilesAway.setText("( " + list.get(position).getDistance() + " miles away )");
        if (list.get(position).getSubVenues() != null && list.get(position).getSubVenues().size() > 0)
            holder.tvSubVenue.setText(list.get(position).getSubVenues().size() + " Sub - Venue");
        else
            holder.tvSubVenue.setText("0 Sub - Venue");
    }

    @Override
    public int getItemCount() {
        return (list == null) ? 0 : list.size();
    }

    public class FuelAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView venueName, tvMilesAway, tvSubVenue, tvDesc;

        public FuelAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            venueName = itemView.findViewById(R.id.tvVenueName);
            tvMilesAway = itemView.findViewById(R.id.tvMilesAway);
            tvSubVenue = itemView.findViewById(R.id.tvSubVenue);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }

    public void refresh(List<VenueDAO> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public interface VenueClickListener {
        void onVenueClicked(VenueDAO dao);
    }
}
