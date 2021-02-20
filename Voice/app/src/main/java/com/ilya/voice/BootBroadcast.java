package com.ilya.voice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class BootBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        try {
            Log.i("mylog", "BROAD start");
            Intent resultIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,"default")
                    .setSmallIcon(android.R.drawable.ic_menu_view)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle("Open application")
                    .setOngoing(true)
                    .setChannelId("BootBroadcast")
                    .setShowWhen(false)
                    .setNotificationSilent()
                    .setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    "BootBroadcast",
                    "Voice",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(MainActivity.NOTIFY_ID,builder.build());
        }catch (Exception e){}
    }
}
