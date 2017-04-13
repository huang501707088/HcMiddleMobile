package com.android.hcframe.netdisc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.share.ShareActivity;

import java.util.List;

/**
 * Created by pc on 2016/8/8.
 */
public class ShareSuccessActivity extends HcBaseActivity implements View.OnClickListener {
    TextView netdisc_id_tv_name, netdisc_id_tv_link, netdisc_id_tv_code;
    LinearLayout netdisc_id_ll_code;
    int num;
    String code;
    String name;
    String link;
    private TopBarView mTopBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_share_sunccess_layout);
        netdisc_id_tv_name = (TextView) findViewById(R.id.netdisc_id_tv_name);
        netdisc_id_tv_link = (TextView) findViewById(R.id.netdisc_id_tv_link);
        netdisc_id_tv_code = (TextView) findViewById(R.id.netdisc_id_tv_code);
        netdisc_id_ll_code = (LinearLayout) findViewById(R.id.netdisc_id_ll_code);
        findViewById(R.id.netdisc_id_ll_wechat).setOnClickListener(this);
        findViewById(R.id.netdisc_id_ll_qq).setOnClickListener(this);
        findViewById(R.id.netdisc_id_ll_other).setOnClickListener(this);
        findViewById(R.id.netdisc_id_bt_copy).setOnClickListener(this);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        num = getIntent().getIntExtra("num", 1);
        code = getIntent().getStringExtra("code");
        name = getIntent().getStringExtra("name");
        link = getIntent().getStringExtra("link");
        mTopBarView.setTitle("分享成功");
        if (code != null && !"".equals(code)) {
            netdisc_id_tv_code.setText(code);
        } else {
            netdisc_id_ll_code.setVisibility(View.INVISIBLE);
        }
        if (num > 1) {
            netdisc_id_tv_name.setText(name + "等文件");
        } else {
            netdisc_id_tv_name.setText(name + "文件");
        }
        netdisc_id_tv_link.setText(link);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.netdisc_id_ll_wechat) {//微信
            doStartApplicationWithPackageName("com.tencent.mm");
        } else if (i == R.id.netdisc_id_ll_qq) {//QQ
            doStartApplicationWithPackageName("com.tencent.mobileqq");
        } else if (i == R.id.netdisc_id_ll_other) {//其他应用
            if (code != null && !"".equals(code)) {
                showShareActivity("分享链接：" + link + "访问密码：" + code);
            } else {
                showShareActivity("分享链接：" + link);
            }

        } else if (i == R.id.netdisc_id_bt_copy) {//复制链接
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 将文本内容放到系统剪贴板里。
            if (code != null && !"".equals(code)) {
                cm.setText("分享链接：" + link + "访问密码：" + code);
            } else {
                cm.setText("分享链接：" + link);
            }

        }
    }

    private void doStartApplicationWithPackageName(String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_SEND);
        resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
//        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        resolveIntent.setType("text/plain");
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
//
//            intent.setComponent(cn);
//            startActivity(intent);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setComponent(cn);
            intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
            if (code != null && !"".equals(code)) {
                intent.putExtra(Intent.EXTRA_TEXT, "分享链接：" + link + "访问密码：" + code);
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, "分享链接：" + link);
            }
            intent.putExtra(Intent.EXTRA_TITLE, HcUtil.getApplicationName(this) + "  V" + HcConfig.getConfig().getAppVersion());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    private void showShareActivity(String shareInfo) {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(ShareActivity.SHARE_KEY_TYPE, ShareActivity.SHARE_TEXT);
        intent.putExtra(ShareActivity.SHARE_KEY_CONTENT, shareInfo);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, 0);
    }
}
