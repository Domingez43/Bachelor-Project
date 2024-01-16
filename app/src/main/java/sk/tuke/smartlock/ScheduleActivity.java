package sk.tuke.smartlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import sk.tuke.smartlock.database.Weekday;
import sk.tuke.smartlock.scheduling.AlarmSchedulingModule;
import sk.tuke.smartlock.scheduling.WeekDaysDatabaseModule;
import sk.tuke.smartlock.scheduling.WeekDaysNames;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;


public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {

    private int hour;
    private int minute;
    private final Button[] weekdaysButtons = new Button[7];
    private Button confirmBtn;
    private WeekDaysDatabaseModule weekDaysDbModule;

    private WeekDaysNames actualDay;
    private ButtonsColorSelector buttonsColorSelector;

    private AlarmSchedulingModule alarmModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        weekdaysButtons[0] = findViewById(R.id.mon_button);
        weekdaysButtons[1] = findViewById(R.id.tue_button);
        weekdaysButtons[2] = findViewById(R.id.wed_button);
        weekdaysButtons[3] = findViewById(R.id.thu_button);
        weekdaysButtons[4] = findViewById(R.id.fri_button);
        weekdaysButtons[5] = findViewById(R.id.sat_button);
        weekdaysButtons[6] = findViewById(R.id.sun_button);

        ImageButton homeBtn = findViewById(R.id.home_button);
        confirmBtn = findViewById(R.id.confirm_time_button);


        LinearLayout startLayout = findViewById(R.id.start_time_layout);
        LinearLayout stopLayout = findViewById(R.id.stop_time_layout);

        actualDay = WeekDaysNames.MONDAY;

        buttonsColorSelector = new ButtonsColorSelector(getApplicationContext());
        weekDaysDbModule = new WeekDaysDatabaseModule(getApplicationContext(), this);

        alarmModule = new AlarmSchedulingModule(getApplicationContext(),weekDaysDbModule);

        buttonsColorSelector.setWeekButtonsColor(weekdaysButtons,actualDay);

        for(Button button : weekdaysButtons){
            button.setOnClickListener(this);
        }
        homeBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        startLayout.setOnClickListener(this);
        stopLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.mon_button:
                actualDay = WeekDaysNames.MONDAY;
                onButtonClickActions();
                break;

            case R.id.tue_button:
                actualDay = WeekDaysNames.TUESDAY;
                onButtonClickActions();
                break;

            case R.id.wed_button:
                actualDay = WeekDaysNames.WEDNESDAY;
                onButtonClickActions();
                break;

            case R.id.thu_button:
                actualDay = WeekDaysNames.THURSDAY;
                onButtonClickActions();
                break;

            case R.id.fri_button:
                actualDay = WeekDaysNames.FRIDAY;
                onButtonClickActions();
                break;

            case R.id.sat_button:
                actualDay = WeekDaysNames.SATURDAY;
                onButtonClickActions();
                break;

            case R.id.sun_button:
                actualDay = WeekDaysNames.SUNDAY;
                onButtonClickActions();
                break;

            case R.id.start_time_layout:
                openStartTimePicker();
                break;

            case R.id.stop_time_layout:
                openEndTimePicker();
                break;

            case R.id.confirm_time_button:
                onStartButtonClickActions();
                break;

            case R.id.home_button:
                openMainActivity();
                break;

            default:
                break;
        }
    }

    public void onButtonClickActions(){
        setTextTime();
        buttonsColorSelector.setWeekButtonsColor(weekdaysButtons,actualDay);
        changeButtonColor();
    }

    private void onStartButtonClickActions(){
        if(alarmModule.isAlarmScheduled(actualDay)){
            alarmModule.cancelStart(actualDay);
            alarmModule.cancelEnd(actualDay);
            alarmModule.setIsAlarmScheduled(actualDay,false);
        }
        else{
            alarmModule.setStart(actualDay);
            alarmModule.setEnd(actualDay);
            alarmModule.setIsAlarmScheduled(actualDay,true);
            showToast(getApplicationContext(),"Start alarm and Stop Time successfully set!",Toast.LENGTH_LONG);
        }
        changeButtonColor();
    }

    private void setTextTime(){
        TextView startTime = findViewById(R.id.start_time);
        TextView endTime = findViewById(R.id.stop_time);
        Weekday day = weekDaysDbModule.getWeekdayDao().getWeekdayByName(actualDay.getName());
        startTime.setText(day.startTime);
        endTime.setText(day.endTime);
    }

    private void setAllDay(){
        setStartTime(0,0);
        setEndTime(23,59);
    }

    private void changeButtonColor(){
        if(alarmModule.isAlarmScheduled(actualDay)){
            confirmBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_background_stop));
            confirmBtn.setText("REMOVE");
        }
        else{
            confirmBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.button_background_start));
            confirmBtn.setText("SET");
        }
    }

    private void openStartTimePicker(){
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Start Time")
                .setHour(weekDaysDbModule.getHour(actualDay,"start"))
                .setMinute(weekDaysDbModule.getMinute(actualDay,"start"))
                .build();
        picker.show(getSupportFragmentManager(),"Start Time");
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hour = picker.getHour();
                minute = picker.getMinute();
                setStartTime(hour,minute);
            }
        });
    }

    private void openEndTimePicker(){
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select End Time")
                .setHour(weekDaysDbModule.getHour(actualDay,"stop"))
                .setMinute(weekDaysDbModule.getMinute(actualDay,"stop"))
                .build();
        picker.show(getSupportFragmentManager(),"End Time");
        picker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hour = picker.getHour();
                minute = picker.getMinute();
                setEndTime(hour,minute);
            }
        });
    }

    private void setStartTime(int hour, int minute){
        TextView startTime = findViewById(R.id.start_time);
        startTime.setText(convertTimeToString(hour,minute));
        weekDaysDbModule.getWeekdayDao().updateStartTimeByName(actualDay.getName(),convertTimeToString(hour,minute));

    }

    private void setEndTime(int hour, int minute){
        TextView endTime = findViewById(R.id.stop_time);
        endTime.setText(convertTimeToString(hour,minute));
        weekDaysDbModule.getWeekdayDao().updateEndTimeByName(actualDay.getName(),convertTimeToString(hour,minute));
    }


    private String convertTimeToString(int hour, int minute){
        if(hour < 10 && minute < 10){
            return ("0" + hour + ":0" + minute);
        }else if(hour < 10 && minute > 9 ){
            return ("0" + hour + ":" + minute);
        }else if(hour > 9 && minute < 10 ) {
            return ((hour) + ":0" + (minute));
        }else{
            return((hour) + ":" + (minute));
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public ScheduleActivity getActivity(){
        return this;
    };
}