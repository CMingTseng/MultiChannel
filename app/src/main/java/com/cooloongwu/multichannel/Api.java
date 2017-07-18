package com.cooloongwu.multichannel;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Api接口
 * Created by CooLoongWu on 2017-2-20 14:16.
 */

public class Api {

    /**
     * 微信URL，通过code换取access_token、refresh_token和已授权scope
     */
    private static final String URL_WECHAT_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";

    /**
     * 微信URL，获取微信用户的个人信息
     */
    private static final String URL_WECHAT_USER_INFO = "https://api.weixin.qq.com/sns/userinfo";


    /**
     * 得到微信的Token
     *
     * @param context 上下文
     * @param code    微信code
     * @param handler 处理
     */
    public static void getWeChatToken(Context context, String code,
                                      JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.add("appid", BuildConfig.WEXIN_APPID);
        params.add("secret", BuildConfig.WEIXIN_APPSECRET);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        HttpClient.weChatPost(context, URL_WECHAT_ACCESS_TOKEN, params, handler);
    }

    /**
     * 得到微信用户的用户信息
     *
     * @param context      上下文
     * @param access_token token
     * @param openid       openid
     * @param handler      处理
     */
    public static void getWeChatUserInfo(Context context, String access_token, String openid, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.add("access_token", access_token);
        params.add("openid", openid);
        HttpClient.weChatPost(context, URL_WECHAT_USER_INFO, params, handler);
    }
}
