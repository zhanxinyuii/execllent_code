package com.etcp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {
    static final DelayQueue<DelayTask> queue = new DelayQueue<>();

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new put(queue));
            t.start();
        }
        // 在这里拿出延迟队列的延迟任务进行处理
        while (true) {
            try {
                    // take方法 ，若队列中无元素，等待 poll方法，若队列中无元素，返回null
                    DelayTask task = queue.take();
                    System.out.println("给用户" + task.name + "在"+ TimeUtil.getNow() +"发送邮件完毕");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class DelayTask implements Delayed {
    public String name;
    public Long delayTime;
    public TimeUnit delayTimeUnit;
    public Long executeTime;//ms

    DelayTask(String name, long delayTime, TimeUnit delayTimeUnit) {
        this.name = name;
        this.delayTime = delayTime;
        this.delayTimeUnit = delayTimeUnit;
        this.executeTime = System.currentTimeMillis() + delayTimeUnit.toMillis(delayTime);
    }


    @Override
    //队列中的元素根据到时时间排序
    public int compareTo(Delayed o) {
        if (this.getDelay(TimeUnit.MILLISECONDS) > o.getDelay(TimeUnit.MILLISECONDS)) {
            return 1;
        } else if (this.getDelay(TimeUnit.MILLISECONDS) < o.getDelay(TimeUnit.MILLISECONDS)) {
            return -1;
        }
        return 0;
    }

    @Override
    // 得到距离到时时间还有多久
    public long getDelay(TimeUnit unit) {
        return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

}

class put implements Runnable {

    DelayQueue<DelayTask> queue;

    public put(DelayQueue<DelayTask> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Random r = new Random();
        try {
            Thread.sleep(r.nextInt(10) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int thisTime = r.nextInt(100);

        System.out.println("用户 "+thisTime + " 在 " + TimeUtil.getNow() + " 完成注册");
        // 在这里进行延迟任务的入列
        queue.add(new DelayTask(thisTime + "", 5000L, TimeUnit.MILLISECONDS));
    }
}

class TimeUtil{

    public static String getNow(){
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String theTime = dateFormat.format(date);
        return theTime;
    }
}