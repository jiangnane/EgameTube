/*
 * FileName:	TestActivity.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package cn.egame.terminal.net.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;


import org.json.JSONObject;

import java.io.InputStream;

import cn.egame.terminal.net.FastTube;
import cn.egame.terminal.net.core.EgameTube;
import cn.egame.terminal.net.core.TubeResponse;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.TubeListener;
import cn.egame.terminal.utils.Logger;

/**
 * </Br> <功能详细描述> </Br>
 *
 * @author Hein
 * @hide
 */
public class TestActivity extends Activity {

    public static final int LOGGER = 0x00;

    private static final String TEST_URL1 = "http://ip-api.com/json/";
    private static final String TEST_URL2 = "http://ip-api.com/json/?lang=zh-CN";
    private static final String TEST_URL3 = "https://play.cn/static/cloudgaming/homepage/static/media/icon_logo.17427ce0.svg";

    private static final TubeOptions NORMAL_OPTIONS = new TubeOptions.Builder()
            .setSoTimeOut(2 * 1000)
            .setConnectionTimeOut(2 * 1000)
            .setReconnectionTimes(2).create();

    private FastTube mFastTube = FastTube.getInstance();

    {
        mFastTube.init(new TubeConfig.Builder().setThreadCount(10).create());
    }

    private TextView mLoggerView = null;
    private ScrollView mScrollView = null;

    private boolean isProcessing = false;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case LOGGER:
                    if (mLoggerView == null) {
                        return;
                    }

                    mLoggerView.append((CharSequence) msg.obj);

                    mScrollView.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Logger.register(mHandler, LOGGER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mLoggerView = (TextView) findViewById(R.id.logout);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        findViewById(R.id.test1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doTest1();
            }
        });

        findViewById(R.id.test2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doTest2();
            }
        });

        findViewById(R.id.test3).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                doTest3();
            }
        });

        Logger.v("VERBOSE READY!");
        Logger.d("DEBUG READY!");
        Logger.i("INFO READY!");
        Logger.w("WARN READY!");
        Logger.e("ERROR READY!");
        Logger.wtf("WTF READY!");
    }

    /**
     * Get
     */
    private void doTest1() {
        mFastTube.getJSON(TEST_URL1, NORMAL_OPTIONS, new JSONTubeListener<JSONObject>() {

            @Override
            public JSONObject doInBackground(JSONObject water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(JSONObject result) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
    }

    /**
     * Post
     */
    private void doTest2() {
        Logger.i("See the demo source.");
        // KeyValuePostBody

        /*
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("key", "value");

        TubeOptions opt = new TubeOptions.Builder().setPostBody(new KeyValuePostBody(body)).create();

        mFastTube.post("http://<posturl>/", opt, new StringTubeListener<String>() {

            @Override
            public String doInBackground(String water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(String result) {
                // TODO Auto-generated method stub
                Logger.i("HEIN", result);
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
         */

        // JSONPostBody
        /*
        JSONObject body = new JSONObject();

        try {
            body.put("Name", "HEIN");
            body.put("Age", "30");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TubeOptions opt = new TubeOptions.Builder()
                .setPostBody(new JSONPostBody(body))
                .setPostInGzip(true)
                .create();

        mFastTube.post(testUrl1, opt, new StringTubeListener<String>() {

            @Override
            public String doInBackground(String water) {
                // TODO Auto-generated method stub
                return water;
            }

            @Override
            public void onSuccess(String result) {
                // TODO Auto-generated method stub
                Logger.i("HEIN", result);
            }

            @Override
            public void onFailed(TubeException e) {
                // TODO Auto-generated method stub
                Logger.e("HEIN", e.getLocalizedMessage());
            }
        });
         */
    }

    /**
     * TubeResponse
     * Custom TubeOptions
     */
    private void doTest3() {

        EgameTube tube = new EgameTube();
        tube.init(TubeConfig.getDefault());
        TubeOptions opt = new TubeOptions.Builder()
                .setSoTimeOut(15 * 1000)
                .setConnectionTimeOut(15 * 1000)
                .setRange(0, 100)
                .setReconnectionTimes(10).create();
        tube.get(TEST_URL3, opt, new TubeListener<Object, String>() {
            @Override
            public String doInBackground(Object water) throws Exception {

                if (water instanceof TubeResponse) {

                    TubeResponse resp = (TubeResponse) water;
                    InputStream is = resp.toStream();

                    byte[] buf = new byte[8 * 1024];
                    int len = is.read(buf);
                    Logger.d("HEIN", "TubeResponse is OK. Data length: " + len);
                }

                return "OK";
            }

            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onFailed(TubeException e) {
                Logger.e("HEIN", e.getMessage());
            }
        });

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Logger.unRegister(mHandler);
    }

}
