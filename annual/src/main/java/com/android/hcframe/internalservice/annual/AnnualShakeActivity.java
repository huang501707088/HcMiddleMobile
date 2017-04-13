/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-11 下午5:38:55
*/
package com.android.hcframe.internalservice.annual;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.PopTextView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONObject;

public class AnnualShakeActivity extends HcBaseActivity implements IHttpResponse, 
	SensorEventListener {

	private static final String TAG = "AnnualShakeActivity";
	
	private RelativeLayout mParent;
	/******************* 等待摇奖 *************/
	private RelativeLayout mWaitingShakeParent;
	/******************* 摇奖 *************/
	private LinearLayout mShakeParent;
	private View mTop;
	private View mBottom;
	/******************* 等待开奖 *************/
	private RelativeLayout mWaitingPrizeParent;
	private TextView mPrizeId;
	private TextView mRefreshBtn;
	
	/******************* 开奖 *************/
	private LinearLayout mPrizedParent;
	private TextView mPrizedId;
	private PopTextView mPopTextView; // 没有使用
	
	private ShakeStatus mStatus = ShakeStatus.NONE;
	
	private SparseArray<String> mMd5Uri = new SparseArray<String>();
	
	private String mAnnualId;
	
	private AudioManager mAudioManager;
	/** 摇奖编号 */
	private String mPrizeCode;
	
	private boolean mPrized = false;
	
	private ObjectAnimator mMiddleToUp;
	private ObjectAnimator mMiddleToDown;
	private ObjectAnimator mTopToMiddle;
	private ObjectAnimator mBottomToMiddle;
	
	private SensorManager mSensorManager;
	
	/** 是否在摇一摇 */
	private boolean mShaking = false;
	
	private SensorHandler mHandler;
	
	private MediaPlayer mPlayer;
	
	private static final String MUSIC_SHAKING = "shaking.mp3";//"file:///android_asset/shaking.mp3";
	
	private static final String MUSIC_SHAKED = "shaked.mp3";//"file:///android_asset/shaked.mp3";
	
	private AssetManager mAssetManager;
	/** 当前的摇奖轮次 */
	private int mRount = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAnnualId = parseAnnualId(SettingHelper.getAnnualInfo(this));
		if (TextUtils.isEmpty(mAnnualId)) { // 不应该出现这种情况,会在AnnualHomeView里面做控制
			finish();
			return;
		}
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mHandler = new SensorHandler(1000);
		mAssetManager = getAssets();
		mPlayer = new MediaPlayer();
		setContentView(R.layout.activity_annual_shake);
		initViews();
		initAnimator();
		updateViews(mStatus);
		
	}

	private void initAnimator() {
		float topTranslationY = mTop.getTranslationY();
		float bottomTranslationY = mBottom.getTranslationY();
		mMiddleToUp = ObjectAnimator.ofFloat(mTop, "translationY", topTranslationY, topTranslationY - 200f);
		mMiddleToUp.setDuration(200);
		
		mMiddleToDown = ObjectAnimator.ofFloat(mBottom, "translationY", bottomTranslationY, bottomTranslationY + 200f);
		mMiddleToUp.setDuration(200);
		
		mTopToMiddle = ObjectAnimator.ofFloat(mTop, "translationY", topTranslationY - 200f, topTranslationY);
		mTopToMiddle.setDuration(200);
		
		mBottomToMiddle = ObjectAnimator.ofFloat(mBottom, "translationY", bottomTranslationY + 200, bottomTranslationY);
		mBottomToMiddle.setDuration(200);
		
		mTopToMiddle.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				if (mPlayer != null) {
					if (mPlayer.isPlaying()) {
						mPlayer.stop();
					}
				}
				if (HcUtil.isNetWorkAvailable(AnnualShakeActivity.this)) {
					HcHttpRequest.getRequest().sendAnnualProgramShakeCommand(mAnnualId, mRount, AnnualShakeActivity.this);
					mPopTextView.setVisibility(View.VISIBLE);
				} 
					
			}

		});
	}
	
	private void initViews() {
		mParent = (RelativeLayout) findViewById(R.id.annual_shake_parent);
		
		mWaitingShakeParent = (RelativeLayout) findViewById(R.id.annual_shake_status_waiting_parent);
		
		mShakeParent = (LinearLayout) findViewById(R.id.annual_shake_status_shake_parent);
		mTop = findViewById(R.id.annual_shake_top);
		mBottom = findViewById(R.id.annual_shake_bottom);
		
		mWaitingPrizeParent = (RelativeLayout) findViewById(R.id.annual_shake_status_waiting_prize);
		mPrizeId = (TextView) findViewById(R.id.annual_shake_prize_id);
		mRefreshBtn = (TextView) findViewById(R.id.annual_shake_refresh_prize_btn);
		
		mPrizedParent = (LinearLayout) findViewById(R.id.annual_shake_prize_parent);
		mPrizedId = (TextView) findViewById(R.id.annual_shake_prize);
		mPopTextView = (PopTextView) findViewById(R.id.annual_shake_prize_list);
		
		mWaitingShakeParent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (HcUtil.isNetWorkAvailable(AnnualShakeActivity.this)) {
					HcDialog.showProgressDialog(AnnualShakeActivity.this, R.string.dialog_title_refresh_data);
					HcHttpRequest.getRequest().sendAnnualProgramShakeStatusCommand(mAnnualId, AnnualShakeActivity.this);
				}
				
			}
		});
		
		mWaitingPrizeParent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (HcUtil.isNetWorkAvailable(AnnualShakeActivity.this)) {
					HcDialog.showProgressDialog(AnnualShakeActivity.this, "查看开奖结果");
					HcHttpRequest.getRequest().sendAnnualProgramShakeStatusCommand(mAnnualId, AnnualShakeActivity.this);
				}
			}
		});
	}
	
	public enum ShakeStatus {
		/**
		 * 初始状态
		 */
		NONE,
		/**
		 * 等待摇奖
		 */
		WAITING_SHAKE,
		/**
		 * 可摇奖
		 */
		SHAKE,
		/**
		 * 等待开奖
		 */
		WAITING_PRIZE,
		/**
		 * 已开奖
		 */
		PRIZED
	}
	
	private void registerListener() {
		if (mSensorManager != null) {
			// 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率 
			mSensorManager.registerListener(this,  
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					//还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，  
					//根据不同应用，需要的反应速率不同，具体根据实际情况设定
					SensorManager.SENSOR_DELAY_NORMAL);
			
		}
	}
	
	private void unRegisterListener() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(this);
		}
	}
	
	private void updateViews(ShakeStatus status) {
		switch (status) {
		case WAITING_SHAKE:
		case NONE:
			mParent.setBackgroundResource(R.drawable.annual_shake_home_bg);
			if (mPopTextView.getVisibility() != View.GONE) {
				mPopTextView.setVisibility(View.GONE);
			}
			if (mWaitingShakeParent.getVisibility() != View.VISIBLE) {
				mWaitingShakeParent.setVisibility(View.VISIBLE);
			}
			if (mShakeParent.getVisibility() != View.GONE) {
				mShakeParent.setVisibility(View.GONE);
			}
			if (mWaitingPrizeParent.getVisibility() != View.GONE) {
				mWaitingPrizeParent.setVisibility(View.GONE);
			}
			if (mPrizedParent.getVisibility() != View.GONE) {
				mPrizedParent.setVisibility(View.GONE);
			}
			unRegisterListener();
			break;
		case SHAKE:
			mParent.setBackgroundColor(Color.parseColor("#16181b"));
//			mParent.setBackgroundResource(R.drawable.annual_shake_home_bg);
			if (mPopTextView.getVisibility() != View.GONE) {
				mPopTextView.setVisibility(View.GONE);
			}
			if (mWaitingShakeParent.getVisibility() != View.GONE) {
				mWaitingShakeParent.setVisibility(View.GONE);
			}
			if (mShakeParent.getVisibility() != View.VISIBLE) {
				mShakeParent.setVisibility(View.VISIBLE);
			}
			if (mWaitingPrizeParent.getVisibility() != View.GONE) {
				mWaitingPrizeParent.setVisibility(View.GONE);
			}
			if (mPrizedParent.getVisibility() != View.GONE) {
				mPrizedParent.setVisibility(View.GONE);
			}
			registerListener();
			break;
		case WAITING_PRIZE:
			mParent.setBackgroundResource(R.drawable.annual_shake_home_bg);
			if (mPopTextView.getVisibility() != View.GONE) {
				mPopTextView.setVisibility(View.GONE);
			}
			if (mWaitingShakeParent.getVisibility() != View.GONE) {
				mWaitingShakeParent.setVisibility(View.GONE);
			}
			if (mShakeParent.getVisibility() != View.GONE) {
				mShakeParent.setVisibility(View.GONE);
			}
			if (mWaitingPrizeParent.getVisibility() != View.VISIBLE) {
				mWaitingPrizeParent.setVisibility(View.VISIBLE);
			}
			if (mPrizedParent.getVisibility() != View.GONE) {
				mPrizedParent.setVisibility(View.GONE);
			}
			
			mPrizeId.setText(mPrizeCode);
			unRegisterListener();
			break;
		case PRIZED:
			mParent.setBackgroundResource(R.drawable.annual_shake_prize_bg);
			if (mPopTextView.getVisibility() != View.GONE) {
				mPopTextView.setVisibility(View.GONE);
			}
			if (mWaitingShakeParent.getVisibility() != View.GONE) {
				mWaitingShakeParent.setVisibility(View.GONE);
			}
			if (mShakeParent.getVisibility() != View.GONE) {
				mShakeParent.setVisibility(View.GONE);
			}
			if (mWaitingPrizeParent.getVisibility() != View.GONE) {
				mWaitingPrizeParent.setVisibility(View.GONE);
			}
			if (mPrizedParent.getVisibility() != View.VISIBLE) {
				mPrizedParent.setVisibility(View.VISIBLE);
			}
			unRegisterListener();
			// 需要根据中奖情况设置背景图片
			if (mPrized) {
				mPrizedParent.setBackgroundResource(R.drawable.annual_shake_prized_icon);
				mPrizedId.setText("奖券编号：" +mPrizeCode);
			} else {
				mPrizedParent.setBackgroundResource(R.drawable.annual_shake_unprized_icon);
				mPrizedId.setText("");
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		mMd5Uri.delete(request.ordinal());
		HcDialog.deleteProgressDialog();
		switch (request) {
		case ANNUAL_SHAKE:
			if (mPopTextView.getVisibility() != View.GONE) {
				mPopTextView.setVisibility(View.GONE);
			}
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof String) {
					HcLog.D(TAG + " #notify parse SUCCESS data = "+data);
					try {
						JSONObject object = new JSONObject((String) data);
						int code = object.getInt("code");
						if (code == 0) {
							// 更新数据
							object = object.getJSONObject("body");
							if (HcUtil.hasValue(object, "award_code")) {
								play(false);
								mPrizeCode = object.getString("award_code");
								mStatus = ShakeStatus.WAITING_PRIZE;
								updateViews(mStatus);
							} else {
								registerListener();
							}
						} else if (code == HcHttpRequest.REQUEST_ANNUAL_SHAKE_ITMEOUT) {// 摇奖已经超时
							mStatus = ShakeStatus.PRIZED;
							mPrized = false;
//							if (HcUtil.hasValue(object, "msg")) {
//								String msg = object.getString("msg");
//								HcUtil.showToast(this, msg);
//							}
							HcUtil.showToast(this, "已过摇奖时间！");
							updateViews(mStatus);
						} else {
							if (HcUtil.hasValue(object, "msg")) {
								String msg = object.getString("msg");
								/**
								 * czx
								 * 2016.4.13
								 */
								if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == code
										|| HcHttpRequest.REQUEST_TOKEN_FAILED == code
										) {
									if (HcUtil.hasValue(object, "body")) {
										HcUtil.reLogining(object.getJSONObject("body").toString(), this, msg);
									}
								} else {
									HcUtil.showToast(this, msg);
								}
							}
							registerListener(); // 这里需要考虑下,是否注释掉。
						}
					} catch (Exception e) {
						// TODO: handle exception
						HcLog.D(TAG + " #notify parse Error e = "+e);
						HcUtil.toastDataError(this);
						registerListener();
					}
				}
				break;
			case NETWORK_ERROR:
			case SESSION_TIMEOUT:
				HcUtil.toastNetworkError(this);
				registerListener();
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				registerListener();
				break;

			default:
				break;
			}
			break;
		case ANNUAL_SHAKE_STATUS:
			switch (category) {
			case SUCCESS:
				if (data != null && data instanceof String) {
					HcLog.D(TAG + " #notify parse SUCCESS data = "+data);
					try {
						JSONObject object = new JSONObject((String) data);
						int code = object.getInt("code");
						if (code == 0) {
							// 更新数据
							object = object.getJSONObject("body");
							if (HcUtil.hasValue(object, "status")) {
								int status = object.getInt("status");
								switch (status) {
								case 1:
									if (mStatus == ShakeStatus.WAITING_SHAKE) {
										HcUtil.showToast(this, "请耐心等待摇奖！");
									}
									mStatus = ShakeStatus.WAITING_SHAKE;
									break;
								case 2:
									mStatus = ShakeStatus.SHAKE;
									if (HcUtil.hasValue(object, "round_time")) {
										mRount = object.getInt("round_time");
									}
									break;
								case 3:
									if (mStatus == ShakeStatus.WAITING_PRIZE) {
										HcUtil.showToast(this, "请耐心等待开奖！");
									}
									mStatus = ShakeStatus.WAITING_PRIZE;
									if (HcUtil.hasValue(object, "result")) {
										mPrizeCode = object.getString("result");
									}
									break;
								case 4:
									mStatus = ShakeStatus.PRIZED;
									if (HcUtil.hasValue(object, "result")) {
										String prizeCode = object.getString("result");
										if ("N".equals(prizeCode) || TextUtils.isEmpty(prizeCode)) {
											mPrized = false;
										} else {
											mPrizeCode = prizeCode;
											mPrized = true;
										}
										
									}
									break;

								default:
									break;
								}
								
								updateViews(mStatus);
							}
						} else {
							if (HcUtil.hasValue(object, "msg")) {
								String msg = object.getString("msg");
								HcUtil.showToast(this, msg);
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						HcLog.D(TAG + " #notify parse Error e = "+e);
						HcUtil.toastDataError(this);
					}
				}
				break;
			case NETWORK_ERROR:
			case SESSION_TIMEOUT:
				HcUtil.toastNetworkError(this);
				break;
			case SYSTEM_ERROR:
				HcUtil.toastSystemError(this, data);
				break;

			default:
				break;
			}
			break;

		default:
			break;
		}
		
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		mMd5Uri.append(request.ordinal(), md5Url);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 如果是已经中奖的人,这里就直接显示中奖
		// 如果为中奖,则去服务端获取状态
		if (HcUtil.isNetWorkAvailable(this)) {
			HcDialog.showProgressDialog(this, R.string.dialog_title_refresh_data);
			HcHttpRequest.getRequest().sendAnnualProgramShakeStatusCommand(mAnnualId, this);
		}
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HcDialog.deleteProgressDialog();
		int size = mMd5Uri.size();
		for (int i = 0; i < size; i++) {
			HcHttpRequest.getRequest().cancelRequest(mMd5Uri.get(i));
		}
		mMd5Uri.clear();
		unRegisterListener();
		if (mSensorManager != null) {
			mSensorManager = null;
		}
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		super.onDestroy();
	}
	
	private String parseAnnualId(String data) {
		try {
			JSONObject object = new JSONObject(data);
			if (HcUtil.hasValue(object, "annual_id")) {
				return object.getString("annual_id");
			}
		} catch (Exception e) {
			// TODO: handle exception
			HcLog.D(TAG + " #parseAnnualId data = "+data + " e = "+e);
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (keyCode == KeyEvent.KEYCODE_BACK
			&& event.getAction() == KeyEvent.ACTION_DOWN) {
				HcAppState.getInstance().removeActivity(this);
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
	        mAudioManager.adjustStreamVolume(
	            AudioManager.STREAM_MUSIC,
	            AudioManager.ADJUST_RAISE,
	            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
	        return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	        mAudioManager.adjustStreamVolume(
	            AudioManager.STREAM_MUSIC,
	            AudioManager.ADJUST_LOWER,
	            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
	        return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		int sensorType = event.sensor.getType();  
		  
	    //values[0]:X轴方向的重力加速度，向右为正，
		//values[1]：Y轴方向的重力加速度，向前为正，
		//values[2]：Z轴方向的重力加速度，向上为正,
	    float[] values = event.values;
//	    HcLog.D(TAG + "#onSensorChanged x = "+values[0] + " y = "+values[1] + " z = "+values[2]);
	    if(sensorType == Sensor.TYPE_ACCELEROMETER) {
	    	
	    	/*因为一般正常情况下，任意轴数值最大就在9.8~10之间，只有在你突然摇动手机 
	    	*的时候，瞬时加速度才会突然增大或减少。 
    	    *所以，经过实际测试，只需监听任一轴的加速度大于14的时候，改变你需要的设置 
    	    *就OK了~~~ 
    	    */ 
	    	if((Math.abs(values[0]) > 14|| Math.abs(values[1]) > 14 || Math.abs(values[2]) > 14)) {
	    		if (mStatus == ShakeStatus.SHAKE && !mShaking) {
	    			mShaking = !mShaking;
	    			// 启动音乐
					play(true);
					
	    			// 启动动画
	    			mMiddleToUp.start();
					mMiddleToDown.start();
					
	    		}
	    		if (mShaking && mHandler != null) {
	    			mHandler.pause();
	    			mHandler.resume();
	    		}
	    		
	    	}
	    	
	    } 
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		//当传感器精度改变时回调该方法，Do nothing. 
	}
	
	private class SensorHandler extends Handler {

		private final int mWhat;
		
		public SensorHandler(int what) {
			mWhat = what;
		}
		
		void resume() {
			if (!hasMessages(mWhat)) {
				sendEmptyMessageDelayed(mWhat, 1000);
			}
		}
		
		void pause() {
			removeMessages(mWhat);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (mStatus == ShakeStatus.SHAKE && mShaking) {
				unRegisterListener();
				mShaking = !mShaking;
				// 结束音乐
//				play(false); 音乐放在提交成功播放
				// 结束摇一摇
				mTopToMiddle.start();
				mBottomToMiddle.start();
				
				
				
				// 发送服务端，放到动画播放结束执行
//				HcDialog.showProgressDialog(AnnualShakeActivity.this, "正在获取奖券码...");
//				HcHttpRequest.getRequest().sendAnnualProgramShakeCommand(mAnnualId, mRount, AnnualShakeActivity.this);
			}
		}
		
	}
	
	private void play(boolean start) {
		if (mPlayer != null) {
			try {
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
				mPlayer.reset();
				AssetFileDescriptor fileDescriptor = null;
				if (start) {
					fileDescriptor = mAssetManager.openFd(MUSIC_SHAKING);
				} else {
					fileDescriptor = mAssetManager.openFd(MUSIC_SHAKED);
				}
				mPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());
				mPlayer.prepare();
				if (start) {
					mPlayer.setLooping(true);
				} else {
					mPlayer.setLooping(false);
				}
				
				mPlayer.start();
			} catch (Exception e) {
				// TODO: handle exception
				HcLog.D(TAG + "#play player error = "+e + " start = "+start);
			}
			
			
		}
	}
}
