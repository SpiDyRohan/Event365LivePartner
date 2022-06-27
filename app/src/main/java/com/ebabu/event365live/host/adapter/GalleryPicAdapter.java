package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.ImageDAO;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GalleryPicAdapter extends RecyclerView.Adapter<GalleryPicAdapter.MyViewHolder> {

    List<ImageDAO> eventImages;

    @Inject
    RequestManager requestManager;

    @Inject
    Context context;

    private DeleteEventImage deleteEventImage;
    private boolean isPrimaryImg;

    public GalleryPicAdapter(boolean isPrimaryImg, DeleteEventImage deleteEventImage) {
        App.getAppComponent().inject(this);
        this.deleteEventImage = deleteEventImage;
        this.isPrimaryImg = isPrimaryImg;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_dynamic_image_editable, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (isPrimaryImg) {
            holder.ivMove.setVisibility(View.VISIBLE);
            if (position == 0) {
                holder.materialCardView.setStrokeColor(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBlueColor)));
                holder.materialCardView.setStrokeWidth((int) context.getResources().getDimension(R.dimen._2sdp));
            } else
                holder.materialCardView.setStrokeColor(null);
        } else {
            holder.ivMove.setVisibility(View.GONE);
            holder.materialCardView.setStrokeColor(null);
        }

        if (eventImages != null && eventImages.size() > 0) {
            holder.ivDelete.setTag(eventImages.get(position).getVenueImages());
            requestManager.load(eventImages.get(position).getVenueImages()).into(holder.imageView);
        }

//        holder.itemView.setOnClickListener(view -> {
//            if (position == 0) {
//                Intent intent = new Intent(context, ImageCropperActivity.class);
//                intent.putExtra("uri", eventImages.get(0).getVenueImages());
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
//        });

        holder.ivDelete.setOnClickListener(v -> deleteEventImage.deleteImage(v.getTag().toString(), position));
    }

    @Override
    public int getItemCount() {
        return eventImages == null ? 0 : eventImages.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, ivMove, ivDelete;
        MaterialCardView materialCardView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.myImg);
            ivMove = itemView.findViewById(R.id.ivMove);
            materialCardView = itemView.findViewById(R.id.materialCardView);
            ivDelete = itemView.findViewById(R.id.del_img);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void refresh(List<ImageDAO> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isPrimary()) {
                Collections.swap(list, list.indexOf(list.get(i)), 0);
            }
        }
        eventImages = list;
        notifyDataSetChanged();
    }

    public interface DeleteEventImage {
        void deleteImage(String s, int position);
    }
}
