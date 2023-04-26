package com.black.core.http.code;

import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.http.annotation.*;
import com.black.utils.LocalObject;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.core.annotation.AnnotationUtils;


import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class HttpUtils {

    private static final String CONFIG_START = "${";

    private static final String CONFIG_END = "}";

    private static final String PARAM_START = "#{";

    private static final String PARAM_END = "}";

    public static String ENCODING = "utf-8";

    private static LocalObject<Boolean> sslLocal = new LocalObject<>(() -> false);

    public static HostnameVerifier default_hostname_verifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    public static TrustStrategy trustStrategy = new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    };

    public static void useSSL(){
        sslLocal.set(true);
    }

    public static void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        HttpUtils.default_hostname_verifier = hostnameVerifier;
    }

    public static void setTrustStrategy(TrustStrategy trustStrategy) {
        HttpUtils.trustStrategy = trustStrategy;
    }

    private static CloseableHttpClient getHttpClient(){
        if (sslLocal.current()) {
            return getSSLClient();
        }
        return getDefaultClient();
    }

    public static CloseableHttpClient getSSLClient(){
        trustEveryone();
        return createSSLClientDefault();
    }

    public static CloseableHttpClient getDefaultClient(){
        return HttpClients.createDefault();
    }

    /**
     * 信任任何站点，实现https页面的正常访问
     *
     */

    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(default_hostname_verifier);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Throwable e) {
            throw new HttpsException("trust ssl connect error", e);
        }
    }

    public static CloseableHttpClient createSSLClientDefault() {
        try {
            //使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, trustStrategy).build();
            //NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
            //有效的SSL会话并匹配到目标主机。
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (Throwable e) {
            throw new HttpsException(e);
        }
    }

    public static void finishFetch(){
        endExecute();
    }

    private static void endExecute(){
        sslLocal.removeCurrent();
    }

    public static HttpResponseWrapper sendPostPart(String url, MultipartEntityBuilder builder){
        HttpPost httpPost = null;
        HttpResponse response;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            httpPost = new HttpPost(url);
            //设置请求参数
            HttpEntity httpEntity = builder.build();
            httpPost.setEntity(httpEntity);
            try {
                response = httpClient.execute(httpPost);
            } catch (IOException e) {
                throw new HttpsException(e);
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (log.isInfoEnabled()) {
                    log.info("http response error code: {}", statusCode);
                }
            }
            return new HttpResponseWrapper(response);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            endExecute();
        }
    }

    public static HttpResponseWrapper sendPostJson(String url, String json, Map<String, String> header) {
        HttpPost httpPost = null;
        HttpResponse response;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            try {
                response = httpClient.execute(httpPost);
            } catch (IOException e) {
                throw new HttpsException(e);
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (log.isInfoEnabled()) {
                    log.info("http response error code: {}", statusCode);
                }
            }
            return new HttpResponseWrapper(response);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            endExecute();
        }
    }


    public static HttpResponseWrapper sendGetJson(String url, Map<String, String> header)  {
        HttpGet httpGet = null;
        HttpResponse response;
        try {
            CloseableHttpClient httpClient = getHttpClient();
            httpGet = new HttpGet(url);
            httpGet.setHeader("Content-type", "application/json; charset=utf-8");
            httpGet.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            try {
                response = httpClient.execute(httpGet);
            } catch (IOException e) {
                throw new HttpsException(e);
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (log.isInfoEnabled()) {
                    log.info("http response error code: {}", statusCode);
                }
            }
            return new HttpResponseWrapper(response);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            endExecute();
        }
    }


    public static String resolveResponse(HttpResponse response){
        HttpEntity entity = response.getEntity();
        try {
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            throw new HttpsException(e);
        }
    }

    @Deprecated
    public static String parseResponse(HttpResponse response){
        HttpEntity entity = response.getEntity();
        try {

            InputStream content = entity.getContent();
            byte[] buffer = new byte[content.available()];
            int size = content.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            throw new HttpsException(e);
        }
    }

    public static Map<String, String> parseHeaders(Method method, Object[] args){
        Parameter[] parameters = method.getParameters();
        if (args.length != parameters.length){
            throw new HttpsException("args.size != params.size");
        }

        Map<String, String> headerMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            HttpHeader header = AnnotationUtils.getAnnotation(parameter, HttpHeader.class);
            if (header != null){
                Object arg = args[i];
                if (arg != null){
                    String val = arg.toString();
                    headerMap.put(header.value(), val);
                }
            }

            HttpHeaders httpHeaders = AnnotationUtils.getAnnotation(parameter, HttpHeaders.class);
            if (httpHeaders != null){
                if (!Map.class.isAssignableFrom(type)){
                    throw new HttpsException("http headers type must is map");
                }
                Map<String, String> hm = (Map<String, String>) args[i];
                if (hm != null){
                    headerMap.putAll(hm);
                }
            }
        }
        return headerMap;
    }

    public static String parseJsonBody(Method method, Object[] args){
        Parameter[] parameters = method.getParameters();
        if (args.length != parameters.length){
            throw new HttpsException("args.size != params.size");
        }

        String jsonString = null;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            JsonBody body = AnnotationUtils.getAnnotation(parameter, JsonBody.class);
            if (body != null){
                Object arg = args[i];
                if (arg != null){
                    Class<?> type = parameter.getType();
                    if (!String.class.isAssignableFrom(type)){
                        jsonString = args[i].toString();
                    }
                }
                break;
            }
        }
        return jsonString;
    }

    public static String parse(Method method, String txt, Object[] args){
        Map<String, String> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        if (args.length != parameters.length){
            throw new HttpsException("args.size != params.size");
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();
            UrlString urlString = AnnotationUtils.getAnnotation(parameter, UrlString.class);
            if (urlString != null){
                Object arg = args[i];
                if (arg == null){
                    throw new HttpsException("url string value must not be null");
                }
                String val = arg.toString();
                map.put(urlString.value(), val);
            }
            UrlMap urlMap = AnnotationUtils.getAnnotation(parameter, UrlMap.class);
            if(urlMap != null){
                if (!Map.class.isAssignableFrom(type)){
                    throw new HttpsException("urlMap type must is map");
                }
                Map<String, Object> uM = (Map<String, Object>) args[i];
                if (uM == null){
                    throw new HttpsException("url Map value must not be null");
                }
                Map<String, String> umc = new HashMap<>();
                uM.forEach((k, v) -> {
                    if (v != null){
                        umc.put(k, v.toString());
                    }
                });
                map.putAll(umc);
            }
        }
        String url = txt;
        while (url.contains(PARAM_START)){
            url = HttpUtils.parseUrl(url, PARAM_START, PARAM_END, map);
        }

        if (url.contains(CONFIG_START)){
            Map<String, String> source = new HashMap<>();
            Map<String, String> ss = ApplicationConfigurationReaderHolder.getReader().getSubApplicationConfigSource();
            if (ss != null){
                source.putAll(ss);
            }
            Map<String, String> ms = ApplicationConfigurationReaderHolder.getReader().getMasterApplicationConfigSource();
            if (ms != null){
                source.putAll(ms);
            }
            url = HttpUtils.parseUrl(url, CONFIG_START, CONFIG_END, source);
        }
        return url;
    }


    public static String parseUrl(String txt, String start, String end, Map<String, String> mapValue){

        StringBuilder builder = new StringBuilder();
        String machingStr = txt;
        //首先判断参数填充
        //http:#{url}/#{mapping}/#{a}?value=#{value}&age=#{age}
        int processor = 0;
        int i = machingStr.indexOf(start);
        int lastEndIndex = 0;
        while (i != -1){
            lastEndIndex = machingStr.indexOf(end);
            if (lastEndIndex == -1){
                throw new HttpsException("Missing Terminator: " + end);
            }

            if (i != 0){
                builder.append(machingStr, 0, i);
            }

            String key = machingStr.substring(i + start.length(), lastEndIndex);
            if (!mapValue.containsKey(key)){
                throw new HttpsException("Property name not found: " + key);
            }

            String v = mapValue.get(key);
            if (v == null){
                throw new HttpsException("property value is must not be null: " + key);
            }
            builder.append(v);
            int si = lastEndIndex + end.length();
            machingStr = machingStr.substring(si);
            processor = processor + si;
            i = machingStr.indexOf(start);
        }

        if (lastEndIndex != -1 && lastEndIndex != txt.length() - 1){
            builder.append(txt.substring(processor));
        }
        return builder.toString();
    }
}
