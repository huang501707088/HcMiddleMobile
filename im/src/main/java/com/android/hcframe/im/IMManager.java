package com.android.hcframe.im;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.contacts.data.ContactsCacheData;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.sql.SettingHelper;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketReader;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.muc.UserStatusListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-18 15:38.
 */

/**
 * 说明：
 * 1.切换用户的时候,Socket连接是断开的,但是XMPPConnection对象还是存在的
 * 2.切换用户的时候{@link XMPPConnection#chatManager}必须设置为null,否则接收到数据没有监听器去回调数据,
 * 因为用户退出{@link PacketReader#cleanup()}  }会清空XMPPConnection中的监听器,而接收数据的监听是在初始化
 * {@link ChatManager}的时候注册的
 * 3.设置数据的接收在{@link PacketReader#resetParser()}里面的setInput
 * 4.数据的接收的监听可以有两个地方设置{@link Connection#collectors}&{@link Connection#recvListeners}
 * 具体调用看{@link PacketReader#parsePackets(Thread)}
 * 接收到的消息
 */
public class IMManager implements OnSendListener {

    private static final String TAG = "IMManager";

    private XMPPConnection mConnection;//链接

    private static IMManager mManager = new IMManager();

    public static IMManager getInstance() {
        return mManager;
    }

    private Thread mSocket;

    private volatile boolean mWorking;

    /** 暂定只有一个Listener */
    private List<OnChatReceiveListener> mListeners = new ArrayList<OnChatReceiveListener>();

    private ExecutorService mService;

    private Timer mTimer = new Timer("request_im_queue");

    /**
     * key:请求的消息包的Id value:超时处理的Task
     */
    private Map<String, TimerTask> mTaskMap = new HashMap<String, TimerTask>();

    /** 存储未发送的消息 */
    private List<CacheInfo> mCache = new ArrayList<CacheInfo>();

    /** key:jid value:*/
    private Map<String, OnSendCallback> mCallback = new HashMap<String, OnSendCallback>();

    private ServiceDiscoveryManager mDiscoveryManager;

    /** key:房间的名字(e.g name@server)； value: 房间的信息*/
    private Map<String, MultiUserChat> mRooms = new HashMap<String, MultiUserChat>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IMManager() {

        mService = Executors.newCachedThreadPool();
        ConnectionConfiguration connConfig = new ConnectionConfiguration(IMUtil.getServerName(), IMUtil.getServerPort());
        connConfig.setDebuggerEnabled(HcLog.DEBUG);
        mConnection = new XMPPConnection(connConfig);

    }

