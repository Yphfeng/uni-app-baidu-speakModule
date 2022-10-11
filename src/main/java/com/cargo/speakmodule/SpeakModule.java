package com.cargo.speakmodule;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.alibaba.fastjson.JSONObject;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;
import com.cargo.speakmodule.control.InitConfig;
import com.cargo.speakmodule.control.MySyntherizer;
import com.cargo.speakmodule.control.NonBlockSyntherizer;
import com.cargo.speakmodule.listener.UiMessageListener;
import com.cargo.speakmodule.util.Auth;
import com.cargo.speakmodule.util.AutoCheck;
import com.cargo.speakmodule.util.IOfflineResourceConst;
import com.cargo.speakmodule.util.OfflineResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class SpeakModule extends UniModule {
    // 主控制类，所有合成控制方法从这个类开始
    protected Handler mainHandler;
    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected MySyntherizer synthesizer;

    public Context context;

    private static final int REQUEST_CODE = 9527;



    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;

    protected boolean isOnlineSDK = TtsMode.ONLINE.equals(IOfflineResourceConst.DEFAULT_SDK_TTS_MODE);
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    //语音播报初始化方法
    @UniJSMethod(uiThread = true)
    public void init(JSONObject object, UniJSCallback callback) {
        Log.e("语音播报初始化", "onCreate: ");
        //初始化通知服务
        requestNotification();
        //初始化方法
        NotifyHelper.getInstance().setNotifyListener(new NotifyListener() {
            @Override
            public void onReceiveMessage(String msg) {
                Log.e(msg, "onReceiveMessage: ");
                speak(msg);
            }

            @Override
            public void onRemovedMessage(String msg) {

            }
        });
        try {
            mainHandler = new Handler() {
                /*
                 * @param msg
                 */
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }

            };
            context = mUniSDKInstance.getContext();
            Log.e("context成功", "context: ");
            Auth.getInstance(context);
            appId = Auth.getInstance(context).getAppId();
            appKey = Auth.getInstance(context).getAppKey();
            secretKey = Auth.getInstance(context).getSecretKey();
            Log.e(appId, "onCreate----: ");
            Log.e(appKey, "onCreate2------: ");
            Log.e(secretKey, "onCreate3-----: ");
            initialTts(); // 初始化TTS引擎
            if (!isOnlineSDK) {
                Log.i("SynthActivity", "so version:" + SynthesizerTool.getEngineInfo());
            }
        } catch (Auth.AuthCheckException e) {
            Log.e(String.valueOf(e), "onCreate报错: ");
            return;
        }
    }
    public interface NotifyListener {

        /**
         * 接收到通知栏消息
         * @param msg
         */
        void onReceiveMessage(String msg);

        /**
         * 移除掉通知栏消息
         * @param msg
         */
        void onRemovedMessage(String msg);
    }

    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(context, config, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }


    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getParams();
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。

        initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(context).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
//                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        return initConfig;
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        if (!isOnlineSDK) {
            // 在线SDK版本没有此参数。

            /*
            params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
            // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。
            */
            // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
            OfflineResource offlineResource = createOfflineResource(offlineVoice);
            // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        }
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
//            toPrint("【error】:copy files from assets failed." + e.getMessage());
            Log.w(e.getMessage(), "【error】:copy files from assets failed.");
        }
        return offlineResource;
    }

    @UniJSMethod(uiThread = true)
    public void speak(JSONObject object, UniJSCallback callback) {
        String text = object.getString("text");
        Log.e(text, "speak: ");
        speak(text);
    }
    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(String s) {
        String text = s;
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(s)) {
           return;
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // params.put(SpeechSynthesizer.PARAM_SPEAKER, "3"); // 设置为度逍遥
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }
    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.e("error code :" + result + " method:" + method, "checkResult: ");
        }
    }
    /**
     * 是否启用通知监听服务
     * @return
     */
    public boolean isNLServiceEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(mUniSDKInstance.getContext());
        if (packageNames.contains(mUniSDKInstance.getContext().getPackageName())) {
            return true;
        }
        return false;
    }

    //请求开启通知服务权限
    public void requestNotification() {
        if (!isNLServiceEnabled()) {
            ((Activity)(mUniSDKInstance.getContext())).startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_CODE);
        } else {
            toggleNotificationListenerService(true);
        }
    }

    /**
     * 切换通知监听器服务
     *
     * @param
     */
    public void toggleNotificationListenerService(Boolean enable) {
        PackageManager pm = mUniSDKInstance.getContext().getPackageManager();
        if (enable) {
            pm.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext().getApplicationContext(), NotifyService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(new ComponentName(mUniSDKInstance.getContext().getApplicationContext(), NotifyService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
