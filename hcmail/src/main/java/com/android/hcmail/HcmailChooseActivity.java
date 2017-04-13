package com.android.hcmail;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.email.R;
import com.android.hcframe.hcmail.EmailUtils;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-10 11:22.
 */
public class HcmailChooseActivity extends HcBaseActivity implements HcChooseHomeView.OnOperatorListener {

    private static final String TAG = "HcmailChooseActivity";

    public static final String SELECT = "select";

    private HcChooseHomeView mChooseView;

    private FrameLayout mParent;

    private ChooseObserver mObserver;

    private boolean mSelected;

    private int mLocation = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcmail_activity_choose);
        mParent = (FrameLayout) findViewById(R.id.hcmail_choose_personnel_parent);
        boolean b = getIntent().getBooleanExtra(SELECT, false);
        mSelected = b;
        mLocation = getIntent().getIntExtra("location", HcmailWriteActivity.LOCATION_APP);
        mChooseView = new HcChooseHomeView(this, mParent, b, this, "人员选择", null);
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
        mChooseView.changePages();

    }

    @Override
    public void onParentItemClick(ItemInfo info) {
        if (mLocation == HcmailWriteActivity.LOCATION_APP) {
            String depId = info.getItemId();
            HcLog.D(EmailUtils.DEBUG, TAG + " #onParentItemClick before change info depId = " + depId);
            if (TextUtils.isEmpty(depId) || "0".equals(depId)) {
                depId = getDepId();
            }
            HcLog.D(EmailUtils.DEBUG, TAG + " #onParentItemClick after change info depId = " + depId);
            List<ItemInfo> infos = OperateDatabase.getDepartment(this, TextUtils.isEmpty(depId) ? null : depId, HcDatabase.Contacts.EMAIL);
            Iterator<ItemInfo> iterator = infos.iterator();
            ItemInfo iter;
            while (iterator.hasNext()) {
                iter = iterator.next();
                if (iter instanceof StaffInfo) {
                    if (TextUtils.isEmpty(iter.getItemId())) {
                        iterator.remove();
                    } else {
                        iter.setMultipled(true);
                        iter.setItemValue(iter.getItemValue() + "<" + iter.getItemId() + ">");
                    }
                }
            }

            if (mObserver != null) {
                mObserver.notifyData(infos);
            }

        } else {
            HcDialog.showProgressDialog(this, "正在获取数据...");
            getLoaderManager().initLoader(0, null, LOADER_CALLBACKS);
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

    private String[] mProjection = new String[]{
            Email._ID, // 数据库主键
            /* Email.DATA, Email.DISPLAY_NAME, */
            Email.ADDRESS, // 邮箱地址,也可以用Email.DATA字段
            Email.CONTACT_ID, // 联系人ID,在这里可以取消
            Email.PHOTO_THUMBNAIL_URI, // 头像缩略图地址
            // Email.PHOTO_URI, 头像地址
            //Email.DISPLAY_NAME_ALTERNATIVE,
            Email.DISPLAY_NAME_PRIMARY // 用户姓名,也可以用 Email.DISPLAY_NAME_ALTERNATIVE
    };

    private final LoaderManager.LoaderCallbacks<Cursor> LOADER_CALLBACKS =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new CursorLoader(HcmailChooseActivity.this, Email.CONTENT_URI, mProjection, null, null, null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    HcDialog.deleteProgressDialog();
                    if (data != null) {
                        int size = data.getCount();
                        HcLog.D(EmailUtils.DEBUG, TAG + " #onLoadFinished cursor size = " + data.getCount());
                        if (size == 0) {
                            return;
                        }
                        List<ItemInfo> infos = new ArrayList<ItemInfo>();
                        int id;
                        String address;
                        int contactId;
                        String thumbnailUri;
                        String primary;
                        ItemInfo info;
                        data.moveToFirst();
                        for (int i = 0; i < size; i++) {
                            info = new StaffInfo();
                            id = data.getInt(0);
                            address = data.getString(1);
                            contactId = data.getInt(2);
                            thumbnailUri = data.getString(3);
                            primary = data.getString(4);
                            HcLog.D(EmailUtils.DEBUG, TAG + " id = " + id + " address=" + address + " contactId =" + contactId + " thumbnailUri =" + thumbnailUri
                                    + " primary = " + primary);
                            info.setItemId(address);
                            info.setUserId("" + contactId);
                            info.setItemValue(primary + "<" + address + ">");
                            info.setMultipled(true);
                            info.setIconUrl(thumbnailUri);
                            infos.add(info);
                            data.moveToNext();
                        }
                        if (mObserver != null) {
                            mObserver.notifyData(infos);
                        }

                    }

                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {

                }
            };

    private String getDepId() {
        String depId = null;
        Cursor c = getContentResolver().query(HcProvider.CONTENT_URI_CONTACTS, new String[]{HcDatabase.Contacts.ID}, HcDatabase.Contacts.PARENT_ID + "=0", null, null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                depId = c.getString(0);
            }
            c.close();
        }
        return depId;

    }

}


