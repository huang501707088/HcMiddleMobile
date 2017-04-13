package com.android.hcframe.im;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.dialog.EditDialog;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;

import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/9/30.
 */
public class IMGroupMenuPage extends HcBaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "IMGroupMenuPage";

    private List<ChatGroupMessageInfo> mInfos = new ArrayList<ChatGroupMessageInfo>();
    private IMGroupMenuAdapter mIMGroupMenuAdapter;
    private PullToRefreshListView mListView;

    private TopBarView mTopBar;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_group_activity_layout);
        initView();
        initData();
    }

    private void initView() {
        mTopBar = (TopBarView) findViewById(R.id.chat_group_top_bar);

        mListView = (PullToRefreshListView) findViewById(R.id.contact_group_listview);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mListView.setOnItemClickListener(this);
        mTopBar.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IMGroupMenuPage.this, ChoosePersonnelActivity.class);
                intent.putExtra(ChoosePersonnelActivity.SELECT, true);
                startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
            }
        });

        mTopBar.setTitle(getResources().getString(R.string.im_group_title));
    }

    private void initData() {
        mInfos.addAll(ChatOperatorDatabase.getChatGroups(this));
        HcLog.D(TAG + " #initData groups size = "+mInfos.size());
        if (mIMGroupMenuAdapter == null) {
            mIMGroupMenuAdapter = new IMGroupMenuAdapter(this, mInfos);
            mListView.setAdapter(mIMGroupMenuAdapter);
        }

        if (mInfos.isEmpty()) {
            HcDialog.showProgressDialog(this, "正在获取数据...");
        }

        IMManager.getInstance().execute(new GroupList());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppMessageInfo info = (AppMessageInfo) parent.getItemAtPosition(position);
        switch (info.getType()) {
            case 2: // 群聊消息
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("emp", info);
                intent.putExtra("group", true);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HcLog.D(TAG + " #onActivityResult requestCode = "+requestCode + " resultCode = "+resultCode + " data = "+data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case HcChooseHomeView.REQUEST_CODE:
                        if (data != null && data.getExtras() != null) {
                            List<ItemInfo> infos = data.getParcelableArrayListExtra(HcChooseHomeView.CLICK_KEY);
                            if (infos != null) {
                                int size = infos.size();
                                HcLog.D(TAG + "#onActivityResult infos size = "+size);
                                String nickName = SettingHelper.getName(this);
                                StringBuilder builder = new StringBuilder(nickName + ",");
                                final List<String> jids = new ArrayList<String>();
                                for (int i = 0; i < size; i++) {
                                    HcLog.D(TAG + "#onActivityResult item value = "+infos.get(i).getItemValue() + " userId = "+infos.get(i).getUserId());
                                    builder.append(infos.get(i).getItemValue() + ",");
                                    jids.add(infos.get(i).getUserId() + "@" + IMUtil.getServerName());
                                }
                                builder.deleteCharAt(builder.length() - 1);
                                final String roomName = builder.toString();// + "@" + IMUtil.CONFERENCE + "." + IMUtil.getServerName();
                                HcLog.D(TAG + "#onActivityResult room name ="+roomName);

                                // 创建群组
//                                HcDialog.showProgressDialog(IMGroupMenuPage.this, "正在创建群组...");
//                                IMManager.getInstance().execute(new RoomTask(roomName, SettingHelper.getName(IMGroupMenuPage.this), jids));

                                EditDialog.showEditDialog(IMGroupMenuPage.this, "修改群名", roomName, false, "确认", "取消", new EditDialog.OnClickListener() {
                                    @Override
                                    public void onClick(int which, String content) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            if (TextUtils.isEmpty(content)) {
                                                content = roomName;
                                            }
                                        } else {
                                            content = roomName;
                                        }
                                        EditDialog.deleteEditDialog();
                                        // 创建群组
                                        HcDialog.showProgressDialog(IMGroupMenuPage.this, "正在创建群组...");
                                        IMManager.getInstance().execute(new RoomTask(content + "@" + IMUtil.CONFERENCE + "." + IMUtil.getServerName(), SettingHelper.getName(IMGroupMenuPage.this), jids));
                                    }
                                });


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

    private class GroupList implements Runnable {

        @Override
        public void run() {
            // 去IM服务器获取列表
            final List<HostedRoom> rooms = IMManager.getInstance().getRooms();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    HcDialog.deleteProgressDialog();
                    if (rooms == null) return; // error
                    //mInfos.clear(); // 这里以后再优化...
                    ChatGroupMessageInfo info = null;
                    if (mInfos.size() > 0) {
                        List<ChatGroupMessageInfo> infos = new ArrayList<ChatGroupMessageInfo>(mInfos);
                        Iterator<ChatGroupMessageInfo> iterator = null;
                        String jid;
                        String name;
                        boolean match;
                        for(HostedRoom room : rooms) {
                            match = false;
                            iterator = infos.iterator();
                            jid = room.getJid();
                            name = room.getName();
                            while (iterator.hasNext()) {
                                info = iterator.next();
                                if (jid.equals(info.getId())) {
                                    match = true;
                                    if (name.equals(info.getTitle())) {
                                        // do nothing

                                    } else {
                                        // 需要更新
                                        info.setTitle(name);
                                        ChatOperatorDatabase.updateChatGroup(IMGroupMenuPage.this, info);
                                        ChatOperatorDatabase.updateAppMessageTitle(IMGroupMenuPage.this, info);
                                    }

                                    iterator.remove();
                                    break;
                                }
                            }
                            HcLog.D(TAG + " $GroupList #run match = "+match + " jid = "+jid + " name = "+name);
                            if (!match) {
                                info = new ChatGroupMessageInfo();
                                info.setGroupMembers("");
                                info.setId(jid);
                                info.setTitle(name);
                                info.setCount(1);
                                mInfos.add(info);
                                ChatOperatorDatabase.insertChatGroup(IMGroupMenuPage.this, info);
                                // 创建群
                                IMManager.getInstance().addRoom(jid);
                            }

                            IMManager.getInstance().execute(new RoomInfoTask(info));

                        }
                        // 删除没有的群
                        // 删除群的聊天信息？
                        for (ChatGroupMessageInfo groupInfo : infos) {
                            ChatOperatorDatabase.deleteChatGroup(IMGroupMenuPage.this, groupInfo);
                        	mInfos.remove(groupInfo);
                        }
                        infos.clear();
                        mIMGroupMenuAdapter.notifyDataSetChanged();
                    } else {
                        for(HostedRoom room : rooms) {
                            info = new ChatGroupMessageInfo();
                            info.setGroupMembers("");
                            info.setId(room.getJid());
                            info.setTitle(room.getName());
                            info.setCount(1);
                            mInfos.add(info);
                            // 创建群
                            HcLog.D(TAG + "$GroupList #run  jid = "+room.getJid());
                            IMManager.getInstance().addRoom(room.getJid());
                            IMManager.getInstance().execute(new RoomInfoTask(info));
                        }
                        mIMGroupMenuAdapter.notifyDataSetChanged();
                        ChatOperatorDatabase.insertChatGroups(IMGroupMenuPage.this, mInfos);
                    }


                }
            });
        }
    }

    private class InviteTask implements Runnable {

        private List<String> mJids;

        private MultiUserChat mRoom;

        public InviteTask(MultiUserChat room, List<String> jids) {
            mJids = jids;
            mRoom = room;
        }

        @Override
        public void run() {
            try {
//                mRoom.create("jrjin");
                HcLog.D(TAG + " $InviteTask#run start!!!!!!!!!!!!!");
                StringBuilder builder = new StringBuilder(SettingHelper.getUserId(IMGroupMenuPage.this) + ":" + SettingHelper.getName(IMGroupMenuPage.this) + ";");
                String userId = null;
                for (String jid : mJids) {
                    IMManager.getInstance().invite(mRoom, jid, mRoom.getNickname() + "邀请您加入一起聊天！");
//                	mRoom.invite(jid, mRoom.getNickname() + "邀请您加入一起聊天！");
                    userId = jid.split("@")[0];
                    builder.append(userId + ":" + ChatOperatorDatabase.getNameByUserId(IMGroupMenuPage.this, userId) + ";");
                }
                builder.deleteCharAt(builder.length() - 1);
                // 重新获取列表还是直接创建
                final ChatGroupMessageInfo info = new ChatGroupMessageInfo();
                info.setGroupMembers(builder.toString());
                info.setId(mRoom.getRoom());
                info.setTitle(mRoom.getRoom().split("@")[0]);
                info.setCount(mJids.size() + 1); // 这里不包括创建人
                ChatOperatorDatabase.insertChatGroup(IMGroupMenuPage.this, info);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mInfos.add(0, info);
                        mIMGroupMenuAdapter.notifyDataSetChanged();

//                        EditDialog.showEditDialog(IMGroupMenuPage.this, "修改群名", mRoom.getRoom().split("@")[0], new EditDialog.OnClickListener() {
//                            @Override
//                            public void onClick(int which, String content) {
//                                if (which == DialogInterface.BUTTON_POSITIVE) {
//                                    if (TextUtils.isEmpty(content)) {
//                                        HcUtil.showToast(IMGroupMenuPage.this, "群名不能为空!");
//                                    } else {
//                                        HcDialog.showProgressDialog(IMGroupMenuPage.this, "正在重命名...");
//                                        IMManager.getInstance().execute(new Rename(mRoom.getRoom(), content));
//                                    }
//                                }
//                                EditDialog.deleteEditDialog();
//                            }
//                        });
                    }
                });
                HcLog.D(TAG + " $InviteTask#run end!!!!!!!!!!!!!");
            } catch(Exception e) {
                HcLog.D(TAG + " $InviteTask#run error = "+e);
            }
        }
    }

    private class RoomTask implements Runnable {

        private String mRoomName;
        private String mNickname;
        private List<String> mJids;

        public RoomTask(String roomName, String nickname, List<String> jids) {
            mRoomName = roomName;
            mNickname = nickname;
            mJids = jids;
        }

        @Override
        public void run() {
            MultiUserChat room = IMManager.getInstance().createRoom(mRoomName, mNickname);
            if (room != null) {
                Form form = IMManager.getInstance().getPersistentRoomForm(room);
                if (form != null) {
                    boolean success = IMManager.getInstance().setRoomConfigurationForm(room, form);
                    if (success) {
                        HcDialog.deleteProgressDialog();
                        // 邀请成员加入
                        IMManager.getInstance().execute(new InviteTask(room, mJids));
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                HcDialog.deleteProgressDialog();
                                HcUtil.showToast(IMGroupMenuPage.this, "创建失败！");
                            }
                        });
                    }
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            HcDialog.deleteProgressDialog();
                            HcUtil.showToast(IMGroupMenuPage.this, "创建失败！");
                        }
                    });
                }

            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcDialog.deleteProgressDialog();
                        HcUtil.showToast(IMGroupMenuPage.this, "创建失败！");
                    }
                });
            }
        }
    }


    private class RoomInfoTask implements Runnable {

        private ChatGroupMessageInfo mInfo;

        public RoomInfoTask(ChatGroupMessageInfo info) {
            mInfo = info;
        }

        @Override
        public void run() {
            final RoomInfo info = IMManager.getInstance().getRoomInfo(mInfo.getId(), null);
            if (info != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcLog.D(TAG + " $RoomInfoTask room jid = "+mInfo.getId() + " room name  = "+info.getRoom() +
                                " room Description = "+info.getDescription() + " room count ="+info.getOccupantsCount() +
                                " room subject ="+info.getSubject());

                        if (mInfo.getCount() != info.getOccupantsCount()) {
                            mInfo.setCount(info.getOccupantsCount());
                            ChatOperatorDatabase.updateChatGroup(IMGroupMenuPage.this, mInfo);
                            mIMGroupMenuAdapter.notifyDataSetChanged();
                        }

                    }
                });
            }
        }
    }

    private class Rename implements Runnable {

        private String mJid;
        private String mName;

        public Rename(String room, String name) {
            mJid = room;
            mName = name;
        }

        @Override
        public void run() {
            Form form = IMManager.getInstance().getConfigurationForm(mJid);
            if (form != null) {
                final boolean success = IMManager.getInstance().setRoomNameConfiguration(mJid, form, mName);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcDialog.deleteProgressDialog();
                        if (success) {
                            for (ChatGroupMessageInfo groupInfo : mInfos) { // 上面创建成功后添加在第一个
                            	if (mJid.equals(groupInfo.getId())) {
                                    groupInfo.setTitle(mName);
                                    ChatOperatorDatabase.updateChatGroup(IMGroupMenuPage.this, groupInfo);
                                    mIMGroupMenuAdapter.notifyDataSetChanged();
                                }
                            }

                            // 更改IM主页列表的消息
                            ChatOperatorDatabase.updateGroupChatName(IMGroupMenuPage.this, mJid, mName);

                        } else {
                            HcUtil.showToast(IMGroupMenuPage.this, "更改群名称失败!");
                        }
                    }
                });

            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcDialog.deleteProgressDialog();
                        HcUtil.showToast(IMGroupMenuPage.this, "更改群名称失败!");
                    }
                });

            }
        }
    }
}
