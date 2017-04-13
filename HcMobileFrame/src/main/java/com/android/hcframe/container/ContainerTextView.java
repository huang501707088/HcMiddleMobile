/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-12 下午3:59:39
*/
package com.android.hcframe.container;

import java.util.List;

import com.android.hcframe.BadgeTextView;
import com.android.hcframe.HcLog;
import com.android.hcframe.R;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.container.data.AppInfo;
import com.android.hcframe.container.data.ElementInfo;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class ContainerTextView extends BadgeTextView implements ViewElement, IHttpResponse {

    private static final String TAG = "ContainerTextView";

    private String mId;

    public ContainerTextView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ContainerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ContainerTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AppView_Element, defStyle, 0);
        mId = a.getString(R.styleable.AppView_Element_element_id);
        a.recycle();
    }

    @Override
    public String getElementId() {
        // TODO Auto-generated method stub
        return mId;
    }

    @Override
    public void setValue(String value, int type, String attrId) {
        // TODO Auto-generated method stub
//        HcLog.D(TAG + " setValue value = " + value + " type = " + type + " attrId = " + attrId);
        if (TextUtils.isEmpty(value)) {
            setVisibility(View.GONE);
        } else {
            switch (type) {
                case ViewInfo.VALUE_REQUEST_NONE:
                    if (getVisibility() != View.VISIBLE)
                        setVisibility(View.VISIBLE);
                    setText(value);
                    break;
                case ViewInfo.VALUE_REQUEST_ONCE:
                    // 第一次可能需要去服务端获取

                    break;

                case ViewInfo.VALUE_REQUEST_EVERY:
                    // 去服务的获取数据

                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void setValue(ViewInfo info) {
        // TODO Auto-generated method stub
        if (info instanceof ElementInfo) {
            setElementValue(info);
        } else if (info instanceof AppInfo) {
            List<ViewInfo> elements = info.getViewInfos();
//            HcLog.D(TAG + " setValue element size =" + elements.size());
            boolean visibility = false;
            for (ViewInfo viewInfo : elements) {
//				HcLog.D(TAG + " setValue element id ="+viewInfo.getViewId());
                if (mId.equals(viewInfo.getViewId())) {
                    setElementValue(viewInfo);
                    return;
                }
            }
            if (!visibility)
                setVisibility(View.GONE);
        } else {
            throw new IllegalArgumentException("ContainerTextView setValue ViewInfo must ElementInfo or AppInfo  info = " + info);
        }
        /*List<ViewInfo> elements = info.getViewInfos();
		String value = null;
		String attrId = null;
		int type = ViewInfo.VALUE_REQUEST_NONE;
		for (ViewInfo viewInfo : elements) {
			if (mId.equals(viewInfo.getViewId())) {
				value = viewInfo.getElementValue();
				type = viewInfo.getRequestType();
				attrId = viewInfo.getAttrId();
				break;
			}
		}
		setValue(value, type, attrId);*/
    }

    /**
     * @param info {@link ElementInfo}
     * @author jrjin
     * @time 2015-11-20 下午2:02:14
     */
    private void setElementValue(final ViewInfo info) {
        String value = null;
        String attrId = null;
        String action = null;
        int type = ViewInfo.VALUE_REQUEST_NONE;
        value = info.getElementValue();
        type = info.getRequestType();
        attrId = info.getAttrId();
        action = info.getViewAction();
        setValue(value, type, attrId);
        if (!TextUtils.isEmpty(action)) {
            setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    BadgeCache.getInstance().operateBadge(info.getContainerId() + "_" +info.getAppId());
                    info.onClick(getContext(), ViewInfo.CLICK_TYPE_TEXT);
                }
            });
        }
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {

    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }

}
