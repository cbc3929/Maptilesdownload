package com.hykj.maptilesdownload;

import com.hykj.maptilesdownload.entity.Bounds;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MaptilesdownloadApplicationTests {
    @Test
    public static void main(String[] args) throws Exception {
        Bounds bounds = new Bounds("boxmulidiv");
        Integer boxtiles = 0;

        for (String[] b : bounds.getlistbounds()) {
            String[] ymin = null;
            String[] ymax = null;
            ymin = b[0].split(" ");
            ymax = b[2].split(" ");
            Integer TY = ThreadPoolExecutorDemo.lat2tile(Double.parseDouble(ymax[1])).intValue();
            Integer LX = ThreadPoolExecutorDemo.long2tile(Double.parseDouble(ymin[0])).intValue();
            Integer BY = ThreadPoolExecutorDemo.lat2tile(Double.parseDouble(ymin[1])).intValue();
            Integer RX = ThreadPoolExecutorDemo.long2tile(Double.parseDouble(ymax[0])).intValue();
            boxtiles = boxtiles + ((BY - TY) * (RX - LX));
        }
        Integer TY = ThreadPoolExecutorDemo.lat2tile(29.172041).intValue();
        Integer LX = ThreadPoolExecutorDemo.long2tile(100.060168).intValue();
        Integer BY = ThreadPoolExecutorDemo.lat2tile(27.677017).intValue();
        Integer RX = ThreadPoolExecutorDemo.long2tile(101.665999).intValue();
        Integer tiles = (BY - TY) * (RX - LX);
        System.out.println(boxtiles);
        System.out.println(tiles);
        Double rate = Double.parseDouble(boxtiles.toString()) / Double.parseDouble(tiles.toString());
        System.out.println(rate);
    }

}
