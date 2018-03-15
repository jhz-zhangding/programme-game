package com.efrobot.programme.provider;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.efrobot.library.speech.SpeechSdkProvider;
import com.efrobot.programme.service.ProgrammeGameService;

/**
 * Created by zd on 2017/11/10.
 */
public class ProgrammeProvider extends SpeechSdkProvider {

    private final String TAG = ProgrammeProvider.class.getSimpleName();

    public static OnKeyEventListener mOnKeyEventListener;

    //执行促销TTS语音被打断
    private boolean isBreakSaleTts = false;

    public static void setOnKeyEventListener(OnKeyEventListener onKeyEventListener) {
        mOnKeyEventListener = onKeyEventListener;
    }

    @Override
    public void onReceiveMessage(String s, String s1) {
        if (s.equals(ProgrammeGameService.SPEECH_CODE)) {
            Log.e(TAG, "触发已注册的Tts语音：" + s1);
        }
    }

    @Override
    public void onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        if (mOnKeyEventListener != null) {
            mOnKeyEventListener.onKeyUp(keyCode);
        }
        Log.e(TAG, "onKeyUp = " + keyCode);
    }

    @Override
    public void onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (mOnKeyEventListener != null) {
            mOnKeyEventListener.onKeyDown(keyCode);
        }
        Log.e(TAG, "onKeyDown" + keyCode);
    }

    @Override
    public void onKeyLongPress(int keyCode, KeyEvent event) {
        super.onKeyLongPress(keyCode, event);
        if (mOnKeyEventListener != null) {
            mOnKeyEventListener.onKeyLongPress(keyCode);
        }
        Log.e(TAG, "onKeyLongPress" + keyCode);
    }

    @Override
    public void TTSStart() {
        super.TTSStart();
        Log.e(TAG, "TTSStart：" + "isNeedReceiveTtsEnd  =  ");
    }

    @Override
    public void TTSEnd() {
        super.TTSEnd();
        Log.e(TAG, "TTSEnd：" + "isNeedReceiveTtsEnd  =  " + ProgrammeGameService.isNeedReceiveTtsEnd);

        if (ProgrammeGameService.isNeedReceiveTtsEnd) {
            Intent intent = new Intent(ProgrammeGameService.TTS_END_BROADCAST_ACTION);
            getContext().sendBroadcast(intent);
            ProgrammeGameService.isNeedReceiveTtsEnd = false;
        }
    }
}
