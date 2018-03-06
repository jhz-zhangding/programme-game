package com.efrobot.programme.db;

import android.content.Context;

import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.MainProject;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.List;

/**
 * Created by zd on 2018/3/5.
 */

public class DBManager {

    private RuntimeExceptionDao<MainProject, Integer> mainProjectDao;

    private RuntimeExceptionDao<ExecuteModule, Integer> exceptionDao;

    public DBManager(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        mainProjectDao = dbHelper.getRuntimeExceptionDao(MainProject.class);
        exceptionDao = dbHelper.getRuntimeExceptionDao(ExecuteModule.class);
    }

    /***
     * mainProject
     */
    public void insertProjectItem(MainProject mainProject) {
        mainProjectDao.create(mainProject);
    }

    public void deleteProjectItem(int id) {
        mainProjectDao.deleteById(id);
    }

    public void deleteMoreProjectItem(List<MainProject> mainProjects) {
        mainProjectDao.delete(mainProjects);
    }

    public void updateProjectItem(MainProject mainProject) {
        mainProjectDao.update(mainProject);
    }

    public List<MainProject> queryProjectItem() {
        return mainProjectDao.queryForAll();
    }

    public List<MainProject> queryProjectByName(String projectName) {
        return mainProjectDao.queryForEq("projectName", projectName);
    }

    public boolean isExistProjectName(String projectName) {
        boolean isExist = false;
        List<MainProject> mainProjects = mainProjectDao.queryForEq("projectName", projectName);
        if (mainProjects != null && mainProjects.size() > 0) {
            isExist = true;
        }
        return isExist;
    }

    /***
     * content
     */
    public void insertExcuteItem(ExecuteModule executeModule) {
        exceptionDao.create(executeModule);
    }

    public void deleteExcuteItem(int id) {
        exceptionDao.deleteById(id);
    }

    public void deleteMoreExecuteItems(List<ExecuteModule> executeModules) {
        exceptionDao.delete(executeModules);
    }

    public void updateExcuteItem(ExecuteModule executeModule) {
        exceptionDao.update(executeModule);
    }

    public List<ExecuteModule> queryExecuteItems() {
        return exceptionDao.queryForAll();
    }


}
