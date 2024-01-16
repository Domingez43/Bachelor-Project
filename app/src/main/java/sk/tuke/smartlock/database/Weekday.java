package sk.tuke.smartlock.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weekdays")
public class Weekday {
    @PrimaryKey(autoGenerate = false)
    public int id;
    public Boolean isAlarmSet;
    public String weekName;
    public String startTime;
    public String endTime;

    public Weekday(){
    }
    public Weekday(int key,String weekName,String startTime, String endTime,  Boolean isAlarmSet){
        this.id = key;
        this.isAlarmSet = isAlarmSet;
        this.weekName = weekName;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
