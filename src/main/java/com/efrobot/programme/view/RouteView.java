package com.efrobot.programme.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.efrobot.programme.bean.RouteAction;
import com.efrobot.programme.bean.RoutePoint;
import com.efrobot.programme.db.DBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zd on 2018/3/14.
 */

public class RouteView extends View {

    private DBManager dbManager;

    private List<RoutePoint> routePointList = new ArrayList<>();

    private final String TAG = RouteView.class.getSimpleName();

    private boolean isRestoreView = false;

    Paint routeViewPaint;

    Paint imagePaint = new Paint();

    private int row = 10;

    private int column = 10;

    private int viewSize = 70;

    //手指按下坐标
    private int mDownX = -1;
    private int mDownY = -1;

    //手指按下后当前view坐标
    private int mImageDownX;
    private int mImageDownY;

    private boolean isHasFirstRobotImg = false;

    //当前机器人正面方向：前后左右
    private int currentDirection = 1;
    private final int DOWN = 1;
    private final int UP = 2;
    private final int LEFT = 3;
    private final int RIGHT = 4;

    private boolean isHasOrder = false;
    private int currentOrder = 0;

    public static final int GO_FRONT = 1;
    public static final int GO_BACK = 2;
    public static final int TURN_LEFT = 3;
    public static final int TURN_RIGHT = 4;

    public RouteView(Context context) {
        super(context);
        init();
    }

    public RouteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        routeViewPaint = new Paint();
        routeViewPaint.setColor(Color.BLACK);

