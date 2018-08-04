package com.today.common;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

public class RedisService {


    protected static JedisPool jedispool;
    protected static int EXPIRE = 130;
    static{
        ResourceBundle bundle = ResourceBundle.getBundle("redis");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[redis.properties] is not found!");
        }

        EXPIRE = Integer.valueOf(bundle.getString("redis.expire"));

        JedisPoolConfig jedisconfig = new JedisPoolConfig();
        jedisconfig.setMaxTotal(600);
        jedisconfig.setMaxIdle(300);
        jedisconfig.setMaxWaitMillis(1000);
        jedisconfig.setTestOnBorrow(true);

        jedispool = new JedisPool(jedisconfig, bundle.getString("127.0.0.1"),
                Integer.valueOf(bundle.getString("6379")), 100000);

    }

    public static Jedis getJedis() {
        Jedis jedis = null;
        try {
             jedis = new Jedis("127.0.0.1",6379);
            jedis.auth("test123");
            System.out.println("Connection to server sucessfully");
        } catch (JedisConnectionException jce) {
            getTrace(jce);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                getTrace(e);
            }
            jedis = jedispool.getResource();
        }
        return jedis;
    }

    public static void returnResource(JedisPool pool, Jedis jedis) {
        if (jedis != null) {
            pool.returnResource(jedis);
        }
    }

    public static String getTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        throwable.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }
}
