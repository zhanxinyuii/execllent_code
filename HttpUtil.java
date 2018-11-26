package cn.etcp.test.common.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类.
 * Created by wanglianyi on 2017/6/1 0001.
 */
public class HttpUtil {

    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 70000;

    static {
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
    }

    public static String doGetWithCookies(String url, Map<String,String> cookieMap){
        String result="";
        CloseableHttpClient httpClient;
        try {
            // 根据地址获取请求
            HttpGet request = new HttpGet(url);//这里发送get请求
            // 获取当前客户端对象
            if(cookieMap==null||cookieMap.isEmpty()){
                httpClient=HttpClients.createDefault();
            }else {
                httpClient = generateHttpClientIncludeCookie(cookieMap,getDomain(url),"/");
            }

            // 通过请求对象获取响应对象
            HttpResponse response = httpClient.execute(request);

            // 判断网络连接状态码是否正常(0--200都数正常)
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                result= EntityUtils.toString(response.getEntity(),"utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发送 POST 请求（HTTP），K-V形式
     * @param apiUrl API接口URL
     * @param params 参数map
     * @return
     */
    public static String doPost(String apiUrl, Map<String, Object> params) {
        return doPostIncludeCookie(apiUrl,params,null);
    }

    /**
     * 发送POST-JSON形式的数据(包括COOKIE).
     * @param apiUrl 调用地址
     * @param cookieMap cookie数据
     * @return 返回结果
     */
    public static String doPostIncludeCookie(String apiUrl, Map<String,Object> params,Map<String,String> cookieMap) {
        CloseableHttpClient httpClient;
        if(cookieMap==null||cookieMap.isEmpty()){
            httpClient=HttpClients.createDefault();
        } else {
            httpClient=generateHttpClientIncludeCookie(cookieMap,getDomain(apiUrl),"/");
        }
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);
            System.out.println(response.toString());
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

    /**
     * 发送POST-JSON形式的数据(包括COOKIE).
     * @param apiUrl 调用地址
     * @param cookieMap cookie数据
     * @return 返回结果
     */
    public static String doPostIncludeCookieAndHeader(String apiUrl, Map<String,Object> params,Map<String,String> cookieMap,Map<String,String> header) {
        CloseableHttpClient httpClient;
        if(cookieMap==null||cookieMap.isEmpty()){
            httpClient=HttpClients.createDefault();
        } else {
            httpClient=generateHttpClientIncludeCookie(cookieMap,getDomain(apiUrl),"/");
        }
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpPost.setHeader(entry.getKey(),entry.getValue());
            }
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);
            System.out.println(response.toString());
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     * @param apiUrl
     * @param json json对象
     * @return
     */
    public static String doPostJson(String apiUrl, Object json) {
        return doJSONIncludeCookie(apiUrl,json,null);
    }

    /**
     * 发送POST-JSON形式的数据(包括COOKIE).
     * @param apiUrl 调用地址
     * @param json 发送数据
     * @param cookieMap cookie数据
     * @return 返回结果
     */
    public static String doDelete(String apiUrl) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String httpStr = null;
        HttpDelete httpDelete = new HttpDelete(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpDelete.setConfig(requestConfig);
            response = httpClient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

    /**
     * 发送POST-JSON形式的数据(包括COOKIE).
     * @param apiUrl 调用地址
     * @param json 发送数据
     * @param cookieMap cookie数据
     * @return 返回结果
     */
    public static String doJSONIncludeCookie(String apiUrl, Object json,Map<String,String> cookieMap) {
        CloseableHttpClient httpClient;
        if(cookieMap==null||cookieMap.isEmpty()){
            httpClient = HttpClients.createDefault();
        }else{
            httpClient=generateHttpClientIncludeCookie(cookieMap,getDomain(apiUrl),"/");
        }
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json.toString(),"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

    /**
     * 发送 POST 请求（HTTP），JSON形式
     * @param apiUrl
     * @return
     */
    public static String doPostXML(String apiUrl, String xml) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String httpStr = null;
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(xml.toString(),"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/xml");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            System.out.println(response.getStatusLine().getStatusCode());
            httpStr = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }


    /**
     * 发送 SSL POST 请求（HTTPS），K-V形式
     * @param apiUrl API接口URL
     * @param params 参数map
     * @return
     */
    public static String doPostSSL(String apiUrl, Map<String, Object> params) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr;
        try {
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("utf-8")));

            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
            return httpStr;
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String doPostSSLXML(String apiUrl,String xml,String charSet) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(xml.toString(),charSet);//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/xml");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
            }
            httpStr = EntityUtils.toString(entity, charSet);
            return httpStr;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 发送 SSL POST 请求（HTTPS），JSON形式
     * @param apiUrl API接口URL
     * @param json JSON对象
     * @return
     */
    public static String doPostSSLJSON(String apiUrl, Object json) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        String httpStr = null;

        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json.toString(),"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            httpStr = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return httpStr;
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }





    private static CloseableHttpClient generateHttpClientIncludeCookie (Map<String,String> cookieMap,String domain,String path){
        BasicCookieStore cookieStore = new BasicCookieStore();
        for(Map.Entry<String,String> entry:cookieMap.entrySet()){
            BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
            cookie.setDomain(domain);
            cookie.setVersion(0);
            cookie.setPath(path);
            cookieStore.addCookie(cookie);
        }
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        return httpClient;
    }


    private static String getDomain(String url){
        String domain=url.replaceAll("http://","").replaceAll("https://","");
        domain=domain.substring(0,domain.indexOf("/"));
        if(domain.contains(":")){
            domain=domain.substring(0,domain.indexOf(":"));
        }
        return domain;
    }

    public static PoolingHttpClientConnectionManager getConnMgr() {
        return connMgr;
    }

    public static void setConnMgr(PoolingHttpClientConnectionManager connMgr) {
        HttpUtil.connMgr = connMgr;
    }

    public static RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public static void setRequestConfig(RequestConfig requestConfig) {
        HttpUtil.requestConfig = requestConfig;
    }

    public static int getMaxTimeout() {
        return MAX_TIMEOUT;
    }
}
