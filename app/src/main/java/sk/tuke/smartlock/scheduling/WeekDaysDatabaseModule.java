package sk.tuke.smartlock.scheduling;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import sk.tuke.smartlock.ButtonsColorSelector;
import sk.tuke.smartlock.ScheduleActivity;
import sk.tuke.smartlock.database.Weekday;
import sk.tuke.smartlock.database.WeekdayDao;
import sk.tuke.smartlock.database.WeekdaysDatabase;

import java.time.LocalTime;
import java.util.Objects;

public class WeekDaysDatabaseModule {

    private ButtonsColorSelector buttonsColorSelector;
    private WeekdaysDatabase weekdaysDatabase;
    private WeekdayDao weekdayDao;
    private final Context context;

    public WeekDaysDatabaseModule(Context context, ScheduleActivity scheduleActivity) {
        this.context = context;
        weekdaysDatabase = getDatabase(scheduleActivity);
        weekdayDao = weekdaysDatabase.weekdayDao();
    }

    public int getHour(WeekDaysNames actualDay, String type) {
        String time = "";
        if(Objects.equals(type, "stop")){
            time = weekdayDao.getEndTime(actualDay.getName());
        }else if(Objects.equals(type, "start")){
            time = weekdayDao.getStartTime(actualDay.getName());
        }

        String hour = time.substring(0,2);
        return  Integer.parseInt(hour);
    }

    public int getMinute(WeekDaysNames actualDay, String type) {
        String time = "";
        if(Objects.equals(type, "stop")){
            time = weekdayDao.getEndTime(actualDay.getName());
        }else if(Objects.equals(type, "start")){
            time = weekdayDao.getStartTime(actualDay.getName());
        }
        String minute = time.substring(3,5);
        return  Integer.parseInt(minute);
    }

    public WeekdayDao getWeekdayDao() {
        return weekdayDao;
    }

    private WeekdaysDatabase getDatabase(ScheduleActivity scheduleActivity){
        return Room.databaseBuilder(context,WeekdaysDatabase.class,"weekdays").allowMainThreadQueries().addCallback(new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                WeekdaysDatabase database = WeekdaysDatabase.getInstance(context);
                WeekdayDao weekdayDao = database.weekdayDao();

                LocalTime startTime = LocalTime.of(7,0);
                LocalTime endTime = LocalTime.of(16,30);

                Weekday monday = new Weekday(1,WeekDaysNames.MONDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday tuesday = new Weekday(2,WeekDaysNames.TUESDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday wednesday = new Weekday(3,WeekDaysNames.WEDNESDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday thursday = new Weekday(4,WeekDaysNames.THURSDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday friday = new Weekday(5,WeekDaysNames.FRIDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday saturday = new Weekday(6,WeekDaysNames.SATURDAY.getName(),startTime.toString(),endTime.toString(),false);
                Weekday sunday = new Weekday(7,WeekDaysNames.SUNDAY.getName(),startTime.toString(),endTime.toString(),false);

                AsyncTask.execute(() -> {
                    weekdayDao.insertAll(monday,tuesday,wednesday,thursday,friday,saturday,sunday);
                });
                Log.e("print","F");
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
                WeekdaysDatabase database = WeekdaysDatabase.getInstance(context);
//                scheduleActivity.getActivity().onButtonClickActions();
            }
        }).build();
    }


}
