package com.ebabu.event365live.host.fragments.rsvp_invites;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.api.ApiInterface;
import com.ebabu.event365live.host.databinding.FragmentMyContactsBinding;
import com.ebabu.event365live.host.fragments.rsvp_invites.adapter.MyContactAdapter;
import com.ebabu.event365live.host.fragments.rsvp_invites.model.MyContact;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.utils.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyContactsFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_READ_CONTACT = 6061;
    MyLoader loader;
    List<MyContact> list = new ArrayList<>(), listMain = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    int eventID;
    String name = "", shareTextMsg = "";
    @Inject
    ApiInterface apiInterface;
    private FragmentMyContactsBinding binding;
    private Context context;
    MyContactAdapter.OnClickInviteListener onClickInviteListener = new MyContactAdapter.OnClickInviteListener() {
        @Override
        public void onClickInvite(int pos) {
//            setupBottomFilterDialog(list.get(pos).getContactNumber());
            shareTextMsg = "https://play.google.com/store/apps/details?id=com.ebabu.event365live";

            Uri uri = Uri.parse("smsto:"+list.get(pos).getContactNumber());
          //  Intent it = new Intent(Intent.ACTION_SENDTO, uri);

            Intent intent = new Intent(Intent.ACTION_SEND, uri);
            String shareBody = shareTextMsg;
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(intent, "Share Using"));
        }
    };
    private MyContactAdapter rsvpAdapter;
    private int limit = 20, page = 1;
    private boolean mContactPermissionGranted;

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_contacts, container, false);
        App.getAppComponent().inject(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventID = getArguments().getInt("id", -1);
        loader = new MyLoader(context);

        linearLayoutManager = new LinearLayoutManager(context);
        binding.rv.setLayoutManager(linearLayoutManager);

        setupInviteContactList();

        getContactPermission();


     /*   binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isFetching) {
                    loadAgain=true;
                    page=1;
                    list.clear();
                    rsvpAdapter.notifyDataSetChanged();
                    getContactFromDevice();
                }
            }
        });*/

        binding.inviteBtn.setOnClickListener(v -> {
            boolean selected = false;
            JsonArray jsonArray = new JsonArray();

            for (MyContact userDAO : list)
                if (userDAO.isChecked())
                    jsonArray.add(userDAO.getId());

            if (jsonArray.size() > 0) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("id", jsonArray);

                loader.show("");

                apiInterface.inviteRSVP(eventID, jsonObject).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        loader.dismiss();
                        if (response.isSuccessful()) {

                            try {
                                JSONObject obj = new JSONObject(response.body().toString());

                                Dialogs.showActionDialog(context,
                                        getString(R.string.app_name),
                                        obj.getString(API.MESSAGE),
                                        "Done",
                                        v1 -> getActivity().finish()
                                );

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (response.code() == API.SESSION_EXPIRE)
                                Utility.sessionExpired(context);

                            else {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                    Dialogs.toast(context, v, jsonObject.getString(API.MESSAGE));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        loader.dismiss();
                        t.printStackTrace();
                    }
                });

                /*loader.dismiss();
                Dialogs.showActionDialog(InviteRSVPActivity.this,
                        getString(R.string.app_name),
                        "Congrats! invitation has been send to selected people.",
                        "Done",
                        v1->{
                            rsvpAdapter.notifyDataSetChanged();
                        }
                );*/

            } else {
                Toast.makeText(context, "Select atleast one contact please!", Toast.LENGTH_LONG).show();
            }
        });


      /*  binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                if(!isFetching && loadAgain) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        getContactFromDevice();

                }
            }
        });*/


        binding.searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                    //if( binding.searchEt.getText().toString().trim().length()>0) {
                    if (!name.equalsIgnoreCase(v.getText().toString())) {
                        name = v.getText().toString();
                        list.clear();

                        if (name.equalsIgnoreCase("")) {
                            list.addAll(listMain);
                        } else {
                            for (int i = 0; i < listMain.size(); i++) {
                                if (is_numeric(name)) {
                                    if (listMain.get(i).getContactNumber().contains(name)) {
                                        list.add(listMain.get(i));
                                    }
                                } else {
                                    if (listMain.get(i).getContactName().toUpperCase().contains(name.toUpperCase())) {
                                        list.add(listMain.get(i));
                                    }
                                }
                            }
                        }

                        rsvpAdapter.notifyDataSetChanged();
                        page = 1;
                        //getContactFromDevice();
                    } else
                        Utility.hideKeyboard(getActivity());
                //}
                return false;
            }
        });


    }

    public boolean is_numeric(String msg) {
        boolean is_num = false;
        for (int i = 0; i < 10; i++) {
            if (msg.contains("" + i)) {
                is_num = true;
                break;
            }
        }
        return is_num;
    }

    private void getContactFromDevice() {
        loader.show("");
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
        }

        try {

            Cursor contacts = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null
            );

            while (contacts.moveToNext()) {

                String id = contacts.getString(
                        contacts.getColumnIndex(ContactsContract.Contacts._ID));

                // Get the current contact name
                String name = contacts.getString(
                        contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));

                // Get the current contact phone number
                String phoneNumber = contacts.getString(
                        contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


                MyContact contact = new MyContact();
                contact.setId(id);
                contact.setContactName(name);
                contact.setContactNumber(phoneNumber);
                contact.setChecked(false);

                list.add(contact);

            }


            Log.v("ContTest", "listSize before> " + list.size());

            Set<MyContact> set = new HashSet<>(list);
            list.clear();
            list.addAll(set);

            MyContact myContact = new MyContact();
            MyContact.SortByName sortByName = myContact.new SortByName();
            Collections.sort(list, sortByName);

            contacts.close();
            Log.v("ContTest", "listSize after> " + list.size());
            listMain.addAll(list);
            rsvpAdapter.refresh(list);

        } catch (Exception e) {
            e.printStackTrace();
            loader.dismiss();
        } finally {
            loader.dismiss();
        }
    }

    private void setupInviteContactList() {
        rsvpAdapter = new MyContactAdapter(context, onClickInviteListener);
        binding.rv.setAdapter(rsvpAdapter);
    }

    private void getContactPermission() {

        mContactPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            mContactPermissionGranted = true;
            getContactFromDevice();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mContactPermissionGranted = true;
                getContactFromDevice();
            }
        }
    }

    public void sendSMS(String number) {

        Uri uri = Uri.parse("smsto:"+number);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", shareTextMsg);
        startActivity(it);

    }

    public void shareOnWhatsApp(Context context, String number) {
        boolean isWhatsApp = false;
        String pkg = "";

        if (appInstalledOrNot(context, "com.whatsapp")) {
            isWhatsApp = true;
            pkg = "com.whatsapp";
        } else if (appInstalledOrNot(context, "com.whatsapp.w4b")) {
            isWhatsApp = true;
            pkg = "com.whatsapp.w4b";
        }


        if (isWhatsApp) {
            PackageManager packageManager = context.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);

            try {
                if (!number.contains("+")) {
                    number = "+91" + number;
                }
                Log.v("ContTest", "number> " + number);
                String url = "https://api.whatsapp.com/send?phone=" + number + "&text=" + URLEncoder.encode("" + shareTextMsg, "UTF-8");
                i.setPackage("com.whatsapp");
                i.setData(Uri.parse(url));
                if (i.resolveActivity(packageManager) != null) {
                    context.startActivity(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



        /*if (isWhatsApp) {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, "The text you wanted to share");
            try {
                whatsappIntent.setPackage(pkg);
                context.startActivity(whatsappIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
            }*/
        } else {
            Toast.makeText(context, "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
        }
    }
}