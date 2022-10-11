package com.cargo.speakmodule;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

import io.dcloud.feature.uniapp.UniAppHookProxy;

public class SpeakModule_AppProxy implements UniAppHookProxy {


    @Override
    public void onSubProcessCreate(Application application) {

    }

    @Override
    public void onCreate(Application application) {

    }


    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.e("error code :" + result + " method:" + method, "checkResult: ");
        }
    }




}
