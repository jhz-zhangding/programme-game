package com.efrobot.programme.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.MainProjectAdapter;
import com.efrobot.programme.base.BaseActivity;
import com.efrobot.programme.bean.MainProject;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.dialog.ItemNameDialog;
import com.efrobot.programme.view.drag.DragListView;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ItemNameDialog mItemNameDialog;

    //项目
    private ListView projectListView;
    private MainProjectAdapter mainProjectAdapter;
    private List<MainProject> projectList;

    //内容
    private DragListView dragContentListView;
//    private

    private DBManager dbManager;

    private boolean isShowDelProject = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initListener();
        setProjectAdapter();
    }

    private void init() {
        dbManager = new DBManager(this);
        projectListView = findViewById(R.id.main_item_listview);
    }

    private void initListener() {
        findViewById(R.id.new_scene).setOnClickListener(this);
        findViewById(R.id.edit_sence_item).setOnClickListener(this);
        projectListView.setOnItemClickListener(new OnProjectItemOnClick());
    }

    private void setProjectAdapter() {
        projectList = dbManager.queryProjectItem();
        if (mainProjectAdapter == null) {
            mainProjectAdapter = new MainProjectAdapter(projectList);
            mainProjectAdapter.setOnItemDeleteListener(new MainProjectAdapter.OnItemDeleteListener() {
                @Override
                public void onDelete(int projectId) {
                    //需要删除项目下的所有内容

                }
            });
            projectListView.setAdapter(mainProjectAdapter);
        } else {
            mainProjectAdapter.updateDataSource(projectList);
        }
    }

    class OnProjectItemOnClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //切换项目
            mainProjectAdapter.setSelectPosition(i);
        }
    }

    private final int UPDATE_PROJECT_MESSAGE = 1;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_PROJECT_MESSAGE:
                    setProjectAdapter();
                    projectListView.setSelection(projectList.size());
                    mainProjectAdapter.setSelectPosition(projectList.size() - 1);
                    break;
            }
        }
    };


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.new_scene:
                if (dbManager.queryProjectItem().size() < 100) {
                    newSenceShowDiaolg();
                } else {
                    toast("已达到添加上限，无法继续添加！");
                }
                break;
            case R.id.edit_sence_item:
                if (mainProjectAdapter != null) {
                    isShowDelProject = !isShowDelProject;
                    mainProjectAdapter.setShowDelImg(isShowDelProject);
                }
                break;
        }
    }

    /**
     * 新建项目dialog
     */
    public void newSenceShowDiaolg() {
        mItemNameDialog = new ItemNameDialog(this, "新建场景", -1, 0, "");
        mItemNameDialog.setClicklistener(new ItemNameDialog.ClickListenerInterface() {
            @Override
            public void doConfirm(String senceName) {
                addScene(senceName);
                hintInputMethod(mItemNameDialog.input_ultrasonic);
                mItemNameDialog.dismiss();
            }

            @Override
            public void doCancel() {
                mItemNameDialog.dismiss();
            }
        });
        mItemNameDialog.show();
    }

    public void addScene(String sceneName) {
        if (!TextUtils.isEmpty(sceneName)) {
            try {
                byte[] bytes = sceneName.trim().getBytes("GBK");
                if (bytes.length >= 2) {
                    if (dbManager.isExistProjectName(sceneName)) {
                        toast("名称已重复");
                    } else {
                        MainProject mainProject = new MainProject();
                        mainProject.setProjectName(sceneName);
                        dbManager.insertProjectItem(mainProject);
                        toast("添加成功");
                        handler.sendEmptyMessage(UPDATE_PROJECT_MESSAGE);
                    }
                } else {
                    toast("输入少于1个字符");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            toast("无有效内容");
        }
    }
}
