package com.android.hcframe.menu;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.android.hcframe.BaseWebChromeClient;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;

import java.io.File;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-5-26 11:28.
 */
public class HcWebChromeClient extends BaseWebChromeClient {

    private static final String TAG = "HcWebChromeClient";

    private final static String IMAGE_MIME_TYPE = "image/*";
    private final static String VIDEO_MIME_TYPE = "video/*";
    private final static String AUDIO_MIME_TYPE = "audio/*";

    private final static String FILE_PROVIDER_AUTHORITY = "com.android.browser-classic.file";


    private ValueCallback<Uri> mUploadMessage;

    private ValueCallback<Uri[]> mUploadCallbackAboveFive;

    private FileChooserParams mParams;
    private Uri mCapturedMedia;

    private Activity mActivity;

    public HcWebChromeClient(Activity context) {
        mActivity = context;
    }

    // For Android < 3.0
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "", "");
        HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!<3.0 uploadMsg = " + uploadMsg);
    }

    // For Android 3.0+
    @Override
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, "");
        HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!>3.0 uploadMsg = " + uploadMsg + " acceptType=" + acceptType);
    }

    //For Android 4.1
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        HcLog.D(TAG + " #openFileChooser!!!!!!!!!!!!!!!!>4.1 uploadMsg = " + uploadMsg + " acceptType="+acceptType + " capture="+capture);
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
//			i.setType("image/*");
        i.setType("*/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
    }
    //For Android 5.0+ FileChooserParams 当用的api > 5.0时才会有,当前用的<5.0
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
        HcLog.D(TAG + " #onShowFileChooser valueCallback ="+valueCallback + " FileChooserParams ="+fileChooserParams + " ValueCallback = "+mUploadCallbackAboveFive
            + " sdk version = "+Build.VERSION.SDK_INT);
        if (mUploadCallbackAboveFive != null) {
            // Already a file picker operation in progress.
            return true;
        }
        mUploadCallbackAboveFive = valueCallback;
        mParams = fileChooserParams;
        Intent[] captureIntents = createCaptureIntent();
        HcLog.D(TAG + " #onShowFileChooser captureIntents = "+captureIntents);
