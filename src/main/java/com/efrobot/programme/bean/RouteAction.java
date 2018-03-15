package com.efrobot.programme.bean;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by zd on 2018/3/15.
 * 字段routeId : 最好关联RoutePoint表，有时间再做
 */

public class RouteAction {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int id;

    @DatabaseField(columnName = "actionId")
    private String actionId;

    @DatabaseField(columnName = "actionName")
    private String actionName;

    @DatabaseField(columnName = "routeId")
    private int routeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }
}
