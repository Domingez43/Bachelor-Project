package sk.tuke.smartlock;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import sk.tuke.smartlock.transmitting.TransmittingForegroundService;

public class NotificationsModule {

    private final String id = "REQUEST";
    private final TransmittingForegroundService service;
    private final NotificationManagerCompat notificationManager;
    private JsonParser jsonParser;

    public NotificationsModule(TransmittingForegroundService service, NotificationManagerCompat manager) {
        this.notificationManager = manager;
        this.service = service;
        jsonParser = new JsonParser();
    }

    private Notification createNotification(String title, String time, int notificationID){

        PendingIntent acceptPendingIntent = createPendingIntent(notificationID,true);
        PendingIntent declinePendingIntent = createPendingIntent(notificationID,false);

        return new  NotificationCompat.Builder(service.getApplicationContext(),"REQUEST")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("SENSOR ID " + title + "\nTime: " + time)
                .setChannelId(id)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle("UNLOCK REQUEST")
                .addAction(R.drawable.ic_outline_calendar_month_24, "ACCEPT", acceptPendingIntent)
                .addAction(R.drawable.ic_outline_calendar_month_24, "DECLINE", declinePendingIntent)
                .setAutoCancel(true)
                .build();
    }

    public void publishNotification(String message){
        int notificationID = Integer.parseInt(jsonParser.parseRequestID(message)) + 100;
        String sensorID = jsonParser.parseSensorID(message);
        String time = jsonParser.parseTimestamp(message);

        notificationManager.notify(notificationID, createNotification(sensorID, time, notificationID));
    }

    public void cancelNotification(int id){
        notificationManager.cancel(id);
    };

    private PendingIntent createPendingIntent(int notificationID,boolean isAccepted){
        Intent intent = new Intent(service.getApplicationContext(),MqttMessageService.class);

        Bundle extras = new Bundle();
        extras.putInt("notificationID",notificationID);
        extras.putBoolean("accept",isAccepted);
        intent.putExtras(extras);

        if(isAccepted){
            return PendingIntent.getService(service.getApplicationContext(), notificationID, intent, PendingIntent.FLAG_IMMUTABLE);
        }else{
            return PendingIntent.getService(service.getApplicationContext(), notificationID + 5000, intent, PendingIntent.FLAG_IMMUTABLE);
        }
    }
}
