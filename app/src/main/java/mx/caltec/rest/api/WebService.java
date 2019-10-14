package mx.caltec.rest.api;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface WebService {

    @FormUrlEncoded
    @POST("/api/login")
    Observable<String> login(@Field("usr") String usr, @Field("pwd") String pwd);

}
