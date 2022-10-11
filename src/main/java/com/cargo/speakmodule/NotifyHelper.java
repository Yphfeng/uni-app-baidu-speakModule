package com.cargo.speakmodule;

public class NotifyHelper {
    private static NotifyHelper instance;

    public static final int N_MESSAGE = 0;
    public static final int N_CALL = 1;
    public static final int N_QQ = 2;
    public static final int N_WX = 3;

    private SpeakModule.NotifyListener notifyListener;

    public static NotifyHelper getInstance() {
        if (instance == null) {
            instance = new NotifyHelper();
        }
        return instance;
    }

    /**
     * 收到消息
     * @param msg 消息类型
     */
    public void onReceive(String msg) {
        if (notifyListener != null) {
            notifyListener.onReceiveMessage(msg);
        }

    }

    /**
     * 移除消息
     * @param msg 消息类型
     */
    public void onRemoved(String msg) {
        if (notifyListener != null) {
            notifyListener.onRemovedMessage(msg);
        }
    }

    /**
     * 设置回调方法
     *
     * @param notifyListener 通知监听
     */
    public void setNotifyListener(SpeakModule.NotifyListener notifyListener) {
        this.notifyListener = notifyListener;
    }
}
