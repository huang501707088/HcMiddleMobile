package com.android.hcframe.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.hcframe.badge.BadgeObserver;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-9 19:45.
 */
public class IMBadgeView extends TextView implements BadgeObserver {

    public IMBadgeView(Context context) {
        this(context, null);
    }

    public IMBadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setCount(int count) {

    }

    @Override
    public void setFlag(int flag) {

    }
}
