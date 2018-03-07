package com.efrobot.programme.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.bean.MouseBean;

import java.util.List;

/**
 * Created by zd on 2018/3/7.
 */

public class MouseSpinnerAdapter implements SpinnerAdapter {

    private Context context;

    private List<MouseBean> mouseBeanList;

    public MouseSpinnerAdapter(Context context, List<MouseBean> mouseBeanList) {
        this.context = context;
        this.mouseBeanList = mouseBeanList;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.mouse_item_select, null);
        textView.setText(mouseBeanList.get(i).getName());
        return textView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return mouseBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return mouseBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.mouse_item_select, null);
        textView.setText(mouseBeanList.get(i).getName());
        return textView;
    }

    @Override
    public int getItemViewType(int i) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
