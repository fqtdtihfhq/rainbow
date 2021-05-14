package cloud.tengyee.concurrent.threads;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可回收线程池
 * @author Mr.赵
 * created on 2021/1/30
 */
public class RecyclePool {

    private ExecutorService service;//核心线程服务
    private ExecutorService putService;//投料服务
    private int threadNum;//线程数
    private int recycleNum=10000;//回收的线程极限
    private AtomicInteger onQueue;
    private AtomicInteger usedQueue;
    private ConcurrentLinkedQueue<Runnable> tasks=new ConcurrentLinkedQueue<>();//线程队列

    private final Object touliaoWait=new Object();//投料等待

    private Runnable touliao=new Runnable() {
        @Override
        public void run() {
//            System.out.println(service);
            touliao();
        }
    };

    public RecyclePool(int threadNum) {
        this.threadNum = threadNum;
        init();
    }

    public RecyclePool(int threadNum, int recycleNum) {
        this.threadNum = threadNum;
        this.recycleNum = recycleNum;
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        renew(threadNum);
        if (this.recycleNum<1) {
            throw new RuntimeException("recycleNum 不能小于0");
        }
        putService=Executors.newSingleThreadExecutor();//自动投料
        putService.submit(touliao);
        onQueue=new AtomicInteger(0);//队列中的数目
    }
    /**
     * 给他投料
     */
    private void touliao(){
        //如果搞完了，wait等待notify通知
        if (tasks.size()==0) {
            synchronized (touliaoWait){
                try {
                    touliaoWait.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Runnable task=null;
        ThreadPoolExecutor tpe= (ThreadPoolExecutor) service;
        while (!service.isShutdown()&&tpe.getQueue().size()<3*threadNum&&tasks.size()>0&&(task=tasks.poll())!=null) {
//            System.out.println(tpe.getQueue().size());
            Runnable tuUseTask=task;
            final ExecutorService nowservice=service;
            service.submit(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println(new Date());
//                        System.out.println(nowservice.toString());
//                        System.out.println(nowservice.isShutdown());
//                        System.out.println(nowservice.isTerminated());
                        if (nowservice.isShutdown()) {
                            submit(tuUseTask);
                            return;
                        }
                        onQueue.addAndGet(1);
                        try {
                            tuUseTask.run();
                        } finally {
                            onQueue.addAndGet(-1);
                            usedQueue.addAndGet(1);
                            renew(threadNum);
                            synchronized (touliaoWait){
                                touliaoWait.notify();
                            }
                        }
                    }
                });

        }
        putService.submit(touliao);
    }

    public synchronized void renew(int threadNum){
        if (usedQueue!=null&&usedQueue.get()<recycleNum) {
            return;
        }
        usedQueue=new AtomicInteger(0);
        if (service != null&&!service.isShutdown()) {
            service.shutdown();
        }

//        System.out.println("renew");
        service=Executors.newFixedThreadPool(threadNum);

//        service=Executors.newSingleThreadExecutor();
    }

    public void submit(Runnable task){
        tasks.add(task);
        synchronized (touliaoWait){
            touliaoWait.notify();
        }
    }

    /**
     * 没执行的暂时不执行了
     */
    public LinkedList<Runnable> unDoShutdown(){
        ConcurrentLinkedQueue<Runnable> oldList=tasks;
        tasks=new ConcurrentLinkedQueue<>();
        return new LinkedList<>(oldList);
    }

    public int getRecycleNum() {
        return recycleNum;
    }

    /**
     * 取队列长度
     * @return 队列长度，正在执行的+还没执行的
     */
    public int getQueueNum(){
        return onQueue.get()+tasks.size();
    }

    public void setRecycleNum(int recycleNum) {
        this.recycleNum = recycleNum;
        if (this.recycleNum<1) {
            throw new RuntimeException("recycleNum 不能小于0");
        }
    }
}
