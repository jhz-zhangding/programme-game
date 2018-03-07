package com.efrobot.programme.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zd on 2018/3/7.
 */

public class MouseBean {

    private String name;

    private int type;

    private int image;

    private static int[] mouseImage = new int[]{};

    public static List<MouseBean> getMouseBeans() {
        List<MouseBean> mouseBeans = new ArrayList<>();
        MouseBean mouseBean1 = new MouseBean("电源", mouseImage[0], 1);
        MouseBean mouseBean2 = new MouseBean("游戏", mouseImage[1], 2);
        MouseBean mouseBean3 = new MouseBean("主页", mouseImage[2], 3);
        MouseBean mouseBean4 = new MouseBean("返回", mouseImage[3], 4);
        mouseBeans.add(mouseBean1);
        mouseBeans.add(mouseBean2);
        mouseBeans.add(mouseBean3);
        mouseBeans.add(mouseBean4);
        return mouseBeans;
    }

    public MouseBean(String name, int type, int image) {
        this.name = name;
        this.type = type;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
