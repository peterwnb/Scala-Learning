package com.today.common;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisServiceImpl extends RedisService{


    public static String set(String key, String value) {
        Jedis jedis = null;
        String rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.setex(key, EXPIRE, value);
        } catch (Exception e) {

            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }

    public static String set2(String key, String value) {
        Jedis jedis = null;
        String rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.setex(key, 360000, value);
        } catch (Exception e) {

            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }

    /**
     * Get the value of the specified key.
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        Jedis jedis = null;
        String rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.get(key);
        } catch (Exception e) {

            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }

    /**
     * Get the values of all the specified keys
     *
     * @param keys
     * @return
     */
    public static List<String> mget(String... keys) {
        Jedis jedis = null;
        List<String> rtn = new ArrayList<String>();
        try {
            jedis = getJedis();
            rtn = jedis.mget(keys);
        } catch (Exception e) {

            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }

    /**
     * Set the the respective keys to the respective values.
     *
     * @param keysvalues
     * @return
     */
    public static String mset(String... keysvalues) {
        Jedis jedis = null;
        String rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.mset(keysvalues);
        } catch (Exception e) {

            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }



    /**
     * 设置分布式锁
     *
     * @param key
     * @param value
     * @return
     */
    public static long setLock(String key, String value) {
        Jedis jedis = null;
        Long rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.setnx(key, value);
            jedis.expire(key, EXPIRE);
        } catch (Exception e) {
            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }

    /**
     * 释放锁
     *
     * @param key
     * @return
     */
    public static long delLock(String key) {
        Jedis jedis = null;
        Long rtn = null;
        try {
            jedis = getJedis();
            rtn = jedis.del(key);
        } catch (Exception e) {
            jedispool.returnBrokenResource(jedis);
        } finally {
            returnResource(jedispool, jedis);
        }
        return rtn;
    }


    public static void memoryCid(String dateKey,String cid){
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.sadd(dateKey, cid);
            jedis.expire(dateKey, EXPIRE);
        } catch (Exception e) {
            System.out.println(getTrace(e));
            jedispool.returnBrokenResource(jedis);
        }finally {
            returnResource(jedispool, jedis);
        }
    }

    /**
     * 获取调用链list
     * @param dateKey
     * @return
     */
    public static Set<String> getAllCids(String dateKey){
        Jedis jedis = null;
        Set<String> set = null;
        try {
            jedis = getJedis();
            set = jedis.smembers(dateKey);
        } catch (Exception e) {
            System.out.println(getTrace(e));
            jedispool.returnBrokenResource(jedis);
        }finally {
            returnResource(jedispool, jedis);
        }
        return set;
    }
}
