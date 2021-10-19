/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;


public class AlertNextFit extends BroadcastReceiver {
    @Override

    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("12",
                    "my fitness",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("notification next workout");
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "12")
                .setSmallIcon(R.drawable.ic_notification) // notification icon
                .setContentTitle(context.getString(R.string.userHomeFragment_notification_title)) // title for notification
                .setColor(0XFF00695C)
                .setContentText(context.getString(R.string.userHomeFragment_notification))// message for notification
                .setAutoCancel(true) // clear notification after click
                .setOngoing(true);

        // open back the app after cick on notification
        Intent newIntent = new Intent(context,UserInActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