        imagePaint.setTextSize(10);
        imagePaint.setColor(0xffff0000);
        dbManager = new DBManager(getContext());
        routePointList.addAll(dbManager.queryRoutePointItems());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isRestoreView) {
            drawLayout(canvas);
            isRestoreView = false;
        } else {

            drawLayout(canvas);
            if (routePointList.size() == 0) {
                if (!isHasFirstRobotImg && mDownX != -1) {
                    getCoordinateFromPoint(mDownX, mDownY, canvas);
                }
            } else {
                drawImage(canvas);
            }

            if (isHasOrder) {
                drawRobotRouteImage(currentOrder, canvas);
                isHasOrder = false;
            }

        }

    }

    private void drawLayout(Canvas canvas) {
        //画行
        for (int i = 0; i < row + 1; i++) {
            canvas.drawLine(0, i * viewSize, viewSize * column, i * viewSize, routeViewPaint);
        }

        //画列
        for (int i = 0; i < column + 1; i++) {
            canvas.drawLine(i * viewSize, 0, i * viewSize, viewSize * row, routeViewPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        // 获取第一个手指点的Action
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (routePointList.size() == 0) {
                    mDownX = (int) ev.getX();
                    mDownY = (int) ev.getY();
                    invalidate();
                }

                break;


        }
        return true;
    }

    private void getCoordinateFromPoint(int mDownX, int mDownY, Canvas canvas) {
        //x点所在方格原点
        int columnX = mImageDownX = mDownX / viewSize * viewSize;
        int rowY = mImageDownY = mDownY / viewSize * viewSize;
        if (isPositionValid(columnX, rowY)) {
            addPoint(columnX, rowY, canvas);
            isHasFirstRobotImg = true;
        }
    }

    /**
     * 添加路线路
     *
     * @param x
     * @param y
     * @param canvas
     */
    private void addPoint(int x, int y, Canvas canvas) {
        RoutePoint routePoint = new RoutePoint();
        routePoint.setPointX(x);
        routePoint.setPointY(y);
        routePointList.add(routePoint);
        drawImage(canvas);
        //入库
        dbManager.insertRoutePoint(routePoint);
    }

    /**
     * 画图
     *
     * @param canvas
     */
    private void drawImage(Canvas canvas) {
        for (int i = 0; i < routePointList.size(); i++) {
            int mDownX = routePointList.get(i).getPointX();
            int mDownY = routePointList.get(i).getPointY();
            Rect rect = new Rect(mDownX, mDownY, mDownX + viewSize, mDownY + viewSize);
            canvas.drawRect(rect, imagePaint);
        }
    }

    /**
     * @param order
     * @param canvas
     */
    private void drawRobotRouteImage(int order, Canvas canvas) {
        if (currentOrder != 0) {
            if (isHasFirstRobotImg) {
                switch (order) {
                    case GO_FRONT:
                        //机器人正面朝下
                        if (currentDirection == DOWN) {
                            //向下走一格
                            if (isPositionValid(mImageDownX, mImageDownY + viewSize)) {
                                mImageDownY += viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_FRONT);
                            } else {
                                drawImage(canvas);
                            }
                            Log.e(TAG, "mImageDownY = " + mImageDownY);
                        } else if (currentDirection == UP) {
                            //向上走一格
                            if (isPositionValid(mImageDownX, mImageDownY - viewSize)) {
                                mImageDownY -= viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_FRONT);
                            } else {
                                drawImage(canvas);
                            }
                        } else if (currentDirection == LEFT) {
                            //向左走一格
                            if (isPositionValid(mImageDownX - viewSize, mImageDownY)) {
                                mImageDownX -= viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_FRONT);
                            } else {
                                drawImage(canvas);
                            }
                        } else if (currentDirection == RIGHT) {
                            //向右走一格
                            if (isPositionValid(mImageDownX + viewSize, mImageDownY)) {
                                mImageDownX += viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_FRONT);
                            } else {
                                drawImage(canvas);
                            }
                        }

                        break;
                    case GO_BACK:
                        //机器人正面朝下
                        if (currentDirection == DOWN) {
                            //向上走一格
                            if (isPositionValid(mImageDownX, mImageDownY - viewSize)) {
                                mImageDownY -= viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_BACK);
                            } else {
                                drawImage(canvas);
                            }
                        } else if (currentDirection == UP) {
                            //向下走一格
                            if (isPositionValid(mImageDownX, mImageDownY - viewSize)) {
                                mImageDownY -= viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_BACK);
                            } else {
                                drawImage(canvas);
                            }
                            mImageDownY += viewSize;
                        } else if (currentDirection == LEFT) {
                            //向右走一格
                            if (isPositionValid(mImageDownX + viewSize, mImageDownY)) {
                                mImageDownX += viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_BACK);
                            } else {
                                drawImage(canvas);
                            }
                        } else if (currentDirection == RIGHT) {
                            //向左走一格
                            if (isPositionValid(mImageDownX - viewSize, mImageDownY)) {
                                mImageDownX -= viewSize;
                                addPoint(mImageDownX, mImageDownY, canvas);
                                addRouteActionToDb(GO_BACK);
                            } else {
                                drawImage(canvas);
                            }
                        }
                        break;
                    case TURN_LEFT:
                        //左转向
                        switch (currentDirection) {
                            case DOWN:
                                currentDirection = RIGHT;
                                break;
                            case UP:
                                currentDirection = LEFT;
                                break;
                            case LEFT:
                                currentDirection = DOWN;
                                break;
                            case RIGHT:
                                currentDirection = UP;
                                break;
                        }
                        drawImage(canvas);
                        addRouteActionToDb(TURN_LEFT);
                        break;
                    case TURN_RIGHT:
                        //右转向
                        switch (currentDirection) {
                            case DOWN:
                                currentDirection = LEFT;
                                break;
                            case UP:
                                currentDirection = RIGHT;
                                break;
                            case LEFT:
                                currentDirection = UP;
                                break;
                            case RIGHT:
                                currentDirection = DOWN;
                                break;
                        }
                        drawImage(canvas);
                        addRouteActionToDb(TURN_RIGHT);
                        break;
                }
            }
        }
    }

    /**
     * 是否超出界面
     *
     * @param mDownX
     * @param mDownY
     * @return
     */
    private boolean isPositionValid(int mDownX, int mDownY) {
        return mDownX < row * viewSize && mDownY < column * viewSize && mDownX >= 0 && mDownY >= 0;
    }

    /**
     * 发送机器人移动指令,需要画出线路图
     *
     * @param order
     */
    public void moveRobot(int order) {
        currentOrder = order;
        isHasOrder = true;
        invalidate();
    }

    /**
     * 重做
     */
    public void restoreView() {
        isRestoreView = true;

        isHasFirstRobotImg = false;

        routePointList.clear();

        invalidate();

        //删除所有数据
        dbManager.deleteRouteAction(dbManager.queryRouteActionItems());
        dbManager.deleteRoutePoint(dbManager.queryRoutePointItems());
    }

    /**
     * 撤销一步
     */
    public void revokeView() {
        if (routePointList.size() > 0) {
            routePointList.remove(routePointList.size() - 1);
        }

        List<RouteAction> actionList = dbManager.queryRouteActionItems();
        if (actionList != null && actionList.size() > 0) {
            dbManager.deleteRouteAction(actionList.get(actionList.size() - 1));
        }
        invalidate();
    }

    private void addRouteActionToDb(int type) {
        RouteAction routeAction = new RouteAction();
        switch (type) {
            case GO_FRONT:
                routeAction.setActionId("2008");
                routeAction.setActionName("前进");
                break;
            case GO_BACK:
                routeAction.setActionId("2009");
                routeAction.setActionName("后退");
                break;
            case TURN_LEFT:
                routeAction.setActionId("5");
                routeAction.setActionName("左转");
                break;
            case TURN_RIGHT:
                routeAction.setActionId("6");
                routeAction.setActionName("右转");
                break;
        }
        dbManager.insertRouteAction(routeAction);
    }


}
