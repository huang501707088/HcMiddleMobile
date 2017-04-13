package com.android.hcframe.im;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcLog;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.im.data.IMSystemMessageAdapter;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.sys.SysMassageActivity;
import com.android.hcframe.view.dialog.ListDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-23 14:32.
 */

public class IMSystemMessageView extends AbstractPage implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener {

    private static final String TAG = "IMSystemMessageView";

    private final String mAppId;

    private PullToRefreshListView mListView;

    private List<AppMessageInfo> mInfos = new ArrayList<AppMessageInfo>();

    private IMSystemMessageAdapter mAdapter;

    public IMSystemMessageView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
    }

    @Override
    public void initialized() {
        if (mAdapter == null) {
            mInfos.addAll(ChatOperatorDatabase.getSystemMessages(mContext));
            mAdapter = new IMSystemMessageAdapter(mContext, mInfos);
            mListView.setAdapter(mAdapter);

            HcPushManager.getInstance().addObserver(this);
        }
    }

    @Override
    public void setContentView() {
        if (mView == null) {
            mView = mInflater.inflate(R.layout.im_system_message_home_layout, null);
            mListView = (PullToRefreshListView) mView.findViewById(R.id.im_system_message_listview);

            mListView.setMode(PullToRefreshBase.Mode.DISABLED);

            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(this);

        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void update(Observable observable, Object data) {
        if (mAdapter != null) {
            mInfos.clear();
            mInfos.addAll(ChatOperatorDatabase.getSystemMessages(mContext));
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppMessageInfo info = (AppMessageInfo) parent.getItemAtPosition(position);
        int count = info.getCount();
        String appId = info.getId();
        if (count != 0) {
            info.setCount(0);
            ChatOperatorDatabase.updateAppMessage(mContext, info);
            mAdapter.notifyDataSetChanged();
            info = ChatOperatorDatabase.getAppMessageInfo(mContext, AppMessageInfo.SYSTEM_MESSAGE_ID);
            int allCount = info.getCount() - count;
            info.setCount(allCount < 0 ? 0 : allCount);
            ChatOperatorDatabase.updateAppMessage(mContext, info);
            BadgeInfo badgeInfo = BadgeCache.getInstance().getBadgeInfo(mAppId, mAppId + "_IM");
            if (badgeInfo != null)
                badgeInfo.updateCount(getAllCounts());

        }
        Intent intent = new Intent(mContext, SysMassageActivity.class);
        intent.putExtra("appId", appId);
        mContext.startActivity(intent);
    }

    @Override
    public void onDestory() {
        HcPushManager.getInstance().deleteObserver(this);
        super.onDestory();
    }

    private int getAllCounts() {
        int count = 0;
        List<AppMessageInfo> infos = ChatOperatorDatabase.getAppMessages(mContext);
        for (AppMessageInfo info : infos) {
            count += info.getCount();
        }
        infos.clear();
        return count;
    }

    private int mDeletePosition = -1;

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mDeletePosition = position - 1;
        HcLog.D(TAG + " #onItemLongClick mDeletePosition = "+mDeletePosition);
        ListDialog.showListDialog(mContext, new String[] {"删除该聊天"}, this);
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDeletePosition > -1) {
            int position = mDeletePosition;
            mDeletePosition = -1;
            AppMessageInfo info = mInfos.remove(position);
            int count = info.getCount();
            if (info.getType() == 1) { // 系统模块消息
                // 删除该系统消息记录和该应用模块消息推送记录
                ChatOperatorDatabase.deleteAppMessage(mContext, info.getId());
                OperateDatabase.deleteSystemMessage(mContext, info.getId());

                // 获取其他模块的列表
                String userId = SettingHelper.getUserId(mContext);
                String where = HcDatabase.AppMessage.USER_ID + "=" + "'" + userId + "'" + " AND " + HcDatabase.AppMessage.MESSAGE_TYPE + "=1";
                List<AppMessageInfo> infos = ChatOperatorDatabase.getAppMessages(mContext, where, HcDatabase.AppMessage.MESSAGE_DATE + " DESC");
                if (infos.isEmpty()) {
                    ChatOperatorDatabase.deleteAppMessage(mContext, AppMessageInfo.SYSTEM_MESSAGE_ID);
                } else {
                    // 变更数据
                    info = infos.get(0);
                    String title = info.getTitle();
                    String content = info.getContent();
                    String date = info.getDate();
                    info = ChatOperatorDatabase.getAppMessageInfo(mContext, AppMessageInfo.SYSTEM_MESSAGE_ID);
                    int allCount = info.getCount() - count;
                    info.setTitle(title);
                    info.setContent(content);
                    info.setDate(date);
                    info.setCount(allCount < 0 ? 0 : allCount);
                    ChatOperatorDatabase.updateMessage(mContext, info);
                }

            } else { // 不应该会出现
                throw new UnsupportedOperationException(TAG + "#DialogInterface onClick info type = "+info.getType());
            }

            if (count > 0) {
                BadgeInfo badgeInfo = BadgeCache.getInstance().getBadgeInfo(mAppId, mAppId + "_IM");
                if (badgeInfo != null)
                    badgeInfo.updateCount(getAllCounts());
            }

            mAdapter.notifyDataSetChanged();
        }
        ListDialog.deleteListDialog();
    }
}
