package com.ebabu.event365live.host.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivityLoginBinding;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.LoginViewModel;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends BaseActivity {

    ActivityLoginBinding binding;
    boolean isMasking = true;
    LoginViewModel viewModel;
    private GoogleSignInClient mGoogleSignInClint;
    private int RC_SIGN_IN_REQUEST = 1001;
    private static int REGISTER_CODE = 1002;
    private CallbackManager callbackManager;
    private String getSocialImg;
    private int userId;
    @Inject
    ApiInterface apiInterface;

    MyLoader loader;

    Snackbar networkBar;
    String IPaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getAppComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        viewModel.init(getApplicationContext(), apiInterface);
        binding.setViewmodel(viewModel);
        callbackManager = CallbackManager.Factory.create();
        viewModel.getIsFailed().observe(this, s -> Dialogs.toast(getApplicationContext(), binding.parentLayout, s));

        loader = new MyLoader(this);
        viewModel.getIsConnecting().observe(this, aBoolean -> loader.show(""));
        printHashKey();
        binding.ivBackBtn.setOnClickListener(v -> onBackPressed());
        binding.forgotBtn.setOnClickListener(v -> startActivity(new Intent(this, ForgotPassword.class)));
        binding.ivRegister.setOnClickListener(v -> startActivity(new Intent(this, Register.class)));



        binding.ivGmailIcon.setOnClickListener(view -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail().build();
            mGoogleSignInClint = GoogleSignIn.getClient(this, gso);

            Intent googleSignInIntent = mGoogleSignInClint.getSignInIntent();
            startActivityForResult(googleSignInIntent, RC_SIGN_IN_REQUEST);
        });

        binding.ivFbIcon.setOnClickListener(view -> {
            //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile", "user_friends"));
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });

        App.createEventDAO = null;
        appleLogin();
        addMailPWdWatcher();
        getFirebaseToken();
        getDeviceId();
        NetworkDetect();
        networkBar = Snackbar.make(binding.getRoot(), "No internet connection...", Snackbar.LENGTH_INDEFINITE);
        facebookSignIn();
    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("gethashcode", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {

        } catch (Exception e) {
        }
    }

    private void getFirebaseToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
            String token = instanceIdResult.getToken();
            Utility.setSharedPreferencesString(this, API.FIREBASE_TOKEN, token);
        });

    }

    private void getDeviceId() {
        String device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Android", "Android ID : " + device_id);
        Utility.setSharedPreferencesString(this, API.DEVICE_ID, device_id);
    }

    //Check the internet connection.
    private void NetworkDetect() {
        boolean WIFI = false;
        boolean MOBILE = false;
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = CM.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    WIFI = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    MOBILE = true;
        }
        if (WIFI) {
            IPaddress = Utility.getIPaddress(getApplicationContext());
            Utility.setSharedPreferencesString(this, API.SOURCE_IP, IPaddress);
        }
        if (MOBILE) {
            IPaddress = GetDeviceipMobileData();
            Utility.setSharedPreferencesString(this, API.SOURCE_IP, IPaddress);
        }

    }

    public String GetDeviceipMobileData() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    private void addMailPWdWatcher() {
        binding.mailEt.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(charSequence.toString()).matches())
                    binding.validMailImg.setVisibility(View.VISIBLE);
                else
                    binding.validMailImg.setVisibility(View.INVISIBLE);
            }

            public void afterTextChanged(Editable editable) {
            }
        });

        //validation for password Image
        binding.pwdImg.setOnClickListener(v -> {
            if (binding.pwdEt.getText().toString().trim().length() > 0) {
                   isMasking = !isMasking;
                if (isMasking) {
                    binding.pwdImg.setImageResource(R.drawable.eye_gray);
                    binding.pwdEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    binding.pwdEt.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.pwdImg.setImageResource(R.drawable.eye);
                }
                binding.pwdEt.setSelection(binding.pwdEt.getText().toString().trim().length());
            }

        });


        binding.pwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            //PASSWORD VALIDATION
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if ((charSequence.toString().trim()).length()==0) {
                    binding.validPassImg.setVisibility(View.INVISIBLE);
                    binding.pwdImg.setVisibility(View.INVISIBLE);
                }else if ((charSequence.toString().trim()).length()>=8 && Constants.VALID_PASSWORD_REGEX.matcher(charSequence.toString().trim()).matches()){
                    binding.validPassImg.setVisibility(View.VISIBLE);
                    binding.pwdImg.setVisibility(View.VISIBLE);
               }else{
                    binding.validPassImg.setVisibility(View.INVISIBLE);
                    binding.pwdImg.setVisibility(View.VISIBLE);
               }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(SuccessEvent event) {


        event.getMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                loader.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    String msg = jsonObject.getString(API.MESSAGE);
                    if (jsonObject.getBoolean(API.SUCCESS)) {
                        Boolean isManageUser = jsonObject.getJSONObject(API.DATA).getBoolean(API.IS_MANAGE_USER);
                        Boolean isUnderVenue = jsonObject.getJSONObject(API.DATA).getBoolean(API.IS_UNDER_VENUE);
                        Boolean isVenueOwner = jsonObject.getJSONObject(API.DATA).getBoolean(API.IS_VENUE_OWNER);

                        userId = jsonObject.getJSONObject(API.DATA).getJSONObject("user").getInt("id");
                        String name = jsonObject.getJSONObject(API.DATA).getJSONObject("user").getString("name");
                        String profilePic = jsonObject.getJSONObject(API.DATA).getJSONObject("user").getString("profilePic");

                        Utility.setSharedPreferencesBoolean(getApplicationContext(), API.SESSION_ACTIVE, true);
                        Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_VENUE_OWNER, isVenueOwner);
                        Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_UNDER_VENUE, isUnderVenue);
                        Utility.setSharedPreferencesBoolean(getApplicationContext(), API.IS_MANAGE_USER, isManageUser);

                        Utility.setSharedPreferencesInteger(getApplicationContext(), API.USER_ID, userId);
                        Utility.setSharedPreferencesString(getApplicationContext(), API.USER_NAME, name);
                        Utility.setSharedPreferencesString(getApplicationContext(), API.PROFILE_PIC, profilePic);

                        if (jsonObject.getJSONObject(API.DATA).getJSONObject("user").getString("userType").equalsIgnoreCase("null")) {
                            Intent intent = new Intent(Login.this, Register.class);
                            intent.putExtra("userId", userId);
                            startActivityForResult(intent, REGISTER_CODE);
                        } else {
                            Intent intent = new Intent(Login.this, Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Utility.setSharedPreferencesBoolean(Login.this, Constants.IS_VENUER, false);

                        if (jsonObject.getInt(API.CODE) == 432) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            int id = jsonObject.getJSONObject(API.DATA).getInt(API.ID);
                            Log.e("id", id + "");
                            Intent intent = new Intent(Login.this, VerifiyCode.class);
                            intent.putExtra(API.EMAIL, binding.mailEt.getText().toString());
                            intent.putExtra(API.ID, id);
                            intent.putExtra(Constants.FROM, API.SIGNUP);
                            startActivity(intent);
                        } else if (jsonObject.getInt(API.CODE) == 433) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            int id = jsonObject.getJSONObject(API.DATA).getInt(API.ID);
                            Log.e("id", id + "");
                            Intent intent = new Intent(Login.this, SMSVerification.class);
                            intent.putExtra(API.ID, id);
                            intent.putExtra(Constants.FROM, API.SIGNUP);
                            startActivity(intent);
                        } else if (jsonObject.getInt(API.CODE) == 434) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            int id = jsonObject.getJSONObject(API.DATA).getInt(API.ID);
                            Log.e("id", id + "");
                            Intent intent = new Intent(Login.this, SMSVerification.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(API.ID, id);
                            intent.putExtra(Constants.FROM, API.SIGNUP);
                            startActivity(intent);
                        } else if (jsonObject.getInt(API.CODE) == 410 && jsonObject.getInt(API.CODE) == 409) {
                            Dialogs.loginAttemptsDialog(Login.this, view -> {
                                Intent intent = new Intent(Login.this, ContactUs.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            });
                        } else {
                            Dialogs.toast(getApplicationContext(), binding.createAccBtn, msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Dialogs.toast(getApplicationContext(), binding.createAccBtn, "something went wrong!!!!");
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);


    }

    @Override
    public void onStop() {
        super.onStop();
        googleLogout(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        runOnUiThread(() -> {
            if (isConnected)
                networkBar.dismiss();
            else networkBar.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_CODE) {
            updateSocialLoginData();
        } else if (requestCode == RC_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            //redirecting to home
          /*  Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);*/

            if (account != null) {
                //setting user details in viewmodel
                String name = account.getDisplayName();
                String email = account.getEmail();
                String id = account.getId();
                if (account.getPhotoUrl() != null)
                    getSocialImg = account.getPhotoUrl().toString();
                viewModel.socialLogin(name, email, id, "google");
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void updateSocialLoginData() {
        loader.show("Please wait...");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("userType", Utility.getSharedPreferencesString(this, Constants.USER_TYPE));
        jsonObject.addProperty("OS", "android");
        jsonObject.addProperty("platform", "playstore");
        jsonObject.addProperty("deviceId", Utility.getSharedPreferencesString(this, API.DEVICE_ID));
        jsonObject.addProperty("sourceIp", Utility.getSharedPreferencesString(this, API.SOURCE_IP));

        Call<JsonElement> call = apiInterface.updateSocialLoginData(jsonObject);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    loader.dismiss();
                    String auth = response.headers().get(API.AUTHORIZATION);
                    Log.e("Login Success", "onResponse: " + auth);
                    Utility.setSharedPreferencesString(getApplicationContext(), API.AUTHORIZATION, auth);
                    Intent intent = new Intent(Login.this, Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    loader.dismiss();
                    try {
                        String data = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(data);
                        String msg = jsonObject.getString(API.MESSAGE);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dialogs.toast(getApplicationContext(), binding.createAccBtn, "something went wrong!!!!");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                loader.dismiss();
                Dialogs.toast(getApplicationContext(), binding.createAccBtn, "something went wrong!!!!");
            }
        });
    }

    private void facebookSignIn() {
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                (object, response) -> {
                                    try {
                                        String userFirstName = object.getString("name");
                                        String fbUserEmail = object.getString("email"); /* consider email ID as a facebook id or provider ID*/
                                        String fbUserId = object.getString("id");
                                        getSocialImg = "https://graph.facebook.com/" + fbUserId + "/picture?type=normal";
                                        String fbUserName = "D Raj Pandey";
                                        //  fbUserName = fbUserName.matches("[a-zA-Z.? ]*") ? fbUserName : "";
                                        Log.d("fnaklsfnlkanflsa", fbUserEmail + " fb: " + fbUserName);
                                        viewModel.socialLogin(userFirstName, fbUserEmail, fbUserId, "facebook");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("bfjkanflanl", "JSONException: " + e.getMessage());
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,link,gender,birthday,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Dialogs.toast(getApplicationContext(), binding.createAccBtn, "Login Cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("fnbaslnfklsa", "onError: " + exception.toString());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleLogout(this);
    }

    // google logout function
    public void googleLogout(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        } else if (LoginManager.getInstance() != null) {
            LoginManager.getInstance().logOut();
        }
    }

    public void appleLogin() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("apple.com");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        /*binding.ivAppleIcon.setOnClickListener(view -> {
            mAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(
                            authResult -> {
                                Log.d("TAG", "activitySignIn:onSuccess:" + authResult.getUser());
                                FirebaseUser user = authResult.getUser();
                                String name = user.getDisplayName();
                                String email = user.getEmail();
                                String id = user.getProviderId();
                                viewModel.socialLogin(name, email, id, "apple");
                            })
                    .addOnFailureListener(
                            e -> Log.w("TAG", "activitySignIn:onFailure", e));
        });*/
    }
}