    public void createXMPPSocket() {
        HcLog.D(TAG + "#createXMPPSocket start start start !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (!mConnection.isConnected()) {
            synchronized (IMManager.this) {
                if (!mConnection.isConnected()) {
                    mSocket = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean done = false;
                            try {
                                mConnection.connect();
                                // 登录之前先调用这里需要测试...
                                mDiscoveryManager = new ServiceDiscoveryManager(mConnection);
                                addMessageListener();
                                mConnection.addSendListener(IMManager.this);
                                addInvitationListener();
                                ////////////////////////////////
                                String userId = SettingHelper.getUserId(HcApplication.getContext());
                                if (TextUtils.isEmpty(userId)) {
                                    ;//mConnection.loginAnonymously();
                                } else {
                                    login(userId, SettingHelper.getIMPW(HcApplication.getContext()), "APP");
                                }
//                                mDiscoveryManager = new ServiceDiscoveryManager(mConnection);
//                                addMessageListener();
//                                mConnection.addSendListener(IMManager.this);
//                                addInvitationListener();
                                if (mConnection.isAuthenticated()) {
                                    List<ChatGroupMessageInfo> infos = ChatOperatorDatabase.getChatGroups(HcApplication.getContext());
                                    String jid;
                                    MultiUserChat room;
                                    for (ChatGroupMessageInfo info : infos) {
                                        jid = info.getId();
                                        if (!mRooms.containsKey(jid)) {
                                            room = new MultiUserChat(mConnection, jid);
                                            mRooms.put(jid, room);
                                            room.addParticipantStatusListener(new ParticipantStatus(jid));
                                            room.addSubjectUpdatedListener(new SubjectUpdate(jid));
                                            room.addUserStatusListener(new UserStatus(jid));

                                        }
                                    }

                                    Iterator<MultiUserChat> iterator = mRooms.values().iterator();

                                    while (iterator.hasNext()) {
                                    	room = iterator.next();
                                        joinRoom(room.getRoom(), SettingHelper.getName(HcApplication.getContext()), false);
                                    }
                                }


                                if (!TextUtils.isEmpty(userId)) {
                                    synchronized (mCache) {
                                        if (mCache.size() > 0) {
                                            while (!done && !mConnection.isAuthenticated()) {
                                                // 轮询,需要测试
                                            }
                                            if (mConnection.isAuthenticated()) {
                                                for (CacheInfo info : mCache) {
                                                    if (info.category == 0) {
                                                        sendMessage(info.info, info.message, info.jid, info.group, info.userId, info.receiver);
                                                    } else {
                                                        sendMediaMessage(info.info, info.message, info.jid, info.attachment, info.type, info.duration, info.group, info.userId);
                                                    }

                                                }
                                            }


                                        }
                                    }


                                }
                            } catch (XMPPException e) {
                                done = true;
                                e.printStackTrace();
                                synchronized (mCache) {
                                    for (CacheInfo info : mCache) {
                                        info.info.setState(3);
                                        ChatOperatorDatabase.updateChatMessage(HcApplication.getContext(), info.info);
                                        // 通知界面
                                        OnSendCallback callback = mCallback.get(info.jid);
                                        if (callback != null) {
                                            callback.onResponse(info.info, Result.ERROR);
                                        }
                                    }
                                }
                                HcLog.D(TAG + "#createXMPPSocket error = "+e );
                            } finally {
                                ;
                            }
                            mSocket = null;
                        }

                    }, "create-socket");
                    mSocket.start();
                }
            }
        }
    }

    /**
     * 创建服务器连接
     * */
    public void createXMPPSocket(final String ip, final int port) {

        if (mSocket == null && !mWorking) {
            mSocket = new Thread(new Runnable() {
                @Override
                public void run() {
                    mWorking = true;
                    synchronized (IMManager.this) {
                        if (mConnection == null) {
                            ConnectionConfiguration connConfig = new ConnectionConfiguration(ip, port);
                            connConfig.setDebuggerEnabled(HcLog.DEBUG);
                            mConnection = new XMPPConnection(connConfig);
                            try {
                                mConnection.connect();
                                String userId = SettingHelper.getUserId(HcApplication.getContext());
                                if (TextUtils.isEmpty(userId)) {
                                    ;//mConnection.loginAnonymously();
                                } else {
                                    /*mConnection.*/login(userId, SettingHelper.getIMPW(HcApplication.getContext()), "APP");

                                }
                                addMessageListener();
                                addInvitationListener();
                            } catch (XMPPException e) {
                                mConnection = null;
                                mWorking = false;
                                e.printStackTrace();
                                HcLog.D(TAG + "#createXMPPSocket error = "+e + " ip = "+ip + " port = "+port);
                            } finally {
                                if (mConnection == null) {
                                    mWorking = false;
                                }
                            }
                        }

                    }
                    mSocket = null;
                }
            }, "create-socket");
            mSocket.start();
        } else {
            HcLog.D(TAG + " #createXMPPSocket mConnection ="+mConnection + " mWorking = "+mWorking);
        }

    }

    private void start() {
        if (mSocket == null) {
            mSocket = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (IMManager.this) {
                        try {
                            mConnection.connect();
                        } catch (XMPPException e) {
                            mConnection = null;
                            e.printStackTrace();
                            HcLog.D(TAG + "#createXMPPSocket error = "+e);
                        }
                    }

                }
            });
        }
        mSocket.start();
    }

    public void stop() {
        if (mSocket != null) {
            try {
                Thread t = mSocket;
                t.join();
                mSocket = null;
            } catch (InterruptedException ex) {
                // so now what?
            }
        }
    }

    /**
     * 退出服务器,一般不会主动退出服务端.
     * */
    public void logOut() {
        mConnection.disconnect();
    }

    public void login(String name, String pw, String resource) {
        /**
         *@author jinjr
         *@date 17-3-31 下午1:46
         */
        resource = "phone";
//        if (TextUtils.isEmpty(resource))
//            resource = "Android";
        if (mConnection.isConnected()) {
            try {
                mConnection.login(name, pw, resource);
                // 登录成功,获取通讯录数据
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ContactsCacheData.getInstance().getEmployees();
                    }
                });

            } catch(Exception e) {
                HcLog.D(TAG + "#login error = "+e + " name = "+name + " resource = "+resource);
            }
        } else {
            // 创建默认的连接
            HcLog.D(TAG + "#login Connection is null! " + " name = "+name + " resource = "+resource);
            createXMPPSocket(); // 会自动登录的
        }
