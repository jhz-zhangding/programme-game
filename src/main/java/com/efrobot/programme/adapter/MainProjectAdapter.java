package com.efrobot.programme.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.base.BaseCommAdapter;
import com.efrobot.programme.adapter.base.ViewHolder;
import com.efrobot.programme.bean.MainProject;
import com.efrobot.programme.db.DBManager;

import java.util.List;

/**
 * Created by zd on 2018/3/5.
 */

public class MainProjectAdapter extends BaseCommAdapter<MainProject> {

    private boolean isShowDelImg = false;

    private OnItemDeleteListener onItemDeleteListener;

    private DBManager dbManager;

    int selectPosition = 0;

    public MainProjectAdapter(List<MainProject> datas) {
        super(datas);
    }

    public void setShowDelImg(boolean showDelImg) {
        isShowDelImg = showDelImg;
        notifyDataSetChanged();
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
        notifyDataSetChanged();
    }

    public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener) {
        this.onItemDeleteListener = onItemDeleteListener;
    }

    @Override
    protected void setUI(ViewHolder holder, final int position, Context context) {
        if (dbManager == null)
            dbManager = new DBManager(context);

        final MainProject mainProject = getItem(position);

        RelativeLayout relativeLayout = holder.getItemView(R.id.layout);
        ImageView delImage = holder.getItemView(R.id.del_item);
        TextView textView = holder.getItemView(R.id.item_secenname);

        if (mDatas.size() == 0) {
            isShowDelImg = false;
        }

        if (isShowDelImg) {
            delImage.setVisibility(View.VISIBLE);
        } else {
            delImage.setVisibility(View.GONE);
        }

        if (position == selectPosition) {
            relativeLayout.setBackgroundResource(R.color.item_select);
        } else {
            relativeLayout.setBackgroundColor(Color.parseColor("#01000000"));
        }

        delImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //删除项目
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.onDelete(mainProject.getId());
                }
                dbManager.deleteExcuteItem(mainProject.getId());
                mDatas.remove(position);
                notifyDataSetChanged();
            }
        });

        textView.setText(mainProject.getProjectName());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_listitem;
    }

    public interface OnItemDeleteListener {
        void onDelete(int projectId);
    }
}
