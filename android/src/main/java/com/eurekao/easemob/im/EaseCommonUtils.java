package com.eurekao.easemob.im;

import android.text.TextUtils;

import com.eurekao.easemob.im.IMApplication.Constant;
import com.eurekao.easemob.im.common.ResourceUtil;
import com.eurekao.easemob.im.domain.EaseUser;
import com.hyphenate.chat.EMMessage;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.HanziToPinyin;
import com.hyphenate.util.HanziToPinyin.Token;

import java.util.ArrayList;


/**
 * Created by Eureka on 2017/10/2.
 */

public class EaseCommonUtils {
    protected static final String TAG = "EaseCommonUtils";

    public static boolean isNetWorkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }

        return false;
    }

    public static boolean isSilentMessage(EMMessage message){
        return message.getBooleanAttribute("em_ignore_notification", false);
    }

    public static void setUserInitialLetter(EaseUser user) {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;

        final class GetInitialLetter {
            String getLetter(String name) {
                if (TextUtils.isEmpty(name)) {
                    return DefaultLetter;
                }
                char char0 = name.toLowerCase().charAt(0);
                if (Character.isDigit(char0)) {
                    return DefaultLetter;
                }
                ArrayList<Token> l = HanziToPinyin.getInstance().get(name.substring(0, 1));
                if (l != null && l.size() > 0 && l.get(0).target.length() > 0)
                {
                    Token token = l.get(0);
                    String letter = token.target.substring(0, 1).toUpperCase();
                    char c = letter.charAt(0);
                    if (c < 'A' || c > 'Z') {
                        return DefaultLetter;
                    }
                    return letter;
                }
                return DefaultLetter;
            }
        }

        if ( !TextUtils.isEmpty(user.getNick()) ) {
            letter = new GetInitialLetter().getLetter(user.getNick());
            user.setInitialLetter(letter);
            return;
        }
        if (letter.equals(DefaultLetter) && !TextUtils.isEmpty(user.getUsername())) {
            letter = new GetInitialLetter().getLetter(user.getUsername());
        }
        user.setInitialLetter(letter);
    }

    public static String getMessageDigest(EMMessage message) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION:
                if (message.direct() == EMMessage.Direct.RECEIVE) {
                    digest = ResourceUtil.getString(R.string.location_recv);
                    digest = String.format(digest, message.getFrom());
                    return digest;
                } else {
                    digest = ResourceUtil.getString(R.string.location_prefix);
                }
                break;
            case IMAGE:
                digest = ResourceUtil.getString(R.string.picture);
                break;
            case VOICE:
                digest = ResourceUtil.getString(R.string.voice_prefix);
                break;
            case VIDEO:
                digest = ResourceUtil.getString(R.string.video);
                break;
            case TXT:
                EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
                if(message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VOICE_CALL, false)){
                    digest = ResourceUtil.getString(R.string.voice_call) + txtBody.getMessage();
                }else if(message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_VIDEO_CALL, false)){
                    digest = ResourceUtil.getString(R.string.video_call) + txtBody.getMessage();
                }else if(message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                    if(!TextUtils.isEmpty(txtBody.getMessage())){
                        digest = txtBody.getMessage();
                    }else{
                        digest = ResourceUtil.getString(R.string.dynamic_expression);
                    }
                }else{
                    digest = txtBody.getMessage();
                }
                break;
            case FILE:
                digest = ResourceUtil.getString(R.string.file);
                break;
            default:
                EMLog.e(TAG, "error, unknow type");
                return "";
        }

        return digest;
    }
}
