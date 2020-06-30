package cn.egame.terminal.net.core.dns;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import cn.egame.terminal.utils.Logger;

/**
 * Created by hanwei on 2017/5/23.
 */

public class HttpDnsService {

    private static final String TAG = "HttpDnsService";

    private static HashMap<Class<? extends HttpDns>, HttpDns> sInsMap = new HashMap<Class<? extends HttpDns>, HttpDns>();

    public static HttpDns getService(Class<? extends HttpDns> service) {

        HttpDns hd = sInsMap.get(service);

        if (hd == null) {
            synchronized (sInsMap) {
                if (hd == null) {
                    try {
                        Constructor<? extends HttpDns> constructor = service.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        hd = constructor.newInstance();
                        sInsMap.put(service, hd);
                    } catch (NoSuchMethodException e) {
                        Logger.w(TAG, e.getMessage());
                    } catch (IllegalAccessException e) {
                        Logger.w(TAG, e.getMessage());
                    } catch (InstantiationException e) {
                        Logger.w(TAG, e.getMessage());
                    } catch (InvocationTargetException e) {
                        Logger.w(TAG, e.getMessage());
                    }
                }
            }
        }

        return hd;
    }
}