//        assert(captureIntents != null && captureIntents.length > 0);
        Intent intent = null;
        // Go to the media capture directly if capture is specified, this is the
        // preferred way.
        if (Build.VERSION.SDK_INT >= 21) {
            if (fileChooserParams.isCaptureEnabled() && captureIntents.length == 1) {
                intent = captureIntents[0];
            } else {
                intent = new Intent(Intent.ACTION_CHOOSER);
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, captureIntents);
                intent.putExtra(Intent.EXTRA_INTENT, fileChooserParams.createIntent());
            }
        } else {
            intent = new Intent(Intent.ACTION_CHOOSER);
            intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, captureIntents);
        }

        mActivity.startActivityForResult(intent, FILECHOOSER_RESULTCODE);

        // 用下面的不能出现选择拍照的功能
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("*/*");
//        mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

        return true;
    }

    @Override
    public void onActivityResult(int resultCode, Intent data) {
        HcLog.D(TAG + " #onActivityResult sdk version = "+Build.VERSION.SDK_INT + " resultCode ="+resultCode + " intent data = "+data);
        if (Build.VERSION.SDK_INT < 21) { // sdk < 5.0
            if (null == mUploadMessage) {
                return;
            }
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null
                    : data.getData();
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else {
            if (null == mUploadCallbackAboveFive) {
                return;
            }
//            Uri[] results = null;
//            if (resultCode == Activity.RESULT_OK) {
//                if (data != null) {
//                    String dataString = data.getDataString();
//                    ClipData clipData = data.getClipData();
//                    if (clipData != null) {
//                        int itemCount = clipData.getItemCount();
//                        results = new Uri[itemCount];
//                        for (int i = 0; i < itemCount; i++) {
//                            ClipData.Item item = clipData.getItemAt(i);
//                            results[i] = item.getUri();
//                        }
//                    }
//                    if (dataString != null) {
//                        results = new Uri[]{Uri.parse(dataString)};
//                    }
//                }
//            }
//            mUploadCallbackAboveFive.onReceiveValue(results);
//            mUploadCallbackAboveFive = null;
            Uri[] uris;
            // As the media capture is always supported, we can't use
            // FileChooserParams.parseResult().
            uris = parseResult(resultCode, data);
            mUploadCallbackAboveFive.onReceiveValue(uris);
            mUploadCallbackAboveFive = null;
            return;
        }
    }

    private Intent[] createCaptureIntent() {
        String mimeType = "*/*";
        String[] acceptTypes = null;
        if (Build.VERSION.SDK_INT >= 21)
            acceptTypes = mParams.getAcceptTypes();
        if ( acceptTypes != null && acceptTypes.length > 0) {
            for (String s : acceptTypes) {
                HcLog.D(TAG + " #createCaptureIntent acceptType = "+s);
            }
            mimeType = acceptTypes[0];
        }
        HcLog.D(TAG + " #createCaptureIntent mimeType = "+mimeType + " acceptTypes ="+acceptTypes);
        if (TextUtils.isEmpty(mimeType))
            mimeType = "*/*";
        HcLog.D(TAG + " #createCaptureIntent after setting mimeType = "+mimeType + " acceptTypes ="+acceptTypes);

        Intent[] intents;
        if (mimeType.equals(IMAGE_MIME_TYPE)) {
            intents = new Intent[1];
            intents[0] = createCameraIntent(createTempFileContentUri(".jpg"));
        } else if (mimeType.equals(VIDEO_MIME_TYPE)) {
            intents = new Intent[1];
            intents[0] = createCamcorderIntent();
        } else if (mimeType.equals(AUDIO_MIME_TYPE)) {
            intents = new Intent[1];
            intents[0] = createSoundRecorderIntent();
        } else {
            intents = new Intent[3];
            intents[0] = createCameraIntent(createTempFileContentUri(".jpg"));
            intents[1] = createCamcorderIntent();
            intents[2] = createSoundRecorderIntent();
        }
        return intents;
    }

    private Uri createTempFileContentUri(String suffix) {
        try {
//            File mediaPath = new File(mActivity.getFilesDir(), "captured_media");
            File mediaPath = new File(HcApplication.getImagePhotoPath(), "captured_media");
            if (!mediaPath.exists() && !mediaPath.mkdir()) {
                HcLog.D(TAG + " #createTempFileContentUri Folder cannot be created file = "+mActivity.getFilesDir());
                throw new RuntimeException("Folder cannot be created.");
            }
            HcLog.D(TAG + " #createTempFileContentUri after create Folder  file path = "+mediaPath.getAbsolutePath());
            File mediaFile = File.createTempFile(
                    String.valueOf(System.currentTimeMillis()), suffix, mediaPath);
            Uri uri = Uri.fromFile(mediaFile);
//            Uri uri = FileProvider.getUriForFile(mActivity,
//                    FILE_PROVIDER_AUTHORITY, mediaFile);
            HcLog.D(TAG + " #createTempFileContentUri uri = "+uri + " file path = "+mediaFile.getAbsolutePath());

            return uri;
        } catch (Exception e) {
            HcLog.D(TAG + " #createTempFileContentUri error = "+e);
            throw new RuntimeException(e);
        }
    }

    private Intent createCameraIntent(Uri contentUri) {
        if (contentUri == null) throw new IllegalArgumentException();
        mCapturedMedia = contentUri;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedMedia);
        if (Build.VERSION.SDK_INT >= 16)
            intent.setClipData(ClipData.newUri(mActivity.getContentResolver(),
                FILE_PROVIDER_AUTHORITY, mCapturedMedia));
        return intent;
    }

    private Intent createCamcorderIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }

    private Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

    private Uri[] parseResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return null;
        }
        Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                : intent.getData();

        // As we ask the camera to save the result of the user taking
        // a picture, the camera application does not return anything other
        // than RESULT_OK. So we need to check whether the file we expected
        // was written to disk in the in the case that we
        // did not get an intent returned but did get a RESULT_OK. If it was,
        // we assume that this result has came back from the camera.
        if (result == null && intent == null && resultCode == Activity.RESULT_OK
                && mCapturedMedia != null) {
            result = mCapturedMedia;
        }

        Uri[] uris = null;
        if (result != null) {
            uris = new Uri[1];
            uris[0] = result;
        }
        return uris;
    }
}
