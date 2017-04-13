package com.android.hcframe.im;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;

import java.util.List;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-27 10:08.
 */

public class IMDeletePersonnelActivity extends HcBaseActivity implements HcChooseHomeView.OnOperatorListener {

    private HcChooseHomeView mChooseView;

    private FrameLayout mParent;

    private ChooseObserver mObserver;

    private List<ItemInfo> mItemInfos = null; //= new ArrayList<ItemInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = null;
        boolean selected = true;
        if (intent != null && intent.getExtras() != null) {
            mItemInfos = intent.getParcelableArrayListExtra("items");
            title = intent.getStringExtra("title");
            selected = intent.getBooleanExtra("select", true);
        }
        if (mItemInfos == null || mItemInfos.isEmpty()) {
            finish();
            return;
        }
        if (title == null) {
            title = "删除成员";
        }
        setContentView(R.layout.im_activity_delete_personnel_layout);
        mParent = (FrameLayout) findViewById(R.id.im_delete_personnel_parent);
        mChooseView = new HcChooseHomeView(this, mParent, selected, this, title, null);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
        mChooseView.changePages();
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
        mObserver.notifyData(mItemInfos);
    }

    @Override
    public void onRefresh(ItemInfo info) {

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
}
