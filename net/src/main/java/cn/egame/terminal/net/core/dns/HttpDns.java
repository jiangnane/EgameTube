package cn.egame.terminal.net.core.dns;

import java.util.HashMap;

import cn.egame.terminal.net.core.EgameTube;
import cn.egame.terminal.net.core.TubeConfig;
import cn.egame.terminal.net.core.TubeOptions;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.utils.Logger;
import cn.egame.terminal.net.utils.NetUtils;
import cn.egame.terminal.utils.NetworkAccess;

/**
 * Created by hanwei on 2017/5/23.
 */

public abstract class HttpDns {

    protected static final String TAG = "HttpDns";

    private HashMap<String, Dns> mDnsCache = new HashMap<String, Dns>();

    private static TubeOptions sDefaultOpt =
            new TubeOptions.Builder()
                    .setConnectionTimeOut(5 * 1000)
                    .setSoTimeOut(5 * 1000)
                    .setReconnectionTimes(0)
                    .create();

    private static TubeConfig sDefaultCfg =
            new TubeConfig.Builder()
                    .setDefaultOptions(sDefaultOpt)
                    .create();

    private EgameTube mTube = null;

    protected HttpDns() {
        mTube = new EgameTube();
        mTube.init(sDefaultCfg);
    }

    protected String getString(String url) {
        try {
            return mTube.connectString(url, sDefaultOpt);
        } catch (TubeException e) {
            Logger.w(TAG, e.getMessage());
            return null;
        }
    }

    private void putDnsToCache(String host, Dns dns) {
        mDnsCache.put(host, dns);
    }

    private Dns getDnsFromCache(String host) {
        return mDnsCache.get(host);
    }

    public String getIpByHostSync(String host) {

        if (NetworkAccess.isIpv4(host)) {
            return null;
        }

        Dns dnsInCache = getDnsFromCache(host);
        if (dnsInCache != null && dnsInCache.ips != null && dnsInCache.ips.length > 0) {
            long dur = System.currentTimeMillis() - dnsInCache.updateTime;
            if (dur <= dnsInCache.ttl * 1000) {
                return dnsInCache.ips[0];
            }
        }

        Dns dns = getHttpDns(host);

        if (dns == null) {
            return null;
        }

        putDnsToCache(host, dns);

        return dns.ips[0];
    }

    protected abstract Dns getHttpDns(String host);
}
