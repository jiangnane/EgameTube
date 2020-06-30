package cn.egame.terminal.net.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hanwei on 2017/5/24.
 */

public class NetUtils {

    public static Proxy createProxy(String ip, int port) throws UnknownHostException {
        String[] ipStr = ip.split("\\.");
        byte[] ipBuf = new byte[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipBuf[i] = (byte) (Integer.parseInt(ipStr[i]) & 0xff);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(InetAddress.getByAddress(ipBuf), port));
    }
}
