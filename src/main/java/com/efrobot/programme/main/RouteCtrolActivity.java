package com.efrobot.programme.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.base.BaseActivity;
import com.efrobot.programme.service.ActionService;
import com.efrobot.programme.view.RouteView;

/**
 * Created by zd on 2018/3/14.
 */

public class RouteCtrolActivity extends BaseActivity implements View.OnClickListener {

    private TextView goFrontTv, goBackTv, turnLeftTv, turnRightTv;

    private TextView revokeTv, resetTv;

    private RouteView routeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_ctrol);

        goFrontTv = findViewById(R.id.go_front);
        goBackTv = findViewById(R.id.go_back);
        turnLeftTv = findViewById(R.id.turn_left);
        turnRightTv = findViewById(R.id.turn_right);

        revokeTv = findViewById(R.id.revoke_view);
        resetTv = findViewById(R.id.reset_view);

        routeView = findViewById(R.id.route_view);

        setListener();
        registerMaskStateReceiver();
    }

    private void setListener() {
        goFrontTv.setOnClickListener(this);
        goBackTv.setOnClickListener(this);
        turnLeftTv.setOnClickListener(this);
        turnRightTv.setOnClickListener(this);

        revokeTv.setOnClickListener(this);
        resetTv.setOnClickListener(this);
    }

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
                    }
                }

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isNeedStartService = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isNeedStartService = false;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(maskBroadCast);
        super.onDestroy();
    }
}
