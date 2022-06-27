package com.ebabu.event365live.host.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ebabu.event365live.host.BaseActivity;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.ActivitySignUpBinding;
import com.ebabu.event365live.host.events.FailureEvent;
import com.ebabu.event365live.host.events.SuccessEvent;
import com.ebabu.event365live.host.utils.Constants;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.ebabu.event365live.host.viewmodels.SignUpViewModel;
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

public class SignUp extends BaseActivity {

    ActivitySignUpBinding binding;
    SignUpViewModel signUpViewModel;
    MyLoader loader;
    boolean isMasking = true;

    //newly added code
    private GoogleSignInClient mGoogleSignInClint;
    private int RC_SIGN_IN_REQUEST = 1001;
    private static int REGISTER_CODE = 1002;
    private CallbackManager callbackManager;
    private String  getSocialImg;
    private int userId;
    @Inject
    ApiInterface apiInterface;
    Snackbar networkBar;
    String IPaddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_sign_up);
        signUpViewModel= ViewModelProviders.of(this).get(SignUpViewModel.class);
        signUpViewModel.init();
        loader=new MyLoader(this);
        binding.setViewmodel(signUpViewModel);
        signUpViewModel.getIsFailed().observe(this, s -> Dialogs.toast(getApplicationContext(), binding.parentLayout, s));
        signUpViewModel.getIsConnecting().observe(this,aBoolean -> loader.show(""));
        binding.backArrow.setOnClickListener(v->onBackPressed());
        addWatcher();

        //newly addded code

        callbackManager = CallbackManager.Factory.create();

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

        appleLogin();
        facebookSignIn();
        getFirebaseToken();
        getDeviceId();
        NetworkDetect();
        networkBar = Snackbar.make(binding.getRoot(), "No internet connection...", Snackbar.LENGTH_INDEFINITE);

    }



    private void addWatcher() {
        binding.fullNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()>=2){
                    binding.fullnameIcon.animate().alpha(1f).setDuration(500);
                }
                else{
                    binding.fullnameIcon.animate().alpha(0f).setDuration(500);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        //validation for PASSWORD IMAGE
        binding.pwdEyeimg.setOnClickListener(v -> {
            if (binding.pwdEt.getText().toString().trim().length() > 0) {

                isMasking = !isMasking;
                if (isMasking) {
                    binding.pwdEyeimg.setImageResource(R.drawable.eye_gray);
                    binding.pwdEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    binding.pwdEt.setInputType(InputType.TYPE_CLASS_TEXT);
                    binding.pwdEyeimg.setImageResource(R.drawable.eye);
                }
                binding.pwdEt.setSelection(binding.pwdEt.getText().toString().trim().length());
            }

        });

        //VALIDATION FOR PASSWORD
        binding.pwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if ((charSequence.toString().trim()).length()==0) {
                    binding.pwdImg.setVisibility(View.INVISIBLE);
                    binding.pwdEyeimg.setVisibility(View.INVISIBLE);
                }else if ((charSequence.toString().trim()).length()>=8 && Constants.VALID_PASSWORD_REGEX.matcher(charSequence.toString().trim()).matches()){
                    binding.pwdImg.setVisibility(View.VISIBLE);
                    binding.pwdEyeimg.setVisibility(View.VISIBLE);
                }else{
                    binding.pwdImg.setVisibility(View.INVISIBLE);
                    binding.pwdEyeimg.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        binding.mailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(validateEmail(charSequence.toString()))
                    binding.validMailImg.animate().alpha(1f).setDuration(500);
                else
                    binding.validMailImg.animate().alpha(0f).setDuration(500);

            }

            @Override
            public void afterTextChanged(Editable editable) {            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailureEvent(FailureEvent event) {
        loader.dismiss();
        Dialogs.toast(getApplicationContext(),binding.createAccBtn,event.getMsg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessEvent(SuccessEvent event) {
        event.getMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                loader.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String msg=jsonObject.getString(API.MESSAGE);
                    if(jsonObject.getBoolean(API.SUCCESS)){
                        int id=jsonObject.getJSONObject(API.DATA).getInt(API.ID);
                        Log.e("id",id+"");
                        Intent intent=new Intent(SignUp.this,VerifiyCode.class);
                        intent.putExtra(API.ID,id);
                        intent.putExtra(API.EMAIL,binding.mailEt.getText().toString());
                        intent.putExtra(Constants.FROM,API.SIGNUP);
                        startActivity(intent);
                    }
                    else{
                        Log.e("success",false+"");
                        if (jsonObject.getInt(API.CODE) == 434) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            int id = jsonObject.getJSONObject(API.DATA).getInt(API.ID);
                            Log.e("id", id + "");
                            Intent intent = new Intent(SignUp.this, SMSVerification.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(API.ID, id);
                            intent.putExtra(Constants.FROM, API.SIGNUP);
                            startActivity(intent);
                        }else
                            Dialogs.toast(getApplicationContext(),binding.createAccBtn,msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Dialogs.toast(getApplicationContext(),binding.createAccBtn,"something went wrong!");
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
        EventBus.getDefault().unregister(this);
    }

    private boolean validateEmail(String email){
        if (email.startsWith(".") || email.startsWith("_") || email.contains("..") || email.contains("__")
                || email.contains("._") || email.contains("_.") || email.contains(".@") || email.contains("_@")) {
            return false;
        }
        return Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches();

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

    //updateSocialLogin Data method
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
                    Intent intent = new Intent(SignUp.this, Home.class);
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


    //google code

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
            if (account != null) {
                String name = account.getDisplayName();
                String email = account.getEmail();
                String id = account.getId();
                if (account.getPhotoUrl() != null)
                    getSocialImg = account.getPhotoUrl().toString();
                signUpViewModel.socialLogin(name, email, id, "google");
            }
        } catch (ApiException e) {
            e.printStackTrace();
            Log.d("bakbfjbafa", "ApiException: " + e.getMessage());
        }
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


    //facebook signup code

    private void facebookSignIn() {
        //newly added
    //    CallbackManager callbackManager = CallbackManager.Factory.create();

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
                                        signUpViewModel.socialLogin(userFirstName, fbUserEmail, fbUserId, "facebook");
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

//apple login code

    public void appleLogin() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("apple.com");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
      /*  binding.ivAppleIcon.setOnClickListener(view -> {
            mAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(
                            authResult -> {
                                Log.d("TAG", "activitySignIn:onSuccess:" + authResult.getUser());
                                FirebaseUser user = authResult.getUser();
                                String name = user.getDisplayName();
                                String email = user.getEmail();
                                String id = user.getProviderId();
                                signUpViewModel.socialLogin(name, email, id, "apple");
                            })
                    .addOnFailureListener(
                            e -> Log.w("TAG", "activitySignIn:onFailure", e));
        });*/
    }



}
