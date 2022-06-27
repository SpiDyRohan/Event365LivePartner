package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.TicketBookedRelation;
import com.ebabu.event365live.host.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CheckedInRSVPAdapter extends RecyclerView.Adapter<CheckedInRSVPAdapter.CheckedRSVPHolder> {

    private UserClickedListener listener;
    private List<TicketBookedRelation> list=new ArrayList<>();
    public CheckedInRSVPAdapter(UserClickedListener listener) {
        this.listener = listener;
        App.getAppComponent().inject(this);
    }
    @Inject
    RequestManager requestManager;
    @Inject
    Context context;

    @NonNull
    @Override
    public CheckedRSVPHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_rsvp_layout, parent, false);
        return new CheckedRSVPHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckedRSVPHolder holder, int position) {
        TicketBookedRelation itemDate=list.get(position);
        if (itemDate.getProfilePic() == null || itemDate.getProfilePic().length() < 5) {
            requestManager.load(DrawableUtils.getTempProfilePic(context, itemDate.getName())).into(holder.img);
        } else {
            requestManager.load(itemDate.getProfilePic()).into(holder.img);
        }
        holder.nameTv.setText(itemDate.getName());

        if(itemDate.getTicketType().equalsIgnoreCase("freeNormal")){
            holder.imgLogo.setVisibility(View.GONE);
            holder.priceTV.setVisibility(View.GONE);
        }else {
            holder.imgLogo.setVisibility(View.VISIBLE);
            holder.priceTV.setVisibility(View.VISIBLE);
            holder.priceTV.setText("$ " + itemDate.getPricePerTicket());
            if(itemDate.getTicketType().equalsIgnoreCase("regularTableSeating"))
                holder.imgLogo.setImageResource(R.drawable.table_logo);
            else if(itemDate.getTicketType().equalsIgnoreCase("vipNormal"))
                holder.imgLogo.setImageResource(R.drawable.vip_logo);
            else
                holder.imgLogo.setImageResource(R.drawable.regular_logo);

            if(itemDate.getStatus().equalsIgnoreCase("booked"))
                holder.priceTV.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            else
                holder.priceTV.setTextColor(context.getResources().getColor(R.color.redColor));
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CheckedRSVPHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        ImageView imgLogo;
        TextView nameTv,priceTV;
        LinearLayout cv;

        public CheckedRSVPHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.img);
            imgLogo=itemView.findViewById(R.id.price_img);
            nameTv=itemView.findViewById(R.id.ivShowName);
            priceTV=itemView.findViewById(R.id.tvShowPrice);
            cv=itemView.findViewById(R.id.cv);
            cv.setOnClickListener(v -> listener.onClicked(list.get(getAdapterPosition()).getUserId()));
        }
    }

    public void refresh(List<TicketBookedRelation> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public interface UserClickedListener {
        void onClicked(int id);
    }
}
