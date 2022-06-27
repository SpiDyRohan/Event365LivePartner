package com.ebabu.event365live.host.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentMinPaymentBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MinPaymentFragment extends Fragment {

    @Inject
    ApiInterface apiInterface;
    private FragmentMinPaymentBinding binding;
    private CreateEventDAO createEventDAO;
    private MyLoader loader;
    private UserDAO userDAO;
    private String amount = "0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_min_payment, container, false);
        App.getAppComponent().inject(this);

        loader = new MyLoader(getContext());
        if (getArguments() != null) {
            createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();
        }


        binding.cvProceed.setOnClickListener(view -> {
            PaymentMethodFragment paymentMethodFragment = new PaymentMethodFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("eventDAO", createEventDAO);
            bundle.putString("amount", amount);
            paymentMethodFragment.setArguments(bundle);
            showFragment(paymentMethodFragment, "PaymentMethodFragment");
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvEventName.setText(createEventDAO.getName());
        binding.tvAddress.setText(createEventDAO.getVenueAddress());
        binding.tvVipTicket.setText("VIP Ticket - ");
        binding.tvDateTime.setText(createEventDAO.getStartDate() + " | " + createEventDAO.getStartTime() + "-" + createEventDAO.getEndTime());

        paidEventPrice();
    }

    public void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        fragmentManager.popBackStackImmediate(fragment.getClass().getName(), 0);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.my_nav_host_fragment, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    private void paidEventPrice() {
        loader.show("");

//        {
//            "lastLoginTime":"02-02-2021 15:05:04"
//        }

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("lastLoginTime", Utility.getSharedPreferencesString(getActivity(), Constants.LAST_LOGIN_TIME));

        Call call = apiInterface.paidEventPrice(jsonObject);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                loader.dismiss();

                Log.e("got result", "yes");
                Log.e("got result", "paidEventPrice response> " + response);

                if (response.isSuccessful()) {
                    try {
                        JSONObject OBJ = new JSONObject(response.body().toString());

                        amount = OBJ.getJSONObject("data").getString("amount");
                        Log.e("got result", "amount> " + amount);

                        String msg = getActivity().getString(R.string.minimum_payment_msg);
                        binding.tvMinimumPaymentMsg.setText(msg.replace("AMOUNT", "$"+amount));
                        binding.tvTotalPrice.setText("$"+amount);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
                loader.dismiss();
                Dialogs.toast(getContext(), binding.getRoot(), "Something went wrong!");
            }
        });
    }

}