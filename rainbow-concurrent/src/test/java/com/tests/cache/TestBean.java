package com.tests.cache;

import com.tests.beans.ReturnData;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;

/**
 * @author Mr.赵
 * created on 2020/12/5
 */
public class TestBean {

//    @TyLock(key="hahaha")
//    @TyCache(value = "cacheTest",key = "'hello'+#name")
    @Cacheable(value="cacheTest",key = "'hello'+#name",sync = true)
    public ReturnData hello(String name){
        System.out.println("my name is "+name);
        return new ReturnData("hello "+name);
    }

//    @CacheEvict(value = "cacheTest",key = "'hello'+#name")
    @CacheEvict(value = "cacheTest")
    public void clear(String name){}
//    @Cacheable(value="cacheTest")
}
