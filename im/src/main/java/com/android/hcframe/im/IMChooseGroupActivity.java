package com.android.hcframe.im;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
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
 * Created by jrjin on 17-1-6 09:46.
 */

public class IMChooseGroupActivity extends HcBaseActivity implements HcChooseHomeView.OnOperatorListener {

    private static final String TAG = "IMChooseGroupActivity";

    private HcChooseHomeView mChooseView;

    private FrameLayout mParent;

    private ChooseObserver mObserver;

    private List<ChatGroupMessageInfo> mInfos = new ArrayList<ChatGroupMessageInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_activity_choose_personnel);
        mParent = (FrameLayout) findViewById(R.id.im_choose_personnel_parent);
        mChooseView = new HcChooseHomeView(this, mParent, true, this, "群组选择", null);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
        mInfos.addAll(ChatOperatorDatabase.getChatGroups(this));
        mChooseView.changePages();
    }

    @Override
    protected void onDestroy() {
        if (mChooseView != null) {
            mObserver.deleteObserver(mChooseView);
            mObserver = null;
            mChooseView.onDestory();
            mChooseView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCanelRefreshRequest(ItemInfo info) {

    }

    @Override
    public void onParentItemClick(ItemInfo info) {
        if (mObserver != null && mInfos.size() > 0) {
            List<ItemInfo> itemInfoList = new ArrayList<ItemInfo>();
            ItemInfo item;
            for (ChatGroupMessageInfo groupInfo : mInfos) {
            	item = new StaffInfo();
                item.setItemValue(groupInfo.getTitle());
                item.setMultipled(true);
                item.setItemId(groupInfo.getId());
                item.setIconUrl(groupInfo.getIconUri());
                item.setUserId(groupInfo.getId());
                itemInfoList.add(item);
            }

            mObserver.notifyData(itemInfoList);
        }
    }

    @Override
    public void onRefresh(ItemInfo info) {

    }

    private class ChooseObserver extends Observable {
        public void notifyData(List<ItemInfo> data) {
            setChanged();
            notifyObservers(data);
        }
    }

}
