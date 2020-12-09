package cloud.tengyee.concurrent.cache.redis;

import cloud.tengyee.concurrent.lock.LockGroup;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * redis作缓存
 * @author Mr.赵
 * created on 2020/12/5
 */
public class RedisCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private String cacheName;
    private JedisPool pool;
    private int timeout;

    private LockGroup lockGroup=new LockGroup();

    private static final String REDIS_KEY_HEAD="__rediscache_@system__%s@@%s";

    private synchronized Jedis getJedis() {
        return this.pool.getResource();
    }

    private synchronized void returnJedis(Jedis jedis) {
        if (jedis != null) {
            this.pool.returnResource(jedis);
        }
    }

    public RedisCache(String cacheName, JedisPool pool,int timeout) {
        this.cacheName = cacheName;
        this.pool = pool;
        this.timeout=timeout;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        System.out.println("getNativeCache");
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        Jedis jedis = this.getJedis();
        try {
            byte[] data=jedis.get(parseKey(key).getBytes());
            if (data == null) {
                return null;
            }
            return toValueWrapper(SerializeUtil.deserialize(data));
        } finally {
            this.returnJedis(jedis);
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return (T) get(key).get();
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        //查一下，有没有，没有的话，锁住
        ValueWrapper data=get(key);
        if (data != null) {
            return (T) data.get();
        }
        //解锁后，先查一下有没有，如果有了返回，没有了去调
        Lock lock=lockGroup.getLock(key.toString());
        lock.lock();
        try {
            data=get(key);
            if (data != null) {
                return (T)data.get();
            }
            T result= valueLoader.call();
            this.put(key,result);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        } finally {
            lock.unlock();
            lockGroup.destoryLock(key.toString());
        }
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        Jedis jedis = this.getJedis();
        boolean success;
        try {
            success= value != null && "OK".equals(jedis.setex(parseKey(key).getBytes(), timeout,SerializeUtil.serizlize(value)));
        } finally {
            this.returnJedis(jedis);
        }
        if (!success) {
            logger.debug(key+"创建缓存失败");
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper data=get(key);
        if (data == null) {
            put(key,value);
            data=toValueWrapper(value);
        }
        return data;
    }

    @Override
    public void evict(Object key) {
        Jedis jedis = this.getJedis();
        try {
            jedis.del(parseKey(key).getBytes());
        } finally {
            this.returnJedis(jedis);
        }
    }

    @Override
    public void clear() {
        Jedis jedis = this.getJedis();
        try {
            Set<byte[]> keys=jedis.keys(parseKey("*").getBytes());
            Iterator<byte[]> it=keys.iterator();
            while (it.hasNext()){
                byte[] key=it.next();
                jedis.del(key);
            }
        } finally {
            this.returnJedis(jedis);
        }
    }

    private ValueWrapper toValueWrapper(Object element) {
        return (element != null ? new SimpleValueWrapper(element) : null);
    }

    /**
     * 转化密钥
     * @param key 密钥
     * @return
     */
    private String parseKey(Object key){
        return String.format(REDIS_KEY_HEAD,cacheName,key);
    }
}
