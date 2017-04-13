/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-17 下午1:58:56
*/
package com.android.hcframe;

import android.os.Handler;
import android.os.Message;

public class Countdown extends Handler {

	private static final String TAG = "Countdown";
	
	private static final int PLAY_TIME = 1;
	
	private static final int PLAY_TOTLE = 60;
	
	private int mWhat;
	
	private long mPlayTime;
	
	/**
	 * 总共的时长,单位为s
	 */
	private int mTotle;
	
	private static int mNumber = 0;
	
	private HandlerCallback mCallback;
	
	public Countdown() {
		this(PLAY_TIME, PLAY_TOTLE);
	}
	
	/**
	 * @author jrjin
	 * @date 2015-12-17 下午4:01:00
	 * @param playTime 倒计时时间间隔,单位为second
	 */
	public Countdown(long playTime) {
		this(playTime, PLAY_TOTLE);
	}
	
	/**
	 * 
	 * @param totleTime 倒计时总时间,单位为second
	 */
	public Countdown(int totleTime) {
		this(PLAY_TIME, totleTime);
	}
	
	/**
	 * 
	 * @param playTime 倒计时时间间隔,单位为second
	 * @param totleTime 倒计时总时间,单位为second
	 */
	public Countdown(long playTime,int totleTime) {
		mWhat = mNumber++;
		mPlayTime = playTime;
		mTotle = totleTime;
		HcLog.D(TAG + " Countdown what = "+mWhat);
	}
	
	public void resume() {
        if (!hasMessages(mWhat)) {
            sendEmptyMessage(mWhat);
        }
    }

    public void pause() {
        removeMessages(mWhat);
    }
    
    public void reset() {
    	mTotle = PLAY_TOTLE;
    }
    
    public void reset(int totle) {
    	mTotle = totle;
    }
    
    @Override
    public void handleMessage(Message message) {
    	mTotle -= mPlayTime;
    	if (mTotle <= 0) {
    		pause();
    		if (mCallback != null) {
    			mCallback.setTime(0);
    		}
    		return;
    	}
    	if (mCallback != null) {
    		mCallback.setTime(mTotle);
    	}
        sendEmptyMessageDelayed(mWhat, mPlayTime * 1000);
    }
    
    public int getWhat() {
    	return mWhat;
    }
    
    /**
     * 
     * @author jrjin
     * @time 2015-12-17 下午2:16:22
     * @param totle 总时间,单位为s
     */
    public void setTotleTime(int totle) {
    	if (mTotle != totle && totle >= mPlayTime) {
    		pause();
    		mTotle = totle;
    		resume();
    	}
    }

    /**
     * 设置时间间隔
     * @author jrjin
     * @time 2015-12-17 下午2:20:59
     * @param playTime
     */
    public void setPlayTime(int playTime) {
    	if (mPlayTime != playTime && playTime <= mTotle) {
    		pause();
    		mPlayTime = playTime;
    		resume();
    	}
    }
    
    public interface HandlerCallback {
    	/**
    	 * 总共还有多少时间
    	 * @author jrjin
    	 * @time 2015-12-17 下午2:43:49
    	 * @param totle
    	 */
    	public void setTime(int totle);
    }
    
    public void setHandlerCallback(HandlerCallback callback) {
    	mCallback = callback;
    }
    
    public int getTotleTime() {
    	return mTotle >= 0 ? mTotle : 0;
    }
}
