package cn.egame.terminal.net.core.post;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanwei on 2017/5/19.
 */

public class JSONPostBody extends TextPostBody {

    private Object mBody = null;

    public JSONPostBody(JSONObject obj) {
        mBody = obj;
        init();
    }

    public JSONPostBody(JSONArray array) {
        mBody = array;
        init();
    }

    private void init() {
        init(mBody.toString(), "utf-8");
        setContentType("application/json");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return super.getInputStream();
    }

}
