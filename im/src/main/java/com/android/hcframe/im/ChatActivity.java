package com.android.hcframe.im;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.DotView;
import com.android.hcframe.DraggableGridViewPager;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.ModuleBridge;
import com.android.hcframe.TopBarView;
import com.android.hcframe.badge.BadgeCache;
import com.android.hcframe.badge.BadgeInfo;
import com.android.hcframe.contacts.data.ContactsInfo;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatImageOtherInfo;
import com.android.hcframe.im.data.ChatImageOwnInfo;
import com.android.hcframe.im.data.ChatMessageAdapter;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorAdapter;
import com.android.hcframe.im.data.ChatOperatorAdapter.OperatorItem;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.ChatSmileyAdapter;
import com.android.hcframe.im.data.ChatTextOtherInfo;
import com.android.hcframe.im.data.ChatTextOwnInfo;
import com.android.hcframe.im.data.ChatVoiceOtherInfo;
import com.android.hcframe.im.data.ChatVoiceOwnInfo;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.im.voice.Recorder;
import com.android.hcframe.internalservice.contacts.ContactDetailsAct;
import com.android.hcframe.internalservice.contacts.ContactsOperateDatabase;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.view.dialog.ListDialog;
import com.android.hcframe.view.selector.HcChooseHomeView;
import com.android.hcframe.view.selector.ItemInfo;
import com.android.hcframe.view.selector.StaffInfo;
import com.android.hcframe.view.selector.file.image.ImageChooseActivity;
import com.android.hcframe.view.selector.file.image.ImageItemInfo;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-31 08:47.
 */
public class ChatActivity extends HcBaseActivity implements TextWatcher, OnChatReceiveListener,
        View.OnLayoutChangeListener, Recorder.OnStateChangedListener, DraggableGridViewPager.OnPageChangeListener,
        AdapterView.OnItemClickListener, OnReceiverCallback, OnSendCallback {

    private static final String TAG = "ChatActivity";

    public static final int CHAT_TEXT = 1;
    public static final int CHAT_IMAGE = 2;
    public static final int CHAT_VOICE = 3;
    public static final int CHAT_FILE = 4;
    public static final int CHAT_OTHER = 5;

    private TopBarView mTopBar;

    private PullToRefreshListView mListView;

    private ImageView mSwitchBtn;

    private ImageView mEmojiBtn;

    private ImageView mMoreBtn;

    private EditText mEdit;

    private TextView mVoice;

    /** 当前聊天对象,主要从通讯录进入 */
    private ContactsInfo mEmp;
    /** 当前聊天对象,主要从IMHomeView进入 */
    private AppMessageInfo mMessageInfo;

    private ChatMode mMode = ChatMode.NORMAL;

    private ChatMode mPreMode = ChatMode.NORMAL;

    private ChatMessageAdapter mAdapter;

    private List<ChatMessageInfo> mChats = new ArrayList<ChatMessageInfo>();

    private TextView mSendBtn;
    /** 群聊 */
    private boolean mGroup;

    /** 访问数据库的key,查找对象的聊天记录,有可能是群ID,也有可能是双方的userId的MD5*/
    private String mChatKey;
    /**
     * 每次查询50条记录
     */
    private static final int CHAT_QUERY_COUNT = 20;

    private LinearLayout mParent;

    private InputMethodManager mManager;

    // voice
    private LinearLayout mVoiceParent;
    private LinearLayout mVoiceSpeekParent;
    private ImageView mVoiceFrame; // 动画
    private ImageView mVoiceCanel;
    private TextView mVoiceText;

    private GestureDetector mGestureDetector;


    private LayoutTransition mLayoutAnimation;

    /** 底部操作的View */
    private LinearLayout mBottomParent;

    private FrameLayout mContentParent;

    /** 群聊@选择的人员,但不是最终的人员 */
    private Map<String, String> mReceivers = new HashMap<String, String>();

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:  // not used
                    mVoiceParent.setVisibility(View.GONE);
                    mVoice.setText(getResources().getString(R.string.im_voice_normal));
                    break;
                case 2:
                    if (mRecorder != null) {
                        mRecorder.stopRecording();
                    }
                    break;
                case 3: // 录音时间太短,删除录音的文件.
                    if (mRecorder != null) {
                        mRecorder.delete();
                    }
                    break;
                case 4:
//                    mManager.showSoftInputFromInputMethod(mEdit.getWindowToken(), 0);
                    mManager.showSoftInput(mEdit, 0);
                    break;
                case 5:
                    int size = mChats.size();
                    if (mListView != null && size > 0) {
//                    mListView.smoothScrollToPosition(mChats.size() + 1);
                        ChatMessageInfo chat = mChats.get(size - 1);
                        if (chat instanceof ChatImageOtherInfo || chat instanceof ChatImageOwnInfo) {
//                            mAdapter.notifyDataSetChanged();
                            mListView.setSelection(size);
                        } else {
                            mListView.setSelection(size - 1);
                        }


                        HcLog.D(TAG + " mHandler what = 5    size = "+size);
                    }
                    break;
                case 6:
                    if (mOperatorParent.getVisibility() != View.VISIBLE) {
                        mOperatorParent.setVisibility(View.VISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Object data = intent.getParcelableExtra("emp");
            mGroup = intent.getBooleanExtra("group", false);
            if (data == null) {
                finish();
                return;
            }
            if (data instanceof ContactsInfo) {
                mEmp = (ContactsInfo) data;
            } else {
                mMessageInfo = (AppMessageInfo) data;
            }

        } else {
            finish();
            return;
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        setContentView(R.layout.im_activity_chat_layout);
        mGestureDetector = new GestureDetector(this, new VoiceGestureDetector());
        mGestureDetector.setIsLongpressEnabled(false); // 不能长按,这样长按住可以监听到scroll事件
        initViews();
        initData();
    }

    private void initViews() {
        mParent = (LinearLayout) findViewById(R.id.chat_parent);
        mTopBar = (TopBarView) findViewById(R.id.chat_top_bar);
        mListView = (PullToRefreshListView) findViewById(R.id.chat_listview);
        mEdit = (EditText) findViewById(R.id.chat_edit);
        mEmojiBtn = (ImageView) findViewById(R.id.chat_emoji_btn);
        mMoreBtn = (ImageView) findViewById(R.id.chat_more_btn);
        mSwitchBtn = (ImageView) findViewById(R.id.chat_switch_btn);
        mVoice = (TextView) findViewById(R.id.chat_speek_btn);
        mSendBtn = (TextView) findViewById(R.id.chat_send_btn);

        mContentParent = (FrameLayout) findViewById(R.id.chat_content_parent);

        mParent.addOnLayoutChangeListener(this);

        mBottomParent = (LinearLayout) findViewById(R.id.chat_bottom_parent);
        mVoiceParent = (LinearLayout) findViewById(R.id.chat_voice_parent);
        mVoiceSpeekParent = (LinearLayout) findViewById(R.id.chat_voice_speek_parent);
        mVoiceCanel = (ImageView) findViewById(R.id.chat_voice_canel_image);
        mVoiceFrame = (ImageView) findViewById(R.id.chat_voice_speek);
        mVoiceText = (TextView) findViewById(R.id.chat_voice_text);


        mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mListView.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                // 这里要是数据库里一开始没有数据的话,是不会有下拉的状态的,所以其实不会出现mChats.isEmpty()的情况
                String date = mChats.isEmpty() ? "" + System.currentTimeMillis() : mChats.get(0).getDate();
                List<ChatMessageInfo> chats = ChatOperatorDatabase.getChatMessages(ChatActivity.this, mChatKey, date, CHAT_QUERY_COUNT);
                int size = chats.size();
                if (size < CHAT_QUERY_COUNT) { // 说明数据库里面没有数据了
                    mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                }
//                test();
                // 从数据库里面取出来的是倒序的,即最晚的聊天记录在第一条,所以这里不需要重新排序
                for (int i = 0; i < size; i++) {
                    mChats.add(0, chats.get(i));
                }
                mListView.onRefreshComplete();
                mAdapter.notifyDataSetChanged();
//                mListView.setSelection(8); // test
//                mListView.getFirstVisiblePosition(); // not used
                mListView.setSelection(size - 1);
                chats.clear();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                // do nothing
            }
        });

        mEmojiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSmileyAdapter == null) {
                    initSmiley();
                    mSmileyAdapter = new ChatSmileyAdapter(ChatActivity.this, mIcons);
//                    mViewPager.setAdapter(mSmileyAdapter);
                }

                if (mMode == ChatMode.EMOJI) {
                    setMode(ChatMode.TEXT);
                } else {
                    setMode(ChatMode.EMOJI);
                }
            }
        });

        mSwitchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mMode == ChatMode.VOICE) {
                    setMode(ChatMode.TEXT);
                } else {
                    setMode(ChatMode.VOICE);
                }
            }
        });

        mMoreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOperatorAdapter == null) {
                    OperatorItem item = new OperatorItem("图片", R.drawable.im_chatting_image_icon);
                    mOperators.add(item);
                    mOperatorAdapter = new ChatOperatorAdapter(ChatActivity.this, mOperators);

                }
                if (mMode == ChatMode.SELECT) {
                    setMode(ChatMode.TEXT);
                } else {
                    setMode(ChatMode.SELECT);
                }
            }
        });

        mVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                if ((action & MotionEvent.ACTION_MASK) == event.ACTION_UP) {
                    //
                    if (mScroll == true) {
                        mScroll = false;
                        if (mRecorder != null) {
                            mRecorder.delete(); // 这里包括了停止录音
                        }
                    } else {
                        long current = System.currentTimeMillis();
                        if (mAnimation != null) {
                            mAnimation.stop();
                        }
                        if (current - mLastTime < 1000) {
                            if (mVoiceSpeekParent.getVisibility() != View.GONE)
                                mVoiceSpeekParent.setVisibility(View.GONE);
                            if (mVoiceCanel.getVisibility() != View.VISIBLE)
                                mVoiceCanel.setVisibility(View.VISIBLE);
                            mVoiceCanel.setImageResource(R.drawable.voice_to_short);
                            mVoiceText.setText(getResources().getString(R.string.im_voice_time_short));

                            if (mTask != null) {
                                mTask.cancel();
                                mTime = 0;
                                mTask = null;
                            }
                            // 删除放到handler里面处理
                            mHandler.sendEmptyMessageDelayed(3, 1000);

                        } else {
                            int duration = mTime;
                            String filePath = null;
                            if (mTask != null) {
                                mTask.cancel();
                                mTime = 0;
                                mTask = null;
                            }

                            if (mRecorder != null) {
                                filePath = mRecorder.getFilePath();
                                mRecorder.stopRecording();
                            } else {
                                filePath = "";
                            }
                            // 发送录音
                            setSendVoiceData(duration, filePath);


                        }
                    }
                } else if ((action & MotionEvent.ACTION_MASK) == event.ACTION_CANCEL) { // 需要测试
                    if (mRecorder != null) {
                        mRecorder.delete(); // 这里包括了停止录音
                    }
                    mVoiceParent.setVisibility(View.GONE);
                    mVoice.setText(getResources().getString(R.string.im_voice_normal));
                    if (mAnimation != null) {
                        mAnimation.stop();
                    }
                    // 取消超时
                    if (mTask != null) {
                        mTask.cancel();
                        mTask = null;
                        mTime = 0;
                    }
                }
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                long date = System.currentTimeMillis();
                int size = mChats.size();
                String content = mEdit.getText().toString();
                ChatMessageInfo info = new ChatTextOwnInfo();
                info.setChatId(mChatKey);
                info.setContent(content);
                info.setDate("" + date);
                info.setName(SettingHelper.getName(ChatActivity.this));
                info.setUserId(SettingHelper.getUserId(ChatActivity.this));
                if (size > 0)  {
                    ChatMessageInfo chat = mChats.get(size - 1);
                    int num = Integer.valueOf(chat.getMessageId()) + 1;
                    info.setMessageId(num + "");
                    long oldDate = Long.valueOf(chat.getDate());
                    if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                        info.setShowDate(false);
                    } else {
                        info.setShowDate(true);
                    }
                } else {
                    info.setMessageId("" + 0);
                    info.setShowDate(true);
                }
                info.setState(2);
                mChats.add(info);
                mAdapter.notifyDataSetChanged();

