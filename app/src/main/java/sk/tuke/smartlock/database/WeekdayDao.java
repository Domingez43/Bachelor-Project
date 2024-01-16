package sk.tuke.smartlock.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WeekdayDao {

    @Insert
    void insertAll(Weekday...weekdays);

    @Query("SELECT * FROM weekdays")
    List<Weekday> getAllDays();

    @Query("SELECT * FROM weekdays WHERE weekName = :name LIMIT 1")
    Weekday getWeekdayByName(String name);

    @Query("UPDATE weekdays SET startTime = :startTime WHERE weekName = :name")
    void updateStartTimeByName(String name, String startTime);

    @Query("UPDATE weekdays SET endTime = :endTime WHERE weekName = :name")
    void updateEndTimeByName(String name, String endTime);

    @Query("SELECT startTime FROM weekdays WHERE weekName = :name LIMIT 1")
    String getStartTime(String name);

    @Query("SELECT endTime FROM weekdays WHERE weekName = :name LIMIT 1")
    String getEndTime(String name);

    @Query("SELECT isAlarmSet FROM weekdays WHERE weekName = :name LIMIT 1")
    Boolean isAlarmSet(String name);

    @Query("UPDATE weekdays SET isAlarmSet = :b WHERE weekName = :name")
    void updateIsAlarmSet(String name, Boolean b);
}
