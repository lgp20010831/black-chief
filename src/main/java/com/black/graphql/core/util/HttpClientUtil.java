
package com.black.graphql.core.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class HttpClientUtil {
    public HttpClientUtil() {
    }

    public String doGet(String url, Map<String, String> param) throws IOException, URISyntaxException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resultString = "";
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                Iterator var7 = param.keySet().iterator();

                while(var7.hasNext()) {
                    String key = (String)var7.next();
                    builder.addParameter(key, (String)param.get(key));
                }
            }

            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } finally {
            if (response != null) {
                response.close();
            }

            httpclient.close();
        }

        return resultString;
    }

    public String doGet(String url) throws IOException, URISyntaxException {
        return this.doGet(url, (Map)null);
    }

    public String doPost(String url, Map<String, String> param, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            HttpPost httpPost = new HttpPost(url);
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList();
                Iterator var9 = param.keySet().iterator();

                while(var9.hasNext()) {
                    String key = (String)var9.next();
                    paramList.add(new BasicNameValuePair(key, (String)param.get(key)));
                }

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, "utf-8");
                httpPost.setEntity(entity);
            }

            if (headers != null) {
                Iterator var14 = headers.keySet().iterator();

                while(var14.hasNext()) {
                    String key = (String)var14.next();
                    httpPost.addHeader(key, (String)headers.get(key));
                }
            }

            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } finally {
            if (response != null){
                response.close();
            }
        }

        return resultString;
    }

    public String doPost(String url, Map<String, String> param) throws IOException {
        return this.doPost(url, param, (Map)null);
    }

    public String doPost(String url) throws IOException {
        return this.doPost(url, (Map)null, (Map)null);
    }

    public String doPostJson(String url, String json) throws IOException {
        return this.doPostJson(url, json, (Map)null);
    }

    public String doPostJson(String url, String json, Map<String, String> headers) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = null;

        try {
            HttpPost httpPost = new HttpPost(url);
            if (headers != null) {
                Iterator var8 = headers.keySet().iterator();

                while(var8.hasNext()) {
                    String key = (String)var8.next();
                    httpPost.addHeader(key, (String)headers.get(key));
                }
            }

            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } finally {
            if (response != null){
                response.close();
            }

        }

        return resultString;
    }
}
