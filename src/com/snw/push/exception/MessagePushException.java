package com.snw.push.exception;

/**
 * Created by soncd on 11/09/2018
 */
public class MessagePushException extends Exception {

    public MessagePushException(String message) {
        super(message);
    }

    public MessagePushException(String message, Throwable cause) {
        super(message, cause);
    }
}
