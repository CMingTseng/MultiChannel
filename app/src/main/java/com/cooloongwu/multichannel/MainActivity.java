package com.cooloongwu.multichannel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class MainActivity extends AppCompatActivity {

    private IWXAPI api;
    private int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = WXAPIFactory.createWXAPI(this, BuildConfig.WEXIN_APPID, false);
        api.registerApp(BuildConfig.WEXIN_APPID);


        findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToWeixin();
            }
        });

        //Toast.makeText(MainActivity.this, this.getPackageName(), Toast.LENGTH_SHORT).show();
        Toast.makeText(MainActivity.this, BuildConfig.WEXIN_APPID, Toast.LENGTH_SHORT).show();
    }

    private void sendToWeixin() {
        if (!api.isWXAppInstalled()) {
            return;
        }

//        if (!iwxapi.isWXAppSupportAPI()) {
//            showToast("请您先登陆微信并确保微信是最新版本");
//            return;
//        }

//        SendAuth.Req req = new SendAuth.Req();
//        //应用授权作用域，如获取用户个人信息则填写snsapi_userinfo
//        req.scope = "snsapi_userinfo";
//        //用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加session进行校验
//        req.state = "weather_" + BuildConfig.APPLICATION_ID;
//        api.sendReq(req);

        WXTextObject textObj = new WXTextObject();
        textObj.text = "这就是测试啊，哈哈";

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // msg.title = "Will be ignored";
        msg.description = "就是一条测试啊。哇哈哈哈哈！";

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = mTargetScene;

        api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
