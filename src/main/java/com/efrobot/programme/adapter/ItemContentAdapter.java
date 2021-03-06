package com.efrobot.programme.adapter;

import android.content.Context;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.base.BaseCommAdapter;
import com.efrobot.programme.adapter.base.ViewHolder;
import com.efrobot.programme.bean.TypeBean;

import java.util.List;

/**
 * Created by zd on 2018/3/5.
 */

public class ItemContentAdapter extends BaseCommAdapter<TypeBean> {

    public ItemContentAdapter(List<TypeBean> datas) {
        super(datas);
    }

    @Override
    protected void setUI(ViewHolder holder, final int position, Context context) {
        final TypeBean typeBean = getItem(position);
        TextView textView = holder.getItemView(R.id.item_secenname);
        textView.setText(typeBean.getName());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_type_item;
    }
}
