package cn.egame.terminal.net.core;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.egame.terminal.net.core.dns.HttpDns;
import cn.egame.terminal.net.core.dns.HttpDnsService;
import cn.egame.terminal.net.core.dns.TencentDns;
import cn.egame.terminal.net.core.post.MultipartBody;
import cn.egame.terminal.net.exception.TubeException;
import cn.egame.terminal.net.utils.IOUtils;
import cn.egame.terminal.utils.Logger;
import cn.egame.terminal.net.utils.NetUtils;
import cn.egame.terminal.utils.NetworkAccess;

/**
 * Created by hanwei on 2017/5/18.
 */

public class UrlConnector {

    private static final String TAG = "TUBE";

    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 获取请求数据
     *
     * @param url 请求地址
     * @param cfg 全局配置项
     * @param opt 本次请求参数选项
     * @return
     * @throws TubeException
     */
    public static TubeResponse execute(String url, TubeConfig cfg,
                                       TubeOptions opt) throws TubeException {
        if (cfg == null) {
            throw new TubeException("The cfg is null.");
        }

        if (opt == null) {
            opt = cfg.mDefaultOptions;
        }

        if (opt.mMapHeaders == null) {
            opt.mMapHeaders = cfg.mCommonHeaders;
        } else {
            opt.mMapHeaders.putAll(cfg.mCommonHeaders);
        }

        try {
            return execute(url, opt, cfg.isHttpDNSEnable, 0);
        } catch (TubeException e) {
            throw e;
        }

    }

    /**
     * @param url
     * @param opt
     * @return
     * @throws TubeException
     */
    private static TubeResponse execute(String url, TubeOptions opt, boolean isHttpDNS, int currentDeep) throws TubeException {

        int method = opt.mHttpMethod;
        int soTimeOut = opt.mSoTimeOut;
        int connTimeOut = opt.mConnTimeOut;
        int reConnTimes = opt.mReConnTimes;
        Map<String, String> headers = opt.mMapHeaders;

        URL targetUrl;
        try {
            targetUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new TubeException("The url format is wrong. Please check it.\n" + url);
        }

        int currentTimes = 0;
        int waitingTime = Config.RECONN_INTERVAL;
        HttpURLConnection conn;

        while (currentTimes <= reConnTimes) {
            try {
                conn = createUrlConnection(targetUrl, connTimeOut, soTimeOut, headers, isHttpDNS);

                if (method == TubeOptions.HTTP_METHOD_GET) {

                    return doGet(conn, opt, isHttpDNS, currentDeep);
                } else if (method == TubeOptions.HTTP_METHOD_POST) {

                    reConnTimes = 0; //POST数据不进行重试连接
                    return doPost(conn, opt, isHttpDNS, currentDeep);
                } else if (method == TubeOptions.HTTP_METHOD_HEAD) {

                    return doHead(conn, opt, isHttpDNS, currentDeep);
                } else {
                    throw new TubeException("Unsupported http method.");
                }
            } catch (ConnectException e) { //连接超时大部分情况是网络断开了，适当延长重试间隔 wei.han
                currentTimes++;
                if (currentTimes <= reConnTimes) {
                    waitingTime *= Config.RECONN_INCREASING_STEP;
                    Logger.w(TAG, "Connection timeout, retrying... Times remain " + (reConnTimes - currentTimes + 1) + "\n" +
                            "Increase waiting time to " + waitingTime / 1000 + "s.\n"
                            + e.getClass().getCanonicalName() + ": " + url);
                    waitToReconnect(waitingTime);
                }
            } catch (SocketTimeoutException e) { // 数据读取超时，大部分是服务端延迟，适当延长读取超时时间
                currentTimes++;
                if (currentTimes <= reConnTimes) {
                    soTimeOut *= Config.RECONN_INCREASING_STEP;
                    connTimeOut *= Config.RECONN_INCREASING_STEP;
                    Logger.w(TAG, "Socket timeout, retrying... Times remain " + (reConnTimes - currentTimes + 1) + "\n" +
                            "Increase SoTimeout to " + soTimeOut / 1000 + "s, ConnTimeOut to " + connTimeOut / 1000 + "s.\n"
                            + e.getClass().getCanonicalName() + ": " + url);
                    waitToReconnect(Config.RECONN_INTERVAL);
                }
            } catch (IOException e) {
                currentTimes++;
                if (currentTimes <= reConnTimes) {
                    Logger.w(TAG, "IOException, retrying... Times remain " + (reConnTimes - currentTimes + 1) + "\n" +
                            Config.RECONN_INTERVAL / 1000 + "s later to retry.\n"
                            + e.getClass().getCanonicalName() + ": " + url);
                    waitToReconnect(Config.RECONN_INTERVAL);
                }
            }
        }

        // 如果进入到这个位置，说明所有重连已经结束，仍然无法获取数据，则抛出重连失败异常
        throw new TubeException(
                "All connections is failed. Please check the network.",
                TubeException.IO_ERROR_CODE);
    }

