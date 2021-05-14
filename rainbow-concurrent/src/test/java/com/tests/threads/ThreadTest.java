package com.tests.threads;

import cloud.tengyee.concurrent.threads.RecyclePool;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mr.赵
 * created on 2021/1/30
 */
public class ThreadTest {

    AtomicInteger x=new AtomicInteger(0);
    AtomicInteger y=new AtomicInteger(0);
    @Test
    public void t2(){
        Object obj=new Object();
        synchronized (obj){
            obj.notify();
        }
        synchronized (obj){
            obj.notify();
        }
        synchronized (obj){
            obj.notifyAll();
        }
    }

    @Test
    public void t1() throws InterruptedException {
        System.out.println(123);
//        ExecutorService pool= Executors.newFixedThreadPool(3);
        RecyclePool pool = new RecyclePool(2, 20);
//        ExecutorService pool=Executors.newFixedThreadPool(100);
        for(int i=0;i<30000;i++){
            final int j=i;
            Thread.sleep(200L);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(new Date());
                    y.incrementAndGet();
                    System.out.println(Thread.currentThread().getId()+" 进来了====="+j+" y:"+y.get());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
//        Thread.sleep(1);
//        LinkedList<Runnable> noDo=pool.unDoShutdown();//已经执行的，继续执行，没下锅的不开始了并返回
        System.out.println("has shutdown");
        Thread.sleep(20000000);
    }
}
