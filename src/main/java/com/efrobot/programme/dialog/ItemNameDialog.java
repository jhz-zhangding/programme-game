package com.efrobot.programme.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.utils.NameLengthFilter;

public class ItemNameDialog {
    private Context mContext;
    private String mtitle;
    private ClickListenerInterface clickListenerInterface;
    private TextView tvTitle, dialog_edit, tvCancel, tvConfirm;
    private Dialog dialog;
    private View customview;

    private LinearLayout layout_ultrasonic;
    private LinearLayout layout_sence;
    public EditText input_ultrasonic;
    //0是新建和修改场景名，1是输入距离
    private int mType;
    /**
     * Dialog主题
     */
    private int theme;
    //修改时原本的内容
    private String mycontent;

    public interface ClickListenerInterface {

        public void doConfirm(String senceName);

        public void doCancel();
    }

    public ItemNameDialog(Context context, String title, int theme, int type, String content) {
        mContext = context;
        mtitle = title;
        mycontent = content;
        this.theme = theme;
        this.mType = type;
//        this.theme = R.style.NewSettingDialog;
    }

    public void init() {
        dialog = new Dialog(mContext, theme);
        customview = LayoutInflater.from(mContext).inflate(R.layout.dialog_layout, null);
        dialog.setContentView(customview);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        DisplayMetrics m = mContext.getResources().getDisplayMetrics();
        int screenWidth = Math.min(m.widthPixels, m.heightPixels);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (screenWidth * 0.8);
        params.height = (int) (screenWidth * 0.5);
        dialog.getWindow().setAttributes(params);

        layout_ultrasonic = (LinearLayout) customview.findViewById(R.id.layout_ultrasonic);
        input_ultrasonic = (EditText) customview.findViewById(R.id.input_ultrasonic);
        InputFilter[] filters = {new NameLengthFilter(12)};
        layout_sence = (LinearLayout) customview.findViewById(R.id.layout_sence);
        dialog_edit = (TextView) customview.findViewById(R.id.input_file_name);
        dialog_edit.setFilters(filters);
        tvTitle = (TextView) customview.findViewById(R.id.title);
        tvCancel = (TextView) customview.findViewById(R.id.tv_cancle);
        tvConfirm = (TextView) customview.findViewById(R.id.tv_ok);

        if (mType == 1) {
            input_ultrasonic.setText(mycontent);
            layout_ultrasonic.setVisibility(View.VISIBLE);
            layout_sence.setVisibility(View.GONE);
        } else {
            dialog_edit.setText(mycontent);
            layout_sence.setVisibility(View.VISIBLE);
            layout_ultrasonic.setVisibility(View.GONE);
        }
        tvTitle.setText(mtitle);
        tvConfirm.setText("确定");
        tvCancel.setText("取消");
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerInterface.doCancel();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == 1) {
                    clickListenerInterface.doConfirm(input_ultrasonic.getText().toString().trim());
                } else {
                    clickListenerInterface.doConfirm(dialog_edit.getText().toString().trim());
                }
            }
        });
    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }


    /**
     * 显示
     */
    public void show() {
        if (dialog == null) {
            init();
        }
        dialog.show();

    }

    /**
     * 是否显示
     *
     * @return 是否显示
     */
    public boolean isShowing() {

        return dialog != null && dialog.isShowing();
    }

    /**
     * dialog 小时
     */
    public void dismiss() {
        if (isShowing()) {
            dialog.dismiss();
        }
    }
}
