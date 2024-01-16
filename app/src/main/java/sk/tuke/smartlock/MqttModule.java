package sk.tuke.smartlock;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Log;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

public class MqttModule{
    //efce362f5796485196e48e43b08e5e80.s2.eu.hivemq.cloud
    //DoorLockAppClient
    //Tuke12345
    private final static  String host = "devel.ceelabs.com"; //devel.ceelabs.com
    private final static String username = "zaverecneprace";        //zaverecneprace
    private final static String passwordMQTT = "igieK4Thu6dia6x";            //igieK4Thu6dia6x

    private final static String topic = "hivemq/test/doorlockapp/test";    // "HomeAutomation/A1-B2-C3-D4-E5-F6" + getter from shared perferences
    private final static String topicState = "/State"; //A1-B2-C3-D4-E5-F6
    private final static String topicEvent = "/Event";
    private final static String topicNotify = "/Notify";

    private final static String lwtMessage = "{\"status\":\"OFFLINE\", \"reason\":\"Connection closed unexpectedly\"}";
    private final static String connectMessage = "{\"status\":\"ONLINE\", \"version\":0.1}";

    private final NotificationsModule notifications;
    private String macAddress;

    private Mqtt3AsyncClient clientV3;

    public MqttModule(NotificationsModule notifications,String mac){
        this.notifications = notifications;
        macAddress = "HomeAutomation/" + mac;
        Log.e("MAC",macAddress);
    }

    public void startConnection(){
        buildClientV3();
        connectClientV3();
        publishV3();
        subscribeClientV3();
    }

    public void buildClientV3(){
        clientV3 = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(host)
                .serverPort(1883)
                .automaticReconnectWithDefaultConfig()
                .buildAsync();
    }

    public void connectClientV3(){
        clientV3.connectWith()
                .willPublish()
                    .topic(macAddress + topicState)
                    .qos(MqttQos.EXACTLY_ONCE)     //At most once (0)  At least once (1)  Exactly once (2). TODO: QOS WAL ?
                    .payload(lwtMessage.getBytes())
                    .retain(true)   // RETAIN PRI LWT
                .applyWillPublish()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(passwordMQTT))
                .applySimpleAuth()
                .send();
    }

    private void subscribeClientV3(){
        clientV3.subscribeWith()
                .topicFilter(macAddress + topicNotify)
                .send();
        // set a callback that is called when a message is received (using the async API style)
        clientV3.toAsync().publishes(ALL, publish -> {

            Log.e("MQTT","Received message: " +
                    publish.getTopic() + " -> " +
                    UTF_8.decode(publish.getPayload().get()));

            notifications.publishNotification(String.valueOf(UTF_8.decode(publish.getPayload().get())));
        });
    }

    public void publishV3(){
        clientV3.publishWith()
                .topic(macAddress + topicState)
                .payload(connectMessage.getBytes())
                .send();
    }

    public void disconnectClientV3(){
        try {
            clientV3.disconnect();
        }catch (Exception e){
            Log.e("MQTT","CLIENT ALREADY DISCONNECTED");
        }
    }
}
