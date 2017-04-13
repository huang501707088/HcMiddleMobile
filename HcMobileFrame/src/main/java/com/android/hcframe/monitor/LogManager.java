package com.android.hcframe.monitor;

import android.content.Context;
import android.os.Process;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-2-19 14:17.
 */
public class LogManager implements IHttpResponse {

	private static final String TAG = "LogManager";
	
    /**
     * 缓存日志的最大量
     */
    private static final int MAX_COUNT = 30;

    /**
     * 操作应用
     */
    public static final int TYPE_APP = 1;

    /**
     * 操作应用模块
     */
    public static final int TYPE_MODULE = 2;

    /**
     * 与服务端进行交互操作
     */
    public static final int TYPE_SERVER = 3;

    /**
     * 日志上传到服务端的时间间隔
     */
    private static final int UPDATE_TIME = 10 * 60 * 1000;

    private volatile static LogManager LOG_MANAGER;

    /** 存放不完整的记录 */
    private Stack<OperationLogInfo> mLogStack;

    /** 存放完整的记录,不包括待上传的日志 */
    private List<OperationLogInfo> mLogInfos = new ArrayList<OperationLogInfo>();
    
    private Timer mTimer;

    private TimerTask mTask;
    /** 存放待上传的日志 */
    private List<List<OperationLogInfo>> mTemps = new ArrayList<List<OperationLogInfo>>();

    /** 是否可以上传日志 */
    private boolean mCanPost = true;
    
    private String mPostUrl;
    
    public static LogManager getInstance() {
    	if (LOG_MANAGER == null) {
    		synchronized (LogManager.class) {
    			HcLog.D(TAG + " #getInstance LOG_MANAGER ================================ "+LOG_MANAGER);
				if (LOG_MANAGER == null) {
					LOG_MANAGER = new LogManager();
				}
			}
    	}
    	
        return LOG_MANAGER;
    }

    private LogManager() {
    	
        mLogStack = new Stack<OperationLogInfo>();
        mTimer = new Timer();
        mTask = new LogTask();
        mTimer.schedule(mTask, 0, UPDATE_TIME);
        
    }

    /**
     * 添加新的操作日志,不是完整的记录,还需要更新
     * @see LogManager#updateLog(Context, boolean)
     * @param info 操作日志
     */
    public synchronized void addLog(OperationLogInfo info) {
        mLogStack.push(info);
        HcLog.D(TAG + "#addLog stack size = "+mLogStack.size());
    }

    /**
     * 添加新的完整的操作日志
     * @author jrjin
     * @time 2016-2-23 下午2:32:06
     * @param info 操作日志
     */
    public synchronized void addServerLog(OperationLogInfo info) {
    	mLogInfos.add(info);
    	HcLog.D(TAG + "#addServerLog size = "+mLogInfos.size());
    	if (mLogInfos.size() >= MAX_COUNT) {
    		List<OperationLogInfo> temp = new ArrayList<OperationLogInfo>(mLogInfos.subList(0, MAX_COUNT - 1));
            mTemps.add(temp);
            mLogInfos.removeAll(temp);
            if (mCanPost) {// 说明没有数据在上传
            	if (HcUtil.isNetWorkAvailable(HcApplication.getContext())) {
            		mCanPost = !mCanPost;
                	HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
            	}
            	
            }
        }
    }
    
    /**
     * 添加新的操作日志
     * @see #TYPE_APP
     * @see #TYPE_MODULE
     * @see #TYPE_SERVER
     * @param moduleId 应用模块编号,可以为null
     * @param type 操作类型 1:应用程序；2：应用模块；3：接口请求
     * @param context
     */
    public synchronized void addLog(String name, String moduleId, int type, Context context) {
        OperationLogInfo info = new OperationLogInfo();
        info.setType(type);
        if (moduleId == null) {
            moduleId = "";
        }
        info.setModuleId(moduleId);
        info.setStartTime("" + System.currentTimeMillis());
//		info.setStartTime(HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
		info.setVersion(HcConfig.getConfig().getAppVersion());
        info.setImei(HcUtil.getIMEI(context));
        info.setName(name);
        mLogStack.add(info);
    }

