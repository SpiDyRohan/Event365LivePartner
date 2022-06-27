package com.ebabu.event365live.host.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.ReviewAdapter;
import com.ebabu.event365live.host.databinding.SeeMoreDialogFragBinding;
import com.ebabu.event365live.host.entities.ReviewDao;
import com.ebabu.event365live.host.utils.DrawableUtils;
import com.ebabu.event365live.host.utils.StringUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class SeeMoreReviewsDialogFrag extends DialogFragment {
    private Activity activity;
    private Dialog dialog;
    private SeeMoreDialogFragBinding seeMoreDialogFragBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        seeMoreDialogFragBinding = DataBindingUtil.inflate(inflater, R.layout.see_more_dialog_frag, container, false);
        seeMoreDialogFragBinding.ivBackBtn.setOnClickListener(v -> dialog.dismiss());
        setupSeeMoreRecycler();
        return seeMoreDialogFragBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
    }

    private void setupSeeMoreRecycler() {
        seeMoreDialogFragBinding.seeMoreReviewRecycler.setLayoutManager(new LinearLayoutManager(activity));
        SeeMoreAdapter seeMoreAdapter = new SeeMoreAdapter();
        seeMoreDialogFragBinding.seeMoreReviewRecycler.setAdapter(seeMoreAdapter);
        seeMoreAdapter.notifyDataSetChanged();
    }

    static class SeeMoreAdapter extends RecyclerView.Adapter<SeeMoreAdapter.SeemMoreHolder> {
        private Context context;

        @NonNull
        @Override
        public SeemMoreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_review_list, parent, false);
            return new SeemMoreHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SeemMoreHolder holder, int position) {
            ReviewDao reviewDao = ReviewAdapter.list.get(position);
            holder.name.setText(reviewDao.getReviewer().getName());
            holder.reviewTime.setText(StringUtils.getDate(reviewDao.getUpdated_at()) + " " + StringUtils.getTime(reviewDao.getUpdated_at()));
            holder.reviews_tv.setText(reviewDao.getReviewText());
            holder.ratingBar.setRating(Float.parseFloat(reviewDao.getReviewStar()));

            String pc = reviewDao.getReviewer().getProfilePic();
            if (pc != null && pc.length() > 5)
                Glide.with(context).load(reviewDao.getReviewer().getProfilePic()).apply(RequestOptions.centerCropTransform()).into(holder.img);
            else
                Glide.with(context).load(DrawableUtils.getTempProfilePic(context, reviewDao.getReviewer().getName())).into(holder.img);

        }

        @Override
        public int getItemCount() {
            return ReviewAdapter.list.size();
        }

        static class SeemMoreHolder extends RecyclerView.ViewHolder {
            CircleImageView img;
            TextView name, reviews_tv, reviewTime;
            RatingBar ratingBar;

            SeemMoreHolder(@NonNull View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.circleImageView2);
                name = itemView.findViewById(R.id.name_tv);
                reviews_tv = itemView.findViewById(R.id.reviews_tv);
                reviewTime = itemView.findViewById(R.id.textView49);
                ratingBar = itemView.findViewById(R.id.ratingBar);
            }
        }
    }

}
