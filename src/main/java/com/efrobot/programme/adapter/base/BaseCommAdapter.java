package com.efrobot.programme.adapter.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by zd on 2018/3/5.
 */

public abstract class BaseCommAdapter<T> extends BaseAdapter {

    public List<T> mDatas;

    public BaseCommAdapter(List<T> datas) {
        this.mDatas = datas;
    }

    public void updateDataSource(List<T> datas) {
        this.mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder = ViewHolder
                .newsInstance(convertView, parent.getContext(), getLayoutId());

        setUI(holder,position,parent.getContext());

        return holder.getConverView();
    }

    protected abstract void setUI(ViewHolder holder, int position, Context context);

    protected abstract int getLayoutId();
}
