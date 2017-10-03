package com.eurekao.easemob.im.db;

import android.content.ContentValues;
import android.content.Context;


import com.eurekao.easemob.im.domain.InviteMessage;

import java.util.List;

public class InviteMessgeDao {
    static final String TABLE_NAME = "new_friends_msgs";
    static final String COLUMN_NAME_ID = "id";
    static final String COLUMN_NAME_FROM = "username";
    static final String COLUMN_NAME_GROUP_ID = "groupid";
    static final String COLUMN_NAME_GROUP_Name = "groupname";

    static final String COLUMN_NAME_TIME = "time";
    static final String COLUMN_NAME_REASON = "reason";
    public static final String COLUMN_NAME_STATUS = "status";
    static final String COLUMN_NAME_ISINVITEFROMME = "isInviteFromMe";
    static final String COLUMN_NAME_GROUPINVITER = "groupinviter";

    static final String COLUMN_NAME_UNREAD_MSG_COUNT = "unreadMsgCount";


    public InviteMessgeDao(Context context){
    }

    /**
     * save message
     * @param message
     * @return  return cursor of the message
     */
    public Integer saveMessage(InviteMessage message){
        return DBManager.getInstance().saveMessage(message);
    }

    /**
     * update message
     * @param msgId
     * @param values
     */
    public void updateMessage(int msgId,ContentValues values){
        DBManager.getInstance().updateMessage(msgId, values);
    }

    /**
     * get messges
     * @return
     */
    public List<InviteMessage> getMessagesList(){
        return DBManager.getInstance().getMessagesList();
    }

    public void deleteMessage(String from){
        DBManager.getInstance().deleteMessage(from);
    }

    public void deleteGroupMessage(String groupId) {
        DBManager.getInstance().deleteGroupMessage(groupId);
    }

    public void deleteGroupMessage(String groupId, String from) {
        DBManager.getInstance().deleteGroupMessage(groupId, from);
    }

    public int getUnreadMessagesCount(){
        return DBManager.getInstance().getUnreadNotifyCount();
    }

    public void saveUnreadMessageCount(int count){
        DBManager.getInstance().setUnreadNotifyCount(count);
    }
}
