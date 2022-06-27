package com.ebabu.event365live.host.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.activity.EditEventDetails;
import com.ebabu.event365live.host.activity.Login;
import com.ebabu.event365live.host.activity.ViewAllRSVPActivity;
import com.ebabu.event365live.host.api.API;
import com.ebabu.event365live.host.utils.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class EventMessagingService extends FirebaseMessagingService {

    private String eventName = "";

    @Override
    public void onNewToken(String s) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Event", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.e("My Token", token);
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("Uff", remoteMessage.getData().toString());
        Map<String, String> data = remoteMessage.getData();
        String eventId = data.get("eventId");
        String userId = data.get("userId");
        String eventType = data.get("type");
        if (data.containsKey("eventName")) {
            eventName = data.get("eventName");
        } else if (data.containsKey("name")) {
            eventName = data.get("name");
        }
        String message = data.get("message");
        if (eventType != null) {
            int notificationCount = Utility.getNotificationSharedPrefInteger(this, API.NOTIFICATION_COUNT);
            notificationCount = notificationCount+1;
            Utility.setSharedPreferencesInteger(this, API.NOTIFICATION_COUNT, notificationCount);
            updateNotificationCount();
            if (eventType.equalsIgnoreCase("inviteAccepted")) {
                assert eventId != null;
                setNotification(message, ViewAllRSVPActivity.class, Integer.parseInt(eventId));
            } else if (eventType.equalsIgnoreCase("inviteDeclined")) {
                setNotificationWithoutNavigation(message);
            } else if (eventType.equalsIgnoreCase("eventReminder") || eventType.equalsIgnoreCase("eventOfInterest") || eventType.equalsIgnoreCase("eventFav") || eventType.equalsIgnoreCase("hostTicketBooked") || eventType.equalsIgnoreCase("Invited") || eventType.equalsIgnoreCase("checkIn") || eventType.equalsIgnoreCase("eventReview")) {
                assert eventId != null;
                setNotification(message, EditEventDetails.class, Integer.parseInt(eventId));
            } else if (eventType.equalsIgnoreCase("editMember")) {
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
            }
        }
    }

    private void updateNotificationCount() {
        Intent intent = new Intent("notificationsActions");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private PendingIntent startPendingIntent(Class<?> className, int getEventId) {
        Intent intent = new Intent(this, className);
        intent.putExtra(API.ID, getEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    private void setNotification(String msgBody, Class<?> getClassName, int eventId) {


        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            channel = new NotificationChannel("222", "my_channel", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        getApplicationContext(), "222")
                        .setContentTitle(eventName)
                        .setContentText(msgBody)
                        .setAutoCancel(true)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.logo))
                        .setSmallIcon(R.drawable.logo)
                        //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.electro))
                        .setSmallIcon(R.drawable.logo)
                        .setContentIntent(startPendingIntent(getClassName, eventId));

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify(101, builder.build());

    }

    private void setNotificationWithoutNavigation(String msgBody) {


        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            channel = new NotificationChannel("222", "my_channel", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        getApplicationContext(), "222")
                        .setContentTitle(eventName)
                        .setContentText(msgBody)
                        .setAutoCancel(true)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.logo))
                        .setSmallIcon(R.drawable.logo)
                        //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.electro))
                        .setSmallIcon(R.drawable.logo);
//                        .setContentIntent(startPendingIntent(getClassName, eventId));

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        nm.notify(101, builder.build());

    }

}
