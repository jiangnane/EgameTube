package cn.egame.terminal.net.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanwei on 2017/5/19.
 */

public class IOUtils {

    public static void streamCopy(InputStream is, OutputStream os) throws IOException {
        if (is == null || os == null) {
            throw new IOException("The stream obj is null.");
        }

        try {
            byte[] buf = new byte[8 * 1024];
            int len = -1;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }

            os.flush();
        } catch (IOException e) {
            // TODO: handle exception
            throw e;
        } finally {
            os.close();
            is.close();
        }
    }
}
