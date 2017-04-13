package com.android.hcframe.im;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.http.HttpRequestQueue;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.Affiliate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/9/30.
 */
public class IMDiscussionGroupSettings extends HcBaseActivity implements View.OnClickListener {

    private static final String TAG = "IMDiscussionGroupSettings";
    private TopBarView mTopBarView;
    private GridView mDiscussionGroupGv;
    private LinearLayout mImChattingDiscussionGroup;
    private LinearLayout mImDiscussionGroupName;
    private TextView mImChattingDiscussionGroupNum;
    private TextView mImDiscussionGroupBackText;
    private TextView mImChattingDiscussionGroupName;
    private ImageView mImDiscussionGroupSwitchBtn;
    private ArrayList<IMDissionGroupValue> mIMDissionGroupValueList = new ArrayList<IMDissionGroupValue>();
    private IMDiscussionGroupAdapter discussionGroupAdapter;
    /**
     * 请求参数
     */

    private AppMessageInfo chatGroupMessageInfo;
    private ChatGroupMessageInfo mChatGroupMessageInfo;

    private IMDissionGroupValue mAdd;
    private IMDissionGroupValue mDelete;
    private int mCount;
    private String mTitleName;
    private int mItemHeight;
    private Handler mHandler = new Handler();

    /**
     * 是否修改过群名字
     */
    private boolean mModify = false;
    private final int mTurn = 0;
    private final int mRename = 1;
    private boolean mOwner = false;
    private String roomJid = "";


