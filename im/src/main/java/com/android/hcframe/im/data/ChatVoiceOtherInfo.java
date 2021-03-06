package com.android.hcframe.im.data;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.im.ChatActivity;
import com.android.hcframe.im.IMUtil;
import com.android.hcframe.im.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-23 14:12.
 */

public class ChatVoiceOtherInfo extends ChatMessageInfo {

    private int mDuration;

    private String mFilePath;
    /** 录音是否已读 0:未读取, 1:已经读取 */
    private boolean mIsReaded;

    private boolean mSpeeking;

    public ChatVoiceOtherInfo() {
        super();
        mType = 3;
        mIsOwn = false;
    }

    @Override
    public ViewHolderBase<ChatMessageInfo> createViewHolder(final boolean group) {
        return new ViewHolderBase<ChatMessageInfo>() {

            private TextView mDate;

            private TextView mContent; // voice

            private ImageView mIcon;

            private View mDivider;

            private ImageView mSpeeking;

            private LinearLayout mParent;

            private TextView mName;

            private TextView mMark;

            @Override
            public View createView(LayoutInflater inflater) {
                View item = inflater.inflate(R.layout.im_chatting_voice_item_other, null);
                mDate = (TextView) item.findViewById(R.id.chat_voice_item_other_date);
                mName = (TextView) item.findViewById(R.id.chat_voice_item_other_name);
                mContent = (TextView) item.findViewById(R.id.chat_voice_item_other_second);
                mIcon = (ImageView) item.findViewById(R.id.chat_voice_item_other_icon);
                mDivider = item.findViewById(R.id.chat_voice_item_other_divider);
                mSpeeking = (ImageView) item.findViewById(R.id.chat_voice_item_other_content);
                mParent = (LinearLayout) item.findViewById(R.id.chat_voice_item_other_content_parent);
                mMark = (TextView) item.findViewById(R.id.chat_voice_item_other_mark);
                return item;
            }

            @Override
            public void setItemData(final int position, final ChatMessageInfo data) {
                if (data.isShowDate()) {
                    if (mDivider.getVisibility() != View.GONE)
                        mDivider.setVisibility(View.GONE);
                    if (mDate.getVisibility() != View.VISIBLE)
                        mDate.setVisibility(View.VISIBLE);
                    String formatDate = data.getFormatDate();
                    if (TextUtils.isEmpty(formatDate)) {
                        formatDate = IMUtil.getChatDate(HcApplication.getContext(), Long.valueOf(data.getDate()));
                        data.setFormatDate(formatDate);
                    }
                    mDate.setText(formatDate);
                } else {
                    if (mDate.getVisibility() != View.GONE)
                        mDate.setVisibility(View.GONE);
                    if (mDivider.getVisibility() != View.VISIBLE)
                        mDivider.setVisibility(View.VISIBLE);
                }
                if (group) {
                    mName.setVisibility(View.VISIBLE);
                    mName.setText(data.getName());
                }
                if (data.isReaded()) {
                    if (mMark.getVisibility() != View.GONE)
                        mMark.setVisibility(View.GONE);
                } else {
                    if (mMark.getVisibility() != View.VISIBLE)
                        mMark.setVisibility(View.VISIBLE);
                }
                setLayout(mParent, data.getDuration());
                mContent.setText(data.getDuration() + "\"");
                ImageLoader.getInstance().displayImage(HcUtil.getHeaderUri(data.getUserId()), mIcon, HcUtil.getAccountImageOptions());
                mParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HcLog.D("ChatVoiceOtherInfo #setItemData context = "+v.getContext());
                        if (!data.isReaded()) {
                            data.setReaded(true);
                            mMark.setVisibility(View.GONE);
                            // 更改数据库
                            ChatOperatorDatabase.updateChatMessage(v.getContext(), data);
                        }
                        if (v.getContext() instanceof ChatActivity) {
                            ChatActivity activity = (ChatActivity) v.getContext();
                            if (!data.isSpeeking()) {
                                activity.startPalyer(position, data, mSpeeking);
                            } else {
                                activity.stopPlayer(position, data, mSpeeking);
                            }
                        }
                    }
                });

                Drawable src = mSpeeking.getDrawable();
                AnimationDrawable an;
                if (data.isSpeeking()) {
                    if (src instanceof AnimationDrawable) {
                        an = (AnimationDrawable) src;
                        if (!an.isRunning()) {
                            an.start();
                        }
                    } else {
                        mSpeeking.setImageResource(R.drawable.im_voice_speeking_other);
                        an = (AnimationDrawable) mSpeeking.getDrawable();
                        an.start();
                    }

                } else {
                    if (src instanceof AnimationDrawable) {
                        an = (AnimationDrawable) src;
                        if (an.isRunning()) {
                            an.stop();
                        }
                        mSpeeking.setImageResource(R.drawable.im_voice_speek_other_3);
                    }
                }

                mParent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (v.getContext() instanceof ChatActivity) {
                            ChatActivity activity = (ChatActivity) v.getContext();
                            activity.showListDialog(new String[] {"删除"}, data);
                        }
                        return true;
                    }
                });
            }
        };
    }

    public boolean isReaded() {
        return mIsReaded;
    }

    public void setReaded(boolean readed) {
        mIsReaded = readed;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int second) {
        mDuration = second;
    }

    private void setLayout(View view, int second) {
        int width = 120;

        if (second < 5) {
            width = 200;
        } else if (second < 10) {
            width = 220;
        } else if (second < 20) {
            width = 240;
        } else if (second < 30) {
            width = 260;
        } else if (second < 40) {
            width = 280;
        } else if (second < 50) {
            width = 300;
        } else if (second <= 60) {
            width = 320;
        } else { // 不可能出现
            width = 340;
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        view.setLayoutParams(params);
    }

    public boolean isSpeeking() {
        return mSpeeking;
    }

    public void setSpeeking(boolean speeking) {
        mSpeeking = speeking;
    }

    @Override
    public int getState() {
        return 0;
    }
}
