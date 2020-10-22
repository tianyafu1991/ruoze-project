package com.ruoze.spark.streaming.utils;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author PK哥
 **/
public class RedisUtils {

    private static JedisPool jedisPool = null;

    private static String host = "hadoop";
    private static int port = 16379;

    public static Jedis getJedis(){
        if(null == jedisPool) {
            GenericObjectPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(10);
            config.setMaxTotal(1000);
            config.setMaxWaitMillis(1000);
            config.setTestOnBorrow(true);
            jedisPool = new JedisPool(config, host, port);
        }

        return jedisPool.getResource();
    }

}
