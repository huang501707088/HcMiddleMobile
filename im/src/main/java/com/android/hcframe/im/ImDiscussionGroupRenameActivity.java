package com.android.hcframe.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;

import org.jivesoftware.smackx.Form;

/**
 * Created by pc on 2016/10/28.
 */
public class ImDiscussionGroupRenameActivity extends HcBaseActivity implements View.OnClickListener {
    private static final String TAG = "ImDiscussionGroupRenameActivity";
    private TopBarView mTopBarView;
    private EditText mImDiscussionEdit;
    private TextView mImDiscussionGroupRenameText;
    private ChatGroupMessageInfo mChatGroupMessageInfo;
    private String mName;
    /**
     * 是否修改过群名字
     */
    private boolean mModify = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_discussion_group_rename_layout);
        mTopBarView = (TopBarView) findViewById(R.id.im_discussion_rename_top_bar);
        mImDiscussionEdit = (EditText) findViewById(R.id.im_discussion_edit);
        mImDiscussionGroupRenameText = (TextView) findViewById(R.id.im_discussion_group_rename_text);
        initData();
        mTopBarView.setTitle("修改群组名称");
        mTopBarView.setReturnBtnIcon(R.drawable.center_close);
        mImDiscussionGroupRenameText.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mChatGroupMessageInfo = intent.getParcelableExtra("mChatGroupMessageInfo");
            mName = intent.getExtras().getString("name");
            mImDiscussionEdit.setText(mName);
            mImDiscussionEdit.setSelection(mImDiscussionEdit.getText().length());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.im_discussion_group_rename_text) {
            String name = mImDiscussionEdit.getText().toString().trim();
            HcDialog.showProgressDialog(this, "正在重命名...");
            IMManager.getInstance().execute(new ModifyGroupName(mChatGroupMessageInfo.getId(), name));
        }
    }

    private class ModifyGroupName implements Runnable {

        private String mJid;
        private String mName;

        public ModifyGroupName(String room, String name) {
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
                            mModify = true;
                            mImDiscussionEdit.setText(mName);
                            mChatGroupMessageInfo.setTitle(mName);
                            ChatOperatorDatabase.updateChatGroup(ImDiscussionGroupRenameActivity.this, mChatGroupMessageInfo);
                            ChatOperatorDatabase.updateGroupChatName(ImDiscussionGroupRenameActivity.this, mChatGroupMessageInfo.getId(), mName);
                            Intent intent = new Intent();

                            intent.putExtra("nameobject", mChatGroupMessageInfo);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            HcUtil.showToast(ImDiscussionGroupRenameActivity.this, "更改群名称失败!");
                        }
                    }
                });

            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        HcDialog.deleteProgressDialog();
                        HcUtil.showToast(ImDiscussionGroupRenameActivity.this, "更改群名称失败!");
                    }
                });

            }
        }
    }
}
