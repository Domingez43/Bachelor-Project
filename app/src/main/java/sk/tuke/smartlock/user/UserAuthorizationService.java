package sk.tuke.smartlock.user;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserAuthorizationService {


    @FormUrlEncoded
    @POST("api2-r/iBeacon/data") //
    Call<UserAuthorizationAnswer>authorizationRequest(
            @Field("mac") String mac,
            @Field("description") String description
    );
}
