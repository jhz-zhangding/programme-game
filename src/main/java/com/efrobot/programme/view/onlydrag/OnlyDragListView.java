package com.efrobot.programme.view.onlydrag;

/**
 * Created by zd on 2018/3/1.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.efrobot.programme.view.drag.CommonTool;
import com.efrobot.programme.view.drag.DragListViewAdapter;

/**
 * 可拖拽排序的ListView
 * 特点：
 * 1、可拖拽排序（条目右边1/4空间内）
 * 2、可添加头部控件headerView
 * 3、左滑可删除
 * 4、可设置点击和长按事件
 * <p>
 * Created by Ralap on 2016/5/8.
 */

public class OnlyDragListView extends ListView {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = OnlyDragListView.class.getSimpleName();

    /**
     * 拖拽快照的透明度(0.0f ~ 1.0f)。进入删除时的状态也取此值
     */
    private static final float DRAG_PHOTO_VIEW_ALPHA = .8f;

    /**
     * 上下滚动时的最大距离，可进行设置
     *
     * @see #setMaxDistance(int)
     * @see #getMaxDistance()
     */
    private int mMaxDistance = 30;


    public OnlyDragListView(Context context) {
        super(context);
        initialize();
    }

    public OnlyDragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public OnlyDragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        // 为了防止滑动时和滑动后背景变灰（系统默认颜色）,且XML中没有设置listSelector和cacheColorHint，这里确保两属性都是透明
        setSelector(android.R.color.transparent); // 或0
        setCacheColorHint(0);
    }

    /**
     * 是否处于拖拽中
     */
    private boolean mIsDraging = false;

    /**
     * 按下时的坐标位置
     */
    private int mDownX;
    private int mDownY;

    /**
     * 移动时的坐标
     */
    private int mMoveX;
    private int mMoveY;

    /**
     * 原生偏移量。也就是ListView的左上角相对于屏幕的位置
     */
    private int mRawOffsetX;
    private int mRawOffsetY;

    /**
     * 在条目中的位置
     */
    private int mItemOffsetX;
    private int mItemOffsetY;

    /**
     * 拖拽条目的高度
     */
    private int mDragItemHeight;

    /**
     * 被拖拽的条目位置
     */
    private int mDragPosition;

    /**
     * 窗口管理器，用于显示条目的快照
     */
    private WindowManager mWindowManager;

    /**
     * 窗口管理的布局参数
     */
    private WindowManager.LayoutParams mWindowLayoutParams;

    /**
     * 拖拽条目的快照图片
     */
    private Bitmap mDragPhotoBitmap;

    /**
     * 正在拖拽的条目快照view
     */
    private ImageView mDragPhotoView;

    /**
     * 是否处于滚动状态
     */
    private boolean mIsScrolling;

    /**
     * 长按后，执行了长按事件的标记
     */
    private boolean mLongClickFlag;

    /**
     * 条目长按监听器
     */
    private OnItemLongClickListener mDeletingItemLongClickListener;

    /**
     * 速度追踪器
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 头部总高度
     */
    @SuppressWarnings("unused")
    private int mHeaderHeightSum = 0;
    /**
     * 脚部总高度
     */
    @SuppressWarnings("unused")
    private int mFooterHeightSum = 0;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 获取第一个手指点的Action
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 初始化操作
                setOnItemLongClickListener(mDeletingItemLongClickListener);
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(ev);

                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();
                // 获取当前触摸位置对应的条目索引
                mDragPosition = pointToPosition(mDownX, mDownY);
                // 如果触摸的坐标不在条目上，在分割线、或外部区域，则为无效值-1; 宽度3/4 以右的区域可拖拽; Header和Footer无效
                if (!isPositionValid(mDragPosition)) {
                    return super.onTouchEvent(ev);
                }
                mIsDraging = true;
                mRawOffsetX = (int) (ev.getRawX() - mDownX);
                mRawOffsetY = (int) (ev.getRawY() - mDownY);
                // 开始拖拽的前期工作：展示item快照
                startDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);

                mMoveX = (int) ev.getX();
                mMoveY = (int) ev.getY();
                if (mIsDraging) {
                    // 更新快照位置
                    updateDragView();
                    // 更新当前被替换的位置
                }

                break;
            case MotionEvent.ACTION_UP:
                rstLongClickFlag();

                if (mIsDraging) {
                    // 停止拖拽
                    stopDrag();
                }
                recycleVelocityTracker();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 判断位置是否有效
     *
     * @param position 需判断的位置
     * @return 是否有效
     */
    private boolean isPositionValid(int position) {
        return !(position == AdapterView.INVALID_POSITION
                || position < getHeaderViewsCount());
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean startDrag() {
        // 实际在ListView中的位置，因为涉及到条目的复用
        final View itemView = getItemView(mDragPosition);
        if (itemView == null) {
            return false;
        }
        // 进行绘图缓存
        itemView.setDrawingCacheEnabled(true);
        // 提取缓存中的图片
        mDragPhotoBitmap = Bitmap.createBitmap(itemView.getDrawingCache());
        // 清除绘图缓存，否则复用的时候，会出现前一次的图片。或使用销毁destroyDrawingCache()
        itemView.setDrawingCacheEnabled(false);

        // 隐藏。为了防止隐藏时出现画面闪烁，使用动画去除闪烁效果
        Animation aAnim = new AlphaAnimation(1f, DRAG_PHOTO_VIEW_ALPHA);
        aAnim.setDuration(50);
        aAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsDraging) {
                    itemView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        itemView.startAnimation(aAnim);

        mItemOffsetX = mDownX - itemView.getLeft();
        mItemOffsetY = mDownY - itemView.getTop();
        mDragItemHeight = itemView.getHeight();
        createDragPhotoView();
        return true;
    }

    /**
     * 判断ListView是否全部显示，即无法上下滚动了
     */
    private boolean isShowAll() {
        if (getChildCount() == 0) {
            return true;
        }
        View firstChild = getChildAt(0);
        int itemAllHeight = firstChild.getBottom() - firstChild.getTop() + getDividerHeight();
        return itemAllHeight * getAdapter().getCount() < getHeight();
    }

    /**
     * 创建拖拽快照
     */
    private void createDragPhotoView() {
        // 获取当前窗口管理器
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        // 创建布局参数
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.START;
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 期望的图片为半透明效果，然而并没有看到不一样的效果
        // 下面这些参数能够帮助准确定位到选中项点击位置
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowLayoutParams.windowAnimations = 0; // 无动画
        mWindowLayoutParams.alpha = DRAG_PHOTO_VIEW_ALPHA; // 微透明

        mWindowLayoutParams.x = mDownX + mRawOffsetX - mItemOffsetX;
        mWindowLayoutParams.y = (mDownY + mRawOffsetY - mItemOffsetY);
        Log.e("createDragPhotoView", "mDownY=" + mDownY + "//mRawOffsetY=" + mRawOffsetY + "//mItemOffsetY=" + mItemOffsetY);
        Log.e("createDragPhotoView", "mWindowLayoutParams.y=" + mWindowLayoutParams.y);

        mDragPhotoView = new ImageView(getContext());
        mDragPhotoView.setImageBitmap(mDragPhotoBitmap);
        mWindowManager.addView(mDragPhotoView, mWindowLayoutParams);
    }

    /**
     * 根据Adapter中的位置获取对应ListView的条目
     */
    private View getItemView(int position) {
        if (position < 0 || position >= getAdapter().getCount()) {
            return null;
        }
        int index = position - getFirstVisiblePosition();
        return getChildAt(index);
    }

    private void updateDragView() {
        if (mDragPhotoView != null) {
            Log.e("createDragPhotoView", "mDownY=" + mDownY + "//mRawOffsetY=" + mRawOffsetY + "//mItemOffsetY=" + mItemOffsetY);
            mWindowLayoutParams.y = mMoveY + mRawOffsetY - mItemOffsetY;
            Log.e("createDragPhotoView", "mWindowLayoutParams.y=" + mWindowLayoutParams.y);
            mWindowManager.updateViewLayout(mDragPhotoView, mWindowLayoutParams);
        }
    }

    /**
     * 停止拖拽
     */
    private void stopDrag() {
        // 显示坐标上的条目
        View view = getItemView(mDragPosition);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        // 移除快照
        if (mDragPhotoView != null) {
            mWindowManager.removeView(mDragPhotoView);
            mDragPhotoView.setImageDrawable(null);
            mDragPhotoBitmap.recycle();
            mDragPhotoBitmap = null;
            mDragPhotoView = null;
        }
        mIsDraging = false;
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        super.setOnItemLongClickListener(listener);
        if (listener != null) {
            this.mDeletingItemLongClickListener = listener;
        }
    }

    /**
     * 置位长按事件执行标记
     */
    public void setLongClickFlag() {
        this.mLongClickFlag = true;
    }

    /**
     * 复位长按事件执行标记
     */
    public void rstLongClickFlag() {
        this.mLongClickFlag = false;
    }


    /**
     * 为了获取头部高度
     */
    @Override
    public void addHeaderView(View v) {
        mHeaderHeightSum += v.getMeasuredHeight();
        super.addHeaderView(v);
    }

    /**
     * 为了获取底部高度
     */
    @Override
    public void addFooterView(View v) {
        mFooterHeightSum += v.getMeasuredHeight();
        super.addFooterView(v);
    }


    /**
     * Setter and Getter
     */
    @SuppressWarnings("unused")
    public void setMaxDistance(int maxDistance) {
        if (maxDistance > 0) {
            mMaxDistance = maxDistance;
        }
    }

    @SuppressWarnings("unused")
    public int getMaxDistance() {
        return mMaxDistance;
    }
}