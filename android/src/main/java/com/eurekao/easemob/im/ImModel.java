package com.eurekao.easemob.im;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImModel {

    protected Context context = null;
    protected Map<Key,Object> valueCache = new HashMap<Key,Object>();

    public ImModel(Context ctx){
        context = ctx;
        PreferenceManager.init(context);
    }
    /**
     * save current username
     * @param username
     */
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

    enum Key{
        VibrateAndPlayToneOn,
        VibrateOn,
        PlayToneOn,
        SpakerOn,
        DisabledGroups,
        DisabledIds
    }
}
