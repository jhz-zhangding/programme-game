package com.efrobot.programme.bean;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by zd on 2018/3/2.
 */

public class MainProject {

    @DatabaseField(generatedId = true, columnName = "_id")
    private int id;

    @DatabaseField(columnName = "projectName")
    private String projectName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
