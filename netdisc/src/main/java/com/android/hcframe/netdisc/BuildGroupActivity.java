package com.android.hcframe.netdisc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.container.ContainerImageView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.data.SettingSharedHelper;
import com.android.hcframe.netdisc.netdisccls.MySkydriveFoldItem;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by pc on 2016/8/14.
 * 创建工作组
 */
public class BuildGroupActivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "BuildGroupActivity";
    private TopBarView mTopBarView;
    EditText netdisc_id_et_name;
    LinearLayout netdisc_id_ll_person;
    ContainerImageView netdisc_change_img;
    TextView netdisc_change_executor;
    Button netdisc_id_bt_next;
    private DisplayImageOptions mOptions;
    private ImageLoader mImageLoader;
    String assistantId;
    String name;
    String fileid;
    String folderid;
    Context mContext;
    String userIds = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_build_workgroup);
        mContext = getApplicationContext();
        fileid = getIntent().getStringExtra(WorkGroupActivity.FILEID);
        folderid = getIntent().getStringExtra(WorkGroupActivity.FOLDERID);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        mTopBarView.setTitle("创建新共享");
        netdisc_id_et_name = (EditText) findViewById(R.id.netdisc_id_et_name);
        netdisc_id_ll_person = (LinearLayout) findViewById(R.id.netdisc_id_ll_person);
        netdisc_change_img = (ContainerImageView) findViewById(R.id.netdisc_change_img);
        netdisc_change_img.setVisibility(View.INVISIBLE);
        netdisc_change_executor = (TextView) findViewById(R.id.netdisc_change_executor);
        netdisc_change_executor.setVisibility(View.INVISIBLE);
        netdisc_id_bt_next = (Button) findViewById(R.id.netdisc_id_bt_next);
        netdisc_id_ll_person.setOnClickListener(this);
        netdisc_id_bt_next.setOnClickListener(this);
        /**
         * 用来加载网络图片
         * */
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.netdisc_icon_default_head)
                .showImageForEmptyUri(R.drawable.netdisc_icon_default_head)
                .showImageOnFail(R.drawable.netdisc_icon_default_head).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.netdisc_id_ll_person) {
            Intent intent = new Intent(this, ChoosePersonnelActivity.class);
            intent.putExtra(ChoosePersonnelActivity.SELECT, false);
            intent.putExtra("userIds", "");
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        } else if (id == R.id.netdisc_id_bt_next) {
            name = netdisc_id_et_name.getText().toString();
            if (name != null && !"".equals(name)) {
                Intent intent = new Intent(this, ChoosePersonnelActivity.class);
                intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                intent.setPackage(mContext.getPackageName());
                intent.putExtra("userIds", userIds);
                startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
            } else {
                HcUtil.showToast(getApplicationContext(), "共享空间名称不能为空");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ItemInfo info = data.getParcelableExtra(HcChooseHomeView.ITEM_KEY);
            ArrayList<ItemInfo> mSelected = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
            if (info != null) {
                info.getItemValue();
                info.getIconUrl();
                userIds = info.getUserId();
                assistantId = info.getUserId();
                netdisc_change_img.setVisibility(View.VISIBLE);
                netdisc_change_executor.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(info.getIconUrl(),
                        netdisc_change_img, mOptions);
                netdisc_change_executor.setText(info.getItemValue());
            } else if (mSelected != null && mSelected.size() > 0) {
                submit(mSelected);

            }
//
//
//            intent.putParcelableArrayListExtra(CLICK_KEY, mSelected);
        }
    }

    private void submit(ArrayList<ItemInfo> mSelected) {
        HcDialog.showProgressDialog(BuildGroupActivity.this, "创建共享空间");
        StringBuilder userBuilder = new StringBuilder();//文件ID
        for (int i = 0; i < mSelected.size(); i++) {
            userBuilder.append(mSelected.get(i).getUserId() + ",");
        }
        String userid = userBuilder.toString();
        if (!TextUtils.isEmpty(userid)) {
            userid = userid.substring(0, userid.length() - 1);
        } else {
            userid = "";
        }
        if (TextUtils.isEmpty(fileid)) {
            fileid = "";
        }

        if (TextUtils.isEmpty(folderid)) {
            folderid = "";
        }
        StringBuilder assistantBuilder = new StringBuilder();//助理ID
        assistantBuilder.append(assistantId);
        String assistant = assistantBuilder.toString();
        if (TextUtils.isEmpty(assistant)) {
            assistant = "";
        }
//        StringBuilder nameBuilder = new StringBuilder();//
//        nameBuilder.append(name);
//        name = nameBuilder.toString();
//        ListRequest request = new ListRequest();
//        String url = NetdiscUtil.BASE_URL + "addGroupshare";
//        HttpPost share = new HttpPost(url);
//        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
////        HttpRequestQueue.URLEncode("新建文件夹")
//        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
//        builder.addTextBody("managers", assistant, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        builder.addTextBody("infoid", fileid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        builder.addTextBody("dirid", folderid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        builder.addTextBody("userid", userid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        builder.addTextBody("infoname", name, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        share.setEntity(builder.build());
//        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
//            @Override
//            public void onSuccess(Object data, RequestCategory request) {
//                HcDialog.deleteProgressDialog();
//                finish();
//            }
//
//            @Override
//            public String getTag() {
//                return null;
//            }
//
//            @Override
//            public void onAccountExcluded(String data, String msg, RequestCategory category) {
//                HcDialog.deleteProgressDialog();
//            }
//
//            @Override
//            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
//
//            }
//        }, false);


        BuileRequest buileRequest = new BuileRequest(assistant, fileid, folderid, userid, name);
        String url = NetdiscUtil.BASE_IP + "/clouddiskM-webapp/api/clouddisk/addGroupshare?managers=" + assistant + "&infoid=" + fileid + "&dirid=" + folderid + "&userid=" + userid + "&infoname=" + name;
        buileRequest.sendRequestCommand(url, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                finish();
            }

            @Override
            public String getTag() {
                return null;
            }

            @Override
            public void onAccountExcluded(String data, String msg, RequestCategory category) {
                HcDialog.deleteProgressDialog();
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }

    private class BuileRequest extends AbstractHttpRequest {
        private static final String TAG = BuildGroupActivity.TAG + "BuileRequest";
        Map<String, String> httpparams = new HashMap<String, String>();

        public BuileRequest(String assistant, String fileid, String folderid, String userid, String name) {
            httpparams.put("managers", assistant);
            httpparams.put("infoid", fileid);
            httpparams.put("dirid", folderid);
            httpparams.put("userid", userid);
            httpparams.put("infoname", name);
        }

        public String getRequestMethod() {
            return "addGroupshare";
        }

        @Override
        public String getParameterUrl() {
            String stuxx = "";
            try {
                stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stuxx;
        }

    }

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = BuildGroupActivity.TAG + "ListRequest";

        public ListRequest() {

        }

        public String getRequestMethod() {
            return "clouddiskM-webapp/api/clouddisk/deletefile";
        }

        @Override
        public String getParameterUrl() {

            return "";
        }

    }
}