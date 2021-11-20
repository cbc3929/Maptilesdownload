package com.hykj.maptilesdownload;

import com.hykj.maptilesdownload.entity.Bounds;

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
    private static final int CORE_POOL_SIZE = 500;
    private static final int MAX_POOL_SIZE = 500;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;
    private static final int zoom = 18;
//    private static final double[] bounds = {32.338796628406456, 106.60364921255916, 30.27032679274032, 108.60912358105259};

    public static void download() {
        //使用阿里巴巴推荐的创建线程池的方式
        //通过ThreadPoolExecutor构造函数自定义参数创建
        System.setProperty("proxyType", "4");
        System.setProperty("proxyPort", "10809");
        System.setProperty("proxyHost", "127.0.0.1");
        System.setProperty("proxySet", "true");

        Bounds bounds = new Bounds("boxmulidiv");
        List<String[]> boxs = bounds.getlistbounds();
        Integer tiles = 0;
        for (String[] b : boxs) {
            String[] ymin = null;
            String[] ymax = null;
            ymin = b[0].split(" ");
            ymax = b[2].split(" ");
            Integer TY = lat2tile(Double.parseDouble(ymax[1])).intValue();
            Integer LX = long2tile(Double.parseDouble(ymin[0])).intValue();
            Integer BY = lat2tile(Double.parseDouble(ymin[1])).intValue();
            Integer RX = long2tile(Double.parseDouble(ymax[0])).intValue();

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            int miss = RX - LX;
            List<Integer> listx = new ArrayList<>();

            Random random = new Random();
            Long starttime = System.currentTimeMillis();
            int a = 0;
            for (int y = TY; y < BY; y++) {
                for (int x = LX; x < RX; x++) {
                    String filepath = "f:\\ditu\\muli\\" + zoom + "\\" + x + "\\";
                    File file = new File(filepath + y + ".png");
                    if (!file.exists()) {
                        Runnable woker = new MyRunnable(x, y, zoom);

                        executor.execute(woker);

                    } else System.out.println(filepath + y + ".png has exists");
                }
            }
            //终止线程池
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            Long endtime = System.currentTimeMillis();
//        System.out.println("Finished all threads,download cont="+aaa);
//            System.out.println("总耗时：" + (endtime - starttime) / 1000 + "秒");
            tiles = tiles + ((BY - TY) * (RX - LX));
            System.out.println(tiles);
        }
    }


    static Double long2tile(Double lon) {
        return Math.floor((lon + 180) / 360 * Math.pow(2, ThreadPoolExecutorDemo.zoom));
    }


    static Double lat2tile(Double lat) {
        return Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, ThreadPoolExecutorDemo.zoom));
    }


}