    private static final int REQUEST_CODE_DELETE = 100;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_discussion_group_settings_layout);
        mTopBarView = (TopBarView) findViewById(R.id.details_top_bar);
        mImChattingDiscussionGroup = (LinearLayout) findViewById(R.id.im_chatting_discussion_group);
        mImDiscussionGroupSwitchBtn = (ImageView) findViewById(R.id.im_discussion_group_switch_btn);
        mImDiscussionGroupBackText = (TextView) findViewById(R.id.im_discussion_group_back_text);
        mImChattingDiscussionGroupNum = (TextView) findViewById(R.id.im_chatting_discussion_group_num);
        mImChattingDiscussionGroupName = (TextView) findViewById(R.id.im_chatting_discussion_group_name);
        mImDiscussionGroupName = (LinearLayout) findViewById(R.id.im_discussion_group_name);
        mTopBarView.setTitle("讨论组设置");
        mDiscussionGroupGv = (GridView) findViewById(R.id.im_chatting_discussion_group_settings);
        initData();
        mImDiscussionGroupName.setOnClickListener(this);
        mImChattingDiscussionGroup.setOnClickListener(this);
        mImDiscussionGroupSwitchBtn.setOnClickListener(this);
        mImDiscussionGroupBackText.setOnClickListener(this);
        mDiscussionGroupGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IMDissionGroupValue value = (IMDissionGroupValue) parent.getItemAtPosition(position);

                if (position == 0) {
                    Intent intent = new Intent(IMDiscussionGroupSettings.this, ChoosePersonnelActivity.class);
                    intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                    startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
                } else {
                    if (mOwner) {
                        if (position == 1) {
                            // 删除
                            int size = mIMDissionGroupValueList.size();
                            if (size > 3) {
                                ArrayList<ItemInfo> infos = new ArrayList<ItemInfo>();
                                ItemInfo info;
                                IMDissionGroupValue groupValue;

                                for (int i = 3; i < size; i++) {
                                    groupValue = mIMDissionGroupValueList.get(i);
                                    info = new StaffInfo();
                                    info.setMultipled(true);
                                    info.setItemValue(groupValue.getmIMDissionGroupName());
                                    info.setIconUrl(groupValue.getmIMDissionGroupUrl());
                                    info.setUserId(groupValue.getmIMDissionGroupUserId());
                                    info.setItemId("" + i);
                                    infos.add(info);
                                }

                                Intent intent = new Intent(IMDiscussionGroupSettings.this, IMDeletePersonnelActivity.class);
                                intent.putExtra("items", infos);
                                startActivityForResult(intent, REQUEST_CODE_DELETE);
                            }


                        } else if (position == 2) { // 自己

                        } else { // 查看详情
//                            if (!HcUtil.isNetWorkError(IMDiscussionGroupSettings.this)) {
//                                HcDialog.showProgressDialog(IMDiscussionGroupSettings.this, "正在删除成员...");
//                                IMManager.getInstance().execute(new KickTask(mChatGroupMessageInfo.getId(), value.getNickname(), "不好意思,把您踢了!"));
//                            }

                        }
                    }
                }


            }
        });
        mTopBarView.setReturnViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HcAppState.getInstance().removeActivity(IMDiscussionGroupSettings.this);
                if (mModify) {
                    Intent intent = new Intent();
                    intent.putExtra("name", mTitleName);
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        });
    }

    private void initData() {
        //获得ChatActivity传过来的数据
        chatGroupMessageInfo = getIntent().getExtras().getParcelable("group");
        roomJid = chatGroupMessageInfo.getId();
        mChatGroupMessageInfo = ChatOperatorDatabase.getChatGroupInfo(this, roomJid);
        mTitleName = mChatGroupMessageInfo.getTitle();
        mImChattingDiscussionGroupName.setText(mTitleName);
        mCount = mChatGroupMessageInfo.getCount();
        HcLog.D(TAG + "#initData member count ="+mCount + " noticed = "+mChatGroupMessageInfo.isNoticed());
        mImChattingDiscussionGroupNum.setText("查看全部群成员" + "(" + mCount + ")");
        //从数据库中获取消息是否免打扰的状态
        if (mChatGroupMessageInfo.isNoticed()) {
            mImDiscussionGroupSwitchBtn.setImageResource(R.drawable.im_group_switch_on);
        } else {
            mImDiscussionGroupSwitchBtn.setImageResource(R.drawable.im_group_switch_off);
        }
        // 绑定Adapter
        IMDissionGroupValue imDissionGroupValue = new IMDissionGroupValue();
        imDissionGroupValue.setmIMDissionGroupName("");
        imDissionGroupValue.setmIMDissionGroupUrl("0");
        mIMDissionGroupValueList.add(imDissionGroupValue);
        mAdd = imDissionGroupValue;

        imDissionGroupValue = new IMDissionGroupValue();
        imDissionGroupValue.setmIMDissionGroupName("");
        imDissionGroupValue.setmIMDissionGroupUrl("1");
        mDelete = imDissionGroupValue;

        HcLog.D("initData#mChatGroupMessageInfo.getGroupMembers() = " + mChatGroupMessageInfo.getGroupMembers());
        if (!TextUtils.isEmpty(mChatGroupMessageInfo.getGroupMembers())) {
            String[] mGroupMembers = mChatGroupMessageInfo.getGroupMembers().split(";");

            if (SettingHelper.getUserId(this).equals(mGroupMembers[0].split(":")[0])) {
                mOwner = true;
                mIMDissionGroupValueList.add(mDelete);
                mImDiscussionGroupName.setEnabled(true);
            } else {
                mImDiscussionGroupName.setEnabled(false);
            }
            String[] nicknames = null;
            HcLog.D("initData#mGroupMembers.length = " + mGroupMembers.length);
            for (int i = 0; i < mGroupMembers.length; i++) {
                nicknames = mGroupMembers[i].split(":");
                imDissionGroupValue = new IMDissionGroupValue();
                imDissionGroupValue.setmIMDissionGroupName(ChatOperatorDatabase.getNameByUserId(this, nicknames[0]));
                imDissionGroupValue.setmIMDissionGroupUrl(nicknames[0]);
                HcLog.D("mGroupMembers[" + i + "] = " + mGroupMembers[i]);
                if (nicknames.length > 1) {
                    imDissionGroupValue.setNickname(nicknames[1]);
                } else {
                    imDissionGroupValue.setNickname(ChatOperatorDatabase.getNameByUserId(this, nicknames[0]));
                }
                mIMDissionGroupValueList.add(imDissionGroupValue);
            }
        } else {
            HcDialog.showProgressDialog(this, "正在获取成员列表...");
        }
        HcLog.D("initData#mIMDissionGroupValueList.size() = " +mIMDissionGroupValueList.size());
        // 实例化自定义的IMDiscussionGroupAdapter
        discussionGroupAdapter = new IMDiscussionGroupAdapter(IMDiscussionGroupSettings.this, mIMDissionGroupValueList);
        int row = 0;
        if(mIMDissionGroupValueList.size()%5==0){
            row = mIMDissionGroupValueList.size() / 5;
        }else{
            row = mIMDissionGroupValueList.size() / 5 + 1;
        }
        mDiscussionGroupGv.setAdapter(discussionGroupAdapter);
        setListViewHeightBasedOnChildren(mDiscussionGroupGv,row);
        IMManager.getInstance().execute(new AffiliateList(chatGroupMessageInfo.getId()));
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
        if (id == R.id.im_chatting_discussion_group) {
            //进入查看全部成员界面
            Intent intent = new Intent(this, IMDiscussionGroupAllActivity.class);
            //要把群成员的类中数据传递过去
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("members", mIMDissionGroupValueList);
            bundle.putString("roomJid", roomJid);
            bundle.putInt("count", mCount);
            intent.putExtras(bundle);
            HcLog.D("count=" + mChatGroupMessageInfo.getCount());
            startActivityForResult(intent, mTurn);
        } else if (id == R.id.im_discussion_group_switch_btn) {
            HcLog.D("chatGroupMessageInfo.isNoticedm1()=" + mChatGroupMessageInfo.isNoticed());
            //点击switch按钮，图片改变
            if (!mChatGroupMessageInfo.isNoticed()) {
                mImDiscussionGroupSwitchBtn.setImageResource(R.drawable.im_group_switch_on);
                mChatGroupMessageInfo.setNoticed(true);
                ChatOperatorDatabase.updateChatGroup(this, mChatGroupMessageInfo);
                HcLog.D("chatGroupMessageInfo.isNoticed()2=" + mChatGroupMessageInfo.isNoticed() + "groupId=" + chatGroupMessageInfo.getId());
            } else {
                mImDiscussionGroupSwitchBtn.setImageResource(R.drawable.im_group_switch_off);
                mChatGroupMessageInfo.setNoticed(false);
                ChatOperatorDatabase.updateChatGroup(this, mChatGroupMessageInfo);
                HcLog.D("chatGroupMessageInfo.isNoticed()3=" + mChatGroupMessageInfo.isNoticed() + "groupId=" + chatGroupMessageInfo.getId());
            }
        } else if (id == R.id.im_discussion_group_name) {
            HcLog.D("intent to ImDiscussionGroupRenameActivity");
            //重命名
            Intent intent = new Intent(this, ImDiscussionGroupRenameActivity.class);
            //要把群成员的类中数据传递过去
            intent.putExtra("name", mTitleName);
            intent.putExtra("mChatGroupMessageInfo", mChatGroupMessageInfo);
            startActivityForResult(intent, mRename);

        } else if (id == R.id.im_discussion_group_back_text) {
            //退出讨论组
            if (mOwner) {
                if (!HcUtil.isNetWorkError(this)) {
                    HcDialog.showProgressDialog(this, "正在解散群组...");
                    IMManager.getInstance().execute(new DestoryGroup(mChatGroupMessageInfo.getId(), "群组解散"));
                }

            }

        }
    }

    public boolean isContainUseId(String userId) {
        boolean flag = false;
        for (IMDissionGroupValue value : mIMDissionGroupValueList) {
            if (userId.equals(value.getmIMDissionGroupUserId())) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private class AffiliateList implements Runnable {

        private String mJid;

        public AffiliateList(String jid) {
            mJid = jid;
        }

        @Override
        public void run() {
            HcLog.D(TAG + "$AffiliateList start!!!!!!!!!!!!!!!!!!1");
            final List<Affiliate> mAffiliate = IMManager.getInstance().getAffiliates(mJid);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcDialog.deleteProgressDialog();
                    if (mAffiliate != null) {
                        if (mAffiliate.size() > 0) {
                            mCount = mAffiliate.size();
                            mImChattingDiscussionGroupNum.setText("查看全部群成员" + "(" + mCount + ")");
                            mChatGroupMessageInfo.setCount(mAffiliate.size());
                            StringBuilder builder = new StringBuilder();
                            String jid = null;
                            boolean owner = false;
                            IMDissionGroupValue value;
                            List<IMDissionGroupValue> values = new ArrayList<IMDissionGroupValue>();
//                            mIMDissionGroupValueList.clear();
                            String nickname;
                            for (Affiliate affiliate : mAffiliate) {
                                jid = affiliate.getJid().split("@")[0];
                                nickname = affiliate.getNick();
                                value = new IMDissionGroupValue();
                                value.setmIMDissionGroupName(ChatOperatorDatabase.getNameByUserId(IMDiscussionGroupSettings.this, jid));
                                value.setmIMDissionGroupUrl(jid);
                                value.setmIMDissionGroupUserId(jid);
                                if (nickname == null) {
                                    nickname = ChatOperatorDatabase.getNameByUserId(IMDiscussionGroupSettings.this, jid);
                                    if (nickname == null) {
                                        nickname = "";
                                    }
                                }
                                value.setNickname(nickname);
//                                mIMDissionGroupValueList.add(value);
                                values.add(value);
                                if (affiliate.getAffiliation().equals("owner")) {
                                    builder.insert(0, jid + ":" + nickname + ";");
                                    if (jid.equals(SettingHelper.getUserId(IMDiscussionGroupSettings.this))) {
                                        owner = true;
                                    }
                                } else {
                                    builder.append(jid + ":" + nickname + ";");
                                }
                            }
                            builder.deleteCharAt(builder.length() - 1);
                            mChatGroupMessageInfo.setGroupMembers(builder.toString());
                            ChatOperatorDatabase.updateChatGroup(IMDiscussionGroupSettings.this, mChatGroupMessageInfo);
                            HcLog.D(TAG + "$AffiliateList mChatGroupMessageInfo.GroupMembers = " + mChatGroupMessageInfo.getGroupMembers());
//                            mIMDissionGroupValueList.add(0, mAdd);
                            values.add(0, mAdd);
                            mOwner = owner;
                            if (owner) {
                                mImDiscussionGroupName.setEnabled(true);
                                values.add(1, mDelete);
//                                mIMDissionGroupValueList.add(1, mDelete);
                            } else {
                                mImDiscussionGroupName.setEnabled(false);
                            }
                            HcLog.D(TAG + " $mAffiliate size = " + mAffiliate.size());
                            mIMDissionGroupValueList.clear();
                            mIMDissionGroupValueList.addAll(values);
                            discussionGroupAdapter.notifyDataSetChanged();
                            values.clear();
                        } else {
                            if (TextUtils.isEmpty(mChatGroupMessageInfo.getGroupMembers())) {
                                HcUtil.showToast(IMDiscussionGroupSettings.this, "该群没有成员！");
                            }
                        }
                    } else {
                        if (TextUtils.isEmpty(mChatGroupMessageInfo.getGroupMembers()))
                            HcUtil.showToast(IMDiscussionGroupSettings.this, "网络不给力！");
                    }
                }
            });
        }
    }

