package com.efrobot.programme.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.efrobot.programme.R;
import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.MouseBean;
import com.efrobot.programme.db.DBManager;
import com.efrobot.programme.utils.DiyFaceAndActionUtils;
import com.efrobot.programme.view.drag.DragListViewAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.efrobot.programme.env.Constans.ExecuteContent.ACTION_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.FACE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.MOUSE_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.SPEECH_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TIME_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TOTAL_VIEW_COUNT;
import static com.efrobot.programme.env.Constans.ExecuteContent.TTS_INPUT_TYPE;
import static com.efrobot.programme.env.Constans.ExecuteContent.TTS_TYPE;

/**
 * Created by zd on 2018/3/7.
 */

public class ExecuteItemAdapter extends DragListViewAdapter<ExecuteModule> {

    private LayoutInflater inflater;

    private int mType = 0;

    private final int TYPE_TIME = 0;
    private final int TYPE_MOUSE = 1;
    private final int TYPE_SPEECH = 2;
    private final int TYPE_TEXT = 3;
    private final int TYPE_INPUT_TEXT = 4;

    private DiyFaceAndActionUtils utils;

    public ExecuteItemAdapter(Context context, List<ExecuteModule> dataList) {
        super(context, dataList);
        inflater = LayoutInflater.from(context);
        utils = new DiyFaceAndActionUtils(context);
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    @Override
    public int getItemViewType(int position) {
        ExecuteModule executeModule = getItem(position);
        int type = executeModule.getType();
        switch (type) {
            case TIME_TYPE:
                return TYPE_TIME;
            case MOUSE_TYPE:
                return TYPE_MOUSE;
            case SPEECH_TYPE:
                return TYPE_SPEECH;
            case TTS_TYPE:
                return TYPE_TEXT;
            case ACTION_TYPE:
                return TYPE_TEXT;
            case FACE_TYPE:
                return TYPE_TEXT;
            case TTS_INPUT_TYPE:
                return TYPE_INPUT_TEXT;
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
        InputTextViewHolder inputTextViewHolder = null;

        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case TYPE_TIME:
                    convertView = inflater.inflate(R.layout.item_time_layout, parent, false);
                    timeViewHolder = new TimeViewHolder();
                    timeViewHolder.editText = convertView.findViewById(R.id.time_edit);
                    timeViewHolder.editText.setFocusable(false);
                    timeViewHolder.editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showTimePickerVIew(view);
                        }
                    });
                    convertView.setTag(timeViewHolder);
                    break;
                case TYPE_MOUSE:
                    convertView = inflater.inflate(R.layout.item_mouse_layout, parent, false);
                    mouseViewHolder = new MouseViewHolder();
                    mouseViewHolder.spinner = convertView.findViewById(R.id.mouse_spinner);
                    convertView.setTag(mouseViewHolder);
                    break;
                case TYPE_SPEECH:
                    convertView = inflater.inflate(R.layout.item_speech_layout, parent, false);
                    speechViewHolder = new SpeechViewHolder();
                    speechViewHolder.editText = convertView.findViewById(R.id.speech_edit);
                    convertView.setTag(speechViewHolder);
                    break;
                case TYPE_TEXT:
                    convertView = inflater.inflate(R.layout.item_text_layout, parent, false);
                    textViewHolder = new TextViewHolder();
                    textViewHolder.textView = convertView.findViewById(R.id.text_content);
                    convertView.setTag(textViewHolder);
                    break;
                case TYPE_INPUT_TEXT:
                    convertView = inflater.inflate(R.layout.item_edit_text_input_layout, parent, false);
                    inputTextViewHolder = new InputTextViewHolder();
                    inputTextViewHolder.editText = convertView.findViewById(R.id.tts_edit);
                    convertView.setTag(inputTextViewHolder);
                    break;
            }
        } else {
            switch (type) {
                case TYPE_TIME:
                    timeViewHolder = (TimeViewHolder) convertView.getTag();
                    break;
                case TYPE_MOUSE:
                    mouseViewHolder = (MouseViewHolder) convertView.getTag();
                    break;
                case TYPE_SPEECH:
                    speechViewHolder = (SpeechViewHolder) convertView.getTag();
                    break;
                case TYPE_TEXT:
                    textViewHolder = (TextViewHolder) convertView.getTag();
                    break;
                case TYPE_INPUT_TEXT:
                    inputTextViewHolder = (InputTextViewHolder) convertView.getTag();
                    break;
            }
        }

        if (type == 0) {

        } else if (type == 1) {

        }

        final ExecuteModule executeModule = getItem(position);
        switch (type) {
            case TYPE_TIME:
                timeViewHolder.editText.setText(executeModule.getTime());
                break;
            case TYPE_MOUSE:
                mouseViewHolder.spinner.setAdapter(new MouseSpinnerAdapter(mContext, MouseBean.getMouseBeans()));
                mouseViewHolder.spinner.setSelection(0);
                break;
            case TYPE_SPEECH:
                speechViewHolder.editText.setText(executeModule.getRecognitionText());
                break;
            case TYPE_TEXT:
                int itemType = executeModule.getType();
                if (itemType == TTS_TYPE) {
                    textViewHolder.textView.setText(executeModule.getTts());
                } else if (itemType == ACTION_TYPE) {
                    textViewHolder.textView.setText(utils.contrastAction(executeModule.getAction()));
                } else if (itemType == FACE_TYPE) {
                    textViewHolder.textView.setText(utils.contrastFace(executeModule.getFace()));
                }
                break;
            case TYPE_INPUT_TEXT:
                inputTextViewHolder.editText.setText(executeModule.getTts());
                inputTextViewHolder.editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        executeModule.setTts(editable.toString());
                        new DBManager(mContext).updateExcuteItem(executeModule);
                    }
                });
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

    class InputTextViewHolder {
        EditText editText;
    }

    private void showTimePickerVIew(final View tvTime) {
        //时间选择器
        TimePickerView pvTime = new TimePickerView.Builder(mContext, new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                ((TextView) tvTime).setText(getTime(date));
            }
        }).build();
        pvTime.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
        pvTime.show();
    }

    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

}
