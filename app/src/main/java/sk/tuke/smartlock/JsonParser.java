package sk.tuke.smartlock;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonParser {


    public JsonParser(){
    }

    public String parseRequestID(String message){
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        int requestId = jsonObject.get("REQUEST_ID").getAsInt();
        return  String.valueOf(requestId);
    }

    public String parseSensorID(String message){
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        int sensorId = jsonObject.get("SENSOR_ID").getAsInt();
        Log.e("SENSORID",String.valueOf(sensorId));
        return  String.valueOf(sensorId);
    }

    public String parseTimestamp(String message){
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        long timestampStr = (long) jsonObject.get("TIMESTAMP").getAsInt();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String time = dateFormat.format(new Date(timestampStr*1000));

        Log.e("time",time);
        return time;
    }
}
