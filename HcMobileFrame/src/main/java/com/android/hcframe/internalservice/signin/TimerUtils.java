package com.android.hcframe.internalservice.signin;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

public class TimerUtils {

	private Timer mTimer = null;

	private TimerTask mTimerTask = null;

	private Handler mHandler = null;

	private long updateTime;

	private long delay;

	private ITimerListerner iTimerListerner;

	/**
	 * 1:时间 2:距离
	 */
	private int type;

	public TimerUtils() {

	}

	public interface ITimerListerner {
		public <T> T onReturnData();
	}

	public void initTimer(long delay, long updateTime,
			ITimerListerner iTimerListerner, Handler mHandler) {
		this.mHandler = mHandler;

		this.updateTime = updateTime;

		this.iTimerListerner = iTimerListerner;

		this.delay = delay;
	}

	public void startTimer() {

		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				public void run() {
					String timeData = null;
					if (iTimerListerner != null) {
						timeData = iTimerListerner.onReturnData();
					}
					sendMessage(type, timeData);
				}
			};
		}

		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, delay, updateTime);
	}

	public void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	private void sendMessage(int id, String data) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler);
			message.what = id;
			message.obj = data;
			mHandler.sendMessage(message);
		}
	}

	public void setType(int type) {
		this.type = type;
	}
}
