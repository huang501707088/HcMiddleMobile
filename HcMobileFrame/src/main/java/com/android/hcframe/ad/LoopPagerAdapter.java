/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-28 上午11:46:38
*/
package com.android.hcframe.ad;

import java.util.List;

import com.android.hcframe.HcLog;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.adapter.ViewHolderFactory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public abstract class LoopPagerAdapter<T> extends RecyclingPagerAdapter {

	private static final String TAG = "LoopPagerAdapter";
	
	private final List<T> mDataList;
	
	/** 是否支持循环播放 */
	private final boolean mCanLoop;
	
	private OnClickListener mListener;
	
	protected final ViewHolderFactory<T> mViewHolderFactory;
	
	public LoopPagerAdapter(List<T> data, boolean canLoop, ViewHolderFactory<T> factory) {
		mCanLoop = canLoop;
		mDataList = data;
		mViewHolderFactory = factory;
	}

	/**
	 * 转换为实际的索引值,即根据{@link LoopViewPager#setCurrentItem(int)}设置的值
	 * @param position 在{@link LoopViewPager}中的实际索引
	 * @return
     */
    public int toRealPosition(int position) {
        if (!mCanLoop)return position;
        int realCount = getRealCount();
        if (realCount == 0)
            return 0;
        int realPosition = (position-1) % realCount;
        if (realPosition < 0)
            realPosition += realCount;
//        HcLog.D(TAG + " #toRealPosition position = "+position + " realPosition = "+realPosition);
        return realPosition;
    }

    public int toInnerPosition(int realPosition) {
        if (!mCanLoop) return realPosition;
        int position = (realPosition + 1);
        return position;
    }

    private int getRealFirstPosition() {
        return mCanLoop ? 1 : 0;
    }

    private int getRealLastPosition() {
        return mCanLoop ? getRealFirstPosition() + getRealCount() - 1 : getRealCount() - 1;
    }

    @Override
    public int getCount() {
        return mCanLoop ? getRealCount() + 2 : getRealCount();
    }

    public int getRealCount() {
        return getSize();
    }

    private int getSize() {
    	if(mDataList == null) return 0;
        return mDataList.size();
    }
    
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		int realPosition = toRealPosition(position);
//		HcLog.D(TAG + " #instantiateItem position = "+position + " realPosition ="+realPosition);
		return super.instantiateItem(container, realPosition);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		// 注意,向左滑动的时候,realPostion会有重复的
        int realPosition = toRealPosition(position);
//		HcLog.D(TAG + " #destroyItem position = "+position + " realPosition ="+realPosition);
		super.destroyItem(container, realPosition, object);
	}
    
    public boolean canLoop() {
    	return mCanLoop;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		// TODO Auto-generated method stub
		T data = mDataList.get(position);
		ViewHolderBase<T> base;
		if (convertView == null) {
			base = mViewHolderFactory.createViewHolder();
			if (base != null) {
				convertView = base.createView(LayoutInflater.from(container.getContext()));
//				base.setItemData(position, data);
				convertView.setTag(base);
			} 
		} else {
			base = (ViewHolderBase<T>) convertView.getTag();
			
		}
		if (base != null) {
			base.setItemData(position, data);
		}
		if (mListener != null) {
			convertView.setOnClickListener(mListener);
		}
		return convertView;
	}
	
	public void setOnItemClickListener(OnClickListener listener) {
		mListener = listener;
	}
	
}
