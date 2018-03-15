package com.efrobot.programme.service;

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

import com.efrobot.library.OnRobotStateChangeListener;
import com.efrobot.library.RobotManager;
import com.efrobot.library.RobotState;
import com.efrobot.library.task.GroupManager;
import com.efrobot.programme.bean.RouteAction;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.utils.FileUtils;

import java.util.List;

/**
 * Created by zd on 2018/3/15.
 */

public class ActionService extends Service implements OnRobotStateChangeListener {

    private final String TAG = ActionService.class.getSimpleName();

    private DBManager dbManager;

    //机器人运动管理器
    private GroupManager groupManager;

    private List<RouteAction> routeActions;

    private int currentActionIndex = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbManager = new DBManager(this);
        routeActions = dbManager.queryRouteActionItems();

        if (routeActions == null || routeActions.size() == 0) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT);
            stopSelf();
        }

        groupManager = RobotManager.getInstance(this).getGroupInstance();
        RobotManager.getInstance(this).registerHeadKeyStateChangeListener(this);

        registerMaskStateReceiver();

        handler.sendEmptyMessageDelayed(START_EXECUTE_MESSAGE, 3000);

        return super.onStartCommand(intent, flags, startId);
    }

    private final int START_EXECUTE_MESSAGE = 1;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_EXECUTE_MESSAGE:
                    executeAction();
                    break;
            }

        }
    };

    private void executeAction() {
        if (currentActionIndex == -1 || currentActionIndex > routeActions.size() - 1) {
            stopSelf();
            return;
        }

        RouteAction routeAction = routeActions.get(currentActionIndex);

        if (!TextUtils.isEmpty(routeAction.getActionId())) {
            int action = Integer.parseInt(routeAction.getActionId());

            Log.i(TAG, "执行动作 action = " + action);
            if (action != -1) {
                //执行脚本
                String sport = FileUtils.getActionFile(this, "action/action_" + action);
                groupManager.execute(sport, groupTaskListener);
                Log.i(TAG, "actionJson = " + sport);
            }
        }
        currentActionIndex++;
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
            handler.sendEmptyMessage(START_EXECUTE_MESSAGE);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerMaskStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ROBOT_MASK_CHANGE);
        registerReceiver(maskBroadCast, filter);
    }

    String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS";
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE";
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";

    private BroadcastReceiver maskBroadCast = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ROBOT_MASK_CHANGE)) {
                boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean maskClose = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);
                boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);

                if (maskOpen) {
                    handler.removeCallbacksAndMessages(null);
                    currentActionIndex = -1;
                    groupManager.stop();
                    stopSelf();
                }

            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(maskBroadCast);
        super.onDestroy();
    }

    @Override
    public void onRobotSateChange(int robotStateIndex, int newState) {
        if (robotStateIndex == RobotState.ROBOT_STATE_INDEX_HEAD_KEY) {
            if (newState == 2) {
                handler.removeCallbacksAndMessages(null);
                currentActionIndex = -1;
                groupManager.stop();
                stopSelf();
            }
        }
    }

}
