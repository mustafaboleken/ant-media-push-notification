package io.bola.pushnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DeclineCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Call declined.", Toast.LENGTH_SHORT).show();

        NotificationHelper.dismissCallNotification(context);

        Intent callIntent = new Intent(context, PeerForNotificationActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }
}
