package com.ebabu.event365live.host.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ContactUs extends BaseActivity {

    Spinner spinner;
    List<String> codes;
    EditText messageEt;
    MyLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        loader=new MyLoader(this);
        spinner=findViewById(R.id.spinner);
        messageEt=findViewById(R.id.msg_et);

        codes= Arrays.asList(new String[]{
                "Problem in creating an event",
                "Payment related issues",
                "Check-in issues",
                "Event is not showing"
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ContactUs.this,android.R.layout.simple_spinner_item,codes);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("selected",i+" "+l);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });

        findViewById(R.id.retry_btn).setOnClickListener(v->{
            String message=  messageEt.getText().toString().trim();
            if(TextUtils.isEmpty(message)){
                Dialogs.toast(ContactUs.this,v,"Please type your message!");
                return;
            }

            loader.show("");
            Observable.timer(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        loader.dismiss();
                        Dialogs.showActionDialog(ContactUs.this,
                                getString(R.string.app_name),
                                "Thanks for contacting us!",
                                "Done",
                                v1-> finish()
                        );
                    });
        });

        findViewById(R.id.back_arrow).setOnClickListener(v->{
            onBackPressed();
        });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(()->{
            if(isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);

            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);

        });

    }
}
