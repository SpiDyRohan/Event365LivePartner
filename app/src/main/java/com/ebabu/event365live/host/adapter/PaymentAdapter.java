package com.ebabu.event365live.host.adapter;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.entities.CheckedInDao;
import com.ebabu.event365live.host.utils.DrawableUtils;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.CheckedRSVPHolder> {

    private PaymentUserClickListener listener;

    @Inject
    Application application;

    @Inject
    RequestManager requestManager;
    private List<CheckedInDao> list;
    private Context context;

    CompositeDisposable disposable;

    public PaymentAdapter(CompositeDisposable disposable,PaymentUserClickListener listener){
        this.listener=listener;
        this.disposable=disposable;
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public CheckedRSVPHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_payment_list,parent,false);
        context=parent.getContext();
        return new CheckedRSVPHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckedRSVPHolder holder, int position) {
        CheckedInDao checkedInDao=list.get(position);
        Log.e("checkedin dao",checkedInDao.toString());

        if(checkedInDao.getTicketType().equalsIgnoreCase("freeNormal")){
            holder.priceTv.setVisibility(View.GONE);
        }else {

            int ticketCount=0;
            if(checkedInDao.getTicket_number_booked_rel()!=null && checkedInDao.getTicket_number_booked_rel().size()>0) {
                for (int i=0;i<checkedInDao.getTicket_number_booked_rel().size();i++){
                    if(checkedInDao.getTicket_number_booked_rel().get(i).getStatus().equalsIgnoreCase("booked"))
                        ticketCount++;
                }
            }
            String amout= String.valueOf((int) (Float.parseFloat(checkedInDao.getPricePerTicket())*ticketCount));
            holder.priceTv.setText("$ " +amout);
            holder.priceTv.setVisibility(View.VISIBLE);

            if(checkedInDao.getTicketType().equalsIgnoreCase("regularTableSeating"))
                holder.priceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.table_logo, 0, 0, 0);
            else if(checkedInDao.getTicketType().equalsIgnoreCase("vipNormal"))
                holder.priceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vip_logo, 0, 0, 0);
            else
                holder.priceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.regular_logo, 0, 0, 0);
        }


        if(checkedInDao.getUsers()!=null) {

            CheckedInDao.User dao=checkedInDao.getUsers();

            holder.nameTv.setText(dao.getName());

            if(dao.getProfilePic()!=null)
                requestManager.load(dao.getProfilePic()).into(holder.img);
            else{

                disposable.add(
                        Single.fromCallable(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() throws Exception {
                                return DrawableUtils.getTempProfilePic(context, dao.getName());
                            }
                         })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap myObject) throws Exception {
                                requestManager.load(myObject).into(holder.img);
                            }
                        })
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return list==null?0:list.size();
    }

    class CheckedRSVPHolder extends RecyclerView.ViewHolder {
        CircleImageView img;
        TextView nameTv,priceTv;
        public CheckedRSVPHolder(@NonNull View itemView){
            super(itemView);
            img=itemView.findViewById(R.id.img);
            nameTv=itemView.findViewById(R.id.ivShowName);
            priceTv=itemView.findViewById(R.id.tvShowPrice);

            itemView.setOnClickListener(v->{
                listener.onclick(list.get(getAdapterPosition()).getQRkey(),
                        list.get(getAdapterPosition()).getTicketId(),
                        list.get(getAdapterPosition()).getTicketBookedId());
            });
        }
    }

    public void refresh(List<CheckedInDao> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public interface PaymentUserClickListener{
        void onclick(String id,int tickei_id,int book_id);
    }
}
