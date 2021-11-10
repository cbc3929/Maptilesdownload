package com.hykj.maptilesdownload;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @BelongProject:gassplatform
 * @BelongPackage:com.xhgis
 * @Author:Administrator
 * @CreateTime:2021-10-28-10-47
 * @Description:cc
 */
public class ThreadPoolExecutorDemo {
    private static final int CORE_POOL_SIZE = 5500;
    private static final int MAX_POOL_SIZE = 5500;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;
    private static final int zoom = 19;
    private static final double bounds[] = {32.338796628406456, 106.60364921255916, 30.27032679274032, 108.60912358105259};

    public static void download() throws InterruptedException {
    //使用阿里巴巴推荐的创建线程池的方式
    //通过ThreadPoolExecutor构造函数自定义参数创建
        System.setProperty("proxyType", "4");
        System.setProperty("proxyPort", "10809");
        System.setProperty("proxyHost", "127.0.0.1");
        System.setProperty("proxySet", "true");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
        Integer TY = lat2tile(bounds[0],zoom).intValue();
        Integer LX = long2tile(bounds[1],zoom).intValue();
        Integer BY = lat2tile(bounds[2],zoom).intValue();
        Integer RX = long2tile(bounds[3],zoom).intValue();

        int miss = RX - LX;
        List<Integer> listx = new ArrayList<>();

        Random random = new Random();
        Long starttime = System.currentTimeMillis();
        int a = 0;
        for(int y = TY; y<BY; y++){
            for (int x = LX; x <RX ; x++) {
               String filepath = "d:\\ditu\\dazhou\\" + zoom + "\\" + x + "\\";
               File file = new File(filepath + y + ".png");
               if (!file.exists()) {
                   Runnable woker = new MyRunnable(x, y, zoom);
                   executor.execute(woker);

               }else System.out.println(filepath+y+".png has exists");
            }
        }
        //终止线程池
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        Long endtime = System.currentTimeMillis();
//        System.out.println("Finished all threads,download cont="+aaa);
        System.out.println("总耗时："+(endtime-starttime)/1000 +"秒");

    }



    private static Double long2tile(Double lon, Integer zoom){
        return Math.floor((lon + 180) / 360 * Math.pow(2, zoom));
    }


    private static Double lat2tile(Double lat, Integer zoom){
        return Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2,zoom));
    }
}
