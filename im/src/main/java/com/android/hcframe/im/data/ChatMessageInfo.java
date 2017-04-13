package com.android.hcframe.im.data;

import com.android.hcframe.adapter.ViewHolderBase;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-8 15:48.
 */
public abstract class ChatMessageInfo {
    /**
     * 发送消息的人的名字,群聊的时候有用
     */
    private String mName;

    /**
     * 消息的帐号的ID,可能是自己,也可能是其他人
     */
    private String mUserId;
    /**
     * 消息的发布日期
     */
    private String mDate;
    /**
     * 消息的内容,根据不同的类型,为不同的内容
     */
    private String mContent;

    /**
     * 是否显示日期时间,存在数据库里为0:显示;1:不显示
     */
    private boolean mShowDate = true;
    /**
     * 单聊时为双方userId合并的MD5值/群聊时为群的ID
     */
    private String mChatId;

    /**
     * 消息内容类型 1.文本
     * 2.图片
     * 3.语音
     * 4.文件
     * 5.其他
     */
    protected int mType;
    /**
     * 消息是否为本人,虽然可以根据userId判断,0:本人；1:其他人
     */
    protected boolean mIsOwn;

    /** 当前消息的ID,有可能是一个自增长的字段 */
    private String mMessageId;

    /** 日期的格式化形式,不保存在数据库中,临时使用.
     *  这里会有个小bug,要是停留在聊天界面超过24:00,之前显示的时间会有误差,但这种情况出现的概率很小 */
    private String mFormatDate;

    /** 发送给对方的消息的状态 0:已发送;1:未发送2:正在发送;;3:发送失败 */
    private int mState;

    /** 群消息时,@的人员的userId,多人用";"隔开 */
    private String mReceiver;

    public String getContent() {
        if (mContent == null) return "";
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public boolean isShowDate() {
        return mShowDate;
    }

    public void setShowDate(boolean showDate) {
        mShowDate = showDate;
    }

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String chatId) {
        mChatId = chatId;
    }

    public final int getType() {
        return mType;
    }

    public final boolean isOwn() {
        return mIsOwn;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public abstract ViewHolderBase<ChatMessageInfo> createViewHolder(boolean group);

    public String getFormatDate() {
        return mFormatDate;
    }

    public void setFormatDate(String formatDate) {
        mFormatDate = formatDate;
    }

    public String getFilePath() {
        return "";
//        throw new UnsupportedOperationException("ChatMessageInfo#getFilePath!!!!");
    }

    /**
     * 文件的绝对路径,包括后缀名.
     * @param filePath
     */
    public void setFilePath(String filePath) {
        ;
//        throw new UnsupportedOperationException("ChatMessageInfo#setFilePath!!!! filePath = "+filePath);
    }

    public int getDuration() {
        return 0;
//        throw new UnsupportedOperationException("ChatMessageInfo#getSecond!!!!");
    }

    public void setDuration(int duration) {
        ;
//        throw new UnsupportedOperationException("ChatMessageInfo#setSecond!!!! second = "+second);
    }

    public boolean isReaded() {
        return true;
    }

    public void setReaded(boolean readed) {
        ;
    }

    public boolean isSpeeking() {
        return false;
    }

    public void setSpeeking(boolean speeking) {
        ;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public String getReceiver() {
        return mReceiver;
    }

    public void setReceiver(String receiver) {
        mReceiver = receiver;
    }
}
