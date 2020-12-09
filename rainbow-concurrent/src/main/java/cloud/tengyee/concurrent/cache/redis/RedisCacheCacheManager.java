package cloud.tengyee.concurrent.cache.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 扩充redis缓存
 * @author Mr.赵
 * created on 2020/12/5
 */
public class RedisCacheCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);

    private final Set<String> cacheNames=new HashSet<>();

    private String host;//redis链接
    private int port;//redis端口
    private String password;//redis密码
    private int maxActive=5;//redis最大活跃数
    private int maxIdle=10;//redis最大空闲数
    private int maxWait=6000;//redis最大等待时间

    private int timeout = 10000;
    private int database = 10;

    private JedisPool pool = null;

    private Resource configLocation;//json文件

    private JedisPool getJedisPool() {
        if (this.pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(this.maxActive);
            config.setMaxIdle(this.maxIdle);
            config.setMaxWaitMillis((long)this.maxWait);
            config.setTestOnBorrow(true);
            if ("".equals(this.password)) {
                this.password = null;
            }
            this.pool = new JedisPool(config, this.host, this.port, this.timeout, this.password, this.database);
            //初始化所有缓存
            if (configLocation != null) {
                try {
                    BufferedReader isr=new BufferedReader(new InputStreamReader(new FileInputStream(configLocation.getFile())));
                    StringBuffer sb=new StringBuffer();
                    String str;
                    while((str=isr.readLine())!=null){
                        sb.append(str);
                    }
                    JSONObject json=JSON.parseObject(sb.toString());
                    cacheNames.addAll(json.keySet());
                    Iterator<String> caches=cacheNames.iterator();
                    while (caches.hasNext()) {
                        String cacheName=caches.next();
                        cacheMap.putIfAbsent(cacheName,new RedisCache(cacheName,pool,json.getInteger(cacheName)));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(configLocation.getFilename()+" is not find");
                }
            }
        }
        return this.pool;
    }

    @Override
    public Cache getCache(String name) {
        if (this.pool == null) {
            this.pool=getJedisPool();
        }
        return cacheMap.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        if (this.pool == null) {
            this.pool=getJedisPool();
        }
        return this.cacheNames;
    }

//    private volatile Set<String> cacheNames = Collections.emptySet();


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }
}
