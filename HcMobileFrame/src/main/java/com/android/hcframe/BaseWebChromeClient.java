package com.android.hcframe;

import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-5-12 10:22.
 */
public abstract class BaseWebChromeClient extends WebChromeClient {

    public static final int FILECHOOSER_RESULTCODE = 10000;

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {

    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {

    }

    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

    }
    //For Android 5.0+ FileChooserParams 当用的api > 5.0时才会有,当前用的<5.0
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
        return true;
    }

    public void onActivityResult(int resultCode, Intent data) {

    }
}