//                mListView.setSelection(size); // 这里是size,不是size - 1,因为之前没有添加到mChats里面
                mListView.smoothScrollToPosition(size + 1);
//                mEdit.setText("");

                ChatOperatorDatabase.insertChatMessage(ChatActivity.this, info);
                // 发送消息
                String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.getServerName()
                        : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.getServerName();
                HcLog.D(TAG + " #onClick jid = "+jid);
                if (mGroup) {
                    Editable s = mEdit.getText();
                    BackgroundColorSpan[] spans = s.getSpans(0, s.length(), BackgroundColorSpan.class);
                    int start = -1;
                    int end = -1;
                    StringBuilder builder = new StringBuilder();
                    for (BackgroundColorSpan span : spans) {
                        start = s.getSpanStart(span);
                        end = s.getSpanEnd(span);

                        String name = s.subSequence(start, end - 1).toString();
                        String userId = mReceivers.get(name);
                        HcLog.D(TAG + " $OnClickListener SendBtn Click span start = "+start + " end = "+end + " name = "+name + " userId ="+userId);
                        if (userId != null);
                            builder.append(userId + ";");
                    }
                    int length = builder.length();
                    if (builder.length() > 0) {
                        builder.deleteCharAt(length - 1);
                    }
                    HcLog.D(TAG + " $OnClickListener SendBtn Click receiver = "+builder.toString());
                    mEdit.setText("");
                    IMManager.getInstance().sendMessage(info, content, jid, mGroup, SettingHelper.getUserId(ChatActivity.this), length > 0 ? builder.toString() : null);
                } else {
                    mEdit.setText("");
                    IMManager.getInstance().sendMessage(info, content, jid);
                }


                // 更新主页的消息
                AppMessageInfo appInfo = new AppMessageInfo();
                appInfo.setDate("" + date);
                appInfo.setContent(content);
                if (mMessageInfo != null) {
                    appInfo.setIconUri(mMessageInfo.getIconUri());
                    appInfo.setId(mMessageInfo.getId());
                    appInfo.setTitle(mMessageInfo.getTitle());
                    appInfo.setType(mMessageInfo.getType());
//                    ChatOperatorDatabase.updateAppMessage(ChatActivity.this, appInfo);
                } else { // 说明肯定是单聊
                    appInfo.setIconUri(mEmp.getUserId());
                    appInfo.setId(mChatKey);
                    appInfo.setTitle(mEmp.getName());
                    appInfo.setType(3);
                    // 这里因为不知道有没有数据
//                    ChatOperatorDatabase.insertAppMessage(ChatActivity.this, appInfo);
                }
                ChatOperatorDatabase.updateOrinsertAppMessage(ChatActivity.this, appInfo);
                mEdit.setText("");
            }
        });

        mEdit.addTextChangedListener(this);
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(ChatMode.TEXT);
            }
        });

        IMManager.getInstance().addChatListener(this);

        /*****************表情操作 *********************/

        mOperatorParent = (FrameLayout) findViewById(R.id.chat_operator_parent);
        mSmileyParent = (LinearLayout) findViewById(R.id.chat_smiley_parent);
        mViewPager = (DraggableGridViewPager) findViewById(R.id.chat_smiley_gridview);
        mDotView = (DotView) findViewById(R.id.chat_dot_parent);

        mViewPager.setOnItemClickListener(this);
        mViewPager.setOnPageChangeListener(this);

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                HcLog.D(TAG + " #onTouch v = "+v + " mode = "+mMode);
                if (mMode == ChatMode.EMOJI || mMode == ChatMode.TEXT || mMode == ChatMode.SELECT) {
                    setMode(ChatMode.NORMAL);
                }
                return false;
            }
        });


        mTopBar.setReturnViewListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HcAppState.getInstance().removeActivity(ChatActivity.this);

                /** 这里主要是考虑到从状态栏进入的时候,需要添加个全局监听 */
                IMManager.getInstance().addChatListener(IMReceiverListener.getInstance());
                finish();
            }
        });
    }


    private void initData() {
        mTopBar.setMenuBtnVisiable(View.VISIBLE);
        if (mGroup) {
            mTopBar.setMenuSrc(/*R.drawable.im_chat_group_info_icon*/R.drawable.im_group_setting);
            mTopBar.setMenuListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, IMDiscussionGroupSettings.class);
                    intent.putExtra("group", mMessageInfo);
                    startActivityForResult(intent, 10);
                }
            });
        } else {
            mTopBar.setMenuSrc(R.drawable.im_chatting_chat_info_icon);
            mTopBar.setMenuListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, ContactDetailsAct.class);
                    Bundle bundle = new Bundle();
                    if (mEmp != null)
                        bundle.putParcelable("emp", mEmp);
                    else {
                        ContactsInfo info = ContactsOperateDatabase.getContact(ChatActivity.this, mMessageInfo.getIconUri());
                        bundle.putParcelable("emp", info);
                    }
                    bundle.putBoolean("im", false);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }
        String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.getServerName()
                : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.getServerName();
        IMManager.getInstance().addSendCallback(jid, this);

        mLayoutAnimation = new LayoutTransition();
        mLayoutAnimation.setAnimator(LayoutTransition.CHANGE_APPEARING, null);
        mLayoutAnimation.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);
        mLayoutAnimation.setAnimator(LayoutTransition.DISAPPEARING, null);
        mParent.setLayoutTransition(mLayoutAnimation);
//        setMode(ChatMode.NORMAL);
        mListView.setResetLable(true);
        mListView.setPullDownLable("");
        mListView.setPullDownReleaseLable("");
        mListView.setPullDownRefreshingLable("");
        String title = mEmp == null ? mMessageInfo.getTitle() : mEmp.getName();
        String ownUserId = SettingHelper.getUserId(this);
        mChatKey = mGroup ? mMessageInfo.getId() :
                mEmp == null ? HcUtil.getMD5String(ownUserId + mMessageInfo.getIconUri()) :
                HcUtil.getMD5String(ownUserId + mEmp.getUserId());
        mTopBar.setTitle(title);

        // 去数据库读取数据
        mChats.addAll(ChatOperatorDatabase.getChatMessages(this, mChatKey, "" + System.currentTimeMillis(), CHAT_QUERY_COUNT));
        // 需要排序
        Collections.sort(mChats, new Comparator<ChatMessageInfo>() {
            @Override
            public int compare(ChatMessageInfo lhs, ChatMessageInfo rhs) {
                return Integer.valueOf(lhs.getMessageId()) - Integer.valueOf(rhs.getMessageId());
            }
        });
        if (mChats.size() == CHAT_QUERY_COUNT/*true*/) { // 说明有可能还有数据,可以进行下拉的操作
            mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
//        test();
        if (mChats.size() > 0) {
            mChats.get(0).setShowDate(true);
        }
        mAdapter = new ChatMessageAdapter(this, mChats, mGroup);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mChats.size() - 1);
        mEdit.setHintTextColor(getResources().getColor(R.color.im_edit_hint));
