
package com.eurekao.easemob.im;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.eurekao.easemob.im.common.ResourceUtil;
import com.eurekao.easemob.im.common.log.LogUtil;
import com.eurekao.easemob.im.db.DBManager;
import com.eurekao.easemob.im.db.UserDao;
import com.eurekao.easemob.im.domain.EaseUser;
import com.eurekao.easemob.im.permission.MPermission;
import com.eurekao.easemob.im.permission.annotation.OnMPermissionDenied;
import com.eurekao.easemob.im.permission.annotation.OnMPermissionGranted;
import com.eurekao.easemob.im.permission.annotation.OnMPermissionNeverAskAgain;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;

import java.util.ArrayList;
import java.util.List;


public class RNEasemobImModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    final static int BASIC_PERMISSION_REQUEST_CODE = 100;
    private final static String TAG = "RNEasemobIm";
    private final static String NAME = "RNEasemobIm";
    private final ReactApplicationContext reactContext;
    private Handler handler = new Handler(Looper.getMainLooper());
    protected EMConversation conversation;
    protected int pagesize = 20;
    protected EMMessageListener messageListener = null;
    protected String toChatUsername;

    public RNEasemobImModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);
        ImModel.setReactContext(reactContext);
        registerMessageListener();
    }

    boolean string2Boolean(String bool) {
        return TextUtils.isEmpty(bool) ? false : !"0".equals(bool);
    }

    @Override
    public void initialize() {
        LogUtil.w(TAG, "initialize");
    }

    @Override
    public void onCatalystInstanceDestroy() {
        LogUtil.w(TAG, "onCatalystInstanceDestroy");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void init(Promise promise) {
        LogUtil.w(TAG, "init");
        promise.resolve("200");
    }

    @ReactMethod
    public void login(String contactId, String token, final Promise promise) {
        LogUtil.w(TAG, "_id:" + contactId);
        LogUtil.w(TAG, "t:" + token);

        if (!EaseCommonUtils.isNetWorkConnected(reactContext.getApplicationContext())) {
            Toast.makeText(reactContext.getApplicationContext(), R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        DBManager.getInstance().closeDB();
        IMApplication.getInstance().setCurrentUserName(contactId);
        final long start = System.currentTimeMillis();
        // call login method
        LogUtil.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(contactId, token, new EMCallBack() {

            @Override
            public void onSuccess() {
                LogUtil.d(TAG, "login: onSuccess");

                // ** manually load all local groups and conversation
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();

                // update current user's display name for APNs
                boolean updatenick = EMClient.getInstance().pushManager().updatePushNickname(IMApplication.currentUserNick.trim());
                if (!updatenick) {
                    LogUtil.e("Login", "update current user nick fail");
                }

                // get user's info (this should be get from App's server or 3rd party service)
                IMApplication.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

                promise.resolve(IMApplication.getInstance().getUserProfileManager().getCurrentUserInfo());
            }

            @Override
            public void onProgress(int progress, String status) {
                LogUtil.d(TAG, "login: onProgress");
            }

            @Override
            public void onError(final int code, final String message) {
                LogUtil.d(TAG, "login: onError: " + code);
                promise.reject(Integer.toString(code), ResourceUtil.getString(R.string.Login_failed));
            }
        });
    }

    @ReactMethod
    public void logout(final Promise promise) {
        LogUtil.w(TAG, "logout");
        IMApplication.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                promise.resolve("success");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                promise.reject(Integer.toString(code), message);
            }
        });
    }

    @ReactMethod
    public void getUserInfo(String contactId, final Promise promise) {
        LogUtil.w(TAG, "getUserInfo" + contactId);
        IMApplication.getInstance().getUserProfileManager().asyncGetUserInfo(contactId, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser value) {
                promise.resolve(value);
            }

            @Override
            public void onError(int code, String message) {
                promise.reject(Integer.toString(code), message);
            }
        });
    }

    @ReactMethod
    public void setMessageNotify(String mute, final Promise promise) {
        IMApplication.getImModel().setSettingMsgNotification(string2Boolean(mute));
        promise.resolve("");
    }

    @ReactMethod
    public void setMessageSound(String mute, final Promise promise) {
        IMApplication.getImModel().setSettingMsgSound(string2Boolean(mute));
        promise.resolve("");
    }

    @ReactMethod
    public void setMessageVibrate(String mute, final Promise promise) {
        IMApplication.getImModel().setSettingMsgVibrate(string2Boolean(mute));
        promise.resolve("");
    }

    @ReactMethod
    public void sendTextMessage(String content, final Promise promise) {
        LogUtil.w(TAG, "sendTextMessage" + content);
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    @ReactMethod
    public void sendImageMessage(String file, final Promise promise) {
        EMMessage message = EMMessage.createImageSendMessage(file, false, toChatUsername);
        sendMessage(message);
    }

    @ReactMethod
    public void sendAudioMessage(String file, String duration, final Promise promise) {
        int durationI = 0;
        try {
            durationI = Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        EMMessage message = EMMessage.createVoiceSendMessage(file, durationI, toChatUsername);
        sendMessage(message);
    }

    @ReactMethod
    public void sendLocationMessage(String latitude, String longitude, String address, final Promise promise) {
        double lat = 23.12504;
        try {
            lat = Double.parseDouble(latitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        double lon = 113.327474;
        try {
            lon = Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        EMMessage message = EMMessage.createLocationSendMessage(lat, lon, address, toChatUsername);
        sendMessage(message);
    }

    protected void sendMessage(EMMessage message){
        if (message == null) {
            return;
        }
        //send message
        EMClient.getInstance().chatManager().sendMessage(message);
        List<EMMessage> toShow = new ArrayList<EMMessage>();
        toShow.add(message);
        ImModel.emit(ImModel.observeReceiveMessage, ImModel.createMessageList(toShow));
    }

    @ReactMethod
    public void deleteRecentContact(String rContactId, Promise promise) {
        LogUtil.w(TAG, "deleteRecentContact" + rContactId);
        try {
            EMClient.getInstance().contactManager().deleteContact(rContactId);
            // remove user from memory and database
            UserDao dao = new UserDao(reactContext.getApplicationContext());
            dao.deleteContact(rContactId);
            IMApplication.getInstance().getContactList().remove(rContactId);
            promise.resolve("");
        } catch (final Exception e) {
            promise.reject("-1", ResourceUtil.getString(R.string.Delete_failed) + e.getMessage());
        }
    }

    @ReactMethod
    public void getRecentConversationList(final Promise promise) {
        List<EMConversation> list =  IMApplication.getImModel().loadConversationList();
        if (list.size() > 0)
            promise.resolve(ImModel.createRecentList(list, 0));
        else {
            promise.reject("-1", "");
        }
    }

    @ReactMethod
    public void startSession(String contactId, final Promise promise) {
        LogUtil.w(TAG, "startSession" + contactId);
        if (TextUtils.isEmpty(contactId)) {
            return;
        }
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
        toChatUsername = contactId;
        conversation = EMClient.getInstance().chatManager().getConversation(contactId, EMConversation.EMConversationType.Chat, true);
        conversation.markAllMessagesAsRead();
    }

    @ReactMethod
    public void stopSession(final Promise promise) {
        LogUtil.w(TAG, "stopSession");
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        conversation = null;
        toChatUsername = null;
    }

    protected void registerMessageListener() {
        messageListener = new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                List<EMMessage> toShow = new ArrayList<EMMessage>();
                for (EMMessage message : messages) {
                    String username = null;
                    // group message
                    if (message.getChatType() == ChatType.GroupChat || message.getChatType() == ChatType.ChatRoom) {
                        username = message.getTo();
                    } else {
                        // single chat message
                        username = message.getFrom();
                    }

                    // if the message is for current conversation
                    if (username.equals(toChatUsername) || message.getTo().equals(toChatUsername) || message.conversationId().equals(toChatUsername)) {
                        conversation.markMessageAsRead(message.getMsgId());
                        toShow.add(message);
                    }
                }
                if (toShow.size() > 0)
                    ImModel.emit(ImModel.observeReceiveMessage, ImModel.createMessageList(toShow));
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {}
            @Override
            public void onMessageRead(List<EMMessage> messages) {}
            @Override
            public void onMessageDelivered(List<EMMessage> messages) { }
            @Override
            public void onMessageRecalled(List<EMMessage> messages) {}
            @Override
            public void onMessageChanged(EMMessage emMessage, Object change) { }
        };
    }

    @ReactMethod
    public void queryMessageListEx(String messageId, final int limit, final Promise promise) {
        LogUtil.w(TAG, "queryMessageListEx:" + messageId + "(" + limit + ")");
        List<EMMessage> messages;
        try {
            messages = conversation.loadMoreMsgFromDB(messageId, limit > 0 ? limit : pagesize);
            promise.resolve(ImModel.createMessageList(messages));
        } catch (Exception e1) {
            promise.reject("-1", "");
        }
    }

    /**
     * 基本权限管理
     */
    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, getCurrentActivity(), BASIC_PERMISSIONS);
        MPermission.with(getCurrentActivity())
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        Toast.makeText(getCurrentActivity(), "授权成功", Toast.LENGTH_SHORT).show();
        MPermission.printMPermissionResult(false, getCurrentActivity(), BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(getCurrentActivity(), "未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        MPermission.printMPermissionResult(false, getCurrentActivity(), BASIC_PERMISSIONS);
    }


    void showTip(final String tip) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(reactContext.getCurrentActivity(), tip, Toast.LENGTH_SHORT).show();
            }
        });

    }

    void showTip(final int tipId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(reactContext.getCurrentActivity(), reactContext.getString(tipId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        LogUtil.w(TAG, "onActivityResult:" + requestCode + "-result:" + resultCode);
    }

    @Override
    public void onNewIntent(Intent intent) {
        LogUtil.w(TAG, "onNewIntent:" + intent.getExtras());
    }

    public static String status = "";
    public static Intent launch = null;

    @Override
    public void onHostResume() {
        LogUtil.w(TAG, "onHostResume:" + status);
        status = "";
    }

    @Override
    public void onHostPause() {
        if (TextUtils.isEmpty(status)) {
            status = "onHostPause";
        }
        LogUtil.w(TAG, "onHostPause");
    }

    @Override
    public void onHostDestroy() {
        LogUtil.w(TAG, "onHostDestroy");
    }
}