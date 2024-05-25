package com.example.chatapp.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
        {
            "Content-Type:application/json",
            "Authorization:key=AAAAI8fVpnM:APA91bHRIlzn8lhEfEPKIJ5frkXv-MG3fcT6I2hbqmGpcxH5LvajnH2vMPgS3msE4xJYzrC_xh55sCaJYK0kv5qlfqrVFnoKE_5ALH7FT11ZdDeYSmN_0rmGt9fijTbvcgtnfhalXr58" // Your server key refer to video for finding your server key
        }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body Sender body);
}
