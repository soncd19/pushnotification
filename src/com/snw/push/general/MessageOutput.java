package com.snw.push.general;

import com.snw.common.iface.log.Log;
import com.snw.common.util.LogFactoryUtil;
import com.snw.push.exception.MessagePushException;
import com.snw.push.service.MessagePushDefault;
import com.snw.response.util.TemplateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by soncd on 06/09/2018
 */
public class MessageOutput {
    private Map<String, Object> results;

    public MessageOutput() {

    }

    public MessageOutput(Map<String, Object> results) {
        this.results = results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public String getOutput(String tempPath) {
        try {
            return TemplateUtil.getResultByMap(tempPath, this.results);
        } catch (Exception var3) {
            return "Error parsing template";
        }
    }

    public String getOutput(String tempPath, Map<String, Object> results) {
        try {
            return TemplateUtil.getResultByMap(tempPath, results);
        } catch (Exception var3) {
            return "Error parsing template";
        }
    }

    /**
     * Create object notification
     *
     * @param registrationIds
     * @return
     * @throws MessagePushException
     */
    public String generalMessagePush(List<String> registrationIds) throws MessagePushException {
        try {
            Map<String, Object> results = new HashMap<>();
            String message;
            results.put(MessagePushDefault.TITLE, MessagePushDefault.NOTIFY_TITLE);
            results.put(MessagePushDefault.BODY, MessagePushDefault.NOTIFY_BODY);

            if (registrationIds.size() == 1) {
                results.put(MessagePushDefault.REGISTRATION_ID, registrationIds.get(0));
                message = getOutput(TemplateNotifyConfig.getTempMsgSingle(), results);
            } else {
                results.put(MessagePushDefault.REGISTRATION_IDS, registrationIds);
                message = getOutput(TemplateNotifyConfig.getTempMsgMultiple(), results);
            }

            return message;
        } catch (Exception e) {
            log.error(e);
            throw new MessagePushException("general template push message error");
        }
    }

    public String generalNotificationPush(List<String> registrationIds, String message) throws MessagePushException {
        try {
            Map<String, Object> results = new HashMap<>();
            String notification;
            results.put(MessagePushDefault.NOTIFICATION, message);
            if (registrationIds.size() == 1) {
                results.put(MessagePushDefault.REGISTRATION_ID, registrationIds.get(0));
                notification = getOutput(TemplateNotifyConfig.getTempNotifySingle(), results);
            } else {
                results.put(MessagePushDefault.REGISTRATION_IDS, registrationIds);
                notification = getOutput(TemplateNotifyConfig.getTempNotifyMultiple(), results);
            }

            return notification;

        } catch (Exception e) {
            log.error(e);
            throw new MessagePushException("general template push notification error");
        }
    }


    private static Log log = LogFactoryUtil.getLog(MessageOutput.class);
}