//        SettingHelper.setKeyboradHeight(this, 0); // 用于测试
        int keyBoardHeight = SettingHelper.getKeyboradHeight(this);
        HcLog.D(TAG + "#inisData keyBoardHeight ="+keyBoardHeight);
        if (keyBoardHeight > 0) {
            setMode(ChatMode.NORMAL);
            // 设置高度
            ViewGroup.LayoutParams params = mOperatorParent.getLayoutParams();
            params.height = keyBoardHeight;
            mOperatorParent.setLayoutParams(params);
//            mViewPager.setRowCount(3);
//            mViewPager.setColCount(7);
        } else {
            setMode(ChatMode.TEXT);
//            mEdit.requestFocus();
//            mHandler.sendEmptyMessageDelayed(4, 100);
        }

        if (mGroup) {
            // join
            IMManager.getInstance().execute(new JoinTask(mMessageInfo.getId(), SettingHelper.getName(this)));
//            IMManager.getInstance().joinRoom(mMessageInfo.getId(), SettingHelper.getName(this), true);
        }

    }


    /**
     * 线程不安全的
     * @param name 发送方的名字
     * @param userId 发送方的userId,不一定是当前聊天的对象的userId
     * @param content 发送的内容
     * @param add 是否需要添加到当前列表,true,添加
     * @param type 消息内容类型 1.文本;2.图片;3.语音;4.文件
     * @param attachment 语音或者图片的Base64编码文件
     * @param duration 语音的播放时间
     */
    private void setReceiverData(String name, String userId, String content, boolean add, int type, String attachment, int duration, String chatId, Message.Type messageType) {
        HcLog.D(TAG + " #setReceiverData name = "+name + " userId = "+userId + " content = "+content + " add = "+add + " type = "+type);
        int size = mChats.size();
        long date = System.currentTimeMillis();
        ChatMessageInfo info = null;
        switch (type) {
            case 1:
                info = new ChatTextOtherInfo();
                break;
            case 2:
                info = new ChatImageOtherInfo();
                info.setFilePath(IMUtil.decoderBase64ImageFile(attachment, userId, content));
                content = "[图片]";
                break;
            case 3:
                content = "[语音]";
                info = new ChatVoiceOtherInfo();
                info.setDuration(duration);
                info.setReaded(false);
                info.setFilePath(IMUtil.decoderBase64File(attachment, userId));
                break;
            case 4:
                content = "[文件]";
                break;

            default:
                info = new ChatTextOtherInfo();
                break;
        }
        info.setName(name);
        info.setUserId(userId);
        info.setChatId(chatId);
        info.setContent(content);
        info.setDate("" + date);
        if (size > 0)  {
            ChatMessageInfo chatMessage = mChats.get(size - 1);
            int num = Integer.valueOf(chatMessage.getMessageId()) + 1;
            info.setMessageId(num + "");
            long oldDate = Long.valueOf(chatMessage.getDate());
            if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                info.setShowDate(false);
            } else {
                info.setShowDate(true);
            }
        } else {
            info.setMessageId("" + 0);
            info.setShowDate(true);
        }
        if (add) {
            final ChatMessageInfo chatInfo = info;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mChats.add(chatInfo);
                    mAdapter.notifyDataSetChanged();
                    mListView.smoothScrollToPosition(mChats.size() + 1);
                }
            });

        }

        ChatOperatorDatabase.insertChatMessage(ChatActivity.this, info);

        // 更新主页的消息
        AppMessageInfo appInfo = new AppMessageInfo();
        appInfo.setDate("" + date);
        appInfo.setContent(content);

        appInfo.setId(chatId);
        appInfo.setTitle(name);

        if (messageType == Message.Type.chat) {
            appInfo.setType(3);
            appInfo.setIconUri(userId);
//            ChatOperatorDatabase.updateAppMessage(ChatActivity.this, appInfo);
        } else if (messageType == Message.Type.groupchat) {
            appInfo.setIconUri("drawable://" + R.drawable.im_chat_group_icon);
            appInfo.setType(2);
        } else {
            appInfo.setIconUri(userId);
            appInfo.setType(3);
//            ChatOperatorDatabase.insertAppMessage(ChatActivity.this, appInfo);
        }
        ChatOperatorDatabase.updateOrinsertAppMessage(ChatActivity.this, appInfo);
        HcLog.D(TAG + " #setReceiverData end name = "+name + " userId = "+userId + " content = "+content + " add = "+add + " type = "+type);
    }


    @Override
    public void onReceive(Chat chat, final Message message) {
        // 注意这里可能会接收到单聊或者群聊的信息,也可能会接收到其他信息
        switch (message.getType()) {
            case error:
                HcLog.D(TAG + " 信息发送失败!");
                break;
            case normal:
                IMUtil.parseMessage(this, message, this);
                break;
            case groupchat:
            case chat:
                String name = null;
                String userId = null;
                String title = null;
                if (mEmp != null) { // 说明单聊
                    name = mEmp.getName();
                    userId = mEmp.getUserId();
                    title = name;
                    HcLog.D(TAG + "#onReceive 从通讯录进入 单聊 userId ="+userId);

                } else {
                    if (mGroup) {
                        // 说明群聊
                        title = mMessageInfo.getTitle();
                        userId = mMessageInfo.getId(); // 这里和单聊有区别

                    } else {
                        name = mMessageInfo.getTitle();
                        userId = mMessageInfo.getIconUri();
                        title = name;
                        HcLog.D(TAG + "#parseMessage 从IM列表进入 单聊 userId ="+userId);

                    }
                }
                IMUtil.parseMessage(this, message, name, title,  userId, this);
                /**
                String from = message.getFrom();
                if (TextUtils.isEmpty(from)) { // 出错,应该不会出错.

                } else {

                    HcLog.D(TAG + "#onReceive before sub from = "+from);
                    from = from.substring(0, from.indexOf("@"));
                    HcLog.D(TAG + "#onReceive after sub from = "+from);
//                    int size = mChats.size();
                    String mediaType = message.getMediaType();
                    String name;
                    String userId;
                    String content; // 显示的内容
                    String attachment = null; // 语音或者图片附件
                    int type = 1;
                    int duration = 0;
                    boolean add = true;
                    // 普通文本
                    content = message.getBody();
                    if (!TextUtils.isEmpty(mediaType)) {
                        HcLog.D(TAG + "#onReceive mediaType = "+mediaType);
                        type = Integer.valueOf(mediaType);
                        attachment = message.getAttachment();

                        if (type == 3) {
//                            content = "[语音]";
                            duration = Integer.valueOf(message.getDuration());
                        }
//                        else if (type == 2) {
//                            content = "[图片]";
//                        } else if (type == 4) {
//                            content = "[文件]";
//                        } else {
//                            content = message.getBody();
//                        }

                    }

                    HcLog.D(TAG + " #onReceive message content = "+content + " type = "+type + " duration = "+duration);
//                    if (TextUtils.isEmpty(content)) return;

                    // 先测试
                    if (mEmp != null) { // 说明单聊
                        name = mEmp.getName();
                        userId = mEmp.getUserId();
                        add = userId.equals(from);
                        HcLog.D(TAG + "#onReceive 从通讯录进入 单聊 userId ="+userId);
                        if (!add) {
                            userId = from;
                        }
                        setReceiverData(name, userId, content, add, type, attachment, duration, mChatKey, Message.Type.chat);

                    } else {
                        if (mGroup) {
                            // 说明不需要显示
                        } else {
                            name = mMessageInfo.getTitle();
                            userId = mMessageInfo.getIconUri();
                            add = userId.equals(from);
                            HcLog.D(TAG + "#onReceive 从IM列表进入 单聊 userId ="+userId);
                            if (!add) {
                                userId = from;
                            }
                            setReceiverData(name, userId, content, add, type, attachment, duration, mChatKey, Message.Type.chat);
                        }
                    }
//                    mListView.smoothScrollToPosition(size + 1);
//                mListView.setSelection(mChats.size());
                }

                break;
            case groupchat:

                parseMessage(message, Message.Type.groupchat);
                break;
                 */
            default:
                break;
        }
    }

    private void parseMessage(Message message, Message.Type messageType) {
        String from = message.getFrom();
        if (TextUtils.isEmpty(from)) { // 出错,应该不会出错.
            HcLog.D(TAG + "#parseMessage  error 没有发送方！！！！！");
        } else {

            HcLog.D(TAG + "#parseMessage before sub from = "+from);
            if (messageType == Message.Type.chat)
                from = from.substring(0, from.indexOf("@"));

            HcLog.D(TAG + "#parseMessage after sub from = "+from);
//                    int size = mChats.size();
            String mediaType = message.getMediaType();
            String name;
            String userId;
            String content; // 显示的内容
            String attachment = null; // 语音或者图片附件
            String fromUserId = null; // 群聊的消息发送方
            String chatId = null;
            int type = 1;
            int duration = 0;
            boolean add = true;
            // 普通文本,可能是资源名
            content = message.getBody();
            if (messageType == Message.Type.groupchat) {
                fromUserId = message.getUserId();
                HcLog.D(TAG + "#parseMessage fromUserId = "+fromUserId);
                if (SettingHelper.getUserId(this).equals(fromUserId)) {
                    HcLog.D(TAG + "#parseMessage 群聊 接收到了自己的群聊消息！！！！！");
                    return;
                }
            }
            if (!TextUtils.isEmpty(mediaType)) {
                HcLog.D(TAG + "#parseMessage mediaType = "+mediaType);
                type = Integer.valueOf(mediaType);
                attachment = message.getAttachment();

                if (type == 3) {
                    duration = Integer.valueOf(message.getDuration());
                }

            }

            HcLog.D(TAG + " #parseMessage message content = "+content + " type = "+type + " duration = "+duration);

            // 先测试
            if (mEmp != null) { // 说明单聊
                name = mEmp.getName();
                userId = mEmp.getUserId();
                HcLog.D(TAG + "#parseMessage 从通讯录进入 单聊 userId ="+userId);

            } else {
                if (mGroup) {
                    // 说明群聊
                    name = mMessageInfo.getTitle();
                    userId = mMessageInfo.getId(); // 这里和单聊有区别

                } else {
                    name = mMessageInfo.getTitle();
                    userId = mMessageInfo.getIconUri();
                    HcLog.D(TAG + "#parseMessage 从IM列表进入 单聊 userId ="+userId);

                }
            }
            add = userId.equals(from);
            if (!add) {
                if (messageType == Message.Type.groupchat) {
                    chatId = from;
                    userId = fromUserId;
                } else { // 单聊接收到了其他消息
                    userId = from;
                    chatId = HcUtil.getMD5String(SettingHelper.getUserId(this) + userId);
                }

            } else {
                chatId = mChatKey;
            }
            setReceiverData(name, userId, content, add, type, attachment, duration, chatId, messageType);

//                    mListView.smoothScrollToPosition(size + 1);
//                mListView.setSelection(mChats.size());
        }
    }

    public enum ChatMode {
        /** 普通的状态,没有进入编辑状态 */
        NORMAL,
        /** 文字编辑状态 */
        TEXT,
        /** 语音的状态 */
        VOICE,
        /** 表情的状态 */
        EMOJI,
        /** 选择的状态 */
        SELECT
    }

    private void setMode(ChatMode mode) {
        HcLog.D(TAG + " #setMode pre mode = "+mMode + " current = "+mode);
        if (mode == null | mMode == mode) return;
        mPreMode = mMode;
        mMode = mode;
        switch (mMode) {
            case NORMAL:
                if (mOperatorParent.getVisibility() != View.GONE) {
                    mOperatorParent.setVisibility(View.GONE);
                }
                // 软件盘隐藏
                mManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                unlockContentHeightDelayed();

                mSwitchBtn.setImageResource(R.drawable.im_chatting_voice_btn_src);
                mEmojiBtn.setImageResource(R.drawable.im_chatting_emoji_btn_src);
                if (mVoice.getVisibility() != View.GONE)
                    mVoice.setVisibility(View.GONE);
                Editable edit = mEdit.getText();
                if (edit != null && edit.length() > 0) {
                    if (mSendBtn.getVisibility() != View.VISIBLE)
                        mSendBtn.setVisibility(View.VISIBLE);
                    if (mMoreBtn.getVisibility() != View.GONE)
                        mMoreBtn.setVisibility(View.GONE);
                } else {
                    if (mSendBtn.getVisibility() != View.GONE)
                        mSendBtn.setVisibility(View.GONE);
                    if (mMoreBtn.getVisibility() != View.VISIBLE)
                        mMoreBtn.setVisibility(View.VISIBLE);
                }

                if (mEmojiBtn.getVisibility() != View.VISIBLE)
                    mEmojiBtn.setVisibility(View.VISIBLE);
                if (mEdit.getVisibility() != View.VISIBLE)
                    mEdit.setVisibility(View.VISIBLE);

                break;
            case VOICE:
                if (mOperatorParent.getVisibility() != View.GONE) {
                    mOperatorParent.setVisibility(View.GONE);
                }
                // 软件盘隐藏
                mManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);

                mSwitchBtn.setImageResource(R.drawable.im_chatting_keyboard_btn_src);
                if (mEmojiBtn.getVisibility() != View.GONE)
                    mEmojiBtn.setVisibility(View.GONE);
                if (mSendBtn.getVisibility() != View.GONE)
                    mSendBtn.setVisibility(View.GONE);
                if (mMoreBtn.getVisibility() != View.VISIBLE)
                    mMoreBtn.setVisibility(View.VISIBLE);
                if (mEdit.getVisibility() != View.GONE)
                    mEdit.setVisibility(View.GONE);
                if (mVoice.getVisibility() != View.VISIBLE)
                    mVoice.setVisibility(View.VISIBLE);
                if (mListView != null && mChats.size() > 0) {
                    mListView.setSelection(mChats.size() - 1);
                }
                break;
            case EMOJI:
                // 软件盘隐藏
                mViewPager.setRowCount(3);
                mViewPager.setColCount(7);
                mViewPager.setAdapter(mSmileyAdapter);
                mEdit.requestFocus();
                if (isSoftInputShown() || mPreMode == ChatMode.SELECT) {
                    lockContentHeight();
                    mManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                    if (mOperatorParent.getVisibility() != View.VISIBLE) {
                        mOperatorParent.setVisibility(View.VISIBLE);
                    }
                    unlockContentHeightDelayed();
                } else {
                    HcLog.D(TAG + "#setMode 软件盘没有显示 EMOJI mOperatorParent height = "+mOperatorParent.getHeight());
                    if (mOperatorParent.getVisibility() != View.VISIBLE) {
                        mOperatorParent.setVisibility(View.VISIBLE);
                    }
                }

                if (mDotView.getVisibility() != View.VISIBLE)
                    mDotView.setVisibility(View.VISIBLE);
//                mHandler.sendEmptyMessageDelayed(6, 100);
//                if (mOperatorParent.getVisibility() != View.VISIBLE) {
//                    mOperatorParent.setVisibility(View.VISIBLE);
//                }
                mSwitchBtn.setImageResource(R.drawable.im_chatting_voice_btn_src);
                mEmojiBtn.setImageResource(R.drawable.im_chatting_keyboard_btn_src);
                if (mVoice.getVisibility() != View.GONE)
                    mVoice.setVisibility(View.GONE);
                Editable edit2 = mEdit.getText();
                if (edit2 != null && edit2.length() > 0) {
                    if (mSendBtn.getVisibility() != View.VISIBLE)
                        mSendBtn.setVisibility(View.VISIBLE);
                    if (mMoreBtn.getVisibility() != View.GONE)
                        mMoreBtn.setVisibility(View.GONE);
                } else {
                    if (mSendBtn.getVisibility() != View.GONE)
                        mSendBtn.setVisibility(View.GONE);
                    if (mMoreBtn.getVisibility() != View.VISIBLE)
                        mMoreBtn.setVisibility(View.VISIBLE);
                }
                if (mEmojiBtn.getVisibility() != View.VISIBLE)
                    mEmojiBtn.setVisibility(View.VISIBLE);
                if (mEdit.getVisibility() != View.VISIBLE)
                    mEdit.setVisibility(View.VISIBLE);

                mHandler.sendEmptyMessageDelayed(5, 100);
//                if (mListView != null && mChats.size() > 0) {
////                    mListView.smoothScrollToPosition(mChats.size() + 1);
//                    mListView.setSelection(mChats.size() - 1);
//                }
                break;
            case SELECT:
                mViewPager.setRowCount(1);
                mViewPager.setColCount(4);
                mViewPager.setAdapter(mOperatorAdapter);
//                mMoreBtn.requestFocus();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEdit.requestFocus();
                    }
                }, 200);

                if (isSoftInputShown() || mPreMode == ChatMode.EMOJI) {
                    lockContentHeight();
                    mManager.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
                    if (mOperatorParent.getVisibility() != View.VISIBLE) {
                        mOperatorParent.setVisibility(View.VISIBLE);
                    }
                    unlockContentHeightDelayed();
                } else {
                    HcLog.D(TAG + "#setMode 软件盘没有显示 SELECT mOperatorParent height = "+mOperatorParent.getHeight());
                    if (mOperatorParent.getVisibility() != View.VISIBLE) {
                        mOperatorParent.setVisibility(View.VISIBLE);
                    }
                }
                if (mDotView.getVisibility() != View.INVISIBLE)
                    mDotView.setVisibility(View.INVISIBLE);
