package com.efrobot.programme.bean;

import com.j256.ormlite.field.DatabaseField;

/**
 * 记录坐标图
 * Created by zd on 2018/3/15.
 */

public class RoutePoint {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int id;

    @DatabaseField(columnName = "pointX")
    private int pointX;

    @DatabaseField(columnName = "pointY")
    private int pointY;

    @DatabaseField(columnName = "currentDirection")
    private int currentDirection;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPointX() {
        return pointX;
    }

    public void setPointX(int pointX) {
        this.pointX = pointX;
    }

    public int getPointY() {
        return pointY;
    }

    public void setPointY(int pointY) {
        this.pointY = pointY;
    }

    public int getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(int currentDirection) {
        this.currentDirection = currentDirection;
    }
}