//    private class ModifyGroupName implements Runnable {
//
//        private String mJid;
//        private String mName;
//
//        public ModifyGroupName(String room, String name) {
//            mJid = room;
//            mName = name;
//        }
//
//        @Override
//        public void run() {
//            Form form = IMManager.getInstance().getConfigurationForm(mJid);
//            if (form != null) {
//                final boolean success = IMManager.getInstance().setRoomNameConfiguration(mJid, form, mName);
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        HcDialog.deleteProgressDialog();
//                        if (success) {
//                            mModify = true;
//                            mImChattingDiscussionGroupName.setText(mName);
//                            mChatGroupMessageInfo.setTitle(mName);
//                            ChatOperatorDatabase.updateChatGroup(IMDiscussionGroupSettings.this, mChatGroupMessageInfo);
//                            ChatOperatorDatabase.updateGroupChatName(IMDiscussionGroupSettings.this, mChatGroupMessageInfo.getId(), mName);
//                        } else {
//                            HcUtil.showToast(IMDiscussionGroupSettings.this, "更改群名称失败!");
//                        }
//                    }
//                });
//
//            } else {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        HcDialog.deleteProgressDialog();
//                        HcUtil.showToast(IMDiscussionGroupSettings.this, "更改群名称失败!");
//                    }
//                });
//
//            }
//        }
//    }

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
                                HcLog.D(TAG + "#onActivityResult infos size = " + size);
                                HcLog.D("infos = " + infos);
                                for (int i = 0; i < size; i++) {
                                    String mUserId = infos.get(i).getUserId();
                                    if (!isContainUseId(mUserId)) {
                                        IMDissionGroupValue mValue = new IMDissionGroupValue();
                                        mValue.setmIMDissionGroupName(infos.get(i).getItemValue());
                                        mValue.setmIMDissionGroupUrl(mUserId);
                                        mValue.setmIMDissionGroupUserId(mUserId);
                                        mIMDissionGroupValueList.add(mValue);
                                    }
                                }
                                HcDialog.showProgressDialog(this, "正在提交数据...");
                                //发送请求，将所有的值传递给服务端
                                IMManager.getInstance().execute(new PostGroupInfo(mIMDissionGroupValueList));
                            }
                        }
                        break;
                    case mTurn:
                        //接收IMDiscussionGroupAllActivity传递回的数据
                        mIMDissionGroupValueList = data.getParcelableArrayListExtra("value");
                        mIMDissionGroupValueList.add(0, mAdd);
                        if (mOwner) {
                            mIMDissionGroupValueList.add(1, mDelete);
                            mImChattingDiscussionGroupNum.setText("查看全部群成员" + "(" + (mIMDissionGroupValueList.size()-2) + ")");
                        }else{
                            mImChattingDiscussionGroupNum.setText("查看全部群成员" + "(" + (mIMDissionGroupValueList.size()-1) + ")");
                        }
                        HcLog.D("ReTurnList=" + mIMDissionGroupValueList.size());
                        discussionGroupAdapter = new IMDiscussionGroupAdapter(IMDiscussionGroupSettings.this, mIMDissionGroupValueList);
                        int row;
                        if(mIMDissionGroupValueList.size()%5==0){
                            row = mIMDissionGroupValueList.size() / 5;
                        }else{
                            row = mIMDissionGroupValueList.size() / 5 + 1;
                        }
                        mDiscussionGroupGv.setAdapter(discussionGroupAdapter);
                        setListViewHeightBasedOnChildren(mDiscussionGroupGv, row);
                        break;
                    case REQUEST_CODE_DELETE:
                        if (data != null && data.getExtras() != null) {
                            List<ItemInfo> infos = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            List<String> nicknames = new ArrayList<String>();
                            List<String> jids = new ArrayList<String>();
                            if (infos != null) {
                                // 删除
                                for (ItemInfo info : infos) {
                                    nicknames.add(info.getItemValue());
                                    jids.add(info.getUserId() + "@" + IMUtil.getServerName());
                                }


                            }
//                            IMManager.getInstance().execute(new KickTask(mChatGroupMessageInfo.getId(), nicknames, "踢了你们!"));
                            IMManager.getInstance().execute(new RevokeTask(mChatGroupMessageInfo.getId(), jids));
                        }

                        break;
                    case mRename:
                        mChatGroupMessageInfo = data.getExtras().getParcelable("nameobject");
                        mTitleName = mChatGroupMessageInfo.getTitle();
                        mImChattingDiscussionGroupName.setText(mTitleName);
                        mModify = true;
                        break;
                    default:
                        break;
                }
                break;

            default:
                break;
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
                        IMManager.getInstance().invite(roomJid, mValue.get(i).getmIMDissionGroupUserId(), null);
                    }
