/*
 * FileName:	EgameBox.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 *
 *
 *
 */
package cn.egame.terminal.net.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.listener.JSONTubeListener;
import cn.egame.terminal.net.listener.StreamTubeListener;
import cn.egame.terminal.net.listener.StringTubeListener;
import cn.egame.terminal.net.listener.TubeListener;


/**
 * 网络管道类 对于配置信息相同仅URL不同的请求可共用一个对象
 *
 * @author Hein
 */
public class EgameTube {

    private TubeThreadPool mTubePool = null;
    private TubeConfig mConfig = TubeConfig.getDefault();

    public EgameTube() {
    }

    public void init(TubeConfig cfg) {

        mConfig = cfg;

        if (mTubePool != null) {
            return;
        }

        if (mConfig.mThreadCount > 0) {
            mTubePool = TubeThreadPool.create(mConfig.mThreadCount);
        }

        // Logger.IS_DEBUG_MODE = mConfig.isDebuggable;
    }

    public void putCommonHeader(String key, String value) {
        if (mConfig != null) {
            mConfig.mCommonHeaders.put(key, value);
        }
    }

    public void release() {
        if (mTubePool != null) {
            new Thread() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mTubePool.closePool();
                }

            }.start();
        }

        mConfig = null;
    }

    /**
     * 获取数据
     *
     * @param url      请求地址
     * @param listener 数据返回
     */
    public void get(final String url, final TubeOptions opt,
                    final TubeListener<?, ?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener can not be null.");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        Looper myLooper = Looper.myLooper();

        // 如果本线程没有looper则使用主线程looper 有风险 wei.han 20131031
        if (myLooper == null) {
            myLooper = Looper.getMainLooper();
        }

        if (mTubePool == null) {
            new Thread(getRunnable(myLooper, url, opt, listener), "EgameTube:"
                    + hashCode()).start();
        } else {
            mTubePool.execute(getRunnable(myLooper, url, opt, listener));
        }
    }


    public String connectString(final String url, final TubeOptions opt) throws TubeException {
        return UrlConnector.execute(url, mConfig, opt).toString();
    }

    public TubeResponse connectStream(final String url, final TubeOptions opt) throws TubeException {
        return UrlConnector.execute(url, mConfig, opt);
    }

    private Runnable getRunnable(final Looper myLooper, final String url,
                                 final TubeOptions opt, final TubeListener listener) {
        return new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Handler handler = null;

                // 这种条件说明是异步请求，需要异步返回结果
                if (myLooper != null && listener != null) {
                    handler = new Handler(myLooper);
                }

                try {
                    TubeResponse rr = UrlConnector.execute(url, mConfig, opt);
                    final Object result;

                    if (listener instanceof StringTubeListener) {
                        String water = rr.toString();

                        if (TextUtils.isEmpty(water)) {
                            throw new TubeException("The result is null or empty.");
                        }

                        result = listener.doInBackground(water);
                    } else if (listener instanceof JSONTubeListener) {
                        String water = rr.toString();

                        if (TextUtils.isEmpty(water)) {
                            throw new TubeException("The result is null or empty.");
                        }

                        JSONObject waterJSON = new JSONObject(water);

                        result = listener.doInBackground(waterJSON);
                    } else if (listener instanceof StreamTubeListener) {
                        InputStream is = rr.toStream();

                        if (is == null) {
                            throw new TubeException("The result is null or empty.");
                        }

                        result = listener.doInBackground(is);

                        rr.close();
                    } else {

                        result = listener.doInBackground(rr);

                        rr.close();
                    }

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            listener.onSuccess(result);
                        }
                    });

                } catch (TubeException e) {
                    makeFailed(handler, listener, e);
                } catch (JSONException e) {
                    makeFailed(handler, listener, new TubeException(e, TubeException.DATA_ERROR_CODE));
                } catch (Exception e) {
                    makeFailed(handler, listener, new TubeException(e, TubeException.NORMAL_CODE));
                }

            }
        };
    }

    private void makeFailed(Handler handler, final TubeListener<?, ?> listener,
                            final TubeException e) {
        if (handler == null || listener == null) {
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                listener.onFailed(e);
            }
        });
    }
}
