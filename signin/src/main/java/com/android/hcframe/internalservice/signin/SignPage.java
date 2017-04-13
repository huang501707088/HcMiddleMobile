/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2016-1-4 下午2:25:15
*/
package com.android.hcframe.internalservice.signin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.container.ContainerActivity;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.internalservice.signin.StickyLayout.onScrollListener;
import com.android.hcframe.login.LoginActivity;
import com.android.hcframe.menu.MenuBaseActivity;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshBase.Mode;
import com.android.hcframe.pull.PullToRefreshBase.OnRefreshBothListener;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;

public class SignPage extends AbstractPage implements onScrollListener, IHttpResponse {

    private static final String TAG = "SignPage";

    private final String mAppId;

    private AnimatorSet mShowAnimation;

    private Animator mHindAnimation;

    private StickyLayout mStickyLayout;

    private RelativeLayout mHeaderParent;

    private TextView mDate;
    private TextView mTime;
    private ImageView mAutoSign;

    private ImageView mStatus;

    private TextView mNetworkError;
    private TextView mDistanceText;
    private TextView mDistanceValue;

    private TextView mSigninBtn;
    private TextView mSignoutBtn;

    private RelativeLayout mOffsetHeaderParent;
    private TextView mYearValue;
    private TextView mMonthValue;
    private TextView mScore;
    private TextView mTimes;

    private PullToRefreshListView mListView;
    private TextView mEmpty;

    private DateHandler mHandler;

    private SignDayAdapter mAdapter;

    private List<SignDayInfo> mSignItems = new ArrayList<SignDayInfo>();

    private boolean mSign = false;

    private Map<String, SignMonthInfo> mCacheMap = new HashMap<String, SignMonthInfo>();

    private int mCurrentYear;

    private int mCurrentMonth;

    private static final int CURRENT_MONTH = 0;
    private static final int HISTORY_MONTH = 1;

    private int mRefreshType = CURRENT_MONTH;

    private Calendar mCalendar;

    private final int mYear;
    private final int mMonth;
    Activity mContext;

