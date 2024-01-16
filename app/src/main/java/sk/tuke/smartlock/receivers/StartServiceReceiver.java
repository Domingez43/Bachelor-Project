package sk.tuke.smartlock.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import sk.tuke.smartlock.R;
import sk.tuke.smartlock.transmitting.TransmittingForegroundService;

public class StartServiceReceiver extends BroadcastReceiver {

    private final String id = "SCHEDULE";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        NotificationChannel notificationChannel = new NotificationChannel(id,id, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);

        Intent mqttAcceptIntent = new Intent(context.getApplicationContext(), TransmittingForegroundService.class);
        mqttAcceptIntent.putExtra("cancelNotification",true);
        PendingIntent mqttAcceptPendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, mqttAcceptIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notification = new  NotificationCompat.Builder(context.getApplicationContext(),id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("START REQUEST")
                .setChannelId(id)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle("START REQUEST")
                .addAction(R.drawable.ic_outline_calendar_month_24, "ACCEPT", mqttAcceptPendingIntent)
                .setOngoing(false)
                .setAutoCancel(true);

        notificationManager.notify(18, notification.build());
    }
}
