package sk.tuke.smartlock;

import android.content.Context;
import android.widget.Button;

import androidx.core.content.ContextCompat;

import sk.tuke.smartlock.scheduling.WeekDaysNames;
import sk.tuke.smartlock.transmitting.TransmitRules;

public class ButtonsColorSelector {

    private final Context context;

    public ButtonsColorSelector(Context context) {
        this.context = context;
    }

    public void setWeekButtonsColor(Button[] buttons, WeekDaysNames selectedDay) {
        for(int i = 0; i < 7; i++){
            if(i == 0){
                if(i == selectedDay.getValue() - 1){
                    buttons[i].setBackground(ContextCompat.getDrawable(context, R.drawable.button_week_left_highlight));
                }else{
                    buttons[i].setBackground(ContextCompat.getDrawable(context,R.drawable.button_week_left_unset));
                }
            } else if(i == 6){
                if(i == selectedDay.getValue() - 1){
                    buttons[i].setBackground(ContextCompat.getDrawable(context,R.drawable.button_week_right_highlight));
                }else{
                    buttons[i].setBackground(ContextCompat.getDrawable(context,R.drawable.button_week_right_unset));
                }
            } else{
                if(i == selectedDay.getValue() - 1){
                    buttons[i].setBackground(ContextCompat.getDrawable(context,R.drawable.button_week_center_highlight));
                }else{
                    buttons[i].setBackground(ContextCompat.getDrawable(context,R.drawable.button_week_center_unset));
                }
            }
        }
    }

    public void setTransmitButtonsColor(Button[] buttons, TransmitRules rule){
        switch (rule){
            case ALWAYS:
                buttons[0].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background));
                buttons[1].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));
                buttons[2].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));

                break;
            case LOCKED:
                buttons[1].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background));
                buttons[0].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));
                buttons[2].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));
                break;
            case UNLOCKED:
                buttons[2].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background));
                buttons[0].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));
                buttons[1].setBackground(ContextCompat.getDrawable(context,R.drawable.button_background_alt));
        }
    }
}
