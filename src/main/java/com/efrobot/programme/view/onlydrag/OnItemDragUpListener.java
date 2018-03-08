package com.efrobot.programme.view.onlydrag;

/**
 * Created by zd on 2018/3/8.
 */

public interface OnItemDragUpListener {

    /***
     * 触摸的位置和坐标
     * @param position
     * @param x
     * @param y
     */
    void onViewUpCoordinate(int position, int x, int y);

}
