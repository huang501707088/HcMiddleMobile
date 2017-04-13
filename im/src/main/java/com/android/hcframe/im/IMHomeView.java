package com.android.hcframe.im;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.TopBarView;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.badge.ModuleBadgeInfo;
import com.android.hcframe.im.data.AppMessageAdapter;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.view.dialog.ListDialog;
import com.android.hcframe.view.toast.NoDataView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-29 14:54.
 */
public class IMHomeView extends AbstractPage implements AdapterView.OnItemClickListener,
        OnChatReceiveListener, OnReceiverCallback, AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener {

    private static final String TAG = "IMHomeView";

    private final String mAppId;

    private TextView mOrganization;
    private TextView mContacts;
    private TextView mGroup;
    private TextView mBroadcast;

    private PullToRefreshListView mListView;

    private List<AppMessageInfo> mInfos = new ArrayList<AppMessageInfo>();

    private AppMessageAdapter mAdapter;

    private Handler mHandler = new Handler();

    private RelativeLayout mPopParent;

    private TextView mContactsPop;

    private TextView mGroupPop;

    private TopBarView mBarView;

    private BadgeInfo mBadgeInfo;

    private int mDeletePosition = -1;

//    private LinearLayout mEmptyParent;

    private NoDataView mNoDataView;

    public IMHomeView(Activity context, ViewGroup group, String appId) {
        super(context, group);
        mAppId = appId;
        IMSettings.setIMAppId(context, appId);
    }

    @Override
    public void initialized() {
//        if (isFirst) {
//            isFirst = !isFirst;
//            mInfos.addAll(ChatOperatorDatabase.getAppMessages(mContext));
////            test();
//            mAdapter = new AppMessageAdapter(mContext, mInfos);
//            mListView.setAdapter(mAdapter);
//        }
        if (isFirst) {
            isFirst = !isFirst;
            mNoDataView.setDescription("赶紧找你小伙伴聊聊吧>>");
//            KeepLiveManager.getInstance().startJobService(mContext);
        }
        HcLog.D(TAG + " #initialized end current view = "+this);
    }

    @Override
    public void setContentView() {
        if (mView == null) {

            View topbar = mContext.findViewById(R.id.menu_top_bar);
            if (topbar != null) {
                mBarView = (TopBarView) topbar;
                mBarView.setMenuBtnVisiable(View.VISIBLE);
                mBarView.setMenuSrc(R.drawable.im_group_add_btn);
                mBarView.setMenuListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPopParent.getVisibility() == View.VISIBLE) {
                            mPopParent.setVisibility(View.GONE);
                        } else {
                            mPopParent.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            mView = mInflater.inflate(R.layout.im_home_layout, null);

            mOrganization = (TextView) mView.findViewById(R.id.im_home_organization);
            mContacts = (TextView) mView.findViewById(R.id.im_home_contacts);
            mGroup = (TextView) mView.findViewById(R.id.im_home_group);
            mBroadcast = (TextView) mView.findViewById(R.id.im_home_broadcast);

            mListView = (PullToRefreshListView) mView.findViewById(R.id.im_home_listview);
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mNoDataView = (NoDataView) mView.findViewById(R.id.im_home_no_data);
            mListView.setEmptyView(mNoDataView);

            mPopParent = (RelativeLayout) mView.findViewById(R.id.im_home_pop_view);
            mContactsPop = (TextView) mView.findViewById(R.id.im_home_contacts_btn);
            mGroupPop = (TextView) mView.findViewById(R.id.im_home_group_btn);

            mOrganization.setOnClickListener(this);
            mContacts.setOnClickListener(this);
            mGroup.setOnClickListener(this);
            mBroadcast.setOnClickListener(this);

            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(this);

            mContactsPop.setOnClickListener(this);
            mGroupPop.setOnClickListener(this);

            mNoDataView.setOnClickListener(this);
        }
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.im_home_organization) {
            Intent intent = new Intent(mContext, CommonActivity.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            intent.putExtra("className", "com.android.hcframe.im.IMOrganizationMenuPage");
            mContext.startActivity(intent);
        } else if (id == R.id.im_home_contacts) {
            Intent intent = new Intent(mContext, CommonActivity.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            intent.putExtra("className", "com.android.hcframe.im.IMContactsMenuPage");
            mContext.startActivity(intent);
        } else if (id == R.id.im_home_group) {
            Intent intent = new Intent(mContext, IMGroupMenuPage.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            mContext.startActivity(intent);
        } else if (id == R.id.im_home_broadcast) {
            KeepLiveManager.getInstance().startJobService(mContext);
        } else if (id == R.id.im_home_contacts_btn) {
            mPopParent.setVisibility(View.GONE);
            Intent intent = new Intent(mContext, CommonActivity.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            intent.putExtra("className", "com.android.hcframe.im.IMContactsMenuPage");
            mContext.startActivity(intent);
        } else if (id == R.id.im_home_group_btn) {
            mPopParent.setVisibility(View.GONE);
            Intent intent = new Intent(mContext, IMGroupMenuPage.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            mContext.startActivity(intent);
        } else if (id == R.id.im_home_no_data) {
            Intent intent = new Intent(mContext, CommonActivity.class);
            intent.putExtra("data", mAppId);
            intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
            intent.putExtra("className", "com.android.hcframe.im.IMContactsMenuPage");
            mContext.startActivity(intent);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppMessageInfo info = (AppMessageInfo) parent.getItemAtPosition(position);
        if (info.getType() == 4) { // 进入系统消息界面
            Intent intent = new Intent(mContext, CommonActivity.class);
            intent.putExtra("className", "com.android.hcframe.im.IMSystemMessagePage");
            intent.putExtra("title", "系统消息");
            intent.putExtra("data", mAppId);
            mContext.startActivity(intent);
        } else {
            startChatActivity(info);
        }

    }

    @Override
    public void onResume() {
        if (mPopParent.getVisibility() != View.GONE)
            mPopParent.setVisibility(View.GONE);
        IMManager.getInstance().addChatListener(this);
        if (mAdapter == null) {
            mAdapter = new AppMessageAdapter(mContext, mInfos);
            mListView.setAdapter(mAdapter);
        }
        mInfos.clear();
        mInfos.addAll(ChatOperatorDatabase.getAppMessages(mContext));
        mAdapter.notifyDataSetChanged();

        // 增加IM角标
        if (mBadgeInfo == null) {
            mBadgeInfo = BadgeCache.getInstance().getBadgeInfo(mAppId, mAppId + "_IM");
//            HcLog.D(TAG + " #onResume BadgeInfo = "+mBadgeInfo);
            if (mBadgeInfo == null) {
                mBadgeInfo = new ModuleBadgeInfo();
                mBadgeInfo.setAppId(mAppId);
                mBadgeInfo.setModuleId(mAppId + "_IM");
                mBadgeInfo.setType(1);
                mBadgeInfo.setCount(getAllCounts());
                BadgeCache.getInstance().matchBadge(mBadgeInfo, "add");
            }
        }

        PushInfo info = HcPushManager.getInstance().getPushInfo();
        if (info != null) {
            HcPushManager.getInstance().setPushInfo(null);
            AppMessageInfo messageInfo = ChatOperatorDatabase.getAppMessageInfo(mContext, info.getContent());
            if (messageInfo != null) {
                for (AppMessageInfo info1 : mInfos) {
                	if (messageInfo.getId().equals(info1.getId())) {
                        messageInfo = info1;
                        break;
                    }
                }

                startChatActivity(messageInfo);
            }
        }
    }

    @Override
    public void onReceive(Chat chat, Message message) {
        HcLog.D(TAG + "#onReceiver 在消息列表页面 chat = "+chat + " message = "+message);
        // 注意这里还在子线程里
        switch (message.getType()) {
            case error:
                HcLog.D(TAG + " 信息发送失败!");
                break;
            case chat:
            case groupchat:
            case normal:
                IMUtil.parseMessage(mContext, message, this);
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestory() {
        IMManager.getInstance().addChatListener(IMReceiverListener.getInstance());
        HcLog.D(TAG + " #onDestory end! current view = "+this );
    }

    @Override
    public void onReceiver(ChatMessageInfo chatInfo, AppMessageInfo appInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mInfos.clear();
                mInfos.addAll(ChatOperatorDatabase.getAppMessages(mContext));
                mAdapter.notifyDataSetChanged();
                HcLog.D(TAG + " #onReceiver mBadgeInfo = "+mBadgeInfo);
                if (mBadgeInfo != null) {
                    mBadgeInfo.addCount(1);
                }
            }
        });

    }

    private int getAllCounts() {
        int count = 0;
        for (AppMessageInfo info : mInfos) {
        	count += info.getCount();
        }
        return count;
    }

    private void startChatActivity(AppMessageInfo info) {
        if (info.getCount() != 0) {
            info.setCount(0);
            ChatOperatorDatabase.updateAppMessage(mContext, info);
            mBadgeInfo.updateCount(getAllCounts());
        }

        switch (info.getType()) {
            case 1: // 应用模块

                break;
            case 2: // 群聊消息
                IMSettings.deleteIMReceiverGroup(mContext, info.getId()); // 群聊,删除@的标志

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("emp", info);
                intent.putExtra("group", true);
                mContext.startActivity(intent);
                break;
            case 3: // 单聊消息
                Intent intent2 = new Intent(mContext, ChatActivity.class);
                intent2.putExtra("emp", info);
                intent2.putExtra("group", false);
                mContext.startActivity(intent2);
                break;
            case 4:
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mDeletePosition = position - 1;
        HcLog.D(TAG + " #onItemLongClick mDeletePosition = "+mDeletePosition);
        ListDialog.showListDialog(mContext, new String[] {"删除该聊天"}, this);

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
//        HcLog.D(TAG + " #onClick(dialog) which = "+which);
        if (mDeletePosition > -1) {
            int position = mDeletePosition;
            mDeletePosition = -1;
            AppMessageInfo info = mInfos.remove(position);
            if (info.getType() == 4) { // 系统消息
                // 删除系统消息记录和应用模块消息推送记录
                ChatOperatorDatabase.deleteAllSystemMessages(mContext);
                OperateDatabase.deleteSystemMessage(mContext);
            } else {
                // 删除聊天记录和消息记录
                ChatOperatorDatabase.deleteChatMessages(mContext, info.getId());
                ChatOperatorDatabase.deleteAppMessage(mContext, info.getId());
            }

            if (info.getCount() > 0) {
                mBadgeInfo.updateCount(getAllCounts());
            }

            mAdapter.notifyDataSetChanged();
        }
        ListDialog.deleteListDialog();
    }
}
