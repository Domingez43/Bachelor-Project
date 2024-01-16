package sk.tuke.smartlock.scheduling;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import sk.tuke.smartlock.receivers.StartServiceReceiver;
import sk.tuke.smartlock.receivers.StopForegroundServiceReceiver;

import java.util.Calendar;

public class AlarmSchedulingModule {

    private final Context context;
    private final WeekDaysDatabaseModule weekDaysDbModule;
    private AlarmManager alarmManager;


    public AlarmSchedulingModule(Context context, WeekDaysDatabaseModule weekDaysDbModule) {
        this.context = context;
        this.weekDaysDbModule = weekDaysDbModule;
        alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }


    public void setStart(WeekDaysNames actualDay){
        Intent intent = new Intent(context.getApplicationContext(), StartServiceReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), actualDay.getValue(), intent,
                PendingIntent.FLAG_IMMUTABLE |  PendingIntent.FLAG_CANCEL_CURRENT);

        Log.e("DAY",actualDay.getName());
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, getActualDayVal(actualDay));
        calendar.set(Calendar.HOUR_OF_DAY, weekDaysDbModule.getHour(actualDay,"start"));
        calendar.set(Calendar.MINUTE, weekDaysDbModule.getMinute(actualDay,"start"));
        Log.e("minute",String.valueOf(weekDaysDbModule.getMinute(actualDay,"start")));
        calendar.set(Calendar.SECOND, 0);
        Log.e("start","schedule");
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7 ,pendingIntent); //
    }


    public void setEnd(WeekDaysNames actualDay){
        Intent intent = new Intent(context.getApplicationContext(), StopForegroundServiceReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), actualDay.getValue() + 10, intent,
                PendingIntent.FLAG_IMMUTABLE |  PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, getActualDayVal(actualDay));
        calendar.set(Calendar.HOUR_OF_DAY, weekDaysDbModule.getHour(actualDay,"stop"));
        calendar.set(Calendar.MINUTE, weekDaysDbModule.getMinute(actualDay,"stop"));
        calendar.set(Calendar.SECOND, 0);
        Log.e("end","SCHEDULE");

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7 , pendingIntent); // AlarmManager.INTERVAL_DAY * 7,
    }


    public void cancelStart(WeekDaysNames actualDay){
        Intent intent = new Intent(context.getApplicationContext(), StartServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), actualDay.getValue(),intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }


    public void cancelEnd(WeekDaysNames actualDay){
        Intent intent = new Intent(context.getApplicationContext(), StopForegroundServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), actualDay.getValue() + 10, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }


    public boolean isAlarmScheduled(WeekDaysNames actualDay){
        return weekDaysDbModule.getWeekdayDao().isAlarmSet(actualDay.getName());
    }

    public void setIsAlarmScheduled(WeekDaysNames actualDay, Boolean b){
        weekDaysDbModule.getWeekdayDao().updateIsAlarmSet(actualDay.getName(),b);
    }


    private int getActualDayVal(WeekDaysNames actualDay){
        switch (actualDay){
            case MONDAY:
                return Calendar.MONDAY;
            case TUESDAY:
                return Calendar.TUESDAY;
            case WEDNESDAY:
                return Calendar.WEDNESDAY;
            case THURSDAY:
                return Calendar.THURSDAY;
            case FRIDAY:
                return Calendar.FRIDAY;
            case SATURDAY:
                return Calendar.SATURDAY;
            case SUNDAY:
                return Calendar.SUNDAY;
            default:
                return 2;
        }
    }
}
