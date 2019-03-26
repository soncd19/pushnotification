package com.snw.push.service;

import com.snw.common.iface.log.Log;
import com.snw.common.util.ConfigurationUtil;
import com.snw.common.util.LogFactoryUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;

import java.security.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by soncd on 06/09/2018
 */
public class PushService {

    //Lấy ra FCM key server được sử dụng
    private String fcmApiKey = ConfigurationUtil.getString("snw.push.fcm.api.key");

    public PushService() {

    }

    public PushService(String fcmApiKey) {
        this.fcmApiKey = fcmApiKey;
    }

    public void setFcmApiKey(String fcmApiKey) {
        this.fcmApiKey = fcmApiKey;
    }

    /**
     * Send push..........
     *
     * @param message
     * @return
     * @throws GeneralSecurityException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public HttpResponse send(String message) throws GeneralSecurityException, ExecutionException, InterruptedException {
        return sendAsync(message).get();
    }

    /**
     * Gửi bất đồng bộ tin push sử dụng CloseableHttpAsyncClient
     *
     * @param message
     * @return
     * @throws GeneralSecurityException
     */
    private Future<HttpResponse> sendAsync(String message) throws GeneralSecurityException {
        try {
            HttpPost httpPost = preparePost(message);
            CloseableHttpAsyncClient closeableHttpAsyncClient = HttpAsyncClients.createSystem();
            closeableHttpAsyncClient.start();

            return closeableHttpAsyncClient.execute(httpPost, new ClosableCallback(closeableHttpAsyncClient));
        } catch (Exception e) {
            log.error("send push async exception = " + e);
            throw new GeneralSecurityException(e);
        }
    }

    /**
     * Tạo http post với header và data push gửi đi
     *
     * @param message
     * @return
     */
    private HttpPost preparePost(String message) throws Exception {

        try {

            HttpPost httpPost = new HttpPost(MessagePushDefault.FCM_ENDPOINT);
            httpPost.addHeader(new BasicHeader("Content-Type", "application/json"));
            httpPost.addHeader(new BasicHeader("Authorization", "key=" + fcmApiKey));

            httpPost.setEntity(new StringEntity(message));
            return httpPost;
        }catch (Exception e) {
            throw new Exception(e);
        }

    }

    private static Log log = LogFactoryUtil.getLog(PushService.class);
}
