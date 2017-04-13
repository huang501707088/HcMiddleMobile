package com.android.hcframe.zxing.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hcframe.AbsActiviy;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.badge.AppBadgeInfo;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.badge.ModuleBadgeInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.OneBtnAlterDialog;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.android.hcframe.zxing.camera.CameraManager;
import com.android.hcframe.zxing.decoding.CaptureActivityHandler;
import com.android.hcframe.zxing.decoding.InactivityTimer;
import com.android.hcframe.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Initial the camera
 * 扫描界面
 *
 * @author czx
 */
public class CaptureActivity extends AbsActiviy implements Callback, OnClickListener, IHttpResponse {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private Context mContext;
    private String uuid;

    @Override
    protected void onInitView() {
        setContentView(R.layout.activity_capture);
        mContext = getApplicationContext();
        mTopBarView = (TopBarView) findViewById(R.id.center_top_bar);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onInitData() {
//        mTopBarView.setReturnBtnIcon(R.drawable.center_close);
        mTopBarView.setTitle(getString(R.string.modify_scanlogin));
    }

    @Override
    protected void setPameter() {
        menuPage = "com.android.hcframe.zxing.activity.CaptureActivity";
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 扫描失败
     */
    public void fail() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();


        if ("".equals(resultString)) {
            Toast.makeText(getApplicationContext(), "无效条码", Toast.LENGTH_SHORT).show();
            fail();
        } else {
            if (resultString.contains("?uuid=")) {
                int i = resultString.indexOf("?uuid=");
                uuid = resultString.substring(i + 6);
            }
            if (TextUtils.isEmpty(uuid)) {
                OneBtnAlterDialog(this, resultString);
            } else {
                HcHttpRequest.getRequest().sendScanCommand(resultString, this);
            }

//            Intent resultIntent = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putString("result", resultString);
//            bundle.putParcelable("bitmap", barcode);
//            resultIntent.putExtras(bundle);
//            this.setResult(RESULT_OK, resultIntent);
        }
//        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };


    @Override
    public void onClick(View view) {

    }

    /**
     * 扫码返回值
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
                                Intent intent = new Intent();
                                intent.putExtra("uuid", uuid);
                                intent.setClass(this, ScanLoginActivity.class);
                                startActivity(intent);
                                finish();
                                break;
//                            case SCANLOGIN_INVALID:
//                                Toast.makeText(this, "二维码失效", Toast.LENGTH_SHORT).show();
//                                fail();
//                                break;
                            case REQUEST_FAILED:
                                /**
                                 * czx
                                 * 2016.4.13
                                 */
                                ResponseCodeInfo info = (ResponseCodeInfo) data;
                                if (HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()
                                        || HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode()
                                        ) {
                                    HcUtil.reLogining(info.getBodyData(), this, info.getMsg());
                                } else {
                                    HcUtil.showToast(this, info.getMsg());
                                }
                                break;
                            case NETWORK_ERROR:
                                HcUtil.toastNetworkError(mContext);
                                fail();
                                break;
                            case SYSTEM_ERROR:
                                HcUtil.toastSystemError(mContext, data);
                                fail();
                                break;
                            case DATA_ERROR:
                                HcUtil.toastDataError(mContext);
                                fail();
                                break;
                            case SESSION_TIMEOUT:
                                HcUtil.toastTimeOut(mContext);
                                break;
                            default:
                                fail();
                                break;
                        }
                    } else {
                    }
                    break;
                default:
                    fail();
                    break;
            }
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {

    }

    private static OneBtnAlterDialog alterDialog;

    /**
     * 弹出dialog
     *
     * @param context activity实例
     * @param msg     提示消息体
     */
    public static void OneBtnAlterDialog(final Context context, String msg) {
        if (alterDialog == null) {
            alterDialog = OneBtnAlterDialog.createDialog(context, msg);
            OneBtnAlterDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
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
}