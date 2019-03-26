package com.snw.push.service;

/**
 * Created by soncd on 06/09/2018
 */
public interface MessagePushDefault {
    String TITLE = "title";
    String BODY = "body";
    String TO = "to";
    String NOTIFY_TITLE = "Bạn có tin nhắn mới";
    String NOTIFY_BODY = "https://vala.bkav.com";

    String REGISTRATION_IDS = "registrationIds";
    String REGISTRATION_ID = "registrationId";
    String NOTIFICATION = "notification";

    String FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send";
}
