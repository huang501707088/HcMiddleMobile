/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-3 上午11:10:19
*/
package com.android.hcframe.lock;

import java.util.List;

import com.android.hcframe.HcUtil;
import com.android.hcframe.LoadActivity;
import com.android.hcframe.R;
import com.android.hcframe.lock.LockPatternViewEx.Cell;
import com.android.hcframe.lock.LockPatternViewEx.DisplayMode;
import com.android.hcframe.sql.SettingHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LockActivity extends Activity implements OnClickListener, 
	LockPatternViewEx.OnPatternListener {

	private static final String TAG = "LockActivity";
	
	private LockPatternViewEx mLockView;
	
	private TextView mTitle;
	private TextView mResetPW;
	private TextView mChangeAccount;
	
	private String mPW = null;
	
	private LockState mState = LockState.NORMAL;
	/**
	 * 锁屏状态，需要解锁。
	 */
	public static final int TAG_UNLOCK = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			int tag = intent.getIntExtra(LoadActivity.LOCK_TAG, -1);
			if (tag == TAG_UNLOCK) {
				mState = LockState.UNLOCK;
				mPW = SettingHelper.getGesturePw(this);
			}
		}
		
		setContentView(R.layout.activity_lock);
		initViews();
	}

	private void initViews() {
		mLockView = (LockPatternViewEx) findViewById(R.id.lock_pattern);
		
		mTitle = (TextView) findViewById(R.id.lock_draw_pw);
		mResetPW = (TextView) findViewById(R.id.lock_reset_pw);
		mChangeAccount = (TextView) findViewById(R.id.lock_manager_account);
		
		mResetPW.setOnClickListener(this);
		mChangeAccount.setOnClickListener(this);
		mLockView.setOnPatternListener(this);
		
		if (mState == LockState.UNLOCK) {
			mTitle.setText("");
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPatternStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPatternCleared() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		// TODO Auto-generated method stub
		
		switch (mState) {
		case NORMAL:
			if (pattern.size() < 2) {
				HcUtil.showToast(this, R.string.lock_pw_short);
				return;
			}
			mPW = parsePW(pattern);
			mState = LockState.LOCKED;
			mLockView.setEnabled(false);
			mTitle.setText(getResources().getString(R.string.lock_pw_again));
			mHandler.sendEmptyMessageDelayed(0, 1000);
			break;
		case LOCKED:
			if (!mPW.equals(parsePW(pattern))) {
				mLockView.setDisplayMode(DisplayMode.Wrong);
				mLockView.invalidate();
				mLockView.setEnabled(false);
				mTitle.setText(getResources().getString(R.string.lock_pw_not_same));
				mHandler.sendEmptyMessageDelayed(1, 500);
			} else {
				SettingHelper.setGesturePw(this, mPW);
				finish();
			}
			break;
		case UNLOCK:
			if (!mPW.equals(parsePW(pattern))) {
				mLockView.setDisplayMode(DisplayMode.Wrong);
				mLockView.invalidate();
				mLockView.setEnabled(false);
				mTitle.setText(getResources().getString(R.string.lock_pw_error));
				mHandler.sendEmptyMessageDelayed(2, 500);
			} else {
				finish();
			}
			
			break;

		default:
			break;
		}
	}
	/**
	 * NORMAL:第一次设置密码;
	 * LOCKED：再次设置密码;
	 * UNLOCK：需要解锁
	 * @author jrjin
	 * @time 2015-4-13 下午10:55:03
	 */
	public enum LockState {
		NORMAL,LOCKED,UNLOCK
	}
	
	private String parsePW(List<Cell> pattern) {
		StringBuilder builder = new StringBuilder();
		for (Cell cell : pattern) {
			builder.append(parsePW(cell));
		}
		return builder.toString();
	}
	
	private int parsePW(Cell pattern) {
		switch (pattern.column) {
		case 0:
			switch (pattern.row) {
			case 0:
				return 0;
			case 1:
				
				return 1;
				
			case 2:
				
				return 2;

			default:
				throw new IndexOutOfBoundsException(" row is to big > 2");
				
			}
			
		case 1:
			switch (pattern.row) {
			case 0:
				return 3;
			case 1:
				
				return 4;
				
			case 2:
				
				return 5;

			default:
				
				throw new IndexOutOfBoundsException(" row is to big > 2");
			}
		case 2:
			switch (pattern.row) {
			case 0:
				return 6;
			case 1:
				return 7;
				
			case 2:
				return 8;

			default:
				throw new IndexOutOfBoundsException(" row is to big > 2");
			}

		default:
			throw new IndexOutOfBoundsException(" column is to big > 2");
		}
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				mLockView.setEnabled(true);
				mLockView.clearPattern();
				break;
			case 1:
				mTitle.setText(getResources().getString(R.string.lock_pw_again));
				mLockView.setEnabled(true);
				mLockView.clearPattern();
				break;
			case 2:
				mTitle.setText("");
				mLockView.setEnabled(true);
				mLockView.clearPattern();
				break;

			default:
				break;
			}
		}

	};
}
