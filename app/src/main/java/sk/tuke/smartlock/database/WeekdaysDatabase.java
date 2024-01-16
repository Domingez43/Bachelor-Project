package sk.tuke.smartlock.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Weekday.class},version = 1)
public abstract class WeekdaysDatabase extends RoomDatabase {

    public abstract WeekdayDao weekdayDao();
    private static volatile WeekdaysDatabase INSTANCE;

    public static WeekdaysDatabase getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),WeekdaysDatabase.class,"weekdays").build();
        }
        return INSTANCE;
    }
}
