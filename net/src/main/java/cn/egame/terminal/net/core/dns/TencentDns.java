package cn.egame.terminal.net.core.dns;

import android.text.TextUtils;

import cn.egame.terminal.utils.Logger;

/**
 * Created by hanwei on 2017/5/23.
 */

public class TencentDns extends HttpDns {

    private static String sDnsUrl = "http://119.29.29.29/d?dn=%s.&ttl=1";

    protected TencentDns() {
        super();
    }

    protected Dns getHttpDns(String host) {
        String reqUrl = String.format(sDnsUrl, host);

        String result = getString(reqUrl);

        if (TextUtils.isEmpty(result)) {
            return null;
        }

        try {
            String[] ipsTTL = result.split(",");

            if (ipsTTL == null || ipsTTL.length <= 0) {
                return null;
            }

            String[] ips = ipsTTL[0].split(";");

            if (ips == null || ips.length <= 0) {
                return null;
            }

            if (ipsTTL.length > 1) {
                int ttl = Integer.valueOf(ipsTTL[1]);
                return new Dns(ips, ttl);
            } else {
                return new Dns(ips);
            }

        } catch (Exception e) {
            Logger.w(TAG, e.getMessage());
        }

        return null;
    }
}
