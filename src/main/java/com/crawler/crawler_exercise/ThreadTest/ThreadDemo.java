package com.crawler.crawler_exercise.ThreadTest;

public class ThreadDemo extends Thread{

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
        ThreadDemo threadDemo = new ThreadDemo();
        // 这里和【RunnableDemo】不同的地方在于，这里是直接调用start方法，而不是new Thread
        threadDemo.setName("测试线程1");
        threadDemo.start();

        ThreadDemo threadDemo1 = new ThreadDemo();
        // 这里和【RunnableDemo】不同的地方在于，这里是直接调用start方法，而不是new Thread
        threadDemo1.setName("测试线程2");
        threadDemo1.start();

    }


}
