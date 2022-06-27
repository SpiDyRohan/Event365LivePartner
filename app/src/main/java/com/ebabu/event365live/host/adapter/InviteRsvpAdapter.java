package com.ebabu.event365live.host.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.UserDAO;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class InviteRsvpAdapter extends RecyclerView.Adapter<InviteRsvpAdapter.InviteRsvpHolder>{

    @Inject
    RequestManager requestManager;

    List<UserDAO> list;

    public InviteRsvpAdapter(){

        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public InviteRsvpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_rsvp_custom_layout,parent,false);
        return new InviteRsvpHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteRsvpHolder holder, int position) {

        UserDAO dao=list.get(position);
        holder.nameTv.setText(dao.getName());
        holder.emailTv.setText(dao.getEmail());
        if(dao.getProfilePic()!=null && dao.getProfilePic().length()>10) {
            requestManager.load(dao.getProfilePic()).into(holder.imageView);
            holder.thumbTv.setText("");
        }
        else{
            requestManager.load(R.mipmap.circle_color_icon).into(holder.imageView);
            holder.thumbTv.setText(String.valueOf(dao.getName().charAt(0)).toUpperCase());
        }

        holder.checkBox.setChecked(dao.isChecked());

        holder.checkBox.setOnClickListener(v->list.get(position).setChecked(holder.checkBox.isChecked()));

    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    class InviteRsvpHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView nameTv,emailTv,thumbTv;
        CheckBox checkBox;
        InviteRsvpHolder(@NonNull View itemView){
            super(itemView);
            imageView=itemView.findViewById(R.id.img);
            nameTv=itemView.findViewById(R.id.name_tv);
            emailTv=itemView.findViewById(R.id.email_tv);
            thumbTv=itemView.findViewById(R.id.thumb_tv);
            checkBox=itemView.findViewById(R.id.checkbox);
        }
    }

    public void refresh(List<UserDAO> list){
        this.list=list;
        notifyDataSetChanged();
    }
}
