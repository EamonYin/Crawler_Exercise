package com.crawler.crawler_exercise.ThreadTest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableFutureDemo implements Callable {

    @Override
    public Object call() throws Exception {
        String name = Thread.currentThread().getName();
//        System.out.println("线程名字:"+name);
        return "线程名字:"+name;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CallableFutureDemo callableFutureDemo = new CallableFutureDemo();
        FutureTask futureTask = new FutureTask<>(callableFutureDemo);
        new Thread(futureTask).start();
        System.out.println(futureTask.get());

        CallableFutureDemo callableFutureDemo2 = new CallableFutureDemo();
        FutureTask futureTask2 = new FutureTask<>(callableFutureDemo2);
        new Thread(futureTask2).start();
        System.out.println(futureTask2.get());
    }
}
