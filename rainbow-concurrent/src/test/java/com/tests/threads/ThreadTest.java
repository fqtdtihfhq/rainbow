package com.tests.threads;

import cloud.tengyee.concurrent.threads.RecyclePool;
import org.junit.Test;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mr.赵
 * created on 2021/1/30
 */
public class ThreadTest {

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
        RecyclePool pool = new RecyclePool(40, 1);
        pool.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("进来了=====");
                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread.sleep(1);
        LinkedList<Runnable> noDo=pool.unDoShutdown();//已经执行的，继续执行，没下锅的不开始了并返回
        System.out.println("has shutdown");
        Thread.sleep(20000000);
    }
}
