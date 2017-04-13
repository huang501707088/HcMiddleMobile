package com.android.hcframe.hctask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.hctask.state.TaskState;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.view.selector.DepInfo;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-10 11:22.
 */
public class ChoosePersonnelActivity extends HcBaseActivity implements HcChooseHomeView.OnOperatorListener {

    private static final String TAG = "ChoosePersonnelActivity";

    private HcChooseHomeView mChooseView;

    private FrameLayout mParent;

    private ChooseObserver mObserver;

    private List<ItemInfo> mItemInfoList = new ArrayList<ItemInfo>();
    /**
     * 部门id
     */
    private String mDeptId = "";

    private DempListResponse dResponse;

    private String mMd5Url;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_choose_personnel);
        mParent = (FrameLayout) findViewById(R.id.task_choose_personnel_parent);
        mChooseView = new HcChooseHomeView(this, mParent, false, this, "组员选择", "我的下属");
        mChooseView.changePages();
        mObserver = new ChooseObserver();
        mObserver.addObserver(mChooseView);
    }

    @Override
    public void onParentItemClick(ItemInfo info) {
        HcLog.D(TAG + " #onParentItemClick info id = " + info.getItemId());
        if (HcUtil.isNetWorkAvailable(ChoosePersonnelActivity.this)) {
            DempListRequest dRequest = new DempListRequest(mDeptId);
            if (dResponse == null) {
                dResponse = new DempListResponse();
            }
            HcDialog.showProgressDialog(this, "获取数据列表");
            dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
        }
    }

    @Override
    public void onRefresh(ItemInfo info) {
        HcLog.D(TAG + " #onRefresh info id = " + info.getItemId());
        if (HcUtil.isNetWorkAvailable(ChoosePersonnelActivity.this)) {
            DempListRequest dRequest = new DempListRequest(mDeptId);
            if (dResponse == null) {
                dResponse = new DempListResponse();
            }
            dRequest.sendRequestCommand(RequestCategory.NONE, dResponse, false);
        }
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


    private class DempListRequest extends AbstractHttpRequest {
        private static final String TAG = ChoosePersonnelActivity.TAG + "$ChoosePersonnelRequest";
        Map<String, String> httpparams = new HashMap<String, String>();


        public DempListRequest(String deptId) {
            httpparams.put("deptId", deptId);
        }

        @Override
        public String getRequestMethod() {
            return "getsubordinatedeptandemplist";
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

    private class DempListResponse extends AbstractHttpResponse {

        private static final String TAG = ChoosePersonnelActivity.TAG + "$ChoosePersonnelResponse";

        public DempListResponse() {
        }

        @Override
        public String getTag() {
            return TAG;
        }

        @Override
        public void onSuccess(Object data, RequestCategory request) {
            HcLog.D(TAG + " #onSuccess data = " + data + " request = " + request);
            HcDialog.deleteProgressDialog();
            mItemInfoList.clear();
            try {
                JSONObject object = new JSONObject((String) data);
                JSONArray jsonDeptArray = object.getJSONArray("deptList");
                int jsonDeptArrayNum = jsonDeptArray.length();
                if (jsonDeptArrayNum > 0) {
                    for (int i = 0; i < jsonDeptArrayNum; i++) {
                        ItemInfo itemInfo = new DepInfo();
                        String mDeptName = "";
                        JSONObject jsonDeptObj = jsonDeptArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonDeptObj, "deptId")) {
                            mDeptId = jsonDeptObj.getString("deptId");
                            itemInfo.setItemId(mDeptId);
                        }
                        if (HcUtil.hasValue(jsonDeptObj, "deptName")) {
                            mDeptName = jsonDeptObj.getString("deptName");
                            itemInfo.setItemValue(mDeptName);
                        }
                        mItemInfoList.add(itemInfo);
                    }
                }

                JSONArray jsonUserArray = object.getJSONArray("userList");
                int jsonUserArrayNum = jsonUserArray.length();
                if (jsonUserArrayNum > 0) {
                    for (int i = 0; i < jsonUserArrayNum; i++) {
                        ItemInfo itemInfo = new StaffInfo();
                        itemInfo.setMultipled(false);
                        String mUserId = "", mUserName = "", mUserImg = "", mDeptId = "";
                        JSONObject jsonUserObj = jsonUserArray.getJSONObject(i);
                        if (HcUtil.hasValue(jsonUserObj, "userId")) {
                            mUserId = jsonUserObj.getString("userId");
                            itemInfo.setUserId(mUserId);
                        }
                        if (HcUtil.hasValue(jsonUserObj, "userName")) {
                            mUserName = jsonUserObj.getString("userName");
                            itemInfo.setItemValue(mUserName);
                        }
                        if (HcUtil.hasValue(jsonUserObj, "userImg")) {
                            mUserImg = jsonUserObj.getString("userImg");
                            itemInfo.setIconUrl(mUserImg);
                        }
                        mItemInfoList.add(itemInfo);
                    }
                }

            } catch (JSONException e) {
                HcLog.D(TAG + " #onSuccess parseJson error =" + e);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mObserver != null)
                        mObserver.notifyData(mItemInfoList);
                }
            });
        }

        @Override
        public void onAccountExcluded(String data, String msg, RequestCategory category) {
            HcDialog.deleteProgressDialog();
            HcUtil.reLogining(data, ChoosePersonnelActivity.this, msg);
        }

        @Override
        public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
            //记录md5，用url去取消
            mMd5Url = md5Url;
        }

        public void onNetworkInterrupt(RequestCategory request) {
            super.onNetworkInterrupt(request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void onConnectionTimeout(RequestCategory request) {
            super.onConnectionTimeout(request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void onParseDataError(RequestCategory request) {
            super.onParseDataError(request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void onRequestFailed(int code, final String msg, RequestCategory request) {
            super.onRequestFailed(code,msg,request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void onResponseFailed(int code, RequestCategory request) {
            super.onResponseFailed(code,request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void unknown(RequestCategory request) {
            super.unknown(request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }

        public void onRequestCanel(RequestCategory request) {
            super.onRequestCanel(request);
            if (mObserver != null)
                mObserver.notifyData(null);
        }
    }

    @Override
    public void onCanelRefreshRequest(ItemInfo info) {
        if (mMd5Url != null){
            HttpRequestQueue.getInstance().cancelRequest(mMd5Url);
            mMd5Url = null;
        }
    }

}


