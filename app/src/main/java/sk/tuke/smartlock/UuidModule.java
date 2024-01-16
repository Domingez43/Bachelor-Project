package sk.tuke.smartlock;

import android.util.Log;

import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;

import java.util.UUID;

public class UuidModule {

    private SharedPreferencesManager preferencesManager;
    private String uuid;

    public UuidModule(SharedPreferencesManager preferences){
        preferencesManager = preferences;
    }

    public void generateUUID(){
        uuid = modifyUUID(UUID.randomUUID().toString());
        saveUUID();
    }

    public String getUUID(){
        if(uuid.isEmpty()){
            preferencesManager.getStringVal(SharedPreferencesKeys.USER_MAC.getKey(), SharedPreferencesKeys.FIRST_RUN.getDefaultVal());
        }
        return uuid;
    }

    private String modifyUUID(String uuid){
        StringBuilder sb = new StringBuilder();
        Log.e("UUID",uuid);
        int position = 0;
        for(int i = 0; i < 12; i++){
            if(i != 0 && i%2 == 0){
                sb.append('-');
            }
            if(uuid.charAt(position) == '-'){
                position++;
            }
            char c = uuid.charAt(position);
            if (Character.isLowerCase(c)) {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
            position++;
        }
        Log.e("UUID",sb.toString());
        return sb.toString();
    }

    private void saveUUID(){
        preferencesManager.saveStringVal(SharedPreferencesKeys.USER_MAC.getKey(), uuid);
    }
}
