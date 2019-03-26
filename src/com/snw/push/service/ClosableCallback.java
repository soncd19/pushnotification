package com.snw.push.service;

import com.snw.common.iface.log.Log;
import com.snw.common.util.LogFactoryUtil;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

/**
 * Created by soncd on 06/09/2018
 */
public class ClosableCallback implements FutureCallback<HttpResponse> {

    private CloseableHttpAsyncClient closeableHttpAsyncClient;

    public ClosableCallback(CloseableHttpAsyncClient closeableHttpAsyncClient) {
        this.closeableHttpAsyncClient = closeableHttpAsyncClient;
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        close();
    }

    @Override
    public void failed(Exception e) {
        close();
    }

    @Override
    public void cancelled() {
        close();
    }

    private void close() {
        try {
            closeableHttpAsyncClient.close();
        } catch (Exception e) {
            _log.error(e);
        }
    }

    private Log _log = LogFactoryUtil.getLog(ClosableCallback.class);
}
