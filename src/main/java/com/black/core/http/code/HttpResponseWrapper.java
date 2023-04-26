package com.black.core.http.code;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.query.Wrapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class HttpResponseWrapper implements Wrapper<HttpResponse> {

    private final HttpResponse response;

    private String body;

    private byte[] bytes;

    private final HttpEntity entity;

    private final JHexByteArrayInputStream inputStream;

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public HttpResponseWrapper(HttpResponse response) {
        this.response = response;
        entity = response.getEntity();
        bytes = readBytes0();
        if (bytes == null){
            bytes = new byte[0];
        }
        inputStream = new JHexByteArrayInputStream(bytes);
    }

    public HttpResponse response() {
        return response;
    }

    public HttpEntity entity() {
        return entity;
    }

    public String body() {
        if (body == null){
            try {
                ContentType contentType = ContentType.get(entity);
                Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
                        "HTTP entity too large to be buffered in memory");
                int capacity = (int)entity.getContentLength();
                if (capacity < 0) {
                    capacity = DEFAULT_BUFFER_SIZE;
                }
                Charset charset = null;
                if (contentType != null) {
                    charset = contentType.getCharset();
                    if (charset == null) {
                        final ContentType defaultContentType = ContentType.getByMimeType(contentType.getMimeType());
                        charset = defaultContentType != null ? defaultContentType.getCharset() : null;
                    }
                }
                if (charset == null) {
                    charset = HTTP.DEF_CONTENT_CHARSET;
                }
                final Reader reader = new InputStreamReader(inputStream, charset);
                final CharArrayBuffer buffer = new CharArrayBuffer(capacity);
                final char[] tmp = new char[1024];
                int l;
                while((l = reader.read(tmp)) != -1) {
                    buffer.append(tmp, 0, l);
                }
                return body = buffer.toString();
            }catch (Throwable e){
                throw new HttpsException(e);
            }finally {
                try {
                    inputStream.reset();
                } catch (IOException e) {}
            }
        }
        return body;
    }

    public byte[] bytes(){
        return bytes;
    }

    private byte[] readBytes0(){
        try {
            return EntityUtils.toByteArray(entity);
        } catch (IOException e) {
            throw new HttpsException(e);
        }
    }

    public JHexByteArrayInputStream inputStream(){
        return inputStream;
    }

    @Override
    public HttpResponse get() {
        return response;
    }
}
