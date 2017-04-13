package com.android.hcframe.im;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;

public class HcAudioRecorder {

	private static final String TAG = "HcAudioRecorder";
	private AudioRecord mRecord;
	private short[] mBuffer;
	
	private boolean isRecording = false;
	
	private String mTempFile; 

	private static int[] mSampleRates = new int[] { 16000, 8000, 11025, 22050, 44100 };
	
	private int mRate; // 默认采样率，单位Hz.44100Hz是当前唯一能保证在所有设备上工作的采样率，在一些设备上还有22050, 16000或11025
	private int mFormat; // 音频数据保证支持此格式。 请见ENCODING_PCM_16BIT 和ENCODING_PCM_8BIT。
	private int mChannel; // 描述音频通道设置。 请见CHANNEL_IN_MONO 和 CHANNEL_IN_STEREO。 CHANNEL_IN_MONO保证能在所有设备上工作

	private static final String IM_DIR = "im";

	public HcAudioRecorder(String fileDir, String fileName) {
		File file = new File(HcApplication.getAppDownloadPath(), IM_DIR);
		if (!file.exists())
			file.mkdir();
		file = new File(file.getAbsolutePath(), fileDir);
		if (!file.exists())
			file.mkdir();
		mTempFile = file.getAbsolutePath() + "/" + fileName + "-" + System.currentTimeMillis() + ".m4a";
		initAudio();
		HcLog.D(TAG + "#HcAudioRecorder fileDir = "+fileDir + " fileName = "+fileName);
	}

	private AudioRecord findAudioRecord() {
	    for (int rate : mSampleRates) {
	        for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT }) {
	            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
	                try {
	                    HcLog.D(TAG + "#findAudioRecord Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
	                            + channelConfig);
	                    int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
						HcLog.D(TAG + "#findAudioRecord buffer size = "+bufferSize);
	         
	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

	                        if (recorder != null) {
								HcLog.D(TAG + " #findAudioRecord record = "+recorder + " record state = "+recorder.getState());
							}
							if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
	                        	mBuffer = new short[bufferSize];
	                        	mRate = rate;
	                        	mFormat = audioFormat;
	                        	mChannel = channelConfig;
	                        	return recorder;
	                        }
	                            
	                    }
	                } catch (Exception e) {
						HcLog.D(TAG + "#findAudioRecord rate Exception, keep trying. e = "+e);
	                }
	            }
	        }
	    }
	    return null;
	}
	
	private void startBufferedWrite(final File file) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
					while (isRecording) {
						int readSize = mRecord.read(mBuffer, 0, mBuffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(mBuffer[i]);
						}

					}
					HcLog.D(TAG + "#startBufferedWrite it is recording end!");
				} catch (IOException e) {
					HcLog.D(TAG + "#startBufferedWrite recording error e = "+e);
					
				} finally {
					isRecording = false;
					if (output != null) {
						try {
							output.flush();
						} catch (IOException e) {
							
						} finally {
							try {
								output.close();
							} catch (IOException e) {
								
							}
						}
					}
				}
			}
		}).start();
	}
	
	/**
	 * 初始化音频源
	 */
	private void initAudio() {
		mRecord = findAudioRecord();		
		HcLog.D(TAG + " #initAudio mRecord = "+mRecord);
	}
	
	public void startRecord() {
		if (mRecord == null) {
			throw new NullPointerException("startRecord AudioRecord 没有初始化！");
		}
		if (isRecording) {
			isRecording = false;
			if (mRecord != null && mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				HcLog.D(TAG + "#startRecord in startRecord before stop state = "+mRecord.getState());
				mRecord.stop();
			}
		} else {
			isRecording = true;
			if (mRecord != null && mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
				HcLog.D(TAG + "#startRecord in startRecord before start recording state = "+mRecord.getState());
				mRecord.startRecording();
				File temp = new File(mTempFile);
				if (temp.exists())
					temp.delete();
				startBufferedWrite(new File(mTempFile));
			}
		}
	}

	/**
	 * 停止录音的时候开始格式转化
	 * <p>转化完会有一个回调接口</p>
	 */
	public void stopRecord() {
		if (mRecord == null) {
			throw new NullPointerException("stopRecord AudioRecord 没有初始化！");
		}
		if (isRecording) {
			isRecording = false;
			if (mRecord != null) {
				HcLog.D(TAG + "#startRecord in stopRecord before stop state = "+mRecord.getState());
				mRecord.stop();
			}
			HcLog.D(TAG + "#stopRecord before encode time = "+System.currentTimeMillis());

		}
	}
	
	/**
	 * 退出Activity的时候才被调用
	 */
	public void onDestory() {
		HcLog.D(TAG + "#onDestory it is onDestory! ");
		
		if (mRecord != null) {
			mRecord.release(); // 封装的时候已经调用stop了。
			mRecord = null;
		}

	}
	
	/**
	 * 判断是否可以转MP3
	 * @return
	 */
	public boolean isCanRecord() {
		return mRecord != null;
	}

	/**
	 * 取消录音
	 * <p>转化完会有一个回调接口</p>
	 */
	public void canelRecord() {
		if (mRecord == null) {
			throw new NullPointerException("stopRecord AudioRecord 没有初始化！");
		}
		if (isRecording) {
			isRecording = false;
			if (mRecord != null) {
				HcLog.D(TAG + "#startRecord in stopRecord before stop state = "+mRecord.getState());
				mRecord.stop();
			}
			File file=new File(mTempFile);
			if(!file.exists()) {
				return;
			}
			HcLog.D(TAG + "#stopRecord before encode time = "+System.currentTimeMillis());

		}
	}

}
