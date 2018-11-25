package alick.com.iosuplinelistener;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final String url = "https://appstoreconnect.apple.com/WebObjects/iTunesConnect.woa/ra/ng/app/1435725770/platform/ios/versions/828520779/resolutioncenter";
//    private final String url = "https://www.baidu.com";

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

    private static final int millisInFuture = 20 * 1000;

    private SoundPool soundPool;

    private boolean isHasReply;

    private CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, 1000) {
        @Override
        public void onTick(final long millisUntilFinished) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_countDown.setText(String.format("倒计时:%d", millisUntilFinished/1000));
                }
            });
        }

        @Override
        public void onFinish() {
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
                String newTime = parseTime(html);

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
                    showDialog("傻逼苹果审核🐶给您回复了");
                    MainActivity.this.notify2();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            String call = "javascript:changeStartTime(\"" + time + "\")";
//                            wvContent.loadUrl(call);
                                customWebView.reload();
                        }
                    });
                }
                lastTs = currentTs;
            } catch (Exception e) {
                e.printStackTrace();
                showDialog(e.getMessage());
            }
        }
    }

    public void notify2() {
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long [] pattern = {800, 500, 400, 300};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern,0);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                MainActivity.this.soundPool.play(1,1,1,0,0,1);
            }
        });
        soundPool.play(1, 1, 1, 0, 0, 1);
    }

    private String parseTime(String html) {
        int sendFromApplyIndex = html.indexOf("发件人 Apple");

        String content = html.substring(sendFromApplyIndex - 100, sendFromApplyIndex);

        String time = content.substring(content.indexOf(before), content.indexOf(after));

        String newTime = time;
        //2018年11月25日 上午2:48
        if (!TextUtils.isEmpty(time)) {
            if (time.contains("上午")) {
                time = time.replace("上午", "");
                newTime = time;
            } else if (time.contains("下午")) {
                int index1 = time.indexOf("下午");
                int index2 = time.indexOf(":");
                String hourStr = time.substring(index1 + ("下午".length()), index2);
                int hour = Integer.parseInt(hourStr) + 12;

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
        btn_find = findViewById(R.id.btn_find);
        tv_time=findViewById(R.id.tv_time);
        tv_countDown=findViewById(R.id.tv_countDown);
        btn_logined=findViewById(R.id.btn_logined);

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lordJs();
            }
        });

        btn_logined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customWebView.loadUrl(url);
                btn_logined.setVisibility(View.GONE);
            }
        });

        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.sound, 100);

        customWebView = findViewById(R.id.customWebView);

        // 把刚才的接口类注册到名为HTMLOUT的JavaScript接口
        customWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "java_obj");


        customWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                //返回false防止跳转到系统浏览器
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if(btn_logined.getVisibility()!=View.VISIBLE){
                    countDownTimer.cancel();
                    countDownTimer.start();
                }
            }
        });

        customWebView.loadUrl(url);
    }

    private void showDialog(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("网页解析错误").setMessage(str).setCancelable(false).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(soundPool!=null){
            try {
                soundPool.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
