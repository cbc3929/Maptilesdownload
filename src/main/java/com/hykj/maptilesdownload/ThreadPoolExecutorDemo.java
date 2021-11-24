package com.hykj.maptilesdownload;

import com.hykj.maptilesdownload.entity.Bounds;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    private static final int CORE_POOL_SIZE = 30;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;

    public static void download() throws IOException {
        //使用阿里巴巴推荐的创建线程池的方式
        //通过ThreadPoolExecutor构造函数自定义参数创建
        Map map = GlobalConfig.globalConfig();

        if (map.get("proxyPort") != null) {
            System.setProperty("proxyType", "4");
            System.setProperty("proxyPort", map.get("proxyPort").toString());
            System.setProperty("proxyHost", map.get("proxyHost").toString());
            System.setProperty("proxySet", "true");
        }
        Bounds bounds = new Bounds();
        List<String[]> boxs = bounds.getlistbounds();

        for (String[] b : boxs) {
            String[] ymin = null;
            String[] ymax = null;
            ymin = b[0].split(" ");
            ymax = b[2].split(" ");


            Integer Process_num = Integer.valueOf(map.get("Process_num").toString());
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    Process_num,
                    Process_num,
                    KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                    new ThreadPoolExecutor.CallerRunsPolicy());
            for (int z = Integer.parseInt(map.get("minZoom").toString()); z < (Integer.parseInt(map.get("MaxZoom").toString())) + 1; z++) {
                Integer TY = lat2tile(Double.parseDouble(ymax[1]), z).intValue();
                Integer LX = long2tile(Double.parseDouble(ymin[0]), z).intValue();
                Integer BY = lat2tile(Double.parseDouble(ymin[1]), z).intValue();
                Integer RX = long2tile(Double.parseDouble(ymax[0]), z).intValue();
                for (int y = TY; y < BY; y++) {
                    for (int x = LX; x < RX; x++) {
                        String filepaths = map.get("outPath").toString();
                        String filepath = filepaths + "\\" + z + "\\" + x + "\\";
                        File file = new File(filepath + y + ".png");
                        if (!file.exists()) {
                            Runnable woker = new MyRunnable(x, y, z, map.get("outPath").toString());
                            executor.execute(woker);
                        } else System.out.println(filepath + y + ".png has exists");
                    }
                }
            }
            //终止线程池
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }
    }


    static Double long2tile(Double lon, Integer z) {
        return Math.floor((lon + 180) / 360 * Math.pow(2, z));
    }


    static Double lat2tile(Double lat, Integer z) {
        return Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, z));
    }


}
