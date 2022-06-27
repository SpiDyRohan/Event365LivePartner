package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.ReviewDao;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.StringUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    public static List<ReviewDao> list;

    Context context;
    @Inject
    RequestManager requestManager;

    public ReviewAdapter() {
        App.getAppComponent().inject(this);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_review_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ReviewDao reviewDao = list.get(position);
        holder.name.setText(reviewDao.getReviewer().getName());
        holder.reviewTime.setText(StringUtils.getDate(reviewDao.getUpdated_at()) + " " + StringUtils.getTimeReview(reviewDao.getUpdated_at()));
        holder.reviews_tv.setText(reviewDao.getReviewText());
        holder.ratingBar.setRating(Float.parseFloat(reviewDao.getReviewStar()));

        String pc = reviewDao.getReviewer().getProfilePic();
        if (pc != null && pc.length() > 5)
            requestManager.load(reviewDao.getReviewer().getProfilePic()).into(holder.img);
        else
            requestManager.load(DrawableUtils.getTempProfilePic(context, reviewDao.getReviewer().getName())).into(holder.img);


    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img;
        TextView name, reviews_tv, reviewTime;
        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.circleImageView2);
            name = itemView.findViewById(R.id.name_tv);
            reviews_tv = itemView.findViewById(R.id.reviews_tv);
            reviewTime = itemView.findViewById(R.id.textView49);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

    public void refresh(List<ReviewDao> reviews) {
        list = reviews;
        notifyDataSetChanged();
    }
}
