package com.efrobot.programme.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.AddFaceAndActionAdapter;
import com.efrobot.programme.adapter.ExecuteItemAdapter;
import com.efrobot.programme.adapter.ItemContentAdapter;
import com.efrobot.programme.adapter.MainProjectAdapter;
import com.efrobot.programme.base.BaseActivity;
import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.FaceAndActionEntity;
import com.efrobot.programme.bean.MainProject;
import com.efrobot.programme.bean.TypeBean;
import com.efrobot.programme.dao.DataManager;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.dialog.ItemNameDialog;
import com.efrobot.programme.utils.DiyFaceAndActionUtils;
import com.efrobot.programme.view.drag.DragListView;
import com.efrobot.programme.view.onlydrag.OnItemDragUpListener;
import com.efrobot.programme.view.onlydrag.OnlyDragListView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.efrobot.programme.env.Constans.ExecuteContent.ACTION_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.FACE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TTS_TYPE;

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

    private RelativeLayout typeIncludeRl;

    private View triggerLayout, menuLayout, ttsLayout, actionLayout, faceLayout;

    //输出分类
    private List<String> triggerList = new ArrayList<>();


    private HashMap<String, String> mFaceList;
    private HashMap<String, String> mActionList;
    private int type = 1;

    private ListView typeListView;

    private OnlyDragListView ttsListView;
    private OnlyDragListView faceListView;
    private OnlyDragListView actionListView;

    private String[] types = new String[]{"说话", "动作", "表情"};
    private List<TypeBean> typeBeans;
    private ItemContentAdapter itemContentAdapter;

    private ExecuteModule tempExecuteMoudule;
    private ExecuteItemAdapter executeItemAdapter;
    private List<ExecuteModule> executeModuleList;

    int location[] = new int[2];
    private int dragListViewInScreenX = 0;
    private int dragListViewInScreenY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initListener();
        initData();
        setProjectAdapter();
        dragContentListView.post(new Runnable() {
            @Override
            public void run() {
                dragContentListView.getLocationOnScreen(location);
                dragListViewInScreenX = location[0];
                dragListViewInScreenY = location[1];
            }
        });
    }

    private void init() {
        dbManager = new DBManager(this);
        projectListView = findViewById(R.id.main_item_listview);

        triggerLayout = findViewById(R.id.chufa_condition_layout);
        menuLayout = findViewById(R.id.menu_include_layout);
        ttsLayout = findViewById(R.id.tts_condition_layout);
        actionLayout = findViewById(R.id.action_condition_layout);
        faceLayout = findViewById(R.id.face_condition_layout);

        typeIncludeRl = findViewById(R.id.all_item_rl);
        typeListView = findViewById(R.id.menu_flag_list_view);
        ttsListView = findViewById(R.id.tts_list_view);
        faceListView = findViewById(R.id.face_list_view);
        actionListView = findViewById(R.id.action_list_view);

        dragContentListView = findViewById(R.id.content_drag_list_view);
    }

    private void initListener() {
        findViewById(R.id.new_scene).setOnClickListener(this);
        findViewById(R.id.edit_sence_item).setOnClickListener(this);

        findViewById(R.id.trigger_btn).setOnClickListener(this);
        findViewById(R.id.output_btn).setOnClickListener(this);
        projectListView.setOnItemClickListener(new OnProjectItemOnClick());
        typeListView.setOnItemClickListener(new OnTypeItemOnClick());

        ttsListView.setOnItemDragUpListener(new OnTtsItemDragListener());
        faceListView.setOnItemDragUpListener(new OnFaceItemDragListener());
        actionListView.setOnItemDragUpListener(new OnActionItemDragListener());

    }

    private void initData() {
        mFaceList = new DiyFaceAndActionUtils(this).readFaceData();
        mActionList = new HashMap<>();
        final List<FaceAndActionEntity> list = DataManager.getInstance(getContext()).queryAllAction();
        if (list != null && !list.isEmpty()) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                FaceAndActionEntity faceAndActionEntity = list.get(i);
                mActionList.put(faceAndActionEntity.index, faceAndActionEntity.content + "(" + faceAndActionEntity.time + ")");
            }
        }

        typeBeans = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            TypeBean typeBean = new TypeBean();
            typeBean.setName(types[i]);
            typeBean.setType(i);
            typeBeans.add(typeBean);
        }
        itemContentAdapter = new ItemContentAdapter(typeBeans);
        typeListView.setAdapter(itemContentAdapter);

        showFaceAndAction(1);
        showFaceAndAction(2);

        executeModuleList = new ArrayList<>();
        executeItemAdapter = new ExecuteItemAdapter(this, executeModuleList);
        dragContentListView.setAdapter(executeItemAdapter);
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

    class OnTypeItemOnClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String content = typeBeans.get(i).getName();
            switch (content) {
                case "说话":
                    showTypeListView(ttsLayout);
                    break;
                case "动作":
                    showTypeListView(actionLayout);
                    break;
                case "表情":
                    showTypeListView(faceLayout);
                    break;
            }
        }
    }

    private void showTypeListView(View view) {
        ttsLayout.setVisibility(View.GONE);
        faceLayout.setVisibility(View.GONE);
        actionLayout.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
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
            case R.id.trigger_btn:
                //触发
                typeIncludeRl.setVisibility(View.GONE);
                showView(triggerLayout);
                break;
            case R.id.output_btn:
                //输出
                typeIncludeRl.setVisibility(View.VISIBLE);
                showView(menuLayout);
                break;
        }
    }

    private void showView(View view) {
        triggerLayout.setVisibility(View.GONE);
        menuLayout.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
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

    AddFaceAndActionAdapter actionAdapter;
    AddFaceAndActionAdapter faceAdapter;

    /**
     * 初始化Adapter
     */
    private void initAdapter() {
        if (actionAdapter == null) {
            actionAdapter = new AddFaceAndActionAdapter(getContext());
            actionListView.setAdapter(actionAdapter);
        }
        if (faceAdapter == null) {
            faceAdapter = new AddFaceAndActionAdapter(getContext());
            faceListView.setAdapter(faceAdapter);
        }
    }

    /**
     * 显示动作或表情
     */
    public void showFaceAndAction(int myType) {
        this.type = myType;
        initAdapter();
        if (type == 1) {
            faceAdapter.setDataResource(mFaceList, 1);
        } else if (type == 2) {
            actionAdapter.setDataResource(mActionList, 2);
        }
    }

    /**
     * 从列表中取出来的数据位置和坐标
     */

    class OnTriggerItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int v) {

        }
    }

    /**
     * 左边功能区域拖拽结束后坐标监听
     */
    class OnTtsItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
            tempExecuteMoudule.setType(TTS_TYPE);
            tempExecuteMoudule.setTts(faceAdapter.getItemFromPosition(position).content);
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    class OnActionItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
            tempExecuteMoudule.setType(ACTION_TYPE);
            tempExecuteMoudule.setAction(actionAdapter.getItemFromPosition(position).content);
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    class OnFaceItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
            tempExecuteMoudule.setType(FACE_TYPE);
            tempExecuteMoudule.setFace(faceAdapter.getItemFromPosition(position).content);
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    /**
     * 向执行区域添加View
     *
     * @param executeModule
     * @param x
     * @param y
     */
    private void addDataOnPosition(ExecuteModule executeModule, int x, int y) {
        dragContentListView.addDataToPosition(executeModule, x - dragListViewInScreenX, y - dragListViewInScreenY);
    }

    /**
     * 添加item 需要支持拖拽
     */
    private void addItem() {

    }
}
