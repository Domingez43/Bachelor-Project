package sk.tuke.smartlock.scheduling;

public enum WeekDaysNames {

    MONDAY("monday",1),
    TUESDAY("tuesday",2),
    WEDNESDAY("wednesday",3),
    THURSDAY("thursday",4),
    FRIDAY("friday",5),
    SATURDAY("saturday",6),
    SUNDAY("sunday",7);

    private final int value;
    private final String str;

    WeekDaysNames(String str, int val){
        this.str = str;
        value = val;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return str;
    }
}
