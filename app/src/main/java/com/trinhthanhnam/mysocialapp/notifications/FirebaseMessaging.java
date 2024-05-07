package com.trinhthanhnam.mysocialapp.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trinhthanhnam.mysocialapp.ChatActivity;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        // Safely retrieve 'sent' and 'user' from message data
        String sent = message.getData().get("sented");
        String user = message.getData().get("user");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if 'sent' and 'firebaseUser' are not null before comparing their values
        if (firebaseUser != null && sent != null && sent.equals(firebaseUser.getUid())) {
            // Ensure 'savedCurrentUser' and 'user' are not null before calling .equals()
            if (user != null && !savedCurrentUser.equals(user)) {
                // Determine the correct notification method based on Android version
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    sendOreoNotification(message);
                } else {
                    sendNormalNotification(message);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUID",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOreoNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUID",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         PendingIntent pIntent = PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE );

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification oreoAndAboveNotification = new OreoAndAboveNotification(this);
        Notification.Builder builder = oreoAndAboveNotification.getONotifications(title,body,pIntent,defaultSound,icon);


        int j = 0;
        if (i>0){
            j=i;
        }
        oreoAndAboveNotification.getManager().notify(j,builder.build());
    }
}
