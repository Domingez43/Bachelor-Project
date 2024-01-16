package sk.tuke.smartlock.transmitting;

import android.util.Log;

import org.altbeacon.beacon.Beacon;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class BeaconBuilder {

    private final String password;
    private float txPower;
    private boolean automatic;

    public BeaconBuilder(String password, float txPower, boolean automatic){
        this.password = password;
        this.txPower = txPower;
        this.automatic = automatic;
    }

    public Beacon buildBeacon(){
        return new Beacon.Builder().setId1(sha256())
                .setId2(getMajor())
                .setId3(getMinor())
                .setManufacturer(0x004c)
                .setTxPower((int)txPower)
                .build();
    }

    // ($HESLO|$BT_MAC|$UNIX_TIMESTAMP|$HESLO)
    //https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha-256-in-java
    //HESLO MAJOR MINOR TIMESTAMP
    private String sha256() {
        final StringBuilder sb = new StringBuilder();
        sb.append(password);            Log.e("password",password);
        sb.append(getMajor());          Log.e("major",getMajor());
        sb.append(getMinor());          Log.e("minor",getUUIDMinor());
        sb.append(getTimestamp());      Log.e("timestamp",getTimestamp());
        // TODO: add MAC ??
        Log.e("final string",sb.toString());
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            Log.e("HASH",hexString.substring(0,34));
            return hexString.substring(0,34);
        } catch(Exception ex){
            Log.e("HASH", "CANT GET INSTANCE OF SHA-256");
        }
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEF";
    }

    private String getTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    private String getMajor(){
        if(automatic) return "1";
        else return "0";
    }

    public String getMinor(){
        String str = getTimestamp().substring(6,10);
        String str2 = str.replaceAll("^0*", "");;
        //Log.e("Minor",str);
        return str2;
    }
    public String getUUIDMinor(){
        String str = getTimestamp().substring(6,10);
        return str;
    }
}
