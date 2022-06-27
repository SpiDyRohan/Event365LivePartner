package com.ebabu.event365live.host.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.PaymentAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentPaymentBinding;
import com.ebabu.event365live.host.entities.CheckedInDao;
import com.ebabu.event365live.host.entities.PaymentDetailDao;
import com.ebabu.event365live.host.events.CheckedInEvent;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.StringUtils;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.CreateEventViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {

    FragmentPaymentBinding binding;
    private List<CheckedInDao> list;
    MyLoader loader;
    String rsvpType;

    PaymentAdapter adapter;
    private boolean isFetching;
    private boolean wasNetworkError;
    private String personName="";
    private int limit=30,page=1;
    @Inject
    Context mContext;

    int eventId;

    @Inject
    ApiInterface apiInterface;

    CompositeDisposable disposable=new CompositeDisposable();

    CreateEventViewModel viewModel;

    AlertDialog paymentDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        rsvpType=getArguments().getString("type");
        System.out.println("Type :::::::"+rsvpType);
        eventId=getArguments().getInt("id");
        list=new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_payment, container, false);
        viewModel= ViewModelProviders.of(this).get(CreateEventViewModel.class);

        loader=new MyLoader(getContext());
        personName = Utility.getSharedPreferencesString(mContext,"payment_query");


        adapter=new PaymentAdapter(disposable, this::getTicketDetail);
        binding.rv.setHasFixedSize(true);
        binding.rv.setAdapter(adapter);

        fetchData();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isFetching) {
                    page=1;
                    list.clear();
                    fetchData();
                }
            }
        });

        return binding.getRoot();
    }

    private void getTicketDetail(String qr,int ticket_id,int book_id) {

        if (!Utility.isNetworkAvailable(getContext())) {
            if(binding.swipe.isRefreshing())binding.swipe.setRefreshing(false);
            wasNetworkError=true;
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.textView48.setText(getString(R.string.check_network));
            binding.rv.setVisibility(View.GONE);
            Toast.makeText(getContext(),getString(R.string.check_network),Toast.LENGTH_LONG).show();
            return;
        }


        if(!binding.swipe.isRefreshing())
            loader.show("");
        isFetching=true;
        Call<JsonElement> call= apiInterface.getPaymentDetails(qr,ticket_id);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loader.dismiss();
                isFetching=false;
                if(wasNetworkError){
                    wasNetworkError=false;
                    binding.textView48.setText(getString(R.string.no_data_found));
                    binding.noDataLayout.setVisibility(View.GONE);
                    binding.rv.setVisibility(View.VISIBLE);
                }
                if(binding.swipe.isRefreshing()) binding.swipe.setRefreshing(false);

                if(response.isSuccessful()){
                    try {
                        JSONObject OBJ= new JSONObject(response.body().toString());
                        String paymentData=OBJ.getJSONObject(API.DATA).getJSONObject("paymentUser").toString();

                        PaymentDetailDao detailDao=new Gson().fromJson(paymentData, PaymentDetailDao.class);

                        showTicketPaymentDetails(detailDao,book_id);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    try {

                        JSONObject OBJ= new JSONObject(response.errorBody().string());
                        String message=OBJ.getString(API.MESSAGE);
                        int code=OBJ.getInt(API.CODE);
                        if(code== API.SESSION_EXPIRE){
                            Utility.sessionExpired(mContext);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }


            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call,@NonNull Throwable t) {
                t.printStackTrace();
                loader.dismiss();
            }
        });

    }

    private void fetchData() {

        if (!Utility.isNetworkAvailable(getContext())) {
            if(binding.swipe.isRefreshing())binding.swipe.setRefreshing(false);
            wasNetworkError=true;
            binding.noDataLayout.setVisibility(View.VISIBLE);
            binding.textView48.setText(getString(R.string.check_network));
            binding.rv.setVisibility(View.GONE);
            Toast.makeText(getContext(),getString(R.string.check_network),Toast.LENGTH_LONG).show();
            return;
        }


        if(!binding.swipe.isRefreshing())
            loader.show("");
        isFetching=true;
        Call<JsonElement> call= apiInterface.fetchAllPayment(
                eventId,
                personName,
                rsvpType,
                limit,
                page
        );

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loader.dismiss();
                isFetching=false;
                if(wasNetworkError){
                    wasNetworkError=false;
                    binding.textView48.setText(getString(R.string.no_data_found));
                    binding.noDataLayout.setVisibility(View.GONE);
                    binding.rv.setVisibility(View.VISIBLE);
                }
                if(binding.swipe.isRefreshing()) binding.swipe.setRefreshing(false);

                if(response.isSuccessful()){
                    try {
                        JSONObject OBJ= new JSONObject(response.body().toString());
                        JSONArray data=OBJ.getJSONObject(API.DATA).getJSONArray("rspvType");

                        if(data.length()>0)page++;

                        for(int i=0;i<data.length();i++){
                            String s=data.getJSONObject(i).toString();

                            list.add(new Gson().fromJson(s, CheckedInDao.class));

                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        list.clear();
                    }
                }
                else{
                    try {
                        JSONObject OBJ= new JSONObject(response.errorBody().string());
                        String message=OBJ.getString(API.MESSAGE);
                        int code=OBJ.getInt(API.CODE);
                        if(code== API.SESSION_EXPIRE){
                            Utility.sessionExpired(mContext);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                adapter.refresh(list);
                if(list.size()==0)binding.noDataLayout.setVisibility(View.VISIBLE);
                else{
                    binding.noDataLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonElement> call,@NonNull Throwable t) {
                t.printStackTrace();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTextSearchedEvent(CheckedInEvent event) {
        if(!event.getQuery().equalsIgnoreCase(personName)){
            personName=event.getQuery();
            page=1;
            list.clear();
            fetchData();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        disposable.dispose();
    }

    private void showTicketPaymentDetails(PaymentDetailDao detailDao,int book_id){

        AlertDialog.Builder  builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.booking_info_layout,null,false);

        ((TextView)view.findViewById(R.id.tvShowEventName)).setText(detailDao.getEvents().getName());
        ((TextView)view.findViewById(R.id.addrs_tv)).setText(detailDao.getEvents().getAddress().get(0).getVenueAddress());
        ((TextView)view.findViewById(R.id.textView58)).setText(detailDao.getUser().getEmail());
        if(detailDao.getEvents().getAddress().size()>0)
        ((TextView)view.findViewById(R.id.tvShowEventAdd)).setText(detailDao.getEvents().getAddress().get(0).getVenueAddress());
        ((TextView)view.findViewById(R.id.name_tv)).setText(detailDao.getUser().getName());

        String endTime=StringUtils.getTimeWithZone(detailDao.getEvents().getEnd());
        ((TextView)view.findViewById(R.id.time_tv)).setText(
                StringUtils.getTimeWithZone(detailDao.getEvents().getStart())+" - "+endTime
        );
        ((TextView)view.findViewById(R.id.tvShowStartTime)).setText(
                StringUtils.getDateByPattern("dd MMM, yyyy", detailDao.getEvents().getStart())
        );

        double regular=0.0,regularSeat=0.0,vip=0.0,vipSeat=0.0;
        int totalFree=0,totalRegular=0,totalRegularSeat=0,totalVip=0,totalVipSeat=0;

        List<PaymentDetailDao.Ticket> list=detailDao.getEvents().getTicketBooked();
        for(PaymentDetailDao.Ticket t:list){
            if(t.getTicketType().equalsIgnoreCase("freeNormal"))
                totalFree+=t.getTotalQuantity();

            else if(t.getTicketType().equalsIgnoreCase("regularNormal")){
                regular+=t.getPricePerTicket();
                totalRegular+=t.getTotalQuantity();
            }

            else if(t.getTicketType().equalsIgnoreCase("regularTableSeating")){
                regularSeat+=t.getPricePerTicket();
                totalRegularSeat+=t.getTotalQuantity();
            }

            else if(t.getTicketType().equalsIgnoreCase("vipNormal")){
                vip+=t.getPricePerTicket();
                totalVip+=t.getTotalQuantity();
            }

            else {
                vipSeat+=t.getPricePerTicket();
                totalVipSeat+=t.getTotalQuantity();
            }

            double total=0.0;
            if(totalRegular>0) total+=regular*totalRegular;
            if(totalVip>0) total+=vip*totalVip;

            if(totalRegularSeat>0) total+=regularSeat*totalRegularSeat;
            if(totalVipSeat>0) total+=vipSeat*totalVipSeat;

            ((TextView)view.findViewById(R.id.price_tv)).setText("$"+total);
        }

        LinearLayout layout=view.findViewById(R.id.ticket_layout);

        if(totalFree>0) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_payment_price, null, false);
            ((TextView)view1.findViewById(R.id.price_tv)).setText(totalFree+" Free");
            layout.addView(view1);
        }

        if(totalRegular>0) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_payment_price, null, false);
            ((TextView)view1.findViewById(R.id.price_tv)).setText(totalRegular+" Regular - $"+regular);
            layout.addView(view1);
        }

        if(totalRegularSeat>0) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_payment_price, null, false);
            ((TextView)view1.findViewById(R.id.price_tv)).setText(totalRegularSeat+" Regular Seating - $"+regularSeat);
            layout.addView(view1);
        }

        if(vip>0) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_payment_price, null, false);
            ((TextView)view1.findViewById(R.id.price_tv)).setText(totalVip+" Vip - $"+vip);
            layout.addView(view1);
        }

        if(vipSeat>0) {
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_payment_price, null, false);
            ((TextView)view1.findViewById(R.id.price_tv)).setText(totalVipSeat+" Vip Seating- $"+vipSeat);
            layout.addView(view1);
        }

        final Disposable disposable=Single.fromCallable(()->{

                QRCodeWriter writer = new QRCodeWriter();
                    BitMatrix bitMatrix = writer.encode(detailDao.getQRkey(), BarcodeFormat.QR_CODE, 200, 200);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.parseColor("#f7f6fb"));
                        }
                    }
                    return bmp;
                }
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap->((ImageView)view.findViewById(R.id.ivShowQRCode)).setImageBitmap(bitmap));


        TextView cancelBtn=view.findViewById(R.id.cancel_btn);
        TextView contactBtn=view.findViewById(R.id.contact_btn);


        if(rsvpType.equalsIgnoreCase("cancelled"))
            cancelBtn.setVisibility(View.GONE);
        else
        cancelBtn.setOnClickListener(v->
            new MaterialAlertDialogBuilder(getContext())
                    .setMessage(getString(R.string.ticket_cancel_msg))
                    .setTitle("Alert!")
                    .setNegativeButton("No",null)
                    .setPositiveButton("Yes",(dialog, which) -> {
                        Log.e(detailDao.getQRkey(),detailDao.getUser().getId()+"");

                        loader.show("");
                        viewModel.cancelTicket(detailDao.getUser().getId(),detailDao.getQRkey(),book_id).observe(getViewLifecycleOwner(), myResponse -> {
                            loader.dismiss();
                            if (myResponse.isSuccess()) {
                                paymentDialog.dismiss();
                                Toast.makeText(getContext(), myResponse.getMessage(), Toast.LENGTH_LONG).show();
                                personName="";
                                page=1;
                                PaymentFragment.this.list.clear();
                                fetchData();
                            }
                            else
                                Dialogs.toast(getContext(), binding.getRoot(), myResponse.getMessage());
                        });
                    })
                    .show()
        );

//        contactBtn.setOnClickListener(v->{
//            Intent intent = new Intent(getContext(), ConversationActivity.class);
//            intent.putExtra(ConversationUIService.USER_ID, String.valueOf(detailDao.getUser().getId()));
//            intent.putExtra(ConversationUIService.DISPLAY_NAME, detailDao.getUser().getName()); //put it for displaying the title.
//            intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
//            startActivity(intent);
//        });

        builder.setView(view);

        paymentDialog = builder.create();

        paymentDialog.setOnDismissListener(dialogInterface -> disposable.dispose());
        paymentDialog.setOnDismissListener(dialog1 -> disposable.dispose());
        paymentDialog.show();

        view.findViewById(R.id.cross_img).setOnClickListener(v->paymentDialog.dismiss());

    }
}
