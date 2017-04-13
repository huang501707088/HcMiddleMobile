package com.android.hcframe.netdisc;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.share.ShareActivity;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.android.hcframe.view.toast.NoDataView;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2016/8/14.
 * 我的分享
 */
public class MyShareActivity extends HcBaseActivity implements PullToRefreshBase.OnRefreshBothListener {
    private static final String TAG = "MyShareActivity";
    private TopBarView mTopBarView;
    private PullToRefreshListView mDiscSearchLv;
    List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
    MyShareAdapter myShareAdapter;
    private Handler mHandler = new Handler();
    /**
     * 自定义的底部弹出框类
     */
    private SecondSharePopupWindow menuWindow;
    String name;
    String link;
    String code;
    private NoDataView mNoDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_workgroup_list);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        mTopBarView.setTitle("我的分享");
        mDiscSearchLv = (PullToRefreshListView) findViewById(R.id.netdisc_search_lv);
        mNoDataView = (NoDataView) findViewById(R.id.share_pager_no_data);
        mDiscSearchLv.setEmptyView(mNoDataView);
        HcDialog.showProgressDialog(MyShareActivity.this, "获取数据中");
        initDate();
        mDiscSearchLv.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mDiscSearchLv.setOnRefreshBothListener(MyShareActivity.this);
        // 绑定listView的监听器
        mDiscSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {
                JSONObject jsonObject = (JSONObject) adapter.getAdapter().getItem(key);
                name = jsonObject.optString("infoname");

                if ("B".equals(jsonObject.optString("type"))) {
                    link = jsonObject.optString("link");
                } else {
                    link = "";
                }
                code = jsonObject.optString("code");
                menuWindow = new SecondSharePopupWindow(MyShareActivity.this, itemsOnClick, link, code, name);
                //显示窗口
                menuWindow.showAtLocation(MyShareActivity.this.findViewById(R.id.my_skydrive_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                backgroundAlpha(0.5f);
            }
        });
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    private void initDate() {
        DeleteListRequest request = new DeleteListRequest();
        String url = NetdiscUtil.BASE_URL + "getShareList";
        HttpPost share = new HttpPost(url);
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                jsonObjectList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    JSONArray fileList = jsonObject.optJSONArray("fileList");
                    if (fileList != null && fileList.length() > 0) {
                        for (int i = 0; i < fileList.length(); i++) {
                            jsonObjectList.add((JSONObject) fileList.get(i));
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //清空adapter
                            mDiscSearchLv.onRefreshComplete();
                            if (myShareAdapter == null) {
                                myShareAdapter = new MyShareAdapter(MyShareActivity.this, jsonObjectList, new SuccessExecute());
                                mDiscSearchLv.setAdapter(myShareAdapter);
                            } else {
                                myShareAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public String getTag() {
                return null;
            }

            @Override
            public void onAccountExcluded(String data, String msg, RequestCategory category) {
                HcDialog.deleteProgressDialog();
                mDiscSearchLv.onRefreshComplete();
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }

    private class SuccessExecute implements MyShareAdapter.ISuccessExecute {
        @Override
        public void successExecute(int position) {
            twoBtnAlterDialog(MyShareActivity.this, "是否删除该文件", position);
        }
    }

    private static TwoBtnAlterDialog alterDialog;

    /**
     * 弹出重新登录dialog
     *
     * @param context activity实例
     * @param msg     提示消息体
     */
    private void twoBtnAlterDialog(final Context context, String msg, final int position) {
        if (alterDialog == null) {
            alterDialog = TwoBtnAlterDialog.createDialog(context, msg);
            TwoBtnAlterDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JSONObject json = jsonObjectList.get(position);
                    DeleteListRequest request = new DeleteListRequest();
                    String url = NetdiscUtil.BASE_URL + "cancelShare";
                    HttpPost share = new HttpPost(url);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
                    builder.addTextBody("infoid", json.optString("infoid"));
                    share.setEntity(builder.build());
                    request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
                        @Override
                        public void onSuccess(Object data, RequestCategory request) {
                            HcDialog.deleteProgressDialog();
                            try {
                                JSONObject jsonObject = new JSONObject(data.toString());
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        jsonObjectList.remove(position);
                                        myShareAdapter.notifyDataSetChanged();
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public String getTag() {
                            return null;
                        }

                        @Override
                        public void onAccountExcluded(String data, String msg, RequestCategory category) {
                            HcDialog.deleteProgressDialog();
                            mDiscSearchLv.onRefreshComplete();
                        }

                        @Override
                        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

                        }
                    }, false);
                    alterDialog.dismiss();
                    alterDialog = null;

                }
            });
            TwoBtnAlterDialog.btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alterDialog.dismiss();
                    alterDialog = null;
                }
            });
            alterDialog.show();
        } else {
            alterDialog.dismiss();
            alterDialog = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        HttpRequestQueue.getInstance().cancelRequest("");
        super.onDestroy();
    }

    private class DeleteListRequest extends AbstractHttpRequest {

        private static final String TAG = MyShareActivity.TAG + "$MySkydriveRequest";

        public DeleteListRequest() {

        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/deletefile";
        }

        @Override
        public String getParameterUrl() {

            return "";
        }

    }


    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        initDate();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        //上拉
    }

    //为弹出窗口实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            backgroundAlpha(1);
            menuWindow.dismiss();
            int i = v.getId();
            if (i == R.id.netdisc_picture_linear) {//复制链接
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(link);
            } else if (i == R.id.netdisc_music_linear) {//微信
                doStartApplicationWithPackageName("com.tencent.mm", link);
            } else if (i == R.id.netdisc_video_linear) {//QQ
                doStartApplicationWithPackageName("com.tencent.mobileqq", link);
            } else if (i == R.id.netdisc_file_linear) {//其他应用
                showShareActivity(link);
            }

        }

    };

    private void doStartApplicationWithPackageName(String packagename, String link) {

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
            intent.putExtra(Intent.EXTRA_TEXT, link);
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