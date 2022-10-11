package com.cargo.speakmodule;

import android.content.ComponentName;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

public class NotifyService extends NotificationListenerService {
    public static final String TAG = "NotifyService";

    public static final String HYYJ = "uni.UNIWNQ0000";//货运中国的推送消息

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(sbn.getPackageName(), "onNotificationPosted: ");
        Log.e(String.valueOf(sbn.getNotification() == null), "getNotification: ");
        if (sbn.getNotification() == null) return;
        String msgContent = "";
        if (sbn.getNotification().tickerText != null) {
            msgContent = sbn.getNotification().tickerText.toString();
        }
        if (TextUtils.isEmpty(msgContent)) {
            return;
        }
        switch (sbn.getPackageName()){
            case HYYJ:
                NotifyHelper.getInstance().onReceive(msgContent);

                break;
            default:break;
        }
    }

    @Override
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}
