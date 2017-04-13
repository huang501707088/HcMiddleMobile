package com.android.hcframe.view.toast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.R;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-9 10:13.
 */

public class NoDataView extends LinearLayout {

    private LinearLayout mParent;

    private ImageView mSrc;

    private TextView mDescription;

    public NoDataView(Context context) {
        this(context, null);
    }

    public NoDataView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoDataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public NoDataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.no_data_layout, this, true);
        mParent = (LinearLayout) findViewById(R.id.no_data_parent);
        mSrc = (ImageView) findViewById(R.id.no_data_src);
        mDescription = (TextView) findViewById(R.id.no_data_text);
    }

    /**
     * 设置没有数据时的提示
     * @param description
     */
    public void setDescription(String description) {
        mDescription.setText(description);
    }

    public void setOnClickListener(OnClickListener listener, int resId) {
        if (resId == R.id.no_data_parent) {
            mParent.setOnClickListener(listener);
        } else if (resId == R.id.no_data_src) {
        	mSrc.setOnClickListener(listener);
        } else if (resId == R.id.no_data_text) {
            mDescription.setOnClickListener(listener);
        }
    }

    /**
     * 设置{@link android.view.ViewGroup}的点击事件
     * @param listener
     */
    public void setParentOnClickListener(OnClickListener listener) {
        mParent.setOnClickListener(listener);
    }

    /**
     * 设置{@link ImageView}的点击事件
     * @param listener
     */
    public void setSrcOnClickListener(OnClickListener listener) {
        mSrc.setOnClickListener(listener);
    }

    /**
     * 设置{@link TextView}的点击事件
     * @param listener
     */
    public void setTextOnClickListener(OnClickListener listener) {
        mDescription.setOnClickListener(listener);
    }
}