//        try {
//
//            synchronized (IMManager.this) {
//                if (mConnection != null) {
//                    mConnection.login(name, pw, resource);
//                } else {
//                    // 创建默认的连接
//                    HcLog.D(TAG + "#login Connection is null! " + " name = "+name + " resource = "+resource);
//                    createDefaultSocket();
//                }
//            }
//
//        } catch (Exception e) {
//            HcLog.D(TAG + "#login error = "+e + " name = "+name + " resource = "+resource);
//        }

    }

    public boolean isAuthenticated() {
        return mConnection.isAuthenticated();
    }

    private ChatManagerListener mChatManagerListener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            HcLog.D(TAG + " $ChatManagerListener#chatCreated chat = "+chat + " createdLocally ="+createdLocally);
            if (!createdLocally) {
                chat.addMessageListener(new MessageListener() {
                    public void processMessage(Chat chat, Message mess) {
                        HcLog.D(TAG + " $ChatManagerListener#chatCreated#processMessage chat = "+chat + " message = "+mess + " message id = "+mess.getPacketID() + " message type = "+mess.getType());
                        // 注意这里还是在子线程里,message id 可能为空.
                        synchronized (mListeners) {
                            if (mListeners.isEmpty()) {
                                mListeners.add(IMReceiverListener.getInstance());
                            }
                            for (OnChatReceiveListener listener : mListeners) {
                                listener.onReceive(chat, mess);
                            }
                        }

                    }
                });
            }
        }
    };


    /**
     * 连接成功添加消息接收的监听器,线程不安全.
     */
    private void addMessageListener() {
        if (mConnection.isConnected()) {
            mConnection.getChatManager().addChatListener(mChatManagerListener/**new ChatManagerListener() {
                public void chatCreated(Chat chat, boolean createdLocally) {
                    HcLog.D(TAG + " #addMessageListener chat = "+chat + " createdLocally ="+createdLocally);
                    if (!createdLocally) {
                        chat.addMessageListener(new MessageListener() {
                            public void processMessage(Chat chat, Message mess) {
                                HcLog.D(TAG + " #addMessageListener chat = "+chat + " message = "+mess + " message id = "+mess.getPacketID() + " message type = "+mess.getType());
                                // 注意这里还是在子线程里,message id 可能为空.
                                for (OnChatReceiveListener listener : mListeners) {
                                    listener.onReceive(chat, mess);
                                }
                            }
                        });
                    }
                }
            }*/);
        } else {
            HcLog.D(TAG + " #addMessageListener mConnection is null!");
        }
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param info 发送的消息
     * @param message 消息的标题
     * @param jid 接收方的jid,可能为群的jid
     * @param group 是否是群聊
     * @param userId 群聊的时候发送方的userId
     * @param receiver 群聊的@的对象,格式为 userId;userId;userId
     */
    public void sendMessage(ChatMessageInfo info, String message, String jid, boolean group, String userId, String receiver) {
        if (mConnection.isConnected()) {
//            jid = "286@10.80.7.187";//"15524861013@10.80.7.187";
            ChatManager manager = mConnection.getChatManager();
            Chat chat = manager.createChat(jid, new MessageListener() { // 这个listener只用作当前的chat发送异常,才会回到.接收的chat的listener在addMessageListener()方法里注册了.
                @Override
                public void processMessage(Chat chat, Message message) {
                    HcLog.D(TAG + " #sendMessage processMessage chat = "+chat + " message = "+message + " message id = "+message.getPacketID());
                    synchronized (mListeners) {
                        for (OnChatReceiveListener listener : mListeners) {
                            listener.onReceive(chat, message);
                        }
                    }
                }
            });

            Message newMessage = new Message();
            newMessage.setBody(message);
            String id = newMessage.getPacketID(); // 设置PacketId
            TimerTask task = new RequestTask(info, id, jid);
            synchronized (mTaskMap) {
                mTaskMap.put(id, task);
            }
            mTimer.schedule(task, 5 * 1000);
            try {

                if (group) {
                    HcLog.D(TAG + " #sendMessage 发送群消息！！！");
                    newMessage.setUserId(userId);
                    newMessage.setReceiver(receiver);
//                    chat.sendGroupMessage(message, userId);
                    chat.sendGroupMessage(newMessage);
                } else {

//                    chat.sendMessage(message);
                    chat.sendMessage(newMessage);
//                    testSendSystemMessage(chat);
                }

            } catch (Exception e) {
                synchronized (mTaskMap) {
                    TimerTask task2 = mTaskMap.remove(id);
                    if (task2 != null) {
                        task2.cancel();
                        ((RequestTask) task2).sendError();
                    }
                }
                HcLog.D(TAG + " #sendMessage send message error = "+e);
            }

        } else { // 这里会有个问题,再次创建的时候也不一定成功,所以这里需要做个处理
            CacheInfo cache = new CacheInfo();
            cache.info = info;
            cache.category = 0;
            cache.message = message;
            cache.group = group;
            cache.jid = jid;
            cache.userId = userId;
            cache.receiver = receiver;
            synchronized (mCache) {
                mCache.add(cache);
            }
            // 创建连接
            createXMPPSocket();
            HcLog.D(TAG + " #sendMessage mConnection is not connect!!!!!");
        }
    }

    public void createDefaultSocket() {
        if (mSocket == null && !mWorking) {
            mSocket = new Thread(new Runnable() {
                @Override
                public void run() {
                    mWorking = true;
                    synchronized (IMManager.this) {
                        if (mConnection == null) {
                            ConnectionConfiguration connConfig = new ConnectionConfiguration(IMUtil.getServerName(), IMUtil.getServerPort());
                            connConfig.setDebuggerEnabled(HcLog.DEBUG);
                            mConnection = new XMPPConnection(connConfig);
                            try {
                                mConnection.connect();
                                String userId = SettingHelper.getUserId(HcApplication.getContext());
                                if (TextUtils.isEmpty(userId)) {
                                    ;//mConnection.loginAnonymously();
                                } else {
                                    /*mConnection.*/login(userId, SettingHelper.getIMPW(HcApplication.getContext()), "APP");
                                }
                                addMessageListener();
                                addInvitationListener();
                            } catch (XMPPException e) {
                                mConnection = null;
                                mWorking = false;
                                e.printStackTrace();
                                HcLog.D(TAG + "#createDefaultSocket error = "+e);
                            } finally {
                                if (mConnection == null) {
                                    mWorking = false;
                                }
                            }
                        }

                    }
                    mSocket = null;
                }
            }, "create-socket");
            mSocket.start();
        } else {
            HcLog.D(TAG + " #createDefaultSocket mConnection ="+mConnection + " mWorking = "+mWorking);
        }
    }

    public void addChatListener(OnChatReceiveListener listener) {
        synchronized (mListeners) {
//            if (mListeners.contains(listener)) return;
            mListeners.clear();
            mListeners.add(listener);
        }

    }

    public void removeChatListener(OnChatReceiveListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param info 发送的消息
     * @param message 消息的标题
     * @param jid 接收方的jid,可能为群的jid
     * @param attachment 消息的具体内容
     * @param type 发送消息的类型
     * @param duration 发送语音的时候的时长
     * @param group 是否是群聊
     * @param userId 群聊的时候发送方的userId
     */
    public void sendMediaMessage(ChatMessageInfo info, String message, String jid, String attachment, String type, String duration, boolean group, String userId) {
        if (mConnection.isConnected()) {
//            jid = "286@10.80.7.187";//"15524861013@10.80.7.187";
            ChatManager manager = mConnection.getChatManager();
            Chat chat = manager.createChat(jid, new MessageListener() { // 这个listener只用作当前的chat发送异常,才会回到.接收的chat的listener在addMessageListener()方法里注册了.
                @Override
                public void processMessage(Chat chat, Message message) {
                    HcLog.D(TAG + " #sendMediaMessage processMessage chat = "+chat + " message = "+message);
                    synchronized (mListeners) {
                        for (OnChatReceiveListener listener : mListeners) {
                            listener.onReceive(chat, message);
                        }
                    }
                }
            });

            Message newMessage = new Message();
            newMessage.setBody(message);
            newMessage.setAttachment(attachment);
            newMessage.setDuration(duration);
            newMessage.setMediaType(type);
            String id = newMessage.getPacketID(); // 设置PacketId
            TimerTask task = new RequestTask(info, id, jid);
            synchronized (mTaskMap) {
                mTaskMap.put(id, task);
            }
            mTimer.schedule(task, 10 * 1000);

            try {

                if (group) {
                    newMessage.setUserId(userId);
                    chat.sendGroupMediaMessage(newMessage);
//                    chat.sendGroupMediaMessage(message, attachment, type, duration, userId);
                } else {
                    chat.sendMediaMessage(newMessage);
//                    chat.sendMediaMessage(message, attachment, type, duration);
                }

            } catch (Exception e) {
                synchronized (mTaskMap) {
                    TimerTask task2 = mTaskMap.remove(id);
                    if (task2 != null) {
                        task2.cancel();
                        ((RequestTask) task2).sendError();
                    }
                }


                HcLog.D(TAG + " #sendMessage send message error = "+e);
            }

        } else {
            CacheInfo cache = new CacheInfo();
            cache.info = info;
            cache.category = 1;
            cache.message = message;
            cache.group = group;
            cache.jid = jid;
            cache.userId = userId;
            cache.attachment = attachment;
            cache.duration = duration;
            cache.type = type;
            synchronized (mCache) {
                mCache.add(cache);
            }
            // 创建连接
            createXMPPSocket();
            HcLog.D(TAG + " #sendMessage mConnection is not connect!!!!!");
        }
    }

    /**
     * 获取讨论组列表
     * @return
     */
    public List<HostedRoom> getRooms() {
        List<HostedRoom> rooms = null;
        HcLog.D(TAG + " #getRooms " + " start time = "+ HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
        try {
            if (mConnection.isConnected()) {
                ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(mConnection);
                if (discoManager == null) {
                    discoManager = new ServiceDiscoveryManager(mConnection);
                }
                rooms = new ArrayList<HostedRoom>();
                rooms.addAll(MultiUserChat.getHostedRooms(mConnection, IMUtil.CONFERENCE + "." +IMUtil.getServerName()));
            } else {
                createXMPPSocket();
            }
        } catch(Exception e) {
            StringBuilder builder = new StringBuilder();
            StackTraceElement[] elements = e.getStackTrace();
            for (StackTraceElement stackTraceElement : elements) {
                builder.append(stackTraceElement.toString());
                builder.append("\n");
            }
            Throwable cause = e.getCause();
            if (cause != null) {
                elements = cause.getStackTrace();
                for (StackTraceElement stackTraceElement : elements) {
                    builder.append("causeBy:" + stackTraceElement.toString());
                    builder.append("\n");
                }
            }
            HcLog.D(TAG + " #getRooms Exception e = "+builder.toString() + " error time = "+ HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));
        }
        HcLog.D(TAG + " #getRooms room size = "+(rooms != null ? rooms.size() : 0) + " end time = "+ HcUtil.getDate(HcUtil.FORMAT_LOG_DATE, System.currentTimeMillis()));

        return rooms;
    }

    public final void execute(Runnable r) {
        mService.execute(r);
    }

    /**
     * 新建群组
     * @param roomName 群的JID
     * @param nickname 群的昵称
     * @return
     */
    public MultiUserChat createRoom(String roomName, String nickname) {
        MultiUserChat room = null;
        if (mConnection.isConnected()) {
            room = mRooms.get(roomName);
            if (room == null) {
                room = new MultiUserChat(mConnection, roomName);
                try {
                    room.create(nickname);
                    // 添加监听
                    room.addParticipantStatusListener(new ParticipantStatus(roomName));
                    room.addSubjectUpdatedListener(new SubjectUpdate(roomName));
                    room.addUserStatusListener(new UserStatus(roomName));
                } catch(Exception e) {
                    room = null;
                    HcLog.D(TAG + "#createRoom error e = "+e);
                }
            }

        } else {
            createXMPPSocket();
        }
        return room;
    }

    /**
     * 获取修改好的群聊的配置表单(长久的群配置)
     * @param room 群聊
     * @return
     */
    public Form getPersistentRoomForm(MultiUserChat room) {
        Form form = null;
        if (mConnection.isConnected()) {
            try {
                form = room.getConfigurationForm();
                if (form != null) {
                    form = form.createAnswerForm();

//                form.setAnswer("muc#roomconfig_maxusers", new ArrayList<String>().add("50"));
                    form.setAnswer("muc#roomconfig_persistentroom", true);
                } else {
                    HcLog.D(TAG + " #getConfigurationForm form is null!");
                }

            } catch(Exception e) {
                form = null;
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }

        } else {
            createXMPPSocket();
        }
        return form;
    }

    public boolean setRoomConfigurationForm(MultiUserChat room, Form form) {
        if (mConnection.isConnected()) {
            try {
                room.sendConfigurationForm(form);
                // 房间创建并且配置成功
                mRooms.put(room.getRoom(), room);
                return true;
            } catch(Exception e) {
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    private void addInvitationListener() {
        if (mConnection.isConnected()) {
            MultiUserChat.addInvitationListener(mConnection,
                    new InvitationListener() {
                        public void invitationReceived(Connection conn, String room, String inviter,
                                                       String reason, String password, Message message) {
                            HcLog.D(TAG + "#addInvitationListener room = "+room + " inviter = "+inviter + " reason = "+reason
                                + " password = "+password + " message to = "+message.getTo() + " message from = "+message.getFrom());
                            if (mConnection != null) {
                                String nickName = ChatOperatorDatabase.getNameByUserId(HcApplication.getContext(), message.getTo().split("@")[0]);
                                try {
                                    HcLog.D(TAG + " #invitationReceived before join nickName = "+nickName);
                                    MultiUserChat newRoom = new MultiUserChat(conn, room);
                                    mRooms.put(room, newRoom);
                                    // 添加监听
                                    newRoom.addParticipantStatusListener(new ParticipantStatus(room));
                                    newRoom.addSubjectUpdatedListener(new SubjectUpdate(room));
                                    newRoom.addUserStatusListener(new UserStatus(room));
                                    joinRoom(room, nickName, false);
                                } catch(Exception e) {
                                    HcLog.D(TAG + " #invitationReceived join群聊失败！！  error = "+e);
                                }

                            }
                        }
                    });
        }
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param message
     * @param jid
     */
    public void sendMessage(ChatMessageInfo info, String message, String jid) {
        sendMessage(info, message, jid, false);
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param message
     * @param jid
     * @param group
     */
    public void sendMessage(ChatMessageInfo info, String message, String jid, boolean group) {
        sendMessage(info, message, jid, group, null, null);
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param message
     * @param jid
     * @param attachment
     * @param type
     */
    public void sendMediaMessage(ChatMessageInfo info, String message, String jid, String attachment, String type, String duration) {
        sendMediaMessage(info, message, jid, attachment, type, duration, false);
    }

    /**
     * 发送消息,线程不安全的,注意为了用户体验
     * @param info 发送的消息
     * @param message 消息的标题
     * @param jid 对方的jid
     * @param attachment 消息的具体内容
     * @param type 发送消息的类型
     * @param duration 发送语音的时候的时长
     * @param group 是否是群聊
     */
    public void sendMediaMessage(ChatMessageInfo info, String message, String jid, String attachment, String type, String duration, boolean group) {
        sendMediaMessage(info, message, jid, attachment, type, duration, group, null);
    }

    @Override
    public void onSendCompleted(Packet packet) {
        // 这里在发送的子线程里面
        String packetId = packet.getPacketID();
        if (packetId != null) {
            TimerTask task = mTaskMap.remove(packetId);
            if (task != null) {
                ((RequestTask) task).sendSuccess();
                task.cancel();
            }
        }
    }

    private class RequestTask extends TimerTask {

        private ChatMessageInfo mInfo;

        private String mMessageId;

        private String mJid;

        public RequestTask(ChatMessageInfo info, String messageId, String jid) {
            mInfo = info;
            mMessageId = messageId;
            mJid = jid;
        }

        @Override
        public void run() {
            synchronized (mTaskMap) {
                mTaskMap.remove(mMessageId);
            }

            mInfo.setState(3);
            ChatOperatorDatabase.updateChatMessage(HcApplication.getContext(), mInfo);
            OnSendCallback callback = mCallback.get(mJid);
            if (callback != null) {
                callback.onResponse(mInfo, Result.TIMEOUT);
            }
            mInfo = null;
        }

        public void sendSuccess() {
            mInfo.setState(0);
            ChatOperatorDatabase.updateChatMessage(HcApplication.getContext(), mInfo);
            OnSendCallback callback = mCallback.get(mJid);
            if (callback != null) {
                callback.onResponse(mInfo, Result.SUCCESS);
            }
            mInfo = null;
        }

        public void sendError() {
            mInfo.setState(3);
            ChatOperatorDatabase.updateChatMessage(HcApplication.getContext(), mInfo);
            OnSendCallback callback = mCallback.get(mJid);
            if (callback != null) {
                callback.onResponse(mInfo, Result.ERROR);
            }
            mInfo = null;
        }
    }


    /**
     * 发送消息的时候,连接还未建立
     */
    private static class CacheInfo {

        int category; // 0:发送文本；1：发送文件
        ChatMessageInfo info;
        String message;
        String jid;
        String attachment;
        String type;
        String duration;
        boolean group;
        String userId;
        String receiver;
    }

    public enum Result {

        SUCCESS,
        ERROR,
        TIMEOUT
    }

    public void addSendCallback(String jid, OnSendCallback callback) {
        mCallback.clear();
        mCallback.put(jid, callback);
    }

    public void removeSendCallback() {
        mCallback.clear();
    }

    /**
     * 主动加入房间
     * @param room 房间jid
     * @param nickname 在房间的nickname,暂定为userId
     * @param check
     */
    public void joinRoom(String room, String nickname, boolean check) {
        if (mConnection.isConnected()) {
            MultiUserChat roomChat = mRooms.get(room);//new MultiUserChat(mConnection, room);
            if (roomChat != null) {
                try {
                    if (!check || !roomChat.isJoined())
                        roomChat.join(nickname);
                    else {
                        HcLog.D(TAG + " #joinRoom 你已经加入过了！");
                    }
                } catch(Exception e) {
                    HcLog.D(TAG + " #joinRoom error e = "+e);
                }
            } else {
                HcLog.D(TAG + " #joinRoom 出错了 房间未创建!");
            }


        } else {
            createXMPPSocket();
        }
    }

    private class SubjectUpdate implements SubjectUpdatedListener {

        private static final String TAG = IMManager.TAG + "$SubjectUpdate";

        private String mRoomName;

        public SubjectUpdate(String roomName) {
            mRoomName = roomName;
        }

        @Override
        public void subjectUpdated(String subject, String from) {
            HcLog.D(TAG + " #subjectUpdated subject = "+subject + " from = "+from + " roomName = "+mRoomName);
        }
    }

    private class UserStatus implements UserStatusListener {

        private static final String TAG = IMManager.TAG + "$UserStatus";

        private String mRoomName;

        public UserStatus(String roomName) {
            mRoomName = roomName;
        }

        @Override
        public void adminGranted() {
            HcLog.D(TAG + " #adminGranted roomName = "+mRoomName);
        }

        @Override
        public void kicked(String actor, String reason) {
            HcLog.D(TAG + " #kicked roomName = "+mRoomName + " actor = "+actor + " reason = "+reason);
        }

        @Override
        public void voiceGranted() {
            HcLog.D(TAG + " #voiceGranted roomName = "+mRoomName);
        }

        @Override
        public void voiceRevoked() {
            HcLog.D(TAG + " #voiceRevoked roomName = "+mRoomName);
        }

        @Override
        public void banned(String actor, String reason) {
            HcLog.D(TAG + " #banned roomName = "+mRoomName + " actor = "+actor + " reason = "+reason);
        }

        @Override
        public void membershipGranted() {
            HcLog.D(TAG + " #membershipGranted roomName = "+mRoomName);
        }

        @Override
        public void membershipRevoked() {
            HcLog.D(TAG + " #membershipRevoked roomName = "+mRoomName);
        }

        @Override
        public void moderatorGranted() {
            HcLog.D(TAG + " #moderatorGranted roomName = "+mRoomName);
        }

        @Override
        public void moderatorRevoked() {
            HcLog.D(TAG + " #moderatorRevoked roomName = "+mRoomName);
        }

        @Override
        public void ownershipGranted() {
            HcLog.D(TAG + " #ownershipGranted roomName = "+mRoomName);
        }

        @Override
        public void ownershipRevoked() {
            HcLog.D(TAG + " #ownershipRevoked roomName = "+mRoomName);
        }

        @Override
        public void adminRevoked() {
            HcLog.D(TAG + " #adminRevoked roomName = "+mRoomName);
        }
    }

    /**
     * 参与者的状态
     */
    private class ParticipantStatus implements ParticipantStatusListener {

        private static final String TAG = IMManager.TAG + "$ParticipantStatus";

        private String mRoomName;

        public ParticipantStatus(String roomName) {
            mRoomName = roomName;
        }

        @Override
        public void adminGranted(String participant) {
            HcLog.D(TAG + "#adminGranted roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void joined(String participant) { // 别人邀请加入群,加入成功会接收到,这里需要在本地创建群吗?
            HcLog.D(TAG + "#joined roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void left(String participant) {
            HcLog.D(TAG + "#left roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void kicked(String participant, String actor, String reason) {
            HcLog.D(TAG + "#kicked roomName = "+mRoomName + " participant = "+participant + " actor = "+actor + " reason = "+reason);
        }

        @Override
        public void voiceGranted(String participant) {
            HcLog.D(TAG + "#voiceGranted roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void voiceRevoked(String participant) {
            HcLog.D(TAG + "#voiceRevoked roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void banned(String participant, String actor, String reason) {
            HcLog.D(TAG + "#banned roomName = "+mRoomName + " participant = "+participant + " actor = "+actor + " reason ="+reason);
        }

        @Override
        public void membershipGranted(String participant) {
            HcLog.D(TAG + "#membershipGranted roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void membershipRevoked(String participant) {
            HcLog.D(TAG + "#membershipRevoked roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void moderatorGranted(String participant) {
            HcLog.D(TAG + "#moderatorGranted roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void moderatorRevoked(String participant) {
            HcLog.D(TAG + "#moderatorRevoked roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void ownershipGranted(String participant) {
            HcLog.D(TAG + "#ownershipGranted roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void ownershipRevoked(String participant) {
            HcLog.D(TAG + "#ownershipRevoked roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void adminRevoked(String participant) {
            HcLog.D(TAG + "#adminRevoked roomName = "+mRoomName + " participant = "+participant);
        }

        @Override
        public void nicknameChanged(String participant, String newNickname) {
            HcLog.D(TAG + "#nicknameChanged roomName = "+mRoomName + " participant = "+participant + " newNickname = "+newNickname);
        }
    }

    /**
     * 删除群聊,需要在子线程中进行
     * @param context
     * @param jid 群聊的jid
     * @param reason 删除原因
     * @return
     */
    public boolean destoryRoom(Context context, String jid, String reason) {
        if (mConnection.isConnected()) {
            MultiUserChat room = mRooms.get(jid);
            if (room != null) {
                try {
                    room.destroy(reason, jid);
                    // 删除数据库数据
                    ChatOperatorDatabase.deleteAppMessage(context, jid);
                    ChatOperatorDatabase.deleteChatMessages(context, jid);
                    ChatOperatorDatabase.deleteChatGroup(context, jid);
                    return true;
                } catch(Exception e) {
                    HcLog.D(TAG + " #destoryRoom e ="+e);
                }

            } else {
                HcLog.D(TAG + " #destoryRoom 出错了 房间未创建!");
            }
        } else {
            createXMPPSocket();
        }

        return false;
    }

    /**
     * 剔除群成员
     * @param jid 群的jid
     * @param nickname 群成员的nickname
     * @param reason
     * @return
     */
    public boolean kickParticipant(String jid, String nickname, String reason) {
        try {
            if (mConnection.isConnected()) {
                MultiUserChat room = mRooms.get(jid);
                if (room != null) {
                    room.kickParticipant(nickname, reason);
                    return true;
                } else {
                    HcLog.D(TAG + " #kickParticipant 出错了 房间未创建!");
                }

            } else {
                createXMPPSocket();
            }
        } catch(Exception e) {
            HcLog.D(TAG + " #kickParticipant e ="+e);
        }

        return false;
    }

    /**
     * Invites another user to the room in which one is an occupant. The invitation
     * will be sent to the room which in turn will forward the invitation to the invitee.<p>
     *
     * If the room is password-protected, the invitee will receive a password to use to join
     * the room. If the room is members-only, the the invitee may be added to the member list.
     * @param jid 群的jid
     * @param user the user to invite to the room.(e.g. hecate@shakespeare.lit)
     * @param reason the reason why the user is being invited.
     */
    public void invite(String jid, String user, String reason) {
        if (mConnection.isConnected()) {
            MultiUserChat room = mRooms.get(jid);
            if (room != null) {
                try {
                    room.invite(user, reason);
                } catch(Exception e) {
                    HcLog.D(TAG + " #invite e ="+e);
                }
            } else {
                HcLog.D(TAG + " #invite 出错了 房间未创建!");
            }
        } else {
            createXMPPSocket();
        }
    }

    /**
     * Invites another user to the room in which one is an occupant. The invitation
     * will be sent to the room which in turn will forward the invitation to the invitee.<p>
     *
     * If the room is password-protected, the invitee will receive a password to use to join
     * the room. If the room is members-only, the the invitee may be added to the member list.
     * @param room 群
     * @param user the user to invite to the room.(e.g. hecate@shakespeare.lit)
     * @param reason the reason why the user is being invited.
     */
    public void invite(MultiUserChat room, String user, String reason) {
        if (mConnection.isConnected()) {
            try {
                room.invite(user, reason);
            } catch(Exception e) {
                HcLog.D(TAG + " #invite e ="+e);
            }
        } else {
            createXMPPSocket();
        }
    }

    /**
     * 获取群聊的信息,不包括具体的成员列表
     * @param room
     * @return
     */
    public RoomInfo getRoomInfo(String room) {
        RoomInfo info = null;
        if (mConnection.isConnected()) {
            try {
                info = MultiUserChat.getRoomInfo(mConnection, room);
            } catch(Exception e) {
                HcLog.D(TAG + "#getRoomInfo room = "+room + " error = "+e);
            }

        } else {
            createXMPPSocket();
        }
        return info;
    }

    /**
     * 获取成员
     * @param jid
     * @return
     */
    public List<Occupant> getParticipants(String jid) {
        List<Occupant> participants = null;
        if (mConnection.isConnected()) {
            try {
                MultiUserChat room = mRooms.get(jid);
                if (room != null) {
                    participants = new ArrayList<Occupant>();
                    participants.addAll(room.getParticipants());
                } else {
                    HcLog.D(TAG + " #getParticipants 出错了 房间未创建!");
                }
            } catch(Exception e) {
                HcLog.D(TAG + " #getParticipants e ="+e);
            }
        }
        return participants;
    }

    /**
     * 获取成员
     * @param jid
     * @return
     */
    public List<Affiliate> getAffiliates(String jid) {
        List<Affiliate> affiliates = null;
        if (mConnection.isConnected()) {
            try {
                MultiUserChat room = mRooms.get(jid);
                if (room != null) {
                    affiliates = new ArrayList<Affiliate>();
                    affiliates.addAll(room.getMembers());
                } else {
                    HcLog.D(TAG + " #getParticipants 出错了 房间未创建!");
                }
            } catch(Exception e) {
                HcLog.D(TAG + " #getParticipants e ="+e);
            }
        }
        return affiliates;
    }

    /**
     * 获取群的配置信息
     * @param room 群的jid {e.g name@server}
     * @return
     */
    public Form getConfigurationForm(String room) {
        Form form = null;
        if (mConnection.isConnected()) {
            try {
                MultiUserChat chat = mRooms.get(room);
                if (chat == null) {
                    chat = new MultiUserChat(mConnection, room);
                    mRooms.put(room, chat);
                    chat.addParticipantStatusListener(new ParticipantStatus(room));
                    chat.addSubjectUpdatedListener(new SubjectUpdate(room));
                    chat.addUserStatusListener(new UserStatus(room));
                }
                form = chat.getConfigurationForm();

            } catch(Exception e) {
                form = null;
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }

        } else {
            createXMPPSocket();
        }
        return form;
    }

    /**
     * 获取未修改过的群表单
     * @param room 群聊
     * @returns
     */
    public Form getConfigurationForm(MultiUserChat room) {
        Form form = null;
        if (mConnection.isConnected()) {
            try {
                form = room.getConfigurationForm();

            } catch(Exception e) {
                form = null;
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }

        } else {
            createXMPPSocket();
        }
        return form;
    }

    /**
     * 修改配置上传到服务端
     * @param room 需要配置的群的jid
     * @param form 已经配置过的表单
     * @return
     */
    public boolean setRoomConfiguration(String room, Form form) {
        if (mConnection.isConnected()) {
            try {
                MultiUserChat chat = mRooms.get(room);
                if (chat == null) {
                    chat = new MultiUserChat(mConnection, room);
                    mRooms.put(room, chat);
                    chat.addParticipantStatusListener(new ParticipantStatus(room));
                    chat.addSubjectUpdatedListener(new SubjectUpdate(room));
                    chat.addUserStatusListener(new UserStatus(room));
                }
                chat.sendConfigurationForm(form);
                return true;
            } catch(Exception e) {
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    /**
     * 修改配置上传到服务端
     * @param room 修改配置的群
     * @param form 已经配置过的表单
     * @return
     */
    public boolean setRoomConfiguration(MultiUserChat room, Form form) {
        if (mConnection.isConnected()) {
            try {
                room.sendConfigurationForm(form);
                return true;
            } catch(Exception e) {
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    /**
     * 修改群的持久性配置上传到服务端
     * @param room 需要配置的群的jid
     * @param form 已经配置过的表单
     * @return
     */
    public boolean setPersistentRoomConfiguration(String room, Form form, boolean persistent) {
        if (mConnection.isConnected()) {
            try {
                MultiUserChat chat = mRooms.get(room);
                if (chat == null) {
                    chat = new MultiUserChat(mConnection, room);
                    mRooms.put(room, chat);
                    chat.addParticipantStatusListener(new ParticipantStatus(room));
                    chat.addSubjectUpdatedListener(new SubjectUpdate(room));
                    chat.addUserStatusListener(new UserStatus(room));
                }
                String type = form.getType();
                if (type.equals("form")) {
                    form = form.createAnswerForm();
                }
                form.setAnswer("muc#roomconfig_persistentroom", persistent);
                chat.sendConfigurationForm(form);
                return true;
            } catch(Exception e) {
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    /**
     * 修改群的持久性配置上传到服务端
     * @param room 需要配置的群的jid
     * @param form 已经配置过的表单
     * @return
     */
    public boolean setRoomNameConfiguration(String room, Form form, String name) {
        if (mConnection.isConnected()) {
            try {
                MultiUserChat chat = mRooms.get(room);
                if (chat == null) {
                    chat = new MultiUserChat(mConnection, room);
                    mRooms.put(room, chat); // 要不要join
                    chat.addParticipantStatusListener(new ParticipantStatus(room));
                    chat.addSubjectUpdatedListener(new SubjectUpdate(room));
                    chat.addUserStatusListener(new UserStatus(room));
                }
                String type = form.getType();
                if (type.equals("form")) {
                    form = form.createAnswerForm();
                }
                form.setAnswer("muc#roomconfig_roomname", name);
                chat.sendConfigurationForm(form);
                return true;
            } catch(Exception e) {
                HcLog.D(TAG + "#getConfigurationForm error e = "+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    /**
     * 剔除群成员
     * @param jid 群的jid
     * @param nicknames 群成员的nickname列表
     * @param reason
     * @return
     */
    public boolean kickParticipant(String jid, Collection<String> nicknames, String reason) {
        try {
            if (mConnection.isConnected()) {
                MultiUserChat room = mRooms.get(jid);
                if (room != null) {
                    room.kickParticipant(nicknames, reason);
                    return true;
                } else {
                    HcLog.D(TAG + " #kickParticipant 出错了 房间未创建!");
                }

            } else {
                createXMPPSocket();
            }
        } catch(Exception e) {
            HcLog.D(TAG + " #kickParticipant e ="+e);
        }

        return false;
    }

    /**
     * Revokes users' membership. Only administrators are able to revoke membership. A user
     * that becomes a room member will be able to enter a room of type Members-Only (i.e. a room
     * that a user cannot enter without being on the member list). If the user is in the room and
     * the room is of type members-only then the user will be removed from the room.
     *
     * @param roomName 群的jid
     * @param jids the bare XMPP user IDs of the users to revoke membership.
     * @throws XMPPException if an error occurs revoking membership to a user.
     */
    public boolean revokeMembership(String roomName, Collection<String> jids) {
        if (mConnection != null) {
            try {
                MultiUserChat room = mRooms.get(roomName);
                if (room != null) {
                    room.revokeMembership(jids);
                    return true;
                } else {
                    HcLog.D(TAG + " #revokeMembership 出错了 房间未创建!");
                }
            } catch(Exception e) {
                HcLog.D(TAG + " #revokeMembership e ="+e);
            }
        } else {
            createXMPPSocket();
        }
        return false;
    }

    /**
     * 获取群列表的时候,要是群之前没有,会被调用
     * @param jid
     */
    public void addRoom(String jid) {
        HcLog.D(TAG + " #addRoom jid = "+jid);
        if (mConnection != null) {
            MultiUserChat room = new MultiUserChat(mConnection, jid);
            mRooms.put(jid, room);
            room.addParticipantStatusListener(new ParticipantStatus(jid));
            room.addSubjectUpdatedListener(new SubjectUpdate(jid));
            room.addUserStatusListener(new UserStatus(jid));
            joinRoom(room.getRoom(), SettingHelper.getName(HcApplication.getContext()), false);
        }
    }

    /**
     * 获取群聊的信息,不包括具体的成员列表
     * @param room
     * @return
     */
    public RoomInfo getRoomInfo(String room, String node) {
        RoomInfo info = null;
        if (mConnection.isConnected()) {
            try {
                info = MultiUserChat.getRoomInfo(mConnection, room, node);
            } catch(Exception e) {
                HcLog.D(TAG + "#getRoomInfo room = "+room + " error = "+e);
            }

        } else {
            createXMPPSocket();
        }
        return info;
    }

    /**
     * 重新发送消息,线程不安全的,注意为了用户体验
     * <p>注意,这里@的功能没有重发</p>
     * @param info 发送的消息
     * @param jid 接收方的jid,可能为群的jid
     * @param group 是否是群聊
     */
    public void reSendMessage(ChatMessageInfo info, String jid, boolean group) {
        if (info.getType() == 1) { // 文本
            sendMessage(info, info.getContent(), jid, group, info.getUserId(), info.getReceiver());
        } else if (info.getType() == 2) { // 图片
            sendMediaMessage(info, info.getContent(), jid, IMUtil.encodeBase64File(info.getFilePath()), "" + info.getType(), "", group, info.getUserId());
        } else if (info.getType() == 3) { // 语音
            sendMediaMessage(info, info.getContent(), jid, IMUtil.encodeBase64File(info.getFilePath()), "" + info.getType(), "" + info.getDuration(), group, info.getUserId());
        } else {
            HcLog.D(TAG + " #reSendMessage jid = "+jid + " group = "+group);
        }

    }

    /**
     * test
     * @param chat
     */
    private void testSendSystemMessage(Chat chat) {
        Message newMessage = new Message();
        String body = "{\"title\":\"考勤签到\",\"description\":\"签到提醒\",\"body\":{\"appid\":\"255\"}}";
        newMessage.setBody(body);
        try {
            chat.sendSystemMessage(newMessage);
        } catch(Exception e) {

        }

    }
}
