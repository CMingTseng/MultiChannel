package com.cooloongwu.multichannel.night.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.cooloongwu.multichannel.Api;
import com.cooloongwu.multichannel.BuildConfig;
import com.cooloongwu.multichannel.R;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private IWXAPI iwxapi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);

        iwxapi = WXAPIFactory.createWXAPI(this, BuildConfig.WEXIN_APPID, false);
        iwxapi.handleIntent(getIntent(), this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        iwxapi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Toast.makeText(WXEntryActivity.this, "onReq" + req.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        Toast.makeText(this, "baseresp.getType = " + resp.getType(), Toast.LENGTH_SHORT).show();

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;
                //getCode(resp);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }

    private void getCode(BaseResp baseResp) {
        Bundle bundle = new Bundle();
        baseResp.toBundle(bundle);
        SendAuth.Resp sp = new SendAuth.Resp(bundle);
        String code = sp.code;
        if (null == code || "null".equals(code) || code.isEmpty()) {
            showToast("微信code：" + "无");
        } else {
            showToast("微信code：" + code);
            getToken(code);
        }
    }

    private void getToken(String code) {
        Api.getWeChatToken(this, code, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                showToast("微信token获取成功：" + response.toString());
                try {
                    String access_token = response.getString("access_token");       //接口调用凭证
                    String refresh_token = response.getString("refresh_token");     //用户刷新access_token
                    String openid = response.getString("openid");                   //授权用户唯一标识
                    String scope = response.getString("scope");                     //用户授权的作用域，使用逗号（,）分隔
                    String unionid = response.getString("unionid");                 //当且仅当该移动应用已获得该用户的userinfo授权时，才会出现该字段
                    long expires_in = response.getLong("expires_in");               //access_token接口调用凭证超时时间，单位（秒）

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                showToast("微信token获取失败");
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}