package sk.tuke.smartlock.user;

import android.content.Context;

import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;

public class User{

    private final UserAuthorizationRequest authorization;
    private SharedPreferencesManager preferences;
    private Context context;

    public User(Context context){
        this.context = context;
        authorization = new UserAuthorizationRequest(context);
        preferences = new SharedPreferencesManager(context);
    }

    public UserAuthorizationRequest getAuthorization() {
        return authorization;
    }

    public boolean isAuthorized(){
        return preferences.getBooleanVal(SharedPreferencesKeys.USER_ACTIVATED.getKey(),Boolean.parseBoolean(SharedPreferencesKeys.USER_ACTIVATED.getDefaultVal()));
    }

    public void removeUser(){
        preferences.saveBooleanVal(SharedPreferencesKeys.USER_ACTIVATED.getKey(), false);

        preferences.saveIntVal(SharedPreferencesKeys.USER_ID.getKey(), Integer.parseInt(SharedPreferencesKeys.USER_ID.getDefaultVal()));
        preferences.saveStringVal(SharedPreferencesKeys.USER_MAC.getKey(), SharedPreferencesKeys.USER_MAC.getDefaultVal());
        preferences.saveStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(), SharedPreferencesKeys.USER_PASSWORD.getDefaultVal());
        preferences.saveIntVal(SharedPreferencesKeys.USER_ACCESS_LVL.getKey(),  Integer.parseInt(SharedPreferencesKeys.USER_ACCESS_LVL.getDefaultVal()));
        preferences.saveStringVal(SharedPreferencesKeys.USER_NAME.getKey(), SharedPreferencesKeys.USER_NAME.getDefaultVal());

    }
}
