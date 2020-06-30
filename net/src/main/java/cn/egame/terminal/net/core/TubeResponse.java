package cn.egame.terminal.net.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import cn.egame.terminal.utils.Logger;

/**
 * Created by hanwei on 2017/5/18.
 */

public class TubeResponse {

    private static final String TAG = "TUBE";
    private HttpURLConnection mConn = null;

    private String mFinalLocation = null;


    /**
     * 唯一构造方法
     */
    protected TubeResponse(HttpURLConnection conn) {
        mConn = conn;
    }

    protected void setFinalLocation(String location) {
        this.mFinalLocation = location;
    }

    public String getUrl() {
        if (mFinalLocation != null) {
            return mFinalLocation;
        }

        return mConn.getURL().toString();
    }

    /**
     * 自动读取entity中的流为String ，读取后无法再次读取
     *
     * @return
     */
    public String toString() {

        if (mConn == null) {
            return null;
        }

        try {
            String result = readString(mConn.getContentEncoding(), toStream());
            Logger.printJson(TAG, result);
            return result;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Logger.w(TAG, e.getMessage());
        } finally {
            close();
        }

        return null;
    }

    /**
     * 获得entity中的流对象
     *
     * @return
     */
    public InputStream toStream() throws IOException {
        return mConn.getInputStream();
    }

    public boolean isOK() throws IOException {
        return mConn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    public int getResponseCode() throws IOException {
        return mConn.getResponseCode();
    }

    public int getContentLength() {
        return mConn.getContentLength();
    }

    public String getName() {
        return mConn.getURL().getFile();
    }

    public String getHead(String headKey) {
        return mConn.getHeaderField(headKey);
    }

    /**
     * 关闭此次连接并释放资源
     */
    public void close() {
        if (mConn != null) {
            mConn.disconnect();
        }
    }


    /**
     * 兼容Chunked模式
     */
    private static String readString(String contentEncoding, InputStream is)
            throws IOException {

        StringBuffer sb = new StringBuffer();

        try {
            if ("gzip".equals(contentEncoding)) {
                is = new BufferedInputStream(new GZIPInputStream(is));
            } else {
                is = new BufferedInputStream(is);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }

            return sb.toString();
        } catch (IOException e) {
            Logger.w(TAG, "Error reading InputStream: \n" + e.getMessage());
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Logger.w(TAG, "Error closing InputStream: \n" + e.getMessage());
                }
            }
        }
    }
}
