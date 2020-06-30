/*
 * FileName:	TubeOptions.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	每个请求所使用的连接选项
 * History:		2013-10-24 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import java.net.HttpURLConnection;
import java.util.Map;

import cn.egame.terminal.net.core.post.PostBody;


/**
 * 每个请求所使用的连接选项
 *
 * @author Hein
 * @see Builder
 */
public class TubeOptions {

    /**
     * 使用HTTP GET 发起请求
     */
    public static final int HTTP_METHOD_GET = 0;

    /**
     * 使用HTTP POST 发起请求
     */
    public static final int HTTP_METHOD_POST = 1;

    public static final int HTTP_METHOD_HEAD = 2;

    protected int mSoTimeOut = -1;

    protected int mConnTimeOut = -1;

    protected int mReConnTimes = -1;

    protected Map<String, String> mMapHeaders = null;

    protected PostBody mPostBody = null;

    protected boolean isPostInGzip = false;

    protected int mHttpMethod = -1;

    protected HttpRange mRange = null;

    protected boolean isAutoProcessHttpStatus = true;

    private TubeOptions() {

    }

    /**
     * EgameTube建造器
     *
     * @author Hein
     */
    public static class Builder {

        private int mSoTimeOut = Config.SO_TIMEOUT;

        private int mConnTimeOut = Config.CONN_TIMEOUT;

        private int mReConnTimes = Config.RECONN_TIMES;

        private Map<String, String> mMapHeaders = null;

        private PostBody mPostBody = null;

        private boolean isPostInGzip = false;

        private int mHttpMethod = HTTP_METHOD_GET;

        private HttpRange mRange = null;

        private boolean isAutoProcessHttpStatus = true;

        public Builder() {

        }

        /**
         * 设置socket超时时间
         *
         * @param time
         * @return
         */
        public Builder setSoTimeOut(int time) {
            this.mSoTimeOut = time;
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param time
         * @return
         */
        public Builder setConnectionTimeOut(int time) {
            this.mConnTimeOut = time;
            return this;
        }

        /**
         * 设置请求头列表
         *
         * @param headers
         * @return
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.mMapHeaders = headers;
            return this;
        }

        /**
         * @param body
         * @return
         */
        public Builder setPostBody(PostBody body) {
            mPostBody = body;
            return this;
        }

        /**
         * @return
         */
        public Builder setPostInGzip(boolean isPostInGzip) {
            this.isPostInGzip = isPostInGzip;
            return this;
        }

        /**
         * 设置重连尝试次数
         *
         * @param times
         * @return
         */
        public Builder setReconnectionTimes(int times) {
            this.mReConnTimes = times;
            return this;
        }

        /**
         * 设置 HTTP请求方式 ,如果不设置,默认为Get请求,如果设置了postEntity,则默认为Post请求
         *
         * @param method
         * @return
         * @see TubeOptions#HTTP_METHOD_GET HTTP_METHOD_GET
         * @see TubeOptions#HTTP_METHOD_POST HTTP_METHOD_POST
         */
        public Builder setHttpMethod(int method) {
            this.mHttpMethod = method;
            return this;
        }

        public Builder setRange(long start, long end) {
            this.mRange = new HttpRange(start, end);

            return this;
        }

        public Builder setAutoProcessHttpStatus(boolean isAuto) {
            this.isAutoProcessHttpStatus = isAuto;
            return this;
        }

        /**
         * 创建并返回EgameTube对象
         *
         * @return
         */
        public TubeOptions create() {
            TubeOptions option = new TubeOptions();

            option.mConnTimeOut = this.mConnTimeOut;
            option.mSoTimeOut = this.mSoTimeOut;
            option.mReConnTimes = this.mReConnTimes;
            option.mMapHeaders = this.mMapHeaders;
            option.mPostBody = this.mPostBody;
            option.isPostInGzip = this.isPostInGzip;
            option.mHttpMethod = this.mHttpMethod;
            option.mRange = this.mRange;
            option.isAutoProcessHttpStatus = this.isAutoProcessHttpStatus;

            if (this.mPostBody != null) {
                option.mHttpMethod = HTTP_METHOD_POST;
            } else {
                option.mHttpMethod = this.mHttpMethod;
            }

            return option;
        }

    }

    public static class HttpRange {
        private static final String KEY_START = "start";
        private static final String KEY_RANGE = "Range";
        private static final String VALUE_RANGE = "bytes=%d-%s";

        private long mStart = 0;
        private long mEnd = 0;

        public HttpRange(long start, long end) {
            mStart = start;
            mEnd = end;
        }

        public void setHeaders(HttpURLConnection conn) {
            conn.setRequestProperty(KEY_START,
                    String.valueOf(mStart));

            conn.setRequestProperty(KEY_RANGE,
                    String.format(VALUE_RANGE,
                            mStart,
                            (mEnd == 0 ? "" : String.valueOf(mEnd))));
        }
    }
}
