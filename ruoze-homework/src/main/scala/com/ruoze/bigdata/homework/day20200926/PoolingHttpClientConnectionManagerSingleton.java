package com.ruoze.bigdata.homework.day20200926;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class PoolingHttpClientConnectionManagerSingleton {

    private static PoolingHttpClientConnectionManager instance =new PoolingHttpClientConnectionManager();

    private int maxTotal = 10;
    private int defaultMaxPerRoute = 5;

    private PoolingHttpClientConnectionManagerSingleton(){
        instance.setMaxTotal(maxTotal);
        instance.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }
    public static PoolingHttpClientConnectionManager getInstance() {
        return instance;
    }
}