    protected SignPage(Activity context, ViewGroup group, String appId) {
        super(context, group);
        // TODO Auto-generated constructor stub
        mContext = context;
        mAppId = appId;
        mHandler = new DateHandler();
        mShowAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.sign_offset_header_show);
        mShowAnimation.addListener(new DisplayHeaderView());
        mCalendar = Calendar.getInstance();
        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mCurrentMonth = mMonth;
        mCurrentYear = mYear;
        HcLog.D(TAG + " #SignPage Year = " + mYear + " mMonth = " + mMonth);
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub

    }

    private void uploadSignInfo(int mSignFlag) {
        if (!mSign) return;
        // 上传签到信息到网络
        if (mSignFlag == 0) {
            HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
        } else {
            HcDialog.showProgressDialog(mContext, R.string.dialog_title_post_data);
        }
        Loction loction = new Loction();
        loction.setmAddressLatitude(SignLoctionUtils.getLat() + "");
        loction.setmAddressLongitude(SignLoctionUtils.getLng() + "");
        loction.setmSignFlag(mSignFlag + "");
        loction.setmSignType("1");
        HcHttpRequest.getRequest().sendSignCommand(loction.getmSignFlag(), loction.getmSignFlag(),
                loction.getmAddressLongitude(), loction.getmAddressLatitude(), this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.sign_auto_btn) {
            if (SettingHelper.getAutoSignin(mContext)) {
                mAutoSign.setImageResource(R.drawable.sign_auto_close);
                SettingHelper.setAutoSignin(mContext, false);
                // 停止自动打卡闹钟
                HcUtil.stopAutoSignAlarm(mContext);
            } else {
                if (!HcUtil.isGPS(mContext)) { // 没有开启GPS
                    final AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .create();
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setContentView(R.layout.enable_gps_dialog);
                    Button unagree_dialog = (Button) dialog.getWindow()
                            .findViewById(R.id.unagree_dialog);
                    Button agree_dialog = (Button) dialog.getWindow().findViewById(
                            R.id.agree_dialog);
                    unagree_dialog.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    agree_dialog.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            mAutoSign.setImageResource(R.drawable.sign_auto_open);
                            SettingHelper.setAutoSignin(mContext, true);
                            // 打开 gps
                            HcUtil.openGPS(mContext);
                            // 网络判断
                            if (!HcUtil.isNetworkConnected(mContext)) {
                                HcUtil.showToast(HcApplication.getContext(),
                                        R.string.open_netdata);
                            }

                            // 开启自动打卡的闹钟
                            HcUtil.startAutoSignAlarm(mContext);
                        }
                    });
                } else {
                    mAutoSign.setImageResource(R.drawable.sign_auto_open);
                    SettingHelper.setAutoSignin(mContext, true);
                    // 网络判断
                    if (!HcUtil.isNetworkConnected(mContext)) {
                        HcUtil.showToast(HcApplication.getContext(),
                                R.string.open_netdata);
                    }
                    // 开启自动打卡的闹钟
                    HcUtil.startAutoSignAlarm(mContext);
                }


            }
        } else if (id == R.id.sign_signin_btn) {
            String signDate = SettingHelper.getSigninTime(mContext);
            if (!TextUtils.isEmpty(signDate)) {
                if (signDate.equals(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH_DAY, System.currentTimeMillis()))) {
                    // 不需要再次签到了
                    HcUtil.showToast(mContext, "今天您已经签到!");
                    return;
                }
            }
            if (HcUtil.isNetWorkAvailable(mContext))
                uploadSignInfo(0);

        } else if (id == R.id.sign_signout_btn) {
            if (HcUtil.isNetWorkAvailable(mContext))
                uploadSignInfo(1);
        }
    }

    @Override
    public void initialized() {
        // TODO Auto-generated method stub
        mDate.setText(HcUtil.getDate(HcUtil.FORMAT_SIGN_YEAR_MONTH_DAY, System.currentTimeMillis()));
        if (SettingHelper.getAutoSignin(mContext)) {
            mAutoSign.setImageResource(R.drawable.sign_auto_open);
        } else {
            mAutoSign.setImageResource(R.drawable.sign_auto_close);
        }
        mShowAnimation.setTarget(mOffsetHeaderParent);

        if (isFirst) {
            isFirst = !isFirst;
            SignLoctionUtils.startLocation();
        }

        if (mAdapter == null) {
            mAdapter = new SignDayAdapter(mContext, mSignItems);
            mListView.setAdapter(mAdapter);
        }

        mListView.setPullDownLable("下拉获取当月考勤记录");
        mListView.setPullDownReleaseLable("放开开始获取数据");

    }

    @Override
    public void setContentView() {
        // TODO Auto-generated method stub
        if (mView == null) {
            mView = mInflater.inflate(R.layout.sign_home_layout, null);

            mStickyLayout = (StickyLayout) mView;

            mHeaderParent = (RelativeLayout) mView.findViewById(R.id.sign_header_parent);

            mDate = (TextView) mView.findViewById(R.id.sign_today_date);
            mTime = (TextView) mView.findViewById(R.id.sign_current_time);
            mAutoSign = (ImageView) mView.findViewById(R.id.sign_auto_btn);

            mStatus = (ImageView) mView.findViewById(R.id.sign_state_icon);

            mNetworkError = (TextView) mView.findViewById(R.id.sign_network_error);
            mDistanceText = (TextView) mView.findViewById(R.id.sign_distance_text);
            mDistanceValue = (TextView) mView.findViewById(R.id.sign_distance_value);

            mSigninBtn = (TextView) mView.findViewById(R.id.sign_signin_btn);
            mSignoutBtn = (TextView) mView.findViewById(R.id.sign_signout_btn);

            mOffsetHeaderParent = (RelativeLayout) mView.findViewById(R.id.sign_offset_height_parent);
            mYearValue = (TextView) mView.findViewById(R.id.sign_year_text);
            mMonthValue = (TextView) mView.findViewById(R.id.sign_month_text);
            mScore = (TextView) mView.findViewById(R.id.sign_score_text);
            mTimes = (TextView) mView.findViewById(R.id.sign_times_statitcs);

            mListView = (PullToRefreshListView) mView.findViewById(R.id.sign_listview);
            mEmpty = (TextView) mView.findViewById(R.id.sign_empty_text);

            mListView.setEmptyView(mView.findViewById(R.id.sign_empty_view));

            mAutoSign.setOnClickListener(this);
            mSigninBtn.setOnClickListener(this);
            mSignoutBtn.setOnClickListener(this);

            mStickyLayout.setOnScrollListener(this);

            mListView.setScrollingWhileRefreshingEnabled(false);
            mListView.setOnRefreshBothListener(new OnRefreshBothListener<ListView>() {

                @Override
                public void onPullDownToRefresh(
                        PullToRefreshBase<ListView> refreshView) {
                    // TODO Auto-generated method stub
                    /**
                     * 这里有两种情况，1.offsetHeader隐藏的时候,及mode==PULL_FROM_START；
                     * 2.获取上个月的信息
                     */
                    if (mListView.getMode() == Mode.PULL_FROM_START) {
                        onRefresh();
                    } else {
                        SignMonthInfo info = mCacheMap.get(getNextMonthKey());
                        if (info != null) {
                            updateMonthSign(info);
                            mListView.onRefreshComplete();
                        } else {
                            throw new UnsupportedOperationException(TAG + "#onPullDownToRefresh SignMonthInfo==null");
                        }
                    }
                }

                @Override
                public void onPullUpToRefresh(
                        PullToRefreshBase<ListView> refreshView) {
                    // TODO Auto-generated method stub
                    SignMonthInfo info = mCacheMap.get(getLastMonthKey());
                    if (info != null) {
                        updateMonthSign(info);
                        mListView.onRefreshComplete();
                    } else {
                        onHistory();
                    }

                }
            });

            mListView.setResetLable(true);
        }
    }

    private boolean mShowing = false;

    private final class DisplayHeaderView implements AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + "$DisplayHeaderView#onAnimationStart!");
            mShowing = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // TODO Auto-generated method stub
            HcLog.D(TAG + "$DisplayHeaderView#onAnimationEnd!");
            mShowing = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // TODO Auto-generated method stub

        }

    }

    private class DateHandler extends Handler {

        private static final int WHAT_UPDATE_TIME = 100;
        private static final int WHAT_UPDATE_DISTANCE = 101;


        void resume(int what) {
            if (!hasMessages(what)) {
                sendEmptyMessage(what);
            }
        }

        void pause(int what) {
            removeMessages(what);
        }

        @Override
        public void handleMessage(Message message) {
            long delayMillis;
            int what;
            switch (message.what) {
                case WHAT_UPDATE_TIME:
                    delayMillis = 1 * 1000;
                    what = WHAT_UPDATE_TIME;
                    mTime.setText(HcUtil.getNowDate(HcUtil.FORMAT_CITY_HOUR));
                    break;
                case WHAT_UPDATE_DISTANCE:
                    what = WHAT_UPDATE_DISTANCE;
                    delayMillis = 5 * 1000;
                    refreshDistanceUI(SignLoctionUtils.getDistance());
                    break;

                default:
                    return;
            }
            sendEmptyMessageDelayed(what, delayMillis);
        }
    }

    @Override
    public void updateHeader(int height, boolean visibility) {
        // TODO Auto-generated method stub
        mHeaderParent.getLayoutParams().height = height;
        mHeaderParent.requestLayout();
        if (visibility) {
            if (mOffsetHeaderParent.getVisibility() != View.VISIBLE) {
                mOffsetHeaderParent.setVisibility(View.VISIBLE);
//				if (!mShowing) {
//					mShowAnimation.start();
//				}
                if (mCurrentYear == mYear && mCurrentMonth == mMonth)
                    mListView.setMode(Mode.PULL_FROM_END);
                else {
                    mListView.setMode(Mode.BOTH);
                }
            }
        } else {
            if (mOffsetHeaderParent.getVisibility() != View.GONE) {
                mOffsetHeaderParent.setVisibility(View.GONE);
//				if (mShowing) {
//					mOffsetHeaderParent.clearAnimation();
//				}
                mListView.setMode(Mode.PULL_FROM_START);
                mListView.setPullDownLable("下拉获取当月考勤记录");
                mListView.setPullDownReleaseLable("放开开始获取数据");
            }
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        /**
         * 权限模块的控制,统一放在@MenuBaseActivity#onResume里面处理
         * @author jinjr
         * @date 16-3-21 下午3:41
         *
        if (HcUtil.isEmpty(SettingHelper.getToken(mContext))) {
        Intent login = new Intent(mContext, LoginActivity.class);
        login.putExtra("loginout", false);
        mContext.startActivityForResult(login, HcUtil.REQUEST_CODE_LOGIN);
        return;
        } else {

        SignCache.getInstance().configExist(mContext);

        if (mHandler != null) {
        mHandler.resume(DateHandler.WHAT_UPDATE_DISTANCE);
        mHandler.resume(DateHandler.WHAT_UPDATE_TIME);
        }
        refreshDistanceUI(SignLoctionUtils.getDistance());

        if (mOffsetHeaderParent.getVisibility() != View.VISIBLE) {
        onRefresh();
        //HcHttpRequest.getRequest().sendSignItemCommand(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH, System.currentTimeMillis()), this);
        }
        }
         */
        SignCache.getInstance().configExist(mContext);

        if (mHandler != null) {
            mHandler.resume(DateHandler.WHAT_UPDATE_DISTANCE);
            mHandler.resume(DateHandler.WHAT_UPDATE_TIME);
        }
        refreshDistanceUI(SignLoctionUtils.getDistance());

        if (mOffsetHeaderParent.getVisibility() != View.VISIBLE) {
            onRefresh();
            //HcHttpRequest.getRequest().sendSignItemCommand(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH, System.currentTimeMillis()), this);
        }


//		if (HcUtil.isNetWorkAvailable(mContext)) {
//			if (mDistanceText.getVisibility() != View.VISIBLE)
//				mDistanceText.setVisibility(View.VISIBLE);
//			if (mDistanceValue.getVisibility() != View.VISIBLE)
//				mDistanceValue.setVisibility(View.VISIBLE);
//			if (mNetworkError.getVisibility() != View.GONE)
//				mNetworkError.setVisibility(View.GONE);
//			
//		} else {
//			mStatus.setImageResource(R.drawable.none_sign);
//			if (mDistanceText.getVisibility() != View.GONE)
//				mDistanceText.setVisibility(View.GONE);
//			if (mDistanceValue.getVisibility() != View.GONE)
//				mDistanceValue.setVisibility(View.GONE);
//			if (mNetworkError.getVisibility() != View.VISIBLE)
//				mNetworkError.setVisibility(View.VISIBLE);
//		}
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (mHandler != null) {
            mHandler.pause(DateHandler.WHAT_UPDATE_DISTANCE);
            mHandler.pause(DateHandler.WHAT_UPDATE_TIME);
        }

    }

    @Override
    public void onDestory() {
        // TODO Auto-generated method stub
        SignLoctionUtils.stopLocation();
    }

    @Override
    public boolean onDownTouch() {
        // TODO Auto-generated method stub
        if (mListView.getMode() == Mode.PULL_FROM_START) {
            return false;
        }
        if (mListView.getFirstVisiblePosition() == 0) {
            View view = mListView.getChildAt(0);
            if (view != null && view.getTop() >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        /**
         * 权限模块的控制,统一放在@MenuBaseActivity#onActivityResult里面处理
         * @author jinjr
         * @date 16-3-21 下午3:41
         *
        // 失败跳转界面
        // 成功onresum处理
        if (requestCode == HcUtil.REQUEST_CODE_LOGIN) {
        if (resultCode == HcUtil.LOGIN_SUCCESS)
        ;// 在onresume里处理
        else {
        if (mContext instanceof ContainerActivity) { // 通讯录从应用容器里面进入
        HcAppState.getInstance().removeActivity(mContext);
        mContext.finish();
        } else if (mContext instanceof MenuBaseActivity) {
        HcUtil.startPreActivity(mContext);//登录取消之后跳转到之前的tab
        } else {
        HcAppState.getInstance().removeActivity(mContext);
        mContext.finish();
        }

        }
        }
         */
    }

    @Override
    public void notify(Object data, RequestCategory request,
                       ResponseCategory category) {
        // TODO Auto-generated method stub
        HcDialog.deleteProgressDialog();
        switch (request) {
            case SIGN:
                switch (category) {
                    case SUCCESS:
                        if (data != null && data instanceof String) {
                            try {
                                JSONObject object = new JSONObject((String) data);
                                if (HcUtil.hasValue(object, "msg")) {
                                    HcUtil.showToast(mContext, object.getString("msg"));
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        } else {
                            ; // do nothing
                        }
                        // 刷新列表
                        onRefresh();
//				HcHttpRequest.getRequest().sendSignItemCommand(HcUtil.getDate(HcUtil.FORMAT_YEAR_MONTH, System.currentTimeMillis()), this);
                        break;
                    case NETWORK_ERROR:
                        HcUtil.toastNetworkError(mContext);

                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);

//				if (data != null && data instanceof String) {
//					HcUtil.showToast(mContext, (String) data);
//				} else {
//					HcLog.D(TAG + " notify SIGN DATA_ERROR! data = "+data);
//				}

                        break;
                    case SESSION_TIMEOUT:
                        HcUtil.toastTimeOut(mContext);

                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);

                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        /**
                         * czx
                         * 2016.4.13
                         */
                        if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
                                HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());
                        }


                        break;

                    default:
                        break;
                }
                break;
            case SIGNITEM:
                mListView.onRefreshComplete();
                switch (category) {
                    case SUCCESS:
                        if (data != null && data instanceof String)
                            try {
                                JSONObject object = new JSONObject((String) data).getJSONObject("body");
                                SignMonthInfo info = new SignMonthInfo();
                                if (HcUtil.hasValue(object, "searchDate")) {
                                    info.mMonth = object.getString("searchDate");
                                }
                                if (HcUtil.hasValue(object, "workInTime")) {
                                    info.mWorkInTime = object.getString("workInTime");
                                }
                                if (HcUtil.hasValue(object, "workOutTime")) {
                                    info.mWorkOutTime = object.getString("workOutTime");
                                }
                                if (HcUtil.hasValue(object, "lateAmount")) {
                                    info.mLateAmount = object.getInt("lateAmount");
                                }
                                if (HcUtil.hasValue(object, "leavEearlyAmount")) {
                                    info.mLeavEearlyAmount = object.getInt("leavEearlyAmount");
                                }
                                if (HcUtil.hasValue(object, "absenteeismAmount")) {
                                    info.mAbsenteeismAmount = object.getInt("absenteeismAmount");
                                }
                                if (HcUtil.hasValue(object, "signList")) {
                                    JSONArray array = object.getJSONArray("signList");
                                    int size = array.length();
                                    SignDayInfo dayInfo;
                                    for (int i = 0; i < size; i++) {
                                        object = array.getJSONObject(i);
                                        dayInfo = new SignDayInfo();
                                        dayInfo.mMonth = info.mMonth;
                                        dayInfo.mWorkInTime = info.mWorkInTime;
                                        dayInfo.mWorkOutTime = info.mWorkOutTime;

                                        if (HcUtil.hasValue(object, "id")) {
                                            dayInfo.mId = object.getString("id");
                                        }
                                        if (HcUtil.hasValue(object, "signDate")) {
                                            dayInfo.mDate = object.getString("signDate");
                                        }
                                        if (HcUtil.hasValue(object, "signInType")) {
                                            dayInfo.mSignInType = object.getString("signInType");
                                        }
                                        if (HcUtil.hasValue(object, "signInTime")) {
                                            dayInfo.mSignInTime = object.getString("signInTime");
                                        }
                                        if (HcUtil.hasValue(object, "signOutType")) {
                                            dayInfo.mSignOutType = object.getString("signOutType");
                                        }
                                        if (HcUtil.hasValue(object, "signOutTime")) {
                                            dayInfo.mSignOutTime = object.getString("signOutTime");
                                        }
                                        info.mDayInfos.add(dayInfo);
                                    }
                                }

                                mCacheMap.put(info.mMonth, info);
                                // 刷新界面
                                updateMonthSign(info);


                            } catch (Exception e) {
                                // TODO: handle exception
                                HcUtil.toastDataError(mContext);
                                HcLog.D(TAG + " notify SIGNITEM e = " + e);
                                reSet();
                                mEmpty.setText("获取数据失败！");
                            }
                        break;
                    case NETWORK_ERROR:
                        HcUtil.toastNetworkError(mContext);
                        reSet();
                        mEmpty.setText("网络不给力!");
                        break;
                    case DATA_ERROR:
                        HcUtil.toastDataError(mContext);
//				if (data != null && data instanceof String) {
//					HcUtil.showToast(mContext, (String) data);
//				} else {
//					HcLog.D(TAG + " notify SIGNITEM DATA_ERROR! data = "+data);
//				}
                        reSet();
                        mEmpty.setText("获取数据失败！");
                        break;
                    case SESSION_TIMEOUT:
                        HcUtil.toastTimeOut(mContext);
                        reSet();
                        mEmpty.setText("网络不给力!");
                        break;
                    case SYSTEM_ERROR:
                        HcUtil.toastSystemError(mContext, data);
                        reSet();
                        mEmpty.setText("获取数据失败！");
                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        reSet();
                        mEmpty.setText(info.getMsg());
                        /**
                         * czx
                         * 2016.4.13
                         */
                        if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
                                HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
                            HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
                        } else {
                            HcUtil.showToast(mContext, info.getMsg());

                        }

                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
        // TODO Auto-generated method stub

    }

    private void refreshDistanceUI(String distance) {
        HcLog.D(TAG + " #refreshDistanceUI distance = " + distance);
        mSign = false;
        if (HcUtil.isNetworkConnected(mContext)) {

            if (mDistanceText.getVisibility() != View.VISIBLE)
                mDistanceText.setVisibility(View.VISIBLE);
            if (mDistanceValue.getVisibility() != View.VISIBLE)
                mDistanceValue.setVisibility(View.VISIBLE);
            if (mNetworkError.getVisibility() != View.GONE)
                mNetworkError.setVisibility(View.GONE);
            if (TextUtils.isEmpty(distance)) {
                mStatus.setImageResource(R.drawable.middle_sign);
                mDistanceValue.setText("不能定位或距离太远");
            } else {
                double dis = Double.parseDouble(distance);

                if (dis <= 1000) {
                    mDistanceValue.setText(distance + " 米");
                } else if (dis > 1000 && dis < 100000) {
                    String diskilo = new DecimalFormat("0.0")
                            .format(dis / 1000);
                    mDistanceValue.setText(diskilo + " 公里");
                } else {
                    mDistanceValue.setText("不能定位或距离太远");
                }

                if (dis < SignCache.getInstance().getMaxDistance()) {
                    mStatus.setImageResource(R.drawable.ok_sign);
                    mSign = true;
                } else {
                    mStatus.setImageResource(R.drawable.middle_sign);

                }
            }

        } else {
            mStatus.setImageResource(R.drawable.none_sign);
            if (mDistanceText.getVisibility() != View.GONE)
                mDistanceText.setVisibility(View.GONE);
            if (mDistanceValue.getVisibility() != View.GONE)
                mDistanceValue.setVisibility(View.GONE);
            if (mNetworkError.getVisibility() != View.VISIBLE)
                mNetworkError.setVisibility(View.VISIBLE);
        }

    }


    private void onRefresh() {
        if (HcUtil.isNetWorkAvailable(mContext)) {
            mEmpty.setText("正在获取当月数据...");
            mRefreshType = CURRENT_MONTH;
            mCurrentYear = mYear;
            mCurrentMonth = mMonth;
            HcLog.D(TAG + " #onRefresh year = " + mCurrentYear + " month = " + mCurrentMonth);
            String month = mCurrentMonth < 10 ? "0" + mCurrentMonth : "" + mCurrentMonth;
            HcHttpRequest.getRequest().sendSignItemCommand(mCurrentYear + "-" + month, this);
        }
    }

    private void onHistory() {
        if (HcUtil.isNetWorkAvailable(mContext)) {
            mRefreshType = HISTORY_MONTH;
//			mCurrentMonth --;
//			HcLog.D(TAG + " #onHistory year = "+ mCurrentYear + " month = "+mCurrentMonth);
//			if (mCurrentMonth <= 0) {
//				mCurrentYear--;
//				mCurrentMonth = 12;
//			}
            mEmpty.setText("正在获取数据...");
            HcLog.D(TAG + " #onHistory year = " + mCurrentYear + " month = " + mCurrentMonth);
            String month = mCurrentMonth < 10 ? "0" + mCurrentMonth : "" + mCurrentMonth;
            HcHttpRequest.getRequest().sendSignItemCommand(mCurrentYear + "-" + month, this);
        }

    }

    /**
     * 这个是获取更多数据的时候,请求失败,然后重整数据
     *
     * @author jrjin
     * @time 2016-1-5 下午7:22:36
     */
    private void reSet() {
        /** 这里需要测试 */
        if (mCurrentMonth == mMonth && mCurrentYear == mYear) return;
        mCurrentMonth++;
        if (mCurrentMonth > 12) {
            mCurrentMonth = 1;
            mCurrentYear++;
        }
    }

    private void update(SignMonthInfo info) {
        if (mRefreshType == CURRENT_MONTH) {
            mSignItems.clear();
        }
        mSignItems.addAll(info.mDayInfos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCompleted() {
        // TODO Auto-generated method stub
        HcLog.D(TAG + " #onCompleted ==================");
        switch (mListView.getMode()) {
            case PULL_FROM_START:
                mListView.setPullDownLable("下拉获取当月考勤记录");
                mListView.setPullDownReleaseLable("放开开始获取数据");
                break;
            case PULL_FROM_END:
                mListView.setPullUpLable("上拉获取" + getYearMonth(mCurrentYear, mCurrentMonth, true) + "月考勤记录");
                mListView.setPullUpReleaseLable("放开开始获取数据");
                break;

            default:
                break;
        }

    }

    /**
     * 上拉的时候调用
     *
     * @return
     * @author jrjin
     * @time 2016-1-6 上午10:43:07
     */
    private String getLastMonthKey() {
        mCurrentMonth--;
        if (mCurrentMonth <= 0) {
            mCurrentYear--;
            mCurrentMonth = 12;
        }
        String month = mCurrentMonth < 10 ? "0" + mCurrentMonth : "" + mCurrentMonth;
        return mCurrentYear + "-" + month;
    }

    /**
     * 下拉的时候调用
     *
     * @return
     * @author jrjin
     * @time 2016-1-6 上午10:42:39
     */
    private String getNextMonthKey() {
        mCurrentMonth++;
        if (mCurrentMonth > 12) {
            mCurrentMonth = 1;
            mCurrentYear++;
        }
        String month = mCurrentMonth < 10 ? "0" + mCurrentMonth : "" + mCurrentMonth;
        return mCurrentYear + "-" + month;
    }

    private void updateMonthSign(SignMonthInfo info) {
        if (mCurrentYear == mYear && mCurrentMonth == mMonth) {
            if (mListView.getMode() == Mode.BOTH) {
                mListView.setMode(Mode.PULL_FROM_END);
            }
        } else if (mCurrentYear != mYear || mCurrentMonth != mMonth) {
            if (mListView.getMode() != Mode.BOTH) {
                mListView.setMode(Mode.BOTH);
            }
        }
        switch (mListView.getMode()) {
            case PULL_FROM_START:
                mListView.setPullDownLable("下拉获取当月考勤记录");
                mListView.setPullDownReleaseLable("放开开始获取数据");
                break;
            case PULL_FROM_END:
            case BOTH:

                mListView.setPullDownLable("下拉获取" + getYearMonth(mCurrentYear, mCurrentMonth, false) + "月考勤记录");
                mListView.setPullDownReleaseLable("放开开始获取数据");
                mListView.setPullUpLable("上拉获取" + getYearMonth(mCurrentYear, mCurrentMonth, true) + "月考勤记录");
                mListView.setPullUpReleaseLable("放开开始获取数据");
                break;
            default:
                break;
        }
        mSignItems.clear();
        mSignItems.addAll(info.mDayInfos);
        mAdapter.notifyDataSetChanged();
        // 显示面板数据
        mYearValue.setText("" + mCurrentYear);
        mMonthValue.setText("" + mCurrentMonth);
        if (mSignItems.size() == 0) {
            mTimes.setText("迟到：0  早退：0  缺勤：0");
            mEmpty.setText(mCurrentYear + "年" + mCurrentMonth + "月没有考勤记录");
        } else {
            mTimes.setText("迟到：" + info.mLateAmount + " 早退：" + info.mLeavEearlyAmount
                    + " 缺勤：" + info.mAbsenteeismAmount);
        }

    }

    private String getYearMonth(int year, int month, boolean pullUp) {
        if (pullUp) {
            month--;
            if (month <= 0) {
                year--;
                month = 12;
            }

        } else {
            month++;
            if (month > 12) {
                month = 1;
                year++;
            }
        }

        return year + "年" + month + "月";
    }
}
