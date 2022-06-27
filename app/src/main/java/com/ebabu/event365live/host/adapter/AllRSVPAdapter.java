package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.CheckedInDao;
import com.ebabu.event365live.host.utils.DrawableUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AllRSVPAdapter extends RecyclerView.Adapter<AllRSVPAdapter.CheckedRSVPHolder> {
    private List<CheckedInDao> list;
    private UserClickedListener listener;
    public AllRSVPAdapter(UserClickedListener listener) {
        this.listener=listener;
        App.getAppComponent().inject(this);
    }
    @Inject
    RequestManager requestManager;
    @Inject
    Context context;

    @NonNull
    @Override
    public CheckedRSVPHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_rsvp_layout,parent,false);
        return new CheckedRSVPHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckedRSVPHolder holder, int position) {
        CheckedInDao dao=list.get(position);
       /* vipTableSeating

        vipNormal ----> For Vip
        regularTableSeating ----> For Table/Seating
        regularNormal ---> For RSVp
        regularPaid ---> For Regular paid
        freeNormal ---> For Regular Free*/

        if(dao.getTicketType().equalsIgnoreCase("freeNormal")){
            holder.priceImg.setVisibility(View.GONE);
            holder.priceTV.setVisibility(View.GONE);
        }else {
            int ticketCount=0;
            if(dao.getTicket_number_booked_rel()!=null && dao.getTicket_number_booked_rel().size()>0) {
                for (int i=0;i<dao.getTicket_number_booked_rel().size();i++){
                    if(dao.getTicket_number_booked_rel().get(i).getStatus().equalsIgnoreCase("booked"))
                        ticketCount++;
                }
            }
            String amout= String.valueOf((int) (Float.parseFloat(dao.getPricePerTicket())*ticketCount));
            holder.priceTV.setText("$ " +amout);

            holder.priceImg.setVisibility(View.VISIBLE);
            holder.priceTV.setVisibility(View.VISIBLE);
            if(dao.getTicketType().equalsIgnoreCase("regularTableSeating"))
                holder.priceImg.setImageResource(R.drawable.table_logo);
            else if(dao.getTicketType().equalsIgnoreCase("vipNormal"))
                holder.priceImg.setImageResource(R.drawable.vip_logo);
            else
                holder.priceImg.setImageResource(R.drawable.regular_logo);
        }

        if(dao.getUsers()!=null) {
            if (dao.getUsers().getProfilePic() == null || dao.getUsers().getProfilePic().length() < 5) {
                Bitmap bitmap = DrawableUtils.getTempProfilePic(context, dao.getUsers().getName());
                requestManager.load(bitmap).into(holder.img);
            } else if (dao.getUsers().getProfilePic() != null) {
                requestManager.load(dao.getUsers().getProfilePic()).into(holder.img);
            }

            holder.nameTv.setText(dao.getUsers().getName());
        }
    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    class CheckedRSVPHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        ImageView priceImg;
        TextView nameTv,priceTV;
        LinearLayout cv;
        public CheckedRSVPHolder(@NonNull View itemView){
            super(itemView);
            img=itemView.findViewById(R.id.img);
            priceImg=itemView.findViewById(R.id.price_img);
            nameTv=itemView.findViewById(R.id.ivShowName);
            priceTV=itemView.findViewById(R.id.tvShowPrice);
            cv=itemView.findViewById(R.id.cv);

            cv.setOnClickListener(v-> listener.onClicked(list.get(getAdapterPosition()).getUsers().getId()));


        }
    }

    public void refresh(List<CheckedInDao> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public interface UserClickedListener{
        void onClicked(int id);
    }

}
