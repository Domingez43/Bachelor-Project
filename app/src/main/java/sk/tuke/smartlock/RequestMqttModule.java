package sk.tuke.smartlock;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.util.Log;

import sk.tuke.smartlock.sharedPreferences.SharedPreferencesKeys;
import sk.tuke.smartlock.sharedPreferences.SharedPreferencesManager;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;

public class RequestMqttModule {

    private final static  String host = "devel.ceelabs.com"; //devel.ceelabs.com
    private final static String username = "zaverecneprace";        //zaverecneprace
    private final static String passwordMQTT = "igieK4Thu6dia6x";            //igieK4Thu6dia6x

    private final static String topicEvent = "/Event";

    private Mqtt3BlockingClient clientV3;

    private final Context context;
    private final String macAddress;
    private final int notificationID;

    public RequestMqttModule(Context context, int notificationID){
        this.context = context;
        this.notificationID = notificationID;
        macAddress = "HomeAutomation/" + getMac();
        Log.e("MAC",macAddress);
    }

    public void startConnection(){
        buildClientV3();
        connectClientV3();
    }

    public void buildClientV3(){
        clientV3 = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(host)
                .serverPort(1883)
                .automaticReconnectWithDefaultConfig()
                .buildBlocking();
    }


    public void connectClientV3(){
        clientV3.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .send();
    }


    public void publishV3(Boolean isAccepted){
        String payload = "";
        String answer = isAccepted ? "ACCEPT" : "DECLINE";
        payload = "{\n" +
                "  \"REQUEST_ID\": "+ notificationID+",\n" +
                "  \"ANSWER\": \""+answer+"\"\n" +
                "}";

        clientV3.publishWith()
                .topic(macAddress + topicEvent)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(payload.getBytes())
                .send();
    }


    private String getMac(){
        SharedPreferencesManager preferencesManager = new SharedPreferencesManager(context);
        return preferencesManager.getStringVal(SharedPreferencesKeys.USER_MAC.getKey(),SharedPreferencesKeys.USER_MAC.getDefaultVal());
    }


    public void disconnectClientV3(){
        try {
            clientV3.disconnect();
        }catch (Exception e){
            Log.e("MQTT","CLIENT ALREADY DISCONNECTED");
        }
    }
}

