package com.janitor.common.http;


import com.janitor.common.json.JsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * ClassName HttpUtil
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 13:26
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private HttpAsyncClientBuilder builder;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String PATCH = "PATCH";
    private static final Map<String, Boolean> SUPPORT_METHOD = new HashMap<String, Boolean>() {
        {
            this.put(GET, true);
            this.put(PUT, true);
            this.put(POST, true);
            this.put(DELETE, true);
            this.put(PATCH, true);
        }
    };
    private static final Lock REENTRANT_LOCK = new ReentrantLock();
    private CloseableHttpAsyncClient httpClient;
    private int connectTimeout = 3000;
    private int socketTimeout = 3000;
    private int connectRequestTimeout = 2000;
    private int maxConnections = 100;
    private int maxConnectionsPerRoute = 100;
    private boolean ignoreVerifySsl = true;
    private SSLContext sslContext;

    public HttpUtil() {
    }

    public HttpUtil setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public HttpUtil setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public HttpUtil setConnectRequestTimeout(int connectRequestTimeout) {
        this.connectRequestTimeout = connectRequestTimeout;
        return this;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void build() throws KeyManagementException, NoSuchAlgorithmException, IOReactorException {
        if (this.builder == null) {
            Class<HttpUtil> httpUtilClass = HttpUtil.class;
            synchronized (HttpUtil.class) {
                if (this.ignoreVerifySsl) {
                    this.sslContext = this.ignoreVerifySsl();
                }

                Registry sessionStrategyRegistry = RegistryBuilder.create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", new SSLIOSessionStrategy(this.sslContext)).build();
                IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).build();
                ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
                PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, sessionStrategyRegistry);
                connManager.setMaxTotal(this.maxConnections);
                connManager.setDefaultMaxPerRoute(this.maxConnectionsPerRoute);
                this.builder = HttpAsyncClients.custom().useSystemProperties().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).setConnectionManager(connManager);
            }
        }

    }

    public HttpUtil connections(int maxConnections) {
        this.maxConnections = maxConnections;
        this.maxConnectionsPerRoute = maxConnections;
        return this;
    }

    public HttpUtil useKey(String keyStorePath, String keyStorepass) {
        this.sslContext = this.ssl(keyStorePath, keyStorepass);
        this.ignoreVerifySsl = false;
        return this;
    }

    public HttpUtil baseAuth(String user, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM), new UsernamePasswordCredentials(user, password));
        this.builder.setDefaultCredentialsProvider(provider);
        return this;
    }

    public HttpUtil proxy(String hostOrIp, int port) {
        HttpHost proxy = new HttpHost(hostOrIp, port, "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        this.builder.setRoutePlanner(routePlanner);
        return this;
    }

    public HttpUtil start() {
        try {
            this.build();
            this.httpClient = this.builder.build();
            this.httpClient.start();
        } catch (KeyManagementException e) {
            log.error("key error", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("build key error", e);
        } catch (IOReactorException e) {
            log.error("io reactor thread error", e);
        }
        return this;
    }

    private SSLContext ssl(String keyStorePath, String keyStorepass) {
        SSLContext sc = null;
        FileInputStream fileInputStream = null;
        KeyStore trustStore;

        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            fileInputStream = new FileInputStream(new File(keyStorePath));
            trustStore.load(fileInputStream, keyStorepass.toCharArray());
            sc = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        return sc;
    }

    private SSLContext ignoreVerifySsl() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    public R asyncGet(String url, Map<String, String> headers, Map<String, Object> params, HttpCallback callback, int timeout) {
        return this.send(url, GET, headers, params, null, callback, timeout);
    }

    public R get(String url, Map<String, String> headers, Map<String, Object> params, int timeout) {
        return this.send(url, GET, headers, params, null, null, timeout);
    }

    public R asyncPost(String url, Map<String, String> headers, Map<String, Object> params, Object body, HttpCallback callback, int timeout) {
        return this.send(url, POST, headers, params, body, callback, timeout);
    }

    public R post(String url, Map<String, String> headers, Map<String, Object> params, Object body, int timeout) {
        return this.send(url, POST, headers, params, body, null, timeout);
    }

    public R asyncPatch(String url, Map<String, String> headers, Map<String, Object> params, Object body, HttpCallback callback, int timeout) {
        return this.send(url, PATCH, headers, params, body, callback, timeout);
    }

    public R patch(String url, Map<String, String> headers, Map<String, Object> params, Object body, int timeout) {
        return this.send(url, PATCH, headers, params, body, null, timeout);
    }

    public R asyncDelete(String url, Map<String, String> headers, Map<String, Object> params, Object body, HttpCallback callback, int timeout) {
        return this.send(url, DELETE, headers, params, body, callback, timeout);
    }

    public R delete(String url, Map<String, String> headers, Map<String, Object> params, Object body, int timeout) {
        return this.send(url, DELETE, headers, params, body, null, timeout);
    }

    public R asyncPut(String url, Map<String, String> headers, Map<String, Object> params, Object body, HttpCallback callback, int timeout) {
        return this.send(url, PUT, headers, params, body, callback, timeout);
    }

    public R put(String url, Map<String, String> headers, Map<String, Object> params, Object body, int timeout) {
        return this.send(url, PUT, headers, params, body, null, timeout);
    }

    private R send(String url, String method, Map<String, String> headers, Map<String, Object> params, Object body, HttpCallback callback, int timeout) {
        Asserts.notBlank(url, "url is required");
        Asserts.check(SUPPORT_METHOD.containsKey(method.toUpperCase()), "unsupported method [" + method + "]");
        if (!this.httpClient.isRunning()) {
            REENTRANT_LOCK.lock();

            try {
                if (!this.httpClient.isRunning()) {
                    this.start();
                }
            } finally {
                REENTRANT_LOCK.unlock();
            }
        }

        if (params != null) {
            String qs = params.entrySet().stream().map((entry) -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
            if (url.indexOf("?") > 0) {
                url = url + "&" + qs;
            } else {
                url = url + "?" + qs;
            }
        }

        String m = method.toUpperCase();
        byte httpType = -1;
        switch (m.hashCode()) {
            case 70454:
                if (GET.equals(m)) {
                    httpType = 0;
                }
                break;
            case 79599:
                if (PUT.equals(m)) {
                    httpType = 2;
                }
                break;
            case 2461856:
                if (POST.equals(m)) {
                    httpType = 1;
                }
                break;
            case 75900968:
                if (PATCH.equals(m)) {
                    httpType = 4;
                }
                break;
            case 2012838315:
                if (DELETE.equals(m)) {
                    httpType = 3;
                }
                break;
            default:
                break;
        }

        HttpRequestBase request;
        switch (httpType) {
            case 0:
                request = new HttpGet(url);
                break;
            case 1:
                request = new HttpPost(url);
                break;
            case 2:
                request = new HttpPut(url);
                break;
            case 3:
                request = new HttpDelete(url);
                break;
            case 4:
                request = new HttpPatch(url);
                break;
            default:
                throw new RuntimeException("unsupported method");
        }

        HttpEntity entity = null;
        if (body != null) {
            request.addHeader("Content-Type", "application/json");
            if (body instanceof String) {
                entity = new StringEntity(String.valueOf(body), "UTF-8");
            } else {
                entity = new StringEntity(JsonUtil.toJson(body), "UTF-8");
            }
        }


        if (!GET.equals(m)) {
            assert request instanceof HttpEntityEnclosingRequestBase;

            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = (HttpEntityEnclosingRequestBase) request;
            httpEntityEnclosingRequestBase.setEntity(entity);
        }

        if (headers != null) {
            headers.forEach(request::setHeader);
        }

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout > 0 ? timeout : this.socketTimeout).setConnectionRequestTimeout(this.connectRequestTimeout).setConnectTimeout(this.connectTimeout).build();
        request.setConfig(requestConfig);
        return this.send(request, callback);
    }

    public R send(HttpRequestBase request, HttpCallback callback) {
        R r = new R();
        r.setStatusCode(500);

        try {
            if (callback != null) {
                this.httpClient.execute(request, this.futureCallback(callback));
                return R.success(200, "invoke async, please see callback result!");
            }

            Future<HttpResponse> responseFuture = this.httpClient.execute(request, null);
            return this.convert(responseFuture.get(request.getConfig().getSocketTimeout() + request.getConfig().getConnectTimeout() + request.getConfig().getConnectionRequestTimeout(), TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            log.error("Interrupted exception", e);
        } catch (ExecutionException e) {
            log.error("invoke http request error({}), request line is:\n{}", e, request.getRequestLine());
            r.setStatusCode(-1);
            r.setStatusMsg(e.getMessage());
        } catch (TimeoutException e) {
            log.error("invoke http request timeout({}), request line is:\n{}", e, request.getRequestLine());
            r.setStatusCode(-1);
            r.setStatusMsg("wait response timeout");
        }

        return r;
    }

    private R convert(HttpResponse httpResponse) {
        R r = new R();

        try {
            r.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            r.setStatusMsg(httpResponse.getStatusLine().getReasonPhrase());
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                try {
                    InputStream inputStream = entity.getContent();
                    Throwable throwable = null;

                    try {
                        StringBuilder sb = new StringBuilder();
                        char[] tmp = new char[1024];
                        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

                        int l;
                        while ((l = reader.read(tmp)) != -1) {
                            sb.append(tmp, 0, l);
                        }

                        r.setResponseText(sb.toString());
                    } catch (Throwable readThrowable) {
                        throwable = readThrowable;
                        throw readThrowable;
                    } finally {
                        if (inputStream != null) {
                            if (throwable != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable e) {
                                    throwable.addSuppressed(e);
                                }
                            } else {
                                inputStream.close();
                            }
                        }

                    }
                } catch (Exception e) {
                    log.error("response error: {}", e.getMessage());
                } finally {
                    EntityUtils.consume(entity);
                }
            }
        } catch (IOException e) {
            log.error("error", e);
            r.setStatusCode(-1);
            r.setResponseText(e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }

        return r;
    }

    private FutureCallback<HttpResponse> futureCallback(final HttpCallback callback) {
        return callback == null ? null : new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                try {
                    R r = HttpUtil.this.convert(httpResponse);
                    callback.completed(r);
                } catch (Exception e) {
                    HttpUtil.log.error("http async invoke completed, but convert error", e);
                }

            }

            @Override
            public void failed(Exception e) {
                callback.failed(e);
            }

            @Override
            public void cancelled() {
                callback.cancelled();
            }
        };
    }
}
