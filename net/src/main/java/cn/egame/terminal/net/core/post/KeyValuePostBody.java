package cn.egame.terminal.net.core.post;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import cn.egame.terminal.net.utils.ParamsSplice;

/**
 * Created by hanwei on 2017/5/19.
 */

public class KeyValuePostBody extends TextPostBody {

    private Map<String, String> mBody = null;

    public KeyValuePostBody(Map<String, String> body) {
        mBody = body;
        init(map2Text(mBody), "utf-8");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return super.getInputStream();
    }

    private String map2Text(Map<String, String> body) {
        Set<String> keys = body.keySet();

        ParamsSplice ps = new ParamsSplice(false);
        for (String key : keys) {
            ps.append(key, body.get(key));
        }

        return ps.toString();
    }
}
