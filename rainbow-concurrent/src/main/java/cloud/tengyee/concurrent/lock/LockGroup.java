package cloud.tengyee.concurrent.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 锁组对象
 * @author Mr.赵
 * created on 2020/10/21
 */
public class LockGroup {

    public Map<String, Lock> lockStock=new ConcurrentHashMap<>();

    /**
     * 产生锁
     * @param key
     * @return
     */
    public Lock getLock(String key){
            lockStock.putIfAbsent(key,new ReentrantLock());
            return lockStock.get(key);
    }
    /**
     * 产生锁
     * @param key
     * @return
     */
    public Lock getLock(Long key){
        String mykey=key+"";
            lockStock.putIfAbsent(mykey,new ReentrantLock());
            return lockStock.get(mykey);
    }

    /**
     * 销毁锁，确保不同时锁住
     * @param key
     */
    public void destoryLock(String key){
        String mykey=key+"";
            lockStock.remove(mykey);
    }
    /**
     * 销毁锁，确保不同时锁住
     * @param key
     */
    public void destoryLock(Long key){
        String mykey=key+"";
            lockStock.remove(mykey);
    }
}
