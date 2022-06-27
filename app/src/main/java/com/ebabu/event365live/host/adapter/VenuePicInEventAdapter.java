package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class VenuePicInEventAdapter extends RecyclerView.Adapter<VenuePicInEventAdapter.MyViewHolder> {

    List<String> eventImages = new ArrayList<>();
    private final boolean fromCreate;

    @Inject
    RequestManager requestManager;

    @Inject
    Context context;

    public VenuePicInEventAdapter(boolean fromCreate) {
        App.getAppComponent().inject(this);
        this.fromCreate = fromCreate;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_gallery_img, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (fromCreate) {
            if (position == 0) {
                holder.materialCardView.setStrokeColor(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBlueColor)));
                holder.materialCardView.setStrokeWidth((int) context.getResources().getDimension(R.dimen._2sdp));
            } else
                holder.materialCardView.setStrokeColor(null);
            holder.ivMove.setVisibility(View.VISIBLE);
        } else {
            holder.ivMove.setVisibility(View.GONE);
        }
        requestManager.load(eventImages.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return eventImages == null ? 0 : eventImages.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, ivMove;
        MaterialCardView materialCardView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            ivMove = itemView.findViewById(R.id.ivMove);
            materialCardView = itemView.findViewById(R.id.materialCardView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void refresh(List<String> list) {
        eventImages = list;
        notifyDataSetChanged();
    }
}
