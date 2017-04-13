package com.android.hcframe.zxing.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.zxing.camera.CameraManager;
import com.android.hcframe.zxing.decoding.CaptureActivityHandler;
import com.android.hcframe.zxing.decoding.InactivityTimer;
import com.android.hcframe.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

/**
 * Initial the camera
 * 扫描界面
 *
 * @author czx
 */
public class ScanLoginActivity extends AbsActiviy implements OnClickListener, IHttpResponse {


    Button id_bt_ok;
    TextView id_tv_cencle;
    private Context mContext;
    private String uuid;

    @Override
    protected void onInitView() {
        setContentView(R.layout.activity_scanlogin);
        mContext = getApplicationContext();
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        id_bt_ok = (Button) findViewById(R.id.id_bt_ok);
        id_bt_ok.setOnClickListener(this);
        id_tv_cencle = (TextView) findViewById(R.id.id_tv_cencle);
        id_tv_cencle.setOnClickListener(this);
    }

    @Override
    protected void onInitData() {
//        mTopBarView.setReturnBtnIcon(R.drawable.center_close);
        mTopBarView.setTitle(getString(R.string.modify_weblogin));
        Intent intent = getIntent();
        uuid = intent.getExtras().getString("uuid");
    }

    @Override
    protected void setPameter() {
        menuPage = "com.android.hcframe.zxing.activity.CaptureActivity";
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.id_bt_ok) {//登录
            HcHttpRequest.getRequest().sendScanLoginCommand(uuid, this);
        } else if (i == R.id.id_tv_cencle) {//取消
            finish();
        }
    }

    /**
     * 扫码验证返回值
     *
     * @param data     返回的数据
     * @param request  请求的类型
     * @param category 返回的类型
     */
    @Override
    public void notify(Object data, RequestCategory request, ResponseCategory category) {
        if (request != null) {
            switch (request) {
                case SCAN:
                    if (data != null) {
                        switch (category) {
                            case SUCCESS:
                                HcLog.D(TAG + " ontify SUCCESS callback = ");
                                finish();
                                break;
                            case REQUEST_FAILED:
                                /**
                                 * czx
                                 * 2016.4.13
                                 */
                                ResponseCodeInfo info = (ResponseCodeInfo) data;
                                if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()
                                        || HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode()
                                        ) {
                                    HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                                } else {
                                    HcUtil.showToast(mContext, info.getMsg());
                                }
                                break;
                            case NETWORK_ERROR:
                                HcUtil.toastNetworkError(mContext);
                                break;
                            case SYSTEM_ERROR:
                                HcUtil.toastSystemError(mContext, data);
                                break;
                            case DATA_ERROR:
                                HcUtil.toastDataError(mContext);
                                break;
                            case SESSION_TIMEOUT:
                                HcUtil.toastTimeOut(mContext);
                                break;
                            default:
                                break;
                        }
                    } else {
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }
}