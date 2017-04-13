/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-21 下午1:22:44
*/
package com.android.hcframe.container;

import android.content.Context;
import android.util.AttributeSet;

import com.android.hcframe.view.circle.CircleShader;
import com.android.hcframe.view.circle.RoundedShader;
import com.android.hcframe.view.circle.ShaderHelper;

public class ContainerRoundedImageView extends ContainerImageView {

private static final String TAG = "ContainerRoundedImageView";
	
	private RoundedShader mShader;
	
	public ContainerRoundedImageView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ContainerRoundedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ContainerRoundedImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ShaderHelper createImageViewHelper() {
		// TODO Auto-generated method stub
		mShader = new RoundedShader();
        return mShader;
	}
	
	public final int getRadius() {
        if(mShader != null) {
            return mShader.getRadius();
        }
        return 0;
    }

    public final void setRadius(final int radius) {
        if(mShader != null) {
        	mShader.setRadius(radius);
            invalidate();
        }
    }
}
