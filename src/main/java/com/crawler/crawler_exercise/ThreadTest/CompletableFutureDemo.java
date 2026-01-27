package com.crawler.crawler_exercise.ThreadTest;

import net.bytebuddy.asm.Advice;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        System.out.println("线程"+r.hashCode()+"创建");
                        //线程命名
                        Thread th = new Thread(r,"Eamon-threadPool"+r.hashCode());
                        return th;
                    }
                }, new ThreadPoolExecutor.CallerRunsPolicy());

        CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
        objectCompletableFuture.complete("new 的方式");
        boolean done = objectCompletableFuture.isDone();
        if(done){
            Object o = objectCompletableFuture.get();
            System.out.println(o);
        }

        CompletableFuture.runAsync(()->{
            System.out.println("这是在runAsync中打印的内容，因为它接收的是Runnable，所以没有返回值");
            System.out.println("ThreadName:"+Thread.currentThread().getName());
        },threadPoolExecutor);

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "hello!").thenApply(s -> s+"world");
        String s = completableFuture.get();
        System.out.println(s);

        String s1 = CompletableFuture.supplyAsync(() -> {
            if (true) {
                throw new RuntimeException("在异步中抛出一个异常!");
            }
            System.out.println("ThreadName:"+Thread.currentThread().getName());
            return "error";
        },threadPoolExecutor).handle((res, ex) -> {
            // res 代表返回的结果
            // ex 的类型为 Throwable ，代表抛出的异常
            System.out.println("多线程报错:"+ex);
            return res != null ? res : "world!";
        }).get();

        System.out.println(s1);

        // 关闭自定义线程池，如果没有这句，则在停机的时候spring来销毁
        threadPoolExecutor.shutdown();

        // 虚拟线程（JDK19+）
        Thread thread = Thread.startVirtualThread(() -> {
            System.out.println("虚拟线程");
        });

        ExecutorService virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<String> virtualCompletableFuture = CompletableFuture.supplyAsync(() -> {
            String res = "这里是CompletableFuture创建的虚拟线程";
//            System.out.println(res);
            return res;
        }, virtualThreadPool);
        String s2 = virtualCompletableFuture.get();
        System.out.println(s2);

    }

}
