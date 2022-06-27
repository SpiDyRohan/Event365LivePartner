package com.ebabu.event365live.host.fragments.rsvp_invites.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.MyContactsInviteLayoutBinding;
import com.ebabu.event365live.host.fragments.rsvp_invites.model.MyContact;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class MyContactAdapter extends RecyclerView.Adapter<MyContactAdapter.InviteRsvpHolder>{

    private Context context;
    @Inject
    RequestManager requestManager;

    List<MyContact> list;
    private OnClickInviteListener onClickInviteListener;

    public MyContactAdapter(Context context, OnClickInviteListener onClickInviteListener){
        this.context = context;
        this.onClickInviteListener = onClickInviteListener;
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public InviteRsvpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        MyContactsInviteLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.my_contacts_invite_layout,parent,false);
        return new InviteRsvpHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull InviteRsvpHolder holder, int position) {
        MyContactsInviteLayoutBinding binding = holder.binding;
        MyContact dao=list.get(position);
        binding.nameTv.setText(dao.getContactName());
        binding.thumbTv.setText(String.valueOf(dao.getContactName().charAt(0)).toUpperCase());

        binding.inviteBtn.setOnClickListener(v->{
            onClickInviteListener.onClickInvite(position);
        });
    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    class InviteRsvpHolder extends RecyclerView.ViewHolder {
        MyContactsInviteLayoutBinding binding;

        InviteRsvpHolder(@NonNull View itemView){
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public void refresh(List<MyContact> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public interface OnClickInviteListener{
        public void onClickInvite(int pos);
    }

}
