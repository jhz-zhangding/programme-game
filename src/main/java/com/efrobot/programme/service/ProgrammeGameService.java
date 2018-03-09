package com.efrobot.programme.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * 编程游戏执行类
 * Created by zd on 2018/3/2.
 */

public class ProgrammeGameService extends Service {



    private boolean TTS_FINISH =true;

    private boolean ACTION_FINISH = true;

    private boolean FACE_FINISH = true;

    private long tts_word = 270;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
