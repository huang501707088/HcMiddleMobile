/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-9-22 下午2:43:23
 */
package com.android.hcframe.push;

import java.util.List;

import android.content.Context;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.sys.SystemMessage;
import com.baidu.android.pushservice.PushMessageReceiver;

/**
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下： 0 - Success 10001 - Network Problem 10101 Integrate
 * Check Error 30600 - Internal Server Error 30601 - Method Not Allowed 30602 -
 * Request Params Not Valid 30603 - Authentication Failed 30604 - Quota Use Up
 * Payment Required 30605 -Data Required Not Found 30606 - Request Time Expires
 * Timeout 30607 - Channel Token Timeout 30608 - Bind Relation Not Found 30609 -
 * Bind Number Too Many
 */
public class PushReceiver extends PushMessageReceiver implements HcObserver {

	private static final String TAG = "PushReceiver";

	/**
	 * 没有使用到，先注释掉 private PushManager pushMgr = new PushManager();
	 */

	/**
	 * 调用PushManager.startWork后，sdk将对push
	 * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
	 * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
	 * 
	 * @param context
	 *            BroadcastReceiver的执行Context
	 * @param errorCode
	 *            绑定接口返回值，0 - 成功
	 * @param appid
	 *            应用id。errorCode非0时为null
	 * @param userId
	 *            应用user id。errorCode非0时为null
	 * @param channelId
	 *            应用channel id。errorCode非0时为null
	 * @param requestId
	 *            向服务端发起的请求id。在追查问题时有用；
	 * @return none
	 */
	@Override
	public void onBind(Context context, int errorCode, String appid,
			String userId, String channelId, String requestId) {
		String responseString = "onBind errorCode=" + errorCode + " appid="
				+ appid + " userId=" + userId + " channelId=" + channelId
				+ " requestId=" + requestId;
		HcLog.D(TAG + " #" + responseString);
		final String ci = channelId;
		final Context ct = context;
		if (errorCode == 0) {

			/**
			 * @author jrjin
			 * @date 2015-12-7 下午3:20:23
			 */
			SettingHelper.setChannelId(ct, ci);
			/**
			 * 这里先判断是否有去服务端注册过,因为为了增加消息的到达率,每次应用启动的时候都去百度推送那边去注册下
			 */
			if (!HcPushManager.getInstance().deviceBinded(context))
				HcPushManager.getInstance().registerDevice(context, true);

		} else { // 注册失败
			HcPushManager.getInstance().registerFailed();
		}
	}

	/**
	 * delTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
	 * @param sucessTags
	 *            成功删除的tag
	 * @param failTags
	 *            删除失败的tag
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onDelTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onDelTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;
		HcLog.D(TAG + " #" + responseString);
	}

	/**
	 * listTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示列举tag成功；非0表示失败。
	 * @param tags
	 *            当前应用设置的所有tag。
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onListTags(Context context, int errorCode, List<String> tags,
			String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onListTags errorCode=" + errorCode + " tags="
				+ tags;
		HcLog.D(TAG + "#" + responseString);
	}

	/**
	 * 接收透传消息的函数。
	 * 
	 * @param context
	 *            上下文
	 * @param message
	 *            推送的消息
	 * @param customContentString
	 *            自定义内容,为空或者json字符串
	 */
	@Override
	public void onMessage(Context context, String message,
			String customContentString) {
		// TODO Auto-generated method stub
		String messageString = "onMessage 透传消息 message=\"" + message
				+ "\" customContentString=" + customContentString;
		HcLog.D(TAG + " #" + messageString);
	}

	/**
	 * 接收通知到达的函数。
	 * 
	 * @param context
	 *            上下文
	 * @param title
	 *            推送的通知的标题
	 * @param description
	 *            推送的通知的描述
	 * @param customContentString
	 *            自定义内容，为空或者json字符串
	 */
	@Override
	public void onNotificationArrived(Context context, String title,
			String description, String customContentString) {
		// TODO Auto-generated method stub
		String notifyString = "onNotificationArrived  title=\"" + title
				+ "\" description=\"" + description + "\" customContent="
				+ customContentString;
		HcLog.D(TAG + " # " + notifyString);
		/**
		 * @author jrjin
		 * @date 2015-12-4 上午9:10:02 把数据保存到数据库
		 */
		saveMessage(context, title, description, customContentString, false);
		
		// 判断是否需要清除缓存
		PushInfo info = new PushInfo(customContentString);
		info = null;
	}

	/**
	 * 接收通知点击的函数。
	 * 
	 * @param context
	 *            上下文
	 * @param title
	 *            推送的通知的标题
	 * @param description
	 *            推送的通知的描述
	 * @param customContentString
	 *            自定义内容，为空或者json字符串
	 */
	@Override
	public void onNotificationClicked(Context context, String title,
			String description, String customContentString) {
		// TODO Auto-generated method stub
		String notifyString = "onNotificationClicked 通知点击 title=\"" + title
				+ "\" description=\"" + description + "\" customContent="
				+ customContentString;
		HcLog.D(TAG + " # " + notifyString + " context = "+context);
		/**
		 * @date 2015-12-4 上午9:20:07
		 */
//		saveMessage(context, title, description, customContentString, true);

		PushInfo info = new PushInfo(customContentString);
		HcPushManager.getInstance().setPushInfo(info);
		info.startActivity(context);
	}

	/**
	 * setTags() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
	 * @param sucessTags
	 *            设置成功的tag
	 * @param failTags
	 *            设置失败的tag
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onSetTags(Context context, int errorCode,
			List<String> sucessTags, List<String> failTags, String requestId) {
		// TODO Auto-generated method stub
		String responseString = "onSetTags errorCode=" + errorCode
				+ " sucessTags=" + sucessTags + " failTags=" + failTags
				+ " requestId=" + requestId;
		HcLog.D(TAG + " #" + responseString);
	}

	/**
	 * PushManager.stopWork() 的回调函数。
	 * 
	 * @param context
	 *            上下文
	 * @param errorCode
	 *            错误码。0表示从云推送解绑定成功；非0表示失败。
	 * @param requestId
	 *            分配给对云推送的请求的id
	 */
	@Override
	public void onUnbind(Context context, int errorCode, String requestId) {
		// TODO Auto-generated method stub
		HcLog.D(TAG + " #onUnbind errorCode = " + errorCode + " requsetId = "
				+ requestId);
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {

	}

	/**
	 * 把接收到的消息保存到数据库中
	 * 
	 * @author jrjin
	 * @time 2015-12-4 上午9:17:19
	 * @param context
	 * @param title
	 *            消息标题
	 * @param content
	 *            消息内容
	 * @param json
	 *            自定义消息
	 * @param readed
	 *            是否已读
	 */
	private void saveMessage(Context context, String title, String content,
			String json, boolean readed) {
		SystemMessage message = new SystemMessage();
		PushInfo info = new PushInfo(json);
		message.setAppId(info.getAppId());
		message.setContentId(info.getContent());
		message.setDate("" + System.currentTimeMillis());
		message.setReaded(readed);
		message.setType(info.getType());
		message.setContent(content);
		message.setTitle(title);
		if (readed) {
			int id = OperateDatabase.updateSysMessage(message, context);
			HcLog.D(TAG + " saveMessage updateId = " + id);
		} else {
			OperateDatabase.insertSysMessage(message, context);
		}

		// 通知更新
		HcPushManager.getInstance().notifyUpdateMessage();
		HcLog.D(TAG + " saveMessage end!");
	}

}
