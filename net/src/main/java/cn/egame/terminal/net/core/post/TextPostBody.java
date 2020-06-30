package cn.egame.terminal.net.core.post;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import cn.egame.terminal.utils.Logger;

/**
 * Created by hanwei on 2017/5/19.
 */

public class TextPostBody implements PostBody {

    protected String mText = null;
    protected String mCharset = null;
    private String mContentType = "text/plain";

    protected TextPostBody() {

    }

    public TextPostBody(String text) {
        this(text, "utf-8");
    }

    public TextPostBody(String text, String charset) {
        init(text, charset);
    }

    protected void init(String text, String charset) {
        mText = text;
        mCharset = charset;
    }

    @Override
    public String getContentType() {
        return mContentType;
    }

    public void setContentType(String contentType) {
        mContentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {

        byte[] bytes = getBytes();

        if (bytes == null) {
            throw new IOException("The content is null.");
        }

        return new ByteArrayInputStream(bytes);
    }

    private byte[] getBytes() {

        if (TextUtils.isEmpty(mText)) {
            return null;
        }

        try {
            return mText.getBytes(mCharset);
        } catch (UnsupportedEncodingException e) {
            Logger.w(TAG, e.getMessage());
            return null;
        }
    }

}
