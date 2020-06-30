/*
 * FileName:	TubeConfig.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	<文件描述>
 * History:		2013-10-24 1.00 初始版本
 */
package cn.egame.terminal.net.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量，公共配置项
 *
 * @author Hein
 * @see Builder
 */
public class TubeConfig {

    protected int mThreadCount = 0;

    protected boolean isHttpDNSEnable = true;

    protected TubeOptions mDefaultOptions;

    protected Map<String, String> mCommonHeaders = null;

    private TubeConfig() {
    }

    public static TubeConfig getDefault() {
        return new TubeConfig.Builder().create();
    }

    /**
     * TubeConfig 的建造器
     *
     * @author Hein
     */
    public static class Builder {

        private int mThreadCount = 0;

        private boolean isHttpDNSEnable = true;

        private Map<String, String> mHeaders = new HashMap<String, String>();

        private TubeOptions mDefaultOptions = null;

        /**
         * 设置最高并发线程数，0或不设置为不限制
         *
         * @param count
         * @return
         */
        public Builder setThreadCount(int count) {
            mThreadCount = count;
            return this;
        }

        /**
         * 是否禁用http dns服务，默认开启
         * @return
         */
        public Builder disableHttpDns() {
            this.isHttpDNSEnable = false;
            return this;
        }

        /**
         * 设置全局公共请求头,如果设置,则所有请求都携带此头部信息
         *
         * @param headers
         * @return
         */
        public Builder setCommonHeaders(Map<String, String> headers) {
            mHeaders.putAll(headers);
            return this;
        }

        /**
         * 设置默认的Tube请求选项
         *
         * @param options
         * @return
         */
        public Builder setDefaultOptions(TubeOptions options) {
            mDefaultOptions = options;
            return this;
        }

        /**
         * 创建并返回一个TubeConfig实例
         *
         * @return
         */
        public TubeConfig create() {
            TubeConfig cfg = new TubeConfig();

            if (mDefaultOptions == null) {
                mDefaultOptions = new TubeOptions.Builder().create();
            }

            cfg.mThreadCount = mThreadCount;
            cfg.isHttpDNSEnable = isHttpDNSEnable;
            cfg.mCommonHeaders = mHeaders;
            cfg.mDefaultOptions = mDefaultOptions;

            return cfg;
        }
    }

}
