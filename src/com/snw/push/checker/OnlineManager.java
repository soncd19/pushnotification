package com.snw.push.checker;

import com.snw.client.SnwMemcachedClient;
import com.snw.common.iface.Function;
import com.snw.common.iface.log.Log;
import com.snw.common.util.LogFactoryUtil;
import net.spy.memcached.MemcachedClient;

import java.util.*;

/**
 * Created by soncd on 11/09/2018
 */
public class OnlineManager {

    /**
     * Check 1 clientId có online hay không, nếu get data trong cached mà ko có thì là offline
     *
     * @param clientId
     * @return
     * @throws Exception
     */
    public static boolean checkOnline(long clientId) throws Exception {
        return SnwMemcachedClient.executeWithClient(new Function<Boolean, MemcachedClient>() {

            @Override
            public Boolean apply(MemcachedClient memcachedClient) throws Exception {
                String data = (String) memcachedClient.get(String.valueOf(clientId));
                return !data.isEmpty();
            }
        });
    }
    /**
     * Lấy danh sách các clientId offline
     *
     * @param clientIds
     * @return
     * @throws Exception
     */
    public static String[] getClientIdsOffline(Set<String> clientIds) throws Exception {
        List<String> clientIdOffline = new ArrayList<>();

        Map<String, Object> clientIdOnlineMap = SnwMemcachedClient.executeWithClient(
                new Function<Map<String, Object>, MemcachedClient>() {
                    @Override
                    public Map<String, Object> apply(MemcachedClient memcachedClient) throws Exception {
                        return memcachedClient.getBulk(clientIds);
                    }
                });

        if (clientIdOnlineMap == null || clientIdOnlineMap.size() == 0) {
            clientIds.toArray(new String[clientIds.size()]);
        }

        Iterator iterator = clientIds.iterator();
        while (iterator.hasNext()) {
            String clientId = (String) iterator.next();
            if (clientIdOnlineMap.get(clientId) == null) {
                clientIdOffline.add(clientId);
            }

        }
        return clientIdOffline.toArray(new String[clientIdOffline.size()]);
    }

    private static Log _log = LogFactoryUtil.getLog(OnlineManager.class);
}
