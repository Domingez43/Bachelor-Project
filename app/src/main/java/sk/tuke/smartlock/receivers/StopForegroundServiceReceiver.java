package sk.tuke.smartlock.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sk.tuke.smartlock.transmitting.TransmittingForegroundService;

public class StopForegroundServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(TransmittingForegroundService.class.getName().equals(service.service.getClassName())){
                Intent serviceIntent = new Intent(context, TransmittingForegroundService.class);
                context.stopService(serviceIntent);
            }
        }
    }
}
