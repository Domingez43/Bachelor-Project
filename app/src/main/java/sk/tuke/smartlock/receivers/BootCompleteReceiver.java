package sk.tuke.smartlock.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import sk.tuke.smartlock.transmitting.TransmittingForegroundService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Context cntxt = context.getApplicationContext();
        if ("android.intent.action.BOOT_COMPLETED"
                .equals(intent.getAction())) {
            if (wasServiceRunningBefore(cntxt)
                    && !isTransmittingServiceRunning(cntxt)) {
                Intent intent1 = new Intent(cntxt, TransmittingForegroundService.class);
                cntxt.startForegroundService(intent1);
            }
        }
    }

    private boolean isTransmittingServiceRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(TransmittingForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private boolean wasServiceRunningBefore(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("TransmitStatus", Context.MODE_PRIVATE);
        return pref.getBoolean("status", false);
    }
}
