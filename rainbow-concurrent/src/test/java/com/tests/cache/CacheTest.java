package com.tests.cache;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author Mr.赵
 * created on 2020/12/5
 */
public class CacheTest {

//    @Test
    public void t0(){
        SimpleKeyGenerator simpleKeyGenerator=new SimpleKeyGenerator();
//        simpleKeyGenerator.generate()
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression =
                parser.parseExpression("('Hello' + ' World').concat(#end)");
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("end", "!");
        Assert.assertEquals("Hello World!", expression.getValue(context));
        CacheInterceptor t;

    }

//    @Test
    public void t1() throws InterruptedException {
        // 定义Spring配置文件的路径
        String xmlPath = "ApplicationContext.xml";
        // 初始化Spring容器，加载配置文件，并对bean进行实例化
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                xmlPath);
        // 通过容器获取id为person1的实例
        TestBean b=applicationContext.getBean("testBean",TestBean.class);
        for(int i=0;i<3;i++){
            int finalI = i;
            new Thread(){
                @Override
                public void run() {
                    b.hello("lilei");
                }
            }.start();
            Thread.sleep(1000);
        }
        b.clear("lilei");
        Thread.sleep(2000);
        for(int i=0;i<2;i++){
            int finalI = i;
            new Thread(){
                @Override
                public void run() {
                    b.hello("lilei");
                }
            }.start();
            Thread.sleep(1000);
        }
        System.out.println("结束");
        Thread.sleep(10000000);
    }
}
