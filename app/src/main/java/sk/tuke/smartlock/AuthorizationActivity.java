package sk.tuke.smartlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import sk.tuke.smartlock.R;

import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;
import sk.tuke.smartlock.user.User;

public class AuthorizationActivity extends AppCompatActivity {

    private User user;
    private SharedPreferencesManager preferences;
    private ImageButton homeBtn;
    private Button requestBtn;
    private EditText name;
    TextView statusTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        preferences = new SharedPreferencesManager(this);
        user = new User(this);

        statusTxt = findViewById(R.id.status_text);

        homeBtn = findViewById(R.id.home_button);
        requestBtn = findViewById(R.id.request_button);
        name = findViewById(R.id.user_name_editText);

        if(!user.isAuthorized()){
            requestBtn.setEnabled(false);
        }

        setStatus();

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = name.getText().toString().trim();
                if(!user.isAuthorized()){
                    requestBtn.setEnabled(input.length() > 9);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainActivity();
            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.isAuthorized()) {
                    user.removeUser();
                    setStatus();
                }else{
                    user.getAuthorization().authorizationRequest(name.getText().toString(), getThisActivity());
                    setStatus();
                }
            }
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void setStatus(){
        if(user.isAuthorized()){
            String name = preferences.getStringVal(SharedPreferencesKeys.USER_NAME.getKey(),SharedPreferencesKeys.USER_NAME.getDefaultVal());
            statusTxt.setText("AUTHORIZED AS " + name);
            statusTxt.setTextColor(Color.WHITE);
            requestBtn.setEnabled(true);
            requestBtn.setText("REMOVE USER");
        }
        else{
            statusTxt.setText("STATUS : UNAUTHORIZED");
            statusTxt.setTextColor(Color.rgb(147, 0, 10));
            requestBtn.setEnabled(false);
            requestBtn.setText("REQUEST");
        }
    }

    private AuthorizationActivity getThisActivity(){
        return this;
    }
}