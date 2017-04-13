/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2014-12-3 上午11:49:10
 */
package com.android.hcframe.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleInfo;
import com.android.hcframe.data.DownlaodAppInfo;
import com.android.hcframe.data.HcNewsData;
import com.android.hcframe.data.NewsColumn;
import com.android.hcframe.data.NewsInfo;
import com.android.hcframe.data.NewsPageData;
import com.android.hcframe.doc.data.DocColumn;
import com.android.hcframe.doc.data.DocFileInfo;
import com.android.hcframe.doc.data.DocInfo;
import com.android.hcframe.doc.data.SearchDocInfo;
import com.android.hcframe.monitor.OperationLogInfo;
import com.android.hcframe.servicemarket.photoscan.NewsDetailsInfo;
import com.android.hcframe.servicemarket.photoscan.PicInfo;
import com.android.hcframe.sql.SettingHelper;

public final class HcHttpRequest {

    private static final String TAG = "HcHttpRequest";

    private static HcHttpRequest mRequest = new HcHttpRequest();

    private AbstractHcHttpClient mClient;

    public static final String BASE_URL = "/terminalServer/szf/";

    private static final String STATUS = "code";
    private static final String BODY = "body";

    /**
     * 返回成功
     */
    private static final int REQUEST_SUCCESS = 0;// RequestCategory.SUCCESS.ordinal();
    /**
     * 帐号无效
     */
    private static final int REQUEST_ACCOUNT_INVALID = 1;// RequestCategory.ACCOUNT_INVALID.ordinal();
    /**
     * 认证失败
     */
    private static final int REQUEST_AUTHENTICATION_FAILED = 2;// RequestCategory.AUTHENTICATION_FAILED.ordinal();
    /**
     * 帐号已存在
     */
    private static final int REQUEST_AUCCONT_EXISTS = 3;// RequestCategory.AUCCONT_EXISTS.ordinal();
    /**
     * 密码错误
     */
    private static final int REQUEST_PASSWORD_ERROR = 4;// RequestCategory.SESSION_TIMEOUT.ordinal();
    /**
     * 用户未登录
     */
    private static final int REQUEST_USER_NOT_LOGIN = 5;// RequestCategory.USER_NOT_LOGIN.ordinal();
    /**
     * 系统错误
     */
    private static final int REQUEST_SYSTEM_ERROR = 99;
    /**
     * TOKEN失效
     */
    public static final int REQUEST_TOKEN_FAILED = 101;
    /**
     * 帐号在其他地方登录，被剔除
     */
    public static final int REQUEST_ACCOUT_EXCLUDED = 102;

    /**
     * 摇奖超时
     */
    public static final int REQUEST_ANNUAL_SHAKE_ITMEOUT = 902;

    private Timer mTimer = new Timer("request");

    private static final int TIME_OUT = 35 * 1000;

    // private Map<AbstractHttp, TimerTask> mTaskMap = new HashMap<AbstractHttp,
    // TimerTask>();

    /**
     * key:请求的URL的MD5格式 value:超时处理的Task
     */
    private Map<String, TimerTask> mTaskMap = new HashMap<String, TimerTask>();