//                mHandler.sendEmptyMessageDelayed(6, 100);
//                if (mOperatorParent.getVisibility() != View.VISIBLE) {
//                    mOperatorParent.setVisibility(View.VISIBLE);
//                }
                mSwitchBtn.setImageResource(R.drawable.im_chatting_voice_btn_src);
                mEmojiBtn.setImageResource(R.drawable.im_chatting_emoji_btn_src);
                if (mVoice.getVisibility() != View.GONE)
                    mVoice.setVisibility(View.GONE);

                if (mEmojiBtn.getVisibility() != View.VISIBLE)
                    mEmojiBtn.setVisibility(View.VISIBLE);
                if (mEdit.getVisibility() != View.VISIBLE)
                    mEdit.setVisibility(View.VISIBLE);

                mHandler.sendEmptyMessageDelayed(5, 100);
//                if (mListView != null && mChats.size() > 0) {
////                    mListView.smoothScrollToPosition(mChats.size() + 1);
//                    mListView.setSelection(mChats.size() - 1);
//                }
                break;
            case TEXT:
                mEdit.requestFocus();
                if (mPreMode == ChatMode.NORMAL) {
                    if (mOperatorParent.getVisibility() != View.GONE) {
                        mOperatorParent.setVisibility(View.GONE);
                    }
                    mManager.showSoftInput(mEdit, 0);
                } else {
                    lockContentHeight();//显示软件盘时，锁定内容高度，防止跳闪。
                    if (mOperatorParent.getVisibility() != View.GONE) {
                        mOperatorParent.setVisibility(View.GONE);
                    }
                    mManager.showSoftInput(mEdit, 0);
                    //软件盘显示后，释放内容高度
                    mEdit.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            unlockContentHeightDelayed();
                        }
                    }, 200L);
                }



                mSwitchBtn.setImageResource(R.drawable.im_chatting_voice_btn_src);
                mEmojiBtn.setImageResource(R.drawable.im_chatting_emoji_btn_src);
                if (mVoice.getVisibility() != View.GONE)
                    mVoice.setVisibility(View.GONE);
                Editable edit3 = mEdit.getText();
                if (edit3 != null && edit3.length() > 0) {
                    if (mSendBtn.getVisibility() != View.VISIBLE)
                        mSendBtn.setVisibility(View.VISIBLE);
                    if (mMoreBtn.getVisibility() != View.GONE)
                        mMoreBtn.setVisibility(View.GONE);
                } else {
                    if (mSendBtn.getVisibility() != View.GONE)
                        mSendBtn.setVisibility(View.GONE);
                    if (mMoreBtn.getVisibility() != View.VISIBLE)
                        mMoreBtn.setVisibility(View.VISIBLE);
                }
                if (mEmojiBtn.getVisibility() != View.VISIBLE)
                    mEmojiBtn.setVisibility(View.VISIBLE);
                if (mEdit.getVisibility() != View.VISIBLE)
                    mEdit.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(5, 100);
