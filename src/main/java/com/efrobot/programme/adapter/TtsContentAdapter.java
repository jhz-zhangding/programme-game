package com.efrobot.programme.adapter;

import android.content.Context;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.base.BaseCommAdapter;
import com.efrobot.programme.adapter.base.ViewHolder;
import com.efrobot.programme.bean.TypeBean;

import java.util.List;

/**
 * Created by zd on 2018/3/6.
 */

public class TtsContentAdapter extends BaseCommAdapter<String> {

    public TtsContentAdapter(List<String> datas) {
        super(datas);
    }

    @Override
    protected void setUI(ViewHolder holder, int position, Context context) {
        final String typeBean = getItem(position);
        TextView textView = holder.getItemView(R.id.item_secenname);
        textView.setText(typeBean);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_tts_item;
    }
}
