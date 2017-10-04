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
import com.hyphenate.chat.EMMessage;
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

    public static WritableMap createMessage(EMMessage item) {
        return null;
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
