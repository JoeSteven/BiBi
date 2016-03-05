package com.joe.bibi.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joe.bibi.R;
import com.kyleduo.switchbutton.SwitchButton;

/**
 * Created by Joe on 2016/3/5.
 */
public class SettingView extends FrameLayout {
    private static final String NAMESPACE="http://schemas.android.com/apk/res-auto";
    private TextView title;
    private TextView desc;
    private SwitchButton sb;
    private String mTitle="";//输入的标题
    private String mDesc="";//输入的描述
    private OnToggleChangeListener mListener;
    public SettingView(Context context) {
        super(context);
        initView();
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTitle=attrs.getAttributeValue(NAMESPACE,"setTitle");
        mDesc=attrs.getAttributeValue(NAMESPACE,"setDesc");
        initView();
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTitle=attrs.getAttributeValue(NAMESPACE,"setTitle");
        mDesc=attrs.getAttributeValue(NAMESPACE,"setDesc");
        initView();
    }


    private void initView() {
        View.inflate(getContext(), R.layout.view_setting_view,this);
        title = (TextView) findViewById(R.id.tv_title_setting);
        desc = (TextView) findViewById(R.id.tv_desc_setting);
        sb = (SwitchButton) findViewById(R.id.switch_setting);
        setTitleAndDesc(mTitle, mDesc);
        sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mListener!=null) mListener.onChanged(buttonView,isChecked);
            }
        });
    }

    public void setTitleAndDesc(String Title,String Desc) {
        title.setText(Title);
        if(TextUtils.isEmpty(Desc)){
            desc.setVisibility(View.GONE);
        }else {
            desc.setText(Desc);
            desc.setVisibility(View.VISIBLE);
        }
    }

    public void setToggle(boolean open){
            sb.setCheckedImmediately(open);
    }
    public void setOnToggleChangeListener(OnToggleChangeListener Listener){
        this.mListener=Listener;
    }
    public interface OnToggleChangeListener{
        public void onChanged(CompoundButton buttonView, boolean isChecked);
    }

}
