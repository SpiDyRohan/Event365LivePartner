package com.ebabu.event365live.host.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.VenueDAO;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.FuelAdapterViewHolder> {

    List<VenueDAO> list;
    VenueClickListener listener;

    @Inject
    RequestManager requestManager;

    public VenueAdapter(VenueClickListener listener){
        this.listener=listener;
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public FuelAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_venue,parent,false);
        return new FuelAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelAdapterViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v->listener.onVenueClicked(list.get(position)));
        holder.venueTitle.setText(list.get(position).getName());

        Log.e("WIG",list.get(position).toString());

        requestManager
                .load(list.get(position).getImgPath())
                .into(holder.imgVenue);
    }

    @Override
    public int getItemCount() {
        return (list==null)? 0: list.size();
    }

    public class FuelAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView imgVenue;
        TextView venueTitle;
        public FuelAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVenue=itemView.findViewById(R.id.venue_pic);
            venueTitle=itemView.findViewById(R.id.venue_title);
        }
    }
    public void refresh(List<VenueDAO> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public interface VenueClickListener{
        void onVenueClicked(VenueDAO dao);

    }
}
