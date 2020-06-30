package cn.egame.terminal.net.core.dns;

/**
 * Created by hanwei on 2017/5/23.
 */

public class Dns {
    public String[] ips = null;
    public int ttl = 120;
    public long updateTime = System.currentTimeMillis();

    public Dns(String[] ips) {
        this.ips = ips;
    }

    public Dns(String[] ips, int ttl) {
        this.ips = ips;
        this.ttl = ttl;
    }
}
