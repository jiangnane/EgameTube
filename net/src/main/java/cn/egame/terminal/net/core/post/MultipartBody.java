package cn.egame.terminal.net.core.post;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * FileName:    MultipartBody.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      jianghao
 * Description: 文件描述
 * History:     2018/1/2 1.00初始版本
 */

/*Example:

        MultipartBody multipartBody = new MultipartBody.Builder()
        .addFilePart("down", new File(filePath))
        .addBinaryPart("icon", "".getBytes(), "filename")
        .addTextPart("text", "123123321321")
        .build();

        TubeOptions opt = new TubeOptions.Builder().setPostBody(multipartBody).create();*/

public class MultipartBody implements PostBody {

    private final static String TWO_HYPHENS = "--";
    private final static String LINE_END = "\r\n";
    private final static String BOUNDARY = "--WebKitFormBoundaryxNotbR68VCn0Kghw";
    private final static String MIME_TYPE = "multipart/form-data; boundary=" + BOUNDARY;

    private HashMap<String, String> formFields;
    private HashMap<String, BinaryField> formFile;
    private static final String CHARSET = "utf-8";

    private MultipartBody(Builder builder) {
        formFields = builder.formFields;
        formFile = builder.formFile;
    }

    public static class Builder {

        HashMap<String, String> formFields = new HashMap<>();
        HashMap<String, BinaryField> formFile = new HashMap<>();

        /**
         * 文本格式
         *
         * @param name
         * @param value
         * @return
         */
        public Builder addTextPart(String name, String value) {
            formFields.put(name, value);
            return this;
        }

        /**
         * 二进制格式
         *
         * @param name
         * @param data
         * @param fileName
         * @return
         */
        public Builder addBinaryPart(String name, byte[] data, String fileName) {
            formFile.put(name, new ByteArrayField(fileName, data));
            return this;
        }

        /**
         * 不包含文件名的文件格式
         *
         * @param name
         * @param file
         * @return
         */
        public Builder addFilePart(String name, File file) {
            formFile.put(name, new FileField(file));
            return this;
        }

        /**
         * 包含文件名的文件格式
         *
         * @param name
         * @param file
         * @param fileName
         * @return
         */
        public Builder addFilePart(String name, File file, String fileName) {
            formFile.put(name, new FileField(file, fileName));
            return this;
        }

        public MultipartBody build() {
            return new MultipartBody(this);
        }

    }

    @Override
    public String getContentType() {
        return MIME_TYPE;
    }

    @Override
    public InputStream getInputStream() throws IOException {

        return null;
    }

    /**
     * 拿到connection的输出流并添加multipart标准格式协议
     *
     * @param outputStream
     * @throws IOException
     */
    public void request(BufferedOutputStream outputStream) throws IOException {

        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        for (Map.Entry<String, String> entry : formFields.entrySet()) {
            dataOutputStream.writeBytes(TWO_HYPHENS);
            dataOutputStream.writeBytes(BOUNDARY);
            dataOutputStream.writeBytes(LINE_END);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
            dataOutputStream.writeBytes("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
            dataOutputStream.writeBytes(LINE_END);
            dataOutputStream.writeBytes(entry.getValue());
            dataOutputStream.writeBytes(LINE_END);
        }

        for (Map.Entry<String, BinaryField> entry : formFile.entrySet()) {

            BinaryField binaryField = entry.getValue();
            dataOutputStream.writeBytes(TWO_HYPHENS);
            dataOutputStream.writeBytes(BOUNDARY);
            dataOutputStream.writeBytes(LINE_END);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + binaryField.getFileName() + "\"" + LINE_END);
            dataOutputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(binaryField.getFileName()) + LINE_END);
            dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_END);
            dataOutputStream.writeBytes(LINE_END);

            InputStream inputStream = binaryField.getStream();

            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                inputStream.close();
            }
            dataOutputStream.writeBytes(LINE_END);

        }

        dataOutputStream.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS);
        dataOutputStream.writeBytes(LINE_END);
        dataOutputStream.flush();

        if (dataOutputStream != null) {
            dataOutputStream.close();
        }

    }

    /**
     * 二进制基类
     */
    private abstract static class BinaryField implements Closeable {
        String fileName;
        InputStream stream;

        public BinaryField(String fileName) {
            super();
            setFileName(fileName);
        }

        public InputStream getStream() throws IOException {
            if (stream != null) return stream;
            else
                return stream = openStream();
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String name) {
            fileName = name;
        }

        protected abstract InputStream openStream() throws IOException;

        @Override
        public void close() throws IOException {
            if (stream != null) stream.close();
        }
    }

    /**
     * 字节数组流实现
     */
    private static class ByteArrayField extends BinaryField {
        byte[] mData;

        public ByteArrayField(String fileName, byte[] data) {
            super(fileName);
            mData = data;
        }

        @Override
        protected InputStream openStream() throws IOException {
            return new ByteArrayInputStream(mData);
        }
    }

    /**
     * 文件流实现
     */
    private static class FileField extends BinaryField {
        File file;

        public FileField(File file) {
            this(file, file.getName());
        }

        public FileField(File file, String name) {
            super(name);
            this.file = file;
        }

        @Override
        protected InputStream openStream() throws IOException {
            return new FileInputStream(file);
        }
    }
}
