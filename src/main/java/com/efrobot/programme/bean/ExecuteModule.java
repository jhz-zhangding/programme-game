package com.efrobot.programme.bean;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by zd on 2018/3/2.
 */

public class ExecuteModule implements Serializable {

    @DatabaseField(generatedId = true, columnName = "_id")
    private int id;
    //模块id
    @DatabaseField(columnName = "moduleId")
    private int moduleId;
    //类型 : 1识别语音、2空鼠按键、3定时、4动作、5表情、6说话
    @DatabaseField(columnName = "type")
    private int type;
    @DatabaseField(columnName = "recognitionText")
    private String recognitionText;
    @DatabaseField(columnName = "mouseKeyCode")
    private int mouseKeyCode;
    @DatabaseField(columnName = "time")
    private String time;
    @DatabaseField(columnName = "face")
    private String face = "";
    @DatabaseField(columnName = "action")
    private String action = "";
    @DatabaseField(columnName = "tts")
    private String tts = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRecognitionText() {
        return recognitionText;
    }

    public void setRecognitionText(String recognitionText) {
        this.recognitionText = recognitionText;
    }

    public int getMouseKeyCode() {
        return mouseKeyCode;
    }

    public void setMouseKeyCode(int mouseKeyCode) {
        this.mouseKeyCode = mouseKeyCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTts() {
        return tts;
    }

    public void setTts(String tts) {
        this.tts = tts;
    }
}
