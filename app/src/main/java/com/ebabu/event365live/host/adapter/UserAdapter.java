package com.ebabu.event365live.host.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.utils.DrawableUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    List<UserDAO> list;
    UserClickListener userClickListener;
    Context context;
    public UserAdapter(UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_users,parent,false);
        context=parent.getContext();
        return  new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.editImg.setOnClickListener(v->userClickListener.clickedUser(list.get(position),"edit"));
        holder.delImg.setOnClickListener(v->userClickListener.clickedUser(list.get(position),"delete"));

        UserDAO dao=list.get(position);
        holder.nameTv.setText(dao.getName());
        holder.typeTv.setText(dao.getUserType());

        if(dao.getProfilePic()==null){
            Glide.with(context).load(DrawableUtils.getTempProfilePic(context,dao.getName())).into(holder.profileImg);
        }
        else
            Glide.with(context).load(dao.getProfilePic()).into(holder.profileImg);

        holder.editImg.setVisibility(View.GONE);
        holder.delImg.setVisibility(View.GONE);

        if(dao.getId()!=dao.getCreatedBy()){
            holder.editImg.setVisibility(View.VISIBLE);
            holder.delImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nameTv,typeTv;
        ImageView editImg,delImg;
        CircleImageView profileImg;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            nameTv=itemView.findViewById(R.id.name_tv);
            typeTv=itemView.findViewById(R.id.type_tv);
            editImg=itemView.findViewById(R.id.edit_img);
            delImg=itemView.findViewById(R.id.del_img);
            profileImg=itemView.findViewById(R.id.profile_img);
        }
    }

    public void refresh(List<UserDAO> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public void remove(UserDAO dao){
        list.remove(dao);
        notifyDataSetChanged();
    }

    public interface UserClickListener{
        void clickedUser(UserDAO  userDAO,String type);
    }
}
