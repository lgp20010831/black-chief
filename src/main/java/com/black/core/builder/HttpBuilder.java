package com.black.core.builder;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.http.code.HttpResponseWrapper;
import com.black.core.http.code.HttpUtils;
import com.black.core.http.code.HttpsException;
import com.black.core.util.Assert;
import com.black.core.util.Body;
import com.black.core.util.StringUtils;
import com.black.syntax.SyntaxUtils;
import com.black.utils.ServiceUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpBuilder {

    public static PartSession part(){return new PartSession();}

    public static PostSession post(){return new PostSession();}

    public static GetSession get(){
        return new GetSession();
    }

    public static PartSession part(String url, Object... params){
        return new PartSession(url, params);
    }

    public static PostSession post(String url, Object... params){
        return new PostSession(url, params);
    }

    public static GetSession get(String url, Object... params){
        return new GetSession(url, params);
    }

    public static abstract class Session{
        String url;
        final Map<String, String> headers = new HashMap<>();
        public Session(){}

        public Session(String url) {
            this(url, new Object[0]);
        }

        public Session(String url, Object... queryParams){
            if (!StringUtils.hasText(url)){
                return;
            }
            try {
                ApplicationConfigurationReader handler = ApplicationConfigurationReaderHolder.getReader();
                Map<String, String> source = handler.getMasterAndSubApplicationConfigSource();
                url = escape(HttpUtils.parseUrl(url, "${", "}", source));
            }catch (Throwable e){
                url = escape(url);
            }finally {
                this.url = url;
            }
            if (queryParams.length > 0){
                Map<Object, Object> indexParamMap = SyntaxUtils.indexParamMap(queryParams);
                this.url = ServiceUtils.parseTxt(this.url, "#{", "}", index -> {
                    return String.valueOf(indexParamMap.get(Integer.parseInt(index)));
                });
            }
        }

        public Session url(String url){
            this.url = url;
            return this;
        }

        private String escape(String url){
            url = url.replace(" ", "%20");
            url = url.replace("#", "%23");
            return url;
        }
        public Session putHeaders(Map<String, String> headers){
            if (headers != null)
                this.headers.putAll(headers);
            return this;
        }

        public Session useSSl(boolean use){
            if (use){
                return ssl();
            }
            return this;
        }

        public Session ssl(){
            HttpUtils.useSSL();
            return this;
        }
        public abstract HttpResponseWrapper execute();
        public String executeAndGetBody(){
            return execute().body();
        }
        public JHexByteArrayInputStream executeAndGetInputStream(){
            return execute().inputStream();
        }
        public byte[] executeAndGetBytes(){
            return execute().bytes();
        }
    }

    public static class PostSession extends Session{

        String body;

        public PostSession(){}

        public PostSession(String url, Object... params) {
            super(url, params);
        }

        @Override
        public PostSession url(String url) {
            return (PostSession) super.url(url);
        }

        public PostSession body(Map<String, Object> map){
            return body(new Body(map).toString());
        }

        public PostSession body(String body){
            this.body = body;
            return this;
        }

        public PostSession putHeader(String str, String val){
            headers.put(str, val);
            return this;
        }

        public PostSession putHeaders(Map<String, String> headers){
            if (headers != null)
                this.headers.putAll(headers);
            return this;
        }


        @Override
        public HttpResponseWrapper execute(){
            Assert.notNull(url, "url is null");
            return HttpUtils.sendPostJson(url, body == null ? "" : body, headers);
        }
    }

    public static class GetSession extends Session{

        public GetSession(){}

        public GetSession(String url, Object... params) {
            super(url, params);
        }

        @Override
        public GetSession url(String url) {
            return (GetSession) super.url(url);
        }

        public GetSession putHeader(String str, String val){
            headers.put(str, val);
            return this;
        }

        public GetSession putHeaders(Map<String, String> headers){
            if (headers != null)
                this.headers.putAll(headers);
            return this;
        }

        @Override
        public HttpResponseWrapper execute(){
            Assert.notNull(url, "url is null");
            return HttpUtils.sendGetJson(url, headers);
        }
    }


    public static class PartSession extends Session{
        private final MultipartEntityBuilder builder;

        public PartSession(){
            this("");
        }

        public PartSession(String url, Object... params) {
            super(url, params);
            builder = MultipartEntityBuilder.create();
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);
        }

        @Override
        public PartSession url(String url) {
            return (PartSession) super.url(url);
        }

        public PartSession putHeaders(Map<String, String> headers){
            if (headers != null)
                this.headers.putAll(headers);
            return this;
        }


        public PartSession addMultipartFile(String name, MultipartFile file){
            try {
                return addInputPart(name, file.getInputStream());
            } catch (IOException e) {
                throw new HttpsException(e);
            }
        }

        public PartSession addInputPart(String name, InputStream inputStream){
            builder.addBinaryBody(name, inputStream);
            return this;
        }

        public PartSession addBytePart(String name, byte[] array){
            builder.addBinaryBody(name, array);
            return this;
        }

        public PartSession addFilePart(String name, File file){
            builder.addBinaryBody(name, file);
            return this;
        }

        public PartSession addStringPart(String name, String body){
            ContentType contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8);
            StringBody stringBody = new StringBody(body, contentType);
            builder.addPart(name, stringBody);
            return this;
        }

        @Override
        public HttpResponseWrapper execute() {
            Assert.notNull(url, "url is null");
            return HttpUtils.sendPostPart(url, builder);
        }
    }
}
