package com.efrobot.programme.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.efrobot.library.RobotManager;
import com.efrobot.library.speech.SpeechManager;
import com.efrobot.library.task.GroupManager;
import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.provider.OnKeyEventListener;
import com.efrobot.programme.provider.ProgrammeProvider;
import com.efrobot.programme.utils.FileUtils;
import com.efrobot.programme.utils.TtsUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.efrobot.programme.env.Constans.ExecuteContent.ACTION_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.FACE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.MOUSE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.SPEECH_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TIME_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TTS_TYPE;

/**
 * 编程游戏执行类
 * Created by zd on 2018/3/2.
 */

public class ProgrammeGameService extends Service implements OnKeyEventListener {

    private final String TAG = ProgrammeGameService.class.getSimpleName();

    private DBManager dbManager;

    private SpeechManager speechManager;

    //机器人运动管理器
    private GroupManager groupManager;

    private List<ExecuteModule> executeModuleList;

    public static boolean isNeedReceiveTtsEnd = false;

    private ExecuteModule executeModule;

    private int currentItemIndex = 0;

    private long tts_word = 270;

    private int executeMouseKey = -1;

    AlarmManager alarmManager = null;

    public static String SPEECH_CODE = "1057";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "进入教育编程service");

        dbManager = new DBManager(this);
        executeModuleList = dbManager.queryExecuteItems();

        if (executeModuleList == null || executeModuleList.size() == 0) {
            return super.onStartCommand(intent, flags, startId);
        }


        init();
        registerTtsEndBroadCast();
        exec();

        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        groupManager = RobotManager.getInstance(this).getGroupInstance();

        speechManager = SpeechManager.getInstance(this);
        speechManager.registerKeyListener(new SpeechManager.ISpeechRegisterResultListener() {
            @Override
            public void onRegisterSuccess() {
                Log.e(TAG, "registerKeyListener onRegisterSuccess");
            }

            @Override
            public void onRegisterFail() {
                Log.e(TAG, "registerKeyListener onRegisterFail");
            }
        });

        speechManager.registerSpeechListener(new SpeechManager.ISpeechRegisterResultListener() {
            @Override
            public void onRegisterSuccess() {
                Log.e(TAG, "registerSpeechListener onRegisterSuccess");
            }

            @Override
            public void onRegisterFail() {
                Log.e(TAG, "registerSpeechListener onRegisterSuccess");
            }
        });
    }

    /**
     * 开始执行
     */
    private void exec() {
        Log.e(TAG, "开始执行  currentItemIndex = " + currentItemIndex);

        if (currentItemIndex > executeModuleList.size() - 1) {
            Toast.makeText(this, "执行完毕", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        executeModule = executeModuleList.get(currentItemIndex);
        if (executeModule == null)
            return;

        int type = executeModule.getType();
        switch (type) {
            case TIME_TYPE:
                timeListenning(executeModule.getTime());
                break;
            case MOUSE_TYPE:
                //注册空鼠按键监听
                ProgrammeProvider.setOnKeyEventListener(this);
                break;
            case SPEECH_TYPE:
                registerSpeechTextListener(executeModule.getRecognitionText());
                break;
            case TTS_TYPE:
                isNeedReceiveTtsEnd = true;
                TtsUtils.sendTts(this, executeModule.getTts());
                break;
            case ACTION_TYPE:
                /** 播放动作 **/
                executeAction();
                break;
            case FACE_TYPE:
                TtsUtils.sendTts(getApplicationContext(), "@#;" + executeModule.getFace());
                break;
        }


        currentItemIndex++;
    }

    //定时触发任务
    private void timeListenning(String time) {
        int year = 0;
        int month = 0;
        int date = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        Calendar c = Calendar.getInstance();
        c.set(year, month, date, hour, minute, second);

        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);

    }

    //
    class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "接收到定时任务广播");
            exec();
        }
    }

    /**
     * 语音触发
     *
     * @param text
     */
    private void registerSpeechTextListener(String text) {
        try {
            speechManager.setData(this, createSpeechData(SPEECH_CODE, text, this.getPackageName(), "", "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createSpeechData(String code, String ins, String packageName, String reply, String type) {
        String data = "";

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("code", code);
        dataMap.put("ins", ins);
        dataMap.put("packageName", packageName);
        dataMap.put("reply", reply);
        dataMap.put("type", type);

        data = TtsUtils.simpleMapToJsonStr(dataMap);

        return data;
    }

    private void executeAction() {
        if (!TextUtils.isEmpty(executeModule.getAction())) {
            int action = Integer.parseInt(executeModule.getAction());

            Log.i(TAG, "执行动作 action = " + action);
            if (action != -1) {
                //执行脚本
                String sport = FileUtils.getActionFile(this, "action/action_" + action);
                groupManager.execute(sport, groupTaskListener);
                Log.i(TAG, "actionJson = " + sport);
            }
        }
    }

    /**
     * 执行脚本监听
     */
    GroupManager.OnGroupTaskExecuteListener groupTaskListener = new GroupManager.OnGroupTaskExecuteListener() {

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {
            exec();
        }
    };

    private final int TIME_FINISH = 1;
    private final int MOUSE_FINISH = 2;
    private final int SPEECH_FINISH = 3;
    private final int TTS_FINISH = 4;
    private final int ACTION_FINISH = 5;
    private final int FACE_FINISH = 6;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_FINISH:

                    break;
                case MOUSE_FINISH:

                    break;
                case SPEECH_FINISH:

                    break;
                case TTS_FINISH:
                    exec();
                    break;
                case ACTION_FINISH:
                    exec();
                    break;
                case FACE_FINISH:
                    exec();
                    break;
            }
        }
    };

    private void registerTtsEndBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TTS_END_BROADCAST_ACTION);
        registerReceiver(TtsEndBroadCastReceiver, intentFilter);
    }

    public static final String TTS_END_BROADCAST_ACTION = "com.efrobot.programme.TTS.END";

    private BroadcastReceiver TtsEndBroadCastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TTS_END_BROADCAST_ACTION)) {
                exec();
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onKeyDown(int keyCode) {
        //空鼠监听触发
        if (keyCode == executeMouseKey) {
            exec();
            //监听器需要从新置空
            ProgrammeProvider.setOnKeyEventListener(null);
        }
    }

    @Override
    public void onKeyUp(int keyCode) {

    }

    @Override
    public void onKeyLongPress(int keyCode) {

    }
}
