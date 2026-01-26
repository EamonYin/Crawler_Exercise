package com.crawler.crawler_exercise.ThreadTest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableFutureDemo implements Callable {

    private  static int n = 0;

    @Override
    public Object call() throws Exception {
        String name = Thread.currentThread().getName();
        for (int i = 5; i > 0; i--) {
            System.out.println("Thread: " + name + ", " + i);
            n++;
            // 让线程睡眠一会
            Thread.sleep(3);
        }
//        System.out.println("线程名字:"+name);
        return "线程名字:"+name;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CallableFutureDemo callableFutureDemo = new CallableFutureDemo();
        FutureTask futureTask = new FutureTask<>(callableFutureDemo);
        new Thread(futureTask).start();
        /**
         * 这里如果不调用get方法，会导致主线程直接结束，而子线程还没有执行完毕
         * get() 相当于 join()
         */
//        System.out.println(futureTask.get());

        CallableFutureDemo callableFutureDemo2 = new CallableFutureDemo();
        FutureTask futureTask2 = new FutureTask<>(callableFutureDemo2);
        new Thread(futureTask2).start();
        /**
         * 这里如果不调用get方法，会导致主线程直接结束，而子线程还没有执行完毕
         * get() 相当于 join()
         */
//        System.out.println(futureTask2.get());

        /**
         * 这句打印语句是在[主线程]上的
         *
         * 当不调用get方法时，主线程会直接结束，而子线程还没有执行完毕
         * 所以输出的n值是0，且肯定不是10
         */
        System.out.println("最后 n="+n);
    }
}
