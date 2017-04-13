package com.android.hcframe.im.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.hcframe.HcLog;
import com.android.hcframe.im.ChatActivity;
import com.android.hcframe.sql.HcDatabase;
import com.android.hcframe.sql.HcDatabase.ChatGroup;
import com.android.hcframe.sql.HcDatabase.AppMessage;
import com.android.hcframe.sql.HcDatabase.ChatMessage;
import com.android.hcframe.sql.HcProvider;
import com.android.hcframe.sql.SettingHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-9 15:16.
 */
public final class ChatOperatorDatabase {

    private static final String TAG = "ChatOperatorDatabase";

    /**
     * 把消息保存到数据库中
     *
     * @param context
     * @param infos   需要保存的数据
     * @return
     */
    public static int insertAppMessages(Context context, List<AppMessageInfo> infos) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_MESSAGE, where, null);

        int size = infos.size();
        if (size > 0) {
            ContentValues[] values = new ContentValues[size];
            for (int i = 0; i < size; i++) {
                values[i] = new ContentValues();
                setMessageValues(values[i], infos.get(i), userId);
            }
            int num = cr.bulkInsert(HcProvider.CONTENT_URI_MESSAGE, values);
            return num;
        }

        return 0;
    }

    private static void setMessageValues(ContentValues values, AppMessageInfo info, String userId) {
        values.clear();
        values.put(AppMessage.USER_ID, userId);
        values.put(AppMessage.MESSAGE_CONTENT, info.getContent());
        values.put(AppMessage.MESSAGE_DATE, info.getDate());
        values.put(AppMessage.MESSAGE_ID, info.getId());
        values.put(AppMessage.MESSAGE_TITLE, info.getTitle());
        values.put(AppMessage.MESSAGE_TYPE, info.getType());
        values.put(AppMessage.MESSAGE_ICON, info.getIconUri());
        values.put(AppMessage.MESSAGE_COUNT, info.getCount());
    }

    /**
     * 保存一条数据到数据库中
     *
     * @param context
     * @param info    需要保存的数据
     * @return
     */
    public static Uri insertAppMessage(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_MESSAGE, where, null);
        ContentValues values = new ContentValues();
        setMessageValues(values, info, userId);
        return cr.insert(HcProvider.CONTENT_URI_MESSAGE, values);

    }

    /**
     * 删除一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int deleteAppMessage(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_MESSAGE, where, null);
    }

    /**
     * 更新一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateAppMessage(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(AppMessage.MESSAGE_CONTENT, info.getContent());
        values.put(AppMessage.MESSAGE_DATE, info.getDate());
        values.put(AppMessage.MESSAGE_COUNT, info.getCount());
//        values.put(AppMessage.MESSAGE_TYPE, info.getType());
        return cr.update(HcProvider.CONTENT_URI_MESSAGE, values, where, null);
    }

    /**
     * @param context
     * @param info
     */
    public static void updateOrinsertAppMessage(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        Cursor c = cr.query(HcProvider.CONTENT_URI_MESSAGE, null, where, null, null);
        HcLog.D(TAG + " #updateOrinsertAppMessage info id = " + info.getId());
        if (c != null) {
            int size = c.getCount();
            int count = 0;
            if (size > 0 && info.getCount() > 0) { // 说明需要更新数量
                c.moveToFirst();
                count = c.getInt(c.getColumnIndex(AppMessage.MESSAGE_COUNT));

            }
            c.close();
            if (size > 0) {
                // update
                values.put(AppMessage.MESSAGE_CONTENT, info.getContent());
                values.put(AppMessage.MESSAGE_DATE, info.getDate());
                values.put(AppMessage.MESSAGE_TITLE, info.getTitle());
                values.put(AppMessage.MESSAGE_COUNT, count + info.getCount());
                cr.update(HcProvider.CONTENT_URI_MESSAGE, values, where, null);
            } else {
                // insert
                setMessageValues(values, info, userId);
                cr.insert(HcProvider.CONTENT_URI_MESSAGE, values);
            }
        } else {
            // insert
            insertAppMessage(context, info);
        }
    }

    /**
     * 获取消息数据
     *
     * @param context
     * @return
     */
    public static List<AppMessageInfo> getAppMessages(Context context) {
        String userId = SettingHelper.getUserId(context);
        String[] projection = {AppMessage.MESSAGE_ID, AppMessage.MESSAGE_CONTENT, AppMessage.MESSAGE_TYPE
                , AppMessage.MESSAGE_TITLE, AppMessage.MESSAGE_DATE, AppMessage.MESSAGE_ICON,
                AppMessage.MESSAGE_COUNT};
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" + " AND " + AppMessage.MESSAGE_TYPE + "!=1";
        List<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_MESSAGE, projection, where, null, /*AppMessage.MESSAGE_TYPE + " DESC, " + */AppMessage.MESSAGE_DATE + " DESC"); // 不同时进行按类型排序了
        if (c != null && c.getCount() > 0) {
            AppMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new AppMessageInfo();
                int type = c.getInt(2);
                info.setContent(c.getString(1));
                info.setDate(c.getString(4));
                info.setId(c.getString(0));
                info.setTitle(c.getString(3));
                info.setType(type);
                info.setIconUri(c.getString(5));
                info.setCount(c.getInt(6));
                if (type != 4)
                    infos.add(info);
                else {
                    infos.add(0, info);
                }
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    /*****************  消息聊天的存储  *******************/

    /**
     * 保存聊天记录
     *
     * @param context
     * @param infos
     * @param chatId
     * @return
     */
    public static int insertChatMessages(Context context, List<ChatMessageInfo> infos, String chatId) {
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_CHAT, where, null);

        int size = infos.size();
        if (size > 0) {
            ContentValues[] values = new ContentValues[size];
            for (int i = 0; i < size; i++) {
                values[i] = new ContentValues();
                setChatValues(values[i], infos.get(i), chatId);
            }
            int num = cr.bulkInsert(HcProvider.CONTENT_URI_CHAT, values);
            return num;
        }

        return 0;
    }

    private static void setChatValues(ContentValues values, ChatMessageInfo info, String chatId) {
        values.clear();
        values.put(ChatMessage.Chat_ID, chatId);
        values.put(ChatMessage.USER_ID, info.getUserId());
        values.put(ChatMessage.CHAT_CONTENT, info.getContent());
        values.put(ChatMessage.CHAT_DATE, info.getDate());
        values.put(ChatMessage.CHAT_NAME, info.getName());
        values.put(ChatMessage.CHAT_TYPE, info.getType());
        values.put(ChatMessage.CHAT_OWN, info.isOwn() ? 0 : 1);
        values.put(ChatMessage.CHAT_MESSAGE_ID, info.getMessageId());
        values.put(ChatMessage.CHAT_SHOW_TIME, info.isShowDate() ? 0 : 1);
        values.put(ChatMessage.CHAT_FILE_NAME, info.getFilePath());
        values.put(ChatMessage.CHAT_VOICE_DURATION, info.getDuration());
        values.put(ChatMessage.CHAT_VOICE_READED, info.isReaded() ? 1 : 0);
        values.put(ChatMessage.CHAT_SEND_STATE, info.getState());
        values.put(ChatMessage.CHAT_RECEIVER, info.getReceiver());
    }

    /**
     * 保存一条聊天记录
     *
     * @param context
     * @param info
     * @return
     */
    public static Uri insertChatMessage(Context context, ChatMessageInfo info) {
        String where = ChatMessage.Chat_ID + "=" + "'" + info.getChatId() + "'" +
                " AND " + ChatMessage.CHAT_MESSAGE_ID + "=" + "'" + info.getMessageId() + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_CHAT, where, null);
        ContentValues values = new ContentValues();
        setChatValues(values, info, info.getChatId());
        HcLog.D(TAG + "#insertChatMessage id = " + info.getChatId());
        return cr.insert(HcProvider.CONTENT_URI_CHAT, values);

    }

    /**
     * 删除一条聊天记录
     *
     * @param context
     * @param info
     * @return
     */
    public static int deleteChatMessage(Context context, ChatMessageInfo info) {
        String where = ChatMessage.Chat_ID + "=" + "'" + info.getChatId() + "'" +
                " AND " + ChatMessage.CHAT_MESSAGE_ID + "=" + "'" + info.getMessageId() + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_CHAT, where, null);
    }

    /**
     * 获取全部的聊天记录
     *
     * @param context
     * @param chatId
     * @return
     */
    public static List<ChatMessageInfo> getChatMessages(Context context, String chatId) {
        String[] projection = {ChatMessage.Chat_ID, ChatMessage.CHAT_MESSAGE_ID, ChatMessage.USER_ID,
                ChatMessage.CHAT_OWN, ChatMessage.CHAT_CONTENT, ChatMessage.CHAT_DATE,
                ChatMessage.CHAT_NAME, ChatMessage.CHAT_TYPE, ChatMessage.CHAT_SHOW_TIME,
                ChatMessage.CHAT_FILE_NAME, ChatMessage.CHAT_VOICE_DURATION, ChatMessage.CHAT_VOICE_READED,
                ChatMessage.CHAT_SEND_STATE, ChatMessage.CHAT_RECEIVER};
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'";
        List<ChatMessageInfo> infos = new ArrayList<ChatMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CHAT, projection, where, null, null);
        if (c != null && c.getCount() > 0) {
            ChatMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                int type = c.getInt(7);
                int own = c.getInt(3);
                info = getChatInfo((own << 4) | type);
                info.setContent(c.getString(4));
                info.setDate(c.getString(5));
                info.setName(c.getString(6));
                info.setChatId(c.getString(0));
                info.setMessageId(c.getString(1));
                info.setUserId(c.getString(2));
                info.setShowDate(c.getInt(8) == 0);
                info.setFilePath(c.getString(9));
                info.setDuration(c.getInt(10));
                info.setReaded(c.getInt(11) == 1);
                info.setState(c.getInt(12));
                info.setReceiver(c.getString(13));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    private static ChatMessageInfo getChatInfo(int type) {
        switch (type) {
            case ChatActivity.CHAT_TEXT:
                return new ChatTextOwnInfo();
            case ChatActivity.CHAT_IMAGE:
                return new ChatImageOwnInfo();
            case ChatActivity.CHAT_FILE:
                break;
            case ChatActivity.CHAT_OTHER:
                break;
            case ChatActivity.CHAT_VOICE:
                return new ChatVoiceOwnInfo();
            case (1 << 4) | ChatActivity.CHAT_TEXT:
                return new ChatTextOtherInfo();
            case (1 << 4) | ChatActivity.CHAT_IMAGE:
                return new ChatImageOtherInfo();
            case (1 << 4) | ChatActivity.CHAT_FILE:
                break;
            case (1 << 4) | ChatActivity.CHAT_OTHER:
                break;
            case (1 << 4) | ChatActivity.CHAT_VOICE:
                return new ChatVoiceOtherInfo();

            default:
                break;
        }

        return new ChatTextOwnInfo();
    }

    /**
     * 分页获取聊天记录
     *
     * @param context
     * @param chatId
     * @return
     */
    public static List<ChatMessageInfo> getChatMessages(Context context, String chatId, String date, int pageSize) {
        String[] projection = {ChatMessage.Chat_ID, ChatMessage.CHAT_MESSAGE_ID, ChatMessage.USER_ID,
                ChatMessage.CHAT_OWN, ChatMessage.CHAT_CONTENT, ChatMessage.CHAT_DATE,
                ChatMessage.CHAT_NAME, ChatMessage.CHAT_TYPE, ChatMessage.CHAT_SHOW_TIME,
                ChatMessage.CHAT_FILE_NAME, ChatMessage.CHAT_VOICE_DURATION, ChatMessage.CHAT_VOICE_READED,
                ChatMessage.CHAT_SEND_STATE, ChatMessage.CHAT_RECEIVER};
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'" + " AND " + ChatMessage.CHAT_DATE + "<" + date;
        List<ChatMessageInfo> infos = new ArrayList<ChatMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CHAT, projection, where, null, ChatMessage._ID + " DESC LIMIT " + pageSize + " OFFSET 0");
        if (c != null && c.getCount() > 0) {
            ChatMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                int type = c.getInt(7);
                int own = c.getInt(3);
                info = getChatInfo((own << 4) | type);
                info.setContent(c.getString(4));
                info.setDate(c.getString(5));
                info.setName(c.getString(6));
                info.setChatId(c.getString(0));
                info.setMessageId(c.getString(1));
                info.setUserId(c.getString(2));
                info.setShowDate(c.getInt(8) == 0);
                info.setFilePath(c.getString(9));
                info.setDuration(c.getInt(10));
                info.setReaded(c.getInt(11) == 1);
                info.setState(c.getInt(12));
                info.setReceiver(c.getString(13));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    /**
     * 更新一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateChatMessage(Context context, ChatMessageInfo info) {
        String where = ChatMessage.Chat_ID + "=" + "'" + info.getChatId() + "'" +
                " AND " + ChatMessage.CHAT_MESSAGE_ID + "=" + "'" + info.getMessageId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ChatMessage.CHAT_VOICE_READED, 1);
        values.put(ChatMessage.CHAT_SEND_STATE, info.getState());
        return cr.update(HcProvider.CONTENT_URI_CHAT, values, where, null);
    }

    /**
     * 保存一条群组数据到数据库中
     *
     * @param context
     * @param info    需要保存的数据
     * @return
     */
    public static Uri insertChatGroup(Context context, ChatGroupMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" +
                " AND " + ChatGroup.GROUP_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_GROUP, where, null);
        ContentValues values = new ContentValues();
        setChatGroupValues(values, info, userId);
        return cr.insert(HcProvider.CONTENT_URI_GROUP, values);

    }

    private static void setChatGroupValues(ContentValues values, ChatGroupMessageInfo info, String userId) {
        values.clear();
        values.put(ChatGroup.USER_ID, userId);
        values.put(ChatGroup.GROUP_COUNT, info.getCount());
        values.put(ChatGroup.GROUP_MEMBERS, info.getGroupMembers());
        values.put(ChatGroup.GROUP_ID, info.getId());
        values.put(ChatGroup.GROUP_NAME, info.getTitle());
        values.put(ChatGroup.GROUP_NOTICED, info.isNoticed() ? 0 : 1);
        values.put(ChatGroup.GROUP_ICON, info.getIconUri());
    }

    /**
     * 更新一条群组数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateChatGroup(Context context, ChatGroupMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" +
                " AND " + ChatGroup.GROUP_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ChatGroup.GROUP_NOTICED, info.isNoticed() ? 0 : 1);
        values.put(ChatGroup.GROUP_NAME, info.getTitle());
        values.put(ChatGroup.GROUP_MEMBERS, info.getGroupMembers());
        values.put(ChatGroup.GROUP_COUNT, info.getCount());
        return cr.update(HcProvider.CONTENT_URI_GROUP, values, where, null);
    }

    /**
     * 获取群组数据
     *
     * @param context
     * @return
     */
    public static List<ChatGroupMessageInfo> getChatGroups(Context context) {
        String userId = SettingHelper.getUserId(context);
        String[] projection = {ChatGroup.GROUP_ID, ChatGroup.GROUP_COUNT, ChatGroup.GROUP_ICON
                , ChatGroup.GROUP_NOTICED, ChatGroup.GROUP_NAME, ChatGroup.GROUP_MEMBERS};
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'";
        List<ChatGroupMessageInfo> infos = new ArrayList<ChatGroupMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_GROUP, projection, where, null, ChatGroup._ID + " DESC");
        if (c != null && c.getCount() > 0) {
            ChatGroupMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new ChatGroupMessageInfo();
                info.setId(c.getString(0));
                info.setCount(c.getInt(1));
                info.setIconUri(c.getString(2));
                info.setNoticed(c.getInt(3) == 0);
                info.setTitle(c.getString(4));
                info.setGroupMembers(c.getString(5));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    /**
     * 删除一条群组数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int deleteChatGroup(Context context, ChatGroupMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" +
                " AND " + ChatGroup.GROUP_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_GROUP, where, null);
    }

    /**
     * 把群组列表保存到数据库中
     *
     * @param context
     * @param infos   需要保存的数据
     * @return
     */
    public static int insertChatGroups(Context context, List<ChatGroupMessageInfo> infos) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_GROUP, where, null);

        int size = infos.size();
        if (size > 0) {
            ContentValues[] values = new ContentValues[size];
            for (int i = 0; i < size; i++) {
                values[i] = new ContentValues();
                setChatGroupValues(values[i], infos.get(i), userId);
            }
            int num = cr.bulkInsert(HcProvider.CONTENT_URI_GROUP, values);
            return num;
        }

        return 0;
    }

    /**
     * 获取聊天对象的消息条数
     *
     * @param context
     * @param chatId
     * @return
     */
    public static int getChatCount(Context context, String chatId) {
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CHAT, null, where, null, null);
        int count = 0;
        if (c != null) {
            count = c.getCount();
            c.close();
        }
        return count;
    }

    public static ChatMessageInfo getLastMessage(Context context, String chatId) {
        String[] projection = {ChatMessage.Chat_ID, ChatMessage.CHAT_MESSAGE_ID, ChatMessage.USER_ID,
                ChatMessage.CHAT_OWN, ChatMessage.CHAT_CONTENT, ChatMessage.CHAT_DATE,
                ChatMessage.CHAT_NAME, ChatMessage.CHAT_TYPE, ChatMessage.CHAT_SHOW_TIME,
                ChatMessage.CHAT_FILE_NAME, ChatMessage.CHAT_VOICE_DURATION, ChatMessage.CHAT_VOICE_READED,
                ChatMessage.CHAT_SEND_STATE, ChatMessage.CHAT_RECEIVER};
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'";
        ChatMessageInfo info = null;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CHAT, projection, where, null, ChatMessage._ID + " DESC LIMIT " + 1 + " OFFSET 0");
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int type = c.getInt(7);
            int own = c.getInt(3);
            info = getChatInfo((own << 4) | type);
            info.setContent(c.getString(4));
            info.setDate(c.getString(5));
            info.setName(c.getString(6));
            info.setChatId(c.getString(0));
            info.setMessageId(c.getString(1));
            info.setUserId(c.getString(2));
            info.setShowDate(c.getInt(8) == 0);
            info.setFilePath(c.getString(9));
            info.setDuration(c.getInt(10));
            info.setReaded(c.getInt(11) == 1);
            info.setState(c.getInt(12));
            info.setReceiver(c.getString(13));
        }
        if (c != null)
            c.close();
        return info;
    }

    public static String getNameByUserId(Context context, String userId) {
        String name = "";
        String[] projection = {HcDatabase.Contacts.NAME};
        String where = HcDatabase.Contacts.USER_ID + "=" + "'" + userId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CONTACTS, projection, where, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            name = c.getString(0);
        }
        if (c != null)
            c.close();
        return name;
    }

    /**
     * 获取群组一条数据
     *
     * @param context
     * @param groupId
     * @return
     */
    public static ChatGroupMessageInfo getChatGroupInfo(Context context, String groupId) {
        String userId = SettingHelper.getUserId(context);
        String[] projection = {ChatGroup.GROUP_COUNT, ChatGroup.GROUP_ICON
                , ChatGroup.GROUP_NOTICED, ChatGroup.GROUP_NAME, ChatGroup.GROUP_MEMBERS};
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" + " AND " + ChatGroup.GROUP_ID + "=" + "'" + groupId + "'";
        ChatGroupMessageInfo info = null;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_GROUP, projection, where, null, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            info = new ChatGroupMessageInfo();
            info.setCount(c.getInt(0));
            info.setIconUri(c.getString(1));
            info.setNoticed(c.getInt(2) == 0 ? true : false);
            info.setTitle(c.getString(3));
            info.setGroupMembers(c.getString(4));
            info.setId(groupId);
        }
        if (c != null)
            c.close();
        return info;
    }

    /**
     * 删除一条消息数据
     *
     * @param context
     * @param messageId
     * @return
     */
    public static int deleteAppMessage(Context context, String messageId) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + messageId + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_MESSAGE, where, null);
    }

    /**
     * 删除某人的全部聊天记录
     *
     * @param context
     * @param chatId
     * @return
     */
    public static int deleteChatMessages(Context context, String chatId) {
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_CHAT, where, null);
    }

    /**
     * 删除一条群组数据
     *
     * @param context
     * @param roomId
     * @return
     */
    public static int deleteChatGroup(Context context, String roomId) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" +
                " AND " + ChatGroup.GROUP_ID + "=" + "'" + roomId + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_GROUP, where, null);
    }

    /**
     * 更改群聊天信息的群名字
     *
     * @param context
     * @param chatId 群的ID
     * @param name 群的名字
     * @return
     */
    public static int updateGroupChatName(Context context, String chatId, String name) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + chatId + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(AppMessage.MESSAGE_TITLE, name);
        return cr.update(HcProvider.CONTENT_URI_MESSAGE, values, where, null);
    }

    /**
     * 更新一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateChatMessageContent(Context context, ChatMessageInfo info) {
        String where = ChatMessage.Chat_ID + "=" + "'" + info.getChatId() + "'" +
                " AND " + ChatMessage.CHAT_MESSAGE_ID + "=" + "'" + info.getMessageId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(ChatMessage.CHAT_SEND_STATE, info.getState());
        values.put(ChatMessage.CHAT_CONTENT, info.getContent());
        return cr.update(HcProvider.CONTENT_URI_CHAT, values, where, null);
    }

    /**
     * 更新一条消息标题数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateAppMessageTitle(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(AppMessage.MESSAGE_TITLE, info.getTitle());
        return cr.update(HcProvider.CONTENT_URI_MESSAGE, values, where, null);
    }

    /**
     * 根据群的JID获取群的名字
     * @param context
     * @param jid {e.g name@server}
     * @return
     */
    public static String getTitleByJid(Context context, String jid) {
        String userId = SettingHelper.getUserId(context);
        String where = ChatGroup.USER_ID + "=" + "'" + userId + "'" +
                " AND " + ChatGroup.GROUP_ID + "=" + "'" + jid + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_GROUP, null, where, null, null);
        String title = null;
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                title = c.getString(c.getColumnIndex(ChatGroup.GROUP_NAME));
            }
            c.close();
        }
        return title;
    }

    /**
     * 获取一条消息
     * @param context
     * @param id 消息的id
     * @return
     */
    public static AppMessageInfo getAppMessageInfo(Context context, String id) {
        String userId = SettingHelper.getUserId(context);
        String[] projection = {AppMessage.MESSAGE_ID, AppMessage.MESSAGE_CONTENT, AppMessage.MESSAGE_TYPE
                , AppMessage.MESSAGE_TITLE, AppMessage.MESSAGE_DATE, AppMessage.MESSAGE_ICON,
                AppMessage.MESSAGE_COUNT};
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" + " AND " + AppMessage.MESSAGE_ID + "=" + "'" + id + "'";
        AppMessageInfo info = null;
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_MESSAGE, projection, where, null, null);
        if (c != null && c.getCount() > 0) {
            info = new AppMessageInfo();
            c.moveToFirst();
            info.setContent(c.getString(1));
            info.setDate(c.getString(4));
            info.setId(c.getString(0));
            info.setTitle(c.getString(3));
            info.setType(c.getInt(2));
            info.setIconUri(c.getString(5));
            info.setCount(c.getInt(6));
        }
        if (c != null)
            c.close();
        return info;
    }

    /**
     * 获取系统消息数据
     *
     * @param context
     * @return
     */
    public static List<AppMessageInfo> getSystemMessages(Context context) {
        String userId = SettingHelper.getUserId(context);
        String[] projection = {AppMessage.MESSAGE_ID, AppMessage.MESSAGE_CONTENT, AppMessage.MESSAGE_TYPE
                , AppMessage.MESSAGE_TITLE, AppMessage.MESSAGE_DATE, AppMessage.MESSAGE_ICON,
                AppMessage.MESSAGE_COUNT};
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" + " AND " + AppMessage.MESSAGE_TYPE + "=1";
        List<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_MESSAGE, projection, where, null, AppMessage.MESSAGE_DATE + " DESC");
        if (c != null && c.getCount() > 0) {
            AppMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new AppMessageInfo();
                info.setContent(c.getString(1));
                info.setDate(c.getString(4));
                info.setId(c.getString(0));
                info.setTitle(c.getString(3));
                info.setType(c.getInt(2));
                info.setIconUri(c.getString(5));
                info.setCount(c.getInt(6));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    public static void deleteAllSystemMessages(Context context) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" + " AND (" + AppMessage.MESSAGE_TYPE + "=1"
                + " OR " + AppMessage.MESSAGE_TYPE + "=4)";
        final ContentResolver cr = context.getContentResolver();
        cr.delete(HcProvider.CONTENT_URI_MESSAGE, where, null);

    }

    /**
     * 获取消息数据
     *
     * @param context
     * @return
     */
    public static List<AppMessageInfo> getAppMessages(Context context, String selection, String sortOrder) {

        String[] projection = {AppMessage.MESSAGE_ID, AppMessage.MESSAGE_CONTENT, AppMessage.MESSAGE_TYPE
                , AppMessage.MESSAGE_TITLE, AppMessage.MESSAGE_DATE, AppMessage.MESSAGE_ICON,
                AppMessage.MESSAGE_COUNT};
        List<AppMessageInfo> infos = new ArrayList<AppMessageInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_MESSAGE, projection, selection, null, sortOrder);
        if (c != null && c.getCount() > 0) {
            AppMessageInfo info;
            c.moveToFirst();
            while (!c.isAfterLast()) {
                info = new AppMessageInfo();
                info.setContent(c.getString(1));
                info.setDate(c.getString(4));
                info.setId(c.getString(0));
                info.setTitle(c.getString(3));
                info.setType(c.getInt(2));
                info.setIconUri(c.getString(5));
                info.setCount(c.getInt(6));
                infos.add(info);
                c.moveToNext();
            }
        }
        if (c != null)
            c.close();
        return infos;
    }

    /**
     * 更新一条消息数据
     *
     * @param context
     * @param info
     * @return
     */
    public static int updateMessage(Context context, AppMessageInfo info) {
        String userId = SettingHelper.getUserId(context);
        String where = AppMessage.USER_ID + "=" + "'" + userId + "'" +
                " AND " + AppMessage.MESSAGE_ID + "=" + "'" + info.getId() + "'";
        final ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(AppMessage.MESSAGE_CONTENT, info.getContent());
        values.put(AppMessage.MESSAGE_DATE, info.getDate());
        values.put(AppMessage.MESSAGE_COUNT, info.getCount());
        values.put(AppMessage.MESSAGE_TITLE, info.getTitle());
        return cr.update(HcProvider.CONTENT_URI_MESSAGE, values, where, null);
    }

    /**
     * 删除一条聊天记录
     *
     * @param context
     * @param chatId 聊天对象的id
     * @param chatMessageId 具体某条聊天记录的id
     * @return
     */
    public static int deleteChatMessage(Context context, String chatId, String chatMessageId) {
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'" + " AND " + ChatMessage.CHAT_MESSAGE_ID + "=" + "'" + chatMessageId + "'";
        final ContentResolver cr = context.getContentResolver();
        return cr.delete(HcProvider.CONTENT_URI_CHAT, where, null);
    }

    /**
     * 获取一条聊天记录
     *
     * @param context
     * @param chatId 具体的聊天对象的id
     * @param chatMessageId 具体聊天记录的id
     * @return 一条具体的聊天记录
     */
    public static ChatMessageInfo getChatMessage(Context context, String chatId, String chatMessageId) {
        String[] projection = {ChatMessage.Chat_ID, ChatMessage.CHAT_MESSAGE_ID, ChatMessage.USER_ID,
                ChatMessage.CHAT_OWN, ChatMessage.CHAT_CONTENT, ChatMessage.CHAT_DATE,
                ChatMessage.CHAT_NAME, ChatMessage.CHAT_TYPE, ChatMessage.CHAT_SHOW_TIME,
                ChatMessage.CHAT_FILE_NAME, ChatMessage.CHAT_VOICE_DURATION, ChatMessage.CHAT_VOICE_READED,
                ChatMessage.CHAT_SEND_STATE, ChatMessage.CHAT_RECEIVER};
        String where = ChatMessage.Chat_ID + "=" + "'" + chatId + "'" + " AND " + ChatMessage.CHAT_MESSAGE_ID + "'" + chatMessageId + "'";
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(HcProvider.CONTENT_URI_CHAT, projection, where, null, null);
        ChatMessageInfo info = null;
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int type = c.getInt(7);
            int own = c.getInt(3);
            info = getChatInfo((own << 4) | type);
            info.setContent(c.getString(4));
            info.setDate(c.getString(5));
            info.setName(c.getString(6));
            info.setChatId(c.getString(0));
            info.setMessageId(c.getString(1));
            info.setUserId(c.getString(2));
            info.setShowDate(c.getInt(8) == 0);
            info.setFilePath(c.getString(9));
            info.setDuration(c.getInt(10));
            info.setReaded(c.getInt(11) == 1);
            info.setState(c.getInt(12));
            info.setReceiver(c.getString(13));
        }
        if (c != null)
            c.close();
        return info;
    }
}
