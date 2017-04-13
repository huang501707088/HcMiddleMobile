/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-12-10 上午9:20:49
*/
package com.android.hcframe.internalservice.contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.CommonActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.contacts.data.ContactsCacheData;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.contacts.data.DepartmentInfo;
import com.android.hcframe.contacts.data.EmployeeInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.sql.SettingHelper;

public class ContactPage extends AbstractPage implements HcObserver,
        OnItemClickListener, OnFocusChangeListener, TextWatcher {

    private static final String TAG = "ContactPage";

    /**
     * 部门和员工切换布局
     */
    private LinearLayout mSwitchParent;
    private ImageView mSwitchBtn;
    private RelativeLayout mShowSearch;
    /**
     * 搜索布局
     */
    private LinearLayout mSearchParent;
    private EditText mSearchContent;
    private ImageView mSearchClear;
    private TextView mSearchCanel;
    /**
     * 部门操作布局
     * @deprecated
     */
    private LinearLayout mDepParent;
    /**
     * @deprecated
     */
    private RelativeLayout mReturnTop;
    /**
     * @deprecated
     */
    private RelativeLayout mReturnPre;
    /**
     * 显示部门层级关系
     */
    private LinearLayout mDepNameParent;
    private TextView mDepName;

    private ListView mListView;

    private SideBar mSideBar;

    private DataType mPreDataType = DataType.NONE;

    private DataType mDataType = DataType.EMPLOYEE;

    private SearchAdapter mSearchAdapter;

    private EmployeeAdapter mEmployeeAdapter;

    private DepartmentAdapter mDepartmentAdapter;

    private List<ContactsInfo> mContactsInfos = new ArrayList<ContactsInfo>();
    /**
     * 保存部门层级关系的stack,不包括root
     */
    private Stack<ContactsInfo> mDepStack = new Stack<ContactsInfo>();
    /**
     * 判断在onResume里面是否需要去更新
     */
    private boolean mRefresh = true;

    /** 应用的ID或者是部门的ID */
    private final String mAppId;

    /**
     * 原始的状态,用于判断是否在下级界面
     */
    private final DataType mOriginal;

    /** 是否是IM模块 */
    private final boolean mIMModule;

    /** 进入详情 */
    private static final int REQUEST_CODE_DETAIL = 1;
    /** 进入下级部门 */
    private static final int REQUEST_CODE_DEP = 2;

    private InputMethodManager mManager;

    public ContactPage(Activity context, ViewGroup group, String appId, DataType type, boolean imModule) {
        super(context, group);
        // TODO Auto-generated constructor stub
        mAppId = appId;
        mDataType = type;
        mOriginal = type;
        mIMModule = imModule;
        mManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.contact_home_switch) {
            if (mDataType == DataType.EMPLOYEE) {
                if (mOriginal == DataType.DEPARTEMT_SUB) {
                    updateViews(DataType.DEPARTEMT_SUB);
                } else {
                    updateViews(DataType.DEPARTEMT_SEARCH);
                }

            } else {
                updateViews(DataType.EMPLOYEE);
            }
        } else if (id == R.id.contact_home_showsearch_btn) {
            mPreDataType = mDataType;
            updateViews(DataType.SEARCH);
        } else if (id == R.id.conatct_home_search_clear) {
            mSearchContent.setText("");
            mContactsInfos.clear();
            mSearchAdapter.notifyDataSetChanged();
        } else if (id == R.id.contact_home_search_cancel) {
            mManager.hideSoftInputFromWindow(mSearchContent.getWindowToken(), 0);
            updateViews(mPreDataType);
        } else if (id == R.id.contact_home_return_top) { // not used
            updateViews(DataType.DEPARTEMT_SEARCH);
        } else if (id == R.id.contact_home_return_pre) { // not used
            ContactsInfo info = mDepStack.pop();
            if (mDepStack.isEmpty()) {
                updateViews(DataType.DEPARTEMT_SEARCH);
            } else {
                updateViews(DataType.DEPARTEMT);
                String name = ">" + info.getName();
                String depName = mDepName.getText().toString();
                mDepName.setText(depName.substring(0, depName.length() - name.length()/* + 1*/));
            }
        } else if (id == R.id.topbar_back_btn) {
            ContactsInfo info = mDepStack.pop();
            if (mDepStack.isEmpty()) {
                mContext.finish();
            } else {
                updateViews(DataType.DEPARTEMT_SUB);
                String name = ">" + info.getName();
                String depName = mDepName.getText().toString();
                mDepName.setText(depName.substring(0, depName.length() - name.length()));
            }
        } else if (id == R.id.topbar_menu_btn) { // 需要测试
        	mDepStack.clear();
            mContext.finish();
        }
    }

    @Override
    public void initialized() {
        // TODO Auto-generated method stub
        ContactsCacheData.getInstance().addObserver(this);
        if (!TextUtils.isEmpty(SettingHelper.getAccount(mContext))) {
            ContactsCacheData.getInstance().moduleCheck(mContext);
        }
    }

    @Override
    public void setContentView() {
        // TODO Auto-generated method stub
        if (mView == null) {
            mView = mInflater.inflate(R.layout.contacts_home_layout, null);

            mSwitchParent = (LinearLayout) mView.findViewById(R.id.contact_home_emp_parent);
            mSwitchBtn = (ImageView) mView.findViewById(R.id.contact_home_switch);
            mShowSearch = (RelativeLayout) mView.findViewById(R.id.contact_home_showsearch_btn);

            mSearchParent = (LinearLayout) mView.findViewById(R.id.contact_home_search_parent);
            mSearchContent = (EditText) mView.findViewById(R.id.contact_home_search_content);
            mSearchClear = (ImageView) mView.findViewById(R.id.conatct_home_search_clear);
            mSearchCanel = (TextView) mView.findViewById(R.id.contact_home_search_cancel);

            mDepParent = (LinearLayout) mView.findViewById(R.id.contact_home_dep_parent);
            mReturnTop = (RelativeLayout) mView.findViewById(R.id.contact_home_return_top);
            mReturnPre = (RelativeLayout) mView.findViewById(R.id.contact_home_return_pre);

            mDepNameParent = (LinearLayout) mView.findViewById(R.id.contact_home_dep_name_parent);
            mDepName = (TextView) mView.findViewById(R.id.contact_home_dep_name);

            mListView = (ListView) mView.findViewById(R.id.contact_home_listview);

            mSideBar = (SideBar) mView.findViewById(R.id.contact_home_sideBar);

            mListView.setOnItemClickListener(this);
            mSearchContent.addTextChangedListener(this);
            mSearchContent.setOnFocusChangeListener(this);

            mSwitchBtn.setOnClickListener(this);
            mShowSearch.setOnClickListener(this);
            mSearchClear.setOnClickListener(this);
            mSearchCanel.setOnClickListener(this);
            mReturnTop.setOnClickListener(this);
            mReturnPre.setOnClickListener(this);

            mSideBar.setListView(mListView);

            if (mOriginal == DataType.DEPARTEMT_SUB) {
                mContext.findViewById(R.id.topbar_back_btn).setOnClickListener(this);

                /** 增加关闭按钮 */
                ImageView delete = (ImageView) mContext.findViewById(R.id.topbar_menu_btn);
                delete.setImageResource(R.drawable.contacts_dep_close);
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(this);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub
        if (s != null) {
            String key = s.toString().trim();
            if (!TextUtils.isEmpty(key)) {
                if (mSearchClear.getVisibility() != View.VISIBLE) {
                    mSearchClear.setVisibility(View.VISIBLE);
                }
                key = key.toUpperCase();
                List<ContactsInfo> allInfos = ContactsCacheData.getInstance().getEmployees();
                if (!allInfos.isEmpty()) {
                    List<ContactsInfo> subInfos = new ArrayList<ContactsInfo>();
                    for (ContactsInfo info : allInfos) {
                        if (info.getName().contains(key) || info.getJianpin().contains(key) ||
                                info.getQuanpin().contains(key)) {
                            subInfos.add(info);
                        }
                    }

                    mContactsInfos.clear();
                    mContactsInfos.addAll(subInfos);
                    subInfos.clear();
                    mSearchAdapter.notifyDataSetChanged();
                }
            } else {
                mSearchClear.setVisibility(View.INVISIBLE);
                mContactsInfos.clear();
                if (mSearchAdapter != null) {
                    mSearchAdapter.notifyDataSetChanged();
                }
            }
        } else {
            mSearchClear.setVisibility(View.INVISIBLE);
            mContactsInfos.clear();
            if (mSearchAdapter != null) {
                mSearchAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        Object item = parent.getItemAtPosition(position);
        if (item instanceof EmployeeInfo) {
            EmployeeInfo emp = (EmployeeInfo) item;
            startContactDetailsActivity(emp);
        } else if (item instanceof DepartmentInfo) {
            DepartmentInfo dep = (DepartmentInfo) item;
            if (mOriginal == DataType.DEPARTEMT_SUB) { // 说明已经在下级界面,只需要替换数据
                mDepStack.push(dep);
                mDepName.setText(mDepName.getText().toString() + ">" + dep.getName());
                updateViews(DataType.DEPARTEMT_SUB);
            } else { // 说明在根目录界面,需要跳转到下级界面
                Intent intent = new Intent(mContext, CommonActivity.class);
                intent.putExtra("data", dep.getId());
                intent.putExtra("title", ((TextView) (mContext.findViewById(R.id.topbar_title))).getText());
                if (mIMModule) {
                    intent.putExtra("className", "com.android.hcframe.im.IMDepartmentMenuPage");
                    mContext.startActivityForResult(intent, REQUEST_CODE_DEP);
                } else {
                    intent.putExtra("className", "com.android.hcframe.internalservice.contacts.DepartmentMenuPage");
                    mContext.startActivity(intent);
                }


            }

        }
    }

    private void startContactDetailsActivity(EmployeeInfo emp) {
        Intent intent = new Intent(mContext, ContactDetailsAct.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("emp", emp);
        bundle.putBoolean("im", mIMModule);
        intent.putExtras(bundle);
        if (mIMModule) {
            mContext.startActivityForResult(intent, REQUEST_CODE_DETAIL);
        } else {
            mContext.startActivity(intent);
        }

//        mContext.overridePendingTransition(0, 0);
    }

    @Override
    public void updateData(HcSubject subject, Object data,
                           RequestCategory request, ResponseCategory response) {
        // TODO Auto-generated method stub
        if (request != null) {
            switch (request) {
                case CONTACTS_REQUEST:
                    HcDialog.deleteProgressDialog();
                    switch (response) {
                        case SUCCESS:
                            if (ContactsCacheData.getInstance().isEmpty()) {
                                HcUtil.showToast(mContext, "暂无数据!");
                            } else {
                                updateViews(mDataType);
                            }

                            break;
                        case DATA_ERROR:
                            HcUtil.toastDataError(mContext);
                            break;
                        case SESSION_TIMEOUT:
                        case NETWORK_ERROR:
                            HcUtil.toastTimeOut(mContext);
                            break;
                        case SYSTEM_ERROR:
                            HcUtil.toastSystemError(mContext, data);
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

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public enum DataType {
        /**
         * 按部门显示,显示部门的下级数据
         * @deprecated
         * @see #DEPARTEMT_SUB
         */
        DEPARTEMT,
        /**
         * 全部员工列表
         */
        EMPLOYEE,
        /**
         * 显示搜索
         */
        SEARCH,
        /**
         * 按部门显示,显示公司目录
         */
        DEPARTEMT_SEARCH,
        /**
         * 默认
         */
        NONE,
        /**
         * 按部门显示,显示部门的下级数据
         */
        DEPARTEMT_SUB
    }

    private void updateViews(DataType type) {
        mDataType = type;
        mContactsInfos.clear();
        switch (mDataType) {
            case DEPARTEMT:
                mSwitchBtn.setImageResource(R.drawable.dep_emp_switch);
                if (mSearchParent.getVisibility() != View.GONE) {
                    mSearchParent.setVisibility(View.GONE);
                }
                if (mSwitchParent.getVisibility() != View.GONE) {
                    mSwitchParent.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mDepParent.getVisibility() != View.VISIBLE) {
                    mDepParent.setVisibility(View.VISIBLE);
                }
                if (mDepNameParent.getVisibility() != View.VISIBLE) {
                    mDepNameParent.setVisibility(View.VISIBLE);
                }
                if (mDepartmentAdapter == null) {
                    mDepartmentAdapter = new DepartmentAdapter(mContext, mContactsInfos);
                }
                ContactsInfo info = mDepStack.peek();
                mContactsInfos.addAll(ContactsCacheData.getInstance().getContacts(info.getId()));
                mListView.setAdapter(mDepartmentAdapter);
                break;
            case EMPLOYEE:
                mSwitchBtn.setImageResource(R.drawable.emp_dep_switch);
                if (mSearchParent.getVisibility() != View.GONE) {
                    mSearchParent.setVisibility(View.GONE);
                }
                if (mDepNameParent.getVisibility() != View.GONE) {
                    mDepNameParent.setVisibility(View.GONE);
                }
                if (mDepParent.getVisibility() != View.GONE) {
                    mDepParent.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.VISIBLE) {
                    mSideBar.setVisibility(View.VISIBLE);
                }
                if (mSwitchParent.getVisibility() != View.VISIBLE) {
                    mSwitchParent.setVisibility(View.VISIBLE);
                }
                if (mEmployeeAdapter == null) {
                    mEmployeeAdapter = new EmployeeAdapter(mContext, mContactsInfos);
                }
                mContactsInfos.addAll(ContactsCacheData.getInstance().getEmployees());
                if (mContactsInfos.isEmpty()) {
                    HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
                }
                mEmployeeAdapter.addColors();
                mListView.setAdapter(mEmployeeAdapter);

                break;
            case SEARCH:
                if (mDepNameParent.getVisibility() != View.GONE) {
                    mDepNameParent.setVisibility(View.GONE);
                }
                if (mDepParent.getVisibility() != View.GONE) {
                    mDepParent.setVisibility(View.GONE);
                }
                if (mSwitchParent.getVisibility() != View.GONE) {
                    mSwitchParent.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mSearchParent.getVisibility() != View.VISIBLE) {
                    mSearchParent.setVisibility(View.VISIBLE);
                }
                mSearchContent.requestFocus();
                mManager.showSoftInput(mSearchContent, 0);
                mSearchContent.setText("");
                if (mSearchAdapter == null) {
                    mSearchAdapter = new SearchAdapter(mContext, mContactsInfos);
                }
                mSearchAdapter.addColors();
                mListView.setAdapter(mSearchAdapter);
                break;

            case DEPARTEMT_SEARCH:
                mSwitchBtn.setImageResource(R.drawable.dep_emp_switch);
                if (mDepParent.getVisibility() != View.GONE) {
                    mDepParent.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mSearchParent.getVisibility() != View.GONE) {
                    mSearchParent.setVisibility(View.GONE);
                }
                if (mDepNameParent.getVisibility() != View.VISIBLE) {
                    mDepNameParent.setVisibility(View.VISIBLE);
                }
                if (mSwitchParent.getVisibility() != View.VISIBLE) {
                    mSwitchParent.setVisibility(View.VISIBLE);
                }
                if (mDepartmentAdapter == null) {
                    mDepartmentAdapter = new DepartmentAdapter(mContext, mContactsInfos);
                }
                // 获取数据
                ContactsInfo root = ContactsCacheData.getInstance().getRoot();
                HcLog.D(TAG + " #updateViews root id = "+root.getId());
                if (TextUtils.isEmpty(root.getId())) { // 说明没有数据,一般不会出现这种情况
                    HcDialog.showProgressDialog(mContext, R.string.dialog_title_get_data);
                    ContactsCacheData.getInstance().getEmployees();
                    if (!TextUtils.isEmpty(root.getId())) { // 这里需要测试
                        HcDialog.deleteProgressDialog();
                        mDepStack.clear();
                        mContactsInfos.addAll(root.getAll());
                        mDepName.setText(root.getName());
                    }
                } else {
                    mDepStack.clear();
                    mContactsInfos.addAll(root.getAll());
                    mDepName.setText(root.getName());
                }
                mListView.setAdapter(mDepartmentAdapter);

                break;
            case DEPARTEMT_SUB: // 需要显示下级数据
                mSwitchBtn.setImageResource(R.drawable.dep_emp_switch);
                if (mDepParent.getVisibility() != View.GONE) {
                    mDepParent.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mSideBar.getVisibility() != View.GONE) {
                    mSideBar.setVisibility(View.GONE);
                }
                if (mSearchParent.getVisibility() != View.GONE) {
                    mSearchParent.setVisibility(View.GONE);
                }
                if (mDepNameParent.getVisibility() != View.VISIBLE) {
                    mDepNameParent.setVisibility(View.VISIBLE);
                }
                if (mSwitchParent.getVisibility() != View.VISIBLE) {
                    mSwitchParent.setVisibility(View.VISIBLE);
                }
                if (mDepartmentAdapter == null) {
                    mDepartmentAdapter = new DepartmentAdapter(mContext, mContactsInfos);
                }
                ContactsInfo sub = null;
                if (mDepStack.isEmpty()) {
                    sub = ContactsCacheData.getInstance().getDepartmentById(mAppId);
                    if (sub == null) {
                        throw new NullPointerException(TAG + "#updateViews ContactsInfo is null!!!!s");
                    } else {
                        mDepStack.push(sub);
                        mDepName.setText(ContactsCacheData.getInstance().getRoot().getName() + ">" + sub.getName());
                    }
                } else {
                    sub = mDepStack.peek();
                }
                mContactsInfos.addAll(sub.getAll());
                mListView.setAdapter(mDepartmentAdapter);
                break;

            default:
                break;
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
        } else {
        // 更新界面,获取数据
        if (mRefresh) {
        mRefresh = !mRefresh;
        updateViews(mDataType);
        }

        }
         */
        // 更新界面,获取数据
        if (mRefresh) {
            mRefresh = !mRefresh;
            updateViews(mDataType);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        /**
         * 权限模块的控制,统一放在@MenuBaseActivity#onActivityResult里面处理
         * @author jinjr
         * @date 16-3-21 下午3:41

        // 失败跳转界面
        // 成功onresum处理
        if (requestCode == HcUtil.REQUEST_CODE_LOGIN) {
        if (resultCode == HcUtil.LOGIN_SUCCESS)
        mRefresh = true;// 在onresume里处理
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
        switch (requestCode) {
            case REQUEST_CODE_DEP:
                if (resultCode == Activity.RESULT_OK) {
                    // 关闭当前的Activity
                    mContext.finish();
                }
                break;
            case REQUEST_CODE_DETAIL:
                if (resultCode == Activity.RESULT_OK) {
                    if (mOriginal == DataType.DEPARTEMT_SUB) {
                        mContext.setResult(Activity.RESULT_OK);
                    }
                    mContext.finish();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestory() {
        // TODO Auto-generated method stub
        ContactsCacheData.getInstance().removeObserver(this);
        mContactsInfos.clear();
        mDepStack.clear();
        if (mSearchAdapter != null) {
            mSearchAdapter.releaseAdatper();
            mSearchAdapter = null;
        }
        if (mEmployeeAdapter != null) {
            mEmployeeAdapter.releaseAdatper();
            mEmployeeAdapter = null;
        }
        if (mDepartmentAdapter != null) {
            mDepartmentAdapter.releaseAdatper();
            mEmployeeAdapter = null;
        }
        mContext = null;
    }

}
