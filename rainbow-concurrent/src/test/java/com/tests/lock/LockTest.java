package com.tests.lock;

import com.tests.beans.ReturnData;
import com.tests.cache.TestBean;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Mr.赵
 * created on 2020/12/9
 */
public class LockTest {
    @Test
    public void t1() throws InterruptedException {
        // 定义Spring配置文件的路径
        String xmlPath = "ApplicationContext.xml";
        // 初始化Spring容器，加载配置文件，并对bean进行实例化
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                xmlPath);
        // 通过容器获取id为person1的实例
        TestBean b=applicationContext.getBean("testBean",TestBean.class);
        ReturnData data=new ReturnData();
        data.setName("zhangsan");
        for(int i=0;i<10;i++){
            new Thread(){
                @Override
                public void run() {
                    System.out.println("开始轰");
                    b.lockTest("xiaoming"+Math.random(),data);
                }
            }.start();
        }
        System.out.println("结束");
        for(int i=0;i<10;i++){
            new Thread(){
                @Override
                public void run() {
                    System.out.println("开始炸");
                    b.lockTest("xiaoming",data);
                }
            }.start();
        }
        Thread.sleep(1000000000);
    }
}