//                if (mListView != null && mChats.size() > 0) {
////                    mListView.smoothScrollToPosition(mChats.size() + 1);
//                    mListView.setSelection(mChats.size() - 1);
//                }
                break;

            default:
                break;
        }

        HcLog.D(TAG + " #setMode end !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private void test() {
        ChatMessageInfo info = new ChatTextOtherInfo();
        info.setName("老王");
        info.setContent("晚上要不要回家吃饭,家里来客人了!");
        info.setDate("" + (System.currentTimeMillis() - 60 * 1000));
        mChats.add(info);
        info = new ChatTextOtherInfo();
        info.setName("老王");
        info.setContent("晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!" +
                "晚上要不要回家吃饭,家里来客人了!");
        info.setDate("" + (System.currentTimeMillis() - 60 * 60 * 60 * 1000));
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setContent("晚上回来吃的.准备好饭菜！");
        info.setDate("" + (System.currentTimeMillis() - 24 * 60 * 1000));
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setDate("" + (System.currentTimeMillis() - 30 * 60 * 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！" +
                "晚上回来吃的.准备好饭菜！");
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setShowDate(false);
        info.setName("小王");
        info.setDate("" + (System.currentTimeMillis() - 55 * 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！");
        mChats.add(info);info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setShowDate(false);
        info.setDate("" + (System.currentTimeMillis() - 100 * 24 * 60 * 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！");
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setDate("" + (System.currentTimeMillis() - 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！");
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setDate("" + (System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！");
        mChats.add(info);
        info = new ChatTextOwnInfo();
        info.setName("小王");
        info.setDate("" + (System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000));
        info.setContent("晚上回来吃的.准备好饭菜！");
        mChats.add(info);

    }

    private int mDeleteStart = -1;

    private int mDeleteEnd = -1;

    @Override
    public void afterTextChanged(Editable s) {
        if (s != null && s.length() > 0) {
            HcLog.D(TAG + " #afterTextChanged text = "+s.toString());
            if (mMoreBtn.getVisibility() != View.GONE)
                mMoreBtn.setVisibility(View.GONE);
            if (mSendBtn.getVisibility() != View.VISIBLE)
                mSendBtn.setVisibility(View.VISIBLE);
            if (mGroup && mDeleteStart >=0 && mDeleteEnd > 0 && mDeleteEnd > mDeleteStart) {
                int deleteStart = mDeleteStart;
                int deleteEnd = mDeleteEnd;
                mDeleteStart = -1;
                mDeleteEnd = -1;
                s.delete(deleteStart, deleteEnd);
            }
        } else {
            if (mSendBtn.getVisibility() != View.GONE)
                mSendBtn.setVisibility(View.GONE);
            if (mMoreBtn.getVisibility() != View.VISIBLE)
                mMoreBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        HcLog.D(TAG + " #beforeTextChanged start = "+start + " after = "+after + " count = "+count + " text = "+s.toString());
        if (mGroup && after == 0 && start > 0 && count == 1) { // 说明是删除 && 最少有两位
            String text = s.toString();
            String deleteStr = text.substring(0, start + 1);
            String textChar = deleteStr.substring(start - 1, start); // 倒数第二个
            //如果截取到的光标最后一位为" " && 光标的倒数第二位不为" "
            if (deleteStr.endsWith(" ") && !" ".equals(textChar)) {
                mDeleteStart = deleteStr.lastIndexOf("@");
                mDeleteEnd = start;
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        HcLog.D(TAG + " #onTextChanged start = "+start + " before = "+before + " count = "+count + " text = "+s.toString());
        if (mGroup && before == 0 && count == 1) { // 说明是增加字符
            String text = s.toString();
            String addStr = text.substring(0, start + 1);
            if (addStr.toString().endsWith("@")) {
                startSelectActivity();
            }
        }

    }

    private boolean mIsFirst = true;

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        HcLog.D(TAG + "#onLayoutChange v = "+v + " top = "+top + " bottom = "+bottom + " oldTop = "+oldTop + " oldBottom = "+oldBottom);
        int size = mChats.size();
        if (bottom < oldBottom) { // 软件盘出现,但是第一次没有出现
            if (mIsFirst) {
                mIsFirst = !mIsFirst;
                if (mMode == ChatMode.NORMAL) {
                    setMode(ChatMode.TEXT);
                }
            }
//
//            if (mOperatorParent.getVisibility() != View.GONE)
//                mOperatorParent.setVisibility(View.GONE);
//            if (mListView != null && size > 0) {
//                mListView.smoothScrollToPosition(size + 1);
//            }
            int keyboardHeight = SettingHelper.getKeyboradHeight(this);
            if (keyboardHeight <= 0) {
                keyboardHeight = oldBottom - bottom;
                SettingHelper.setKeyboradHeight(this, keyboardHeight);
                // 设置高度
                ViewGroup.LayoutParams params = mOperatorParent.getLayoutParams();
                params.height = keyboardHeight;
                mOperatorParent.setLayoutParams(params);
            }
        } else if (bottom > oldBottom) { // 软件盘消失
            // 有可能是返回键,也可能是按表情的时候,按语音的时候
            if (mMode == ChatMode.TEXT)
                setMode(ChatMode.NORMAL);
//            if (mListView != null && size > 0) {
//                mListView.smoothScrollToPosition(size - 1);
//            }
        }
    }

    private void setSendVoiceData(int duration, String filePath) {
        long date = System.currentTimeMillis();
        int size = mChats.size();
        ChatMessageInfo info = new ChatVoiceOwnInfo();
        info.setChatId(mChatKey);
        info.setContent(filePath);
        info.setDate("" + date);
        info.setName(SettingHelper.getName(ChatActivity.this));
        info.setUserId(SettingHelper.getUserId(ChatActivity.this));
        if (size > 0)  {
            ChatMessageInfo chat = mChats.get(size - 1);
            int num = Integer.valueOf(chat.getMessageId()) + 1;
            info.setMessageId(num + "");
            long oldDate = Long.valueOf(chat.getDate());
            if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                info.setShowDate(false);
            } else {
                info.setShowDate(true);
            }
        } else {
            info.setMessageId("" + 0);
            info.setShowDate(true);
        }
        HcLog.D(TAG + " #setSendVoiceData filePath = "+filePath);
        info.setFilePath(filePath);
        info.setDuration(duration);
        info.setState(2);
        mChats.add(info);
        mAdapter.notifyDataSetChanged();
        mListView.smoothScrollToPosition(size + 1);

        ChatOperatorDatabase.insertChatMessage(ChatActivity.this, info);
        // 发送消息
        String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.getServerName()
                : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.getServerName();
        HcLog.D(TAG + " #onClick jid = "+jid);
        IMManager.getInstance().sendMediaMessage(info, filePath, jid, IMUtil.encodeBase64File(filePath), "3","" + duration, mGroup, SettingHelper.getUserId(this));

        // 更新主页的消息
        AppMessageInfo appInfo = new AppMessageInfo();
        appInfo.setDate("" + date);
        appInfo.setContent("[语音]");
        if (mMessageInfo != null) {
            appInfo.setIconUri(mMessageInfo.getIconUri());
            appInfo.setId(mMessageInfo.getId());
            appInfo.setTitle(mMessageInfo.getTitle());
            appInfo.setType(mMessageInfo.getType());
        } else { // 说明肯定是单聊
            appInfo.setIconUri(mEmp.getUserId());
            appInfo.setId(mChatKey);
            appInfo.setTitle(mEmp.getName());
            appInfo.setType(3);
        }
        ChatOperatorDatabase.updateOrinsertAppMessage(ChatActivity.this, appInfo);
    }

    private long mLastTime;

    private boolean mScroll;

    private  class VoiceGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            HcLog.D(TAG + "$VoiceGestureDetector#onFling!!!!! ");
            // 滑动结束,录音取消.
            /** 放到up那里和onStateChanged里面处理了
            mVoiceParent.setVisibility(View.GONE);
            mVoice.setText(getResources().getString(R.string.im_voice_normal));
            mHandler.removeMessages(1);
             */
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            HcLog.D(TAG + "$VoiceGestureDetector#onSingleTapConfirmed!!!!!");
            // 没有滑动操作,录音结束.
            /**
             * @jrjin
             * @2016-10-18
             * 放到up里面处理
             */
            /**
            long current = System.currentTimeMillis();
            if (mAnimation != null) {
                mAnimation.stop();
            }
            if (current - mLastTime < 1000) {
                if (mVoiceSpeekParent.getVisibility() != View.GONE)
                    mVoiceSpeekParent.setVisibility(View.GONE);
                if (mVoiceCanel.getVisibility() != View.VISIBLE)
                    mVoiceCanel.setVisibility(View.VISIBLE);
                mVoiceCanel.setImageResource(R.drawable.voice_to_short);
                mVoiceText.setText(getResources().getString(R.string.im_voice_time_short));

                if (mTask != null) {
                    mTask.cancel();
                    mTime = 0;
                    mTask = null;
                }
                // 删除放到handler里面处理
                mHandler.sendEmptyMessageDelayed(3, 1000);

            } else {
                int duration = mTime;
                String filePath = null;
                if (mTask != null) {
                    mTask.cancel();
                    mTime = 0;
                    mTask = null;
                }

                if (mRecorder != null) {
                    filePath = mRecorder.getFilePath();
                    mRecorder.stopRecording();
                } else {
                    filePath = "";
                }
                // 发送录音
                setSendVoiceData(duration, filePath);


            }*/
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            HcLog.D(TAG + "$VoiceGestureDetector#onDown!!!!!");
            // 开始录音
            if (mRecorder == null) {
                mRecorder = new Recorder(mChatKey);
                mRecorder.setOnStateChangedListener(ChatActivity.this);
            }

//            mLastTime = System.currentTimeMillis();
//            if (mVoiceParent.getVisibility() != View.GONE)
//                mVoiceParent.setVisibility(View.GONE);
//            mVoiceParent.setVisibility(View.VISIBLE);
//            if (mVoiceCanel.getVisibility() != View.GONE)
//                mVoiceCanel.setVisibility(View.GONE);
//            if (mVoiceSpeekParent.getVisibility() != View.VISIBLE)
//                mVoiceSpeekParent.setVisibility(View.VISIBLE);
//
//            mVoiceText.setText(getResources().getString(R.string.im_voice_canel));
//            mVoice.setText(getResources().getString(R.string.im_voice_pressed));

            startRecording();


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            HcLog.D(TAG + "$VoiceGestureDetector#onScroll!!!!!");
            // 取消录音,滑动的时候不能停留太久,不然消失不及时.
            mScroll = true;

            if (mVoiceSpeekParent.getVisibility() != View.GONE)
                mVoiceSpeekParent.setVisibility(View.GONE);
            if (mVoiceCanel.getVisibility() != View.VISIBLE)
                mVoiceCanel.setVisibility(View.VISIBLE);
            if (mAnimation != null) {
                mAnimation.stop();
            }
            mVoiceCanel.setImageResource(R.drawable.voice_to_canel);
            mVoiceText.setText(getResources().getString(R.string.im_voice_over));
            mVoice.setText(getResources().getString(R.string.im_voice_over));
            // 取消超时
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
                mTime = 0;
            }


            /**
             * 更改为up的时候再停止录音
             if (mRecorder != null) {
                mRecorder.delete(); // 这里包括了停止录音
             }

             mHandler.sendEmptyMessageDelayed(1, 1000);
             */
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            HcLog.D(TAG + "$VoiceGestureDetector#onSingleTapUp!!!!!");
            return true;
        }
    }

    private AudioManager mAudioManager;

    private Timer mTimer;

    private RecorderTask mTask;

    private volatile int mTime;

    private Recorder mRecorder;

    private static final String AUDIO_3GPP = "audio/3gpp";
    private static final String AUDIO_AMR = "audio/amr";
    private static final String AUDIO_ANY = "audio/*";
    private static final String ANY_ANY = "*/*";
    private static final String AUDIO_AAC = "audio/aac";

    private String mRequestedType = AUDIO_AAC;

    private AnimationDrawable mAnimation;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mMode == ChatMode.TEXT || mMode == ChatMode.EMOJI || mMode == ChatMode.SELECT) {
                        setMode(ChatMode.NORMAL);
                        return true;
                    }
                    HcAppState.getInstance().removeActivity(this);
                    /** 这里主要是考虑到从状态栏进入的时候,需要添加个全局监听 */
                    IMManager.getInstance().addChatListener(IMReceiverListener.getInstance());
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                mAudioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mAudioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class RecorderTask extends TimerTask {

        @Override
        public void run() {
            HcLog.D(TAG + "$RecorderTask#run time = "+mTime);
            mTime ++;
            if (mTime == 60) {
                mHandler.sendEmptyMessage(2);
                cancel();
                mTask = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder = null;
        }
        IMUtil.clearImageCache();
        IMManager.getInstance().removeSendCallback();
        IMManager.getInstance().removeChatListener(this);
        super.onDestroy();
    }

    /*
     * Make sure we're not recording music playing in the background, ask
     * the MediaPlaybackService to pause playback.
     */
    private void stopAudioPlayback() {
        // Shamelessly copied from MediaPlaybackService.java, which
        // should be public, but isn't.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");

        sendBroadcast(i);
    }

    private void startRecording() {
        if (HcUtil.canStorage(this)) {
            stopAudioPlayback();
            if (AUDIO_AMR.equals(mRequestedType)) {
//                if (mTimer == null) {
//                    mTimer = new Timer();
//                }
//                mTime = 0;
//                if (mTask != null) {
//                    mTask.cancel();
//                    mTask = null;
//                }
//                mTask = new RecorderTask();
//                mTimer.schedule(mTask, 1000, 1000);
                mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, ".amr", this,
                        SettingHelper.getUserId(ChatActivity.this) + "-" + System.currentTimeMillis());
                // 开始动画
//                if (mAnimation == null) {
//                    mAnimation = (AnimationDrawable) mVoiceFrame.getDrawable();
//                }
//                mAnimation.start();
            } else if (AUDIO_3GPP.equals(mRequestedType)) {
//                if (mTimer == null) {
//                    mTimer = new Timer();
//                }
//                mTime = 0;
//                mTask = new RecorderTask();
//                mTimer.schedule(mTask, 1000, 1000);
                mRecorder.startRecording(MediaRecorder.OutputFormat.THREE_GPP, ".3gpp",
                        this, SettingHelper.getUserId(ChatActivity.this) + "-" + System.currentTimeMillis());
                // 开始动画
//                if (mAnimation == null) {
//                    mAnimation = (AnimationDrawable) mVoiceFrame.getDrawable();
//                }
//                mAnimation.start();
            } else if (AUDIO_AAC.equals(mRequestedType)) {

                mRecorder.startRecording(MediaRecorder.OutputFormat.AAC_ADTS, ".aac",
                        this, SettingHelper.getUserId(ChatActivity.this) + "-" + System.currentTimeMillis());

            } else {
                throw new IllegalArgumentException("Invalid output file type requested");
            }
        }

    }

    @Override
    public void onError(int error) {
        HcLog.D(TAG + " #onError error = "+error);
    }

    @Override
    public void onStateChanged(int state) {
        HcLog.D(TAG + " #onStateChanged state = "+state + " mPre state = "+mState);
        if (mState == mRecorder.state()) return;
        int preState = mState;
        mState = state;
        switch (mRecorder.state()) {
            case Recorder.IDLE_STATE:
                //
                if (preState == Recorder.PLAYING_STATE) {
                    if (mPlayers.size() > 0) {
                        Player player = mPlayers.remove(0);
                        player.mInfo.setSpeeking(false);
                        ImageView imageView = player.mSrc;
                        Drawable src = imageView.getDrawable();
                        AnimationDrawable an;
                        if (src instanceof AnimationDrawable) {
                            an = (AnimationDrawable) src;
                            if (an.isRunning()) {
                                an.stop();
                            }
                            if (player.mInfo.isOwn())
                                imageView.setImageResource(R.drawable.im_voice_speek_own);
                            else {
                                imageView.setImageResource(R.drawable.im_voice_speek_other_3);
                            }
                        }
                    }

                } else if (preState == Recorder.RECORDING_STATE) {
                    mVoiceParent.setVisibility(View.GONE);
                    mVoice.setText(getResources().getString(R.string.im_voice_normal));
                }
                break;
            case Recorder.PLAYING_STATE:
                if (mPlayers.size() > 0) {
                    Player player = mPlayers.get(0);
                    player.mInfo.setSpeeking(true);
                    ImageView imageView = player.mSrc;
                    Drawable src = imageView.getDrawable();
                    AnimationDrawable an;
                    if (src instanceof AnimationDrawable) {
                        an = (AnimationDrawable) src;
                        if (!an.isRunning()) {
                            an.start();
                        }
                    } else {
                        if (player.mInfo.isOwn())
                            imageView.setImageResource(R.drawable.im_voice_speeking_own);
                        else {
                            imageView.setImageResource(R.drawable.im_voice_speeking_other);
                        }
                        an = (AnimationDrawable) imageView.getDrawable();
                        an.start();
                    }
                } else {
                 // Error
                }
                break;
            case Recorder.RECORDING_STATE:
                mLastTime = System.currentTimeMillis();
                if (mVoiceParent.getVisibility() != View.VISIBLE)
                    mVoiceParent.setVisibility(View.VISIBLE);
//                mVoiceParent.setVisibility(View.VISIBLE);
                if (mVoiceCanel.getVisibility() != View.GONE)
                    mVoiceCanel.setVisibility(View.GONE);
                if (mVoiceSpeekParent.getVisibility() != View.VISIBLE)
                    mVoiceSpeekParent.setVisibility(View.VISIBLE);

                mVoiceText.setText(getResources().getString(R.string.im_voice_canel));
                mVoice.setText(getResources().getString(R.string.im_voice_pressed));

                if (mTimer == null) {
                    mTimer = new Timer();
                }

                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                }
                mTime = 0;
                mTask = new RecorderTask();
                mTimer.schedule(mTask, 1000, 1000);
                if (mAnimation == null) {
                    mAnimation = (AnimationDrawable) mVoiceFrame.getDrawable();
                }
                mAnimation.start();
                break;

            default:
                break;
        }
    }

    /**
     *
     * @param position
     * @param info
     * @param speek
     */
    public void startPalyer(int position, ChatMessageInfo info, ImageView speek) {
        if (mRecorder == null) {
            mRecorder = new Recorder(mChatKey);
            mRecorder.setOnStateChangedListener(ChatActivity.this);
        }
        Player player = new Player();
        player.mInfo = info;
        player.mPostion = position;
        player.mSrc = speek;
        addPlayer(player);
        HcLog.D(TAG + " #startPalyer player size = "+mPlayers.size() + " file path = "+info.getFilePath());
        mRecorder.startPlayback(info.getFilePath());
    }

    /**
     *
     * @param position
     * @param info
     * @param speek
     */
    public void stopPlayer(int position, ChatMessageInfo info, ImageView speek) {
        if (mRecorder == null) {
            mRecorder = new Recorder(mChatKey);
            mRecorder.setOnStateChangedListener(ChatActivity.this);
        }
        Player player = new Player();
        player.mInfo = info;
        player.mPostion = position;
        player.mSrc = speek;
        if (containPlayer(player)) {
            mRecorder.stopPlayback();
        } else { // 说明出错了
            info.setSpeeking(false);
            Drawable src = speek.getDrawable();
            AnimationDrawable an;
            if (src instanceof AnimationDrawable) {
                an = (AnimationDrawable) src;
                if (an.isRunning()) {
                    an.stop();
                }
                if (info.isOwn())
                    speek.setImageResource(R.drawable.im_voice_speek_own);
                else {
                    speek.setImageResource(R.drawable.im_voice_speek_other_3);
                }
            }
        }

    }

    private int mState = -1;

    private static class Player {
        int mPostion;
        ChatMessageInfo mInfo;
        ImageView mSrc;
    }

    private List<Player> mPlayers = new ArrayList<Player>();

    private void addPlayer(Player player) {
        Iterator<Player> iterator = mPlayers.iterator();
        boolean added = false;
        while (iterator.hasNext()) {
            if (iterator.next().mInfo == player.mInfo) {
                added = true;
                break;
            }
        }
        if (!added) {
            mPlayers.add(player);
        }
    }

    private boolean containPlayer(Player player) {
        Iterator<Player> iterator = mPlayers.iterator();
        boolean contain = false;
        while (iterator.hasNext()) {
            if (iterator.next().mInfo == player.mInfo) {
                contain = true;
                break;
            }
        }
        return contain;
    }


    /*********************表情和更多的操作*********************************/

    private FrameLayout mOperatorParent;
    private LinearLayout mSmileyParent;
    private DraggableGridViewPager mViewPager;
    private DotView mDotView;

    private List<String> mIcons = new ArrayList<String>();
    private ChatSmileyAdapter mSmileyAdapter;

    private ChatOperatorAdapter mOperatorAdapter;
    private List<OperatorItem> mOperators = new ArrayList<OperatorItem>();

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mDotView != null) {
            mDotView.setCurrentItem(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mMode == ChatMode.EMOJI) {
            String smileyName = mIcons.get(position);
            int selection = mEdit.getSelectionStart();
            if (SMILEY_DELETE.equals(smileyName)) { // 删除按钮
                String text = mEdit.getText().toString();
                if (selection > 0) { // 这里到时还需要解析系统输入法的表情
                    String deleteStr = text.substring(0, selection);
                    String textChar = deleteStr.substring(selection - 1);
                    //如果截取到的光标最后一位为"]"
                    if ("]".equals(textChar)) {
                        int start = deleteStr.lastIndexOf("[");
                        int end = selection;
                        mEdit.getText().delete(start, end);
                    } else if (" ".equals(textChar) && deleteStr.length() > 1 &&
                            !" ".endsWith(deleteStr.substring(selection -2, selection - 1))) {
                        int start = deleteStr.lastIndexOf("@");
                        int end = selection;
                        mEdit.getText().delete(start, end);
                    } else {
                        mEdit.getText().delete(selection - 1, selection);
                    }
                }
            } else {
                SpannableString spannableString = getSmileyString(smileyName);
                if (spannableString != null) {
                    Editable editable = mEdit.getText();
                    editable.insert(selection, spannableString);
                } else {
                    HcLog.D(TAG + " #onItemClick error 添加表情失败! smileyName = "+smileyName);
                }

            }
        } else if (mMode == ChatMode.SELECT) {
            switch (position) {
                case 0:
                    //点击图片按钮
                    Intent intent = new Intent(this, ImageChooseActivity.class);
                    intent.putExtra("className", "com.android.hcframe.im.image.IMImagePage");
                    intent.putExtra("data", "chat");
                    startActivityForResult(intent, 100);
                    break;

                default:
                    break;
            }
        }

    }

    private static final int PAGE_SIZE = 21;
    private static final String SMILEY_DELETE = "im_chatting_smiley_delete_src";

    private void initSmiley() {
        for (int i = 0; i < 90; i++) {
        	mIcons.add("smiley_" + i);
        }
        int pageSize = PAGE_SIZE - 1; //需要减去删除按钮
        int page = (mIcons.size() + pageSize - 1) / pageSize;
        for (int i = 1; i < page; i++) {
        	mIcons.add(i * PAGE_SIZE - 1, SMILEY_DELETE);
        }
        // 增加最后一页的删除按钮
        mIcons.add(SMILEY_DELETE);
        mDotView.setTotalItems(page);
    }

    private SpannableString getSmileyString(String smileyName) {
        int resId = getResources().getIdentifier(smileyName, "drawable", getPackageName());
        if (resId == 0) return null;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        if (bitmap == null) return null;
        Bitmap scanBitmap = Bitmap.createScaledBitmap(bitmap, HcUtil.dip2px(this, 18), HcUtil.dip2px(this, 18), true);
        bitmap.recycle();
        bitmap = null;
        String spannableString = "[" + smileyName + "]";
        ImageSpan imageSpan = new ImageSpan(this, scanBitmap);
        SpannableString spannable = new SpannableString(spannableString);
        spannable.setSpan(imageSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }


    /*************计算ListView的高度**************/
    private int mScreenHeight; // 屏幕的有效高度,不包括内置的返回按钮布局.
    private int mCurrentHeight; // 当前listview的高度
    private int mPreHeight;
    private boolean mChanged = true; // 是否需要改变listview

    private void setParams(int currentHeight) {
        mPreHeight = mCurrentHeight;
        mCurrentHeight = currentHeight;
        HcLog.D(TAG + " #setParams currentHeight = "+mCurrentHeight + " ScreenHeigh = "+mScreenHeight + " preHeight = "+mPreHeight);
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = mCurrentHeight;
        mListView.setLayoutParams(params);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        HcLog.D(TAG + " #onActivityResult requestCode ="+requestCode + " resultCode ="+resultCode + " data = "+data);
        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
                    case 100:
                        if (data != null && data.getExtras() != null) {
                            ImageItemInfo info = data.getParcelableExtra("image");
                            if (info != null) {
                                setSendImageData(info.getImagePath());
                            } else {
                                ArrayList<ImageItemInfo> infos = data.getParcelableArrayListExtra("images");
                                if (infos != null) {
                                    for(ImageItemInfo item : infos) {
                                        setSendImageData(item.getImagePath());
                                    }
                                }
                            }

                        }
                        break;
                    case 10: // 更改群的名字
                        if (data != null && data.getExtras() != null) {
                            String name = data.getStringExtra("name");
                            if (!TextUtils.isEmpty(name)) {
                                mTopBar.setTitle(name);
                                mMessageInfo.setTitle(name);
                            }
                            boolean destory = data.getBooleanExtra("destory", false);
                            if (destory) {
                                finish();
                            }
                        }
                        break;
                    case HcChooseHomeView.REQUEST_CODE:
                        if (data != null && data.getExtras() != null) {
                            ItemInfo info = data.getParcelableExtra(HcChooseHomeView.ITEM_KEY);
                            if (info != null) {
                                int selection = mEdit.getSelectionStart();
                                String name = info.getItemValue() + " ";
                                SpannableString spannableString = new SpannableString(name);
                                spannableString.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                Editable editable = mEdit.getText();
                                editable.insert(selection, spannableString);
                                mReceivers.put(info.getItemValue(), info.getUserId());
                            }
                        }
                        break;
                    case REQUEST_CODE_FORWARD:
                        if (mForward != null && data != null && data.getExtras() != null) {
                            ChatMessageInfo forward = mForward;
                            mForward = null;
                            List<AppMessageInfo> infos = data.getParcelableArrayListExtra("chats");

                            ChatMessageInfo newInfo;
                            long date = System.currentTimeMillis();
                            ChatMessageInfo last;
                            String content;
                            for (AppMessageInfo info : infos) {
                                if (forward.getType() == 1) { // 文本
                                    newInfo = new ChatTextOwnInfo();
                                    content = forward.getContent();
                                } else { // 图片
                                    newInfo = new ChatImageOwnInfo();
                                    content = "[图片]";
                                    newInfo.setFilePath(forward.getFilePath());
                                }
                                newInfo.setState(2);
                                newInfo.setChatId(info.getId());
                                newInfo.setContent(content);
                                newInfo.setDate("" + date);
                                newInfo.setName(SettingHelper.getName(ChatActivity.this));
                                newInfo.setUserId(SettingHelper.getUserId(ChatActivity.this));

                                last = ChatOperatorDatabase.getLastMessage(this, info.getId());
                                if (last != null) {
                                    int num = Integer.valueOf(last.getMessageId()) + 1;
                                    newInfo.setMessageId(num + "");
                                    long oldDate = Long.valueOf(last.getDate());
                                    if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                                        newInfo.setShowDate(false);
                                    } else {
                                        newInfo.setShowDate(true);
                                    }
                                } else {
                                    newInfo.setMessageId("" + 0);
                                    newInfo.setShowDate(true);
                                }


                                ChatOperatorDatabase.insertChatMessage(this, newInfo);

                                String jid;
                                // 注意：@功能转发实现
                                if (info.getType() == 2) { // 群聊
                                    jid = info.getId();
                                    if (forward.getType() == 1) { // 文本
                                        IMManager.getInstance().sendMessage(newInfo, newInfo.getContent(), jid, true, SettingHelper.getUserId(ChatActivity.this), null);
                                    } else { // 图片
                                        String filePath = forward.getFilePath();
                                        String[] strings = filePath.split("/");

                                        newInfo.setContent(strings[strings.length - 1]); // 为了发送失败重发
                                        IMManager.getInstance().sendMediaMessage(newInfo, strings[strings.length - 1], jid, IMUtil.encodeBase64File(filePath), "2","" + 0, true, SettingHelper.getUserId(this));
                                    }
                                } else {
                                    jid = info.getIconUri() + "@" + IMUtil.getServerName();
                                    if (forward.getType() == 1) { // 文本
                                        IMManager.getInstance().sendMessage(newInfo, newInfo.getContent(), jid);
                                    } else { // 图片
                                        String filePath = forward.getFilePath();
                                        String[] strings = filePath.split("/");

                                        newInfo.setContent(strings[strings.length - 1]); // 为了发送失败重发
                                        IMManager.getInstance().sendMediaMessage(newInfo, strings[strings.length - 1], jid, IMUtil.encodeBase64File(filePath), "2","" + 0, false, SettingHelper.getUserId(this));
                                    }
                                }

                                // 更新消息列表
                                info.setDate("" + date);
                                info.setContent(content);
                                // 其他的都不用设置了

                                ChatOperatorDatabase.updateOrinsertAppMessage(this, info);


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

    private void setSendImageData(String filePath) {
        long date = System.currentTimeMillis();
        int size = mChats.size();
//        String content = mEdit.getText().toString();
        ChatMessageInfo info = new ChatImageOwnInfo();
        info.setState(1);
        info.setChatId(mChatKey);
        info.setContent("[图片]");
        info.setDate("" + date);
        info.setName(SettingHelper.getName(ChatActivity.this));
        info.setUserId(SettingHelper.getUserId(ChatActivity.this));
        if (size > 0)  {
            ChatMessageInfo chat = mChats.get(size - 1);
            int num = Integer.valueOf(chat.getMessageId()) + 1;
            info.setMessageId(num + "");
            long oldDate = Long.valueOf(chat.getDate());
            if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                info.setShowDate(false);
            } else {
                info.setShowDate(true);
            }
        } else {
            info.setMessageId("" + 0);
            info.setShowDate(true);
        }
        info.setFilePath(filePath);

        mChats.add(info);
        mAdapter.notifyDataSetChanged();

//                mListView.setSelection(size); // 这里是size,不是size - 1,因为之前没有添加到mChats里面
        mListView.smoothScrollToPosition(size + 1);
        mEdit.setText("");

        ChatOperatorDatabase.insertChatMessage(ChatActivity.this, info);
        ///////////////////// 发送消息放到sendImage方法中实现  ////////////////
//        String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.IM_SERVER
//                : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.IM_SERVER;
//        HcLog.D(TAG + " #onClick jid = "+jid);
//        String[] strings = filePath.split("/");
//        IMManager.getInstance().sendMediaMessage(strings[strings.length - 1], jid, IMUtil.encodeBase64File(IMUtil.fileExist(filePath, mChatKey).getAbsolutePath()), "2","" + 0);
        /////////////////// 发送消息  ////////////////////
        // 更新主页的消息
        AppMessageInfo appInfo = new AppMessageInfo();
        appInfo.setDate("" + date);
        appInfo.setContent("[图片]");
        if (mMessageInfo != null) {
            appInfo.setIconUri(mMessageInfo.getIconUri());
            appInfo.setId(mMessageInfo.getId());
            appInfo.setTitle(mMessageInfo.getTitle());
            appInfo.setType(mMessageInfo.getType());
        } else { // 说明肯定是单聊
            appInfo.setIconUri(mEmp.getUserId());
            appInfo.setId(mChatKey);
            appInfo.setTitle(mEmp.getName());
            appInfo.setType(3);
        }
        ChatOperatorDatabase.updateOrinsertAppMessage(ChatActivity.this, appInfo);
    }

    public void sendImage(String filePath, ChatMessageInfo info) {
        String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.getServerName()
                : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.getServerName();
        HcLog.D(TAG + " #onClick jid = "+jid);
        String[] strings = filePath.split("/");
        info.setState(2);
        info.setContent(strings[strings.length - 1]); // 为了发送失败重发
        mAdapter.notifyDataSetChanged();
        ChatOperatorDatabase.updateChatMessageContent(this, info);
        IMManager.getInstance().sendMediaMessage(info, strings[strings.length - 1], jid, IMUtil.encodeBase64File(filePath), "2","" + 0, mGroup, SettingHelper.getUserId(this));

    }


    /*****************键盘的一些处理****************/

    /**
     * 是否显示软件盘
     * @return
     */
    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    /**
     * 获取软件盘的高度
     * @return
     */
    private int getSupportSoftInputHeight() {

        Rect r = new Rect();
        /**
         * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
         * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
         */
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        //获取屏幕的高度
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        //计算软件盘的高度
        int softInputHeight = screenHeight - r.bottom;

        HcLog.D(TAG + " #getSupportSoftInputHeight softInputHeight = "+softInputHeight + " screenHeight = "+screenHeight + " r.bottom = "+r.bottom);
        if (softInputHeight <= 0) return softInputHeight;
        /**
         * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
         * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
         * 我们需要减去底部虚拟按键栏的高度（如果有的话）
         */
        if (Build.VERSION.SDK_INT >= 20) { // 这里注意,经过测试好像不用处理的
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        }

        HcLog.D(TAG + " #getSupportSoftInputHeight 经过底部虚拟键盘处理后 softInputHeight = "+softInputHeight);

        if (softInputHeight < 0) {
            HcLog.D("EmotionKeyboard--Warning: value of softInputHeight is below zero!");
        }
        //存一份到本地
        if (softInputHeight > 0) {
            // 这里先不做处理,放到onLayoutChange方法里面

//            SettingHelper.setKeyboradHeight(this, softInputHeight);
//            // 设置高度
//            ViewGroup.LayoutParams params = mOperatorParent.getLayoutParams();
//            params.height = softInputHeight;
//            mOperatorParent.setLayoutParams(params);
        }
        return softInputHeight;
    }


    /**
     * 底部虚拟按键栏的高度
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    /**
     * 锁定内容高度，防止跳闪
     */
    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentParent.getLayoutParams();
        HcLog.D(TAG + "#lockContentHeight mContentParent height = "+mContentParent.getHeight());
        params.height = mContentParent.getHeight();
        params.weight = 0.0F;
    }

    /**
     * 释放被锁定的内容高度
     */
    private void unlockContentHeightDelayed() {
        mEdit.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout.LayoutParams) mContentParent.getLayoutParams()).weight = 1.0F;
            }
        }, 200L);
    }

    private BadgeInfo mBadgeInfo;

    @Override
    public void onReceiver(final ChatMessageInfo chatInfo, AppMessageInfo appInfo) {
        if (chatInfo == null) { // 接收到了系统消息或者其他聊天对象的消息
            // 增加IM角标
            if (mBadgeInfo == null) {
                String appId = IMSettings.getIMAppId(this);
                mBadgeInfo = BadgeCache.getInstance().getBadgeInfo(appId, appId + "_IM");
                if (mBadgeInfo != null)
                    mBadgeInfo.addCount(1);
            } else {
                mBadgeInfo.addCount(1);
            }

        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mChats.add(chatInfo);
                    mAdapter.notifyDataSetChanged();
                    mListView.smoothScrollToPosition(mChats.size() + 1);
                }
            });
        }

    }

    @Override
    public void onResponse(ChatMessageInfo info, IMManager.Result result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 重新发送消息
     * @param info
     * @param group
     */
    public void reSendMessage(ChatMessageInfo info, boolean group) {
        info.setState(2);
        ChatOperatorDatabase.updateChatMessage(this, info);
        mAdapter.notifyDataSetChanged();
        String jid = mEmp != null ? mEmp.getUserId() + "@" + IMUtil.getServerName()
                : mGroup ? mMessageInfo.getId() : mMessageInfo.getIconUri() + "@" + IMUtil.getServerName();
        IMManager.getInstance().reSendMessage(info, jid, group);
    }

    private class JoinTask implements Runnable {

        private String mJid;

        private String mNickname;

        public JoinTask(String jid, String nickName) {
            mJid = jid;
            mNickname = nickName;
        }

        @Override
        public void run() {
            IMManager.getInstance().joinRoom(mJid, mNickname, true);
        }
    }

    private void startSelectActivity() {
        ChatGroupMessageInfo info = ChatOperatorDatabase.getChatGroupInfo(this, mMessageInfo.getId());
        ArrayList<ItemInfo> infos = null;
        ItemInfo itemInfo = null;
        if (info != null) {
            String memberList = info.getGroupMembers();
            if (!TextUtils.isEmpty(memberList)) {
                String userId = SettingHelper.getUserId(this);
                String[] members = memberList.split(";");
                String memberId = null;
                for (String member : members) {
                	memberId = member.substring(0, member.indexOf(":"));
                    if (!userId.equals(memberId)) {
                        if (infos == null) {
                            infos = new ArrayList<ItemInfo>();
                        }
                        itemInfo = new StaffInfo();
                        itemInfo.setUserId(memberId);
                        itemInfo.setItemValue(ChatOperatorDatabase.getNameByUserId(this, memberId));
                        itemInfo.setIconUrl(HcUtil.getHeaderUri(memberId));
                        infos.add(itemInfo);
                    }
                }
            }
        }


        if (infos != null) {
            Intent intent = new Intent(this, IMDeletePersonnelActivity.class);
            intent.putExtra("items", infos);
            intent.putExtra("title", "人员选择");
            intent.putExtra("select", false);
            startActivityForResult(intent, HcChooseHomeView.REQUEST_CODE);
        } else {
            // 删除@
            HcUtil.showToast(this, "没有群成员!");
            Editable editable = mEdit.getText();
            mEdit.setText(editable.delete(editable.length() -1, editable.length()));
        }

    }

    public void showListDialog(CharSequence[] items, final ChatMessageInfo info) {

        final int type = info.getType();
        final String content = info.getContent();
        final String messageId = info.getMessageId();
        final String chatId = info.getChatId();
        HcLog.D(TAG + " #showListDialog data type = "+type + " content = "+content + " chatId = "+chatId + " messageId = "+messageId);

        ListDialog.showListDialog(this, items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HcLog.D(TAG + " #showListDialog#DialogInterface click button = "+which);
                switch (type) {
                    case 1: // 文本
                        switch (which) {
                            case 0: // 复制
                                copyText(content);
                                break;
                            case 1: // 转发
                                forwardMessage(info);
                                break;
                            case 2: // 删除
                                deleteChatMessage(chatId, messageId, info);
                                break;
                            case 3: // 任务
                                createTask(content);
                                break;

                            default:
                                break;
                        }
                        break;
                    case 2: // 图片
                        switch (which) {
                            case 0: // 转发
                                forwardMessage(info);
                                break;
                            case 1: // 删除
                                deleteChatMessage(chatId, messageId, info);
                                break;

                            default:
                                break;
                        }
                        break;
                    case 3: // 语音
                        deleteChatMessage(chatId, messageId, info);
                        break;

                    default:
                        break;
                }

                ListDialog.deleteListDialog();
            }
        });
    }


    private void deleteChatMessage(String chatId, String chatMessageId, ChatMessageInfo chatInfo) {
        // 删除当前的一条聊天记录
        // 更新首页的消息列表的显示
        mChats.remove(chatInfo);
        ChatOperatorDatabase.deleteChatMessage(this, chatId, chatMessageId);
        mAdapter.notifyDataSetChanged();
        ChatMessageInfo info = ChatOperatorDatabase.getLastMessage(this, chatId);
        if (info == null) { // 没有聊天记录
            ChatOperatorDatabase.deleteAppMessage(this, mChatKey);
        } else {
            String content = info.getContent();
            switch (info.getType()) {
                case 2:
                    content = "[图片]";
                    break;
                case 3:
                    content = "[语音]";
                    break;

                default:
                    break;
            }
            AppMessageInfo messageInfo = ChatOperatorDatabase.getAppMessageInfo(this, mChatKey);
            messageInfo.setContent(content);
            messageInfo.setDate(info.getDate());
            messageInfo.setCount(0);
            ChatOperatorDatabase.updateAppMessage(this, messageInfo);
        }
    }

    private void copyText(String content) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(content);
    }

    private void createTask(String content) {
        String userId =  mGroup ? null : mEmp != null ? mEmp.getUserId() : mMessageInfo.getIconUri();
        ModuleBridge.startTaskActivity(ChatActivity.this, userId, content);
    }

    /************************************ 转发 ***************************/

    private static final int REQUEST_CODE_FORWARD = 1;

    private ChatMessageInfo mForward;

    private void forwardMessage(ChatMessageInfo forward) {
        mForward = forward;
        Intent intent = new Intent(this, IMForwardActivity.class);
//        intent.putExtra("chatId", chatId);
//        intent.putExtra("chatMessageId", chatMessageId);
        startActivityForResult(intent, REQUEST_CODE_FORWARD);
    }
}
