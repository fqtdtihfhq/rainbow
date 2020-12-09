# 1.0.0

### redis缓存功能
>基于spring-cache升级

#### 用法

* spring配置
```xml
    <!--命名为cacheManager，表示设置为默认缓存对象-->
    <bean id="cacheManager" class="cloud.tengyee.concurrent.cache.redis.RedisCacheCacheManager">
        <property name="host" value="192.168.1.195"/>
        <property name="port" value="6379" />
        <property name="password" value="123456"/>
        <property name="database" value="10"/>

        <property name="configLocation" value="classpath:/redisCache.json"/>
    </bean>
```
host、port、password、database为redis配置，分别为redis的地址、端口、密码、数据库编号

. json配置
```json
{
  "cacheTest": 100
}
```
其中"cacheTest"为缓存名，100为缓存时间，单位为秒。

. 注解的使用
```java
    @Cacheable(value="cacheTest",key = "'hello'+#name",sync = true)
    public ReturnData hello(String name){//注意返回对象ReturnData需要进行序列化，否则会报错
        System.out.println("my name is "+name);
        return new ReturnData("hello "+name);
    }

    @CacheEvict(value = "cacheTest")
    public void clear(String name){}
```
规则与原先缓存玩法一致，其中sync为是否支持多线程，设置为true的情况下，多线程同时进入时，按查-锁-查-执行的顺序走。

#### spring-cache知识点扫盲

. 当返回类型为void时，设置缓存永久无效。
. 默认缓存对象，名字必须为"cacheManager"，如果要设置多个cacheManager，需要使用其它名称，需要在@Cacheable注解里指定。例如：

```xml
    <bean id="ehCacheManagers" class="org.springframework.cache.ehcache.EhCacheManagerFactoryBean"
          p:shared="true"	>
        <property name="configLocation" value="classpath:ehcache.xml"/>
    </bean>
    <bean id="cacheManager" class="org.springframework.cache.ehcache.EhCacheCacheManager">
        <property name="cacheManager" ref="ehCacheManagers"/>
        <property name="transactionAware" value="true"/>
    </bean>
<!--假如需要同时使用ehcache，又要使用redisCache-->
    <bean id="redisCacheManager" class="cloud.tengyee.concurrent.cache.redis.RedisCacheCacheManager">
        <property name="host" value="192.168.1.195"/>
        <property name="port" value="6379" />
        <property name="password" value="123456"/>
        <property name="database" value="10"/>
        <property name="configLocation" value="classpath:/redisCache.json"/>
    </bean>
```

那么注解时需要有所体现
```java
    @Cacheable(value="cacheTest",cacheManager = "redisCacheManager",key = "'hello'+#name",sync = true)
    public ReturnData hello(String name){
        System.out.println("my name is "+name);
        return new ReturnData("hello "+name);
    }
```