    private static HttpURLConnection createUrlConnection(URL targetUrl,
                                                         int connTimeOut,
                                                         int soTimeOut,
                                                         Map<String, String> headers, boolean isHttpDNS)
            throws IOException, TubeException {

        HttpURLConnection conn = openConnection(targetUrl, isHttpDNS);

        if (conn instanceof HttpsURLConnection) {
//            trustAllHosts();
            ((HttpsURLConnection) conn).setHostnameVerifier(DO_NOT_VERIFY);
        }

        conn.setConnectTimeout(connTimeOut);
        conn.setReadTimeout(soTimeOut);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("User-Agent", Config.USER_AGENT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            System.setProperty("http.keepAlive", "false");
        }

        if (headers != null && !headers.isEmpty()) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }

        return conn;
    }

    private static HttpURLConnection openConnection(URL targetUrl, boolean isHttpDNS) throws IOException {
        String host = targetUrl.getHost();
        int port = targetUrl.getPort();
        if (port == -1) {
            port = 80;
        }

        if (isHttpDNS && !NetworkAccess.isIpv4(host) && !"https".equalsIgnoreCase(targetUrl.getProtocol())) {
            String ip = null;

            HttpDns dns = HttpDnsService.getService(TencentDns.class);

            if (dns != null) {
                ip = dns.getIpByHostSync(host);
            }

            if (!TextUtils.isEmpty(ip)) {
                if (null != NetUtils.createProxy(ip, port)) {
                    return (HttpURLConnection) targetUrl.openConnection(NetUtils.createProxy(ip, port));
                }

            } else {
                Logger.w(TAG, "Error to get httpdns ip, degrade to localdns");
            }
        }

        return (HttpURLConnection) targetUrl.openConnection();
    }

    private static TubeResponse doHead(HttpURLConnection conn,
                                       TubeOptions opt,
                                       boolean isHttpDNS,
                                       int currentDeep)
            throws IOException, TubeException {
        if (opt.mRange != null) {
            opt.mRange.setHeaders(conn);
        }

        conn.setRequestMethod("HEAD");
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        String requestHeaders = getRequestHeader(conn);

        conn.connect();

        return getResponse(conn, opt, isHttpDNS, currentDeep, requestHeaders);
    }

    private static TubeResponse doGet(HttpURLConnection conn,
                                      TubeOptions opt,
                                      boolean isHttpDNS,
                                      int currentDeep)
            throws IOException, TubeException {

        if (opt.mRange != null) {
            opt.mRange.setHeaders(conn);
        }

        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        String requestHeaders = getRequestHeader(conn);

        conn.connect();

        return getResponse(conn, opt, isHttpDNS, currentDeep, requestHeaders);
    }

    private static TubeResponse doPost(HttpURLConnection conn,
                                       TubeOptions opt,
                                       boolean isHttpDNS,
                                       int currentDeep)
            throws IOException, TubeException {

        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);

        // 获取OutputStream并输出 Begin
        if (opt.mPostBody != null) {
            conn.setRequestProperty("Content-Type", opt.mPostBody.getContentType());
            if (opt.isPostInGzip) {
                conn.setRequestProperty("Content-Encoding", "gzip");
                conn.setChunkedStreamingMode(0);
            }
            conn.setDoOutput(true);
        }

        //必须在此处获取头信息，否则连接后无法再次获取
        String requestHeaders = getRequestHeader(conn);

        if (opt.mPostBody != null) {

            BufferedOutputStream bos = null;
            if (opt.isPostInGzip) {
                bos = new BufferedOutputStream(new GZIPOutputStream(conn.getOutputStream()));
            } else {
                bos = new BufferedOutputStream(conn.getOutputStream());
            }

            if (opt.mPostBody instanceof MultipartBody) {

                ((MultipartBody) opt.mPostBody).request(bos);

            } else {

                BufferedInputStream bis = new BufferedInputStream(opt.mPostBody.getInputStream());

                IOUtils.streamCopy(bis, bos);
            }

        }
        // End

        return getResponse(conn, opt, isHttpDNS, currentDeep, requestHeaders);
    }

    private static TubeResponse getResponse(HttpURLConnection conn,
                                            TubeOptions opt,
                                            boolean isHttpDNS,
                                            int currentDeep,
                                            String requestHeaders)
            throws TubeException, IOException {

        String responseHeaders;
        String url = conn.getURL().toString();

        long startTime = System.currentTimeMillis();

        responseHeaders = getResponseHeader(conn);

        printLog(url,
                conn.getRequestMethod(),
                requestHeaders,
                responseHeaders,
                (System.currentTimeMillis() - startTime));

        if (opt.isAutoProcessHttpStatus) {
            int sc = conn.getResponseCode();
            switch (sc) {
                case HttpURLConnection.HTTP_OK: // OK时，非分段请求直接返回，分段请求则抛出错误
                    if (opt.mRange != null) {
                        throw new TubeException("We got 200 status in range request, not 206. Exit.",
                                TubeException.SERVER_ERROR_CODE);
                    }
                    break;
                case HttpURLConnection.HTTP_PARTIAL:
                    break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM: // 取出URL重新发起请求

                    if (currentDeep >= Config.REDIRECT_DEEP) {
                        throw new TubeException("We have got 302 redirect code too many times. Stop trying.",
                                TubeException.SERVER_ERROR_CODE);
                    }

                    String location = conn.getHeaderField("location");
                    Logger.d(TAG, "RedirectUrl-->" + location);
                    return execute(location, opt, isHttpDNS, ++currentDeep);
                default:
                    // http状态不正确,主动抛出异常
                    throw new TubeException("HttpStatus is not OK. -> " + sc,
                            TubeException.SERVER_ERROR_CODE);
            }
        }

        TubeResponse resp = new TubeResponse(conn);

        if (currentDeep != 0) {
            resp.setFinalLocation(url);
        }

        return resp;
    }

    private static void waitToReconnect(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {

        }
    }


    private static void trustAllHosts() throws TubeException {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }

                    public void checkClientTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }
                }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw new TubeException(e, TubeException.HTTPS_ERROR_CODE);
        }
    }

    private static void printLog(String url, String method, String reqHeaders, String respHeaders, long respTime) {
        if (Logger.isDebuggable()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Request: \n    " + url + "\n");
            sb.append("Request Method: \n    " + method + "\n");
            sb.append("Request Headers: \n" + reqHeaders);

            if (!TextUtils.isEmpty(respHeaders)) {
                sb.append("Response Headers: \n" + respHeaders);

            }
            sb.append("Response time: " + respTime + "ms.\n");

            String log = sb.toString();
            Logger.d(TAG, log);
        }
    }

    //读取请求头
    private static String getRequestHeader(HttpURLConnection conn) {
        //https://github.com/square/okhttp/blob/master/okhttp-urlconnection/src/main/java/okhttp3/internal/huc/HttpURLConnectionImpl.java#L236
        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
        if (requestHeaderMap == null || requestHeaderMap.isEmpty()) {
            return "";
        }
        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
            sbRequestHeader.append("    ");

            sbRequestHeader.append(requestHeaderKey);
            sbRequestHeader.append(":");
            sbRequestHeader.append(requestHeaderValue);
            sbRequestHeader.append("\n");
        }
        return sbRequestHeader.toString();
    }

    //读取响应头
    private static String getResponseHeader(HttpURLConnection conn) {
        Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
        if (responseHeaderMap == null || responseHeaderMap.isEmpty()) {
            return "";
        }

        Iterator<String> responseHeaderIterator = responseHeaderMap.keySet().iterator();
        StringBuilder sbResponseHeader = new StringBuilder();
        while (responseHeaderIterator.hasNext()) {
            String responseHeaderKey = responseHeaderIterator.next();
            String responseHeaderValue = conn.getHeaderField(responseHeaderKey);
            sbResponseHeader.append("    ");
            if (responseHeaderKey != null) {
                sbResponseHeader.append(responseHeaderKey);
                sbResponseHeader.append(":");
            }
            sbResponseHeader.append(responseHeaderValue);
            sbResponseHeader.append("\n");
        }

        return sbResponseHeader.toString();
    }
}
