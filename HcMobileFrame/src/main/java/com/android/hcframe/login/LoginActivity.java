/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-3-11 下午1:45:07
 */
package com.android.hcframe.login;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.hcframe.CacheManager;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.TopBarView;
import com.android.hcframe.adapter.LoginUserAdapter;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.lock.LockActivity;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.push.PushManager;
import com.android.hcframe.sql.SettingHelper;

public class LoginActivity extends HcBaseActivity implements OnClickListener,
        HcObserver, OnItemClickListener, OnFocusChangeListener {

    private static final String TAG = "LoginActivity";

    private ImageView mUserSelect;
    private EditText mUser;
    private EditText mPw;
    private TextView mLogin;
    private ListView mUserList;
    private LoginUserAdapter mAdapter;

    private List<String> mUsers = new ArrayList<String>();

    private LoginManager mManager;

    private String account;

    private TopBarView login_top_bar;

    private boolean mLoginOut = true;

    private boolean iserror = false;

    private PushManager pushMgr = new PushManager();

    private TextView forget_pwd_login;

    private TextView register_user_login;

    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null)
            mLoginOut = intent.getExtras().getBoolean("loginout", true);

        setContentView(R.layout.activity_login);
        initViews();
        initData();
    }

    private void initViews() {
        mUserSelect = (ImageView) findViewById(R.id.login_select_btn);
        mUser = (EditText) findViewById(R.id.login_user);
        mPw = (EditText) findViewById(R.id.login_pw);
        mLogin = (TextView) findViewById(R.id.login_btn);
        mUserList = (ListView) findViewById(R.id.login_user_list);
        login_top_bar = (TopBarView) findViewById(R.id.login_top_bar);
        forget_pwd_login = (TextView) findViewById(R.id.forget_pwd_login);
        register_user_login = (TextView) findViewById(R.id.register_user_login);
        login_top_bar.setTitle(getString(R.string.user_login));
        login_top_bar.setSettingsVisiable(View.GONE);

        mCheckBox = (CheckBox) findViewById(R.id.remember_pwd);

        if (HcConfig.getConfig().canForgetPw()) {
            forget_pwd_login.setOnClickListener(this);
        } else {
            forget_pwd_login.setVisibility(View.INVISIBLE);
        }

        mLogin.setOnClickListener(this);
        mUserSelect.setOnClickListener(this);
        mUserList.setOnItemClickListener(this);

        register_user_login.setOnClickListener(this);
        mPw.setOnFocusChangeListener(this);
        mUser.setOnFocusChangeListener(this);
        mUser.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                HcLog.D("LoginActivity onEditorAction mUser onKey KEYCODE_ENTER! view = " + v + " actionId = " + actionId);

				/*判断是否是“NEXT”键*/
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mUserList.setVisibility(View.GONE);
                    mPw.requestFocus();
//                	/*隐藏软键盘*/  
//                    InputMethodManager imm = (InputMethodManager) v  
//                            .getContext().getSystemService(  
//                                    Context.INPUT_METHOD_SERVICE);  
//                    if (imm.isActive()) {  
//                        imm.hideSoftInputFromWindow(  
//                                v.getApplicationWindowToken(), 0);  
//                    }  

                    return true;
                }
                return false;
            }
        });

        mPw.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                HcLog.D("LoginActivity onEditorAction mPw onKey KEYCODE_ENTER! view = " + v + " actionId = " + actionId);
				/*判断是否是“DONE”键*/
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                	/*隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(
                                v.getApplicationWindowToken(), 0);
                    }
                    loginin();
                    return true;
                }
                return false;
            }
        });

//		mUser.setOnKeyListener(new OnKeyListener() {
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if (v.hasFocus() && keyCode == KeyEvent.KEYCODE_ENTER) {
//					HcLog.D("LoginActivity mUser onKey KEYCODE_ENTER! view = "+v);
////					mPw.requestFocus();
//					mUserList.setVisibility(View.GONE);
//					return true;
//				}
//				return false;
//			}
//		});
//		mPw.setOnKeyListener(new OnKeyListener() {
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if (v.hasFocus() && keyCode == KeyEvent.KEYCODE_ENTER) {
//					HcLog.D("LoginActivity mPw onKey KEYCODE_ENTER! view = "+v);
//					loginin();
//					return true;
//				}
//				return false;
//			}
//		});

        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                SettingHelper.setLoginAuto(LoginActivity.this, isChecked);
//				HcUtil.twoBtnAlterDialog(LoginActivity.this, "asdasd");
            }
        });

    }

    private void initData() {
        if (HcConfig.getConfig().canRegister()) {
            register_user_login.setVisibility(View.VISIBLE);
        } else {
            register_user_login.setVisibility(View.INVISIBLE);
        }
        mCheckBox.setChecked(SettingHelper.getLoginAuto(this));
        mManager = new LoginManager();
        String accounts = SettingHelper.getLoginUsers(this);
        if (!TextUtils.isEmpty(accounts)) {
            String[] account = accounts.split("&");
            mUsers.addAll(Arrays.asList(account));
            if (account.length > 0)
                mUser.setText(account[0]);
        }

        if (mAdapter == null) {
            mAdapter = new LoginUserAdapter(this, mUsers);
        }
        mUserList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.login_btn) {
            loginin();
        } else if (id == R.id.login_select_btn) {
            showList();
        } else if (id == R.id.forget_pwd_login) {
            startActivity(new Intent(this, RetrieveActivityOne.class));
        } else if (id == R.id.register_user_login) {
            startActivity(new Intent(this, RegisterActivityOne.class));
        }
		/*
		 * switch (v.getId()) { case R.id.login_btn: account =
		 * mUser.getText().toString().trim(); String pw =
		 * mPw.getText().toString(); if (TextUtils.isEmpty(account)) {
		 * HcUtil.showToast(this, R.string.toast_login_accout_null); return; }
		 * if (TextUtils.isEmpty(pw)) { HcUtil.showToast(this,
		 * R.string.toast_login_pw_null); return; } if
		 * (!HcUtil.isNetWorkError(this)) { HcDialog.showProgressDialog(this,
		 * R.string.toast_login_dialog); mManager.login(account, pw,
		 * HcUtil.getIMEI(this)); } break; case R.id.login_select_btn:
		 * showList(); break;
		 * 
		 * default: break; }
		 */
    }

    public void loginin() {
        account = mUser.getText().toString().trim();
        String pw = mPw.getText().toString();
        if (TextUtils.isEmpty(account)) {
            HcUtil.showToast(this, R.string.toast_login_accout_null);
            return;
        }
        if (TextUtils.isEmpty(pw)) {
            HcUtil.showToast(this, R.string.toast_login_pw_null);
            return;
        }
        if (!HcUtil.isNetWorkError(this)) {
            HcDialog.showProgressDialog(this, R.string.toast_login_dialog);
            mManager.login(account, pw, HcUtil.getIMEI(this));
        }
    }

    private void showList() {
        if (!mUsers.isEmpty() && mUserList.getVisibility() != View.VISIBLE) {
            mUserList.setVisibility(View.VISIBLE);
            mUserList.requestFocus();
        } else {
            mUserList.setVisibility(View.GONE);
        }
    }

    private void startLockActivity() {
        Intent intent = new Intent(this, LockActivity.class);
        startActivity(intent);
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        // TODO Auto-generated method stub
        if (subject != null && subject instanceof LoginManager) {
            HcDialog.deleteProgressDialog();
            if (request == RequestCategory.LOGIN) {
                switch (response) {
                    case SESSION_TIMEOUT:
                    case NETWORK_ERROR:
                        HcUtil.toastTimeOut(this);
                        // startLockActivity();
                        break;
                    case SUCCESS:
                        JSONObject object;
                        String token = "";
                        String code = "0";
                        String msg = "";
                        String vpn_account = "";
                        String vpn_passwd = "";
                        String name = "";
                        String user_icon = "";
                        String mobile_phone = "";
                        String permisstion = "";
                        String userId = "";
                        String email = "";
                        try {
                            object = new JSONObject((String) data);
                            code = object.getString("code");
                            msg = object.getString("msg");
                            if ("0".equals(code)) {
                                JSONObject body = object.getJSONObject("body");
                                if (HcUtil.hasValue(body, "token")) {
                                    token = body.getString("token");
                                }
                                if (HcUtil.hasValue(body, "vpn_account")) {
                                    vpn_account = body.getString("vpn_account");
                                }
                                if (HcUtil.hasValue(body, "vpn_passwd")) {
                                    vpn_passwd = body.getString("vpn_passwd");
                                }

                                if (HcUtil.hasValue(body, "name")) {
                                    name = body.getString("name");
                                }

                                if (HcUtil.hasValue(body, "mobile_phone")) {
                                    mobile_phone = body.getString("mobile_phone");
                                }

                                if (HcUtil.hasValue(body, "user_icon")) {
                                    user_icon = body.getString("user_icon");
                                }

                                if (HcUtil.hasValue(body, "appIds")) {
                                    permisstion = body.getString("appIds");
                                }

                                if (HcUtil.hasValue(body, "user_id")) {
                                    userId = body.getString("user_id");
                                }
                                /**
                                 *@author jinjr
                                 *@date 17-3-16 上午9:16
                                 */
                                if (HcUtil.hasValue(body, "email")) {
                                    email = body.getString("email");
                                }
                            }
                        } catch (JSONException e) {
                            HcUtil.toastDataError(this);
                            HcLog.D(TAG + " #updateData JSONException = " + e);
                        }
                        if ("0".equals(code)) {
                            HcLog.D(TAG + " mLoginOut = " + mLoginOut);

                            if (!mLoginOut) { // 说明用户没有进行登出操作，从未登录状态--->登录状态


                                /**
                                 *@author jinjr
                                 *@date 16-3-11 下午4:30
                                 */
                                CacheManager.getInstance().clearCaches(false);
                            } else {
                                BadgeCache.getInstance().clearCache(false);
                            }

                            // 绑定用户

                            // if
                            // (TextUtils.isEmpty(SettingHelper.getAccount(this))) {
                            // // 说明从未登录状态--->登录状态 ：需要存储未登录时的数据
                            // // 要是从登录-->退出--->登录状态
                            // }

                            HcLog.D(TAG + " before set account = "
                                    + SettingHelper.getAccount(this));

                            HcLog.D(TAG + " token = " + token + " vpn_account = "
                                    + vpn_account + " vpn_pw = " + vpn_passwd + " permisstion = " + permisstion + " msg = " + msg + " userId = "+userId);
                            SettingHelper.setAccount(this, account);
                            SettingHelper.setToken(this, token);
                            SettingHelper.setLoginUsers(this, account);
                            // startActivity(new Intent(this, Menu1Activity.class));

                            SettingHelper.setVpnAccount(this, vpn_account);
                            SettingHelper.setVpnPwd(this, vpn_passwd);
                            SettingHelper.setName(this, name);
                            SettingHelper.setIcon(this, user_icon);
                            SettingHelper.setMobile(this, mobile_phone);

                            /**
                             * @author jrjin
                             * @date 2016-06-02 13:53
                             */
                            SettingHelper.setUserId(this, userId);

                            /**
                             *@author jinjr
                             *@date 17-3-16 上午9:16
                             */
                            SettingHelper.setEmail(this, email);


                            SettingHelper.setOperatePermisstion(this, permisstion);
                            HcConfig.getConfig().updatePermisstion(this, false);

                            setResult(HcUtil.LOGIN_SUCCESS);
                            HcAppState.getInstance().removeActivity(this);
                            BadgeCache.getInstance().createBadge(this);

                            // 记录登录的MD5密码
                            SettingHelper.setIMPW(this, mPw.getText().toString());
                            // 连接IM,这里需要判断,通过广播告知IM模块
                            Intent intent = new Intent("com.android.hcframe.login");
                            intent.setPackage(getPackageName());
                            sendBroadcast(intent);


                            LoginActivity.this.finish();

                            // HcDialog.showProgressDialog(this, R.string.binding);
                            // // 绑定用户
                            // pushMgr.sendBindChannel(SettingHelper.getAccount(this),
                            // SettingHelper.getChannelId(this), HcConfig
                            // .getConfig().getAppVersion(), 0 + "");

                            // if (mLoginOut) { //
                            // 保证去重新获取一次，确保HcAppData里的App列表去服务端获取过了，因为当页面不在应用列表页面的时候，退出登录然后直接退出会删除原来用户下的应用
                            // HcAppData.getInstance().refreshApps(HcAppData.APP_CATEGORY_ALL,
                            // RequestCategory.APP_ALL);
                            // }

                        } else { // 这里不会出现，请查看HcHttpRequest#Login#parseJson
                            HcUtil.showToast(this, msg);
                        }
                        break;
                    case SYSTEM_ERROR: // 增加不能登录的原因
//                        if (data != null && data instanceof String) {
//                            HcUtil.showToast(this, (String) data);
//                        }
                        HcUtil.toastSystemError(this, data);
                        break;
                    case REQUEST_FAILED:
                        ResponseCodeInfo info = (ResponseCodeInfo) data;
                        HcUtil.showToast(this, info.getMsg());
                        break;
                    default:
//					// HcUtil.showToast(this, R.string.toast_login_failed);
//					mLogin.setText(getString(R.string.user_pwd_error));
//					iserror = true;
                        break;
                }
            }

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mManager.addObserver(this);
        pushMgr.addObserver(this);
        mUser.setSelection(mUser.getText().toString().length());
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mManager.removeObserver(this);
        pushMgr.removeObserver(this);
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        String account = (String) parent.getItemAtPosition(position);
        mUser.setText(account);
        mUser.setSelection(account.length());
        mUserList.setVisibility(View.GONE);
        mUser.setSelection(account.length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getApplicationContext(), "切换为横屏", Toast.LENGTH_SHORT)
                    .show();
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(getApplicationContext(), "切换为竖屏", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onFocusChange(View view, boolean isfocused) {
        if (view.getId() == R.id.login_pw && isfocused && iserror) {
            mLogin.setText(R.string.at_once_login);
            iserror = !iserror;
        } else if (view == mUser && isfocused) {
            mUserList.setVisibility(View.GONE);
        }
    }

    public void hindListView() {
        mUserList.setVisibility(View.GONE);
    }
}
