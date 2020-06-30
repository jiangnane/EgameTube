package cn.egame.terminal.net.demo;

import android.app.Application;

import cn.egame.terminal.utils.Logger;

/**
 * Created by hanwei on 2017/6/1.
 */

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.setDebuggable(true);
    }
}