//                    if (discussionGroupAdapter == null) {
                    if (mDelete != null && mDelete.getmIMDissionGroupUrl().equals("1")) {
                        mCount = mValue.size() - 2;
                        mImChattingDiscussionGroupNum.setText("查看全部成员(" + mCount + ")");
                    } else {
                        mCount = mValue.size() - 1;
                        mImChattingDiscussionGroupNum.setText("查看全部成员(" + mCount + ")");
                    }
                    discussionGroupAdapter = new IMDiscussionGroupAdapter(IMDiscussionGroupSettings.this, mValue);
                    mDiscussionGroupGv.setAdapter(discussionGroupAdapter);
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mModify) {
                        Intent intent = new Intent();
                        intent.putExtra("name", mChatGroupMessageInfo.getTitle());
                        setResult(Activity.RESULT_OK, intent);
                    }
                }
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class DestoryGroup implements Runnable {

        private String mRoom;

        private String mReason;

        public DestoryGroup(String room, String reason) {
            mRoom = room;
            mReason = reason;
            if (mReason == null)
                mReason = "";
        }

        @Override
        public void run() {
            boolean success = IMManager.getInstance().destoryRoom(IMDiscussionGroupSettings.this, mRoom, mReason);
            HcDialog.deleteProgressDialog();
            if (success) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 删除群的信息
                        ChatOperatorDatabase.deleteChatGroup(IMDiscussionGroupSettings.this, mRoom);
                        ChatOperatorDatabase.deleteAppMessage(IMDiscussionGroupSettings.this, mRoom);
                        ChatOperatorDatabase.deleteChatMessages(IMDiscussionGroupSettings.this, mRoom);
                        // 告知聊天界面关闭
                        Intent intent = new Intent();
                        intent.putExtra("destory", true);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcUtil.showToast(IMDiscussionGroupSettings.this, "退出群失败！");
                    }
                });
            }
        }
    }
    public void setListViewHeightBasedOnChildren(GridView listView, int row) {
        // 获取listview的adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        HcLog.D("row = " + row);
        for (int i = 0; i < row; i++) {
           // 获取listview的每一个item
           View listItem = listAdapter.getView(i, null, listView);
           listItem.measure(0, 0);
           mItemHeight = listItem.getMeasuredHeight();
          // 获取item的高度和
          totalHeight += listItem.getMeasuredHeight()+20;
        }
        // 获取listview的布局参数
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // 设置高度
        params.height = totalHeight;
        HcLog.D("totalHeight=" + totalHeight + "," + "params.height=" + params.height);
        // 设置margin
        ((ViewGroup.MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        // 设置参数
        listView.setLayoutParams(params);
    }

    private class KickTask implements Runnable {

        private String mRoom;
        private List<String> mNicknames;
        private String mReason;

        public KickTask(String room, List<String> nicknames, String reason) {
            mRoom = room;
            mNicknames = nicknames;
            mReason = reason;
        }

        @Override
        public void run() {
            if (mNicknames.isEmpty()) {
                HcDialog.deleteProgressDialog();
                return;
            }
            HcLog.D(TAG + " $KickTask before kick time = " + HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
            boolean success;
            if (mNicknames.size() > 1) {
                success = IMManager.getInstance().kickParticipant(mRoom, mNicknames, mReason);
            } else {
                success = IMManager.getInstance().kickParticipant(mRoom, mNicknames.get(0), mReason);
            }

            HcLog.D(TAG + " $KickTask after kick time = " + HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()) + " success = " + success);
            HcDialog.deleteProgressDialog();
            if (success) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 删除成员
                        StringBuilder builder = new StringBuilder();
                        int size = mNicknames.size();
                        Iterator<IMDissionGroupValue> iterator;
                        IMDissionGroupValue value;
                        String nickname;
                        for (int i = 0; i < size; i++) {
                            iterator = mIMDissionGroupValueList.iterator();
                            nickname = mNicknames.get(i);
                            while (iterator.hasNext()) {
                                value = iterator.next();
                                if (nickname.equals(value.getNickname())) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }

                        size = mIMDissionGroupValueList.size();
                        for (int i = 2; i < size; i++) { // 跳过+-按钮
                            value = mIMDissionGroupValueList.get(i);
                            builder.append(value.getmIMDissionGroupUserId() + ":" + value.getNickname() + ";");
                        }


                        builder.deleteCharAt(builder.length() - 1);
                        mChatGroupMessageInfo.setGroupMembers(builder.toString());
                        discussionGroupAdapter.notifyDataSetChanged();
                        ChatOperatorDatabase.updateChatGroup(IMDiscussionGroupSettings.this, mChatGroupMessageInfo);

                    }
                });


            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcUtil.showToast(IMDiscussionGroupSettings.this, "删除" + mNicknames.get(0) + "失败!");
                    }
                });
            }
        }
    }

    private class RevokeTask implements Runnable {

        private String mRoom;
        private List<String> mJids;

        public RevokeTask(String room, List<String> jids) {
            mRoom = room;
            mJids = jids;
        }

        @Override
        public void run() {
            if (mJids.isEmpty()) {
                HcDialog.deleteProgressDialog();
                return;
            }
            HcLog.D(TAG + " $RevokeTask before kick time = " + HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
            boolean success = IMManager.getInstance().revokeMembership(mRoom, mJids);

            HcLog.D(TAG + " $RevokeTask after kick time = " + HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()) + " success = " + success);
            HcDialog.deleteProgressDialog();
            if (success) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 删除成员
                        StringBuilder builder = new StringBuilder();
                        int size = mJids.size();
                        Iterator<IMDissionGroupValue> iterator;
                        IMDissionGroupValue value;
                        String userId;
                        for (int i = 0; i < size; i++) {
                            iterator = mIMDissionGroupValueList.iterator();
                            userId = mJids.get(i).split("@")[0];
                            while (iterator.hasNext()) {
                                value = iterator.next();
                                if (userId.equals(value.getmIMDissionGroupUserId())) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }

                        size = mIMDissionGroupValueList.size();
                        for (int i = 2; i < size; i++) { // 跳过+-按钮
                            value = mIMDissionGroupValueList.get(i);
                            builder.append(value.getmIMDissionGroupUserId() + ":" + value.getNickname() + ";");
                        }


                        builder.deleteCharAt(builder.length() - 1);
                        mChatGroupMessageInfo.setGroupMembers(builder.toString());
                        discussionGroupAdapter.notifyDataSetChanged();
                        ChatOperatorDatabase.updateChatGroup(IMDiscussionGroupSettings.this, mChatGroupMessageInfo);

                    }
                });


            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcUtil.showToast(IMDiscussionGroupSettings.this, "删除成员失败!");
                    }
                });
            }
        }
    }
}
