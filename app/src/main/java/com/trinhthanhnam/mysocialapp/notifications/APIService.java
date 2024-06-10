package com.trinhthanhnam.mysocialapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAASDRORoE:APA91bFVRjMLv3UWCBuEZIH3qbOkKRD74oTU-0aF6xibnb6ev9yvhp-vgOsaZGyWxfWB4rtoaZsGEVUeUymPDtUn_55OrX5cdO64eXwIZEYwBmD0TO5CY2U_rmuZvSK0mimn52HbUf2P"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
    //Firebase Cloud Messaging
}
