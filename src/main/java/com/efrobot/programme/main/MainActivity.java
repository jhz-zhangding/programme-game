package com.efrobot.programme.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.AddFaceAndActionAdapter;
import com.efrobot.programme.adapter.ExecuteItemAdapter;
import com.efrobot.programme.adapter.ItemContentAdapter;
import com.efrobot.programme.adapter.MainProjectAdapter;
import com.efrobot.programme.adapter.TtsContentAdapter;
import com.efrobot.programme.base.BaseActivity;
import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.FaceAndActionEntity;
import com.efrobot.programme.bean.MainProject;
import com.efrobot.programme.bean.TypeBean;
import com.efrobot.programme.dao.DataManager;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.dialog.ItemNameDialog;
import com.efrobot.programme.env.DataUtils;
import com.efrobot.programme.service.ProgrammeGameService;
import com.efrobot.programme.utils.DiyFaceAndActionUtils;
import com.efrobot.programme.utils.PreferencesUtils;
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

    private DBManager dbManager;

    private boolean isShowDelProject = false;

    private RelativeLayout typeIncludeRl;

    //输出分类
    private ExecuteItemAdapter triggerItemAdapter;
    private OnlyDragListView triggerListView;
    private List<ExecuteModule> triggerBeanList;

    private View triggerLayout, menuLayout, ttsLayout, actionLayout, faceLayout;

    //动作、表情
    private HashMap<String, String> mFaceList;
    private HashMap<String, String> mActionList;
    private int type = 1;

    //输出列表
    private ListView typeListView;

    //说话
    private OnlyDragListView ttsListView;
    private TtsContentAdapter ttsContentAdapter;
    private List<String> stringList;
    //表情
    private OnlyDragListView faceListView;
    //动作
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
    private boolean isNeedStartService;

    private int currentSelectId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentSelectId = PreferencesUtils.getInt(this, "currentSelectId", -1);

        init();
        initListener();
        setProjectAdapter();
        initData();
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

        triggerListView = findViewById(R.id.trigger_condition_list_view);
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
        findViewById(R.id.run_btn).setOnClickListener(this);
        projectListView.setOnItemClickListener(new OnProjectItemOnClick());
        typeListView.setOnItemClickListener(new OnTypeItemOnClick());

        triggerListView.setOnItemDragUpListener(new OnTriggerItemDragListener());
        ttsListView.setOnItemDragUpListener(new OnTtsItemDragListener());
        faceListView.setOnItemDragUpListener(new OnFaceItemDragListener());
        actionListView.setOnItemDragUpListener(new OnActionItemDragListener());

        registerMaskStateReceiver();
    }

    private void initData() {
        /**
         * Tts
         */
        stringList = DataUtils.getTtsList();
        ttsContentAdapter = new TtsContentAdapter(MainActivity.this, stringList);
        ttsListView.setAdapter(ttsContentAdapter);

        /**
         * 动作和表情数据
         */
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

        showFaceAndAction(1);
        showFaceAndAction(2);

        /**
         * 输出类型数据
         */
        typeBeans = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            TypeBean typeBean = new TypeBean();
            typeBean.setName(types[i]);
            typeBean.setType(i);
            typeBeans.add(typeBean);
        }
        itemContentAdapter = new ItemContentAdapter(typeBeans);
        typeListView.setAdapter(itemContentAdapter);

        /**
         * 可执行Item数据
         */
        executeModuleList = dbManager.queryExecuteById(currentSelectId);
        executeItemAdapter = new ExecuteItemAdapter(this, executeModuleList);
        dragContentListView.setAdapter(executeItemAdapter);

        /**
         * 触发类型数据
         */
        triggerBeanList = DataUtils.getTiggerData();
        triggerItemAdapter = new ExecuteItemAdapter(this, triggerBeanList);
        triggerListView.setAdapter(triggerItemAdapter);
    }

    /**
     * 项目数据
     */
    private void setProjectAdapter() {
        projectList = dbManager.queryProjectItem();
        if (projectList == null || projectList.size() == 0) {
            currentSelectId = -1;
        }
        if (mainProjectAdapter == null) {
            mainProjectAdapter = new MainProjectAdapter(projectList);
            mainProjectAdapter.setSelectPosition(currentSelectId);
            mainProjectAdapter.setOnItemDeleteListener(new MainProjectAdapter.OnItemDeleteListener() {
                @Override
                public void onDelete(int projectId) {
                    //需要删除项目下的所有内容
                    dbManager.deleteMoreExecuteItems(dbManager.queryExecuteById(projectId));
                    updateExecuteData();
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
            currentSelectId = projectList.get(i).getId();
            mainProjectAdapter.setSelectPosition(currentSelectId);

            updateExecuteData();
        }
    }

    private void updateExecuteData() {
        executeModuleList = dbManager.queryExecuteById(currentSelectId);
        if (executeItemAdapter != null) {
            executeItemAdapter.updateAdapteData(executeModuleList);
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
                    currentSelectId = projectList.get(projectList.size() - 1).getId();
                    mainProjectAdapter.setSelectPosition(currentSelectId);

                    updateExecuteData();
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
            case R.id.run_btn:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //重新排序
                        dbManager.deleteMoreExecuteItems(executeModuleList);
                        for (int i = 0; i < executeModuleList.size(); i++) {
                            dbManager.insertExcuteItem(executeModuleList
                                    .get(i));
                        }
                        Intent intentService = new Intent(MainActivity.this, ProgrammeGameService.class);
                        startService(intentService);
                    }
                });
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

    /**
     * 添加项目
     *
     * @param sceneName
     */
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
     * 从触发列表中取出来的数据位置和坐标
     */

    class OnTriggerItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = triggerBeanList.get(position);
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    /**
     * 左边功能区域拖拽结束后坐标监听
     */
    class OnTtsItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
//            if (position == 0) {
//                tempExecuteMoudule.setType(TTS_INPUT_TYPE);
//                tempExecuteMoudule.setTts("");
//            } else
            {
                tempExecuteMoudule.setType(TTS_TYPE);
                tempExecuteMoudule.setTts(stringList.get(position));
            }
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    class OnActionItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
            tempExecuteMoudule.setType(ACTION_TYPE);
            tempExecuteMoudule.setAction(actionAdapter.getItemFromPosition(position).index);
            addDataOnPosition(tempExecuteMoudule, x, y);
        }
    }

    class OnFaceItemDragListener implements OnItemDragUpListener {

        @Override
        public void onViewUpCoordinate(int position, int x, int y) {
            tempExecuteMoudule = new ExecuteModule();
            tempExecuteMoudule.setType(FACE_TYPE);
            tempExecuteMoudule.setFace(faceAdapter.getItemFromPosition(position).index);
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
        executeModule.setModuleId(currentSelectId);
        if (currentSelectId == -1) {
            Toast.makeText(this, "项目为空", Toast.LENGTH_SHORT).show();
            return;
        }
        dragContentListView.addDataToPosition(executeModule, x - dragListViewInScreenX, y - dragListViewInScreenY);
        dbManager.insertExcuteItem(executeModule);
    }

    private void registerMaskStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ROBOT_MASK_CHANGE);
        registerReceiver(maskBroadCast, filter);
    }

    String ROBOT_MASK_CHANGE = "android.intent.action.MASK_CHANGED";
    public final static String KEYCODE_MASK_ONPROGRESS = "KEYCODE_MASK_ONPROGRESS";
    public final static String KEYCODE_MASK_CLOSE = "KEYCODE_MASK_CLOSE";
    public final static String KEYCODE_MASK_OPEN = "KEYCODE_MASK_OPEN";

    private BroadcastReceiver maskBroadCast = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ROBOT_MASK_CHANGE)) {
                boolean maskOnProgress = intent.getBooleanExtra(KEYCODE_MASK_ONPROGRESS, false);
                boolean maskClose = intent.getBooleanExtra(KEYCODE_MASK_CLOSE, false);
                boolean maskOpen = intent.getBooleanExtra(KEYCODE_MASK_OPEN, false);

                if (maskClose) {
                    if (isNeedStartService) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //重新排序
                                dbManager.deleteMoreExecuteItems(executeModuleList);
                                for (int i = 0; i < executeModuleList.size(); i++) {
                                    dbManager.insertExcuteItem(executeModuleList
                                            .get(i));
                                }
                                Intent intentService = new Intent(context, ProgrammeGameService.class);
                                context.startService(intentService);
                            }
                        });

                    }
                }

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isNeedStartService = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isNeedStartService = false;
        PreferencesUtils.putInt(this, "currentSelectId", currentSelectId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(maskBroadCast);

    }
}
