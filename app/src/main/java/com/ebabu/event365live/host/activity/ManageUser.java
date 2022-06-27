package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.adapter.UserAdapter;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityManageUserBinding;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.entities.UserResponse;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.ManageUserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;


public class ManageUser extends BaseActivity {

    private static final int REQUEST_USER_ADDED = 121;
    ActivityManageUserBinding binding;
    UserAdapter adapter;
    ManageUserViewModel viewModel;
    MyLoader loader;
    List<UserDAO> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_user);
        loader = new MyLoader(this);
        loader.show("");

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setHasFixedSize(true);

        adapter = new UserAdapter((userDAO, type) -> {

            if (type.equalsIgnoreCase("edit")) {
                startActivityForResult(new Intent(this, AddUser.class).putExtra("id", userDAO.getId()), REQUEST_USER_ADDED);
            } else
                new MaterialAlertDialogBuilder(ManageUser.this)
                        .setTitle(getString(R.string.alert))
                        .setMessage(getString(R.string.really_want_delete))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loader.show("");
                                viewModel.deleteUser(userDAO.getId()).observe(ManageUser.this, new Observer<MyResponse>() {
                                    @Override
                                    public void onChanged(MyResponse myResponse) {
                                        loader.dismiss();
                                        if (myResponse.isSuccess()) {
                                            if (list != null) list.remove(userDAO);
                                            adapter.notifyDataSetChanged();
                                            if (list.size() == 0)
                                                binding.noDataLayout.setVisibility(View.VISIBLE);
                                            Toast.makeText(getApplicationContext(), "User deleted successfully!", Toast.LENGTH_LONG).show();
                                            //Toast.makeText(getApplicationContext(), myResponse.getMessage(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Dialogs.toast(getApplicationContext(), binding.getRoot(), myResponse.getMessage());
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        })
                        .show();
        });

        binding.rv.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(ManageUserViewModel.class);

        viewModel.getUsers().observe(this, new Observer<UserResponse>() {
            @Override
            public void onChanged(UserResponse response) {
                loader.dismiss();
                Log.e("code", response.getCode() + "");
                if (response.getCode() == API.SESSION_EXPIRE) {
                    response.setCode(0);
                    Utility.sessionExpired(ManageUser.this);
                } else if (response.isSuccess() && response.getCode() == 200 && response.getUsers().size() == 0) {
                    response.setCode(0);
                    binding.noDataLayout.setVisibility(View.VISIBLE);
                } else {
                    list = response.getUsers();
                    adapter.refresh(list);
                    binding.noDataLayout.setVisibility(View.GONE);
                }
            }
        });
        binding.backArrow.setOnClickListener(v -> finish());
        binding.retryBtn.setOnClickListener(v -> startActivityForResult(new Intent(this, AddUser.class), REQUEST_USER_ADDED));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER_ADDED) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra(API.SUCCESS, false)) {
                    viewModel.fetchUser();
                }
            }
        }
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
