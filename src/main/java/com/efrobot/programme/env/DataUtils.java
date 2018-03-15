package com.efrobot.programme.env;

import android.content.Context;

import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.db.DBManager;

import java.util.ArrayList;
import java.util.List;

import static com.efrobot.programme.env.Constans.ExecuteContent.MOUSE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.SPEECH_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TIME_TYPE;

/**
 * Created by zd on 2018/3/9.
 */

public class DataUtils {

    public static List<ExecuteModule> getExecuteData(Context context) {
        List<ExecuteModule> executeModuleList = new ArrayList<>();
        executeModuleList.addAll(new DBManager(context).queryExecuteItems());
        return executeModuleList;
    }

    public static List<ExecuteModule> getTiggerData() {
        List<ExecuteModule> executeModuleList = new ArrayList<>();

        ExecuteModule executeModule1 = new ExecuteModule();
        executeModule1.setType(TIME_TYPE);

        ExecuteModule executeModule2 = new ExecuteModule();
        executeModule2.setType(MOUSE_TYPE);

        ExecuteModule executeModule3 = new ExecuteModule();
        executeModule3.setType(SPEECH_TYPE);

        executeModuleList.add(executeModule1);
        executeModuleList.add(executeModule2);
        executeModuleList.add(executeModule3);

        return executeModuleList;
    }

    public static List<String> getTtsList() {
        List<String> strings = new ArrayList<>();
        strings.add("");
        strings.add("福建省的客户的风格");
        strings.add("东方国际时尚的了解过");
        strings.add("的方式更健康地方的风格的风格地方");
        strings.add("啊收到回复开始的好看还是客观和开发商的");
        return strings;
    }

}
