package sk.tuke.smartlock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

public class MqttMessageService extends Service {

    private RequestMqttModule mqtt;
    private boolean isAccepted;
    private int notificationID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isAccepted = intent.getBooleanExtra("accept",false);
        notificationID = intent.getIntExtra("notificationID",2);
        this.mqtt = new RequestMqttModule(getApplicationContext(),notificationID - 100);
        this.mqtt.startConnection();

        sendMessage();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessage(){
        mqtt.publishV3(isAccepted);
        mqtt.disconnectClientV3();
        removeNotification();
        stopSelf();
    }

    private void removeNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        Log.e("notificationID 2", String.valueOf(notificationID));
        notificationManager.cancel(notificationID);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
