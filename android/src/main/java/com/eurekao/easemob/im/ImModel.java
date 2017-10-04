package com.eurekao.easemob.im;

import android.content.Context;
import android.util.Pair;

import com.eurekao.easemob.im.common.log.LogUtil;
import com.eurekao.easemob.im.db.UserDao;
import com.eurekao.easemob.im.domain.EaseUser;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.Status;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImModel {
    protected static final String TAG = "ImModel";
    public final static String observeRecentContact = "observeRecentContact";//'最近会话'
    public final static String observeReceiveMessage = "observeReceiveMessage";//'接收消息'

    UserDao dao = null;
    protected Context context = null;
    protected Map<Key,Object> valueCache = new HashMap<Key,Object>();

    private static ReactContext reactContext;

    public static void setReactContext(ReactContext reactContext) {
        ImModel.reactContext = reactContext;
    }

    public static ReactContext getReactContext() {
        return reactContext;
    }

    public static void emit(String eventName, Object date) {
        try {
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImModel(Context ctx){
        context = ctx;
        PreferenceManager.init(context);
    }

    public boolean saveContactList(List<EaseUser> contactList) {
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, EaseUser> getContactList() {
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }

    public void saveContact(EaseUser user){
        UserDao dao = new UserDao(context);
        dao.saveContact(user);
    }

    public void setCurrentUserName(String username){
        PreferenceManager.getInstance().setCurrentUserName(username);
    }

    public String getCurrentUsernName(){
        return PreferenceManager.getInstance().getCurrentUsername();
    }

    public void setSettingMsgNotification(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgNotification(paramBoolean);
        valueCache.put(Key.VibrateAndPlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgNotification() {
        Object val = valueCache.get(Key.VibrateAndPlayToneOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgNotification();
            valueCache.put(Key.VibrateAndPlayToneOn, val);
        }

        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgSound(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSound(paramBoolean);
        valueCache.put(Key.PlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgSound() {
        Object val = valueCache.get(Key.PlayToneOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgSound();
            valueCache.put(Key.PlayToneOn, val);
        }

        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgVibrate(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgVibrate(paramBoolean);
        valueCache.put(Key.VibrateOn, paramBoolean);
    }

    public boolean getSettingMsgVibrate() {
        Object val = valueCache.get(Key.VibrateOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgVibrate();
            valueCache.put(Key.VibrateOn, val);
        }

        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgSpeaker(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSpeaker(paramBoolean);
        valueCache.put(Key.SpakerOn, paramBoolean);
    }

    public boolean getSettingMsgSpeaker() {
        Object val = valueCache.get(Key.SpakerOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgSpeaker();
            valueCache.put(Key.SpakerOn, val);
        }

        return (Boolean) (val != null?val:true);
    }



    public void setDisabledGroups(List<String> groups){
        if(dao == null){
            dao = new UserDao(context);
        }

        List<String> list = new ArrayList<String>();
        list.addAll(groups);
        dao.setDisabledGroups(list);
        valueCache.put(Key.DisabledGroups, list);
    }

    public List<String> getDisabledGroups(){
        Object val = valueCache.get(Key.DisabledGroups);

        if(dao == null){
            dao = new UserDao(context);
        }

        if(val == null){
            val = dao.getDisabledGroups();
            valueCache.put(Key.DisabledGroups, val);
        }

        //noinspection unchecked
        return (List<String>) val;
    }

    public void setDisabledIds(List<String> ids){
        if(dao == null){
            dao = new UserDao(context);
        }

        dao.setDisabledIds(ids);
        valueCache.put(Key.DisabledIds, ids);
    }

    public List<String> getDisabledIds(){
        Object val = valueCache.get(Key.DisabledIds);

        if(dao == null){
            dao = new UserDao(context);
        }

        if(val == null){
            val = dao.getDisabledIds();
            valueCache.put(Key.DisabledIds, val);
        }

        //noinspection unchecked
        return (List<String>) val;
    }

    public void setGroupsSynced(boolean synced){
        PreferenceManager.getInstance().setGroupsSynced(synced);
    }

    public boolean isGroupsSynced(){
        return PreferenceManager.getInstance().isGroupsSynced();
    }

    public void setContactSynced(boolean synced){
        PreferenceManager.getInstance().setContactSynced(synced);
    }

    public boolean isContactSynced(){
        return PreferenceManager.getInstance().isContactSynced();
    }

    public void setBlacklistSynced(boolean synced){
        PreferenceManager.getInstance().setBlacklistSynced(synced);
    }

    public boolean isBacklistSynced(){
        return PreferenceManager.getInstance().isBacklistSynced();
    }

    public void allowChatroomOwnerLeave(boolean value){
        PreferenceManager.getInstance().setSettingAllowChatroomOwnerLeave(value);
    }

    public boolean isChatroomOwnerLeaveAllowed(){
        return PreferenceManager.getInstance().getSettingAllowChatroomOwnerLeave();
    }

    public void setDeleteMessagesAsExitGroup(boolean value) {
        PreferenceManager.getInstance().setDeleteMessagesAsExitGroup(value);
    }

    public boolean isDeleteMessagesAsExitGroup() {
        return PreferenceManager.getInstance().isDeleteMessagesAsExitGroup();
    }

    public void setAutoAcceptGroupInvitation(boolean value) {
        PreferenceManager.getInstance().setAutoAcceptGroupInvitation(value);
    }

    public boolean isAutoAcceptGroupInvitation() {
        return PreferenceManager.getInstance().isAutoAcceptGroupInvitation();
    }


    public void setAdaptiveVideoEncode(boolean value) {
        PreferenceManager.getInstance().setAdaptiveVideoEncode(value);
    }

    public boolean isAdaptiveVideoEncode() {
        return PreferenceManager.getInstance().isAdaptiveVideoEncode();
    }

    public void setPushCall(boolean value) {
        PreferenceManager.getInstance().setPushCall(value);
    }

    public boolean isPushCall() {
        return PreferenceManager.getInstance().isPushCall();
    }

    public void setRestServer(String restServer){
        PreferenceManager.getInstance().setRestServer(restServer);
    }

    public String getRestServer(){
        return  PreferenceManager.getInstance().getRestServer();
    }

    public void setIMServer(String imServer){
        PreferenceManager.getInstance().setIMServer(imServer);
    }

    public String getIMServer(){
        return PreferenceManager.getInstance().getIMServer();
    }

    public void enableCustomServer(boolean enable){
        PreferenceManager.getInstance().enableCustomServer(enable);
    }

    public boolean isCustomServerEnable(){
        return PreferenceManager.getInstance().isCustomServerEnable();
    }

    public void enableCustomAppkey(boolean enable) {
        PreferenceManager.getInstance().enableCustomAppkey(enable);
    }

    public boolean isCustomAppkeyEnabled() {
        return PreferenceManager.getInstance().isCustomAppkeyEnabled();
    }

    public void setCustomAppkey(String appkey) {
        PreferenceManager.getInstance().setCustomAppkey(appkey);
    }

    public boolean isMsgRoaming() {
        return PreferenceManager.getInstance().isMsgRoaming();
    }

    public void setMsgRoaming(boolean roaming) {
        PreferenceManager.getInstance().setMsgRoaming(roaming);
    }

    public String getCutomAppkey() {
        return PreferenceManager.getInstance().getCustomAppkey();
    }


    private void sortConversationByLastChatTime(List<Pair<Long, EMConversation>> conversationList) {
        Collections.sort(conversationList, new Comparator<Pair<Long, EMConversation>>() {
            @Override
            public int compare(final Pair<Long, EMConversation> con1, final Pair<Long, EMConversation> con2) {

                if (con1.first.equals(con2.first)) {
                    return 0;
                } else if (con2.first.longValue() > con1.first.longValue()) {
                    return 1;
                } else {
                    return -1;
                }
            }

        });
    }

    protected List<EMConversation> loadConversationList(){
        // get all conversations
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        List<Pair<Long, EMConversation>> sortList = new ArrayList<Pair<Long, EMConversation>>();
        synchronized (conversations) {
            for (EMConversation conversation : conversations.values()) {
                if (conversation.getAllMessages().size() != 0) {
                    sortList.add(new Pair<Long, EMConversation>(conversation.getLastMessage().getMsgTime(), conversation));
                }
            }
        }
        try {
            sortConversationByLastChatTime(sortList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<EMConversation> list = new ArrayList<EMConversation>();
        for (Pair<Long, EMConversation> sortItem : sortList) {
            list.add(sortItem.second);
        }
        return list;
    }

    public static Object createRecentList(List<EMConversation> recents, int unreadNum) {
        LogUtil.w(TAG, "size:" + (recents == null ? 0 : recents.size()));
        WritableMap writableMap = Arguments.createMap();
        WritableArray array = Arguments.createArray();
        int unreadNumTotal = 0;
        if (recents != null && recents.size() > 0) {
            WritableMap map;
            for (EMConversation conversation : recents) {
                map = Arguments.createMap();
                String username = conversation.conversationId();
                unreadNumTotal += conversation.getUnreadMsgCount();
                map.putString("username", username);
                map.putString("unreadCount", String.valueOf(conversation.getUnreadMsgCount()));
                if (conversation.getAllMsgCount() != 0) {
                    EMMessage lastMessage = conversation.getLastMessage();
                    String content = EaseCommonUtils.getMessageDigest(lastMessage);
                    map.putString("content", content);
                    map.putString("time", DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
                }
                array.pushMap(map);
            }
        }
        writableMap.putArray("recents", array);
        writableMap.putString("unreadCount", Integer.toString(unreadNumTotal));
        return writableMap;
    }

    public static Object createMessageList(List<EMMessage> messageList) {
        WritableArray writableArray = Arguments.createArray();
        if (messageList != null) {
            int size = messageList.size();
            for (int i = 0; i < size; i++) {
                EMMessage item = messageList.get(i);
                if (item != null) {
                    WritableMap itemMap = createMessage(item);
                    if (itemMap != null) {
                        writableArray.pushMap(itemMap);
                    }
                }
            }
        }
        return writableArray;
    }

    public static WritableMap createMessage(EMMessage message) {
        WritableMap itemMap = Arguments.createMap();
        itemMap.putString(MessageConstant.Message.MSG_ID, message.getMsgId());
        itemMap.putString(MessageConstant.Message.MSG_TYPE, getMessageType(message.getType()));
        itemMap.putString(MessageConstant.Message.TIME_STRING, Long.toString(message.getMsgTime() / 1000));
        itemMap.putString(MessageConstant.Message.IS_OUTGOING, message.direct() == EMMessage.Direct.SEND ? "0" : "1");
        itemMap.putString(MessageConstant.Message.STATUS, getMessageStatus(message.status()));

        WritableMap user = Arguments.createMap();
        //user.putString(MessageConstant.User.DISPLAY_NAME, displayName);
        user.putString(MessageConstant.User.USER_ID, message.getFrom());
        //user.putString(MessageConstant.User.AVATAR_PATH, avatar);
        itemMap.putMap(MessageConstant.Message.FROM_USER, user);

        switch (message.getType()) {
            case TXT:
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                itemMap.putString(MessageConstant.Message.MSG_TEXT, txtBody.getMessage());
                break;
            case LOCATION:
                WritableMap locationObj = Arguments.createMap();
                EMLocationMessageBody locBody = (EMLocationMessageBody) message.getBody();
                locationObj.putString(MessageConstant.Location.LATITUDE, Double.toString(locBody.getLatitude()));
                locationObj.putString(MessageConstant.Location.LONGITUDE, Double.toString(locBody.getLongitude()));
                locationObj.putString(MessageConstant.Location.ADDRESS, locBody.getAddress());
                itemMap.putMap(MessageConstant.Message.EXTEND, locationObj);
                break;
            case IMAGE:
                WritableMap imageObj = Arguments.createMap();
                EMImageMessageBody imageBody = (EMImageMessageBody) message.getBody();
                if (message.direct() == EMMessage.Direct.SEND) {
                    imageObj.putString(MessageConstant.MediaFile.THUMB_PATH, imageBody.getLocalUrl());
                    imageObj.putString(MessageConstant.MediaFile.PATH, imageBody.getLocalUrl());
                } else {
                    imageObj.putString(MessageConstant.MediaFile.THUMB_PATH, imageBody.getThumbnailUrl());
                    imageObj.putString(MessageConstant.MediaFile.PATH, imageBody.getThumbnailUrl());
                }
                imageObj.putString(MessageConstant.MediaFile.URL, imageBody.getRemoteUrl());
                imageObj.putString(MessageConstant.MediaFile.DISPLAY_NAME, imageBody.getFileName());
                imageObj.putString(MessageConstant.MediaFile.HEIGHT, Integer.toString(imageBody.getHeight()));
                imageObj.putString(MessageConstant.MediaFile.WIDTH, Integer.toString(imageBody.getWidth()));
                itemMap.putMap(MessageConstant.Message.EXTEND, imageObj);
                break;
            case VOICE:
                WritableMap audioObj = Arguments.createMap();
                EMVoiceMessageBody voiceBody = (EMVoiceMessageBody) message.getBody();
                int len = voiceBody.getLength();
                if (message.direct() == EMMessage.Direct.SEND) {
                    audioObj.putString(MessageConstant.MediaFile.THUMB_PATH, voiceBody.getLocalUrl());
                    audioObj.putString(MessageConstant.MediaFile.PATH, voiceBody.getLocalUrl());
                    audioObj.putString(MessageConstant.MediaFile.URL, voiceBody.getLocalUrl());
                } else {
                    audioObj.putString(MessageConstant.MediaFile.THUMB_PATH, voiceBody.getRemoteUrl());
                    audioObj.putString(MessageConstant.MediaFile.PATH, voiceBody.getRemoteUrl());
                    audioObj.putString(MessageConstant.MediaFile.URL, voiceBody.getRemoteUrl());
                }
                audioObj.putString(MessageConstant.MediaFile.DURATION, Long.toString(len));
                itemMap.putMap(MessageConstant.Message.EXTEND, audioObj);
                break;
            case VIDEO:
                WritableMap videoDic = Arguments.createMap();
                EMVideoMessageBody videoBody = (EMVideoMessageBody) message.getBody();
                if (message.direct() == EMMessage.Direct.SEND) {
                    videoDic.putString(MessageConstant.MediaFile.PATH, videoBody.getLocalUrl());
                    videoDic.putString(MessageConstant.MediaFile.URL, videoBody.getLocalUrl());
                } else {
                    videoDic.putString(MessageConstant.MediaFile.PATH, videoBody.getRemoteUrl());
                    videoDic.putString(MessageConstant.MediaFile.URL, videoBody.getRemoteUrl());
                }
                videoDic.putString(MessageConstant.MediaFile.THUMB_PATH, videoBody.getThumbnailUrl());
                videoDic.putString(MessageConstant.MediaFile.DISPLAY_NAME, videoBody.getFileName());
                videoDic.putString(MessageConstant.MediaFile.DURATION, Integer.toString(videoBody.getDuration()));
                itemMap.putMap(MessageConstant.Message.EXTEND, videoDic);
                break;
            default:
                break;
        }
        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        return itemMap;
    }


    static String getMessageType(EMMessage.Type EMType) {
        String type = MessageConstant.MsgType.CUSTON;
        switch (EMType) {
            case TXT:
                type = MessageConstant.MsgType.TEXT;
                break;
            case IMAGE:
                type = MessageConstant.MsgType.IMAGE;
                break;
            case VOICE:
                type = MessageConstant.MsgType.VOICE;
                break;
            case VIDEO:
                type = MessageConstant.MsgType.VIDEO;
                break;
            case LOCATION:
                type = MessageConstant.MsgType.LOCATION;
                break;
            case FILE:
                type = MessageConstant.MsgType.FILE;
                break;
            case CMD:
                type = MessageConstant.MsgType.NOTIFICATION;
                break;
            default:
                type = MessageConstant.MsgType.CUSTON;
                break;
        }

        return type;
    }

    static String getMessageStatus(Status statusEnum) {
        switch (statusEnum) {
            case CREATE:
                return MessageConstant.MsgStatus.SEND_DRAFT;
            case INPROGRESS:
                return MessageConstant.MsgStatus.SEND_SENDING;
            case SUCCESS:
                return MessageConstant.MsgStatus.SEND_SUCCESS;
            case FAIL:
                return MessageConstant.MsgStatus.SEND_FAILE;
            default:
                return MessageConstant.MsgStatus.SEND_DRAFT;
        }

    }

    enum Key{
        VibrateAndPlayToneOn,
        VibrateOn,
        PlayToneOn,
        SpakerOn,
        DisabledGroups,
        DisabledIds
    }
}
