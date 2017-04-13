package com.android.hcframe.netdisc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.util.NetdiscUtil;
import com.android.hcframe.netdisc.util.PubShareDialog;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

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
 * 人员列表
 */
public class PersonnelActivity extends HcBaseActivity {
    private static final String TAG = "PersonnelActivity";
    private TopBarView mTopBarView;
    private GridView mDiscSearchLv;
    String infoid;
    String type;
    private Handler mHandler = new Handler();
    List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
    PersonnelAdapter workAdapter;
    String userIds = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_personnel);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        infoid = getIntent().getStringExtra("infoid");
        type = getIntent().getStringExtra("type");
        mTopBarView.setTitle("成员管理");
        mDiscSearchLv = (GridView) findViewById(R.id.netdisc_search_lv);
        HcDialog.showProgressDialog(PersonnelActivity.this, "获取数据中");
        initDate();
        // 绑定listView的监听器
        mDiscSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int key,
                                    long arg3) {
                JSONObject jsonObject = (JSONObject) adapter.getAdapter().getItem(key);
                String type = jsonObject.optString("userroleType");
                if ("0".equals(type)) {
                    userIds = "";
                    StringBuilder userBuilder = new StringBuilder();
                    for (int i = 0; i < jsonObjectList.size(); i++) {
                        JSONObject jsonObject1 = jsonObjectList.get(i);
                        if (jsonObject1.optString("oldUserid") != null && !"".equals(jsonObject1.optString("oldUserid"))) {
                            userBuilder.append(jsonObject1.optString("oldUserid") + ",");
                        }
                    }
                    userIds = userBuilder.toString();
                    if (TextUtils.isEmpty(userIds)) {
                        userIds = "";
                    } else {
                        userIds = userIds.substring(0, userIds.length() - 1);
                    }
                    Intent intent = new Intent(PersonnelActivity.this, ChoosePersonnelActivity.class);
                    intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                    intent.putExtra("userIds", userIds);
                    startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
                } else {
                    personPowerDialog(key, jsonObject.optString("username"), jsonObject.optString("userroleType"), jsonObject.optString("userid"));
                }


            }
        });
    }

    private class ListRequest extends AbstractHttpRequest {

        private static final String TAG = PersonnelActivity.TAG + "ListRequest";

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

    private void initDate() {
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "getGroupUserList";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                userIds = "";
                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    JSONArray jsonArray = jsonObject.optJSONArray("userList");
                    JSONObject js = new JSONObject();
                    js.put("userroleType", "0");
                    js.put("username", "");
                    jsonObjectList.add(js);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObjectList.add((JSONObject) jsonArray.get(i));
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            workAdapter = new PersonnelAdapter(PersonnelActivity.this, jsonObjectList);
                            mDiscSearchLv.setAdapter(workAdapter);
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
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
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

    private Dialog personDialog;
    public LinearLayout netdisc_id_ll_delete, netdisc_id_pri_share;
    private TextView netdisc_id_tv_name, netdisc_id_tv_zhuli, netdisc_id_tv_zuyuan, netdisc_id_tv_fangke;

    /**
     * 弹出dialog
     *
     * @param name
     */
    private void personPowerDialog(final int position, String name, final String type, final String userid) {
        if (personDialog == null) {
            personDialog = new Dialog(PersonnelActivity.this, R.style.CustomAlterDialog);
            LayoutInflater inflater = LayoutInflater.from(PersonnelActivity.this);
            View view = inflater.inflate(R.layout.netdisc_alter_dialog_personpower, null);
            personDialog.setContentView(view);
            Window win = personDialog.getWindow();
            win.getAttributes().gravity = Gravity.CENTER;
            netdisc_id_tv_name = (TextView) personDialog.findViewById(R.id.netdisc_id_tv_name);
            netdisc_id_tv_zhuli = (TextView) personDialog.findViewById(R.id.netdisc_id_tv_zhuli);
            netdisc_id_tv_zuyuan = (TextView) personDialog.findViewById(R.id.netdisc_id_tv_zuyuan);
            netdisc_id_tv_fangke = (TextView) personDialog.findViewById(R.id.netdisc_id_tv_fangke);
            personDialog.findViewById(R.id.netdisc_id_iv_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    personDialog.dismiss();
                    personDialog = null;
                }
            });
            netdisc_id_tv_name.setText(name);
            if ("2".equals(type)) {
                transformred(netdisc_id_tv_zhuli);
                transformgray(netdisc_id_tv_zuyuan);
                transformgray(netdisc_id_tv_fangke);
            } else if ("3".equals(type)) {
                transformred(netdisc_id_tv_zuyuan);
                transformgray(netdisc_id_tv_zhuli);
                transformgray(netdisc_id_tv_fangke);
            } else if ("4".equals(type)) {
                transformred(netdisc_id_tv_fangke);
                transformgray(netdisc_id_tv_zhuli);
                transformgray(netdisc_id_tv_zuyuan);
            }
            netdisc_id_ll_delete = (LinearLayout) personDialog.findViewById(R.id.netdisc_id_ll_delete);
            netdisc_id_pri_share = (LinearLayout) personDialog.findViewById(R.id.netdisc_id_pri_share);
            netdisc_id_tv_zhuli.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    httppower("2", userid, position);
                }
            });
            netdisc_id_tv_zuyuan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    httppower("3", userid, position);
                }
            });
            netdisc_id_tv_fangke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    httppower("4", userid, position);
                }
            });
            netdisc_id_ll_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deletePerson(userid, position);
                    personDialog.dismiss();
                    personDialog = null;
                }
            });

            personDialog.show();
        } else {
            personDialog.dismiss();
            personDialog = null;
        }

    }

    public void deletePerson(String userid, final int position) {
//        HcDialog.showProgressDialog(PersonnelActivity.this, "提交数据");
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "removeUser";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("userid", userid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("infoid", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        jsonObjectList.remove(position);
                        workAdapter.notifyDataSetChanged();
                    }
                });
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

    public void httppower(final String userroleType, String userid, final int position) {
//        HcDialog.showProgressDialog(PersonnelActivity.this, "提交数据");
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "giveUserRole";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("userid", userid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("infoid", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("userroleType", userroleType, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if ("2".equals(userroleType)) {
                            transformred(netdisc_id_tv_zhuli);
                            transformgray(netdisc_id_tv_zuyuan);
                            transformgray(netdisc_id_tv_fangke);
                        } else if ("3".equals(userroleType)) {
                            transformred(netdisc_id_tv_zuyuan);
                            transformgray(netdisc_id_tv_zhuli);
                            transformgray(netdisc_id_tv_fangke);
                        } else if ("4".equals(userroleType)) {
                            transformred(netdisc_id_tv_fangke);
                            transformgray(netdisc_id_tv_zhuli);
                            transformgray(netdisc_id_tv_zuyuan);
                        }

                        JSONObject jsonObject = jsonObjectList.get(position);
                        try {
                            jsonObject.put("userroleType", userroleType);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        workAdapter.notifyDataSetChanged();
                    }
                });
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


    public void transformgray(TextView view) {
        view.setTextColor(getResources().getColor(R.color.netdisc_99_gray));
        view.setBackgroundResource(R.drawable.shape_circle_gray_44);
    }

    public void transformred(TextView view) {
        view.setTextColor(getResources().getColor(R.color.netdisc_write));
        view.setBackgroundResource(R.drawable.shape_circle_red_44);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ArrayList<ItemInfo> mSelected = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
            if (mSelected != null && mSelected.size() > 0) {
                submit(mSelected);

            }
        }
    }

    private void submit(ArrayList<ItemInfo> mSelected) {
        HcDialog.showProgressDialog(PersonnelActivity.this, "添加成员");
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
        ListRequest request = new ListRequest();
        String url = NetdiscUtil.BASE_URL + "addGroupUser";
        HttpPost share = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.addTextBody("infoid", infoid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        builder.addTextBody("userid", userid, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
        share.setEntity(builder.build());
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcDialog.deleteProgressDialog();
                jsonObjectList.clear();
                userIds = "";
                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    JSONArray jsonArray = jsonObject.optJSONArray("userList");
                    JSONObject js = new JSONObject();
                    js.put("userroleType", "0");
                    js.put("username", "");
                    jsonObjectList.add(js);
                    StringBuilder userBuilder = new StringBuilder();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        userBuilder.append(jsonObject1.optString("oldUserid") + ",");
//                        userIds = userIds + jsonObject1.optString("oldUserid") + ",";
                        jsonObjectList.add((JSONObject) jsonArray.get(i));
                    }
                    userIds = userBuilder.toString();
                    if (TextUtils.isEmpty(userIds)) {
                        userIds = "";
                    } else {
                        userIds = userIds.substring(0, userIds.length() - 1);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (workAdapter != null) {
                                workAdapter.notifyDataSetChanged();
                            } else {
                                workAdapter = new PersonnelAdapter(PersonnelActivity.this, jsonObjectList);
                                mDiscSearchLv.setAdapter(workAdapter);
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
            }

            @Override
            public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

            }
        }, false);
    }
}