package com.snw.push.impl;

import com.snw.push.exception.MessagePushException;
import com.snw.push.general.MessageOutput;
import com.snw.push.general.RegistrationTokenClient;
import com.snw.push.service.PushMessaging;
import com.snw.push.service.PushService;
import com.snw.common.iface.log.Log;
import com.snw.common.util.ConfigurationUtil;
import com.snw.common.util.LogFactoryUtil;
import org.apache.http.HttpResponse;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by soncd on 10/09/2018
 */
public class PushNotification implements PushMessaging {
    private String[] userIds;
    private PushService pushService = new PushService();

    private static ExecutorService executorService = new ThreadPoolExecutor(100, 100,
            0L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100, true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public PushNotification() {

    }

    public PushNotification(String[] userIds) {
        this.userIds = userIds;
    }

    public void setUserIds(String[] userIds) {
        this.userIds = userIds;
    }

    @Override
    public void send(String message) throws Exception {
        try {

            RegistrationTokenClient registrationTokenClients = new RegistrationTokenClient();
            List<String> registrationIds = registrationTokenClients.getRegistrationIds(userIds);

            if (registrationIds != null && registrationIds.size() > 0) {
                registrationIds = registrationIds.stream().distinct()
                        .filter(Objects::nonNull).filter(s -> !s.isEmpty()).collect(Collectors.toList());

                if (registrationIds.size() > 1000) {
                    int loop = registrationIds.size() / 1000;
                    for (int i = 0; i < loop; i++) {
                        List<String> subRegistrationIds = registrationIds.stream().limit(1000).collect(Collectors.toList());
                        executorService.execute(new FireBasePushExecutor(subRegistrationIds, message));
                        registrationIds.removeAll(subRegistrationIds);
                    }
                }

                executorService.execute(new FireBasePushExecutor(registrationIds, message));

            }
        } catch (Exception e) {
            executorService.shutdown();
        }
    }

    /*-----------------------------------------------PUSH MESSAGING-------------------------------------------------------*/

    /**
     * Thread gửi push message
     */
    private class FireBasePushExecutor implements Runnable {
        private List<String> registrationIds;
        private String message;

        FireBasePushExecutor() {

        }

        FireBasePushExecutor(List<String> registrationIds, String message) {
            this.registrationIds = registrationIds;
            this.message = message;
        }

        public void setRegistrationIds(List<String> registrationIds) {
            this.registrationIds = registrationIds;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                if (registrationIds != null && registrationIds.size() > 0) {
                    MessageOutput notificationOutput = new MessageOutput();
                    try {

                        Map<String, String> mapToken = convertRegistrationIdToMap(registrationIds);
                        List<String> tokenIds = new ArrayList<>(mapToken.values());
                        tokenIds = tokenIds.stream().distinct().collect(Collectors.toList());

                        String messageTpl = notificationOutput.generalNotificationPush(tokenIds, message);
                        HttpResponse httpResponse = pushService.send(messageTpl);
                        RegistrationTokenClient.processRegistrationIdError(mapToken, tokenIds, httpResponse);

                    } catch (GeneralSecurityException e) {
                        log.error("GeneralSecurityException= " + e.getMessage());
                    } catch (ExecutionException e) {
                        log.error("ExecutionException= " + e.getMessage());
                    } catch (InterruptedException e) {
                        log.error("InterruptedException= " + e.getMessage());
                    } catch (MessagePushException e) {
                        log.error("MessagePushException=" + e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("FireBasePushExecutor run error = " + e);
            }
        }

        /**
         * Chuyển chuỗi gồm clientID và registrationID firebase về dạng Map
         *
         * @param registrationIds
         * @return
         * @throws Exception
         */
        public Map<String, String> convertRegistrationIdToMap(List<String> registrationIds) throws Exception {
            Map<String, String> mapData = new HashMap<>();
            registrationIds.forEach(registrationId -> {
                try {
                    String[] tokenArr = registrationId.split("@");
                    mapData.put(tokenArr[0], tokenArr[1]);
                } catch (Exception e) {
                    log.error("convert registrationId to map error");
                }
            });
            return mapData;
        }

    }

    private static final Log log = LogFactoryUtil.getLog(PushNotification.class);

}
