package com.android.hcframe.im.data;

/**
 * Created by pc on 2016/10/11.
 */

import android.os.Parcel;

import com.android.hcframe.im.R;

/**
 * 讨论组信息
 */
public class ChatGroupMessageInfo extends AppMessageInfo {

    /**
     * 讨论组中显示的人数
     */
    private int mCount;

    /**
     * (组成员用;隔开,userId:nickname;userId:nickname)
     */
    private String mGroupMembers;

    /**
     * 消息免打扰
     * 保存的时候0=true */
    private boolean mNoticed ;

    public ChatGroupMessageInfo() {
        super();
    }

    public ChatGroupMessageInfo(Parcel p) {
        super(p);
        mCount = p.readInt();
        mGroupMembers = p.readString();
        mNoticed = p.readInt() == 0;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public String getGroupMembers() {
        return mGroupMembers;
    }

    public void setGroupMembers(String groupMembers) {
        mGroupMembers = groupMembers;
    }

    public boolean isNoticed() {
        return mNoticed;
    }

    public void setNoticed(boolean noticed) {
        mNoticed = noticed;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mCount);
        dest.writeString(mGroupMembers);
        dest.writeInt(mNoticed ? 0 : 1);
    }

    public static final Creator<ChatGroupMessageInfo> CREATOR = new Creator<ChatGroupMessageInfo>() {
        @Override
        public ChatGroupMessageInfo createFromParcel(Parcel in) {
            return new ChatGroupMessageInfo(in);
        }

        @Override
        public ChatGroupMessageInfo[] newArray(int size) {
            return new ChatGroupMessageInfo[size];
        }
    };

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public String getIconUri() {
        return "drawable://" + R.drawable.im_chat_group_icon;
    }

    @Override
    public String getDate() {
        return "";
    }

    @Override
    public String getContent() {
        return "";
    }

    @Override
    public void setType(int type) {
        ;
    }
}
