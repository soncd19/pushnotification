package com.snw.push.general;

import com.snw.common.iface.json.JSONArray;
import com.snw.common.iface.json.JSONObject;
import com.snw.common.util.ConfigurationUtil;
import com.snw.common.util.JSONFactoryUtil;
import com.snw.client.SnwRedisClient;
import com.snw.common.iface.Function;
import com.snw.common.iface.log.Log;
import com.snw.common.util.LogFactoryUtil;
import com.snw.push.exception.MessagePushException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by soncd on 06/09/2018
 */
public class RegistrationTokenClient {

    private static String redisKeyName = ConfigurationUtil.getString("snw.push.client.key");

    public RegistrationTokenClient() {

    }

    /**
     * Tổng hợp list registrationIds theo nhiều userId
     *
     * @param userIds
     * @return
     * @throws Exception
     */
    public List<String> getRegistrationIds(String[] userIds) throws MessagePushException {
        Set<String> clientIds = new HashSet<>();

        try {
            SnwRedisClient.executeWithClient(new Function<Void, Jedis>() {
                @Override
                public Void apply(Jedis jedis) throws Exception {
                    for (int i = 0; i < userIds.length; i++) {
                        String keyName = String.format(redisKeyName, userIds[i]);
                        Set<String> ids = jedis.zrange(keyName, 0L, Long.MAX_VALUE);
                        if (ids != null && ids.size() > 0) {
                            clientIds.addAll(ids);
                        }
                    }
                    return null;
                }
            });

            if (clientIds.size() == 0) {
                return new ArrayList<>();
            }

            return getListRegistrationIds(clientIds);

        } catch (Exception e) {
            _log.error("general registrationIds error " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * get from 1 userId
     *
     * @param userId
     * @return
     * @throws MessagePushException
     */
    public List<String> getRegistrationIds(String userId) throws MessagePushException {
        Set<String> clientIds = new HashSet<>();

        try {
            SnwRedisClient.executeWithClient(new Function<Void, Jedis>() {
                @Override
                public Void apply(Jedis jedis) throws Exception {
                    String keyName = String.format(redisKeyName, userId);
                    Set<String> ids = jedis.zrange(keyName, 0L, Long.MAX_VALUE);
                    if (ids != null && ids.size() > 0) {
                        clientIds.addAll(ids);
                    }
                    return null;
                }
            });

            if (clientIds.size() == 0) {
                return new ArrayList<>();
            }

            return getListRegistrationIds(clientIds);

        } catch (Exception e) {
            _log.error("general registrationIds error " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * get list registrationIds offline
     *
     * @param clientIds
     * @return
     * @throws MessagePushException
     */
    public List<String> getListRegistrationIds(Set<String> clientIds) throws MessagePushException {
        try {
            List<String> registrationIds = new ArrayList<>();
            String[] clientOfflines = clientIds.toArray(new String[clientIds.size()]);
            registrationIds = SnwRedisClient.executeWithClient(new Function<List<String>, Jedis>() {
                @Override
                public List<String> apply(Jedis jedis) throws Exception {
                    return jedis.mget(clientOfflines);
                }
            });

            return (registrationIds == null) ? new ArrayList<>() : registrationIds;
        } catch (Exception e) {
            _log.error("general registrationIds error " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Xử lý kết quả trả về sau khi push ----> check những token failure thì xóa trong cache
     *
     * @throws Exception
     */
    public static void processRegistrationIdError(Map<String, String> dataToken, List<String> registrationIds, HttpResponse httpResponse) throws Exception {
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonResponse = JSONFactoryUtil.createJSONObject(responseString);
        int failure = jsonResponse.getInt("failure");
        List<String> clientIdsError = new ArrayList<>();
        if (failure > 0) {
            JSONArray jsonResults = jsonResponse.getJSONArray("results");
            for (int i = 0; i < jsonResults.length(); i++) {
                try {
                    JSONObject jsonObject = jsonResults.getJSONObject(i);
                    if (jsonObject.getKeys()[0].equals("error")) {
                        try {
                            String idError = registrationIds.get(i);

                            List<String> clientIds = dataToken.entrySet()
                                    .stream()
                                    .filter(entry -> Objects.equals(entry.getValue(), idError))
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

                            clientIdsError.addAll(clientIds);
                        } catch (Exception e) {
                            _log.error(e);
                        }
                    }
                } catch (Exception e) {
                    _log.error(e);
                }
            }
            removeTokenError(clientIdsError);
        }

    }

    /**
     * xóa cached lưu token, clientId không còn đăng ký
     *
     * @param clientIdsErr
     * @throws Exception
     */
    public static void removeTokenError(List<String> clientIdsErr) throws Exception {

        String[] keyClientId = clientIdsErr.toArray(new String[clientIdsErr.size()]);
        Map<String, List<String>> mapClientIds = new HashMap<>();
        for (int i = 0; i < clientIdsErr.size(); i++) {
            try {

                String clientId = clientIdsErr.get(i);

                String[] clientIdArr = clientId.split("_");
                String userId = clientIdArr[0];
                if (mapClientIds.containsKey(userId)) {
                    mapClientIds.get(userId).add(clientId);
                } else {
                    List<String> clientIds = new ArrayList<>();
                    clientIds.add(clientId);
                    mapClientIds.put(userId, clientIds);
                }
            } catch (Exception e) {
                _log.error(String.format("get data from registrationId= %s error ", clientIdsErr.get(i)) + e);
            }
        }

        SnwRedisClient.executeWithClient(new Function<Object, Jedis>() {
            @Override
            public Object apply(Jedis jedis) throws Exception {
                jedis.del(keyClientId);

                for (String userId : mapClientIds.keySet()) {
                    try {
                        String redisName = String.format(redisKeyName, userId);
                        List<String> dataCli = mapClientIds.get(userId);
                        String[] clientIds = dataCli.toArray(new String[dataCli.size()]);
                        jedis.zrem(redisName, clientIds);
                    } catch (Exception e) {
                        _log.error(String.format("delete in rediscache userId=%s error", userId));
                    }
                }

                return null;
            }
        });
    }

    private static Log _log = LogFactoryUtil.getLog(RegistrationTokenClient.class);

}
