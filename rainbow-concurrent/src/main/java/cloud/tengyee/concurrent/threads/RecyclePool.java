package cloud.tengyee.concurrent.threads;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private LinkedList<Runnable> tasks=new LinkedList<>();//线程队列

    private final Object touliaoWait=new Object();//投料等待

    private Runnable touliao=new Runnable() {
        @Override
        public void run() {
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
        while (!service.isShutdown()&&onQueue.get()<=threadNum&&tasks.size()>0&&(task=tasks.pollFirst())!=null) {
            Runnable tuUseTask=task;
            service.submit(new Runnable() {
                    @Override
                    public void run() {
                        onQueue.addAndGet(1);
                        try {
                            tuUseTask.run();
                        } finally {
                            onQueue.addAndGet(-1);
                        }
                    }
                });
            usedQueue.addAndGet(1);
            if (usedQueue.get()>=recycleNum) {
                renew(threadNum);
            }
        }
        putService.submit(touliao);
    }

    public void renew(int threadNum){
        usedQueue=new AtomicInteger(0);
        if (service != null&&!service.isShutdown()) {
            service.shutdown();
        }
        service=Executors.newFixedThreadPool(threadNum);
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
        LinkedList<Runnable> oldList=tasks;
        tasks=new LinkedList<>();
        return oldList;
    }

    public int getRecycleNum() {
        return recycleNum;
    }

    public void setRecycleNum(int recycleNum) {
        this.recycleNum = recycleNum;
        if (this.recycleNum<1) {
            throw new RuntimeException("recycleNum 不能小于0");
        }
    }
}
