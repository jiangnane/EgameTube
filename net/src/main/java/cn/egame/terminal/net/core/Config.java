package cn.egame.terminal.net.core;

import android.os.Build;

import cn.egame.terminal.net.BuildConfig;

/**
 * Created by hanwei on 2017/5/18.
 */

public class Config {

    public static final String USER_AGENT = "EgameTube/" + BuildConfig.VERSION_NAME +
            " (" +

            "Android " + Build.VERSION.SDK_INT + "; " +
            Build.MODEL + " Build/ " + Build.ID +

            ")";

    /**
     * Socket超时时间
     */
    public static final int SO_TIMEOUT = 15 * 1000;

    /**
     * TCP连接建立超时时间
     */
    public static final int CONN_TIMEOUT = 15 * 1000;

    /**
     * 默认重试连接次数
     */
    public static final int RECONN_TIMES = 4;

    /**
     * 重试连接时，原有的超时时间的翻倍系数，如15s STEP为2 则没一轮超时时间翻2倍
     */
    public static final float RECONN_INCREASING_STEP = 2f;

    /**
     * 链接跳转深度
     */
    public static final int REDIRECT_DEEP = 3;

    /**
     * 重试连接间隔
     */
    public static final int RECONN_INTERVAL = 2 * 1000;

}
