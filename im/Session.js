/**
 * Created by dowin on 2017/8/2.
 */
'use strict'
import { NativeModules,Platform } from 'react-native';

const { RNEasemobIm } = NativeModules;
class Session {


    /**
     * 登陆
     * @param account
     * @param token
     * @returns {*} @see observeRecentContact observeOnlineStatus
     */
    login(contactId, token) {
        return RNEasemobIm.login(contactId, token);
    }

    /**
     * 退出
     * @returns {*}
     */
    logout() {
        return RNEasemobIm.logout();
    }
    /** ******************************会话  聊天****************************************** **/


    /**
     * 最近会话列表
     * @returns {*}
     */
    getRecentConversationList() {
        return RNEasemobIm.getRecentConversationList();
    }
    /**
     * 删除最近会话
     * @param recentContactId
     * @returns {*}
     */
    deleteRecentContact(recentContactId) {
        return RNEasemobIm.deleteRecentContact(recentContactId);
    }
    /**
     * 进入聊天会话
     * @param contactId
     * @param type
     * @returns {*} @see observeReceiveMessage 接收最近20消息记录
     */
    startSession(contactId) {
        return RNEasemobIm.startSession(contactId);
    }

    /**
     * 退出聊天会话
     * @returns {*}
     */
    stopSession() {
        return RNEasemobIm.stopSession();
    }

    /**
     * 获取最近聊天内容
     * @param messageId
     * @param limit 查询结果的条数限制
     * @returns {*}  @see 回调返回最近所有消息记录
     */
    queryMessageListEx(messageId, limit) {
        return RNEasemobIm.queryMessageListEx(messageId, limit);
    }

    /** ******************************发送 消息****************************************** **/

    /**
     *1.发送文本消息
     * @param content 文本内容
     * @param atUserIds @的群成员ID ["abc","abc12"]
     */
    sendTextMessage(content) {
        return RNEasemobIm.sendTextMessage(content);
    }

    /**
     * 发送图片消息
     * @param file 图片文件对象
     * @param type 0:图片 1：视频
     * @param displayName 文件显示名字，如果第三方 APP 不关注，可为空
     * @returns {*}
     */
    sendImageMessages(file) {
        if (Platform.OS === 'ios') {
            return RNEasemobIm.sendImageMessages(file, displayName);
        }

        return RNEasemobIm.sendImageMessage(file.replace("file://", ""));
    }
    /**
     * 发送音频消息
     * @param file 音频文件
     * @param duration 音频持续时间，单位是ms
     * @returns {*}
     */
    sendAudioMessage(file, duration) {
        return RNEasemobIm.sendAudioMessage(file, duration);
    }

    /**
     * 发送地理位置消息
     * @param latitude 纬度
     * @param longitude 经度
     * @param address 地址信息描述
     * @returns {*}
     */
    sendLocationMessage(latitude, longitude, address) {
        return RNEasemobIm.sendLocationMessage(latitude, longitude, address);
    }

    getMessageSound() {
        return RNEasemobIm.getMessageSound();
    }
    setMessageSound(checked) {
        return RNEasemobIm.setMessageSound(checked);
    }
    
    getMessageVibrate() {
        return RNEasemobIm.getMessageVibrate();
    }
    setMessageVibrate(checked) {
        return RNEasemobIm.setMessageVibrate(checked);
    }
        
    getMessageNotify() {
        return RNEasemobIm.getMessageNotify();
    }
    setMessageNotify(checked) {
        return RNEasemobIm.setMessageNotify(checked);
    }
}
module.exports = new Session();