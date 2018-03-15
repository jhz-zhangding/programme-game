package com.efrobot.programme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.adapter.base.BaseCommAdapter;
import com.efrobot.programme.adapter.base.ViewHolder;
import com.efrobot.programme.bean.TypeBean;

import java.util.List;

/**
 * Created by zd on 2018/3/6.
 */

public class TtsContentAdapter extends BaseAdapter {

    private final int TYPE_EDIT = 0;
    private final int TYPE_TEXT = 1;
    private int TYPE_COUNT = 2;

    private List<String> datas;
    private Context mContext;

    private LayoutInflater inflater;

    public TtsContentAdapter(Context context, List<String> data) {
        this.mContext = context;
        this.datas = data;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_EDIT;
        }
        return TYPE_TEXT;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        EditViewHolder editViewHolder = null;
        TextViewHolder textViewHolder = null;
        int type = getItemViewType(i);
        if (view == null) {
            switch (type) {
                case TYPE_EDIT:
                    view = inflater.inflate(R.layout.item_text_input_layout, viewGroup, false);
                    editViewHolder = new EditViewHolder();
                    editViewHolder.editText = view.findViewById(R.id.tts_edit);
                    editViewHolder.editText.setFocusable(false);
                    view.setTag(editViewHolder);
                    break;
                case TYPE_TEXT:
                    view = inflater.inflate(R.layout.item_tts_item, viewGroup, false);
                    textViewHolder = new TextViewHolder();
                    textViewHolder.textView = view.findViewById(R.id.item_secenname);
                    view.setTag(textViewHolder);
                    break;
            }

        } else {
            if (type == TYPE_EDIT) {
                editViewHolder = (EditViewHolder) view.getTag();
            } else if (type == TYPE_TEXT) {
                textViewHolder = (TextViewHolder) view.getTag();
            }
        }

        if (type == TYPE_EDIT) {
            editViewHolder.editText.setText("");
        } else if (type == TYPE_TEXT) {
            textViewHolder.textView.setText(datas.get(i));
        }


        return view;
    }


    class EditViewHolder {
        TextView editText;
    }

    class TextViewHolder {
        TextView textView;
    }

}
