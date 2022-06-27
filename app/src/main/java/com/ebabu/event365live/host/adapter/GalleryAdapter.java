package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.EventImages;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    public static List<EventImages> eventImages;
    ImageClickListener listener;

    @Inject RequestManager requestManager;

    public GalleryAdapter(ImageClickListener listener) {
        App.getAppComponent().inject(this);
        this.listener=listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_gallery_img,parent,false);
        return  new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        requestManager.load(eventImages.get(position).getEventImage()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return eventImages==null?0:eventImages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView=itemView.findViewById(R.id.img);

            imageView.setOnClickListener(v->listener.onClick(getAdapterPosition(),eventImages));
        }
    }

    public void refresh(List<EventImages> list){
        eventImages=list;
        notifyDataSetChanged();
    }

    public interface ImageClickListener{
        void onClick(int position,List<EventImages> list);
    }
}
