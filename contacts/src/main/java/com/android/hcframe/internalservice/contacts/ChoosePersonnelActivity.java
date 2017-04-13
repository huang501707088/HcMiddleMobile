package com.android.hcframe.internalservice.contacts;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.contacts.data.ContactsCacheData;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.contacts.data.EmployeeInfo;
import com.android.hcframe.view.selector.DepInfo;
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
 * Created by jrjin on 16-8-10 11:22.
 */
public class ChoosePersonnelActivity extends HcBaseActivity implements HcChooseHomeView.OnOperatorListener {

    private static final String TAG = "ScheduleChooseActivity";

    public static final String SELECT = "select";

    private HcChooseHomeView mChooseView;

    private FrameLayout mParent;

    private ChooseObserver mObserver;

    private boolean mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity_choose_personnel);
        mParent = (FrameLayout) findViewById(R.id.contacts_choose_personnel_parent);
        boolean b = getIntent().getBooleanExtra(SELECT, false);
        mSelected = b;
        mChooseView = new HcChooseHomeView(this, mParent, b, this, "人员选择", null);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
        mChooseView.changePages();

    }

    @Override
    public void onParentItemClick(ItemInfo info) {
        String depId = info.getItemId();
        HcLog.D(TAG + " #onParentItemClick info id = " + depId);
        List<ContactsInfo> contacts = ContactsCacheData.getInstance().getContacts(TextUtils.isEmpty(depId) ? null : depId);
        List<ItemInfo> itemInfoList = new ArrayList<ItemInfo>();
        ItemInfo item;
        for (ContactsInfo contactsInfo : contacts) {
        	if (contactsInfo instanceof EmployeeInfo) {
                if (TextUtils.isEmpty(contactsInfo.getUserId()))
                    continue;
                item = new StaffInfo();
                item.setUserId(contactsInfo.getUserId());
                item.setMultipled(mSelected);
                item.setIconUrl(HcUtil.getHeaderUri(contactsInfo.getUserId()));
            } else {
                item = new DepInfo();
                item.setIconUrl("drawable://" + HcUtil.getDepResId());
            }
            item.setItemId(contactsInfo.getId());
            item.setItemValue(contactsInfo.getName());

            itemInfoList.add(item);
        }
        contacts.clear();
        if (mObserver != null) {
            mObserver.notifyData(itemInfoList);
        }
    }

    @Override
    public void onRefresh(ItemInfo info) {
        HcLog.D(TAG + " #onRefresh info id = " + info.getItemId());

    }

    @Override
    protected void onDestroy() {
        if (mChooseView != null) {
            mObserver.deleteObserver(mChooseView);
            mChooseView.onDestory();
            mChooseView = null;
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
}


