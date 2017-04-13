package com.android.hcframe.netdisc;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.AbstractHttpRequest;
import com.android.hcframe.http.AbstractHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.netdisc.util.DiscInfoRingView;
import com.android.hcframe.netdisc.util.NetdiscUtil;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 网盘信息
 * Created by pc on 2016/8/8.
 */
public class DiscInfoActivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "DiscInfoActivity";
    private TopBarView mTopBarView;
    private Handler mHandler = new Handler();
    long shapespace, userspace, totalspace;
    DiscInfoRingView discInfoRingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netdisc_disc_info_layout);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        mTopBarView.setTitle("网盘信息");
        discInfoRingView= (DiscInfoRingView) findViewById(R.id.id_disc_info);
        initData();
    }

    private void initData() {
        HcDialog.showProgressDialog(this, "数据加载中...");
        Request request = new Request();
        String url = NetdiscUtil.BASE_URL + "getDiskInfo";
        HttpPost share = new HttpPost(url);
        request.sendRequestCommand(url, share, RequestCategory.NONE, new AbstractHttpResponse() {
            @Override
            public void onSuccess(Object data, RequestCategory request) {
                HcLog.D(TAG + "###########################");
                HcDialog.deleteProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(data.toString());
                    shapespace = jsonObject.optInt("sharespace");
                    userspace = jsonObject.optInt("userspace");
                    totalspace = jsonObject.optInt("totalspace");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
//                            initView();
                            discInfoRingView.setInfo(shapespace,userspace,totalspace);
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


    private class Request extends AbstractHttpRequest {

        @Override
        public String getRequestMethod() {
            return null;
        }

        @Override
        public String getParameterUrl() {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

    }
}
