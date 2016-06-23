package app.Represent.cc;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.softrangers.represent.R;

import android.util.Log;

import java.util.Random;

/**
 * Created by eduard on 06.05.16.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    final static String GROUP_KEY_ALL = "group_key_all";

    static int notificationID = 0;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("data");
        User user = new User.Builder()
                .url(data.getString("url"))
                .message(data.getString("message"))
                .jsonUserData(data.getString("actor"))
                .build();

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(user);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     */
    private void sendNotification(User user) {
        //limit to 5 notifications, send a sound if ID is 1
        notificationID += 1;
        if (notificationID > 5)
            notificationID = 1;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.NOTIFICATION_ACTION);
        intent.putExtra(MainActivity.USER_EXTRAS, user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcer_represent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcer_represent))
                .setContentTitle(user.getName())
                .setContentText(user.getMessage())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
                //.setGroup(GROUP_KEY_ALL);
        if(notificationID == 1)
            notificationBuilder.setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // we should look into using this for updated grouped notifications
        //NotificationManagerCompat notificationManager =
        //        NotificationManagerCompat.from(this);

        //int n_id =  new Random().nextInt();
        notificationManager.notify(notificationID, notificationBuilder.build());

        //summary notifications, disabled
        /*
        Notification summaryNotification = new NotificationCompat.Builder(this)
                .setContentTitle("New interactions on Represent")
                .setSmallIcon(R.mipmap.ic_launcer_represent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcer_represent))
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Alex Faaborg   Check this out")
                        .addLine("Jeff Chang   Launch Party")
                        .setBigContentTitle("New interactions on Represent")
                        .setSummaryText("johndoe@gmail.com"))
                .setGroup(GROUP_KEY_ALL)
                .setGroupSummary(true)
                .build();

        notificationManager.notify(300103, summaryNotification);*/
    }
}
