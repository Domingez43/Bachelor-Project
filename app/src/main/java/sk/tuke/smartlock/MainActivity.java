package sk.tuke.smartlock;


import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;
import sk.tuke.smartlock.transmitting.TransmitRules;
import sk.tuke.smartlock.transmitting.TransmittingForegroundService;
import sk.tuke.smartlock.user.User;
import com.google.android.material.slider.Slider;


import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Settings and params
    private String password = "";
    private float txPower = -60;
    private float periodicity = 10;
    private TransmitRules rule = TransmitRules.ALWAYS;

    //password, txPower, and periodicity are saved in the Shared Preferences
    //shared preferences are used for saving data when app is killed
    private SharedPreferencesManager preferencesManager;

    //permissions
    ActivityResultLauncher<String[]> bluetoothPermissions;
    private boolean isBluetoothGranted = false;

    private User user;

    //private Button firebaseBtn;

    private Button settingsBtn;
    private Button scheduleBtn;
    private Button transmitBtn;
    private Button profileBtn;
    private TextView timestamp;

    private ButtonsColorSelector buttonsColorSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FORCING DARK MODE IN APP
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        user = new User(this);

        //getting data saved in shared preferences
        preferencesManager = new SharedPreferencesManager(this);

        //TEXT VIEWS
        timestamp = findViewById(R.id.timestamp);

        //BUTTONS
        settingsBtn = findViewById(R.id.settings_button);
        scheduleBtn = findViewById(R.id.schedule_btn);
        profileBtn = findViewById(R.id.profile_btn);
        transmitBtn = findViewById(R.id.transmit_btn);

        buttonsColorSelector = new ButtonsColorSelector(getApplicationContext());

        setTransmitButtonColor();
        updateProperties();

        initializationActions();

        bluetoothPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.BLUETOOTH_ADVERTISE) != null){
                    isBluetoothGranted = result.get(Manifest.permission.BLUETOOTH_ADVERTISE);
                }
            }
        });
        requestPermission();
    }

    private void openAuthorizationActivity() {
        Intent intent = new Intent(MainActivity.this, AuthorizationActivity.class);
        startActivity(intent);
    }

    private void openScheduleActivity() {
        Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
        startActivity(intent);
    }

    //settingsButton
    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.settings_layout);

        Button[] transmitButtons = new Button[3];
        transmitButtons[0]  = dialog.findViewById(R.id.always_button);
        transmitButtons[1] = dialog.findViewById(R.id.locked_button);
        transmitButtons[2] = dialog.findViewById(R.id.unlocked_button);


        SwitchCompat switchButton = dialog.findViewById(R.id.switch_button);
        buttonsColorSelector.setTransmitButtonsColor(transmitButtons,rule);

        TextView statusTxt = dialog.findViewById(R.id.switch_status);
        Slider periodicitySlider = dialog.findViewById(R.id.periodicity_slider);
        Slider txPowerSlider = dialog.findViewById(R.id.TXPower_slider);
        TextView periodicityVal = dialog.findViewById(R.id.periodicity_val);
        TextView tXPowerVal = dialog.findViewById(R.id.txpower_val);

        //setting values
        periodicitySlider.setValue(periodicity);
        txPowerSlider.setValue(txPower);
        periodicityVal.setText(String.format("%.0f", periodicity));
        tXPowerVal.setText(String.format("%.0f", txPower));
        switchButton.setChecked(preferencesManager.getBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(),true));
        if(preferencesManager.getBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(),true)){
            statusTxt.setText("ENABLED");
        }else {
            statusTxt.setText("DISABLED");
        }

        transmitButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.ALWAYS;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(), TransmitRules.ALWAYS.toString());
                buttonsColorSelector.setTransmitButtonsColor(transmitButtons,rule);
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart transmission to apply the changes", Toast.LENGTH_SHORT);
                }
                updateProperties();
            }
        });

        transmitButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.LOCKED;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),TransmitRules.LOCKED.toString());
                buttonsColorSelector.setTransmitButtonsColor(transmitButtons,rule);
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart transmission to apply the changes", Toast.LENGTH_SHORT);
                }
            }
        });
        transmitButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule = TransmitRules.UNLOCKED;
                preferencesManager.saveStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),TransmitRules.UNLOCKED.toString());
                buttonsColorSelector.setTransmitButtonsColor(transmitButtons,rule);
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart the transmission to apply the changes", Toast.LENGTH_SHORT);
                }
                updateProperties();
            }
        });

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean actualVal = preferencesManager.getBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(), Boolean.parseBoolean(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getDefaultVal()));
                preferencesManager.saveBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(),!actualVal);

                if(actualVal){
                    statusTxt.setText("DISABLED");
                }else {
                    statusTxt.setText("ENABLED");
                }
                Log.e("AUTOMATIC",String.valueOf(!actualVal));
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart the transmission to apply the changes", Toast.LENGTH_SHORT);
                }
                updateProperties();
            }
        });

        //periodicity slider
        periodicitySlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                periodicity = periodicitySlider.getValue();
                periodicityVal.setText(String.format("%.0f", periodicity));
                preferencesManager.saveFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(), periodicity);
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart the transmission to apply the changes", Toast.LENGTH_SHORT);
                }
                updateProperties();
            }
        });

        //txPower slider
        txPowerSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                txPower = txPowerSlider.getValue();
                tXPowerVal.setText(String.format("%.0f", txPower));
                preferencesManager.saveFloatVal(SharedPreferencesKeys.TXPOWER.getKey(), txPower);
                if(isTransmittingServiceRunning()){
                    showToast(getApplicationContext(),"Restart the transmission to apply the changes", Toast.LENGTH_SHORT);
                }
                updateProperties();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //requiring bt advertising permission, new added in recent versions of API
    private void requestPermission(){
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        isBluetoothGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionsRequests = new ArrayList<>();

        if(!isBluetoothGranted){
            permissionsRequests.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }

        bluetoothPermissions.launch(permissionsRequests.toArray(new String[0]));
    }


    //METHOD VERIFY IF SERVICE IS RUNNING
    public boolean isTransmittingServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(TransmittingForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private void getSavedData(){
        password = preferencesManager.getStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(),
                SharedPreferencesKeys.USER_PASSWORD.getDefaultVal());

        periodicity =  preferencesManager.getFloatVal(SharedPreferencesKeys.PERIODICITY.getKey(),
                Float.parseFloat(SharedPreferencesKeys.PERIODICITY.getDefaultVal()));

        txPower = preferencesManager.getFloatVal(SharedPreferencesKeys.TXPOWER.getKey(),
                Float.parseFloat(SharedPreferencesKeys.TXPOWER.getDefaultVal()));

        String str = preferencesManager.getStringVal(SharedPreferencesKeys.TRANSMIT_RULE.getKey(),
                SharedPreferencesKeys.TRANSMIT_RULE.getDefaultVal());

        rule = str.equals("ALWAYS") ? TransmitRules.ALWAYS : str.equals("LOCKED") ? TransmitRules.LOCKED : TransmitRules.UNLOCKED;
    }

    private void firstLaunch(){
       boolean firstRun =  preferencesManager.getBooleanVal(SharedPreferencesKeys.FIRST_RUN.getKey(), Boolean.parseBoolean(SharedPreferencesKeys.FIRST_RUN.getDefaultVal()));
        if(firstRun){
            openAuthorizationActivity();
            preferencesManager.saveBooleanVal(SharedPreferencesKeys.FIRST_RUN.getKey(), false);
        }
    }

    private void setTransmitButtonColor(){
        if(isTransmittingServiceRunning()){
            transmitBtn.setText("STOP");
            transmitBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_background_stop));
            timestamp.setText("TRANSMITTING");
        }
        else if(!isTransmittingServiceRunning()){
            transmitBtn.setText("START");
            transmitBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_background_start));
            timestamp.setText("STOPPED");
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.settings_button:
                showDialog();
                String str =  isTransmittingServiceRunning() ? "IS RUNNING" : "IS NOT RUNNING";
                Log.e("VALIDATION",str);
                break;
            case R.id.schedule_btn:
                openScheduleActivity();
                break;
            case R.id.profile_btn:
                openAuthorizationActivity();
                break;

            case R.id.transmit_btn:
                Intent TransmitServiceIntent = new Intent(getApplicationContext(), TransmittingForegroundService.class);
                if(!isTransmittingServiceRunning()){
                    Log.e("VALIDATION","IS NOT RUNNING, STARTING SERVICE");
                    showToast(getApplicationContext(),"Starting Transmitting",Toast.LENGTH_SHORT);
                    startForegroundService(TransmitServiceIntent);
                }
                else if(isTransmittingServiceRunning()){
                    getApplicationContext().stopService(TransmitServiceIntent);
                    showToast(getApplicationContext(),"Stopping Transmitting",Toast.LENGTH_SHORT);
                    Log.e("VALIDATION","IS RUNNING, STOPPING SERVICE");
                }
                setTransmitButtonColor();
                updateProperties();

            default:
                break;
        }
    }

    private void initializationActions(){
        getSavedData();
        firstLaunch();
        buttonSetup();
        toggleTransmitButtonState();
        updateProperties();
    }

    private void updateProperties(){
        TextView restrictionsTxt  = findViewById(R.id.restrictions_txt);
        TextView unlockModeTxt  = findViewById(R.id.mode_txt);
        TextView periodicityTxt  = findViewById(R.id.periodicity_txt);
        TextView txPowerTxt  = findViewById(R.id.txpower_txt);

        if(rule == TransmitRules.ALWAYS){
            restrictionsTxt.setText("ALWAYS");
        } else if(rule == TransmitRules.LOCKED){
            restrictionsTxt.setText("LOCKED");
        }else {
            restrictionsTxt.setText("UNLOCKED");
        }
        if(preferencesManager.getBooleanVal(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getKey(), Boolean.parseBoolean(SharedPreferencesKeys.AUTOMATIC_UNLOCK.getDefaultVal()))){
            unlockModeTxt.setText("AUTOMATIC");
        }else{
            unlockModeTxt.setText("MANUAL");
        }

        txPowerTxt.setText(String.valueOf(txPower));
        periodicityTxt.setText(periodicity + " sec");

    }


    private void buttonSetup(){
        settingsBtn.setOnClickListener(this);
        scheduleBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);
        transmitBtn.setOnClickListener(this);
    }


    private void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    private void toggleTransmitButtonState(){
        if( preferencesManager.getBooleanVal(SharedPreferencesKeys.USER_ACTIVATED.getKey(),Boolean.parseBoolean(SharedPreferencesKeys.USER_ACTIVATED.getDefaultVal()))){
            transmitBtn.setEnabled(true);
        }else {
            transmitBtn.setEnabled(false);
        }
    }
}