    /**
     * @author jrjin
     * @date 2015-12-5 下午4:59:32 最大请求次数
     */
    private static final int REQUEST_MAX_COUNT = 3;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (msg.obj == null || !(msg.obj instanceof AbstractHttp))
                return;
            int what = msg.what;
            AbstractHttp http = (AbstractHttp) msg.obj;
            if (http.shutDown()) return; // 已经主动退出请求
            switch (what) {
                case 402:
                    /**
                     * @author jrjin
                     * @date 2015-12-5 下午4:33:09 增加了重复请求的次数，减少容错率
                     */
                    if (http.mRequestCount < REQUEST_MAX_COUNT) {
                        http.mRequestCount = http.mRequestCount + 1;
                        mClient.execute(http);
                        HcLog.D(TAG + " handleMessage  http = " + http + " response = " + http.mResponse);
                    } else { // 已经超过设定的请求次数了
//					HcUtil.toastNetworkError(HcApplication.getContext());
                        cancelTask(http);
                        if (http instanceof DownloadApp) {
                            postView(((DownloadApp) http).mAppId,
                                    http.mResponse,
                                    ResponseCategory.NETWORK_ERROR,
                                    http.mCategory);
                        } else if (http instanceof NewsList) {
                            postView(((NewsList) http).mId,
                                    http.mResponse,
                                    ResponseCategory.NETWORK_ERROR,
                                    http.mCategory);
                        } else if (http instanceof NewsScrollList) {
                            postView(((NewsScrollList) http).mId,
                                    http.mResponse,
                                    ResponseCategory.NETWORK_ERROR,
                                    http.mCategory);
                        } else {
                            postView(http.mCategory, http.mResponse,
                                    ResponseCategory.NETWORK_ERROR,
                                    http.mCategory);
                        }
                        HcLog.D(TAG + " request = " + http + " response = " + http.mResponse);
                    }

                    break;
                default:
                    cancelTask(http);
                    if (http instanceof DownloadApp) {
                        postView(((DownloadApp) http).mAppId,
                                http.mResponse,
                                ResponseCategory.SYSTEM_ERROR,
                                http.mCategory);
                    } else if (http instanceof NewsList) {
                        postView(((NewsList) http).mId,
                                http.mResponse,
                                ResponseCategory.SYSTEM_ERROR,
                                http.mCategory);
                    } else if (http instanceof NewsScrollList) {
                        postView(((NewsScrollList) http).mId,
                                http.mResponse,
                                ResponseCategory.SYSTEM_ERROR,
                                http.mCategory);
                    } else {
                        /**
                         * @jrjin
                         * @date 2017.03.08
                         * 需要返回code的错误码
                         */
                        postView(what, http.mResponse,
                                ResponseCategory.SYSTEM_ERROR,
                                http.mCategory);
                    }
                    HcLog.D(TAG + " request = " + http + " response = " + http.mResponse);

                    break;
            }

        }

    };

    private HcHttpRequest() {
        mClient = new DefaultClient();
    }

    public static HcHttpRequest getRequest() {
        return mRequest;
    }

    public static String URLEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            HcLog.D("it is in URLEncode e = " + e);
        }
        return str;
    }

    private void cancelTask(AbstractHttp http) {
        synchronized (this) {
            TimerTask task = mTaskMap.remove(http.mUrl);
            if (task != null)
                task.cancel();
        }
        HcLog.D(TAG + " #cancelTask end!");
    }

    private void addTask(AbstractHttp http) {
        if (http == null)
            return;
        RequestTask task = new RequestTask(http);
        synchronized (this) {
            mTaskMap.put(http.mUrl, task);
            mTimer.schedule(task, TIME_OUT);
        }
    }

    private void postView(final Object category, final IHttpResponse respose,
                          final ResponseCategory rc, final RequestCategory rq) {
        if (respose != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    respose.notify(category, rq, rc);
                }
            });
        }
    }

    /**
     * 判断对应的key是否存在value
     *
     * @param object
     * @param tag
     * @return true:有数据；false：没有数据
     */
    private boolean hasValue(JSONObject object, String tag) {
        boolean exist = false;
        if (object != null && object.has(tag)) {
            try {
                Object object2 = object.get(tag);
                // LOG.D(TAG + " object2 = "+object2 + " tag = "+tag);
                if (object2 != null && !object2.equals("")
                        && !object.isNull(tag)) {
                    exist = true;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
        return exist;
    }

    private void toastError(int code, JSONObject object, AbstractHttp http) {
        try {
            String toast = "";
            if (hasValue(object, "msg")) {
                toast = object.getString("msg");
            }
            if (hasValue(object, "body")) {
                object = object.getJSONObject("body");
            }
            postView(new ResponseCodeInfo(code, toast, object.toString()), http.mResponse, ResponseCategory.REQUEST_FAILED,
                    http.mCategory);
        } catch (Exception e) {
            // TODO: handle exception
            postView(http.mCategory, http.mResponse, ResponseCategory.DATA_ERROR,
                    http.mCategory);
        }

    }

    /**
     * 下载图片
     *
     * @param imageUrl
     * @param response
     * @author jrjin
     * @time 2014-10-9 上午10:54:12
     */
    public void sendDownloadImage(String appId, String imageUrl,
                                  String version, IHttpResponse response) {
        String url = imageUrl;
        HcLog.D(TAG + " sendDownloadImage url = " + imageUrl);
        String key = hasInTask(url);
        if (key == null)
            return; // 说明请求的task正在执行或者url == null
        HttpGet get = new HttpGet(url);
        DownloadImage image = new DownloadImage(get, appId, version);
        image.mCategory = RequestCategory.DOWNLOAD_IMAGE;
        image.mResponse = response;
        image.mUrl = key;
        addTask(image);
        response.notifyRequestMd5Url(image.mCategory, key);
        mClient.execute(image);
    }

    /**
     * 用户登录
     *
     * @param account
     * @param pw
     * @param response
     * @author jrjin
     * @time 2014-10-14 下午4:28:08
     */
    public void sendLoginCommand(String account, String pw, String deviceid,
                                 IHttpResponse response) {

        String date = "";
        try {
            date = URLEncoder.encode(
                    HcUtil.getDate(HcUtil.FORMAT_POLLUTION,
                            new Date().getTime()), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "";
        if (!HcUtil.isEmpty(date)) {
            url = HcUtil.getScheme() + BASE_URL + Login.URL + "account="
                    + URLEncode(account) + "&password=" + HcUtil.getMD5String(pw)
                    + "&terminalId=" + deviceid + "&appId=123&operaTime="
                    + date;

            // url="http://198.10.80.10:8080/terminalServer/szf/vcheck?appid=4&sversion=android4.4.4&appver=1.0.0&ptype=1";
        }
        HcLog.D(TAG + "sendLoginCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        Login login = new Login(get, account, pw);
        login.mCategory = RequestCategory.LOGIN;
        login.mResponse = response;
        login.mUrl = key;
        addTask(login);
        response.notifyRequestMd5Url(login.mCategory, key);
        mClient.execute(login);
    }

    /**
     * 获取应用超市的应用
     *
     * @param type     系统类型 0：Android；1：IOS
     * @param rc
     * @param response
     * @author jrjin
     * @time 2015-5-15 下午3:04:09
     */
    public void sendAppListCommand(String type, RequestCategory rc,
                                   IHttpResponse response) {
        sendAppListCommand(type, "" + 0, rc, response, "" + 0);
    }

    /**
     * 获取服务超市应用
     *
     * @param rc
     * @param response
     * @author jrjin
     * @time 2015-5-15 下午3:12:34
     */
    public void sendServerAppListCommand(RequestCategory rc,
                                         IHttpResponse response) {
        sendAppListCommand("" + 0, "" + 0, rc, response, "" + 1);
    }

    public void sendAppListCommand(String type, String category,
                                   RequestCategory rc, IHttpResponse response, String marketType) {
        String url = HcUtil.getScheme() + BASE_URL + MarketAppList.URL
                + "type=" + type + "&category=0" + "&market_type=" + marketType;
        HcLog.D(TAG + " sendAppListCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        MarketAppList list = new MarketAppList(get);
        list.mCategory = rc;
        list.mResponse = response;
        list.mUrl = key;
        addTask(list);
        response.notifyRequestMd5Url(list.mCategory, key);
        mClient.execute(list);
    }

    public void sendModifyCommand(String account, String oldpwd, String newpwd,
                                  IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + Modify.URL + "account="
                + URLEncode(account) + "&passwdold=" + HcUtil.getMD5String(oldpwd)
                + "&passwdnew=" + HcUtil.getMD5String(newpwd);
        HcLog.D(TAG + "sendLoginCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        Modify modify = new Modify(get);
        modify.mCategory = RequestCategory.MODIFY;
        modify.mResponse = response;
        modify.mUrl = key;
        addTask(modify);
        response.notifyRequestMd5Url(modify.mCategory, key);
        mClient.execute(modify);
    }

    public void sendGetCodeCommand(String account, String mobile, String type,
                                   IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + GetCode.URL + "account="
                + URLEncode(account) + "&mobile=" + mobile + "&type=" + type;
        HcLog.D(TAG + "sendGetCodeCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        GetCode getCode = new GetCode(get);
        getCode.mCategory = RequestCategory.GETCODE;
        getCode.mResponse = response;
        getCode.mUrl = key;
        addTask(getCode);
        response.notifyRequestMd5Url(getCode.mCategory, key);
        mClient.execute(getCode);
    }

    public void sendCheckCodeCommand(String account, String mobile,
                                     String type, String code, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + CheckCode.URL + "account="
                + URLEncode(account) + "&mobile=" + mobile + "&code=" + code + "&type="
                + type;
        HcLog.D(TAG + "sendCheckCodeCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        CheckCode checkCode = new CheckCode(get);
        checkCode.mCategory = RequestCategory.CHECKCODE;
        checkCode.mResponse = response;
        checkCode.mUrl = key;
        addTask(checkCode);
        response.notifyRequestMd5Url(checkCode.mCategory, key);
        mClient.execute(checkCode);
    }

    public void sendRegisterCommand(String account, String mobile,
                                    String password, String code, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + Register.URL + "account="
                + URLEncode(account) + "&mobile=" + mobile + "&code=" + code
                + "&password=" + HcUtil.getMD5String(password);
        HcLog.D(TAG + "sendRegisterCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        Register register = new Register(get);
        register.mCategory = RequestCategory.REGISTER;
        register.mResponse = response;
        register.mUrl = key;
        addTask(register);
        response.notifyRequestMd5Url(register.mCategory, key);
        mClient.execute(register);
    }

    public void sendCheckAppVersionCommand(String appver, String ptype,
                                           String imei, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + CheckAppVersion.URL
                + "appver=" + appver + "&ptype=" + ptype + "&imei=" + imei;
        HcLog.D(TAG + "  sendCheckAppVersionCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        CheckAppVersion checkappv = new CheckAppVersion(get);
        checkappv.mCategory = RequestCategory.CHECKAV;
        checkappv.mResponse = response;
        checkappv.mUrl = key;
        addTask(checkappv);
        response.notifyRequestMd5Url(checkappv.mCategory, key);
        mClient.execute(checkappv);
    }

    /**
     * 角标获取
     *
     * @param start_time
     * @param response   czx
     */
    public void sendGetCornerCommand(String start_time, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + GetCorner.URL + "start_time="
                + start_time + "&versioncode="+ HcConfig.getConfig().getAppVersion()
                + "&pType=0";
        HcLog.D(TAG + " sendGetCornerCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        GetCorner getCorner = new GetCorner(get);
        getCorner.mCategory = RequestCategory.CORNER;
        getCorner.mResponse = response;
        getCorner.mUrl = key;
        addTask(getCorner);
        response.notifyRequestMd5Url(getCorner.mCategory, key);
        mClient.execute(getCorner);

    }

    private class GetCorner extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#GetCorner";

        private static final String URL = "getRemind?";

        public GetCorner(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(object.getJSONObject(BODY).toString(), mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getRemind";
        }
    }

    /**
     * 二维码扫码
     *
     * @param url      二维码校验路径
     * @param response czx
     */
    public void sendScanCommand(String url, IHttpResponse response) {
        HcLog.D(TAG + " sendScanCommand url = " + url);
        String key = hasInTask(url);
//        String key = hasInTask("http://10.80.7.88:8080/terminalServer/szf/webLoginScanQRCode?uuid=40CB85A93CCE4F74064DA2DA8C2F761E");
        if (key == null)
            return;
//        HttpGet get = new HttpGet("http://10.80.7.88:8080/terminalServer/szf/webLoginScanQRCode?uuid=40CB85A93CCE4F74064DA2DA8C2F761E");
        HttpGet get = new HttpGet(url);
        GetScan getscan = new GetScan(get);
        getscan.mCategory = RequestCategory.SCAN;
        getscan.mResponse = response;
        getscan.mUrl = key;
        addTask(getscan);
        response.notifyRequestMd5Url(getscan.mCategory, key);
        mClient.execute(getscan);

    }

    private class GetScan extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#GetScan";

        public GetScan(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView("成功", mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    }
//                    else if(status == 102){
//                        postView("失效", mResponse, ResponseCategory.SCANLOGIN_INVALID,
//                                mCategory);
//                    }
                    else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getRemind";
        }
    }

    /**
     * 二维码扫码授权登录
     *
     * @param uuid
     * @param response czx
     */
    public void sendScanLoginCommand(String uuid, IHttpResponse response) {
//        String url = "http://10.80.7.88:8080" + BASE_URL + GetScanLogin.URL + "uuid=" + uuid;
        String url = HcUtil.getScheme() + BASE_URL + GetScanLogin.URL + "uuid=" + uuid;
        HcLog.D(TAG + " sendUpdateTerStsCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        GetScanLogin getScanLogin = new GetScanLogin(get);
        getScanLogin.mCategory = RequestCategory.SCAN;
        getScanLogin.mResponse = response;
        getScanLogin.mUrl = key;
        addTask(getScanLogin);
        response.notifyRequestMd5Url(getScanLogin.mCategory, key);
        mClient.execute(getScanLogin);

    }

    private class GetScanLogin extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#GetScanLogin";

        private static final String URL = "webloginauthorize?";

        public GetScanLogin(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView("SECCESS", mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getRemind";
        }
    }

    /**
     * 获取下属列表及当天打卡信息
     *
     * @param searchDate 查询日期：yyyy-MM-dd
     * @param response   czx
     */
    public void sendWorkDetailNowCommand(String searchDate, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + GetWorkDetailNow.URL
                + "account=" + SettingHelper.getAccount(HcApplication.getContext())
                + "&searchDate=" + searchDate;
        HcLog.D(TAG + " sendWorkDetailNowCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        GetWorkDetailNow getWorkDetailNow = new GetWorkDetailNow(get);
        getWorkDetailNow.mCategory = RequestCategory.WORKDETAIL;
        getWorkDetailNow.mResponse = response;
        getWorkDetailNow.mUrl = key;
        addTask(getWorkDetailNow);
        response.notifyRequestMd5Url(getWorkDetailNow.mCategory, key);
        mClient.execute(getWorkDetailNow);

    }

    private class GetWorkDetailNow extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#GetWorkDetailNow";

        private static final String URL = "getSubordinateSignInfo?";

        public GetWorkDetailNow(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getRemind";
        }
    }

    public void sendUpdateTerStsCommand(String imei, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + UpdateTerSts.URL + "imei="
                + imei;
        HcLog.D(TAG + " sendUpdateTerStsCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        UpdateTerSts update = new UpdateTerSts(get);
        update.mCategory = RequestCategory.UPDATEAS;
        update.mResponse = response;
        update.mUrl = key;
        addTask(update);
        response.notifyRequestMd5Url(update.mCategory, key);
        mClient.execute(update);
    }

    public void sendRegetPwdCommand(String account, String mobile,
                                    String newpwd, String code, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + RegetPwd.URL + "account="
                + URLEncode(account) + "&mobile=" + mobile + "&code=" + code + "&newpwd="
                + HcUtil.getMD5String(newpwd);
        HcLog.D(TAG + " sendRegetPwdCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        RegetPwd regetPwd = new RegetPwd(get);
        regetPwd.mCategory = RequestCategory.REGETPWD;
        regetPwd.mResponse = response;
        regetPwd.mUrl = key;
        addTask(regetPwd);
        response.notifyRequestMd5Url(regetPwd.mCategory, key);
        mClient.execute(regetPwd);
    }

    public void sendNicknameCommand(String account, String realName,
                                    IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + ModifyNickname.URL
                + "account=" + URLEncode(account) + "&realName=" + URLEncode(realName);
        HcLog.D(TAG + " sendNicknameCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        ModifyNickname nickname = new ModifyNickname(get);
        nickname.mCategory = RequestCategory.NICKNAME;
        nickname.mResponse = response;
        nickname.mUrl = key;
        addTask(nickname);
        response.notifyRequestMd5Url(nickname.mCategory, key);
        mClient.execute(nickname);
    }

    public void sendBindPhoneCommand(String account, String oldmobile,
                                     String newmobile, String oldcode, String newcode,
                                     IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + ModifyBindPhone.URL
                + "account=" + URLEncode(account) + "&oldmobile=" + oldmobile
                + "&newmobile=" + newmobile + "&oldcode=" + oldcode
                + "&newcode=" + newcode;
        HcLog.D(TAG + " sendBindPhoneCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        ModifyBindPhone bindphone = new ModifyBindPhone(get);
        bindphone.mCategory = RequestCategory.BINDP;
        bindphone.mResponse = response;
        bindphone.mUrl = key;
        addTask(bindphone);
        response.notifyRequestMd5Url(bindphone.mCategory, key);
        mClient.execute(bindphone);
    }

    private class Modify extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#Modify";

        private static final String URL = "passwdedit?";

        public Modify(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "passwdedit";
        }

    }

    private class GetCode extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#GetCode";

        private static final String URL = "getCode?";

        public GetCode(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getCode";
        }

    }

    private class CheckCode extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#CheckCode";

        private static final String URL = "checkCode?";

        public CheckCode(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "checkCode";
        }
    }

    private class Register extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#Register";

        private static final String URL = "registere?";

        public Register(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "registere";
        }

    }

    private class RegetPwd extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#RegetPwd";

        private static final String URL = "regetPwd?";

        public RegetPwd(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "regetPwd";
        }
    }

    private class CheckAppVersion extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#CheckAppVersion";

        private static final String URL = "vcheck?";

        public CheckAppVersion(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(object.getJSONObject(BODY).toString(), mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "vcheck";
        }
    }

    private class UpdateTerSts extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#updateTerminal";

        private static final String URL = "updateTerminal?";

        public UpdateTerSts(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "updateTerminal";
        }
    }

    private class ModifyNickname extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#ModifyNickname";

        private static final String URL = "updateRealName?";

        public ModifyNickname(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "updateRealName";
        }
    }

    private class ModifyBindPhone extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#ModifyBindPhone";

        private static final String URL = "updateMobile?";

        public ModifyBindPhone(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "ModifyBindPhone";
        }
    }

    public void downloadApp(String appId, String url, IHttpResponse response) {
        HcLog.D("HcHttpRequest downloadApp url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        DownloadApp apps = new DownloadApp(get, appId);
        apps.mCategory = RequestCategory.DOWNLOAD_APP;
        apps.mResponse = response;
        apps.mUrl = key;
        addTask(apps);
        response.notifyRequestMd5Url(apps.mCategory, key);
        mClient.execute(apps);
    }

    public void sendLogoutCommand(IHttpResponse response, String terminalId) {
        String url = HcUtil.getScheme() + BASE_URL + Logout.URL + "terminalId=" + terminalId;
                //+ URLEncode(terminalId);
        HcLog.D("HcHttpRequest sendLogoutCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        Logout logout = new Logout(get);
        logout.mCategory = RequestCategory.LOGOUT;
        logout.mResponse = response;
        logout.mUrl = key;
        addTask(logout);
        response.notifyRequestMd5Url(logout.mCategory, key);
        mClient.execute(logout);
    }

    public void sendDownPdfCommand(String url, IHttpResponse response) {
        String urll = url;
        if (!urll.contains("http")) {
            urll = HcUtil.getScheme() + "/terminalServer" + url;
        }

        String key = hasInTask(urll);
        if (key == null)
            return;

        HttpGet get = new HttpGet(urll);
        DownloadPdf pdf = new DownloadPdf(get);
        pdf.mCategory = RequestCategory.DOWNLOAD_PDF;
        pdf.mResponse = response;
        pdf.mUrl = key;
        addTask(pdf);
        response.notifyRequestMd5Url(pdf.mCategory, key);
        mClient.execute(pdf);
    }

    public void sendDownGifCommand(String url, IHttpResponse response) {
        String urll = url;
        if (!urll.contains("http")) {
            urll = HcUtil.getScheme() + "/terminalServer" + url;
        }

        String key = hasInTask(urll);
        if (key == null)
            return;

        HttpGet get = new HttpGet(urll);
        DownloadGif pdf = new DownloadGif(get);
        pdf.mCategory = RequestCategory.DOWNLOAD_GIF;
        pdf.mResponse = response;
        pdf.mUrl = key;
        addTask(pdf);
        response.notifyRequestMd5Url(pdf.mCategory, key);
        mClient.execute(pdf);
    }

    public void sendNewsColumnCommand(IHttpResponse response, String moduleId) {
        String url = HcUtil.getScheme() + BASE_URL + NewsCum.NewsCumUrl;
        HcLog.D("HcHttpRequest sendNewsColumnCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        NewsCum newsColumn = new NewsCum(get, moduleId);
        newsColumn.mCategory = RequestCategory.NEWSCOLUMN;
        newsColumn.mResponse = response;
        addTask(newsColumn);
        response.notifyRequestMd5Url(newsColumn.mCategory, key);
        mClient.execute(newsColumn);
    }


    public void sendSignCommand(String singType, String flag, String longitude,
                                String latitude, IHttpResponse response) {
        sendSignCommand(singType, flag, longitude, latitude, "", response);
    }

    public void sendSignCommand(String singType, String flag, String longitude,
                                String latitude, /*String type, */String address, IHttpResponse response) {
        Map<String, String> httpparams = new HashMap<String, String>();
        httpparams.put("signType", singType);
        httpparams.put("signFlag", flag);
        httpparams.put("addressLongitude", longitude);
        httpparams.put("addressLatitude", latitude);
//        httpparams.put("type", type);
        httpparams.put("address", address);
        httpparams.put("account",
                URLEncode(SettingHelper.getAccount(HcApplication.getContext())));

        String stuxx = "";
        try {
            stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = HcUtil.getScheme() + BASE_URL + SignInfo.URL + stuxx;// HcUtil.getScheme()
        // +
        HcLog.D("HcHttpRequest sendSignCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        SignInfo signInfo = new SignInfo(get, flag, singType);
        signInfo.mCategory = RequestCategory.SIGN;
        signInfo.mResponse = response;
        addTask(signInfo);
        response.notifyRequestMd5Url(signInfo.mCategory, key);
        mClient.execute(signInfo);
    }

    public void sendSignItemCommand(String searchDate, IHttpResponse response) {
        Map<String, String> httpparams = new HashMap<String, String>();
        httpparams.put("searchDate", searchDate);
        httpparams.put("account",
                URLEncode(SettingHelper.getAccount(HcApplication.getContext())));
        String stuxx = "";
        try {
            stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = HcUtil.getScheme() + BASE_URL + SignItemInfo.URL + stuxx;// BASE_URL
        // +
        // SignItemInfo.URL
        // +
        // stuxx;
        // HcUtil.getScheme() + BASE_URL + SignItemInfo.URL + stuxx;
        HcLog.D("HcHttpRequest sendSignItemCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        SignItemInfo signItemInfo = new SignItemInfo(get);
        signItemInfo.mCategory = RequestCategory.SIGNITEM;
        signItemInfo.mResponse = response;
        addTask(signItemInfo);
        response.notifyRequestMd5Url(signItemInfo.mCategory, key);
        mClient.execute(signItemInfo);
    }

    public void sendSignAddrCommand(IHttpResponse response) {
        Map<String, String> httpparams = new HashMap<String, String>();
        httpparams.put("account",
                URLEncode(SettingHelper.getAccount(HcApplication.getContext())));
        String stuxx = "";
        try {
            stuxx = HcUtil.getGetParams(HcUtil.mapToList(httpparams));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = HcUtil.getScheme() + BASE_URL + SignAddr.SignAddrUrl
                + stuxx;
        // HcUtil.getScheme() + BASE_URL + SignItemInfo.URL + stuxx;
        HcLog.D("HcHttpRequest sendSignAddrCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        SignAddr signAddr = new SignAddr(get);
        signAddr.mCategory = RequestCategory.SIGNADDR;
        signAddr.mResponse = response;
        addTask(signAddr);
        response.notifyRequestMd5Url(signAddr.mCategory, key);
        mClient.execute(signAddr);
    }

    /**
     * 获取资料中心的栏目
     *
     * @param response 请求的回调接口
     * @author jrjin
     * @time 2015-8-28 上午11:54:32
     */
    public void sendDataColumnCommand(IHttpResponse response, String moduleId) {
        String url = HcUtil.getScheme() + BASE_URL + DataColumn.URL
                + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()));
        // url = "http://10.80.7.86:8080" + BASE_URL + DataColumn.URL
        // + "account=cmk";
        HcLog.D(TAG + " #sendDataColumnCommand url = " + url);

        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        DataColumn column = new DataColumn(get, moduleId);
        column.mCategory = RequestCategory.DATA_COLUMN;
        column.mResponse = response;
        column.mUrl = key;
        addTask(column);
        response.notifyRequestMd5Url(column.mCategory, key);
        mClient.execute(column);
    }

    /**
     * 根据栏目获取资料数据列表
     *
     * @param columnId 栏目编号
     * @param page     当前页
     * @param size     每页的大小
     * @param response 请求的回调接口
     * @author jrjin
     * @time 2015-8-28 下午12:02:54
     */
    public void sendDataListCommand(String columnId, int page, int size,
                                    IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + DataList.URL + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()))
                + "&columnId=" + columnId + "&pageSize=" + size + "&start="
                + page;
        // url = "http://10.80.7.86:8080" + BASE_URL + DataList.URL
        // + "account=cmk"
        // + "&columnId=" + columnId + "&pageSize=" + size + "&start="
        // + page;

        HcLog.D(TAG + " #sendDataListCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        DataList list = new DataList(get, columnId);
        list.mCategory = RequestCategory.DATA_LIST;
        list.mResponse = response;
        list.mUrl = key;
        addTask(list);
        response.notifyRequestMd5Url(list.mCategory, key);
        mClient.execute(list);
    }

    /**
     * 根据内容去检索相应的资料数据列表
     *
     * @param columnId 栏目编号，要是为-1，则检索全部的资料
     * @param page     当前页
     * @param size     返回的页大小
     * @param keys     需要检索的内容
     * @param response 请求的回调接口
     * @author jrjin
     * @time 2015-8-28 下午12:03:57
     */
    public void sendSearchDataListCommand(String columnId, int page, int size,
                                          String keys, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + SearchData.URL
                + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()))
                + "&columnId=" + columnId + "&pageSize=" + size + "&start="
                + page + "&key=" + URLEncode(keys.trim());

        HcLog.D(TAG + " #sendSearchDataListCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        SearchData list = new SearchData(get, columnId);
        list.mCategory = "-1".equals(columnId) == true ? RequestCategory.SEARCH_ALL_DATA
                : RequestCategory.SEARCH_DATA;
        list.mResponse = response;
        list.mUrl = key;
        addTask(list);
        response.notifyRequestMd5Url(list.mCategory, key);
        mClient.execute(list);
    }

    /**
     * 根据检索的列表项获取资料详情
     *
     * @param dataId   资料编号、文件编号
     * @param flag     资料标识：0—标题，1—主文件，2—附件
     * @param response 请求的回调接口
     * @author jrjin
     * @time 2015-8-28 下午12:09:35
     */
    public void sendSearchDataDetail(String dataId, int flag,
                                     IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + SearchDataDetail.URL
                + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()))
                + "&dataId=" + dataId + "&dataFlag=" + "" + flag;

        HcLog.D(TAG + " #sendSearchDataDetail url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        SearchDataDetail detail = new SearchDataDetail(get, dataId);
        detail.mCategory = RequestCategory.SEARCH_DATA_DETAIL;
        detail.mResponse = response;
        detail.mUrl = key;
        addTask(detail);
        response.notifyRequestMd5Url(detail.mCategory, key);
        mClient.execute(detail);
    }

    /**
     * 获取通讯录联系人
     *
     * @param response
     * @author jrjin
     * @time 2015-10-19 下午4:24:53
     */
    public void sendContactsRequest(String moduleId, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL
                + ContactsRequest.CONTACTS_URL + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()));

        HcLog.D(TAG + " #sendContactsRequest url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        ContactsRequest contacts = new ContactsRequest(get, moduleId);
        contacts.mCategory = RequestCategory.CONTACTS_REQUEST;
        contacts.mResponse = response;
        contacts.mUrl = key;
        addTask(contacts);
        response.notifyRequestMd5Url(contacts.mCategory, key);
        mClient.execute(contacts);

        /** test */
        // postView(ContanctsInfoTest.getContacts(HcApplication.getContext()),
        // response, ResponseCategory.SUCCESS,
        // RequestCategory.CONTACTS_REQUEST);
    }

    /**
     * 检测模块的是否需要更新
     *
     * @param info
     * @param response
     * @author jrjin
     * @time 2015-10-22 下午11:53:15
     */
    public void sendModuleCheckCommand(ModuleInfo info, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + CheckModuleTime.URL
                + "account="
                + URLEncode(SettingHelper.getAccount(HcApplication.getContext()))
                + "&updateTime=" + info.getUpdateTime() + "&moduleId="
                + info.getModuleId();

        HcLog.D(TAG + " #sendModuleCheckCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        CheckModuleTime module = new CheckModuleTime(get, info.getModuleId());
        module.mCategory = RequestCategory.CHECK_MODULE_TIME;
        module.mResponse = response;
        module.mUrl = key;
        addTask(module);
        response.notifyRequestMd5Url(module.mCategory, key);
        mClient.execute(module);
    }

    public void sendQueryNewsDetails(String newsId, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL
                + NewPicDetails.NewsDetailsUrl + "id=" + newsId;
        HcLog.D(TAG + " #sendQueryNewsDetails url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        NewPicDetails details = new NewPicDetails(get);
        details.mCategory = RequestCategory.NEWDETAILS;
        details.mResponse = response;
        details.mUrl = key;
        addTask(details);
        response.notifyRequestMd5Url(details.mCategory, key);
        mClient.execute(details);

    }

    public void sendBindChannel(String imei, String channelID,
                                String versioncode, String ptype, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + BindChannel.URL + "imei="
                + imei + "&channelID=" + channelID + "&versioncode="
                + versioncode + "&ptype=" + ptype;
        HcLog.D(TAG + " # sendBindChannel url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        BindChannel bind = new BindChannel(get);
        bind.mCategory = RequestCategory.BINDCHAN;
        bind.mResponse = response;
        bind.mUrl = key;
        addTask(bind);
        response.notifyRequestMd5Url(bind.mCategory, key);
        mClient.execute(bind);

    }

    public void sendPushModuleList(String versioncode, String channelId,
                                   String ptype, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + PushModuleList.URL
                + "versioncode=" + versioncode + "&channelID=" + channelId
                + "&pType=" + ptype;
        HcLog.D(TAG + " #sendPushModuleList url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        PushModuleList pushList = new PushModuleList(get);
        pushList.mCategory = RequestCategory.PushModuleList;
        pushList.mResponse = response;
        pushList.mUrl = key;
        addTask(pushList);
        response.notifyRequestMd5Url(pushList.mCategory, key);
        mClient.execute(pushList);

    }

    public void sendUpdatePushSettings(String channelId, String App_id,
                                       String Is_push, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + UpdatePushSettings.URL
                + "channelId=" + channelId + "&item_id=" + App_id + "&Is_push="
                + Is_push;
        HcLog.D(TAG + " #sendUpdatePushSettings url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        UpdatePushSettings pushList = new UpdatePushSettings(get);
        pushList.mCategory = RequestCategory.UpdatePushSettings;
        pushList.mResponse = response;
        pushList.mUrl = key;
        addTask(pushList);
        response.notifyRequestMd5Url(pushList.mCategory, key);
        mClient.execute(pushList);

    }

    public class SignAddr extends AbstractHttp {
        private static final String TAG = HcHttpRequest.TAG + "#SignAddr";

        private static final String SignAddrUrl = "getsignaddress";

        public SignAddr(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(object.getJSONObject(BODY).toString(), mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else if (/*status == 1*/status == 601) { // 未设置考勤地点,当作外勤
                        postView("", mResponse, ResponseCategory.NOT_MATCH, mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getsignaddress";
        }
    }

    public class NewsCum extends AbstractHttp {
        private static final String TAG = HcHttpRequest.TAG + "#NewsCum";

        private static final String NewsCumUrl = "getnews";

        private final String mModuleId;

        public NewsCum(HttpUriRequest request, String moduleId) {
            super(mClient, request, mHandler);
            mModuleId = moduleId;
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);

            JSONObject object;
            String code = "0";
            List<NewsColumn> newsColumnTemps = new ArrayList<NewsColumn>();

            try {
                object = new JSONObject((String) data);
                code = object.getString("code");
                if ("0".equals(code)) {
                    JSONObject body = object.getJSONObject("body");
                    if (hasValue(body, "updateTime")) {
                        HcLog.D(TAG + " #parseJson set module time  ========================== ");
                        SettingHelper.setModuleTime(
                                HcApplication.getContext(), mModuleId,
                                body.getString("updateTime"), false);
                    }
                    JSONArray array = body.getJSONArray("newscolumn");
                    NewsColumn newsColumnTemp = null;
                    JSONObject newsColumnObj = null;
                    for (int i = 0, n = array.length(); i < n; i++) {
                        newsColumnObj = array.getJSONObject(i);
                        newsColumnTemp = new NewsColumn();

                        if (hasValue(newsColumnObj, "id")) {
                            newsColumnTemp.setNewsId(newsColumnObj
                                    .getString("id"));
                        }
                        if (hasValue(newsColumnObj, "type")) {
                            newsColumnTemp.setmType(newsColumnObj
                                    .getInt("type"));
                        }
                        if (hasValue(newsColumnObj, "contenttype")) {
                            newsColumnTemp.setmContenttype(newsColumnObj
                                    .getInt("contenttype"));
                        }
                        if (hasValue(newsColumnObj, "name")) {
                            newsColumnTemp.setmName(newsColumnObj
                                    .getString("name"));
                        }
                        if (hasValue(newsColumnObj, "isrolltopic")) {
                            newsColumnTemp.setIsSrolltopic(newsColumnObj
                                    .getInt("isrolltopic"));
                        }

                        newsColumnTemps.add(newsColumnTemp);
                    }

                    postView(newsColumnTemps, mResponse,
                            ResponseCategory.SUCCESS, mCategory);
                } else {
                    toastError(Integer.valueOf(code), object, this);
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                }
            } catch (Exception e) {
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getnews";
        }
    }

    private class NewPicDetails extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#NewPicDetails";

        public static final String NewsDetailsUrl = "getnewsdetail?";

        public NewPicDetails(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);

            JSONObject object;
            String code = "0";
            NewsDetailsInfo newsDetailsInfo = new NewsDetailsInfo();
            try {
                object = new JSONObject((String) data);
                code = object.getString("code");
                if ("0".equals(code)) {

                    JSONObject body = object.getJSONObject("body");
                    JSONObject news = body.getJSONObject("news");
                    if (hasValue(news, "contentType")) {
                        newsDetailsInfo.setContentType(news
                                .getString("contentType"));
                    }
                    if (hasValue(news, "id")) {
                        newsDetailsInfo.setId(news.getString("id"));
                    }
                    if (hasValue(news, "title")) {
                        newsDetailsInfo.setTitle(news.getString("title"));
                    }
                    if (hasValue(news, "itemUrl")) {
                        newsDetailsInfo.setItemUrl(news.getString("itemUrl"));
                    }
                    if (hasValue(news, "date")) {
                        newsDetailsInfo.setDate(news.getString("date"));
                    }
                    if (hasValue(news, "picList")) {
                        JSONArray array = news.getJSONArray("picList");
                        JSONObject jsonobj = null;
                        for (int i = 0, n = array.length(); i < n; i++) {
                            jsonobj = array.getJSONObject(i);
                            PicInfo pi = new PicInfo();
                            if (hasValue(jsonobj, "picUrl")) {
                                pi.setPicUrl(jsonobj.getString("picUrl"));
                            }
                            if (hasValue(jsonobj, "picText")) {
                                pi.setPicText(jsonobj.getString("picText"));
                            }

                            newsDetailsInfo.getPics().add(pi);
                        }
                    }

                    postView(newsDetailsInfo, mResponse,
                            ResponseCategory.SUCCESS, mCategory);
                } else {
                    toastError(Integer.valueOf(code), object, this);
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                }
            } catch (Exception e) {
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getnewsdetail";
        }
    }

    private class Logout extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#Monitor";

        private static final String URL = "logout?";

        public Logout(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "logout";
        }
    }

    /**
     * 客户绑定（解绑）
     *
     * @author ncll
     */
    private class BindChannel extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#BindChannel";

        private static final String URL = "registerchannelid?";

        public BindChannel(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            try {
                JSONObject object = new JSONObject(data);
                int code = object.getInt(STATUS);
                if (code == REQUEST_SUCCESS)
                    postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
                else {
                    toastError(code, object, this);
//					postView(data, mResponse, ResponseCategory.DATA_ERROR, mCategory);
                }
            } catch (Exception e) {
                postView(data, mResponse, ResponseCategory.DATA_ERROR, mCategory);
            }


        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "registerchannelid";
        }
    }

    /**
     * 客户端推送模块列表
     *
     * @author ncll
     */
    private class PushModuleList extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#PushModuleList";

        private static final String URL = "clientPushList?";

        public PushModuleList(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "clientPushList";
        }
    }

    /**
     * 客户端配置模块是否接收推送
     *
     * @author ncll
     */
    private class UpdatePushSettings extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG
                + "#UpdatePushSettings";

        private static final String URL = "saveClientPush?";

        public UpdatePushSettings(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "saveClientPush";
        }
    }

    /**
     * 防止android.os.NetworkOnMainThreadException Android在4.0之前的版本 支持在主线程中访问网络，
     * 但是在4.0以后对这部分程序进行了优化， 也就是说访问网络的代码不能写在主线程中了。
     *
     * @author jrjin
     * @time 2014-12-17 上午10:15:23
     */
    private class DownloadApp extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#DownloadApp";
        private final String mAppId;

        public DownloadApp(HttpUriRequest request, String id) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mAppId = id;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void parseInputStream(InputStream stream) {
            // TODO Auto-generated method stub
            cancelTask(this);
            if (stream != null) {
                DownlaodAppInfo info = new DownlaodAppInfo();
                info.appId = mAppId;
                info.stream = stream;
                mResponse.notify(info, mCategory, ResponseCategory.SUCCESS);
                // postView(stream, mResponse, ResponseCategory.SUCCESS,
                // mCategory);
            } else {
                mResponse.notify(null, mCategory, ResponseCategory.DATA_ERROR);
                // postView(null, mResponse, ResponseCategory.DATA_ERROR,
                // mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "download";
        }
    }

    private class RequestTask extends TimerTask {

        private AbstractHttp mHttp;

        public RequestTask(AbstractHttp http) {
            mHttp = http;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #RequestTask http = " + mHttp);
            if (mHttp == null)
                throw new NullPointerException(" AbstractHttp is null!");

            /**
             * @author jrjin
             * @date 2016-2-23 下午3:25:15
             */
            mHttp.updateLog(false);

            /**
             * @author jrjin
             * @date 2015-12-5 下午4:36:11 取消返回的处理
             */
            if (mHttp instanceof DownloadApp) {
                postView(((DownloadApp) mHttp).mAppId, mHttp.mResponse,
                        ResponseCategory.SESSION_TIMEOUT, mHttp.mCategory);
            } else if (mHttp instanceof NewsList) {
                postView(((NewsList) mHttp).mId,
                        mHttp.mResponse,
                        ResponseCategory.SESSION_TIMEOUT,
                        mHttp.mCategory);
            } else if (mHttp instanceof NewsScrollList) {
                postView(((NewsScrollList) mHttp).mId,
                        mHttp.mResponse,
                        ResponseCategory.SESSION_TIMEOUT,
                        mHttp.mCategory);
            } else {
                postView(mHttp.mCategory, mHttp.mResponse,
                        ResponseCategory.SESSION_TIMEOUT, mHttp.mCategory);
            }
            mHttp.cancelRequest();
            synchronized (HcHttpRequest.this) {
                mTaskMap.remove(mHttp.mUrl);
            }

            mHttp = null;
        }

    }

    private class Login extends AbstractHttp {

        private static final String TAG = "#Login";
        private static final String URL = "login?";

        private final String mAccount;
        private final String mPw;

        public Login(HttpUriRequest request, String account, String pw) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mAccount = account;
            mPw = pw;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else { // 除了成功需要处理，其他的返回code都toast服务端返回的msg
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "login";
        }
    }

    private class SignInfo extends AbstractHttp {

        private static final String TAG = "#sign";

        private static final String URL = "sign";
        /**
         * 签到/签出：0—签到，1—签出
         */
        private final String mFlag;
        /**
         * 签到/签出方式：0—自动，1—手动
         */
        private final String mType;

        public SignInfo(HttpUriRequest request, String flag, String type) {
            super(mClient, request, mHandler);
            mFlag = flag;
            mType = type;
        }

        @Override
        public void parseJson(String data) {
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);

            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == /*REQUEST_SUCCESS*/610 || status == 620) {
//                        if (mFlag.equals("0")) {
//                            SettingHelper.setTodaySignin(HcApplication.getContext());
//                        } else if (mFlag.equals("1") && mType.equals("0")) {
//                            SettingHelper.setTodaySignout(HcApplication.getContext());
//                        }
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else { // 除了成功需要处理，其他的返回code都toast服务端返回的msg
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "sign";
        }
    }

    private class SignItemInfo extends AbstractHttp {

        private static final String TAG = "#signItem";

        private static final String URL = "getsignlist";

        public SignItemInfo(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else {
                        toastError(status, object, this);
                    }
                }

            } catch (Exception e) {
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }

        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getsignlist";
        }
    }

    private class MarketAppList extends AbstractHttp {

        private static final String TAG = "MarketAppList";

        private static final String URL = "applist?";

        public MarketAppList(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == REQUEST_SUCCESS) {

                        /**
                         * @author jinjr
                         * @date 2016-03-15 14:47
                         */
                        mResponse.notify(data, mCategory, ResponseCategory.SUCCESS);

//						JSONArray array = object.getJSONArray(BODY);
//						List<AppInfo> infos = new ArrayList<AppInfo>();
//						JSONObject app;
//						AppInfo info = null;
//						for (int i = 0, n = array.length(); i < n; i++) {
//							app = array.getJSONObject(i);
//							info = null;
//							if (hasValue(app, "type")) {
//								String type = app.getString("type");
//								if (!TextUtils.isEmpty(type)
//										&& type.equals("0")) {
//									info = new NativeAppInfo();
//									info.setAppType(0);
//								}
//
//							}
//							if (info == null) {
//								info = new Html5AppInfo();
//								info.setAppType(1);
//							}
//							// HcLog.D(TAG + " info = " + info);
//							if (hasValue(app, "id")) {
//								info.setAppId(app.getString("id"));
//							}
//							if (hasValue(app, "name")) {
//								info.setAppName(app.getString("name"));
//							}
//							if (hasValue(app, "icon")) {
//								info.setAppIcon(app.getString("icon"));
//							}
//							if (hasValue(app, "version")) {
//								info.setAppVersion(app.getString("version"));
//							}
//							if (hasValue(app, "url")) {
//								info.setAppUrl(app.getString("url"));
//							}
//							if (hasValue(app, "package_name")) {
//								info.setAppPackage(app
//										.getString("package_name"));
//							}
//							if (hasValue(app, "category")) {
//								// HcLog.D(TAG +
//								// " category = "+app.getString("category"));
//								info.setAppCategory(Integer.valueOf(app
//										.getString("category")));
//								// HcLog.D(TAG + " category = "
//								// + app.getString("category"));
//							}
//							if (hasValue(app, "appsize")) {
//								info.setAppSize(Integer.valueOf(app
//										.getString("appsize")));
//							}
//							if (hasValue(app, "category_name")) {
//								info.setCategoryName(app
//										.getString("category_name"));
//							}
//
//							info.setAllOrder(i);
//							info.setServerOrder(i);
//							infos.add(info);
//						}
//
//						// 确认有多少种应用类型
//						Set<Integer> category = new HashSet<Integer>();
//						for (AppInfo appInfo : infos) {
//							category.add(appInfo.getAppCategory());
//						}
//
//						for (Integer integer : category) {
//							filterApps(infos, integer);
//						}
//
//						// filterApps(infos, HcAppData.APP_CATEGORY_DATA);
//						// filterApps(infos, HcAppData.APP_CATEGORY_OA);
//						// filterApps(infos, HcAppData.APP_CATEGORY_SERVICE);
//						// filterApps(infos, HcAppData.APP_CATEGORY_SUPERVICE);
//						// for (AppInfo appInfo : infos) {
//						// HcLog.D(TAG + " all order = "
//						// + appInfo.getAllOrder());
//						// }
//						postView(infos, mResponse, ResponseCategory.SUCCESS,
//								mCategory);
                    } else {
                        toastError(status, object, this);
//						postView(null, mResponse, ResponseCategory.DATA_ERROR,
//								mCategory);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

//		private void filterApps(List<AppInfo> infos, int category) {
//			List<AppInfo> appInfos = new ArrayList<AppInfo>();
//			for (AppInfo info : infos) {
//				if (category == info.getAppCategory()) {
//					appInfos.add(info);
//				}
//				// HcLog.D(TAG +
//				// " filterApps info category = "+info.getAppCategory() +
//				// " category = "+category);
//			}
//			for (int i = 0, n = appInfos.size(); i < n; i++) {
//				appInfos.get(i).setCategoryOrder(i);
//			}
//
//		}

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "applist";
        }
    }

    private class DownloadImage extends AbstractHttp {

        private static final String TAG = "DownloadImage";
        private final String mAppId;
        private final String mVersion;

        public DownloadImage(HttpUriRequest request, String id, String version) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mAppId = id;
            mVersion = version;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void parseInputStream(InputStream stream) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " DownloadImage  stream = " + stream);
            cancelTask(this);
            if (stream != null) {
                DownlaodAppInfo info = new DownlaodAppInfo();
                info.appId = mAppId;
                info.stream = stream;
                info.appVersion = mVersion;
                mResponse.notify(info, mCategory, ResponseCategory.SUCCESS);
            } else {
                postView(mAppId + "_" + mVersion, mResponse,
                        ResponseCategory.DATA_ERROR, mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "download";
        }
    }

    private class DownloadPdf extends AbstractHttp {

        public DownloadPdf(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void parseInputStream(InputStream stream) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " DownloadImage  stream = " + stream);
            cancelTask(this);
            if (stream != null) {
                mResponse.notify(stream, mCategory, ResponseCategory.SUCCESS);
            } else {
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "download";
        }
    }

    private class DownloadGif extends AbstractHttp {

        public DownloadGif(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
        }

        @Override
        public void parseInputStream(InputStream stream) {
            HcLog.D(TAG + " DownloadGif  stream = " + stream);
            cancelTask(this);
            if (stream != null) {
                mResponse.notify(stream, mCategory, ResponseCategory.SUCCESS);
            } else {
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "download";
        }
    }

    /**
     * @param url
     * @return 要是第一次请求返回url的MD5格式, 否则返回null
     * @author jrjin
     * @time 2015-6-19 上午11:47:18
     */
    private String hasInTask(String url) {
        String key = null;
        if (!TextUtils.isEmpty(url)) { // Null不需要请求
            key = HcUtil.getMD5String(url);
            synchronized (this) {
                if (mTaskMap.containsKey(key))
                    key = null; // 已经存在则返回Null
            }
        }
        return key;
    }

    private class DataColumn extends AbstractHttp {

        private static final String TAG = "DataColumn";

        private static final String URL = "getdatacolumns?";

        private final String mModuleId;

        public DataColumn(HttpUriRequest request, String moduleId) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mModuleId = moduleId;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    object = object.getJSONObject(BODY);
                    if (hasValue(object, "updateTime")) {
                        SettingHelper.setModuleTime(
                                HcApplication.getContext(), mModuleId,
                                object.getString("updateTime"), false);
                    }

                    List<DocColumn> columns = new ArrayList<DocColumn>();

                    if (hasValue(object, "dataColumnList")) {
                        JSONArray array = object
                                .getJSONArray("dataColumnList");
                        DocColumn column;
                        for (int i = 0, n = array.length(); i < n; i++) {
                            column = new DocColumn();
                            object = array.getJSONObject(i);
                            if (hasValue(object, "columnId")) {
                                column.setNewsId(object
                                        .getString("columnId"));
                            }
                            if (hasValue(object, "columnName")) {
                                column.setmName(object
                                        .getString("columnName"));
                            }
                            columns.add(column);
                        }

                    }

                    postView(columns, mResponse,
                            ResponseCategory.SUCCESS, mCategory);
                } /*else if (status == 96) {
                    postView(null, mResponse, ResponseCategory.ACCOUNT_INVALID,
							mCategory);
				} */ else {
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                    toastError(status, object, this);
                }

            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getdatacolumns";
        }
    }

    private class DataList extends AbstractHttp {

        private static final String TAG = "DataList";

        private static final String URL = "getdatalist?";
        /**
         * 栏目编号
         */
        private final String mId;

        public DataList(HttpUriRequest request, String id) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mId = id;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    object = object.getJSONObject(BODY);
                    if (hasValue(object, "dataList")) {
                        JSONArray array = object.getJSONArray("dataList");
                        List<DocInfo> infos = new ArrayList<DocInfo>();
                        DocInfo docInfo;
                        DocFileInfo fileInfo;
                        for (int i = 0, n = array.length(); i < n; i++) {
                            docInfo = new DocInfo();
                            object = array.getJSONObject(i);
                            fileInfo = new DocFileInfo();
                            docInfo.setColumnId(mId);

                            if (hasValue(object, "dataId")) {
                                docInfo.setDataId(object
                                        .getString("dataId"));
                            }
                            if (hasValue(object, "source")) {
                                docInfo.setDataSource(object
                                        .getString("source"));
                            }
                            if (hasValue(object, "title")) {
                                docInfo.setDataTitle(object
                                        .getString("title"));
                            }
                            if (hasValue(object, "createDate")) {
                                docInfo.setDate(object
                                        .getString("createDate"));
                            }
                            /** 解析主文件 */
                            fileInfo.setFlag(DocInfo.FLAG_MAIN);
                            if (hasValue(object, "fileId")) {
                                fileInfo.setFileId(object
                                        .getString("fileId"));
                            }
                            if (hasValue(object, "fileName")) {
                                fileInfo.setFileName(object
                                        .getString("fileName"));
                            }
                            if (hasValue(object, "fileSize")) {
                                fileInfo.setFileSize(Integer.valueOf(object
                                        .getString("fileSize")));
                            }
                            if (hasValue(object, "filePath")) {
                                fileInfo.setFileUrl(object
                                        .getString("filePath"));
                            }
                            docInfo.addDocInfo(fileInfo);

                            /** 开始解析附件 */
                            if (hasValue(object, "annexList")) {
                                JSONArray annex = object
                                        .getJSONArray("annexList");
                                for (int j = 0, m = annex.length(); j < m; j++) {
                                    fileInfo = new DocFileInfo();
                                    object = annex.getJSONObject(j);
                                    if (hasValue(object, "annexFileId")) {
                                        fileInfo.setFileId(object
                                                .getString("annexFileId"));
                                    }
                                    if (hasValue(object, "annexFileName")) {
                                        fileInfo.setFileName(object
                                                .getString("annexFileName"));
                                    }
                                    if (hasValue(object, "annexFileSize")) {
                                        fileInfo.setFileSize(Integer.valueOf(object
                                                .getString("annexFileSize")));
                                    }
                                    if (hasValue(object, "annexFilePath")) {
                                        fileInfo.setFileUrl(object
                                                .getString("annexFilePath"));
                                    }
                                    docInfo.addDocInfo(fileInfo);
                                }
                            }

                            infos.add(docInfo);
                        }

                        postView(infos, mResponse,
                                ResponseCategory.SUCCESS, mCategory);
                    } else {
                        postView(new ArrayList<DocInfo>(), mResponse,
                                ResponseCategory.SUCCESS, mCategory);
                    }
                } /*else if (status == 96) {
                    postView(null, mResponse, ResponseCategory.ACCOUNT_INVALID,
							mCategory);
				} */ else {
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                    toastError(status, object, this);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getdatalist";
        }
    }

    private class SearchData extends AbstractHttp {

        private static final String TAG = "SearchData";

        private static final String URL = "searchdatalist?";

        private final String mId;

        public SearchData(HttpUriRequest request, String id) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mId = id;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    object = object.getJSONObject(BODY);
                    if (hasValue(object, "dataList")) {
                        JSONArray array = object.getJSONArray("dataList");
                        List<SearchDocInfo> infos = new ArrayList<SearchDocInfo>();
                        SearchDocInfo info;
                        for (int i = 0, n = array.length(); i < n; i++) {
                            info = new SearchDocInfo();
                            info.setColumnId(mId);
                            object = array.getJSONObject(i);

                            if (hasValue(object, "dataId")) {
                                info.setFileId(object.getString("dataId"));
                            }
                            if (hasValue(object, "dataName")) {
                                info.setFileName(object
                                        .getString("dataName"));
                            }
                            if (hasValue(object, "dataSize")) {
                                info.setFileSize(Integer.valueOf(object
                                        .getString("dataSize")));
                            }
                            if (hasValue(object, "dataPath")) {
                                info.setFileUrl(object
                                        .getString("dataPath"));
                            }

                            if (hasValue(object, "dataFlag")) {
                                info.setFlag(Integer.valueOf(object
                                        .getString("dataFlag")));
                            }
                            infos.add(info);
                        }
                        postView(infos, mResponse,
                                ResponseCategory.SUCCESS, mCategory);

                    } else {

                        postView(new ArrayList<SearchDocInfo>(), mResponse,
                                ResponseCategory.SUCCESS, mCategory);
                    }
                } /*else if (status == 96) {
                    postView(null, mResponse, ResponseCategory.ACCOUNT_INVALID,
							mCategory);
				} */ else {
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                    toastError(status, object, this);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "searchdatalist";
        }
    }

    private class SearchDataDetail extends AbstractHttp {

        private static final String TAG = "SearchDataDetail";

        private static final String URL = "searchdatadetail?";

        private final String mId;

        public SearchDataDetail(HttpUriRequest request, String id) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mId = id;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    object = object.getJSONObject(BODY);
                    if (hasValue(object, "data")) {
                        object = object.getJSONObject("data");
                        DocInfo info = new DocInfo();
                        DocFileInfo fileInfo = new DocFileInfo();
                        info.setColumnId(mId);
                        if (hasValue(object, "dataId")) {
                            info.setDataId(object.getString("dataId"));
                        }
                        if (hasValue(object, "source")) {
                            info.setDataSource(object.getString("source"));
                        }
                        if (hasValue(object, "title")) {
                            info.setDataTitle(object.getString("title"));
                        }
                        if (hasValue(object, "createDate")) {
                            info.setDate(object.getString("createDate"));
                        }
                        /** 解析主文件 */
                        fileInfo.setFlag(DocInfo.FLAG_MAIN);
                        if (hasValue(object, "fileId")) {
                            fileInfo.setFileId(object.getString("fileId"));
                        }
                        if (hasValue(object, "fileName")) {
                            fileInfo.setFileName(object
                                    .getString("fileName"));
                        }
                        if (hasValue(object, "fileSize")) {
                            fileInfo.setFileSize(Integer.valueOf(object
                                    .getString("fileSize")));
                        }
                        if (hasValue(object, "filePath")) {
                            fileInfo.setFileUrl(object
                                    .getString("filePath"));
                        }
                        info.addDocInfo(fileInfo);

                        /** 开始解析附件 */
                        if (hasValue(object, "annexList")) {
                            JSONArray annex = object
                                    .getJSONArray("annexList");
                            for (int j = 0, m = annex.length(); j < m; j++) {
                                fileInfo = new DocFileInfo();
                                object = annex.getJSONObject(m);
                                if (hasValue(object, "annexFileId")) {
                                    fileInfo.setFileId(object
                                            .getString("annexFileId"));
                                }
                                if (hasValue(object, "annexFileName")) {
                                    fileInfo.setFileName(object
                                            .getString("annexFileName"));
                                }
                                if (hasValue(object, "annexFileSize")) {
                                    fileInfo.setFileSize(Integer.valueOf(object
                                            .getString("annexFileSize")));
                                }
                                if (hasValue(object, "annexFilePath")) {
                                    fileInfo.setFileUrl(object
                                            .getString("annexFilePath"));
                                }
                                info.addDocInfo(fileInfo);
                            }
                        }
                        postView(info, mResponse, ResponseCategory.SUCCESS,
                                mCategory);

                    } else {
                        postView(null, mResponse,
                                ResponseCategory.DATA_ERROR, mCategory);
                    }
                } /*else if (status == 96) {
                    postView(null, mResponse, ResponseCategory.ACCOUNT_INVALID,
							mCategory);
				} */ else {
//					postView(null, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                    toastError(status, object, this);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(null, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);

            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "searchdatadetail";
        }
    }

    /**
     * 用户主动取消请求或者退出当前页面
     *
     * @param md5Url
     * @author jrjin
     * @time 2015-9-24 下午4:06:12
     */
    public void cancelRequest(String md5Url) {
        synchronized (this) {
            TimerTask task = mTaskMap.remove(md5Url);
            if (null != task) {
                AbstractHttp http = ((RequestTask) task).mHttp;
                if (null != http) {
                    http.cancelRequest();
                    http = null;
                }
                task.cancel();
                task = null;
            }
        }
    }

    private class ContactsRequest extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "#ContactsRequest";

        private static final String CONTACTS_URL = "getemployeelist?";

        private final String mModuleId;

        public ContactsRequest(HttpUriRequest request, String moduleId) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mModuleId = moduleId;
        }

        /**
         * 解析数据在
         * {@link ContactsCacheData#notify(Object, RequestCategory, ResponseCategory)}
         */
        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            /** 过时 重新启用*/
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
            /**
             * 重新用上面的，现在分模块了
             * @author jrjin
             * @date 2016-03-03 14:37
             * */
//			try {
//				JSONObject object = new JSONObject(data);
//				int status = object.getInt(STATUS);
//				if (status == REQUEST_SUCCESS) {
//					/** 全部部门和员工 */
//					List<ContactsInfo> contactsInfos = new ArrayList<ContactsInfo>();
//
//					if (hasValue(object, BODY)) {
//						object = object.getJSONObject(BODY);
//						if (hasValue(object, "updateTime")) {
//							SettingHelper.setModuleTime(
//									HcApplication.getContext(), mModuleId,
//									object.getString("updateTime"));
//						}
//						if (hasValue(object, "list")) {
//							JSONArray array = object.getJSONArray("list");
//							ContactsInfo info = null;
//							int size = array.length();
//							HcLog.D(TAG + " parseJson array size = " + size);
//							int type = 0;
//							for (int i = 0; i < size; i++) {
////								HcLog.D(TAG + " parseJson array start position i = " + i);
//								object = array.getJSONObject(i);
//								if (hasValue(object, "type")) {
//									type = object.getInt("type");
//								}
//
//								if (type == 0) { // 人员
//									info = new EmployeeInfo();
//
//									if (hasValue(object, "mobilePhone")) {
//										info.setMobilePhone(object
//												.getString("mobilePhone"));
//									}
//									if (hasValue(object, "standbyPhone")) {
//										info.setStandbyPhone(object
//												.getString("standbyPhone"));
//									}
//									if (hasValue(object, "fixedPhone")) {
//										info.setFixedPhone(object
//												.getString("fixedPhone"));
//									}
//									if (hasValue(object, "extensionNumber")) {
//										info.setExtensionNumber(object
//												.getString("extensionNumber"));
//									}
//									if (hasValue(object, "virtualNetNumber")) {
//										info.setVirtualNetNumber(object
//												.getString("virtualNetNumber"));
//									}
//									if (hasValue(object, "email")) {
//										info.setEmail(object.getString("email"));
//									}
//									if (hasValue(object, "standbyEmail")) {
//										info.setStandEmail(object
//												.getString("standbyEmail"));
//									}
//
//								} else {
//									info = new DepartmentInfo();
//								}
//
//								if (hasValue(object, "name")) {
//									info.setName(object.getString("name"));
//								}
//								if (hasValue(object, "id")) {
//									info.setId(object.getString("id"));
//								}
//								if (hasValue(object, "parentName")) {
//									info.setParentName(object
//											.getString("parentName"));
//								}
//								if (hasValue(object, "parentId")) {
//									info.setParentId(object
//											.getString("parentId"));
//								}
//
//								// for end!
//								contactsInfos.add(info);
////								HcLog.D(TAG + " parseJson array end position i = " + i);
//							}
//						}
//					} else {
//						// 没有数据
//						;
//					}
//
//					// HcLog.D(TAG +
//					// " parseJson thread = "+Thread.currentThread());
//					// post
//
//					postView(contactsInfos, mResponse,
//							ResponseCategory.SUCCESS, mCategory);
//					// 添加到数据库,需要测试
//					OperateDatabase.insertContacts(contactsInfos,
//							HcApplication.getContext());
//
//				} /*else if (status == 96) {
//					postView(null, mResponse, ResponseCategory.ACCOUNT_INVALID,
//							mCategory);
//				}*/ else {
//					// other code
//					toastError(status, object, this);
////					postView(null, mResponse, ResponseCategory.DATA_ERROR,
////							mCategory);
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//				//e.printStackTrace();
//				HcLog.D(TAG + " parseJson error = " + e);
//				postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
//						mCategory);
//			}

        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getemployeelist";
        }
    }

    private class CheckModuleTime extends AbstractHttp {

        private static final String TAG = "CheckModuleTime";

        private static final String URL = "checkmoduledata?";

        private final String mModuleId;

        public CheckModuleTime(HttpUriRequest request, String moduleId) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
            mModuleId = moduleId;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " #parseJson data = " + data);
            cancelTask(this);
            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    if (hasValue(object, BODY)) {
                        object = object.getJSONObject(BODY);
                        if (hasValue(object, "flag")) {
                            int flag = object.getInt("flag");
                            postView(new ModuleInfo(mModuleId, flag),
                                    mResponse, ResponseCategory.SUCCESS,
                                    mCategory);
                            return;
                        }
                    }
                    postView(new ModuleInfo(mModuleId, 1), mResponse,
                            ResponseCategory.SUCCESS, mCategory);
                } else {
                    // error
                    postView(mModuleId, mResponse, ResponseCategory.DATA_ERROR,
                            mCategory);
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " parseJson error = " + e);
                postView(mModuleId, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "checkmoduledata";
        }
    }

    private class Feedback extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "$Feedback";

        private static final String URL = "submitComments?";

        public Feedback(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            // do nothing
            HcLog.D(TAG + " #parseJson data = " + data);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "submitComments";
        }
    }

    public void sendFeedbackCommand(String content) {
        String url = HcUtil.getScheme() + BASE_URL + Feedback.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext())) + "&comments=" + content;
        HcLog.D(TAG + " sendFeedbackCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        Feedback feedback = new Feedback(get);
        feedback.mUrl = key;
        addTask(feedback);
        mClient.execute(feedback);
    }

    /**
     * @param bitmap
     * @param response
     * @author jrjin
     * @time 2014-10-15 上午9:13:50
     */
    public void sendPostImage(Bitmap bitmap, IHttpResponse response) {

        String url = HcUtil.getScheme() + BASE_URL + PostImage.URL + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext()));
        HcLog.D(TAG + " sendPostImage url = " + url);
        HcLog.D(TAG + " sendPostImage bitmap width = " + bitmap.getWidth()
                + " bitmap height = " + bitmap.getHeight());
        File imageFile = null;
        imageFile = new File(HcApplication.getImagePhotoPath() + System.currentTimeMillis() + ".jpg");
        try {
            OutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " stream error = " + e);
        }

        FileBody fileBody = new FileBody(imageFile);
        // MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        // builder.addPart("data", fileBody);

        MultipartEntity multipartEntity = new MultipartEntity();
        multipartEntity.addPart("account", new StringBody(URLEncode(SettingHelper.getAccount(HcApplication.getContext())), ContentType.DEFAULT_TEXT));
        multipartEntity.addPart("file", fileBody);

        // ByteArrayOutputStream arry = new ByteArrayOutputStream();
        // bitmap.compress(CompressFormat.PNG, 100, arry);
        // ByteArrayEntity arrayEntity = new
        // ByteArrayEntity(arry.toByteArray());
        // arrayEntity.setContentType("multipart/form-data;boundary=10"/*"application/octet-stream"*/);

        HttpPost post = new HttpPost(url);
        post.setEntity(multipartEntity);
        PostImage image = new PostImage(post);
        image.mCategory = RequestCategory.POST_IMAGE;
        image.mResponse = response;
        addTask(image);
        mClient.execute(image);
    }

    /**
     * 上传图片
     *
     * @author jrjin
     * @time 2014-10-10 上午9:57:41
     */
    private class PostImage extends AbstractHttp {

        private static final String TAG = "PostImage";
        private static final String URL = "uploadUserIcon?";

        public PostImage(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS,
                    mCategory);

        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "uploadFileServlet";
        }
    }

    /**
     * 获取新闻列表
     *
     * @param newsId      新闻栏目ID
     * @param size        每一页的条目数
     * @param currentpage 当前需要获取的页
     * @param response
     * @param type        {@link HcNewsData#GET_DATA_MORE} and {@link HcNewsData#GET_DATA_REFRESH}
     * @author jrjin
     * @time 2015-12-29 上午9:43:25
     */
    public void sendNewsListCommand(String newsId, int size, int currentpage,
                                    IHttpResponse response, int type) {
        String url = HcUtil.getScheme() + BASE_URL + NewsList.NewsUrl + "id=" + newsId + "&size=" + size + "&currentpage=" + currentpage;
        HcLog.D("HcHttpRequest sendNewsListCommand url = " + url);

        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        NewsList news = new NewsList(get, type, newsId);
        news.mCategory = RequestCategory.NEWS_LIST;
        news.mResponse = response;
        news.mUrl = key;
        addTask(news);
        response.notifyRequestMd5Url(news.mCategory, key);
        mClient.execute(news);
    }

    private class NewsList extends AbstractHttp {
        private static final String TAG = HcHttpRequest.TAG + "$NewsList";

        public static final String NewsUrl = "getnewslist?";

        private final int mType;
        /**
         * 栏目编号
         */
        private final String mId;

        public NewsList(HttpUriRequest request, int type, String id) {
            super(mClient, request, mHandler);
            mType = type;
            mId = id;
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);

            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    JSONObject body = object.getJSONObject(BODY);
                    JSONArray array = body.getJSONArray("newsList");
                    NewsInfo newsTemp = null;
                    JSONObject newsObj = null;
                    NewsPageData pageData = new NewsPageData();
                    pageData.mGetType = mType;
                    pageData.mId = mId;
                    for (int i = 0, n = array.length(); i < n; i++) {
                        newsObj = array.getJSONObject(i);
                        newsTemp = new NewsInfo();
                        if (hasValue(newsObj, "id")) {
                            newsTemp.mId = (newsObj.getString("id"));
                        }
                        if (hasValue(newsObj, "newsSummary")) {
                            newsTemp.newsSummary = (newsObj
                                    .getString("newsSummary"));
                        }
                        if (hasValue(newsObj, "title")) {
                            newsTemp.mTitle = (newsObj.getString("title"));
                        }
                        if (hasValue(newsObj, "iconUrl")) {
                            String iconUrl = newsObj.getString("iconUrl");
                            newsTemp.mIconUrl = iconUrl;
                        }
                        if (hasValue(newsObj, "address")) {
                            newsTemp.mAddress = (newsObj.getString("address"));
                        }
                        if (hasValue(newsObj, "date")) {
                            newsTemp.mDate = (newsObj.getString("date"));
                        }
                        if (hasValue(newsObj, "itemUrl")) {
                            String itemUrl = newsObj.getString("itemUrl");
                            newsTemp.mContentUrl = itemUrl;
                        }
                        if (hasValue(newsObj, "contentType")) {
                            newsTemp.mContentType = newsObj
                                    .getString("contentType");
                        }
                        if (hasValue(newsObj, "images")) {
                            JSONArray images = newsObj.getJSONArray("images");
                            for (int m = 0; m < images.length(); m++) {
                                newsTemp.mImgs.add(images.getJSONObject(m)
                                        .getString("url"));
                            }
                        }
                        if (hasValue(newsObj, "imageNum")) {
                            newsTemp.mCount = Integer.valueOf(newsObj.getString("imageNum"));
                        }

                        pageData.mInfos.add(newsTemp);
                    }

                    postView(pageData, mResponse, ResponseCategory.SUCCESS,
                            mCategory);
                } else {
                    String msg = object.getString("msg");
                    postView(new ResponseNewsInfo(status, msg, mId), mResponse, ResponseCategory.REQUEST_FAILED,
                            mCategory);
                }
            } catch (Exception e) {
                postView(mId, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }

        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getnewslist";
        }
    }

    public void sendNewsScrollListCommand(String id, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + NewsScrollList.NewsScrollUrl
                + "id=" + id;
        HcLog.D("HcHttpRequest sendNewsScrollCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpGet get = new HttpGet(url);
        NewsScrollList newsScroll = new NewsScrollList(get, id);
        newsScroll.mCategory = RequestCategory.NEWSSCROLL;
        newsScroll.mResponse = response;
        newsScroll.mUrl = key;
        addTask(newsScroll);
        response.notifyRequestMd5Url(newsScroll.mCategory, key);
        mClient.execute(newsScroll);
    }

    private class NewsScrollList extends AbstractHttp {
        private static final String TAG = HcHttpRequest.TAG + "$NewsScrollList";

        public static final String NewsScrollUrl = "getrolltopiclist?";

        /**
         * 栏目编号
         */
        private final String mId;

        public NewsScrollList(HttpUriRequest request, String id) {
            super(mClient, request, mHandler);
            mId = id;
        }

        @Override
        public void parseJson(String data) {
            cancelTask(this);
            HcLog.D(TAG + " parseJson data = " + data);

            try {
                JSONObject object = new JSONObject(data);
                int status = object.getInt(STATUS);
                if (status == REQUEST_SUCCESS) {
                    JSONObject body = object.getJSONObject(BODY);
                    JSONArray array = body.getJSONArray("newscolumn");
                    NewsPageData pageData = new NewsPageData();
                    pageData.mId = mId;
                    NewsInfo newsTemp = null;
                    JSONObject newsObj = null;
                    int count = array.length() > 5 ? 5 : array.length();
                    for (int i = 0, n = count; i < n; i++) {
                        newsObj = array.getJSONObject(i);
                        newsTemp = new NewsInfo();
                        newsTemp.mScroll = true;
                        if (hasValue(newsObj, "rolltopicUrl")) {
                            String iconUrl = newsObj.getString("rolltopicUrl");

                            newsTemp.mIconUrl = iconUrl;
                        }
                        if (hasValue(newsObj, "itemUrl")) {
                            String contentUrl = (newsObj.getString("itemUrl"));
                            newsTemp.mContentUrl = contentUrl;
                        }
                        if (hasValue(newsObj, "title")) {
                            newsTemp.mTitle = (newsObj.getString("title"));
                        }
                        if (hasValue(newsObj, "contentType")) {
                            newsTemp.mContentType = newsObj
                                    .getString("contentType");
                        }
                        if (hasValue(newsObj, "id")) {
                            newsTemp.mId = newsObj.getString("id");
                        }
                        pageData.mInfos.add(newsTemp);
                    }
                    postView(pageData, mResponse, ResponseCategory.SUCCESS,
                            mCategory);
                } else {
                    toastError(status, object, this);
//					postView(mId, mResponse, ResponseCategory.DATA_ERROR,
//							mCategory);
                }
            } catch (Exception e) {
                postView(mId, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }

        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getrolltopiclist";
        }
    }

    /**
     * 获取年会的配置信息
     *
     * @param response
     * @author jrjin
     * @time 2016-1-11 下午4:26:43
     */
    public void sendAnnualInfoCommand(IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + AnnualConfig.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext()));
        HcLog.D(TAG + " sendAnnualInfoCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        AnnualConfig annualConfig = new AnnualConfig(get);
        annualConfig.mUrl = key;
        annualConfig.mCategory = RequestCategory.ANNUAL_CONFIG;
        annualConfig.mResponse = response;
        addTask(annualConfig);
        response.notifyRequestMd5Url(annualConfig.mCategory, key);
        mClient.execute(annualConfig);
    }

    private class AnnualConfig extends AbstractHttp {

        private static final String URL = "getAnnualMsg?";

        public AnnualConfig(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getAnnualMsg";
        }
    }

    /**
     * 获取年会节目列表
     *
     * @param annualId 年会标识
     * @param response
     * @author jrjin
     * @time 2016-1-12 下午3:50:12
     */
    public void sendAnnualProgramListCommand(String annualId, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + AnnualProgramList.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext())) +
                "&annual_id=" + annualId;
        HcLog.D(TAG + " sendAnnualProgramListCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        AnnualProgramList programList = new AnnualProgramList(get);
        programList.mUrl = key;
        programList.mCategory = RequestCategory.ANNUAL_PROGRAM;
        programList.mResponse = response;
        addTask(programList);
        response.notifyRequestMd5Url(programList.mCategory, key);
        mClient.execute(programList);
    }

    private class AnnualProgramList extends AbstractHttp {

        private static final String URL = "getShowList?";

        public AnnualProgramList(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getShowList";
        }
    }

    /**
     * 提交年会节目打分
     *
     * @param annualId  年会标识
     * @param score     分数1-5
     * @param programId 节目编号
     * @param response
     * @author jrjin
     * @time 2016-1-12 下午3:50:40
     */
    public void sendAnnualProgramScoreCommand(String annualId, int score, String programId, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + AnnualProgramScore.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext())) +
                "&annual_id=" + annualId + "&show_id=" + programId + "&show_score=" + score;
        HcLog.D(TAG + " sendAnnualProgramScoreCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        AnnualProgramScore programScore = new AnnualProgramScore(get);
        programScore.mUrl = key;
        programScore.mCategory = RequestCategory.ANNUAL_SCORE;
        programScore.mResponse = response;
        addTask(programScore);
        response.notifyRequestMd5Url(programScore.mCategory, key);
        mClient.execute(programScore);
    }

    private class AnnualProgramScore extends AbstractHttp {

        private static final String URL = "updateScore?";

        public AnnualProgramScore(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "updateScore";
        }
    }

    /**
     * 摇一摇
     *
     * @param annualId 年会标识
     * @param response
     * @author jrjin
     * @time 2016-1-12 下午3:51:27
     */
    public void sendAnnualProgramShakeCommand(String annualId, int round, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + AnnualProgramShake.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext())) +
                "&annual_id=" + annualId + "&round_time=" + round;
        HcLog.D(TAG + " sendAnnualProgramShakeCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        AnnualProgramShake programShake = new AnnualProgramShake(get);
        programShake.mUrl = key;
        programShake.mCategory = RequestCategory.ANNUAL_SHAKE;
        programShake.mResponse = response;
        addTask(programShake);
        response.notifyRequestMd5Url(programShake.mCategory, key);
        mClient.execute(programShake);
    }

    private class AnnualProgramShake extends AbstractHttp {

        private static final String URL = "shakeMobile?";

        public AnnualProgramShake(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "shakeMobile";
        }
    }

    /**
     * 获取年会进行状态
     *
     * @param annualId 年会标识
     * @param response
     * @author jrjin
     * @time 2016-1-12 下午3:52:00
     */
    public void sendAnnualProgramShakeStatusCommand(String annualId, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + AnnualProgramShakeStatus.URL
                + "account=" + URLEncode(SettingHelper.getAccount(HcApplication.getContext())) +
                "&annual_id=" + annualId;
        HcLog.D(TAG + " sendAnnualProgramShakeStatusCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;

        HttpGet get = new HttpGet(url);
        AnnualProgramShakeStatus programShakeStatus = new AnnualProgramShakeStatus(get);
        programShakeStatus.mUrl = key;
        programShakeStatus.mCategory = RequestCategory.ANNUAL_SHAKE_STATUS;
        programShakeStatus.mResponse = response;
        addTask(programShakeStatus);
        response.notifyRequestMd5Url(programShakeStatus.mCategory, key);
        mClient.execute(programShakeStatus);
    }

    private class AnnualProgramShakeStatus extends AbstractHttp {

        private static final String URL = "getAwardCode?";

        public AnnualProgramShakeStatus(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getAwardCode";
        }
    }

    /**
     * 上传日志信息到服务端
     *
     * @param logs     需要上传的日志
     * @param response
     * @author jrjin
     * @time 2016-2-24 上午9:23:54
     */
    public void sendLogsCommand(List<OperationLogInfo> logs, IHttpResponse response) {
        String uri = HcUtil.getScheme() + BASE_URL + PostLog.URL;
        HcLog.D(TAG + "#sendLogsCommand uri = " + uri);
        MultipartEntity multipartEntity = new MultipartEntity();
//        multipartEntity.addPart("logList", new StringBody(getJsonLogs(logs), ContentType.APPLICATION_JSON));
        multipartEntity.addPart("logList", new StringBody(getJsonLogs(logs), ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8)));
        HttpPost post = new HttpPost(uri);
        try {
            post.setEntity(/*new StringEntity(getJsonLogs(logs))*/multipartEntity);
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + "#sendLogsCommand setEntity error  = " + e);
        }
        PostLog log = new PostLog(post);
        log.mCategory = RequestCategory.POST_LOGS;
        log.mResponse = response;
        addTask(log);
        mClient.execute(log);
    }

    private String getJsonLogs(List<OperationLogInfo> infos) {
        JSONObject logs = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject log;
        try {
            for (OperationLogInfo info : infos) {
                log = new JSONObject();
                log.put("deviceType", info.getDeviceType());
                log.put("result", info.getResult());
                log.put("type", info.getType());
                log.put("account", TextUtils.isEmpty(info.getAccount()) ? "" : info.getAccount()/*URLEncode(info.getAccount())*/);
                log.put("appId", info.getAppId());
                log.put("endTime", info.getEndTime());
                log.put("imei", info.getImei());
                log.put("moduleId", info.getModuleId());
                log.put("name", TextUtils.isEmpty(info.getName()) ? "" : info.getName()/*URLEncode(info.getName())*/);
                log.put("startTime", info.getStartTime());
                log.put("version", info.getVersion());
                log.put("appName", TextUtils.isEmpty(info.getAppName()) ? "" : info.getAppName()/*URLEncode(info.getAppName())*/);
                array.put(log);
            }
//			logs.put("logList", array);


        } catch (JSONException e) {
            // TODO: handle exception
            HcLog.D(TAG + "#getJsonLogs error = " + e);
        }
        HcLog.D(TAG + "#getJsonLogs logs = " + array.toString());
        return array.toString();
    }

    private class PostLog extends AbstractHttp {

        private static final String TAG = HcHttpRequest.TAG + "$PostLog";

        private static final String URL = "getbehaviorlogs?";

        public PostLog(HttpUriRequest request) {
            super(mClient, request, mHandler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void parseJson(String data) {
            // TODO Auto-generated method stub
            cancelTask(this);
            HcLog.D(PostLog.TAG + "#parseJson data = " + data);
            postView(data, mResponse, ResponseCategory.SUCCESS, mCategory);
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "getbehaviorlogs";
        }

    }


    public AbstractHcHttpClient getHttpClient() {
        return mClient;
    }

    public Handler getHandler() {
        return mHandler;
    }

    /**
     * 外勤签到
     *
     * @param account  当前签到用户
     * @param lng      经度
     * @param lat      维度
     * @param address  签到地址
     * @param remark   备注
     * @param images   图片列表
     */
    public void sendFieldSignCommand(String account, String lng, String lat, String address, String remark,
                                     List<String> images, IHttpResponse response) {
        String url = HcUtil.getScheme() + BASE_URL + FieldSign.URL + "?account=" + URLEncode(account) +
                "&signType=1&signFlag=3" + "&addressLongitude=" + lng + "&addressLatitude=" + lat
                + "&address=" + URLEncode(address) + "&remark=" + URLEncode(remark);
        // +
        HcLog.D("HcHttpRequest sendFieldSignCommand url = " + url);
        String key = hasInTask(url);
        if (key == null)
            return;
        HttpPost post = new HttpPost(url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(Charset.forName(HTTP.UTF_8));//设置请求的编码格式
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addTextBody("signType", "1");
        builder.addTextBody("signFlag", "3");
        builder.addTextBody("addressLongitude", lng);
        builder.addTextBody("addressLatitude", lat);
//        builder.addTextBody("address", address, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));
//        builder.addTextBody("remark", remark, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8));

        File file;
        String path;
        for (String uri : images) {
            path = uri.substring("file://".length());
            HcLog.D(TAG + " #sendFieldSignCommand file path = " + path);
            file = new File(path);
            if (file.exists()) {

                File newFile = new File(HcApplication.getImagePhotoPath(), System.currentTimeMillis() + ".jpg");
                Bitmap bitmap = null;
                OutputStream stream = null;
                try {
                    bitmap = HcUtil.getSmallBitmap(path, 320, 480);
                    HcLog.D(TAG + " #sendFieldSignCommand bitmap size = " + bitmap.getByteCount() + " width = " + bitmap.getWidth() + " height = " + bitmap.getHeight());
                    stream = new FileOutputStream(newFile);
                    bitmap.compress(CompressFormat.JPEG, 100, stream);
                    stream.flush();
                } catch (Exception e) {
                    // TODO: handle exception
                    HcLog.D(TAG + " stream error = " + e);
                    newFile = null;
                } finally {
                    try {
                        if (stream != null)
                            stream.close();
                    } catch (Exception e) {
                        // do nothing
                    }

                    if (bitmap != null) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }
                builder.addBinaryBody("file", newFile == null ? file : newFile);


            }
        }
        post.setEntity(builder.build());
        FieldSign signInfo = new FieldSign(post);
        signInfo.mCategory = RequestCategory.SIGN;
        signInfo.mResponse = response;
        addTask(signInfo, 60 * 1000);
        response.notifyRequestMd5Url(signInfo.mCategory, key);
        mClient.execute(signInfo);
    }

    private class FieldSign extends AbstractHttp {

        private static final String TAG = "#sign";

        private static final String URL = "sign";

        public FieldSign(HttpUriRequest request) {
            super(mClient, request, mHandler);
        }

        @Override
        public void parseJson(String data) {
            HcLog.D(TAG + " it is in parseJson data = " + data);
            cancelTask(this);

            try {
                JSONObject object = new JSONObject(data);
                if (object != null) {
                    int status = object.getInt(STATUS);
                    if (status == 610) { // 签到成功
                        postView(data, mResponse, ResponseCategory.SUCCESS,
                                mCategory);
                    } else { // 除了成功需要处理，其他的返回code都toast服务端返回的msg
                        toastError(status, object, this);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                HcLog.D(TAG + " error = " + e);
                postView(mCategory, mResponse, ResponseCategory.DATA_ERROR,
                        mCategory);
            }
        }

        @Override
        public String getRequestMethod() {
            // TODO Auto-generated method stub
            return "sign";
        }

    }

    private void addTask(AbstractHttp http, long timeOut) {
        if (http == null)
            return;
        RequestTask task = new RequestTask(http);
        synchronized (this) {
            mTaskMap.put(http.mUrl, task);
            mTimer.schedule(task, timeOut);
        }
    }


}
