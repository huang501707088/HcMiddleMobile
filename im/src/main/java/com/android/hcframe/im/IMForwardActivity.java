package com.android.hcframe.im;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.selector.HcChooseFragment;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-1-5 11:32.
 */

/**
 * 转发Activity
 */
public class IMForwardActivity extends HcBaseActivity implements HcChooseFragment.OnOperatorListener {

    private static final String TAG = "IMForwardActivity";

    private HcChooseFragment mFragment;

    private FrameLayout mParent;

    private TopBarView mTopBar;

    private ChooseObserver mObserver;

    private LinearLayout mPersonnelParent;

    private LinearLayout mGroupParent;

    private int mCount;

    private List<AppMessageInfo> mInfos = new ArrayList<AppMessageInfo>();

    private static final int REQUEST_CODE_GROUP = 1;

    private ChatMessageInfo mChatInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Bundle bundle = getIntent().getExtras();
//        if (bundle == null) {
//            finish();
//            return;
//        }
//        String chatId = bundle.getString("chatId");
//        String chatMessageId = bundle.getString("chatMessageId");
//        if (chatId == null || chatMessageId == null) {
//            finish();
//            return;
//        }
//        mChatInfo = ChatOperatorDatabase.getChatMessage(this, chatId, chatMessageId);
//        if (mChatInfo == null) {
//            finish();
//            return;
//        }
        setContentView(R.layout.im_activity_forward);
        initViews();
        initData();
    }

    private void initViews() {
        mTopBar = (TopBarView) findViewById(R.id.im_forward_top_bar);
        mParent = (FrameLayout) findViewById(R.id.im_forward_select_parent);
        mPersonnelParent = (LinearLayout) findViewById(R.id.im_forward_select_friends_parent);
        mGroupParent = (LinearLayout) findViewById(R.id.im_forward_select_groups_parent);

        mPersonnelParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IMForwardActivity.this, ChoosePersonnelActivity.class);
                intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
            }
        });

        mGroupParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IMForwardActivity.this, IMChooseGroupActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GROUP);
            }
        });

        mTopBar.setReturnViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFragment != null) {
                    mFragment.onReturn();
                }
            }
        });

        mTopBar.setRightViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCount > 0) {
                    // 发送
                    List<ItemInfo> selected = mFragment.getAllSelected();
                    ArrayList<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
                    int size = mInfos.size();
                    ItemInfo item;
                    for (int i = 0; i < mCount; i++) {
                        item = selected.get(i);
                        for (int j = size - 1; j >= 0; j--) {
                            if (item.getItemId().equals(mInfos.get(j).getId())) {
                                infos.add(mInfos.remove(j));
                                size --;
                                break;
                            }

                        }
                    }
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra("chats", infos);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void initData() {
        mTopBar.setTitle("选择");
        String userId = SettingHelper.getUserId(this);
        String selection = HcDatabase.AppMessage.USER_ID + "=" + "'" + userId + "'"
                + " AND " + HcDatabase.AppMessage.MESSAGE_TYPE + "!=1"
                + " AND " + HcDatabase.AppMessage.MESSAGE_TYPE + "!=4";
        mInfos.addAll(ChatOperatorDatabase.getAppMessages(this, selection, HcDatabase.AppMessage.MESSAGE_DATE + " DESC"));
        mFragment = new HcChooseFragment(this, mParent, true, this, false);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mFragment);
        mFragment.changePages();
    }

    @Override
    protected void onDestroy() {
        if (mObserver != null) {
            mObserver.deleteObserver(mFragment);
            mObserver = null;
        }
        if (mFragment != null) {
            mFragment.onDestory();
            mFragment = null;
        }

        if (mInfos != null) {
            mInfos.clear();
        }
        super.onDestroy();
    }

    private class ChooseObserver extends Observable {
        public void notifyData(List<ItemInfo> data) {
            setChanged();
            notifyObservers(data);
        }
    }

    @Override
    public void onCanelRefreshRequest(ItemInfo info) {

    }

    @Override
    public void onParentItemClick(ItemInfo info) {

        if (mObserver != null && mInfos.size() > 0) {
            List<ItemInfo> itemInfos = new ArrayList<ItemInfo>();
            ItemInfo item;
            for (AppMessageInfo messageInfo : mInfos) {
            	item = new StaffInfo();
                item.setItemValue(messageInfo.getTitle());
                item.setMultipled(true);
                item.setItemId(messageInfo.getId());
                if (messageInfo.getType() == 3) {// 私聊
                    item.setIconUrl(HcUtil.getHeaderUri(messageInfo.getIconUri()));
                    item.setUserId(messageInfo.getIconUri());
                } else {
                    item.setIconUrl(messageInfo.getIconUri());
                    item.setUserId(messageInfo.getId());
                }
                itemInfos.add(item);
            }
            mObserver.notifyData(itemInfos);
        }
    }

    @Override
    public void onRefresh(ItemInfo info) {

    }

    @Override
    public void setSelectedCount(int count) {
        mCount = count;
        mTopBar.setRightText(count == 0 ? "发送" : "发送(" + count + ")");
    }

    @Override
    public void setSelectedStatus(HcChooseFragment.SelectStatus status) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
                    case REQUEST_CODE_GROUP: // 都是群组
                        if (data != null && data.getExtras() != null) {
                            List<ItemInfo> list = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            if (list != null) {
                                ArrayList<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
                                AppMessageInfo info;
                                for (ItemInfo item : list) {
                                	info = new AppMessageInfo();
                                    info.setId(item.getItemId());
                                    info.setTitle(item.getItemValue());
                                    info.setIconUri(item.getIconUrl());
                                    info.setType(2);
                                    infos.add(info);
                                }

                                Intent intent = new Intent();
                                intent.putParcelableArrayListExtra("chats", infos);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        break;
                    case HcChooseHomeView.REQUEST_CODE: // 都是私聊
                        if (data != null && data.getExtras() != null) {
                            List<ItemInfo> list = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            if (list != null) {
                                ArrayList<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
                                AppMessageInfo info;
                                for (ItemInfo item : list) {
                                    info = new AppMessageInfo();
                                    info.setId(HcUtil.getMD5String(SettingHelper.getUserId(this) + item.getUserId()));
                                    info.setTitle(item.getItemValue());
                                    info.setIconUri(item.getUserId());
                                    info.setType(3);
                                    infos.add(info);
                                }

                                Intent intent = new Intent();
                                intent.putParcelableArrayListExtra("chats", infos);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }
}
