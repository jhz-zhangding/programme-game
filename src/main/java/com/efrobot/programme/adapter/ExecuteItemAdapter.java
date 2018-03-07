package com.efrobot.programme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.efrobot.programme.R;
import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.MouseBean;
import com.efrobot.programme.view.drag.DragListViewAdapter;

import java.util.List;

import static com.efrobot.programme.env.Constans.ExecuteContent.ACTION_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.FACE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.MOUKEY_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.SPEECH_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TIME_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TOTAL_VIEW_COUNT;
import static com.efrobot.programme.env.Constans.ExecuteContent.TTS_TYPE;

/**
 * Created by zd on 2018/3/7.
 */

public class ExecuteItemAdapter extends DragListViewAdapter<ExecuteModule> {

    private LayoutInflater inflater;

    private List<MouseBean> mouseBeanList;

    public ExecuteItemAdapter(Context context, List<ExecuteModule> dataList) {
        super(context, dataList);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        ExecuteModule executeModule = getItem(position);
        int type = executeModule.getType();
        if (type == TTS_TYPE || type == ACTION_TYPE || type == FACE_TYPE) {
            //同种布局
            return TTS_TYPE;
        }
        return type;
    }

    @Override
    public int getViewTypeCount() {
        return TOTAL_VIEW_COUNT;
    }

    @Override
    public View getItemView(int position, View convertView, ViewGroup parent) {
        TimeViewHolder timeViewHolder = null;
        MouseViewHolder mouseViewHolder = null;
        SpeechViewHolder speechViewHolder = null;
        TextViewHolder textViewHolder = null;

        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case TIME_TYPE:
                    convertView = inflater.inflate(R.layout.item_time_layout, parent, false);
                    timeViewHolder = new TimeViewHolder();
                    timeViewHolder.editText = convertView.findViewById(R.id.time_edit);
                    convertView.setTag(timeViewHolder);
                    break;
                case MOUKEY_TYPE:
                    convertView = inflater.inflate(R.layout.item_mouse_layout, parent, false);
                    mouseViewHolder = new MouseViewHolder();
                    mouseViewHolder.spinner = convertView.findViewById(R.id.mouse_spinner);
                    convertView.setTag(mouseViewHolder);
                    break;
                case SPEECH_TYPE:
                    convertView = inflater.inflate(R.layout.item_speech_layout, parent, false);
                    speechViewHolder = new SpeechViewHolder();
                    speechViewHolder.editText = convertView.findViewById(R.id.speech_edit);
                    convertView.setTag(speechViewHolder);
                    break;
                case TTS_TYPE:
                    convertView = inflater.inflate(R.layout.item_text_layout, parent, false);
                    textViewHolder = new TextViewHolder();
                    textViewHolder.textView = convertView.findViewById(R.id.text_content);
                    convertView.setTag(textViewHolder);
                    break;
            }
        } else {
            switch (type) {
                case TIME_TYPE:
                    timeViewHolder = (TimeViewHolder) convertView.getTag();
                    break;
                case MOUKEY_TYPE:
                    mouseViewHolder = (MouseViewHolder) convertView.getTag();
                    break;
                case SPEECH_TYPE:
                    speechViewHolder = (SpeechViewHolder) convertView.getTag();
                    break;
                case TTS_TYPE:
                    textViewHolder = (TextViewHolder) convertView.getTag();
                    break;
            }
        }

        ExecuteModule executeModule = getItem(position);
        switch (type) {
            case TIME_TYPE:
                timeViewHolder.editText.setText(executeModule.getTime());
                break;
            case MOUKEY_TYPE:
                mouseViewHolder.spinner.setAdapter(new MouseSpinnerAdapter(mContext, MouseBean.getMouseBeans()));
                mouseViewHolder.spinner.setSelection(0);
                break;
            case SPEECH_TYPE:
                speechViewHolder.editText.setText(executeModule.getRecognitionText());
                break;
            case TTS_TYPE:
                textViewHolder.textView.setText(executeModule.getTts());
                break;
        }

        return convertView;
    }

    class TimeViewHolder {
        EditText editText;
    }

    class MouseViewHolder {
        Spinner spinner;
    }

    class SpeechViewHolder {
        EditText editText;
    }

    class TextViewHolder {
        TextView textView;
    }

}
