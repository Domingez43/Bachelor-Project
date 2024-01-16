package sk.tuke.smartlock.transmitting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import sk.tuke.smartlock.MainActivity;
import sk.tuke.smartlock.MqttModule;
import sk.tuke.smartlock.NotificationsModule;
import sk.tuke.smartlock.R;

import sk.tuke.smartlock.receivers.TransmitRulesReceiver;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

public class TransmittingForegroundService extends Service{

    //time in milliseconds that code is transmitted
    private final static int TransmitTime = 1500;

    private final String notificationChannelID = "REQUEST";
    protected static final String TAG = "BeaconTransmitting";

    //parameters provided by User
    private String password = "";
    private float txPower = -60;
    private float periodicity = 10;
    private TransmitRules rule = TransmitRules.ALWAYS;
    private Boolean automaticUnlock;

    //Transmit status
    private boolean transmitting = false;
    private boolean isServiceOn = false;

    //Receiver for handling transmit rules
    private final TransmitRulesReceiver transmitRulesReceiver = new TransmitRulesReceiver(this);
    // intent filter for transmit receiver above
    private final IntentFilter transmitRulesFilter = new IntentFilter();

    //MQTT service which handles connection, receiving and sending messages
    private MqttModule mqttModule;

    //INTENT
    private PendingIntent pendingIntent;

    //Handler used for transmitting
    private Handler taskHandler1;

    private SharedPreferencesManager preferences;

    private NotificationsModule notifications;

    @Override
    public void onCreate() {
        //adding actions for filter
        transmitRulesFilter.addAction(Intent.ACTION_SCREEN_ON);
        transmitRulesFilter.addAction(Intent.ACTION_SCREEN_OFF);
        transmitRulesFilter.addAction(Intent.ACTION_USER_PRESENT);

        //creating instance of shared preferences to get transmit setup
        preferences = new SharedPreferencesManager(getApplicationContext());

        //intent for opening app after clicking on notification
        Intent intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //channel for sending notifications
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelID, notificationChannelID, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(notificationChannel);

        notifications = new NotificationsModule(this,notificationManager);

        mqttModule = new MqttModule(notifications,getMAC());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() != null && intent.getAction().equals("START_ACTION")) {
            Log.e("Start","Start transmitting");
            startForeground(1,createNotification());
        }

        startForeground(1,createNotification());
        // create an MQTT client
        mqttModule.startConnection();

        //receiving params from mainActivity
        dataReceiver();

        cancelNotificationIfRequested(intent);

        //registration of service
        if(rule != TransmitRules.ALWAYS){
            Log.e("REGISTRING","RECEIVER");
            getApplicationContext().registerReceiver(transmitRulesReceiver, transmitRulesFilter);
        }

        //Status for BootCompleteReceiver
        setStatus(true);

        //creating new handler
        taskHandler1 = new Handler();
        startHandler();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startHandler() {
        transmitting = true;
        repetitiveTaskRunnable.run();
    }

    public void stopHandler() {
        transmitting = false;
        taskHandler1.removeCallbacks(repetitiveTaskRunnable);
    }


    private final Runnable repetitiveTaskRunnable = new Runnable() {
        public void run() {
            transmittingLoop();
            taskHandler1.postDelayed(this,(long)periodicity * 1000);
        }
    };


    private void transmittingLoop(){
        BeaconBuilder beaconBuilder = new BeaconBuilder(password,txPower, automaticUnlock);
        Beacon beacon = beaconBuilder.buildBeacon();

        BeaconParser beaconParser = new BeaconParser().setBeaconLayout ("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter bt = new BeaconTransmitter(getApplicationContext(), beaconParser);

        Log.e("ADVERTISING","STARTING" + beaconBuilder.getMinor());
        startAdvertising(bt,beacon);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("ADVERTISING","STOPPING");
                bt.stopAdvertising();
            }
        }, TransmitTime);
    }

    //------------------------------------STARTING ADVERTISING BEACON------------------------------------
    private void startAdvertising(BeaconTransmitter bt,Beacon beacon){
        bt.startAdvertising(beacon, new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
            }
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });
    }

    @Override
    public void onDestroy(){
        setStatus(false);
        mqttModule.disconnectClientV3();
        stopHandler();
        if(rule != TransmitRules.ALWAYS) {
            getApplicationContext().unregisterReceiver(transmitRulesReceiver);
        }
        super.onDestroy();
    }

    private void setStatus(Boolean b) {
        isServiceOn = !isServiceOn;
        SharedPreferences pref = getSharedPreferences("TransmitStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("status",b);
        editor.apply();
    }

    public TransmitRules getRule(){
        return rule;
    }

    public boolean getStatus(){
        return transmitting;
    }

    private Notification createNotification(){
        final String id = "Foreground Service";
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationChannel notificationChannel = new NotificationChannel(id,id, NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
        return new  NotificationCompat.Builder(this,id)
                .setContentText("Transmitting")
                .setContentTitle("DoorLock")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }


    //------------------------------------RECEIVING PASSWORD PERIODICITY AND TX POWER FROM MAIN ACTIVITY------------------------------------
    private void dataReceiver(){
        password = preferences.getStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(),
                SharedPreferencesKeys.USER_PASSWORD.getDefaultVal());

        txPower = preferences.getFloatVal(SharedPreferencesKeys.TXPOWER.getKey(),
                Float.parseFloat(SharedPreferencesKeys.TXPOWER.getDefaultVal()));

        periodicity = preferences.getFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(),
                Float.parseFloat(SharedPreferencesKeys.PERIODICITY.getDefaultVal()));

        automaticUnlock = preferences.getBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(),
                Boolean.parseBoolean(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getDefaultVal()));

        String str = preferences.getStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(), SharedPreferencesKeys.TRANSMIT_RULE.getDefaultVal());
        rule = str.equals("ALWAYS") ? TransmitRules.ALWAYS : str.equals("LOCKED") ? TransmitRules.LOCKED : TransmitRules.UNLOCKED;

        Log.e("password",password);
        Log.e("txPower",String.valueOf(txPower));
        Log.e("periodicity",String.valueOf(periodicity));
    }

    private String getMAC(){
        return preferences.getStringVal(SharedPreferencesKeys.USER_MAC.getKey(),SharedPreferencesKeys.USER_MAC.getDefaultVal());
    }

    public void cancelNotificationIfRequested(Intent intent) {
        if (intent.getBooleanExtra("cancelNotification", false)) {
            notifications.cancelNotification(18);
        }
    }

}
//----------------TRANSMITTER NOTES
//        m - matching byte sequence for this beacon type to parse (exactly one required)
//        s - ServiceUuid for this beacon type to parse (optional, only for Gatt-based beacons)
//        i - identifier (at least one required, multiple allowed)
//        p - power calibration field (exactly one required)
//        d - data field (optional, multiple allowed)
//        x - extra layout. Signifies that the layout is secondary to a primary layout with the same matching byte sequence (or ServiceUuid).
//            Extra layouts do not require power or identifier fields and create Beacon objects without identifiers.
