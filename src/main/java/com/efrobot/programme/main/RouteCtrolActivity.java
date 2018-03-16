package com.efrobot.programme.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.base.BaseActivity;
import com.efrobot.programme.bean.RouteAction;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.service.ActionService;
import com.efrobot.programme.view.CustomHintDialog;
import com.efrobot.programme.view.RouteView;

import java.util.List;

/**
 * Created by zd on 2018/3/14.
 */

public class RouteCtrolActivity extends BaseActivity implements View.OnClickListener {

    private TextView goFrontTv, goBackTv, turnLeftTv, turnRightTv;

    private TextView revokeTv, resetTv;

    private RouteView routeView;

    private ScrollView scrollView;

    private TextView routeTextView;
    private CustomHintDialog customHintDialog;

    private DBManager dbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_ctrol);

        dbManager = new DBManager(this);

        goFrontTv = findViewById(R.id.go_front);
        goBackTv = findViewById(R.id.go_back);
        turnLeftTv = findViewById(R.id.turn_left);
        turnRightTv = findViewById(R.id.turn_right);

        revokeTv = findViewById(R.id.revoke_view);
        resetTv = findViewById(R.id.reset_view);

        routeView = findViewById(R.id.route_view);

        scrollView = findViewById(R.id.scrollView);
        routeTextView = findViewById(R.id.route_history_tv);

        setListener();
        registerMaskStateReceiver();
        handler.sendEmptyMessage(UPDATE_ROUTE_HISTORY);
    }

    private void setListener() {
        goFrontTv.setOnClickListener(this);
        goBackTv.setOnClickListener(this);
        turnLeftTv.setOnClickListener(this);
        turnRightTv.setOnClickListener(this);

        revokeTv.setOnClickListener(this);
        resetTv.setOnClickListener(this);

        findViewById(R.id.start_tv).setOnClickListener(this);

        routeView.setOnAddActionListener(new RouteView.OnAddActionListener() {
            @Override
            public void onAdd() {
                handler.sendEmptyMessage(UPDATE_ROUTE_HISTORY);
            }
        });
    }

    private final int UPDATE_ROUTE_HISTORY = 1;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_ROUTE_HISTORY:
                    List<RouteAction> routeActionList = new DBManager(RouteCtrolActivity.this).queryRouteActionItems();
                    String action = "";
                    if (routeActionList != null) {
                        for (int i = 0; i < routeActionList.size(); i++) {
                            action = action + "\n" + routeActionList.get(i).getActionName() + ";";
                        }
                        routeTextView.setText(action);
                        routeTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_front:
                routeView.moveRobot(RouteView.GO_FRONT);
                break;
            case R.id.go_back:
                routeView.moveRobot(RouteView.GO_BACK);
                break;
            case R.id.turn_left:
                routeView.moveRobot(RouteView.TURN_LEFT);
                break;
            case R.id.turn_right:
                routeView.moveRobot(RouteView.TURN_RIGHT);
                break;
            case R.id.revoke_view:
                routeView.revokeView();
                break;
            case R.id.reset_view:
                routeView.restoreView();
                break;
            case R.id.start_tv:
                List<RouteAction> routeActions = dbManager.queryRouteActionItems();
                if (routeActions == null || routeActions.size() == 0) {
                    toast("动作为空，请先选择路径");
                    return;
                }
                isNeedStartService = true;
                if (customHintDialog == null) {
                    customHintDialog = new CustomHintDialog(this, -1);
                    customHintDialog.setMessage("关闭面罩机器人开始移动\n摸头即可停止");
                    customHintDialog.setCancelable(true);
                    customHintDialog.setOnDismissListener(new CustomHintDialog.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            isNeedStartService = false;
                            Log.e("RouteCtrolActivity", "isNeedStartService = " + isNeedStartService);
                        }
                    });
                }
                customHintDialog.show();
                break;
        }
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

    private boolean isNeedStartService = false;
    private BroadcastReceiver maskBroadCast = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ROBOT_MASK_CHANGE)) {
                boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean maskClose = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);
                boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);

                if (maskClose) {
                    if (isNeedStartService) {
                        Intent intentService = new Intent(context, ActionService.class);
                        context.startService(intentService);
                        if (customHintDialog != null && customHintDialog.isShowing()) {
                            customHintDialog.dismiss();
                        }
                    }
                }

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(maskBroadCast);
        super.onDestroy();
    }
}
