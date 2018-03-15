package com.efrobot.programme.provider;

/**
 * Created by zd on 2017/11/11.
 */
public interface OnKeyEventListener {

    void onKeyDown(int keyCode);

    void onKeyUp(int keyCode);

    void onKeyLongPress(int keyCode);
}
