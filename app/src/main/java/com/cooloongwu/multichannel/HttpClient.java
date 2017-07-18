package com.cooloongwu.multichannel;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 定义了基本的HttpClient，以及请求超时、链接超时等信息
 * Created by CooLoongWu on 2017-2-20 14:10.
 */

public class HttpClient {

    private static final int CONNECT_TIME = 8 * 1000;
    private static final int RESPONSE_TIME = 10 * 1000;

    private static AsyncHttpClient clientGeneral;

    /**
     * 设置静态Client
     *
     * @param client 客户端
     */
    static void setClientGeneral(AsyncHttpClient client) {
        clientGeneral = client;
        clientGeneral.setConnectTimeout(CONNECT_TIME);
        clientGeneral.setResponseTimeout(RESPONSE_TIME);
    }

    static void weChatPost(Context context, String url, RequestParams params, AsyncHttpResponseHandler handler) {
        clientGeneral.get(context, url, params, handler);
    }
}
