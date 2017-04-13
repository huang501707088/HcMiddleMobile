/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午12:31:24
*/
package com.android.hcframe.container;

import android.content.Context;
import android.util.AttributeSet;

import com.android.hcframe.view.circle.CircleShader;
import com.android.hcframe.view.circle.ShaderHelper;

public class ContainerCircleImageView extends ContainerImageView implements ViewElement {

	private static final String TAG = "ContainerCircleImageView";
	
	private CircleShader mShader;
	
	public ContainerCircleImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ContainerCircleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ContainerCircleImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ShaderHelper createImageViewHelper() {
		// TODO Auto-generated method stub
		mShader = new CircleShader();
        return mShader;
	}
	
	public float getBorderRadius() {
        if(mShader != null) {
            return mShader.getBorderRadius();
        }
        return 0;
    }

    public void setBorderRadius(final float borderRadius) {
        if(mShader != null) {
        	mShader.setBorderRadius(borderRadius);
            invalidate();
        }
    }
}
