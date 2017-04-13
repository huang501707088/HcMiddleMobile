package com.android.hcframe.im;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2016/9/30.
 */
public class IMDiscussionGroupAllActivity extends HcBaseActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {

    private static final String TAG = "IMDiscussionGroupAllActivity";
    private TopBarView mTopBarView;
    private PullToRefreshListView mImDiscussionGroupAll;
    private LinearLayout mImGroupSearchShow;
    private ImageView mImGroupSearchImageClear;
    private LinearLayout mImGroupSearch;
    private LinearLayout mSearch;
    private EditText mImGroupSearchContent;
    private int mNum;
    private String mRoomJid;
    private List<IMDissionGroupValue> mIMDissionGroupAllValue = new ArrayList<IMDissionGroupValue>();
    private List<IMDissionGroupValue> mIMDissionGroupAll = new ArrayList<IMDissionGroupValue>();
    private IMDiscussionGroupAllAdapter discussionGroupAllAdapter;
    private Handler mHandler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_discussion_group_all_layout);
        mTopBarView = (TopBarView) findViewById(R.id.im_discussion_group_top_bar);
        mImGroupSearchImageClear = (ImageView) findViewById(R.id.im_group_search_image_clear);
        mImGroupSearchShow = (LinearLayout) findViewById(R.id.im_group_search_show);
        mImGroupSearch = (LinearLayout) findViewById(R.id.im_group_search);
        mImGroupSearchContent = (EditText) findViewById(R.id.im_group_search_content);
        mImDiscussionGroupAll = (PullToRefreshListView) findViewById(R.id.im_discussion_group_all_lv);
        initData();
        mImGroupSearchContent.addTextChangedListener(this);
        mImGroupSearchContent.setOnFocusChangeListener(this);
        mImGroupSearchShow.setOnClickListener(this);
        mImGroupSearchImageClear.setOnClickListener(this);
        mImDiscussionGroupAll.setMode(PullToRefreshBase.Mode.DISABLED);
        mTopBarView.setTitle("聊天成员(" + mNum + ")");
        mTopBarView.setReturnBtnVisiable(View.VISIBLE);
        mTopBarView.setMenuSrc(R.drawable.im_chat_group_info_icon);
        mTopBarView.setReturnViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                HcDialog.showProgressDialog(IMDiscussionGroupAllActivity.this, "正在提交数据...");
                IMManager.getInstance().execute(new PostGroupInfo(mIMDissionGroupAllValue));
                intent.putParcelableArrayListExtra("value", (ArrayList<? extends Parcelable>) mIMDissionGroupAllValue);
                setResult(Activity.RESULT_OK, intent); //intent为A传来的带有Bundle的intent，当然也可以自己定义新的Bundle
                finish();
            }
        });
        mTopBarView.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IMDiscussionGroupAllActivity.this, ChoosePersonnelActivity.class);
                intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mIMDissionGroupAllValue = intent.getParcelableArrayListExtra("members");
            mNum = intent.getExtras().getInt("count");
            mRoomJid = intent.getExtras().getString("roomJid");
            HcLog.D("mIMDissionGroupAllValue = " + mIMDissionGroupAllValue + "," + " mNum = " + mNum + "," + " mRoomJid=" + mRoomJid);
        }
        if (mIMDissionGroupAllValue.get(0).getmIMDissionGroupUrl().equals("0")) {
            mIMDissionGroupAllValue.remove(0);
        }
        if (mIMDissionGroupAllValue.get(0).getmIMDissionGroupUrl().equals("1")) {
            mIMDissionGroupAllValue.remove(0);
        }
        mIMDissionGroupAll.addAll(mIMDissionGroupAllValue);
        discussionGroupAllAdapter = new IMDiscussionGroupAllAdapter(this, mIMDissionGroupAllValue);
        mImDiscussionGroupAll.setAdapter(discussionGroupAllAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        HttpRequestQueue.getInstance().cancelRequest("");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.im_group_search_show) {
            mImGroupSearchShow.setVisibility(View.GONE);
            mImGroupSearch.setVisibility(View.VISIBLE);
            mSearch.setVisibility(View.VISIBLE);
            mImGroupSearchContent.isFocused();
        } else if (id == R.id.im_group_search_image_clear) {
            //点击搜索栏中的x形按钮
            discussionGroupAllAdapter = new IMDiscussionGroupAllAdapter(this, mIMDissionGroupAllValue);
            mImDiscussionGroupAll.setAdapter(discussionGroupAllAdapter);
            mImGroupSearchShow.setVisibility(View.VISIBLE);
            mImGroupSearch.setVisibility(View.GONE);
            mSearch.setVisibility(View.GONE);
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s != null) {
            String key = s.toString().trim();
            if (!TextUtils.isEmpty(key)) {
                key = key.toUpperCase();
                if (!mIMDissionGroupAll.isEmpty()) {
                    List<IMDissionGroupValue> subGroupValue = new ArrayList<IMDissionGroupValue>();
                    for (IMDissionGroupValue groupvalue : mIMDissionGroupAll) {
                        if (groupvalue.getmIMDissionGroupName().contains(key)) {
                            subGroupValue.add(groupvalue);
                        }
                    }
                    mIMDissionGroupAll.clear();
                    mIMDissionGroupAll.addAll(subGroupValue);
                    subGroupValue.clear();
                    discussionGroupAllAdapter = new IMDiscussionGroupAllAdapter(this, mIMDissionGroupAll);
                    mImDiscussionGroupAll.setAdapter(discussionGroupAllAdapter);
//                    discussionGroupAllAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class PostGroupInfo implements Runnable {
        private List<IMDissionGroupValue> mValue;

        public PostGroupInfo(List<IMDissionGroupValue> listValue) {
            mValue = listValue;
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcDialog.deleteProgressDialog();
                    for (int i = 0; i < mValue.size(); i++) {
                        IMManager.getInstance().invite(mRoomJid, mValue.get(i).getmIMDissionGroupUserId(), null);
                    }
                    discussionGroupAllAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HcLog.D(TAG + " #onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode + " data = " + data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case HcChooseHomeView.REQUEST_CODE:
                        if (data != null && data.getExtras() != null) {
                            List<ItemInfo> infos = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            if (infos != null) {
                                int size = infos.size();
                                HcLog.D("before size = " + mIMDissionGroupAllValue.size());
                                for (int i = 0; i < size; i++) {
                                    String mUserId = infos.get(i).getUserId();
                                    //过滤人员
                                    if (!isContainUseId(mUserId)) {
                                        IMDissionGroupValue mValue = new IMDissionGroupValue();
                                        HcLog.D("name[" + i + "]=" + ChatOperatorDatabase.getNameByUserId(this, infos.get(i).getUserId()));
                                        mValue.setmIMDissionGroupName(infos.get(i).getItemValue());
                                        mValue.setmIMDissionGroupUrl(mUserId);
                                        mValue.setmIMDissionGroupUserId(mUserId);
                                        mIMDissionGroupAllValue.add(mValue);
                                    }
                                }
                                mNum = mIMDissionGroupAllValue.size();
                                mTopBarView.setTitle("聊天成员(" + mNum + ")");
                                HcLog.D("after size = " + mIMDissionGroupAllValue.size());
                                //更新list中的数据
                                discussionGroupAllAdapter.notifyDataSetChanged();
                            }
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

    public boolean isContainUseId(String userId) {
        boolean flag = false;
        for (IMDissionGroupValue value : mIMDissionGroupAllValue) {
            if (userId.equals(value.getmIMDissionGroupUserId())) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
