package sk.tuke.smartlock.user;

public class UserAuthorizationAnswer{

    private String status;

    private UserAuthorizationAnswerData data;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setData(UserAuthorizationAnswerData data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public UserAuthorizationAnswerData getData() {
        return data;
    }
}
