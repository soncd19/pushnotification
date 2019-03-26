package com.snw.push.general;

import com.snw.common.util.ConfigurationUtil;

import java.io.File;

/**
 * Created by soncd on 06/09/2018
 */
public class TemplateNotifyConfig {
    private static String USER_DIR = System.getProperty("user.dir");

    //"snw.notify.push.template" = resources/push_template

    public static String getTemplatePath(String tempName) {
        return USER_DIR + File.separator + ConfigurationUtil.getString("snw.push.template")
                + File.separator + tempName;
    }

    public static String getTempMsgSingle() {
        return USER_DIR + File.separator + ConfigurationUtil.getString("snw.push.template")
                + File.separator + "message_single.tpl";
    }

    public static String getTempMsgMultiple() {
        return USER_DIR + File.separator + ConfigurationUtil.getString("snw.push.template")
                + File.separator + "message_multiple.tpl";
    }

    public static String getTempNotifySingle() {
        return USER_DIR + File.separator + ConfigurationUtil.getString("snw.push.template")
                + File.separator + "notification_single.tpl";
    }

    public static String getTempNotifyMultiple() {
        return USER_DIR + File.separator + ConfigurationUtil.getString("snw.push.template")
                + File.separator + "notification_multiple.tpl";
    }
}
