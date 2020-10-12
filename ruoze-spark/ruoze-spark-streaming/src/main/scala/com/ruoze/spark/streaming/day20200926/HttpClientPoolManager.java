package com.ruoze.spark.streaming.day20200926;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientPoolManager {

    public static PoolingHttpClientConnectionManager clientConnectionManager = null;

    private int maxTotal = 2;

    private int defaultMaxPerRoute = 2;

    private HttpClientPoolManager(int maxTotal, int defaultMaxPerRoute){
        this.maxTotal = maxTotal;
        this.defaultMaxPerRoute = defaultMaxPerRoute;
        clientConnectionManager.setMaxTotal(maxTotal);
        clientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }
    private HttpClientPoolManager(){
        clientConnectionManager.setMaxTotal(maxTotal);
        clientConnectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }

    private static HttpClientPoolManager poolManager = null;


    public synchronized static HttpClientPoolManager getInstance(){
        if(poolManager == null){
            clientConnectionManager = new PoolingHttpClientConnectionManager();
            poolManager = new HttpClientPoolManager();

        }

        return poolManager;
    }
    public synchronized static HttpClientPoolManager getInstance(int maxTotal, int defaultMaxPerRoute){
        if(poolManager == null){
            poolManager = new HttpClientPoolManager(maxTotal, defaultMaxPerRoute);
        }

        return poolManager;
    }

    public static CloseableHttpClient getHttpClient(){
        if(clientConnectionManager == null){
            clientConnectionManager = new PoolingHttpClientConnectionManager();
            getInstance();
        }

        return HttpClients.custom().setConnectionManager(clientConnectionManager).build();
    }

    public static PoolingHttpClientConnectionManager getClientConnectionManager(){
        if(poolManager == null){
//            clientConnectionManager = new PoolingHttpClientConnectionManager();
            getInstance();
        }
        return clientConnectionManager;
    }
}
