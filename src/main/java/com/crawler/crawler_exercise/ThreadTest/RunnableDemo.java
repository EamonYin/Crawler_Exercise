package com.crawler.crawler_exercise.ThreadTest;

public class RunnableDemo implements Runnable{
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("线程名字:"+threadName);
        try {
            for(int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                // 让线程睡眠一会
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("线程[结束]:"+threadName);
    }


    public static void main(String[] args) {
        RunnableDemo runnableDemo = new RunnableDemo();
        // 这里和【ThreadDemo】不同
        Thread t1 = new Thread(runnableDemo, "my-thread[1]");
        t1.start();

        RunnableDemo runnableDemo1 = new RunnableDemo();
        // 这里和【ThreadDemo】不同
        Thread t2 = new Thread(runnableDemo1, "my-thread[2]");
        t2.start();
    }
}
