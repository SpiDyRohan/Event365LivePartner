package com.ebabu.event365live.host.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.databinding.ActivityAddUserBinding;
import com.ebabu.event365live.host.entities.MyResponse;
import com.ebabu.event365live.host.entities.UserDAO;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.AddUserViewModel;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class AddUser extends BaseActivity {

    ActivityAddUserBinding binding;
    private boolean isValid;
    AddUserViewModel viewModel;
    MyLoader loader;
    private int id;
    String userType;
    JsonArray userArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_user);
        viewModel = ViewModelProviders.of(this).get(AddUserViewModel.class);

        binding.setViewmodel(viewModel);
        loader = new MyLoader(this);

        id = getIntent().getIntExtra("id", -1);
        if (id != -1) {
            fetchUserById(id);
            binding.textView20.setText("Update User");
        }

        binding.contactChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            binding.usedTitle.setText(getString(R.string.this_acc_willbe_used_to));
            binding.usedCg.setVisibility(View.VISIBLE);

            if (userArray != null) {
                String s = userArray.get(0).toString();
                if ("checkin".equals(s.substring(1, s.length() - 1))) userArray = null;
            }

            if (checkedId == R.id.member_chip) {
                userType = "member";
                binding.usedTitle.setText(getString(R.string.member_checked_title));
                binding.usedCg.setVisibility(View.GONE);

                if (binding.both.isChecked()) binding.both.setChecked(false);
                if (binding.createChip.isChecked()) binding.createChip.setChecked(false);
                if (binding.manageChip.isChecked()) binding.manageChip.setChecked(false);

                userArray = new JsonArray();
                userArray.add("checkin");

            } else if (checkedId == R.id.host_chip) {
                userType = "host";
                binding.both.setVisibility(View.GONE);
                binding.manageChip.setVisibility(View.GONE);
                if (binding.both.isChecked()) binding.both.setChecked(false);
                if (binding.manageChip.isChecked()) binding.manageChip.setChecked(false);
            } else if (checkedId == R.id.promotor_chip) {
                userType = "promoter";
                binding.both.setVisibility(View.VISIBLE);
                binding.manageChip.setVisibility(View.VISIBLE);
            } else userType = null;

        });

        binding.firstNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 1) {
                    binding.firstNameImg.animate().alpha(1f).setDuration(500);
                } else
                    binding.firstNameImg.animate().alpha(0f).setDuration(500);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.lastNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 1) {
                    binding.lastnameImg.animate().alpha(1f).setDuration(500);
                } else
                    binding.lastnameImg.animate().alpha(0f).setDuration(500);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        binding.pwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > 5) {
                    binding.pwdImg.animate().alpha(1f).setDuration(500);
                } else
                    binding.pwdImg.animate().alpha(0f).setDuration(500);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        binding.mailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValid = validateEmail(charSequence.toString());

                if (isValid)
                    binding.mailImg.animate().alpha(1f).setDuration(500);
                else
                    binding.mailImg.animate().alpha(0f).setDuration(500);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.retryBtn.setOnClickListener(view -> {
            if (id != -1) {
                if (validate(view)) {
                    //update
                    loader.show("");


                    JsonObject object = new JsonObject();
                    object.addProperty("userType", userType);
                    object.add("roles", userArray);

                    viewModel.updateUser(id, object).observe(this, new Observer<MyResponse>() {
                        @Override
                        public void onChanged(MyResponse myResponse) {
                            loader.dismiss();
                            if (myResponse.isSuccess()) {

                                Toast.makeText(getApplicationContext(), myResponse.getMessage(), Toast.LENGTH_LONG).show();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("success", true);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();

                            } else {
                                Dialogs.toast(getApplicationContext(), view, myResponse.getMessage());
                            }
                        }
                    });

                }
            } else if (validate(view)) {
                //add
                loader.show("");
                viewModel.go(userArray).observe(this, new Observer<MyResponse>() {
                    @Override
                    public void onChanged(MyResponse s) {
                        if (s != null) {
                            loader.dismiss();
                            if (s.isSuccess()) {
                                Dialogs.showActionDialog(AddUser.this,
                                        getString(R.string.app_name),
                                        s.getMessage(),
                                        "Done",
                                        v1 -> {
                                            Intent returnIntent = new Intent();
                                            returnIntent.putExtra("success", true);
                                            setResult(Activity.RESULT_OK, returnIntent);
                                            finish();
                                        }
                                );
                            } else {
                                Dialogs.toast(getApplicationContext(), view, s.getMessage());
                            }
                        }
                    }
                });
            }
        });

        binding.usedCg.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                if (checkedId == R.id.create_chip) {
                    userArray = new JsonArray();
                    userArray.add("event_management");
                } else if (checkedId == R.id.manage_chip) {
                    userArray = new JsonArray();
                    userArray.add("user_management");
                } else if (checkedId == R.id.both) {
                    userArray = new JsonArray();
                    userArray.add("event_management");
                    userArray.add("user_management");
                } else userArray = null;


            }
        });
        if (id == -1) {
            binding.createChip.setChecked(true);
            binding.hostChip.setChecked(true);
            userType = "host";
        }
        binding.backArrow.setOnClickListener(v -> onBackPressed());
    }

    private void fetchUserById(int id) {
        loader.show("");
        viewModel.getUserById(id).observe(this, userReponse -> {
            loader.dismiss();
            if (userReponse.isSuccess()) {
                UserDAO dao = userReponse.getUserDAO();
                String name = dao.getName();
                if (name != null) {
                    String[] names = name.split(" ");
                    binding.firstNameEt.setText(names[0]);
                    StringBuffer buffer = new StringBuffer();
                    for (int i = 1; i < names.length; i++) {
                        buffer.append(" ").append(names[i]);
                    }
                    binding.lastNameEt.setText(buffer);
                }
                binding.mailEt.setText(dao.getEmail());
                binding.phoneEt.setText(dao.getPhoneNo());
                binding.pwdEt.setText("*******");

                binding.firstNameEt.setFocusable(false);
                binding.lastNameEt.setFocusable(false);
                binding.mailEt.setFocusable(false);
                binding.pwdEt.setFocusable(false);
                binding.phoneEt.setFocusable(false);

                userType = dao.getUserType();
                if ("member".equals(userType)) binding.memberChip.setChecked(true);
                else if ("host".equals(userType)) binding.hostChip.setChecked(true);
                else if ("promoter".equals(userType)) binding.promotorChip.setChecked(true);

                try {
                    JSONArray jsonObject = new JSONArray(userReponse.getMessage());
                    if (jsonObject.length() > 0) {
                        if (jsonObject.length() == 2) {
                            binding.both.setChecked(true);
                        } else if (jsonObject.getString(0).equals("event_management"))
                            binding.createChip.setChecked(true);
                        else if (jsonObject.getString(0).equals("user_management"))
                            binding.manageChip.setChecked(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                if (userReponse.getCode() == API.SESSION_EXPIRE) {
                    userReponse.setCode(0);
                    Utility.sessionExpired(AddUser.this);
                } else {
                    Dialogs.toast(getApplicationContext(), binding.backArrow, userReponse.getMessage());
                }
            }
        });
    }

    private boolean validate(View v) {

        if (userType == null) {
            Dialogs.toast(getApplicationContext(), v, "Select user type please!");
            return false;
        }

        if (binding.firstNameEt.getText().toString().trim().length() < 2) {
            Dialogs.toast(getApplicationContext(), v, getString(R.string.enter_first_name));
            binding.firstNameEt.requestFocus();
            return false;
        }

        if (binding.lastNameEt.getText().toString().trim().length() < 2) {
            Dialogs.toast(getApplicationContext(), v, getString(R.string.enter_lastname));
            binding.lastNameEt.requestFocus();
            return false;
        }


        if (!isValid) {
            Dialogs.toast(getApplicationContext(), v, getString(R.string.enter_valid_mail));
            binding.mailEt.requestFocus();
            return false;
        }

        if (binding.pwdEt.getText().toString().trim().length() < 6) {
            Dialogs.toast(getApplicationContext(), v, getString(R.string.password_err));
            binding.pwdEt.requestFocus();
            return false;
        }

        if (userArray == null) {
            Dialogs.toast(getApplicationContext(), v, "Please choose This account will be used to!");
            return false;
        }

        return true;
    }

    private boolean validateEmail(String email) {
        if (email.startsWith(".") || email.startsWith("_") || email.contains("..") || email.contains("__")
                || email.contains("._") || email.contains("_.") || email.contains(".@") || email.contains("_@")) {
            return false;
        }
        return Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches();

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                findViewById(R.id.no_internet_layout).setVisibility(View.GONE);
            else
                findViewById(R.id.no_internet_layout).setVisibility(View.VISIBLE);
        });
    }
}
