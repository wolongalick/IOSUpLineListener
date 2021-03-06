package alick.com.iosuplinelistener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final String url = "https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1435725770/platform/ios/versions/828520779/resolutioncenter";
//    private final String url = "https://www.baidu.com";

    private final String LOGIN_URL="https://appstoreconnect.apple.com/login";
    private final String CAN_NOT_HANDLE_REQUEST ="https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/wa/defaultError";


    private Button btn_find;
    private CustomWebView customWebView;
    private TextView tv_time;
    private TextView tv_countDown;
    private TextView btn_logined;

    private final String before = "2018年";
    private final String after = "</div>";

    private final String SP_KEY_TIME = "sp_key_time";

    private boolean isFirstUpdate;

    private long lastTs;

    private static final int millisInFuture = 30 * 1000;

    private MediaPlayer mediaPlayer;

    private boolean isHasReply;

    private PowerManager.WakeLock mWakeLock;

    private final String alertNeedRelogin = "请关闭app并重新打开";
    private final String alertCleanCache = "请关闭app,清除应用数据并重新打开";
    private final String alertNetException = "请检查网络后重试";
    private final String alertCanNotHandleRequest = "无法处理您的请求,请关闭app重新打开";

    private Vibrator vibrator;

    private AlertDialog alertDialog;

    private String currentUrl;

    private final int failCountMax =3;                  //请求失败的次数上限
    private final int canNotHandleRequestCountMax =3;   //无法请求的次数上限

    private int failCount;                              //请求失败的次数

    private int canNotHandleRequestCount;               //无法请求的次数

    private CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, 1000) {
        @Override
        public void onTick(final long millisUntilFinished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_countDown.setText(String.format("倒计时:%d", millisUntilFinished / 1000));
                }
            });
        }

        @Override
        public void onFinish() {
            if (isHasReply) {
                notifyReply();
            }
            lordJs();
        }
    };


    private void lordJs() {
        customWebView.loadUrl("javascript:window.java_obj.getSource(document.documentElement.outerHTML);void(0)");
    }


    //自己定义的类
    public final class InJavaScriptLocalObj {
        //一定也要加上这个注解,否则没有用
        @JavascriptInterface
        public void getSource(String html) {
            try {
                BLog.i("当前url:"+currentUrl);

                if(!TextUtils.isEmpty(currentUrl)){
                    if(currentUrl.startsWith(LOGIN_URL)){
                        showDialog("提示", alertNeedRelogin);
                        notifyNeedRelogin();
                        return;
                    }else if(currentUrl.startsWith(CAN_NOT_HANDLE_REQUEST)){
                        canNotHandleRequestCount++;
                        if(canNotHandleRequestCount > canNotHandleRequestCountMax){
                            showDialog("提示", alertCanNotHandleRequest);
                            notifyCleanCache();
                            return;
                        }
                    }else {
                        canNotHandleRequestCount=0;//重置次数
                    }
                }

                String newTime = parseTime(html);
                failCount =0;//重置连续失败次数

                BLog.i("完整的html:" + html);
//                Toast.makeText(getApplicationContext(), "时间:" + newTime, Toast.LENGTH_SHORT).show();

                final String finalNewTime = newTime;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_time.setText(String.format("苹果最新回复时间:\n%s", finalNewTime));
                    }
                });

                long currentTs = TimeUtils.parseStringToMillis(newTime, TimeUtils.format16);
                if (lastTs > 0 && currentTs > lastTs) {
                    showDialog("喜讯!", "傻逼苹果审核🐶给您回复了");
                    isHasReply = true;
                    MainActivity.this.notifyReply();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            String call = "javascript:changeStartTime(\"" + time + "\")";
//                            wvContent.loadUrl(call);
                            if(alertDialog!=null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            customWebView.reload();
                        }
                    });
                }
                lastTs = currentTs;
            } catch (Exception e) {
                e.printStackTrace();
                showDialog("提示", e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                            String call = "javascript:changeStartTime(\"" + time + "\")";
//                            wvContent.loadUrl(call);
                        customWebView.reload();
                    }
                });
            }
        }
    }

    /**
     * 通知用户:需要重新登录
     */
    public void notifyNeedRelogin() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {800, 500, 400, 300};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 0);

        try {
            mediaPlayer.reset();
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.need_exception);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知用户:需要重新登录
     */
    public void notifyCleanCache() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {800, 500, 400, 300};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 0);

        try {
            mediaPlayer.reset();
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.need_exception);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知用户:iOS来新的回复了
     */
    public void notifyReply() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {800, 500, 400, 300};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 0);

        vibrator.cancel();

        try {
            mediaPlayer.reset();
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.sound);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知用户:网络异常
     */
    public void notifyNetException() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {800, 500, 400, 300};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 0);

        vibrator.cancel();

        try {
            mediaPlayer.reset();
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.need_exception);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String parseTime(String html) {
        int sendFromApplyIndex = html.indexOf("发件人 Apple");

        String content = html.substring(sendFromApplyIndex - 100, sendFromApplyIndex);

        String time = content.substring(content.indexOf(before), content.indexOf(after));

        String newTime = time;
//        newTime="2018年11月29日 下午12:43";
        //2018年11月25日 上午2:48
        if (!TextUtils.isEmpty(time)) {
            if (time.contains("上午")) {
                time = time.replace("上午", "");
                newTime = time;
            } else if (time.contains("下午")) {
                int index1 = time.indexOf("下午");
                int index2 = time.indexOf(":");
                String hourStr = time.substring(index1 + ("下午".length()), index2);
                int i = Integer.parseInt(hourStr);
                if(i==12){
                    i=0;
                }
                int hour = i + 12;
                newTime = time.substring(0, index1 + ("下午".length())) + hour + time.substring(index2, time.length());
                newTime = newTime.replace("下午", "");
            }
        }
        return newTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }
        btn_find = findViewById(R.id.btn_find);
        tv_time = findViewById(R.id.tv_time);
        tv_countDown = findViewById(R.id.tv_countDown);
        btn_logined = findViewById(R.id.btn_logined);

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lordJs();
            }
        });

        btn_logined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMainUrl();
                btn_logined.setVisibility(View.GONE);
            }
        });

        customWebView = findViewById(R.id.customWebView);

        // 把刚才的接口类注册到名为HTMLOUT的JavaScript接口
        customWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");
        customWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        customWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                //返回false防止跳转到系统浏览器
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                MainActivity.this.currentUrl=url;

                if (btn_logined.getVisibility() != View.VISIBLE) {
                    countDownTimer.cancel();
                    countDownTimer.start();
                }
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                failCount++;//增加一次失败次数

                if(failCount < failCountMax){
                    customWebView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadMainUrl();
                        }
                    }, 3000);
                }else {
                    //当连续失败次数达到上限时,给出提示
                    showDialog("提示", alertNetException);
                    notifyNetException();
                }
            }
        });

        loadMainUrl();
    }

    private void loadMainUrl() {
        customWebView.loadUrl(url);
    }

    private void showDialog(String title, final String msg) {
        alertDialog = new AlertDialog.Builder(this).setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (alertNeedRelogin.equals(msg) || alertCleanCache.equals(msg) || alertNetException.equals(msg) || alertCanNotHandleRequest.equals(msg)) {
                            System.exit(0);
                        }
                    }
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLog.i("销毁MainActivity--->onDestroy()");
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
