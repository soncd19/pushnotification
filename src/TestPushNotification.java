

import com.snw.push.impl.PushNotification;
import com.snw.push.service.PushMessaging;

/**
 * Created by soncd on 15/03/2019
 */
public class TestPushNotification {
    public static void main(String[] args) throws Exception {
        String userId = String.valueOf(281612415674252L);
        String[] userIds = new String[]{userId};
        PushMessaging pushMessaging = new PushNotification(userIds);
        String message = "{\n" +
                "  \"content\": \"son dep zai\"\n" +
                "}";
        pushMessaging.send(message);
    }
}
