package com.android.hcframe.share;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.push.HcAppState;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-3-16 10:41.
 */
public class ShareActivity extends HcBaseActivity implements OnItemClickListener {

    public static final String TAG = "ShareActivity";

    public static final String SHARE_KEY_TYPE = "type";
    public static final String SHARE_KEY_CONTENT = "content";

    public static final int SHARE_TEXT = 0;
    public static final int SHARE_IMAGE = 1;
    public static final int SHARE_AUDIO = 2;

    private int mType = SHARE_TEXT;

    private List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();

    private GridView mGridView;

    private TextView mCanel;

    private ShareAppAdapter mAdapter;

    private String mContent;

    private Uri mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mType = intent.getExtras().getInt(SHARE_KEY_TYPE, SHARE_TEXT);
            mContent = intent.getExtras().getString(SHARE_KEY_CONTENT, "");
            mSource = intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
        }
        searchShareApps();
        HcLog.D(TAG + " it is onCraete! app size = "+mApps.size());

        if (mApps.size() == 0) {
            HcUtil.showToast(this, "你手机里未安装可分享的应用");
            finish();
            return;
        }
        setContentView(R.layout.activity_share);

        mGridView = (GridView) findViewById(R.id.share_grid);
        mCanel = (TextView) findViewById(R.id.share_canel);
        mGridView.setOnItemClickListener(this);
        mAdapter = new ShareAppAdapter(this);
        mGridView.setAdapter(mAdapter);
        mCanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canelShare();
            }
        });
    }

    private void searchShareApps() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        String type = getShareType();
        if (type != null) {
            intent.setType(type);
        }
        mApps.addAll(getPackageManager().queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT));
    }

    private String getShareType() {
        switch (mType) {
            case SHARE_AUDIO:
                return "audio/*";
            case SHARE_IMAGE:
                return "image/*";
            case SHARE_TEXT:
                return "text/plain";

            default:
                return null;

        }
    }

    private class ShareAppAdapter extends HcBaseAdapter<ResolveInfo> {

        public ShareAppAdapter(Context context) {
            super(context, mApps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ResolveInfo info = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_share_app, parent, false);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.share_item_icon);
                holder.mName = (TextView) convertView.findViewById(R.id.share_item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mIcon.setImageDrawable(info.loadIcon(getPackageManager()) /*info.activityInfo.loadIcon(getPackageManager())*/);
            holder.mName.setText(info.loadLabel(getPackageManager()));
            return convertView;
        }

        private class ViewHolder {
            ImageView mIcon;
            TextView mName;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ResolveInfo info = (ResolveInfo) parent.getItemAtPosition(position);
        share(info);
        canelShare();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    canelShare();
                }
                return true;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void canelShare() {
        HcAppState.getInstance().removeActivity(this);
        finish();
        overridePendingTransition(0, R.anim.slide_out_to_bottom);
    }

    private void share(ResolveInfo info) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getShareType());
        intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
        if (mType == SHARE_IMAGE && mSource != null) {
            intent.putExtra(Intent.EXTRA_STREAM, mSource);
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, mContent);
        intent.putExtra(Intent.EXTRA_TITLE, HcUtil.getApplicationName(this) + "  V" + HcConfig.getConfig().getAppVersion());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
