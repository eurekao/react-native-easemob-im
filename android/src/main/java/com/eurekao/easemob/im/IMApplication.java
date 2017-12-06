package com.eurekao.easemob.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.eurekao.easemob.im.common.crash.AppCrashHandler;
import com.eurekao.easemob.im.db.DBManager;
import com.eurekao.easemob.im.db.UserDao;
import com.eurekao.easemob.im.domain.EaseUser;
import com.eurekao.easemob.im.parse.UserProfileManager;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IMApplication {

    public interface DataSyncListener {
        void onSyncComplete(boolean success);
    }

    protected static final String TAG = "IMApplication";

    private static ImModel imModel = null;
    private static Context context;
    private static Class mainActivityClass;
    private static IMApplication instance = null;
    private static LocalBroadcastManager broadcastManager;
    public static String currentUserNick = "";

    Queue<String> msgQueue = new ConcurrentLinkedQueue<>();
    android.os.Handler handler;
    EMConnectionListener connectionListener;
    private List<DataSyncListener> syncGroupsListeners;
    private List<DataSyncListener> syncContactsListeners;
    private List<DataSyncListener> syncBlackListListeners;

    private boolean isSyncingGroupsWithServer = false;
    private boolean isSyncingContactsWithServer = false;
    private boolean isSyncingBlackListWithServer = false;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    private boolean isGroupAndContactListenerRegisted;
    protected EMMessageListener messageListener = null;
    private String username;
    private Map<String, EaseUser> contactList;
    private UserProfileManager userProManager;
    private EaseNotifier notifier = null;


    public EaseNotifier getNotifier(){
        if (notifier == null) {
            notifier = new EaseNotifier();
            notifier.init(context);
        }
        return notifier;
    }
    public synchronized static IMApplication getInstance() {
        if (instance == null) {
            instance = new IMApplication();
        }
        return instance;
    }

    public static ImModel getImModel(){
        return imModel;
    }

    public static void init(Context context, Class mainActivityClass, Looper looper) {
        IMApplication.context = context.getApplicationContext();
        IMApplication.mainActivityClass = mainActivityClass;
        getInstance().initHandler(looper);
        AppCrashHandler.getInstance(context);
        imModel = new ImModel(context);
        EMOptions options = initChatOptions();
        EMClient.getInstance().init(context, options);
        EMClient.getInstance().setDebugMode(true);
        PreferenceManager.init(context);
        getInstance().getUserProfileManager().init(context);
        broadcastManager = LocalBroadcastManager.getInstance(context);
        getInstance().setGlobalListeners();
    }


    private static EMOptions initChatOptions() {
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // set if accept the invitation automatically
        options.setAcceptInvitationAlways(false);
        // set if you need read ack
        options.setRequireAck(true);
        // set if you need delivery ack
        options.setRequireDeliveryAck(false);

        //you need apply & set your own id if you want to use google cloud messaging.
        //options.setGCMNumber("324169311137");
        //you need apply & set your own id if you want to use Mi push notification
        //options.setMipushConfig("2882303761517426801", "5381742660801");

        //set custom servers, commonly used in private deployment
        if (imModel.isCustomServerEnable() && imModel.getRestServer() != null && imModel.getIMServer() != null) {
            options.setRestServer(imModel.getRestServer());
            options.setIMServer(imModel.getIMServer());
            if (imModel.getIMServer().contains(":")) {
                options.setIMServer(imModel.getIMServer().split(":")[0]);
                options.setImPort(Integer.valueOf(imModel.getIMServer().split(":")[1]));
            }
        }

        if (imModel.isCustomAppkeyEnabled() && imModel.getCutomAppkey() != null && !imModel.getCutomAppkey().isEmpty()) {
            options.setAppKey(imModel.getCutomAppkey());
        }

        options.allowChatroomOwnerLeave(imModel.isChatroomOwnerLeaveAllowed());
        options.setDeleteMessagesAsExitGroup(imModel.isDeleteMessagesAsExitGroup());
        options.setAutoAcceptGroupInvitation(imModel.isAutoAcceptGroupInvitation());

        return options;
    }

    public void showToast(final String message) {
        Log.d(TAG, "receive invitation to join the group：" + message);
        if (handler != null) {
            Message msg = Message.obtain(handler, 0, message);
            handler.sendMessage(msg);
        } else {
            msgQueue.add(message);
        }
    }


    public void initHandler(Looper looper) {
        handler = new android.os.Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                String str = (String) msg.obj;
                Toast.makeText(context, str, Toast.LENGTH_LONG).show();
            }
        };
        while (!msgQueue.isEmpty()) {
            showToast(msgQueue.remove());
        }
    }

    protected void setGlobalListeners() {
        syncGroupsListeners = new ArrayList<>();
        syncContactsListeners = new ArrayList<>();
        syncBlackListListeners = new ArrayList<>();

        isGroupsSyncedWithServer = imModel.isGroupsSynced();
        isContactsSyncedWithServer = imModel.isContactSynced();
        isBlackListSyncedWithServer = imModel.isBacklistSynced();

        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                EMLog.d("global listener", "onDisconnect" + error);
                if (error == EMError.USER_REMOVED) {
                    onUserException(Constant.ACCOUNT_REMOVED);
                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onUserException(Constant.ACCOUNT_CONFLICT);
                } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
                    onUserException(Constant.ACCOUNT_FORBIDDEN);
                } else if (error == EMError.USER_KICKED_BY_CHANGE_PASSWORD) {
                    onUserException(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD);
                } else if (error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
                    onUserException(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE);
                }
            }

            @Override
            public void onConnected() {
                // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
                if (isGroupsSyncedWithServer && isContactsSyncedWithServer) {
                    EMLog.d(TAG, "group and contact already synced with servre");
                } else {
                    if (!isGroupsSyncedWithServer) {
                        asyncFetchGroupsFromServer(null);
                    }

                    if (!isContactsSyncedWithServer) {
                        asyncFetchContactsFromServer(null);
                    }


                    if (!isBlackListSyncedWithServer) {
                        asyncFetchBlackListFromServer(null);
                    }
                }
            }
        };

        EMClient.getInstance().addConnectionListener(connectionListener);
        //register group and contact event listener
        registerGroupAndContactListener();
        //register message event listener
        registerMessageListener();
    }

    protected void registerMessageListener() {
        messageListener = new EMMessageListener() {
            private BroadcastReceiver broadCastReceiver = null;

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    // in background, do not refresh UI, notify it in notification bar
                    getNotifier().onNewMsg(message);
                }
                ImModel.emit(ImModel.observeRecentContact, ImModel.createRecentList(imModel.loadConversationList(), 0));
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "receive command message");
                    //get message body
                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();//获取自定义action

                    if (action.equals("__Call_ReqP2P_ConferencePattern")) {
                        String title = message.getStringAttribute("em_apns_ext", "conference call");
                        Toast.makeText(context, title, Toast.LENGTH_LONG).show();
                    }
                    //end of red packet code
                    //获取扩展属性 此处省略
                    //maybe you need get extension of your message
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("Command：action:%s,message:%s", action, message.toString()));
                }
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
            }

            @Override
            public void onMessageRecalled(List<EMMessage> messages) {
                for (EMMessage msg : messages) {
                    EMMessage msgNotification = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    EMTextMessageBody txtBody = new EMTextMessageBody(String.format(context.getString(R.string.msg_recall_by_user), msg.getFrom()));
                    msgNotification.addBody(txtBody);
                    msgNotification.setFrom(msg.getFrom());
                    msgNotification.setTo(msg.getTo());
                    msgNotification.setUnread(false);
                    msgNotification.setMsgTime(msg.getMsgTime());
                    msgNotification.setLocalTime(msg.getMsgTime());
                    msgNotification.setChatType(msg.getChatType());
                    msgNotification.setAttribute(Constant.MESSAGE_TYPE_RECALL, true);
                    EMClient.getInstance().chatManager().saveMessage(msgNotification);
                }
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                EMLog.d(TAG, "change:");
                EMLog.d(TAG, "change:" + change);
            }
        };

        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    public void registerGroupAndContactListener() {
        if (!isGroupAndContactListenerRegisted) {
            // EMClient.getInstance().groupManager().addGroupChangeListener(new MyGroupChangeListener());
            // EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
            // EMClient.getInstance().addMultiDeviceListener(new MyMultiDeviceListener());
            isGroupAndContactListenerRegisted = true;
        }

    }

    public synchronized void asyncFetchGroupsFromServer(final EMCallBack callback) {
        if (isSyncingGroupsWithServer) {
            return;
        }

        isSyncingGroupsWithServer = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    List<EMGroup> groups = EMClient.getInstance().groupManager().getJoinedGroupsFromServer();

                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isGroupsSyncedWithServer = false;
                        isSyncingGroupsWithServer = false;
                        noitifyGroupSyncListeners(false);
                        return;
                    }

                    imModel.setGroupsSynced(true);
                    isGroupsSyncedWithServer = true;
                    isSyncingGroupsWithServer = false;
                    noitifyGroupSyncListeners(true);

                    if (callback != null) {
                        callback.onSuccess();
                    }
                } catch (HyphenateException e) {
                    imModel.setGroupsSynced(false);
                    isGroupsSyncedWithServer = false;
                    isSyncingGroupsWithServer = false;
                    noitifyGroupSyncListeners(false);
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }

    public void noitifyGroupSyncListeners(boolean success) {
        for (DataSyncListener listener : syncGroupsListeners) {
            listener.onSyncComplete(success);
        }
    }

    public void asyncFetchContactsFromServer(final EMValueCallBack<List<String>> callback) {
        if (isSyncingContactsWithServer) {
            return;
        }
        isSyncingContactsWithServer = true;
        new Thread() {
            @Override
            public void run() {
                List<String> usernames = null;
                List<String> selfIds = null;
                try {
                    usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    selfIds = EMClient.getInstance().contactManager().getSelfIdsOnOtherPlatform();
                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isContactsSyncedWithServer = false;
                        isSyncingContactsWithServer = false;
                        notifyContactsSyncListener(false);
                        return;
                    }
                    if (selfIds.size() > 0) {
                        usernames.addAll(selfIds);
                    }
                    Map<String, EaseUser> userlist = new HashMap<String, EaseUser>();
                    for (String username : usernames) {
                        EaseUser user = new EaseUser(username);
                        EaseCommonUtils.setUserInitialLetter(user);
                        userlist.put(username, user);
                    }
                    // save the contact list to cache
                    getContactList().clear();
                    getContactList().putAll(userlist);
                    // save the contact list to database
                    UserDao dao = new UserDao(IMApplication.getContext());
                    List<EaseUser> users = new ArrayList<EaseUser>(userlist.values());
                    dao.saveContactList(users);

                    imModel.setContactSynced(true);
                    EMLog.d(TAG, "set contact syn status to true");

                    isContactsSyncedWithServer = true;
                    isSyncingContactsWithServer = false;

                    //notify sync success
                    notifyContactsSyncListener(true);

                    getUserProfileManager().asyncFetchContactInfosFromServer(usernames, new EMValueCallBack<List<EaseUser>>() {

                        @Override
                        public void onSuccess(List<EaseUser> uList) {
                            updateContactList(uList);
                            getUserProfileManager().notifyContactInfosSyncListener(true);
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                        }
                    });
                    if (callback != null) {
                        callback.onSuccess(usernames);
                    }
                } catch (HyphenateException e) {
                    imModel.setContactSynced(false);
                    isContactsSyncedWithServer = false;
                    isSyncingContactsWithServer = false;
                    notifyContactsSyncListener(false);
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }

    public void notifyContactsSyncListener(boolean success){
        for (DataSyncListener listener : syncContactsListeners) {
            listener.onSyncComplete(success);
        }
    }

    public void asyncFetchBlackListFromServer(final EMValueCallBack<List<String>> callback) {

        if (isSyncingBlackListWithServer) {
            return;
        }

        isSyncingBlackListWithServer = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    List<String> usernames = EMClient.getInstance().contactManager().getBlackListFromServer();

                    // in case that logout already before server returns, we should return immediately
                    if (!isLoggedIn()) {
                        isBlackListSyncedWithServer = false;
                        isSyncingBlackListWithServer = false;
                        notifyBlackListSyncListener(false);
                        return;
                    }

                    imModel.setBlacklistSynced(true);

                    isBlackListSyncedWithServer = true;
                    isSyncingBlackListWithServer = false;

                    notifyBlackListSyncListener(true);
                    if (callback != null) {
                        callback.onSuccess(usernames);
                    }
                } catch (HyphenateException e) {
                    imModel.setBlacklistSynced(false);

                    isBlackListSyncedWithServer = false;
                    isSyncingBlackListWithServer = true;
                    e.printStackTrace();

                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }

            }
        }.start();
    }

    public void notifyBlackListSyncListener(boolean success) {
        for (DataSyncListener listener : syncBlackListListeners) {
            listener.onSyncComplete(success);
        }
    }

    protected void onUserException(String exception) {
        EMLog.e(TAG, "onUserException: " + exception);
        showToast(exception);
    }

    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
        Log.d(TAG, "logout: " + unbindDeviceToken);
        EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "logout: onSuccess");
                reset();
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                Log.d(TAG, "logout: onSuccess");
                reset();
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    public void setCurrentUserName(String username) {
        this.username = username;
        imModel.setCurrentUserName(username);
    }

    public String getCurrentUsernName() {
        if (username == null) {
            username = imModel.getCurrentUsernName();
        }
        return username;
    }

    public void setContactList(Map<String, EaseUser> aContactList) {
        if(aContactList == null){
            if (contactList != null) {
                contactList.clear();
            }
            return;
        }
        contactList = aContactList;
    }

    public void saveContact(EaseUser user){
        contactList.put(user.getUsername(), user);
        imModel.saveContact(user);
    }

    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            contactList = imModel.getContactList();
        }

        // return a empty non-null object to avoid app crash
        if(contactList == null){
            return new Hashtable<String, EaseUser>();
        }

        return contactList;
    }

    public boolean isSpeakerOpened() {
        return imModel.getSettingMsgSpeaker();
    }

    public boolean isMsgVibrateAllowed(EMMessage message) {
        return imModel.getSettingMsgVibrate();
    }

    public boolean isMsgSoundAllowed(EMMessage message) {
        return imModel.getSettingMsgSound();
    }

    public boolean isMsgNotifyAllowed(EMMessage message) {
        if(message == null){
            return imModel.getSettingMsgNotification();
        }
        if(!imModel.getSettingMsgNotification()){
            return false;
        }else{
            String chatUsename = null;
            List<String> notNotifyIds = null;
            // get user or group id which was blocked to show message notifications
            if (message.getChatType() == EMMessage.ChatType.Chat) {
                chatUsename = message.getFrom();
                notNotifyIds = imModel.getDisabledIds();
            } else {
                chatUsename = message.getTo();
                notNotifyIds = imModel.getDisabledGroups();
            }

            if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                return true;
            } else {
                return false;
            }
        }
    }

    synchronized void reset() {
        username = null;
        isSyncingGroupsWithServer = false;
        isSyncingContactsWithServer = false;
        isSyncingBlackListWithServer = false;

        imModel.setGroupsSynced(false);
        imModel.setContactSynced(false);
        imModel.setBlacklistSynced(false);

        isGroupsSyncedWithServer = false;
        isContactsSyncedWithServer = false;
        isBlackListSyncedWithServer = false;

        isGroupAndContactListenerRegisted = false;

        setContactList(null);
        getUserProfileManager().reset();
        DBManager.getInstance().closeDB();
    }

    public void updateContactList(List<EaseUser> contactInfoList) {
        for (EaseUser u : contactInfoList) {
            contactList.put(u.getUsername(), u);
        }
        ArrayList<EaseUser> mList = new ArrayList<EaseUser>();
        mList.addAll(contactList.values());
        imModel.saveContactList(mList);
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
    }

    public static Context getContext() {
        return context;
    }

    public static Class getMainActivityClass() {
        return mainActivityClass;
    }

    public class Constant {

        // EaseUI 中使用SharedPreference 保存信息的文件名
        public static final String EASEUI_SHARED_NAME = "ease_shared";

        /**
         * 设置自己扩展的消息类型的 key
         */
        // 通用的 msg_id key
        public static final String EASE_ATTR_MSG_ID = "em_msg_id";
        // 需要撤销的消息的 msg_id key
        public static final String EASE_ATTR_REVOKE_MSG_ID = "em_revoke_messageId";
        // 撤回消息的 key
        public static final String EASE_ATTR_REVOKE = "em_revoke";
        // 阅后即焚的 key
        public static final String EASE_ATTR_READFIRE = "em_readFire";

        public static final String MESSAGE_ATTR_IS_VOICE_CALL = "is_voice_call";
        public static final String MESSAGE_ATTR_IS_VIDEO_CALL = "is_video_call";

        public static final String MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression";
        public static final String MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id";
        public static final int CHATTYPE_SINGLE = 1;
        public static final int CHATTYPE_GROUP = 2;
        public static final int CHATTYPE_CHATROOM = 3;

        public static final String EXTRA_CHAT_TYPE = "chatType";
        public static final String EXTRA_USER_ID = "userId";

        public static final String NEW_FRIENDS_USERNAME = "item_new_friends";
        public static final String GROUP_USERNAME = "item_groups";
        public static final String CHAT_ROOM = "item_chatroom";
        public static final String ACCOUNT_REMOVED = "account_removed";
        public static final String ACCOUNT_CONFLICT = "conflict";
        public static final String ACCOUNT_FORBIDDEN = "user_forbidden";
        public static final String ACCOUNT_KICKED_BY_CHANGE_PASSWORD = "kicked_by_change_password";
        public static final String ACCOUNT_KICKED_BY_OTHER_DEVICE = "kicked_by_another_device";
        public static final String CHAT_ROBOT = "item_robots";
        public static final String MESSAGE_ATTR_ROBOT_MSGTYPE = "msgtype";
        public static final String ACTION_GROUP_CHANAGED = "action_group_changed";
        public static final String ACTION_CONTACT_CHANAGED = "action_contact_changed";
        public static final String MESSAGE_TYPE_RECALL = "recall";
    }
}
