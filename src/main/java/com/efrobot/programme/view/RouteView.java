package com.efrobot.programme.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.efrobot.programme.R;
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

    Paint routeViewPaint;

    Paint imagePaint = new Paint();

    //行
    private int row = 10;

    //列
    private int column = 10;

    //格子大小
    private int viewSize = 70;

    //重做
    private boolean isRestoreView = false;

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

    //是否有发送移动指令
    private boolean isHasOrder = false;
    private int currentOrder = 0;

    public static final int GO_FRONT = 1;
    public static final int GO_BACK = 2;
    public static final int TURN_LEFT = 3;
    public static final int TURN_RIGHT = 4;

    private Bitmap backBitmap;
    private Bitmap frontBitmap;
    private Bitmap leftBitmap;
    private Bitmap rightBitmap;

    //图片相对格子的偏移量
    private int offSetX = 10;
    private int offSetY = 10;

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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setViewSize(int viewSize) {
        this.viewSize = viewSize;
    }

    private void init() {
        routeViewPaint = new Paint();
        routeViewPaint.setColor(Color.BLACK);

        imagePaint.setTextSize(10);
        imagePaint.setColor(0x90ff0000);
        dbManager = new DBManager(getContext());
        routePointList.addAll(dbManager.queryRoutePointItems());

        if (routePointList.size() > 0) {
            mImageDownX = routePointList.get(routePointList.size() - 1).getPointX();
            mImageDownY = routePointList.get(routePointList.size() - 1).getPointY();
        }

        backBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robot_back);
        frontBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robot_front);
        leftBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robot_left);
        rightBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.robot_right);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取默认的宽高值
        setMeasuredDimension(column * viewSize, row * viewSize);
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
                    mDownX = -1;
                }
            } else {
                if (isHasOrder) {
                    drawRobotRouteImage(currentOrder, canvas);
                    isHasOrder = false;
                } else {
                    drawImage(canvas);
                }
            }

        }

    }

    /**
     * 路线图
     *
     * @param canvas
     */
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

    /**
     * 获取手指按压下的格子坐标
     *
     * @param mDownX
     * @param mDownY
     * @param canvas
     */
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
        routePoint.setCurrentDirection(currentDirection);
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
        if (routePointList.size() > 0) {
            RoutePoint routePoint = routePointList.get(routePointList.size() - 1);
            int mX = routePoint.getPointX() + offSetX;
            int mY = routePoint.getPointY() + offSetY;
            currentDirection = routePoint.getCurrentDirection();
            switch (currentDirection) {
                case DOWN:
                    canvas.drawBitmap(frontBitmap, mX, mY, null);
                    break;
                case UP:
                    canvas.drawBitmap(backBitmap, mX, mY, null);
                    break;
                case LEFT:
                    canvas.drawBitmap(leftBitmap, mX, mY, null);
                    break;
                case RIGHT:
                    canvas.drawBitmap(rightBitmap, mX, mY, null);
                    break;

            }
        }
    }

    /**
     * @param order
     * @param canvas
     */
    private void drawRobotRouteImage(int order, Canvas canvas) {
        if (routePointList.size() > 0) {
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
                            toast("机器人已到达边缘，不能前进啦");
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
                            toast("机器人已到达边缘，不能前进啦");
                        }
                    } else if (currentDirection == LEFT) {
                        //向左走一格
                        if (isPositionValid(mImageDownX - viewSize, mImageDownY)) {
                            mImageDownX -= viewSize;
                            addPoint(mImageDownX, mImageDownY, canvas);
                            addRouteActionToDb(GO_FRONT);
                        } else {
                            drawImage(canvas);
                            toast("机器人已到达边缘，不能前进啦");
                        }
                    } else if (currentDirection == RIGHT) {
                        //向右走一格
                        if (isPositionValid(mImageDownX + viewSize, mImageDownY)) {
                            mImageDownX += viewSize;
                            addPoint(mImageDownX, mImageDownY, canvas);
                            addRouteActionToDb(GO_FRONT);
                        } else {
                            drawImage(canvas);
                            toast("机器人已到达边缘，不能前进啦");
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
                            toast("机器人已到达边缘，不能后退啦");
                        }
                    } else if (currentDirection == UP) {
                        //向下走一格
                        if (isPositionValid(mImageDownX, mImageDownY + viewSize)) {
                            mImageDownY += viewSize;
                            addPoint(mImageDownX, mImageDownY, canvas);
                            addRouteActionToDb(GO_BACK);
                        } else {
                            drawImage(canvas);
                            toast("机器人已到达边缘，不能后退啦");
                        }
                    } else if (currentDirection == LEFT) {
                        //向右走一格
                        if (isPositionValid(mImageDownX + viewSize, mImageDownY)) {
                            mImageDownX += viewSize;
                            addPoint(mImageDownX, mImageDownY, canvas);
                            addRouteActionToDb(GO_BACK);
                        } else {
                            drawImage(canvas);
                            toast("机器人已到达边缘，不能后退啦");
                        }
                    } else if (currentDirection == RIGHT) {
                        //向左走一格
                        if (isPositionValid(mImageDownX - viewSize, mImageDownY)) {
                            mImageDownX -= viewSize;
                            addPoint(mImageDownX, mImageDownY, canvas);
                            addRouteActionToDb(GO_BACK);
                        } else {
                            drawImage(canvas);
                            toast("机器人已到达边缘，不能后退啦");
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
                    addRouteActionToDb(TURN_LEFT);
                    addPoint(mImageDownX, mImageDownY, canvas);
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
                    addRouteActionToDb(TURN_RIGHT);
                    addPoint(mImageDownX, mImageDownY, canvas);
                    break;
            }
        } else {
            Toast.makeText(getContext(), "请选择起点", Toast.LENGTH_SHORT).show();
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
        if (routePointList.size() == 0) {
            toast("请先在方格中选择机器人开始的位置");
        }
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

        currentDirection = 1;

        currentOrder = 0;

        invalidate();

        //删除所有数据
        dbManager.deleteRouteAction(dbManager.queryRouteActionItems());
        dbManager.deleteRoutePoint(dbManager.queryRoutePointItems());

        if (onAddActionListener != null) {
            onAddActionListener.onAdd();
        }
    }

    /**
     * 撤销一步
     */
    public void revokeView() {
        if (routePointList.size() > 0) {
            if (routePointList.size() == 1) {
                isHasFirstRobotImg = false;
            }
            routePointList.remove(routePointList.size() - 1);
            List<RouteAction> actionList = dbManager.queryRouteActionItems();
            if (actionList != null && actionList.size() > 0) {
                dbManager.deleteRouteAction(actionList.get(actionList.size() - 1));
            }
            invalidate();
        }

        if (onAddActionListener != null) {
            onAddActionListener.onAdd();
        }
    }

    private void addRouteActionToDb(int type) {
        RouteAction routeAction = new RouteAction();
        switch (type) {
            case GO_FRONT:
                routeAction.setActionId("2008");
                routeAction.setActionName("前进一格");
                break;
            case GO_BACK:
                routeAction.setActionId("2009");
                routeAction.setActionName("后退一格");
                break;
            case TURN_LEFT:
                routeAction.setActionId("5");
                routeAction.setActionName("左转90°");
                break;
            case TURN_RIGHT:
                routeAction.setActionId("6");
                routeAction.setActionName("右转90°");
                break;
        }
        dbManager.insertRouteAction(routeAction);
        if (onAddActionListener != null) {
            onAddActionListener.onAdd();
        }
    }

    private OnAddActionListener onAddActionListener;

    public void setOnAddActionListener(OnAddActionListener onAddActionListener) {
        this.onAddActionListener = onAddActionListener;
    }

    public interface OnAddActionListener {
        void onAdd();
    }

    private Toast mToast;

    public void toast(String info) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        }
        mToast.setText(info);
        mToast.show();
    }

}
