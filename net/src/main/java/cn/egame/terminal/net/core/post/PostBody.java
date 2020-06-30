package cn.egame.terminal.net.core.post;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanwei on 2017/5/19.
 */

public interface PostBody {

    String TAG = "PostBody";

    String getContentType();

    InputStream getInputStream() throws IOException;
}