    /**
     * 更新操作日志
     * @param context
     * @param exit 是否退出应用
     */
    public synchronized void updateLog(Context context, boolean exit) {
        if (exit) { // 退出应用
        	if (mPostUrl != null) {
        		HcHttpRequest.getRequest().cancelRequest(mPostUrl);
        	}
        	if (!mLogStack.isEmpty()) {
        		OperationLogInfo info;
        		while (!mLogStack.isEmpty()) {
					info = mLogStack.pop();
					info.setEndTime("" + System.currentTimeMillis());
//					info.setEndTime(HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
					info.setAccount(SettingHelper.getAccount(context));
	                mLogInfos.add(info);
				}
        	}
        	
        	insertLogsToDatabase(context);
        } else {
        	if (!mLogStack.isEmpty()) {
        		OperationLogInfo info = mLogStack.pop();
        		HcLog.D(TAG + " updateLog name =" + info.getName() + " type = " + info.getType());
        		info.setEndTime("" + System.currentTimeMillis());
//				info.setEndTime(HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
				info.setAccount(SettingHelper.getAccount(context));
                mLogInfos.add(info);
                if (mLogInfos.size() >= MAX_COUNT) {
                	List<OperationLogInfo> temp = new ArrayList<OperationLogInfo>(mLogInfos.subList(0, MAX_COUNT - 1));
                    mTemps.add(temp);
                    mLogInfos.removeAll(temp);
                    if (mCanPost) {// 说明没有数据在上传
                    	if (HcUtil.isNetWorkAvailable(HcApplication.getContext())) {
                    		mCanPost = !mCanPost;
                        	HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
                    	}
                    	
                    }
                }
        	}
		}

    }

    /**
     * 把日志从数据库中转到临时缓存中
     * @param context
     */
    public synchronized void addLogsFromDatabase(Context context) {
        mLogInfos.addAll(LogOperatorDatabase.getOperationLogs(context));
    }

    private void insertLogsToDatabase(Context context) {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        List<OperationLogInfo> logInfos = new ArrayList<OperationLogInfo>();
        for (List<OperationLogInfo> logs : mTemps) {
			logInfos.addAll(logs);
			logs.clear();
		}
        logInfos.addAll(mLogInfos);
        mLogInfos.clear();
        HcLog.D(TAG + " #insertLogsToDatabase log size = "+logInfos.size());
        LogOperatorDatabase.insertLogs(context, logInfos);
    }

    private class LogTask extends TimerTask {

        /**
         * The task to run should be specified in the implementation of the {@code run()}
         * method.
         */
        @Override
        public void run() {
            // 日志上传到服务端
        	HcLog.D(TAG + " $LogTask #run it is start! thread nam = "+Thread.currentThread().getName() + " class name = "+LogManager.this
        			+ " pid ====="+Process.myPid());
        	postLogs();
        }
    }

    private synchronized void postLogs() {
    	if (mCanPost && mTemps.size() == 0 && mLogInfos.size() > 0
    			&& HcUtil.isNetWorkAvailable(HcApplication.getContext())) {
    		mCanPost = !mCanPost;
    		mTemps.add(new ArrayList<OperationLogInfo>(mLogInfos));
    		mLogInfos.clear();
    		HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
    	}
    }
    
	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " #notify  ===========================  request = "+request + " category = "+category);
		// 已经到主线程
		mPostUrl = null;
		switch (category) {
		case SUCCESS:
			if (data != null && data instanceof String) {
				HcLog.D(TAG + " #notify parse SUCCESS data = "+data);
				try {
					JSONObject object = new JSONObject((String) data);
					int code = object.getInt("code");
					if (code == 0) { // 成功
						mTemps.remove(0);
						// 重置倒计时,这里需要测试
						mTask.cancel();
						mTask = new LogTask();
						mTimer.schedule(mTask, UPDATE_TIME, UPDATE_TIME);
						if (mTemps.isEmpty()) {
							mCanPost = true;
						} else {
							if (HcUtil.isNetWorkAvailable(HcApplication.getContext())) {
								mCanPost = false;
								HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
							
							} else {
								mCanPost = true;
							}
							
						}
					} else {
						mCanPost = true;
					}
				} catch (Exception e) {
					// TODO: handle exception
					mCanPost = true;
					HcLog.D(TAG + " #notify parse Error e = "+e);
				}
			}
			
			
			
			break;
		case SESSION_TIMEOUT:
		case NETWORK_ERROR:
			/**
			if (mTemps.isEmpty()) {
				mCanPost = true;
			} else {
				if (HcUtil.isNetWorkAvailable(HcApplication.getContext())) {
					mCanPost = false;
					HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
				
				} else {
					mCanPost = true;
				}
			}
			break;
			*/
		default: // 失败重新发送?
			mCanPost = true;
//			HcHttpRequest.getRequest().sendLogsCommand(mTemps.get(0), this);
			break;
		}
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		mPostUrl = md5Url;
	}
    
    
}
